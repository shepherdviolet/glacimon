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

package sviolet.thistle.model.concurrent;

import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>负载执行器(用于压测)</p>
 * <p>多线程执行同一个任务, 可以实时调整并发数/时间间隔, 需要调用start方法启动, 调用close方法停止.</p>
 * <p></p>
 * <p>注意:</p>
 * <p>1.设置完毕后, 调用start方法启动.</p>
 * <p>2.记得调用close方法停止, 但执行线程不会立刻终止, 要等单个任务执行完毕才会停止.</p>
 * <p>3.配合Apollo之类的配置中心使用更佳, 执行参数可以实时调整(实时生效).</p>
 * <p>4.压测的时候, 关闭程序的日志输出能大大提高TPS, 可以只保留必要的日志写盘. 日志输出到磁盘性能影响巨大, 输出到Console性能影响较小.</p>
 *
 * @author shepherdviolet
 */
public class LoadRunner implements AutoCloseable, Closeable {

    private final Task task;

    private volatile boolean started = false;
    private volatile int maxThreadNum = 0;
    private volatile int intervalMillis = 0;

    private volatile long startupDelay = 0L;
    private volatile long createThreadDelay = 0L;

    private final AtomicInteger currentThreadNum = new AtomicInteger(0);

    private final ExecutorService dispatcherThreadPool = ThreadPoolExecutorUtils.createLazy(10, "LoadRunner-dispatcher");
    private final ExecutorService workerThreadPool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 10, "LoadRunner-worker-%d");

    /**
     * @param task 执行的任务
     * @param maxThreadNum 最大线程数
     * @param intervalMillis 单线程执行间隔
     */
    public LoadRunner(Task task, int maxThreadNum, int intervalMillis) {
        if (task == null) {
            throw new IllegalArgumentException("task is null");
        }
        if (maxThreadNum < 0) {
            throw new IllegalArgumentException("maxThreadNum < 0");
        }
        if (intervalMillis < 0) {
            throw new IllegalArgumentException("intervalMillis < 0");
        }

        this.task = task;
        this.maxThreadNum = maxThreadNum;
        this.intervalMillis = intervalMillis;
    }

    /**
     * 设置最大执行线程数
     * @param maxThreadNum 最大执行线程数
     */
    public LoadRunner setMaxThreadNum(int maxThreadNum) {
        this.maxThreadNum = maxThreadNum;
        if (started) {
            dispatcherThreadPool.execute(DISPATCH_TASK);
        }
        return this;
    }

    /**
     * 设置每个线程执行任务的间隔
     * @param intervalMillis 每个线程执行任务的间隔, ms
     */
    public LoadRunner setIntervalMillis(int intervalMillis) {
        this.intervalMillis = intervalMillis;
        return this;
    }

    /**
     * 设置启动延迟, 每次启动(start)或追加线程(setMaxThreadNum)前, 会延迟指定时间
     * @param startupDelay 启动延迟, ms
     */
    public LoadRunner setStartupDelay(long startupDelay){
        this.startupDelay = startupDelay;
        return this;
    }

    /**
     * 设置线程创建延迟, 每创建一个新的执行线程前, 会延迟指定时间
     * @param createThreadDelay 线程创建延迟, ms
     */
    public LoadRunner setCreateThreadDelay(long createThreadDelay){
        this.createThreadDelay = createThreadDelay;
        return this;
    }

    /**
     * 启动
     */
    public LoadRunner start() {
        started = true;
        dispatcherThreadPool.execute(DISPATCH_TASK);
        return this;
    }

    /**
     * 停止, 不会打断执行中的线程
     */
    @Override
    public void close() {
        started = false;
    }

    public int getMaxThreadNum() {
        return maxThreadNum;
    }

    public int getIntervalMillis() {
        return intervalMillis;
    }

    public int getCurrentThreadNum() {
        return currentThreadNum.get();
    }

    private final Runnable DISPATCH_TASK = new Runnable() {
        @Override
        public void run() {
            if (startupDelay > 0L) {
                try {
                    Thread.sleep(startupDelay);
                } catch (InterruptedException ignore) {
                }
            }

            for (int i = 0 ; i < maxThreadNum && started && currentThreadNum.get() < maxThreadNum ; i++) {

                if (createThreadDelay > 0L) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(createThreadDelay);
                    } catch (InterruptedException ignore) {
                    }
                }

                workerThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        int id = currentThreadNum.getAndIncrement();
                        while (started && id < maxThreadNum) {
                            try {
                                if (intervalMillis > 0L) {
                                    //noinspection BusyWait
                                    Thread.sleep(intervalMillis);
                                }
                                task.onExecute(id);
                            } catch (InterruptedException ignore) {
                            } catch (Throwable t) {
                                try {
                                    task.onException(id, t);
                                } catch (Throwable ignore) {
                                }
                            }
                        }
                        currentThreadNum.getAndDecrement();
                    }
                });

            }
        }
    };

    /**
     * 任务
     */
    public interface Task {

        /**
         * 任务执行代码
         */
        void onExecute(int id);

        /**
         * 异常处理
         */
        void onException(int id, Throwable t);

    }

}
