/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.thistle.util.concurrent;

import sviolet.thistle.compat.concurrent.CompatThreadFactoryBuilder;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.*;

/**
 * <p>ThreadPoolExecutor线程池工具</p>
 *
 * <p>
 * ThreadPoolExecutor笔记:<br>
 * 1.核心线程一般不会终止, 始终等待队列中的新任务.<br>
 * 2.非核心线程空闲时会终止, 等待超过设定时间(keepAliveTime)后结束.<br>
 * 3.线程数达到corePoolSize之前, 每次执行(execute)都会创建一个新的核心线程.<br>
 * 4.当线程数达到corePoolSize之后, 会将任务(Runnable)加入工作队列(workQueue).<br>
 * --4.1.如果此时线程数为0, 则会创建一个非核心线程(仅此一个).<br>
 * --4.2.如果任务入队成功, 存活的线程会从队列中获取任务执行. <br>
 * --4.3.如果任务入队失败(BlockingQueue.offer(E e)返回false), 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
 * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
 * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
 * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
 * 6.使用SynchronousQueue工作队列时, 并发任务会直接增加线程(包括核心线程和非核心线程).<br>
 * --6.1.当并发量超过maximumPoolSize时, 拒绝任务并由RejectedExecutionHandler处理.<br>
 * --6.2.因此, 一般maximumPoolSize >= corePoolSize.<br>
 * </p>
 *
 * @author S.Violet
 */
public class ThreadPoolExecutorUtils {

    private static final Set<EnhancedExecutor> POOL = Collections.newSetFromMap(new WeakHashMap<EnhancedExecutor, Boolean>());

    /**
     * <p>会超时的单线程池, 核心线程数0, 最大线程数1, 队列长度Integer.MAX_VALUE</p>
     * <p>这个线程池不会阻止JVM自然结束(核心线程数为0)</p>
     *
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * </p>
     *
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     */
    public static ExecutorService createSingle(long keepAliveSeconds, String threadNameFormat){
        return create(
                0,
                1,
                keepAliveSeconds,
                threadNameFormat,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * <p>会超时的单线程池, 核心线程数0, 最大线程数1, 队列长度Integer.MAX_VALUE</p>
     * <p>这个线程池不会阻止JVM自然结束(核心线程数为0)</p>
     *
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * </p>
     *
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadFactory 线程工厂 (可以设置线程名称, 是否daemon等)
     */
    public static ExecutorService createSingle(long keepAliveSeconds, ThreadFactory threadFactory){
        return create(
                0,
                1,
                keepAliveSeconds,
                threadFactory,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * <p>[特殊用途]惰性单线程池, 核心线程数0, 最大线程数1, 队列长度1, 策略DiscardPolicy</p>
     * <p>这个线程池不会阻止JVM自然结束(核心线程数为0)</p>
     * <p></p>
     * <p>
     *     警告: 请明确用途后再使用!!!<br>
     *     WARNING: This ThreadPool should be used with caution!!!<br>
     * </p>
     * <p></p>
     * <p>特性:</p>
     * <p></p>
     * <p>
     * 1.单线程, 同时只能执行一个任务, 线程有存活期限.<br>
     * 2.队列长度1, 同时执行(execute)多个任务时, 至多执行2个, 多余的任务会被抛弃(且不会抛出异常).<br>
     * 3.能保证在最后一次执行(execute)之后, 有一次完整的任务处理(Runnable.run()).<br>
     * 4.用于实现调度/清扫. 例如: 实现一个调度任务, 从某个队列中, 循环获取元素进行处理的功能. 在每次元素入队列时, 使用本线程池
     * 执行调度任务, 任务中循环处理队列中的元素直到队列为空. 因为是单线程池, 所以处理逻辑不会被同时重复执行; 因为长度为1的等待
     * 队列, 能保证队列中的元素都被及时处理(每次execute之后必然会有一次完整的处理流程); 因为核心线程数0, 闲时能释放线程, 比无限
     * 循环的实现方式占资源少, 比定时执行的实现方式实时性高.<br>
     * </p>
     * <p></p>
     * <p>笔记:</p>
     * <p></p>
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * </p>
     * <p></p>
     * <p>如果忽略掉本工具类提供的新特性, 可以简化为:</p>
     * <pre>
     *          // 这么写就没有本工具类提供的新特性了: 自定义线程名, 执行前后监听, 统一管理和销毁...
     *          new ThreadPoolExecutor(0, 1, 60L,
     *                     TimeUnit.SECONDS,
     *                     new LinkedBlockingQueue<Runnable>(1),
     *                     Executors.defaultThreadFactory(),
     *                     new ThreadPoolExecutor.DiscardPolicy());
     * </pre>
     *
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     */
    public static ExecutorService createLazy(long keepAliveSeconds, String threadNameFormat){
        return create(
                0,
                1,
                keepAliveSeconds,
                threadNameFormat,
                new LinkedBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardPolicy(),
                null);
    }

    /**
     * <p>[特殊用途]惰性单线程池, 核心线程数0, 最大线程数1, 队列长度1, 策略DiscardPolicy</p>
     * <p>这个线程池不会阻止JVM自然结束(核心线程数为0)</p>
     * <p></p>
     * <p>
     *     警告: 请明确用途后再使用!!!<br>
     *     WARNING: This ThreadPool should be used with caution!!!<br>
     * </p>
     * <p></p>
     * <p>特性:</p>
     * <p></p>
     * <p>
     * 1.单线程, 同时只能执行一个任务, 线程有存活期限.<br>
     * 2.队列长度1, 同时执行(execute)多个任务时, 至多执行2个, 多余的任务会被抛弃(且不会抛出异常).<br>
     * 3.能保证在最后一次执行(execute)之后, 有一次完整的任务处理(Runnable.run()).<br>
     * 4.用于实现调度/清扫. 例如: 实现一个调度任务, 从某个队列中, 循环获取元素进行处理的功能. 在每次元素入队列时, 使用本线程池
     * 执行调度任务, 任务中循环处理队列中的元素直到队列为空. 因为是单线程池, 所以处理逻辑不会被同时重复执行; 因为长度为1的等待
     * 队列, 能保证队列中的元素都被及时处理(每次execute之后必然会有一次完整的处理流程); 因为核心线程数0, 闲时能释放线程, 比无限
     * 循环的实现方式占资源少, 比定时执行的实现方式实时性高.<br>
     * </p>
     * <p></p>
     * <p>笔记:</p>
     * <p></p>
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * </p>
     * <p></p>
     * <p>如果忽略掉本工具类提供的新特性, 可以简化为:</p>
     * <pre>
     *          // 这么写就没有本工具类提供的新特性了: 自定义线程名, 执行前后监听, 统一管理和销毁...
     *          new ThreadPoolExecutor(0, 1, 60L,
     *                     TimeUnit.SECONDS,
     *                     new LinkedBlockingQueue<Runnable>(1),
     *                     Executors.defaultThreadFactory(),
     *                     new ThreadPoolExecutor.DiscardPolicy());
     * </pre>
     *
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadFactory 线程工厂 (可以设置线程名称, 是否daemon等)
     */
    public static ExecutorService createLazy(long keepAliveSeconds, ThreadFactory threadFactory){
        return create(
                0,
                1,
                keepAliveSeconds,
                threadFactory,
                new LinkedBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardPolicy(),
                null);
    }

    /**
     * <p>固定线程数的线程池, 核心线程数poolSize, 最大线程数poolSize, 队列长度Integer.MAX_VALUE</p>
     * <p>注意!!!这个线程池会阻止JVM自然结束(核心线程数大于0), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * </p>
     *
     * @param poolSize 线程数
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     */
    public static ExecutorService createFixed(int poolSize, String threadNameFormat){
        return create(
                poolSize,
                poolSize,
                0L,
                threadNameFormat,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * <p>固定线程数的线程池, 核心线程数poolSize, 最大线程数poolSize, 队列长度Integer.MAX_VALUE</p>
     * <p>注意!!!这个线程池会阻止JVM自然结束(核心线程数大于0), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * </p>
     *
     * @param poolSize 线程数
     * @param threadFactory 线程工厂 (可以设置线程名称, 是否daemon等)
     */
    public static ExecutorService createFixed(int poolSize, ThreadFactory threadFactory){
        return create(
                poolSize,
                poolSize,
                0L,
                threadFactory,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * <p>动态线程数的线程池, 核心线程数corePoolSize, 最大线程数maximumPoolSize, 队列长度0</p>
     * <p>注意!!!这个线程池可能会阻止JVM自然结束(当核心线程数大于0时), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 6.使用SynchronousQueue工作队列时, 并发任务会直接增加线程(包括核心线程和非核心线程).<br>
     * --6.1.当并发量超过maximumPoolSize时, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --6.2.因此, 一般maximumPoolSize >= corePoolSize.<br>
     * </p>
     *
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     */
    public static ExecutorService createCached(int corePoolSize,
                                               int maximumPoolSize,
                                               long keepAliveSeconds,
                                               String threadNameFormat){
        return create(
                corePoolSize,
                maximumPoolSize,
                keepAliveSeconds,
                threadNameFormat,
                new SynchronousQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * <p>动态线程数的线程池, 核心线程数corePoolSize, 最大线程数maximumPoolSize, 队列长度0</p>
     * <p>注意!!!这个线程池可能会阻止JVM自然结束(当核心线程数大于0时), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 6.使用SynchronousQueue工作队列时, 并发任务会直接增加线程(包括核心线程和非核心线程).<br>
     * --6.1.当并发量超过maximumPoolSize时, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --6.2.因此, 一般maximumPoolSize >= corePoolSize.<br>
     * </p>
     *
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadFactory 线程工厂 (可以设置线程名称, 是否daemon等)
     */
    public static ExecutorService createCached(int corePoolSize,
                                               int maximumPoolSize,
                                               long keepAliveSeconds,
                                               ThreadFactory threadFactory){
        return create(
                corePoolSize,
                maximumPoolSize,
                keepAliveSeconds,
                threadFactory,
                new SynchronousQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * <p>创建线程池</p>
     * <p>注意!!!这个线程池可能会阻止JVM自然结束(当核心线程数大于0时), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * 6.使用SynchronousQueue工作队列时, 并发任务会直接增加线程(包括核心线程和非核心线程).<br>
     * --6.1.当并发量超过maximumPoolSize时, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --6.2.因此, 一般maximumPoolSize >= corePoolSize.<br>
     * </p>
     *
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     * @param workQueue 工作队列
     * @param rejectHandler nullable, 拒绝处理器, 默认: new ThreadPoolExecutor.AbortPolicy()
     * @param executeListener nullable, 监听执行前执行后的事件
     */
    public static ExecutorService create(int corePoolSize,
                                         int maximumPoolSize,
                                         long keepAliveSeconds,
                                         String threadNameFormat,
                                         BlockingQueue<Runnable> workQueue,
                                         RejectedExecutionHandler rejectHandler,
                                         final ExecuteListener executeListener){
        return create(
                corePoolSize,
                maximumPoolSize,
                keepAliveSeconds,
                new CompatThreadFactoryBuilder().setNameFormat(threadNameFormat).build(),
                workQueue,
                rejectHandler,
                executeListener
        );
    }

    /**
     * <p>创建线程池</p>
     * <p>注意!!!这个线程池可能会阻止JVM自然结束(当核心线程数大于0时), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 5.使用LinkedBlockingQueue工作队列时, 在填满核心线程后, 后续任务会加入队列, 队列满之前都不会尝试增加非核心线程.<br>
     * --5.1.如果队列满了, 会尝试增加非核心线程. 如果增加失败, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --5.2.因此, 一般corePoolSize == maximumPoolSize, 或者corePoolSize = 0 maximumPoolSize = 1(会超时的单线程池).<br>
     * 6.使用SynchronousQueue工作队列时, 并发任务会直接增加线程(包括核心线程和非核心线程).<br>
     * --6.1.当并发量超过maximumPoolSize时, 拒绝任务并由RejectedExecutionHandler处理.<br>
     * --6.2.因此, 一般maximumPoolSize >= corePoolSize.<br>
     * </p>
     *
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadFactory 线程工厂
     * @param workQueue 工作队列
     * @param rejectHandler nullable, 拒绝处理器, 默认: new ThreadPoolExecutor.AbortPolicy()
     * @param executeListener nullable, 监听执行前执行后的事件
     */
    public static ExecutorService create(int corePoolSize,
                                         int maximumPoolSize,
                                         long keepAliveSeconds,
                                         ThreadFactory threadFactory,
                                         BlockingQueue<Runnable> workQueue,
                                         RejectedExecutionHandler rejectHandler,
                                         final ExecuteListener executeListener){

        EnhancedThreadPoolExecutor executorService = new EnhancedThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new RejectedExecutionHandlerWrapper(rejectHandler != null ? rejectHandler : new ThreadPoolExecutor.AbortPolicy()),
                executeListener);

        synchronized (POOL) {
            POOL.add(executorService);
        }

        return executorService;

    }

    /**
     * <p>创建定时线程池</p>
     * <p>注意!!!这个线程池会阻止JVM自然结束(核心线程数大于0), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 1.定时线程池能够延迟/循环执行任务
     * </p>
     *
     * @param corePoolSize 核心线程数, >= 1
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     */
    public static ScheduledExecutorService createScheduled(int corePoolSize, String threadNameFormat){
        return createScheduled(corePoolSize, threadNameFormat, null, null);
    }

    /**
     * <p>创建定时线程池</p>
     * <p>注意!!!这个线程池会阻止JVM自然结束(核心线程数大于0), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 1.定时线程池能够延迟/循环执行任务
     * </p>
     *
     * @param corePoolSize 核心线程数, >= 1
     * @param threadFactory 线程工厂 (可以设置线程名称, 是否daemon等)
     */
    public static ScheduledExecutorService createScheduled(int corePoolSize, ThreadFactory threadFactory){
        return createScheduled(corePoolSize, threadFactory, null, null);
    }

    /**
     * <p>创建定时线程池</p>
     * <p>注意!!!这个线程池会阻止JVM自然结束(核心线程数大于0), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 1.定时线程池能够延迟/循环执行任务
     * </p>
     *
     * @param corePoolSize 核心线程数, >= 1
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     * @param rejectHandler nullable, 拒绝处理器, 默认: new ThreadPoolExecutor.AbortPolicy()
     * @param executeListener nullable, 监听执行前执行后的事件
     */
    public static ScheduledExecutorService createScheduled(int corePoolSize,
                                                           String threadNameFormat,
                                                           RejectedExecutionHandler rejectHandler,
                                                           final ExecuteListener executeListener){
        return createScheduled(
                corePoolSize,
                new CompatThreadFactoryBuilder().setNameFormat(threadNameFormat).build(),
                rejectHandler,
                executeListener);
    }

    /**
     * <p>创建定时线程池</p>
     * <p>注意!!!这个线程池会阻止JVM自然结束(核心线程数大于0), 可以考虑设置为守护线程(daemon=true), 或者在合适的时候停止线程池(ExecutorService#shutdown)</p>
     * <p>通过ThreadFactory设置守护线程示例: new CompatThreadFactoryBuilder().setNameFormat("name-%d").setDaemon(true).build()</p>
     *
     * <p>
     * 1.定时线程池能够延迟/循环执行任务
     * </p>
     *
     * @param corePoolSize 核心线程数, >= 1
     * @param threadFactory 线程工厂
     * @param rejectHandler nullable, 拒绝处理器, 默认: new ThreadPoolExecutor.AbortPolicy()
     * @param executeListener nullable, 监听执行前执行后的事件
     */
    public static ScheduledExecutorService createScheduled(int corePoolSize,
                                         ThreadFactory threadFactory,
                                         RejectedExecutionHandler rejectHandler,
                                         final ExecuteListener executeListener){

        //JDK8的BUG, corePoolSize=0时会吃CPU(无限循环)
        //https://bugs.openjdk.java.net/browse/JDK-8129861
        if (corePoolSize < 1) {
            throw new IllegalArgumentException("corePoolSize must >= 1 for ScheduledExecutorService, otherwise it will result in high CPU usage.");
        }

        EnhancedScheduledThreadPoolExecutor executorService = new EnhancedScheduledThreadPoolExecutor(
                corePoolSize,
                threadFactory,
                new RejectedExecutionHandlerWrapper(rejectHandler != null ? rejectHandler : new ThreadPoolExecutor.AbortPolicy()),
                executeListener);

        synchronized (POOL) {
            POOL.add(executorService);
        }

        return executorService;

    }

    /**
     * 将所有通过此工具创建的ExecutorService停止(shutdownNow, 实际上是向线程发送interrupt信号,
     * 并不是直接杀死线程).
     * 谨慎使用此方法, 调用后之前所有创建的ExecutorService都将无法使用, 通常在停止服务时调用.
     * 另外, 此方法停止的线程池, RejectedExecutionHandler的异常也会被拦截, 不会抛出.
     */
    public static void shutdownNowAll(){
        synchronized (POOL) {
            for (EnhancedExecutor executorService : POOL) {
                if (executorService != null) {
                    try {
                        executorService.enhancedShutdownNow();
                    } catch (Throwable ignore){
                    }
                }
            }
        }
    }

    private interface EnhancedExecutor {
        void enhancedShutdownNow();
    }

    /**
     * ThreadPoolExecutor加强
     */
    private static class EnhancedThreadPoolExecutor extends ThreadPoolExecutor implements EnhancedExecutor {

        private final ExecuteListener executeListener;
        private final RejectedExecutionHandlerWrapper rejectedExecutionHandlerWrapper;

        private EnhancedThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue,
                                  ThreadFactory threadFactory,
                                  RejectedExecutionHandlerWrapper handler,
                                  ExecuteListener executeListener) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
            this.executeListener = executeListener;
            this.rejectedExecutionHandlerWrapper = handler;
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            if (executeListener != null) {
                executeListener.beforeExecute(t, r);
            }
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (executeListener != null) {
                executeListener.afterExecute(r, t);
            }
        }

        /**
         * shutdownNow的同时, 屏蔽RejectedExecutionHandler的异常, 忽略异常
         */
        @Override
        public void enhancedShutdownNow() {
            try {
                rejectedExecutionHandlerWrapper.shutdown();
                super.shutdownNow();
            } catch (Throwable ignore){
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    /**
     * ScheduledThreadPoolExecutor加强
     */
    private static class EnhancedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements EnhancedExecutor {

        private final ExecuteListener executeListener;
        private final RejectedExecutionHandlerWrapper rejectedExecutionHandlerWrapper;

        private EnhancedScheduledThreadPoolExecutor(int corePoolSize,
                                           ThreadFactory threadFactory,
                                           RejectedExecutionHandlerWrapper handler,
                                           ExecuteListener executeListener) {
            super(corePoolSize, threadFactory, handler);
            this.executeListener = executeListener;
            this.rejectedExecutionHandlerWrapper = handler;
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            if (executeListener != null) {
                executeListener.beforeExecute(t, r);
            }
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (executeListener != null) {
                executeListener.afterExecute(r, t);
            }
        }

        /**
         * shutdownNow的同时, 屏蔽RejectedExecutionHandler的异常, 忽略异常
         */
        @Override
        public void enhancedShutdownNow() {
            try {
                rejectedExecutionHandlerWrapper.shutdown();
                super.shutdownNow();
            } catch (Throwable ignore){
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    /**
     * RejectedExecutionHandler包装类
     */
    private static class RejectedExecutionHandlerWrapper implements RejectedExecutionHandler{

        private RejectedExecutionHandler provider;
        private volatile boolean isShutdown = false;

        private RejectedExecutionHandlerWrapper(RejectedExecutionHandler provider) {
            this.provider = provider;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            /*
                如果shutdown状态, 则不抛出异常
             */
            try {
                provider.rejectedExecution(r, executor);
            } catch (Throwable t) {
                if (isShutdown){
                    return;
                }
                throw t;
            }
        }

        private void shutdown(){
            isShutdown = true;
        }

    }

    public interface ExecuteListener {

        /**
         * 在Runnable执行前调用
         * @param t 线程
         * @param r runnable
         */
        void beforeExecute(Thread t, Runnable r);

        /**
         * 在Runnable执行后调用
         * @param r runnable
         * @param t 线程
         */
        void afterExecute(Runnable r, Throwable t);

    }

}
