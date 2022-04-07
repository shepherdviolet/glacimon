/*
 * Copyright (C) 2015-2020 S.Violet
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>挂起当前线程等待计数器归0(类似于CountDownLatch).</p>
 *
 * <p>一般情况下推荐使用CountDownLatch. 但是CountDownLatch必须在开始前确定计数器初值, 如果无法确定初值, 就可以使用本实现.</p>
 *
 * <pre>
 *     // 初值为0
 *     CountDownWaiter waiter = new CountDownWaiter(0);
 *     // map是ConcurrentHashMap, 能够在多线程情况下遍历, 但遍历时map.size()会变化, 所以不能用CountDownLatch
 *     for (Map.Entry entry : map.entrySet()) {
 *          // 要在主线程countUp
 *          waiter.countUp();
 *          threadPool.execute(() -> {
 *              try {
 *                  // ......
 *              } finally {
 *                  waiter.countDown();
 *              }
 *          });
 *     }
 *     if (!waiter.await()) {
 *          // 超时处理
 *     }
 *     // 正常处理
 * </pre>
 *
 * @author S.Violet
 */
public class CountDownWaiter {

    /**
     * 超时时间ms
     */
    private final long timeout;

    /**
     * 计数器归零就结束等待
     */
    private final AtomicInteger counter;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * 无限等待
     *
     * @param initCountNum 初始计数器值, 该值归零就结束等待
     */
    public CountDownWaiter(int initCountNum) {
        this(initCountNum, 0);
    }

    /**
     * 有限等待
     *
     * @param initCountNum 初始计数器值, 该值归零就结束等待
     * @param timeout      超时时间(ms), 该参数<=0时无限等待
     */
    public CountDownWaiter(int initCountNum, long timeout) {
        this.timeout = timeout;
        counter = new AtomicInteger(initCountNum);
    }

    /**
     * [原线程]计数值+1
     */
    public void countUp() {
        counter.getAndIncrement();
    }

    /**
     * [子线程]计数值-1
     */
    public void countDown() {
        lock.lock();
        try {
            int count = counter.decrementAndGet();
            if (count <= 0) {
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        return counter.get();
    }

    /**
     * [原线程]挂起当前线程等待计数器归0, 注意如果await时计数值已经是0的话会直接返回
     *
     * @return true: 计数器归0, false: 计数器未归0, 超时了
     */
    public boolean await() throws InterruptedException {
        final long deadLine = System.currentTimeMillis() + timeout;
        lock.lock();
        try {
            if (timeout > 0) {
                while (counter.get() > 0) {
                    final long remainTimeout = deadLine - System.currentTimeMillis();
                    if (remainTimeout <= 0) {
                        return false;
                    }
                    if (!condition.await(remainTimeout, TimeUnit.MILLISECONDS)) {
                        return false;
                    }
                }
            } else {
                while (counter.get() > 0) {
                    condition.await();
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

}
