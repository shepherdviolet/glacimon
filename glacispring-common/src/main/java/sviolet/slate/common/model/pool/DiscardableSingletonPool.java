/*
 * Copyright (C) 2015-2022 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.model.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.concurrent.ConcurrentUtils;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

/**
 * <p>单例对象池 [线程安全|Thread-Safe]</p>
 * <p></p>
 * <p>[1.简介]</p>
 * <p>1.1.维护单例对象, 在获取(getInstanceProvider)时创建对象(懒加载). </p>
 * <p>1.2.支持安全地丢弃(销毁)对象: 丢弃(discard)的同时不影响获取(会创建新的), 对象会在使用完毕后(引用计数为0)销毁(destroy). </p>
 * <p>1.3.支持丢弃全部对象/指定对象. </p>
 * <p></p>
 * <p>[2.注意!!]</p>
 * <p>2.1.请仔细阅读说明, 确保正确地使用它. </p>
 * <p>2.2.对象池返回的是提供者InstanceProvider, 对象每次使用完毕后, 都必须调用InstanceProvider#close方法释放引用, 否则对象在
 *        被丢弃(discard)后不会被销毁(destroy). </p>
 * <pre>
 *
 *      // 示例1: 用try-with-resource写法保证在使用完毕后释放引用
 *      try (DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam)){
 *          Foo foo = instanceProvider.getInstance();
 *          foo.invoke(input);
 *      }
 *      
 *      // 示例2: 普通写法, 在使用完毕后释放引用
 *      DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam);
 *      try {
 *          Foo foo = instanceProvider.getInstance();
 *          foo.invoke(input);
 *      } finally {
 *          instanceProvider.close();
 *      }
 *
 * </pre>
 * <p></p>
 * <p>[3.使用示例]</p>
 * <p>3.1.详见: slate-common/src/test/java/sviolet/slate/common/model/pool/DiscardableSingletonPoolSampleService</p>
 * <p></p>
 * <p>[4.工作机制: 获取对象]</p>
 * <p>4.1.获取对象(getInstanceProvider)时, 先从"对象池"获取, 若存在, 则直接返回, 若不存在, 则调用InstanceManager#createInstance创建对象.
 *        锁保证同时只会创建一个实例. </p>
 * <p>4.2.获取对象后, 对象的引用计数+1, 这使得它不会被销毁(destroy). </p>
 * <p>4.3.对象使用完毕后, 调用InstanceProvider#close方法释放引用, 引用计数-1 </p>
 * <p></p>
 * <p>[5.工作机制: 丢弃对象]</p>
 * <p>5.1.丢弃对象(discard)时, 将对象从"对象池"移除, 随后加入"丢弃队列". 此时, 对象并没有被销毁(destroy), 只是无法从"对象池"获取
 *        (getInstanceProvider将返回新实例). </p>
 * <p></p>
 * <p>[6.工作机制: 销毁对象]</p>
 * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
 * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
 * <p>6.3.销毁器执行流程: </p>
 * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
 * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
 * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
 * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
 * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
 * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
 * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
 * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
 *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
 *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
 *
 * @author shepherdviolet
 */
public class DiscardableSingletonPool<InstanceType, CreateParamType> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 对象管理器
    private final InstanceManager<InstanceType, CreateParamType> instanceManager;

    // [参数] 强制销毁时间, 如果设置了这个参数, 对象在被丢弃后超过指定时间, 即使 "引用计数大于0" 也会被强制销毁
    private long forceDestroyDiscardedInstancesAfterMillis = Long.MAX_VALUE;

    // 对象池
    private final ConcurrentHashMap<String, InstanceHolder<InstanceType>> instances;
    // 对象创建锁
    private final ConcurrentHashMap<String, ReentrantLock> createLocks;

    // 执行异步销毁
    private final ExecutorService destroyExecutor = ThreadPoolExecutorUtils.createLazy(60, "Slate-DiscardableSingletonPool-destroyer-%d");
    // 丢弃队列
    private final BlockingQueue<InstanceHolder<InstanceType>> discardingInstances = new LinkedBlockingQueue<>();
    // 丢弃池
    private List<InstanceHolder<InstanceType>> discardedInstances = new ArrayList<>();

    // 统计信息
    private final StatisticInfo statisticInfo = new StatisticInfo();

    /**
     * @param instanceManager 对象管理器: 创建/销毁对象
     */
    public DiscardableSingletonPool(InstanceManager<InstanceType, CreateParamType> instanceManager) {
        this(instanceManager, 16);
    }

    /**
     * @param instanceManager 对象管理器: 创建/销毁对象
     * @param initialCapacity 对象池初始容量
     */
    public DiscardableSingletonPool(InstanceManager<InstanceType, CreateParamType> instanceManager, int initialCapacity) {
        if (instanceManager == null) {
            throw new IllegalArgumentException("DiscardableSingletonPool | instanceManager is null");
        }

        this.instanceManager = instanceManager;
        this.instances = new ConcurrentHashMap<>(initialCapacity);
        this.createLocks = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * <p>获取对象, 不存在则创建. </p>
     * <p>对象每次使用完毕后, 都必须调用InstanceProvider#close方法释放引用, 否则对象在被丢弃(discard)后不会被销毁(destroy).</p>
     *
     * <pre>
     *
     *      // 示例1: 用try-with-resource写法保证在使用完毕后释放引用
     *      try (DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam)){
     *          Foo foo = instanceProvider.getInstance();
     *          foo.invoke(input);
     *      }
     *
     *      // 示例2: 普通写法, 在使用完毕后释放引用
     *      DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam);
     *      try {
     *          Foo foo = instanceProvider.getInstance();
     *          foo.invoke(input);
     *      } finally {
     *          instanceProvider.close();
     *      }
     *
     * </pre>
     *
     * @param key 对象名称
     * @return 获取对象实例: InstanceProvider#getInstance, 释放引用: InstanceProvider#close
     * @exception InstanceCreateException 对象创建失败
     */
    public final InstanceProvider<InstanceType> getInstanceProvider(String key) {
        return getInstanceProvider(key, null);
    }

    /**
     * <p>获取对象, 不存在则创建. </p>
     * <p>对象每次使用完毕后, 都必须调用InstanceProvider#close方法释放引用, 否则对象在被丢弃(discard)后不会被销毁(destroy).</p>
     *
     * <pre>
     *
     *      // 示例1: 用try-with-resource写法保证在使用完毕后释放引用
     *      try (DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam)){
     *          Foo foo = instanceProvider.getInstance();
     *          foo.invoke(input);
     *      }
     *
     *      // 示例2: 普通写法, 在使用完毕后释放引用
     *      DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam);
     *      try {
     *          Foo foo = instanceProvider.getInstance();
     *          foo.invoke(input);
     *      } finally {
     *          instanceProvider.close();
     *      }
     *
     * </pre>
     *
     * @param key 对象名称
     * @param createParams 对象创建参数, 在对象不存在需要创建时传递给InstanceManager#createInstance
     * @return 获取对象实例: InstanceProvider#getInstance, 释放引用: InstanceProvider#close
     * @exception InstanceCreateException 对象创建失败
     */
    public InstanceProvider<InstanceType> getInstanceProvider(String key, CreateParamType createParams) {
        // check
        if (key == null) {
            throw new IllegalArgumentException("DiscardableSingletonPool | key is null");
        }

        boolean newInstance = false;

        // 从对象池获取, 不存在则尝试创建
        InstanceHolder<InstanceType> instanceHolder = instances.get(key);
        if (instanceHolder == null) {
            // 获取对象创建锁, 保证对象不重复创建
            ReentrantLock createLock = createLocks.computeIfAbsent(key, k -> new ReentrantLock());
            createLock.lock();
            try {
                // 再次从对象池获取, 不存在则尝试创建
                instanceHolder = instances.get(key);
                if (instanceHolder == null) {
                    // 调用对象管理器创建对象, 传入创建参数
                    InstanceType instance = instanceManager.createInstance(key, createParams);
                    // 包装对象
                    instanceHolder = new InstanceHolder<>(this, key, instance);
                    // 加入对象池
                    instances.put(key, instanceHolder);
                    // mark
                    newInstance = true;
                }
            } catch (Throwable t) {
                statisticInfo.createFailedCount.getAndIncrement();
                throw new InstanceCreateException("DiscardableSingletonPool | Create instance failed", t);
            } finally {
                createLock.unlock();
            }
        }

        if (newInstance) {
            statisticInfo.createCount.getAndIncrement();
            if (logger.isInfoEnabled()) {
                logger.info("DiscardableSingletonPool | Create-New-Instance: " + instanceHolder);
            }
        }

        // 返回对象提供者, 供获取对象实例, 在使用完毕后调用InstanceProvider#close释放引用
        return new InstanceProvider<>(instanceHolder);
    }

    /**
     * <p>丢弃指定名称的对象, 它将在被使用完毕(引用计数为0)后销毁.</p>
     * <p></p>
     * <p>[5.工作机制: 丢弃对象]</p>
     * <p>5.1.丢弃对象(discard)时, 将对象从"对象池"移除, 随后加入"丢弃队列". 此时, 对象并没有被销毁(destroy), 只是无法从"对象池"获取
     *        (getInstanceProvider将返回新实例). </p>
     * <p></p>
     * <p>[6.工作机制: 销毁对象]</p>
     * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
     * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
     * <p>6.3.销毁器执行流程: </p>
     * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
     * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
     * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
     * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
     * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     *
     * @param key 对象名称
     */
    public DiscardableSingletonPool<InstanceType, CreateParamType> discard(String key) {
        if (key == null) {
            throw new IllegalArgumentException("DiscardableSingletonPool | key is null");
        }

        InstanceHolder<InstanceType> instanceHolder = instances.get(key);
        if (instanceHolder != null) {
            instanceHolder.discard();
        }

        return this;
    }

    /**
     * <p>丢弃指定的对象, 它们将在被使用完毕(引用计数为0)后销毁.</p>
     * <p></p>
     * <p>[5.工作机制: 丢弃对象]</p>
     * <p>5.1.丢弃对象(discard)时, 将对象从"对象池"移除, 随后加入"丢弃队列". 此时, 对象并没有被销毁(destroy), 只是无法从"对象池"获取
     *        (getInstanceProvider将返回新实例). </p>
     * <p></p>
     * <p>[6.工作机制: 销毁对象]</p>
     * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
     * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
     * <p>6.3.销毁器执行流程: </p>
     * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
     * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
     * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
     * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
     * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     *
     * @param discardOrNot 根据对象实例和对象信息, 判断是否要丢弃对象, 返回true丢弃, 返回false不丢弃
     */
    public DiscardableSingletonPool<InstanceType, CreateParamType> discard(BiFunction<InstanceType, InstanceInfo, Boolean> discardOrNot) {
        // 复制镜像
        Map<String, InstanceHolder<InstanceType>> instances = ConcurrentUtils.getSnapShot(this.instances);
        if (instances == null) {
            return this;
        }

        // 遍历
        for (InstanceHolder<InstanceType> instance : instances.values()) {
            // 判断是否要丢弃
            if (discardOrNot.apply(instance.getInstance(), instance.getInfo())) {
                instance.discard();
            }
        }

        return this;
    }

    /**
     * <p>丢弃当前所有的对象, 它们将在被使用完毕(引用计数为0)后销毁.</p>
     * <p></p>
     * <p>[5.工作机制: 丢弃对象]</p>
     * <p>5.1.丢弃对象(discard)时, 将对象从"对象池"移除, 随后加入"丢弃队列". 此时, 对象并没有被销毁(destroy), 只是无法从"对象池"获取
     *        (getInstanceProvider将返回新实例). </p>
     * <p></p>
     * <p>[6.工作机制: 销毁对象]</p>
     * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
     * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
     * <p>6.3.销毁器执行流程: </p>
     * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
     * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
     * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
     * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
     * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     */
    public DiscardableSingletonPool<InstanceType, CreateParamType> discardAll() {
        return discard((instance, info) -> true);
    }

    /**
     * <p>通知销毁器, 尝试销毁被丢弃的对象, 异步执行.</p>
     * <p></p>
     * <p>[6.工作机制: 销毁对象]</p>
     * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
     * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
     * <p>6.3.销毁器执行流程: </p>
     * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
     * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
     * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
     * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
     * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     */
    public DiscardableSingletonPool<InstanceType, CreateParamType> notifyDestroyDiscardedInstances() {
        destroyExecutor.execute(destroyer);
        return this;
    }

    /**
     * 当前维护的实例数 (不含被丢弃的)
     */
    public int getInstancesNum() {
        return instances.size();
    }

    /**
     * "丢弃池"中暂未被销毁的对象数, 不包括"丢弃队列"中的, 这个只能用来大致查看堆积情况
     */
    public int getDiscardedInstancesNum() {
        return discardedInstances.size();
    }

    /**
     * 其他统计信息
     */
    public StatisticInfo getStatisticInfo() {
        return statisticInfo;
    }

    /**
     * <p>设置强制销毁时间</p>
     * <p></p>
     * <p>正常情况下, 被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁. 设置了这个参数后, "销毁器"在判断对象是否可以销毁时,
     * 追加了一种情况, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象, 无视对象的引用计数情况.
     * 注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     * 也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     * 方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     * <p></p>
     * <pre>
     *     // 设置强制销毁时间为10分钟
     *     <code>@Value("${force-destroy-discarded-instances-after-millis:600000}")</code>
     *     public void setForceDestroyDiscardedInstancesAfterMillis(long forceDestroyDiscardedInstancesAfterMillis){
     *         discardableSingletonPool.setForceDestroyDiscardedInstancesAfterMillis(forceDestroyDiscardedInstancesAfterMillis);
     *     }
     *     // 定时, 每隔10分钟通知"销毁器"执行销毁, 这样即使没人触发丢弃(discard)也没有被丢弃的对象引用计数归0, "到期"的对象依然会被强制销毁了
     *     <code>@Scheduled(fixedRateString = "${notify-destroy-discarded-instances-period-millis:600000}")</code>
     *     public void notifyDestroyer(){
     *         discardableSingletonPool.notifyDestroyDiscardedInstances();
     *     }
     * </pre>
     * <p></p>
     * <p>[6.工作机制: 销毁对象]</p>
     * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
     * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
     * <p>6.3.销毁器执行流程: </p>
     * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
     * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
     * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
     * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
     * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     *
     * @param forceDestroyAfterMillis 设置强制销毁时间, 单位毫秒(ms), 默认: Long.MAX_VALUE (默认不强制销毁)
     */
    public DiscardableSingletonPool<InstanceType, CreateParamType> setForceDestroyDiscardedInstancesAfterMillis(long forceDestroyAfterMillis) {
        this.forceDestroyDiscardedInstancesAfterMillis = forceDestroyAfterMillis;
        if (logger.isInfoEnabled()) {
            logger.info("DiscardableSingletonPool | setForceDestroyDiscardedInstancesAfterMillis: " + forceDestroyAfterMillis);
        }
        return this;
    }

    @Override
    public String toString() {
        return "DiscardableSingletonPool{" +
                "instancesNum=" + getInstancesNum() +
                ", discardedInstancesNum=" + getDiscardedInstancesNum() +
                ", forceDestroyDiscardedInstancesAfterMillis=" + forceDestroyDiscardedInstancesAfterMillis +
                ", statisticInfo=" + statisticInfo +
                '}';
    }

    /**
     * 执行丢弃
     */
    private void performDiscard(InstanceHolder<InstanceType> instance) {
        // 从对象池移除
        if (instances.remove(instance.getKey(), instance)) {
            // 加入丢弃队列
            // noinspection ResultOfMethodCallIgnored
            discardingInstances.offer(instance);
            // 通知销毁器执行销毁
            notifyDestroyDiscardedInstances();
            // log
            if (logger.isInfoEnabled()) {
                logger.info("DiscardableSingletonPool | Discard-Instance: " + instance);
            }
        }
    }

    /**
     * <p>销毁器, 异步执行</p>
     * <p></p>
     * <p>[6.工作机制: 销毁对象]</p>
     * <p>6.1.被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁, 销毁过程异步执行. </p>
     * <p>6.2.三种情况会触发"销毁器"执行销毁流程: 对象被丢弃(discard), 被丢弃的对象引用计数归0, 手动触发(DiscardableSingletonPool#notifyDestroyDiscardedInstances). </p>
     * <p>6.3.销毁器执行流程: </p>
     * <p>----6.3.1.遍历"丢弃池"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.2.遍历"丢弃队列"中的对象, 若 "引用计数为0" 则销毁. 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.3.3.本轮未被销毁的对象, 会加入丢弃池, 等待下一轮 (等待触发). </p>
     * <p>6.4.关于参数 "强制销毁时间 forceDestroyDiscardedInstancesAfterMillis": </p>
     * <p>----6.4.1.该参数默认为Long.MAX_VALUE, 默认不强制销毁. </p>
     * <p>----6.4.2.如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一. </p>
     * <p>----6.4.3.如果设置了这个参数, 在"销毁器"执行流程中, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象. </p>
     * <p>----6.4.4.注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     *              也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了. 所以, 建议配套一个定时器, 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances
     *              方法, 唤醒"销毁器"将"已到期"的对象强制销毁. </p>
     */
    private final Runnable destroyer = new Runnable() {

        @Override
        public void run() {
            statisticInfo.destroyerRunCount++;

            //log
            if (logger.isTraceEnabled()) {
                logger.trace("DiscardableSingletonPool | destroyer | Start");
            }

            // 新的丢弃池, 存放本轮未被销毁的对象
            List<InstanceHolder<InstanceType>> newDiscardedInstances = new ArrayList<>();

            // 遍历丢弃池, 尝试销毁
            for (InstanceHolder<InstanceType> instanceHolder : discardedInstances) {
                tryDestroy(newDiscardedInstances, instanceHolder);
            }

            // 遍历丢弃队列, 尝试销毁
            InstanceHolder<InstanceType> instanceHolder;
            while ((instanceHolder = discardingInstances.poll()) != null) {
                statisticInfo.discardCount++;
                tryDestroy(newDiscardedInstances, instanceHolder);
            }

            // 未被销毁的, 存入丢弃池
            discardedInstances = newDiscardedInstances;

            if (logger.isTraceEnabled()) {
                logger.trace("DiscardableSingletonPool | destroyer | End");
            }

        }

        private void tryDestroy(List<InstanceHolder<InstanceType>> newDiscardedInstances,
                                InstanceHolder<InstanceType> instanceHolder) {

            if (logger.isTraceEnabled()) {
                logger.trace("DiscardableSingletonPool | destroyer | Check-Discarded-Instance: " + instanceHolder);
            }

            // 判断是否能被销毁
            if (instanceHolder.canDestroy(forceDestroyDiscardedInstancesAfterMillis)) {
                if (instanceHolder.getRefCount() > 0) {
                    if (logger.isInfoEnabled()) {
                        logger.info("DiscardableSingletonPool | destroyer | Destroy-Discarded-Instance (Force): " + instanceHolder);
                    }
                    statisticInfo.destroyForceCount++;
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("DiscardableSingletonPool | destroyer | Destroy-Discarded-Instance: " + instanceHolder);
                    }
                }

                // 销毁对象
                try {
                    statisticInfo.destroyCount++;
                    instanceManager.destroyInstance(instanceHolder.getInfo(), instanceHolder.getInstance());
                } catch (Throwable t) {
                    statisticInfo.destroyFailedCount++;
                    // log
                    if (logger.isErrorEnabled()) {
                        logger.error("DiscardableSingletonPool | destroyer | Error when destroy instance: " + instanceHolder, t);
                    }
                    // 销毁失败处理, 一般就打日志
                    try {
                        instanceManager.onInstanceDestroyError(instanceHolder.getInfo(), instanceHolder.getInstance(), t);
                    } catch (Throwable t2) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("DiscardableSingletonPool | destroyer | Uncaught exception from instanceManager#onInstanceDestroyError, instance info: " + instanceHolder, t2);
                        }
                    }
                }
            } else {
                // 不能销毁的对象加入新的丢弃池
                newDiscardedInstances.add(instanceHolder);
            }

        }

    };

    /**
     * 单例对象包装
     */
    private static class InstanceHolder<InstanceType> {

        // 弱引用对象池
        private final WeakReference<DiscardableSingletonPool<InstanceType, ?>> pool;

        // 信息
        private final InstanceInfo info;
        // 实例
        private final InstanceType instance;

        // 是否被丢弃
        private final AtomicBoolean discarded = new AtomicBoolean(false);
        // 引用计数
        private final AtomicInteger refCount = new AtomicInteger(0);

        private InstanceHolder(DiscardableSingletonPool<InstanceType, ?> pool, String key, InstanceType instance) {
            this.pool = new WeakReference<>(pool);
            this.info = new InstanceInfo(key);
            this.instance = instance;
        }

        /**
         * 对象名称
         */
        private String getKey() {
            return info.getKey();
        }

        /**
         * 对象信息
         */
        private InstanceInfo getInfo() {
            return info;
        }

        /**
         * 对象实例
         */
        private InstanceType getInstance() {
            return instance;
        }

        /**
         * 引用计数+1, 更新最后使用时间
         */
        private void acquireReference() {
            refCount.getAndIncrement();
            info.setLastUsedTimeMillis(System.currentTimeMillis());
        }

        /**
         * 引用计数-1, 若已被丢弃且计数为0, 通知销毁器
         */
        private void releaseReference() {
            // 引用计数-1, 然后判断是否被丢弃是否计数为0, 如果是, 则通知销毁器
            if (refCount.decrementAndGet() == 0 && discarded.get()) {
                DiscardableSingletonPool<InstanceType, ?> pool = this.pool.get();
                if (pool != null) {
                    // 通知销毁器执行销毁
                    pool.notifyDestroyDiscardedInstances();
                }
            }
        }

        /**
         * 丢弃本单例对象, 它将在被使用完毕(引用计数为0)后销毁.
         */
        private void discard() {
            if (discarded.compareAndSet(false, true)) {
                info.setDiscardTimeMillis(System.currentTimeMillis());
                DiscardableSingletonPool<InstanceType, ?> pool = this.pool.get();
                if (pool != null) {
                    // discard from pool
                    pool.performDiscard(this);
                }
            }
        }

        /**
         * 判断是否能被销毁
         * @param forceDestroyDiscardedInstancesAfterMillis 强制销毁时间, 如果设置了这个参数, 对象在被丢弃后超过指定时间, 即使 "引用计数大于0" 也会被强制销毁
         * @return true: 能被销毁
         */
        private boolean canDestroy(long forceDestroyDiscardedInstancesAfterMillis) {
            /*
             * 被销毁的条件:
             * 已被丢弃 && ( 引用计数为0 || 被丢弃时间大于forceDestroyDiscardedInstancesAfterMillis )
             */
            return discarded.get() && (
                    refCount.get() <= 0 ||
                    (System.currentTimeMillis() - info.getDiscardTimeMillis()) > forceDestroyDiscardedInstancesAfterMillis
            );
        }

        private int getRefCount() {
            return refCount.get();
        }

        @Override
        public String toString() {
            return "{" +
                    "info=" + info +
                    ", discarded=" + discarded.get() +
                    ", refCount=" + refCount.get() +
                    '}';
        }
    }

    /**
     * <p>单例对象提供者, 对象每次使用完毕后, 都必须调用InstanceProvider#close方法释放引用, 否则对象在被丢弃(discard)后不会被销毁(destroy).</p>
     *
     * <pre>
     *      // 示例1: 用try-with-resource写法保证在使用完毕后释放引用
     *      try (DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam)){
     *          Foo foo = instanceProvider.getInstance();
     *          foo.invoke(input);
     *      }
     *
     *      // 示例2: 普通写法, 在使用完毕后释放引用
     *      DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam);
     *      try {
     *          Foo foo = instanceProvider.getInstance();
     *          foo.invoke(input);
     *      } finally {
     *          instanceProvider.close();
     *      }
     * </pre>
     */
    public static class InstanceProvider<InstanceType> implements AutoCloseable {

        // 对象包装
        private final InstanceHolder<InstanceType> instanceHolder;

        // 引用是否被释放
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private InstanceProvider(InstanceHolder<InstanceType> instanceHolder) {
            this.instanceHolder = instanceHolder;
            instanceHolder.acquireReference();
        }

        /**
         * <p>返回对象实例, 对象每次使用完毕后, 都必须调用InstanceProvider#close方法释放引用, 否则对象在被丢弃(discard)后不会被销毁(destroy).</p>
         *
         * <pre>
         *      // 示例1: 用try-with-resource写法保证在使用完毕后释放引用
         *      try (DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam)){
         *          Foo foo = instanceProvider.getInstance();
         *          foo.invoke(input);
         *      }
         *
         *      // 示例2: 普通写法, 在使用完毕后释放引用
         *      DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam);
         *      try {
         *          Foo foo = instanceProvider.getInstance();
         *          foo.invoke(input);
         *      } finally {
         *          instanceProvider.close();
         *      }
         * </pre>
         */
        public InstanceType getInstance() {
            return instanceHolder.getInstance();
        }

        /**
         * <p>[重要] 对象每次使用完毕后, 都必须调用InstanceProvider#close方法释放引用, 否则对象在被丢弃(discard)后不会被销毁(destroy).</p>
         *
         * <pre>
         *      // 示例1: 用try-with-resource写法保证在使用完毕后释放引用
         *      try (DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam)){
         *          Foo foo = instanceProvider.getInstance();
         *          foo.invoke(input);
         *      }
         *
         *      // 示例2: 普通写法, 在使用完毕后释放引用
         *      DiscardableSingletonPool.InstanceProvider<Foo> instanceProvider = discardableSingletonPool.getInstanceProvider(key, createParam);
         *      try {
         *          Foo foo = instanceProvider.getInstance();
         *          foo.invoke(input);
         *      } finally {
         *          instanceProvider.close();
         *      }
         * </pre>
         */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                instanceHolder.releaseReference();
            }
        }

        /**
         * 丢弃本单例对象, 它将在被使用完毕(引用计数为0)后销毁.
         */
        public void discard() {
            instanceHolder.discard();
        }

    }

    /**
     * 对象信息
     */
    public static class InstanceInfo {

        // 对象名称
        private final String key;
        // 对象创建时间
        private final long createTimeMillis;
        // 对象最后使用时间(被引用时更新)
        private long lastUsedTimeMillis;
        // 对象被丢弃时间(被丢弃时更新)
        private long discardTimeMillis;

        private InstanceInfo(String key) {
            long currentTime = System.currentTimeMillis();
            this.key = key;
            this.createTimeMillis = currentTime;
            this.lastUsedTimeMillis = currentTime;
        }

        /**
         * 对象名称
         */
        public String getKey() {
            return key;
        }

        /**
         * 对象创建时间
         */
        public long getCreateTimeMillis() {
            return createTimeMillis;
        }

        /**
         * 对象最后使用时间(被引用时更新)
         */
        public long getLastUsedTimeMillis() {
            return lastUsedTimeMillis;
        }

        /**
         * 对象最后使用时间(被引用时更新)
         */
        private void setLastUsedTimeMillis(long lastUsedTimeMillis) {
            this.lastUsedTimeMillis = lastUsedTimeMillis;
        }

        /**
         * 对象被丢弃时间(被丢弃时更新)
         */
        public long getDiscardTimeMillis() {
            return discardTimeMillis;
        }

        /**
         * 对象被丢弃时间(被丢弃时更新)
         */
        private void setDiscardTimeMillis(long discardTimeMillis) {
            this.discardTimeMillis = discardTimeMillis;
        }

        @Override
        public String toString() {
            return "{" +
                    "key='" + key + '\'' +
                    ", createTimeMillis=" + createTimeMillis +
                    ", lastUsedTimeMillis=" + lastUsedTimeMillis +
                    ", discardTimeMillis=" + discardTimeMillis +
                    '}';
        }
    }

    /**
     * 统计信息
     */
    public static class StatisticInfo {

        private final AtomicInteger createCount = new AtomicInteger(0);
        private final AtomicInteger createFailedCount = new AtomicInteger(0);
        private int discardCount = 0;
        private int destroyCount = 0;
        private int destroyForceCount = 0;
        private int destroyFailedCount = 0;
        private int destroyerRunCount = 0;

        public int getCreateCount() {
            return createCount.get();
        }

        public int getCreateFailedCount() {
            return createFailedCount.get();
        }

        public int getDiscardCount() {
            return discardCount;
        }

        public int getDestroyCount() {
            return destroyCount;
        }

        public int getDestroyForceCount() {
            return destroyForceCount;
        }

        public int getDestroyFailedCount() {
            return destroyFailedCount;
        }

        public int getDestroyerRunCount() {
            return destroyerRunCount;
        }

        @Override
        public String toString() {
            return "{" +
                    "createCount=" + createCount.get() +
                    ", createFailedCount=" + createFailedCount.get() +
                    ", discardCount=" + discardCount +
                    ", destroyCount=" + destroyCount +
                    ", destroyForceCount=" + destroyForceCount +
                    ", destroyFailedCount=" + destroyFailedCount +
                    ", destroyerRunCount=" + destroyerRunCount +
                    '}';
        }

    }

    /**
     * 对象创建失败
     */
    public static class InstanceCreateException extends RuntimeException {

        private static final long serialVersionUID = 3021413964159435308L;

        public InstanceCreateException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * 单例对象管理器, 创建/销毁对象
     * @param <InstanceType> 对象类型
     * @param <CreateParamType> 创建参数
     */
    public interface InstanceManager<InstanceType, CreateParamType> {

        /**
         * 创建对象
         * @param key 对象名称
         * @param createParam 创建参数, 可为空, 由DiscardableSingletonPool#getInstanceProvider传入
         * @return 创建出来的对象
         * @throws Exception 这里抛出的异常会被封装为InstanceCreateException抛出
         */
        InstanceType createInstance(String key, CreateParamType createParam) throws Exception;

        /**
         * 销毁对象, 如果这里抛出异常, 会回调onInstanceDestroyError方法
         * @param info 对象信息
         * @param instance 对象实例(销毁它)
         */
        void destroyInstance(InstanceInfo info, InstanceType instance) throws Exception;

        /**
         * 处理destroyInstance方法抛出的异常, 一般就打印日志
         * @param info 对象信息
         * @param instance 对象实例
         * @param t 异常
         */
        void onInstanceDestroyError(InstanceInfo info, InstanceType instance, Throwable t);

    }

}
