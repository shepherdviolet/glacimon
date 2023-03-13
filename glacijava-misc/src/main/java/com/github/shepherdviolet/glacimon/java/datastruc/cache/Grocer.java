/*
 * Copyright (C) 2022-2023 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.java.datastruc.cache;

import com.github.shepherdviolet.glacimon.java.concurrent.CountDownWaiter;
import com.github.shepherdviolet.glacimon.java.concurrent.GuavaThreadFactoryBuilder;
import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>[缓存|线程安全] 元素零售商</p>
 * <p></p>
 * <p>概述 =========================================================================================================</p>
 * <p>
 *     1. 本质上是一个元素缓存, 批量从数据源获取元素(以下称"补货""purchase")缓存起来, 逐个提供给调用者(以下称"获取""fetch").
 *        有点像一个零售商, 批量进货, 单个售卖. <br>
 *     2. 获取(fetch)和补货(purchase)是异步的, 调用fetch方法只会从缓存(队列)中获取元素, 如果缓存为空, 则挂起等待直到有货或者超时.
 *        异步线程会在合适的时候执行补货流程, 调用补货器(purchaser), 对缓存中的元素进行补充. <br>
 *     3. 本缓存适用于本地序列号缓存等需求.
 * </p>
 * <p></p>
 * <p>建议及注意事项 ================================================================================================</p>
 * <p>
 *     1. 正确地销毁Grocer: Grocer内部维护了三个线程池, 如果你需要重复创建-销毁Grocer实例, 务必在销毁时调用Grocer#close方法. <br>
 *     2. '缓存容量'设置建议: 缓存容量 >= 2 * fetch平均速率(每秒) * 补货平均耗时(秒) / 补货阈值(purchaseThresholdPercent).
 *        例如: Grocer作为序列号缓存, 每一次请求产生一个序列号, 系统预估请求TPS=120, 补货平均耗时0.2s, 补货阈值0.6,
 *        则缓存容量 >= 2 * 120 * 0.2 / 0.6 = 80, 缓存容量建议设置为80以上. <br>
 * </p>
 * <p></p>
 * <p>补货器Purchaser实现说明 =======================================================================================</p>
 * <p>
 *     方法1. purchase方法: 实现根据指定KEY和数量, 返回所需元素的逻辑. 如果成功, 则返回Goods.success(), 注意null元素无效; 如果失败,
 *        则返回Goods.fail(), 设置异常和'补货错误延迟时间', 例如 Goods.fail(new MyException("补货失败"), 2000) , 表示停止补货
 *        2秒, 同时fetch方法在此期间会抛出new MyException("补货失败")异常; 如果该方法返回null或者抛出异常, 补货也视为失败, '补货
 *        错误延迟时间'为默认值(Grocer#setDefaultErrorDuration). <br>
 *     注意1. purchase方法会被多线程同时调用(最大线程数为purchaseThreadNum), 但同一个KEY的补货不会同时发起. <br>
 * </p>
 * <p></p>
 * <p>获取(fetch)操作的过程及机制 ====================================================================================</p>
 * <p>
 *     过程1. 尝试从KEY对应的队列中获取元素(立即返回, 无等待). <br>
 *     过程2. 如果获取成功, 立即返回元素 --> [结束] <br>
 *     过程3. 如果获取失败(缓存为空), 且存在补货错误, 则抛出对应的异常 --> [结束] <br>
 *     过程4. 如果入参timeout<=0, 则抛出超时异常 --> [结束] <br>
 *     过程5. 尝试从队列中获取元素并挂起等待, 总等待时间为timeout, 超时则抛出超时异常 --> [结束] <br>
 *     过程6. 等待期间每隔一段时间(wakeupIntervalOnFetch)会唤醒并检查是否存在补货错误, 若存在错误, 则抛出对应的异常 --> [结束] <br>
 *     过程7. 获取成功, 返回元素 --> [结束] <br>
 *     -------------------------------------------------------------------------------------------- <br>
 *     其他机制1. 如果缓存中的元素数量<=补货阈值, 会通知补货线程进行补货(异步). (如果元素数量低于告警值, 会标记为TopUrgent) <br>
 *     其他机制2. 从队列中等待元素时, 会周期性唤醒检查异常, 避免补货异常时fetch操作长时间等待, 实现快速失败(详见步骤4和步骤5).
 *               (唤醒检查的间隔时间通过wakeupIntervalOnFetch设置, 默认1s) <br>
 *     其他机制3. 关于补货错误延迟时间: 当补货器返回Goods.fail()时, 会设置一个'补货错误延迟时间', 表示在错误发生后的这段时间内, 缓存
 *               不会再次发起补货, fetch时会返回指定异常. <br>
 * </p>
 * <p></p>
 * <p>补货(purchase)的流程及机制 =====================================================================================</p>
 * <p>
 *     调度流程1. 当"补货通知"方法被调用时, 启动单线程进行补货调度. <br>
 *     调度流程2. 先遍历标记了TopUrgent的队列, 用补货工作线程(线程数=purchaseThreadNum)异步执行补货流程. <br>
 *     调度流程3. 再遍历其他队列, 用补货工作线程(线程数=purchaseThreadNum)异步执行补货流程. <br>
 *     调度流程4. 等待本次发起的所有补货工作线程执行结束 --> [结束调度] <br>
 *     -------------------------------------------------------------------------------------------- <br>
 *     补货流程1. 如果之前存在补货错误(且在"补货错误维持时间"内), 跳过本次补货 --> [结束] <br>
 *     补货流程2. 如果缓存中的元素数量>补货阈值, 跳过本次补货 --> [结束] <br>
 *     补货流程3. 计算补货数量, 如果未缺货, 补货数量 = 缓存容量 - 缓存中元素数量; 如果缺货(元素数量<=告警值),
 *               会加大补货量 = (缓存容量 + 请求积压数) * (1 + 连续缺货次数) <br>
 *     补货流程4. 调用Purchaser#purchase进行补货, 若方法抛出异常或返回null(不建议这样), 视为补货失败, 补货错误维持时间为默认值
 *               (Grocer#setDefaultErrorDuration) --> [结束] <br>
 *     补货流程5. 若Purchaser#purchase返回Goods.fail(), 视为补货失败, 补货错误维持时间为Goods.fails()方法设定值 --> [结束] <br>
 *     补货流程6. 若Purchaser#purchase返回Goods.success(), 视为补货成功, 将元素加入队列 (注意, null元素不会加入队列). <br>
 *     补货流程7. 若本次补充的元素数量小于预期, 会再次通知补货. <br>
 *     -------------------------------------------------------------------------------------------- <br>
 *     其他机制1. 缺货时会增大补货量, 最大补货量受到参数maxPurchaseQuantity限制. (未缺货时, 补货量不受该参数限制) <br>
 *     其他机制2. 关于补货错误延迟时间: 当补货器返回Goods.fail()时, 会设置一个'补货错误延迟时间', 表示在错误发生后的这段时间内, 缓存
 *               不会再次发起补货, fetch时会返回指定异常. <br>
 * </p>
 * <p></p>
 * <p>清理(sweep)的流程及机制 ========================================================================================</p>
 * <p></p>
 * <p>
 *     调度流程1. 存在一个清理线程(单线程), 每隔elementExpireSeconds秒钟执行一次清理任务. <br>
 *     调度流程2. 遍历所有队列, 依次调用它们的清理方法 --> [结束调度] <br>
 *     -------------------------------------------------------------------------------------------- <br>
 *     清理流程1. 丢弃缓存队列中超过'缓存容量'限制的元素, 以保证队列长度 <= 缓存容量 (如果刚发生补货则跳过, 请求有积压未来得及返回). <br>
 *     清理流程2. 如果上一个清理周期内, 消耗(成功返回)的元素数小于缓存容量, 则丢弃元素直到消耗的元素 = 缓存容量, Grocer通过这种方式
 *               实现元素过期. (例如, elementExpireSeconds=3600s, cacheSize=10, 清扫线程每隔1小时执行一次, 如果某个KEY的元素在
 *               过去的1小时内只消耗了5个, 就在清扫时额外丢弃(expire)5个. 这样, 元素不可能在缓存中存在超过elementExpireSeconds秒). <br>
 * </p>
 *
 * @param <E> 元素类型
 * @param <T> 异常类型
 */
public class Grocer<E, T extends Throwable> implements Closeable {

    private static final int CACHE_WARNING_THRESHOLD = 1; // 缓存告警值
    private static final long BACKLOG_DIGESTION_TIME = 500L; // 积压消化时间 (在大批量补货后, 留给fetch等待线程返回的时间)

    private static final int CACHE_SIZE = 40; // 缓存容量(默认适用于50TPS, 补货耗时0.2s的场景)
    private static final float PURCHASE_THRESHOLD_PERCENT = 0.5f; // 补货阈值(百分比)
    private static final int PURCHASE_THREAD_NUM = 16; // 补货线程数
    private static final int MAX_PURCHASE_QUANTITY = 10000; // 最大补货数量
    private static final int ELEMENT_EXPIRE_SECONDS = 60 * 60; // 元素有效期
    private static final long WAKEUP_INTERVAL_ON_FETCH = 1000L; // fetch等待时, 会以这个周期唤醒线程, 用来检查是否有补货异常, 实现快速失败
    private static final long DEFAULT_ERROR_DURATION = 2000L; // 默认的补货错误维持时间

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // options
    private Purchaser<E, T> purchaser;
    private int cacheSize; // 千万不能在这里设置初值, 否则第一次setCacheSize方法不会被调用了
    private float purchaseThresholdPercent; // 千万不能在这里设置初值, 否则第一次setPurchaseThresholdPercent方法不会被调用了
    private int purchaseThresholdValue;
    private int maxPurchaseQuantity;
    private long elementExpireSeconds; // 千万不能在这里设置初值, 否则第一次setElementExpireSeconds方法不会被调用了

    // advanced options
    private long wakeupIntervalOnFetch = WAKEUP_INTERVAL_ON_FETCH;
    private long defaultErrorDuration = DEFAULT_ERROR_DURATION;

    private final Map<String, ElementQueue> elementQueueMap = new ConcurrentHashMap<>(); // 元素缓存队列

    private final ExecutorService purchaseDispatchThreadPool; // 补货调度线程池 (单线程)
    private final ThreadPoolExecutor purchaseWorkerThreadPool; // 补货工作线程池

    private final ScheduledThreadPoolExecutor sweepThreadPool; // 清理线程
    private ScheduledFuture<?> sweepFuture;
    private int sweepTaskId = 0; // 清理任务ID (保证被取消的任务不再执行)
    private long sweepCount = 0; // 清理事件次数
    private long lastSweepTime = System.currentTimeMillis(); // 上次清理时间

    private volatile boolean destroyed = false; // 销毁标志

    /**
     * <p>[缓存|线程安全] 元素零售商</p>
     * <p>必须设置: purchaser</p>
     * <p>建议设置: cacheSize purchaseThresholdPercent purchaseThreadNum maxPurchaseQuantity elementExpireSeconds</p>
     */
    public Grocer() {
        this(null);
    }

    /**
     * <p>[缓存|线程安全] 元素零售商</p>
     * <p>建议设置: cacheSize purchaseThresholdPercent purchaseThreadNum maxPurchaseQuantity elementExpireSeconds</p>
     * @param purchaser 补货器, Grocer从这个补货器获取新的元素
     */
    public Grocer(Purchaser<E, T> purchaser) {
        this(purchaser, CACHE_SIZE, PURCHASE_THRESHOLD_PERCENT, PURCHASE_THREAD_NUM, MAX_PURCHASE_QUANTITY, ELEMENT_EXPIRE_SECONDS);
    }

    /**
     * <p>[缓存|线程安全] 元素零售商</p>
     * <p></p>
     * <p>cacheSize设置建议:</p>
     * <p></p>
     * <p>
     *     缓存容量'设置建议: 缓存容量 >= 2 * fetch平均速率(每秒) * 补货平均耗时(秒) / 补货阈值(purchaseThresholdPercent).
     *     例如: Grocer作为序列号缓存, 每一次请求产生一个序列号, 系统预估请求TPS=120, 补货平均耗时0.2s, 补货阈值0.6,
     *     则缓存容量 >= 2 * 120 * 0.2 / 0.6 = 60, 缓存容量建议设置为60以上.
     * </p>
     * <p></p>
     * <p>关于maxPurchaseQuantity:</p>
     * <p></p>
     * <p>
     *      一般情况下, 每次补货数量 = 缓存容量 - 剩余数量, 即补满缓存. <br>
     *      如果出现缺货(补货时剩余数量接近0, 可能出现请求积压), 补货数量 = 缓存容量 + 请求积压数, 即在满足积压的前提下补满缓存, 若此时补
     *      货数量大于maxPurchaseQuantity, 则补货数量=maxPurchaseQuantity. <br>
     *      如果出现连续缺货, 补货数量 = (缓存容量 + 请求积压数) * (1 + 连续缺货次数), 以应对突然的高并发, 若此时补货数量大于
     *      maxPurchaseQuantity, 则补货数量=maxPurchaseQuantity. <br>
     *      注意, 当maxPurchaseQuantity < cacheSize时, 最大补货数为cacheSize. <br>
     * </p>
     * <p></p>
     * <p>关于elementExpireSeconds:</p>
     * <p></p>
     * <p>
     *      Grocer内部通过创建一个间隔elementExpireSeconds秒执行一次的清理线程实现元素过期,
     *      当缓存中的元素数量大于缓存数量时, 超出的部分会被清理; 当一个缓存在elementExpireSeconds秒内消耗掉的元素数量小于缓存容量时,
     *      会过期掉一部分元素, 使得这个周期内的元素消耗数达到缓存容量.
     * </p>
     *
     * @param purchaser 补货器, Grocer从这个补货器获取新的元素
     * @param cacheSize 默认的缓存容量(每个key都有一个缓存), 默认40
     * @param purchaseThresholdPercent 设置默认的补货阈值, 默认0.5. 例如: 0.7表示当缓存中元素数<=70%时, 会触发补货流程.
     * @param purchaseThreadNum 设置补货流程的工作线程数, 默认16
     * @param maxPurchaseQuantity 设置每次补货获取的最大数量, 默认10000
     * @param elementExpireSeconds 设置元素过期时间, 默认3600s
     */
    @SuppressWarnings("resource")
    public Grocer(Purchaser<E, T> purchaser, int cacheSize, float purchaseThresholdPercent, int purchaseThreadNum, int maxPurchaseQuantity, int elementExpireSeconds) {
        this.purchaser = purchaser;

        this.purchaseDispatchThreadPool = ThreadPoolExecutorUtils.createLazy(60,
                new GuavaThreadFactoryBuilder().setNameFormat("Glacijava-Grocer-dispatch-%s").setDaemon(true).build());

        this.purchaseWorkerThreadPool = ThreadPoolExecutorUtils.create(
                purchaseThreadNum,
                Integer.MAX_VALUE,
                60,
                new GuavaThreadFactoryBuilder().setNameFormat("Glacijava-Grocer-worker-%s").setDaemon(true).build(),
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);

        this.sweepThreadPool = ThreadPoolExecutorUtils.createScheduled(1,
                new GuavaThreadFactoryBuilder().setNameFormat("Glacijava-Grocer-sweep-%s").setDaemon(true).build());
        this.sweepThreadPool.setRemoveOnCancelPolicy(true); // 取消任务时从队列删除

        setCacheSizeAndThreshold(cacheSize, purchaseThresholdPercent);
        setPurchaseThreadNum(purchaseThreadNum);
        setMaxPurchaseQuantity(maxPurchaseQuantity);
        setElementExpireSeconds(elementExpireSeconds);
    }

    /**
     * 获取元素, 会挂起等待直到获取成功或超时 (超时时间为wakeupIntervalOnFetch, 默认1000ms)
     * @param key key
     * @param timeout 超时时间, 如果等待超过指定时间, 会抛出FetchTimeoutException异常; 注意如果timeout<=0, 获取不到元素一样会抛出异常
     * @return 元素, 不可能为null
     * @throws T 补货异常
     * @throws FetchTimeoutException 获取超时 (如果timeout<=0, 获取不到元素也会抛出这个异常)
     */
    public E fetch(String key, long timeout) throws T, FetchTimeoutException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (purchaser == null) {
            throw new IllegalStateException("Purchaser is null, you must provide a Purchaser for the Grocer");
        }
        if (destroyed) {
            throw new IllegalStateException("Grocer has been destroyed");
        }
        return getOrCreateElementQueue(key).fetch(timeout);
    }

    /**
     * [必要] 设置补货器, Grocer从这个补货器获取新的元素
     * @param purchaser 补货器
     */
    public Grocer<E, T> setPurchaser(Purchaser<E, T> purchaser) {
        this.purchaser = purchaser;
        return this;
    }

    /**
     * <p>设置默认的缓存容量(每个key都有一个缓存).</p>
     * <p></p>
     * <p>
     *     缓存容量'设置建议: 缓存容量 >= 2 * fetch平均速率(每秒) * 补货平均耗时(秒) / 补货阈值(purchaseThresholdPercent).
     *     例如: Grocer作为序列号缓存, 每一次请求产生一个序列号, 系统预估请求TPS=120, 补货平均耗时0.2s, 补货阈值0.6,
     *     则缓存容量 >= 2 * 120 * 0.2 / 0.6 = 60, 缓存容量建议设置为60以上.
     * </p>
     *
     * @param cacheSize 缓存容量, 默认40, [4, ∞)
     */
    public Grocer<E, T> setCacheSize(int cacheSize) {
        setCacheSizeAndThreshold(cacheSize, purchaseThresholdPercent);
        return this;
    }

    /**
     * <p>设置默认的补货阈值.</p>
     * <p>例如: 0.7表示当缓存中元素数<=70%时, 会触发补货流程. </p>
     * <p></p>
     * <p>
     *     缓存容量'设置建议: 缓存容量 >= 2 * fetch平均速率(每秒) * 补货平均耗时(秒) / 补货阈值(purchaseThresholdPercent).
     *     例如: Grocer作为序列号缓存, 每一次请求产生一个序列号, 系统预估请求TPS=120, 补货平均耗时0.2s, 补货阈值0.6,
     *     则缓存容量 >= 2 * 120 * 0.2 / 0.6 = 60, 缓存容量建议设置为60以上.
     * </p>
     *
     * @param purchaseThresholdPercent 补货阈值, 默认0.5, (0.0, 1.0)
     */
    public Grocer<E, T> setPurchaseThresholdPercent(float purchaseThresholdPercent) {
        setCacheSizeAndThreshold(cacheSize, purchaseThresholdPercent);
        return this;
    }

    private void setCacheSizeAndThreshold(int cacheSize, float purchaseThresholdPercent) {
        if (cacheSize < 4) {
            throw new IllegalArgumentException("cacheSize < 4");
        }
        if (purchaseThresholdPercent <= 0.0f) {
            throw new IllegalArgumentException("purchaseThresholdPercent <= 0.0");
        }
        if (purchaseThresholdPercent >= 1.0f) {
            throw new IllegalArgumentException("purchaseThresholdPercent >= 1.0");
        }
        synchronized (this) {
            if (this.cacheSize == cacheSize && this.purchaseThresholdPercent == purchaseThresholdPercent) {
                return;
            }
            int purchaseThresholdValue = (int) Math.floor((double) cacheSize * (double) purchaseThresholdPercent);
            if (purchaseThresholdValue < 1) {
                purchaseThresholdValue = 1;
            }
            if (purchaseThresholdValue >= cacheSize) {
                purchaseThresholdValue = cacheSize - 1;
            }
            this.cacheSize = cacheSize;
            this.purchaseThresholdValue = purchaseThresholdValue;
            this.purchaseThresholdPercent = purchaseThresholdPercent;
        }
        notifyPurchase();
    }

    /**
     * <p>设置指定KEY的缓存容量(每个key都有一个缓存).</p>
     * <p>设置指定KEY的补货阈值. 例如: 0.7表示当缓存中元素数<=70%时, 会触发补货流程</p>
     * <p>如果在指定KEY获取(fetch)前调用本方法, 会提前触发补货流程.</p>
     * <p></p>
     * <p>
     *     缓存容量'设置建议: 缓存容量 >= 2 * fetch平均速率(每秒) * 补货平均耗时(秒) / 补货阈值(purchaseThresholdPercent).
     *     例如: Grocer作为序列号缓存, 每一次请求产生一个序列号, 系统预估请求TPS=120, 补货平均耗时0.2s, 补货阈值0.6,
     *     则缓存容量 >= 2 * 120 * 0.2 / 0.6 = 60, 缓存容量建议设置为60以上.
     * </p>
     * @param key key
     * @param cacheSize 缓存容量, 默认40, [4, ∞)
     * @param purchaseThresholdPercent 补货阈值, 默认0.5, (0.0, 1.0)
     */
    public Grocer<E, T> setCacheSizeAndPurchaseThreshold(String key, int cacheSize, float purchaseThresholdPercent) {
        if (destroyed) {
            return this;
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        getOrCreateElementQueue(key).setCacheSizeAndPurchaseThreshold(cacheSize, purchaseThresholdPercent);
        return this;
    }

    /**
     * 设置补货流程的工作线程数
     * @param purchaseThreadNum 补货流程的工作线程数, 默认16
     */
    public Grocer<E, T> setPurchaseThreadNum(int purchaseThreadNum) {
        if (purchaseThreadNum <= 0) {
            throw new IllegalArgumentException("purchaseThreadNum <= 0");
        }
        synchronized (this) {
            if (destroyed) {
                return this;
            }
            this.purchaseWorkerThreadPool.setCorePoolSize(purchaseThreadNum);
        }
        return this;
    }

    /**
     * <p>设置每次补货获取的最大数量.</p>
     * <p></p>
     * <p>
     * 一般情况下, 每次补货数量 = 缓存容量 - 剩余数量, 即补满缓存. <br>
     * 如果出现缺货(补货时剩余数量接近0, 可能出现请求积压), 补货数量 = 缓存容量 + 请求积压数, 即在满足积压的前提下补满缓存, 若此时补
     * 货数量大于maxPurchaseQuantity, 则补货数量=maxPurchaseQuantity. <br>
     * 如果出现连续缺货, 补货数量 = (缓存容量 + 请求积压数) * (1 + 连续缺货次数), 以应对突然的高并发, 若此时补货数量大于
     * maxPurchaseQuantity, 则补货数量=maxPurchaseQuantity. <br>
     * 注意, 当maxPurchaseQuantity < cacheSize时, 最大补货数为cacheSize. <br>
     * </p>
     *
     * @param maxPurchaseQuantity 每次补货获取的最大数量, 默认10000
     */
    public Grocer<E, T> setMaxPurchaseQuantity(int maxPurchaseQuantity) {
        if (maxPurchaseQuantity < 4) {
            throw new IllegalArgumentException("maxPurchaseQuantity < 4");
        }
        this.maxPurchaseQuantity = maxPurchaseQuantity;
        return this;
    }

    /**
     * <p>设置元素过期时间.</p>
     * <p>Grocer内部通过创建一个间隔elementExpireSeconds秒执行一次的清理线程实现元素过期,
     * 当缓存中的元素数量大于缓存数量时, 超出的部分会被清理; 当一个缓存在elementExpireSeconds秒内消耗掉的元素数量小于缓存容量时,
     * 会过期掉一部分元素, 使得这个周期内的元素消耗数达到缓存容量.</p>
     *
     * @param elementExpireSeconds 元素过期时间, 默认3600s, 单位秒, [10, ∞)
     */
    public Grocer<E, T> setElementExpireSeconds(long elementExpireSeconds) {
        if (elementExpireSeconds < 10L) {
            throw new IllegalArgumentException("elementExpireSeconds < 10s");
        }
        synchronized (sweepThreadPool) {
            if (destroyed) {
                return this;
            }
            if (this.elementExpireSeconds == elementExpireSeconds) {
                return this;
            }
            this.elementExpireSeconds = elementExpireSeconds;
            if (sweepFuture != null) {
                sweepFuture.cancel(false);
            }
            // 计算距离上次SWEEP过了多少时间
            long passedTime = (System.currentTimeMillis() - lastSweepTime) / 1000L;
            long initDelay = Math.max(elementExpireSeconds - passedTime, 0L);
            sweepFuture = sweepThreadPool.scheduleAtFixedRate(new SweepTask(++sweepTaskId),
                    initDelay, elementExpireSeconds, TimeUnit.SECONDS);
        }
        return this;
    }

    /**
     * <p>[高级设置] 获取(fetch)等待时, 中途唤醒线程检查是否有错误的间隔.</p>
     * <p></p>
     * <p>我们将获取(fetch)的等待超时时间(timeout)根据唤醒间隔(wakeupIntervalOnFetch)拆分为多次等待, 缓解因异步补货失败导致fetch长时
     * 间等待直到超时的问题. 这样设计, 挂起的线程可以更快地返回错误(最长等待wakeupIntervalOnFetch).</p>
     * @param wakeupIntervalOnFetch 单位ms, 默认1000ms
     */
    public Grocer<E, T> setWakeupIntervalOnFetch(long wakeupIntervalOnFetch) {
        if (wakeupIntervalOnFetch < 100L) {
            throw new IllegalArgumentException("wakeupIntervalOnFetch < 100ms");
        }
        this.wakeupIntervalOnFetch = wakeupIntervalOnFetch;
        return this;
    }

    /**
     * <p>[高级设置] 默认的补货错误维持时间, 如果设置为<=0, 则不阻断补货(fetch不抛出异常, 会继续等待), 单位ms
     * (补货错误维持时间: 当发生补货错误, 指定时间内不会发起补货, 且期间的fetch都会抛出指定异常).</p>
     * <p></p>
     * <p>Grocer内部补货逻辑发生错误, 或者Purchaser#purchase()方法抛出异常或返回null时, 会采用这个"补货错误维持时间".
     * 如果Purchaser#purchase()方法抛出异常, 会采用Grocer#setDefaultErrorDuration()指定的时间.
     * 如果Purchaser#purchase()方法返回Goods#fail(), 会采用Goods#fail()指定的时间.</p>
     * @param defaultErrorDuration 单位ms, 默认2000ms
     */
    public Grocer<E, T> setDefaultErrorDuration(long defaultErrorDuration) {
        if (defaultErrorDuration < 100L) {
            throw new IllegalArgumentException("defaultErrorDuration < 100ms");
        }
        this.defaultErrorDuration = defaultErrorDuration;
        return this;
    }

    /**
     * 停止缓存服务(调用后无法再次工作)
     */
    @Override
    public void close() {
        destroyed = true;
        try {
            purchaseDispatchThreadPool.shutdownNow();
        } catch (Throwable ignore) {
        }
        try {
            sweepThreadPool.shutdownNow();
        } catch (Throwable ignore) {
        }
        try {
            purchaseWorkerThreadPool.shutdownNow();
        } catch (Throwable ignore) {
        }
    }

    /**
     * 获取统计信息
     */
    public String getStatisticInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        int line = 0;
        for (Map.Entry<String, ElementQueue> entry : elementQueueMap.entrySet()) {
            if (line++ > 0) {
                stringBuilder.append('\n');
            }
            stringBuilder.append(entry.getValue().getStatisticInfo());
        }
        return stringBuilder.toString();
    }

    private Grocer<E, T>.ElementQueue getOrCreateElementQueue(String key) {
        ElementQueue queue = elementQueueMap.get(key);
        if (queue == null) {
            queue = new ElementQueue(key);
            ElementQueue previous = elementQueueMap.putIfAbsent(key, queue);
            if (previous != null) {
                queue = previous;
            }
        }
        return queue;
    }

    private void notifyPurchase() {
        if (destroyed) {
            return;
        }
        try {
            purchaseDispatchThreadPool.execute(purchaseDispatchTask);
        } catch (Throwable ignore) {
        }
    }

    private void doInPurchaseWorkerThread(Runnable runnable) {
        purchaseWorkerThreadPool.execute(runnable);
    }

    private final Runnable purchaseDispatchTask = new Runnable() {

        @Override
        public void run() {
            CountDownWaiter waiter = new CountDownWaiter(0);
            for (Map.Entry<String, ElementQueue> entry : elementQueueMap.entrySet()) {
                if (entry.getValue().topUrgent) {
                    if (purchaseInWorkerThread(waiter, entry.getValue())) return;
                }
            }
            for (Map.Entry<String, ElementQueue> entry : elementQueueMap.entrySet()) {
                if (!entry.getValue().topUrgent) {
                    if (purchaseInWorkerThread(waiter, entry.getValue())) return;
                } else {
                    entry.getValue().topUrgent = false;
                }
            }
            if (destroyed) {
                return;
            }
            //等待异步操作全部完成
            try {
                waiter.await();
            } catch (InterruptedException ignore) {
            }
        }

        private boolean purchaseInWorkerThread(CountDownWaiter waiter, ElementQueue queue) {
            if (destroyed) {
                return true;
            }
            waiter.countUp();
            try {
                doInPurchaseWorkerThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            queue.purchase();
                        } finally {
                            waiter.countDown();
                        }
                    }
                });
            } catch (RejectedExecutionException ignore) {
                // destroyed
                return true;
            }
            return false;
        }

    };

    private class SweepTask implements Runnable {

        private final int taskId;

        private SweepTask(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
            Map<String, Integer> expiredInfos;
            if (logger.isInfoEnabled()) {
                logger.info("Grocer | Statistic report before sweep\n" + getStatisticInfo());
            }

            synchronized (sweepThreadPool) {
                // 检查本任务是否被取消 (一般不会出现这种情况, 因为任务已经从队列里移除了)
                if (taskId != sweepTaskId) {
                    return;
                }
                lastSweepTime = System.currentTimeMillis();
                sweepCount++;
                expiredInfos = new HashMap<>(elementQueueMap.size());
                for (Map.Entry<String, ElementQueue> entry : elementQueueMap.entrySet()) {
                    expiredInfos.put(entry.getKey(), entry.getValue().sweep());
                }
            }

            // 通知补货
            notifyPurchase();

            if (logger.isInfoEnabled()) {
                logger.info("Grocer | Sweeping complete (" + sweepCount + " times)");
                for (Map.Entry<String, Integer> expiredInfo : expiredInfos.entrySet()) {
                    logger.info("Grocer | Sweep | Queue(" + expiredInfo.getKey() + ") " + expiredInfo.getValue() + " elements have expired");
                }
            }
        }

    }

    private class ElementQueue {

        private final LinkedBlockingQueue<E> queue = new LinkedBlockingQueue<>();

        private Integer cacheSize; // 单独设置的缓存容量
        private Integer purchaseThresholdValue; // 单独设置的补货阈值

        private volatile long errorUntil = 0L;
        private volatile T error;
        private volatile RuntimeException errorInner;

        private volatile boolean topUrgent = true; // 新队列设置为紧急
        private volatile long lastPurchaseTime = 0; // 最后一次补货(成功)时间
        private final AtomicInteger outOfStockTimes = new AtomicInteger(0); // 连续发生缺货事件的次数(补货时发现一次算一次)

        // 获取请求数
        private final AtomicLong fetchCounter = new AtomicLong(0);
        // 获取请求积压数(多少线程挂起等待元素)
        private final AtomicInteger fetchBacklog = new AtomicInteger(0);
        // 元素补货计数(元素数)
        private final AtomicLong purchaseCounter = new AtomicLong(0);
        // 元素正常返回计数
        private final AtomicLong returnCounter = new AtomicLong(0);
        // 两次清扫间的正常返回计数
        private final AtomicInteger returnCounterBetweenSweeps = new AtomicInteger(0);
        // 元素过期计数(元素数)
        private final AtomicLong expiredCounter = new AtomicLong(0);
        // 获取超时计数
        private final AtomicLong timeoutCounter = new AtomicLong(0);
        // 获取失败计数
        private final AtomicLong errorCounter = new AtomicLong(0);

        private final String key;

        ElementQueue(String key) {
            this.key = key;
        }

        E fetch(long timeout) throws T, FetchTimeoutException {
            int purchaseThresholdValue = this.purchaseThresholdValue != null ? this.purchaseThresholdValue : Grocer.this.purchaseThresholdValue;

            // 尝试从缓存获取
            fetchCounter.getAndIncrement();
            E element = queue.poll();

            // 缓存里的元素数量低于阈值, 通知补货
            if (queue.size() <= purchaseThresholdValue) {
                // 如果到达警告值, 设置为紧急优先补货
                if (queue.size() <= CACHE_WARNING_THRESHOLD) {
                    topUrgent = true;
                }
                // 通知补货
                notifyPurchase();
            }

            // 如果缓存里没有了
            if (element == null) {
                // 如果存在错误, 则抛出异常
                checkError();
                // 需要挂起线程等待
                if (timeout > 0) {
                    element = waitForElement(timeout);
                }
            }

            // 获取超时 (如果timeout<=0, 获取不到也会抛出这个异常)
            if (element == null) {
                notifyPurchase();
                timeoutCounter.getAndIncrement();
                throw new FetchTimeoutException("Fetch element timeout (" + timeout + ")");
            }

            // 获取成功
            returnCounterBetweenSweeps.getAndIncrement();
            returnCounter.getAndIncrement();
            return element;
        }

        private E waitForElement(long timeout) throws T {
            E element = null;
            fetchBacklog.getAndIncrement();
            try {
                long until = System.currentTimeMillis() + timeout;
                long wait;
                while ((wait = until - System.currentTimeMillis()) > 0) {

                    /*
                     * 挂起线程等待.
                     * 此处将超时时间(timeout)根据唤醒间隔(wakeupIntervalOnFetch)拆分为多次等待, 缓解因异步补货失败导致
                     * 这里长时间等待直到超时的问题. 这样设计, 挂起的线程可以更快地返回错误(最长等待wakeupIntervalOnFetch,
                     * 默认1s).
                     */
                    element = queue.poll(Math.min(wait, wakeupIntervalOnFetch), TimeUnit.MILLISECONDS);

                    // 获取成功
                    if (element != null) {
                        break;
                    }

                    // 获取失败, 如果存在错误, 则抛出异常, 否则继续挂起等待直到超时
                    checkError();

                }
            } catch (InterruptedException ignore) {
            } finally {
                fetchBacklog.getAndDecrement();
            }
            return element;
        }

        void purchase() {
            int cacheSize = this.cacheSize != null ? this.cacheSize : Grocer.this.cacheSize;
            int purchaseThresholdValue = this.purchaseThresholdValue != null ? this.purchaseThresholdValue : Grocer.this.purchaseThresholdValue;

            // 已停止服务
            if (destroyed) {
                return;
            }
            // 如果检查到非缺货状态, 重置连续缺货次数. 判断存货数量大于警戒线(无积压) 且 距离上次补货完成超过"积压消化时间"(并不是大量补货后来不及返回)
            if (outOfStockTimes.get() > 0
                    && queue.size() > CACHE_WARNING_THRESHOLD
                    && System.currentTimeMillis() - lastPurchaseTime > BACKLOG_DIGESTION_TIME) {
                outOfStockTimes.set(0);
            }
            // 补货存在错误, 暂停补货
            if (System.currentTimeMillis() < errorUntil) {
                return;
            }
            // 缓存中的元素数未达到阈值
            if (queue.size() > purchaseThresholdValue) {
                return;
            }

            // 计算补货数量
            int quantity;
            if (queue.size() > CACHE_WARNING_THRESHOLD) {
                // 未到警戒线时, 补足缓存即可
                quantity = cacheSize - queue.size();
            } else {
                // 累加连续缺货次数
                int lastOutOfStockTimes = outOfStockTimes.getAndIncrement();
                // 到达警戒线时, 需要补 缓存容量 + 请求积压数
                // 连续到达警戒线时, 需要补 (缓存容量 + 请求积压数) * (1 + 连续缺货次数)
                quantity = (cacheSize + fetchBacklog.get()) * (1 + lastOutOfStockTimes);
            }
            // 限制最大补货数
            if (quantity > Math.max(maxPurchaseQuantity, cacheSize)) {
                quantity = Math.max(maxPurchaseQuantity, cacheSize);
            }

            // 检查purchaser
            Purchaser<E, T> purchaser = Grocer.this.purchaser;
            if (purchaser == null) {
                // 设置补货错误
                setError(System.currentTimeMillis() + defaultErrorDuration, null,
                        new IllegalStateException("Purchaser is null, you must provide a Purchaser for the Grocer"));
                return;
            }

            // 补货
            Goods<E, T> goods;
            try {
                goods = purchaser.purchase(key, quantity);
            } catch (Throwable t) {
                // 设置补货错误
                setError(System.currentTimeMillis() + defaultErrorDuration, null,
                        new RuntimeException("Catch an unexpected exception thrown by the purchaser when executing the purchase method", t));
                return;
            }

            if (goods == null) {
                // 设置补货错误
                setError(System.currentTimeMillis() + defaultErrorDuration, null,
                        new RuntimeException("Wrong purchaser implementation, Purchaser#purchase() returns null"));
                return;
            }
            if (goods.error != null) {
                // 设置补货错误
                setError(System.currentTimeMillis() + goods.errorDuration, goods.error, null);
                return;
            }

            // 将新元素加入队列
            Collection<E> elements = goods.elements;
            int count = 0;
            if (elements != null) {
                for (E element : elements) {
                    if (element == null) {
                        continue;
                    }
                    queue.offer(element);
                    // 计数
                    purchaseCounter.getAndIncrement();
                    count++;
                }
            }

            // 记录补货时间
            lastPurchaseTime = System.currentTimeMillis();

            // 如果补货数量未达预期, 再次通知补货
            if (count < quantity) {
                notifyPurchase();
            }
        }

        int sweep() {
            int cacheSize = this.cacheSize != null ? this.cacheSize : Grocer.this.cacheSize;

            // 记录Sweep前的expired计数
            long expiredBeforeSweep = expiredCounter.get();

            // 丢弃队列中超过缓存数量限制的部分 (如果刚刚发生补货则不进行丢弃, 说明请求有积压一次补货了很多还没来得及返回)
            if (System.currentTimeMillis() - lastPurchaseTime > BACKLOG_DIGESTION_TIME) {
                while (queue.size() > cacheSize) {
                    queue.poll();
                    expiredCounter.getAndIncrement();
                }
            }

            // 如果一个Sweep周期内(=元素有效期), 返回的元素数小于缓存容量, 就丢弃元素直到消耗完缓存容量 (通过这种方式实现元素过期)
            while (returnCounterBetweenSweeps.getAndIncrement() < cacheSize) {
                queue.poll();
                expiredCounter.getAndIncrement();
            }

            // 重置计数器, 记录下一个Sweep周期的返回计数
            returnCounterBetweenSweeps.set(0);

            return (int) (expiredCounter.get() - expiredBeforeSweep);
        }

        String getStatisticInfo() {
            return "Queue[" + key + "] #errorUntil:" + errorUntil + " #lastPurchaseTime:" + lastPurchaseTime +
                    " #fetchBacklog:" + fetchBacklog.get() + " #returnBetweenSweeps:" + returnCounterBetweenSweeps.get() + " | " +
                    purchaseCounter.get() + "(purchased) = " + queue.size() + "(inCache) + " + returnCounter.get() + "(returned) + " + expiredCounter.get() + "(expired) | " +
                    fetchCounter.get() + "(fetched) = " + fetchBacklog.get() + "(waiting) + " + returnCounter.get() + "(returned) + " + timeoutCounter.get() + "(timeout) + " + errorCounter.get() + "(error)";
        }

        void setCacheSizeAndPurchaseThreshold(int cacheSize, float purchaseThresholdPercent) {
            if (cacheSize < 4) {
                throw new IllegalArgumentException("cacheSize < 4");
            }
            if (purchaseThresholdPercent <= 0.0f) {
                throw new IllegalArgumentException("purchaseThresholdPercent <= 0.0");
            }
            if (purchaseThresholdPercent >= 1.0f) {
                throw new IllegalArgumentException("purchaseThresholdPercent >= 1.0");
            }
            synchronized (this) {
                int purchaseThresholdValue = (int) Math.floor((double) cacheSize * (double) purchaseThresholdPercent);
                if (purchaseThresholdValue < 1) {
                    purchaseThresholdValue = 1;
                }
                if (purchaseThresholdValue >= cacheSize) {
                    purchaseThresholdValue = cacheSize - 1;
                }
                this.cacheSize = cacheSize;
                this.purchaseThresholdValue = purchaseThresholdValue;
            }
            notifyPurchase();
        }

        private void setError(long errorUntil, T error, RuntimeException errorInner) {
            this.errorUntil = errorUntil;
            this.error = error;
            this.errorInner = errorInner;
        }

        private void checkError() throws T {
            if (System.currentTimeMillis() < errorUntil) {
                errorCounter.getAndIncrement();
                if (error != null) {
                    throw error;
                }
                if (errorInner != null) {
                    throw errorInner;
                }
                throw new IllegalStateException("Unknown error, the error returned from purchaser is null");
            }
        }

    }

    /**
     * 补货器返回结果
     */
    public static class Goods<E, T extends Throwable> {

        private final Collection<E> elements;
        private final T error;
        private final long errorDuration;

        private Goods(Collection<E> elements, T error, long errorDuration) {
            this.elements = elements;
            this.error = error;
            this.errorDuration = errorDuration;
        }

        /**
         * 补货成功.
         * @param elements 新元素集合(null元素会被忽略)
         */
        public static <E, T extends Throwable> Goods<E, T> success(Collection<E> elements) {
            return new Goods<>(elements, null, 0);
        }

        /**
         * 补货失败.
         * 如果补货失败, 请返回Goods#fail(), 并设置合适的"补货错误维持时间".
         * @param error 异常, 不可为空
         * @param errorDuration 补货错误维持时间, 如果设置为<=0, 则不阻断补货(fetch不抛出异常, 会继续等待), 单位ms
         *                      (补货错误维持时间: 当发生补货错误, 指定时间内不会发起补货, 且期间的fetch都会抛出指定异常).
         */
        public static <E, T extends Throwable> Goods<E, T> fail(T error, long errorDuration) {
            if (error == null) {
                throw new IllegalArgumentException("Wrong purchaser implementation, Goods#fail() method set a null exception");
            }
            return new Goods<>(null, error, errorDuration);
        }

    }

    public interface Purchaser<E, T extends Throwable> {

        /**
         * 实现补货逻辑(会有多线程执行本方法). 注意! IO操作一定要注意设置超时, 不要挂死线程!
         * 如果补货成功, 请返回Goods.success(...).
         * 如果补货失败, 请返回Goods.fail(...), 并设置合适的"补货错误维持时间".
         * 如果本方法直接抛出异常(尽量不要抛出异常), "补货错误维持时间"会采用Grocer#setDefaultErrorDuration()指定的时间.
         * 请勿返回null, 如果返回null, 则视为补货失败, "补货错误维持时间"会采用Grocer#setDefaultErrorDuration()指定的时间.
         * (P.S.补货错误维持时间: 当发生补货错误, 指定时间内不会发起补货, 且期间的fetch都会抛出指定异常).
         * @param key key
         * @param quantity 请求数量(返回数量允许小于请求数量)
         * @return 补货结果
         */
        Goods<E, T> purchase(String key, int quantity);

    }

    /**
     * 获取元素超时
     */
    public static class FetchTimeoutException extends Exception {

        public FetchTimeoutException(String message) {
            super(message);
        }

        public FetchTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
