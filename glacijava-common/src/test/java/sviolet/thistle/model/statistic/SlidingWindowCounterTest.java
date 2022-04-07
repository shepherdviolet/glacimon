/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.model.statistic;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class SlidingWindowCounterTest {

    /**
     * 简单测试
     */
    @Test
    public void test(){

        //每个样本10s
        final int SAMPLING_DURATION = 10000;
        //窗口总共30个样本
        final int STATISTICAL_PERIOD = 30;

        SlidingWindowCounter counter = new SlidingWindowCounter(SAMPLING_DURATION, STATISTICAL_PERIOD, 10) {
//            @Override
//            protected void printDebugLog(int duration, long currentTimeMillis, List<SlidingWindowArray.Element<AtomicInteger>> elements) {
//                System.out.println("SlidingWindowCounter#getRecently(" + duration + ", " + currentTimeMillis + ")");
//                for (SlidingWindowArray.Element<AtomicInteger> element : elements) {
//                    System.out.println(element);
//                }
//            }
        };

        //5 + 1 + 1
        counter.addAndGet(1, 0);
        counter.addAndGet(1, 2000);
        counter.addAndGet(1, 3000);
        counter.addAndGet(1, 1000);//小抖动
        counter.addAndGet(1, 5000);

        //3
        counter.addAndGet(1, 11000);
        counter.addAndGet(1, 12000);
        counter.addAndGet(1, 8000);//大抖动
        counter.addAndGet(1, 15000);

        //2
        counter.addAndGet(1, 21000);
        counter.addAndGet(1, 5000);//大抖动
        counter.addAndGet(1, 22000);

        Assert.assertEquals(2, counter.getRecently(1 * SAMPLING_DURATION, 30000));
        Assert.assertEquals(5, counter.getRecently(2 * SAMPLING_DURATION, 30000));
        Assert.assertEquals(12, counter.getRecently(Integer.MAX_VALUE, 30000));

        //2 * 2
        counter.addAndGet(2, 41000);
        counter.addAndGet(2, 31000);//大抖动

        Assert.assertEquals(2, counter.getRecently(1 * SAMPLING_DURATION, 50000));
        Assert.assertEquals(4, counter.getRecently(2 * SAMPLING_DURATION, 50000));
        Assert.assertEquals(6, counter.getRecently(3 * SAMPLING_DURATION, 50000));
        Assert.assertEquals(16, counter.getRecently(Integer.MAX_VALUE, 50000));

        //2
        counter.addAndGet(1, 301000);
        counter.addAndGet(1, 291000);//大抖动

        Assert.assertEquals(1, counter.getRecently(1 * SAMPLING_DURATION, 310000));
        Assert.assertEquals(2, counter.getRecently(2 * SAMPLING_DURATION, 310000));
        Assert.assertEquals(8, counter.getRecently(Integer.MAX_VALUE, 310000));

        //1
        counter.addAndGet(1, 311000);

        Assert.assertEquals(1, counter.getRecently(1 * SAMPLING_DURATION, 320000));
        Assert.assertEquals(2, counter.getRecently(2 * SAMPLING_DURATION, 320000));
        Assert.assertEquals(7, counter.getRecently(Integer.MAX_VALUE, 320000));

        //1
        counter.addAndGet(1, 0);//时间倒流的话, 计数无效

        Assert.assertEquals(0, counter.getRecently(1 * SAMPLING_DURATION, 10000));//时间回退的话, 取值也无效
        Assert.assertEquals(0, counter.getRecently(2 * SAMPLING_DURATION, 10000));//时间回退的话, 取值也无效
        Assert.assertEquals(7, counter.getRecently(Integer.MAX_VALUE, 320000));

        for (int i = 0 ; i < 10 ; i++) {
            counter.addAndGet(1, 0);//大量的时间倒流可以导致整个重置(默认64次)
        }

        Assert.assertEquals(1, counter.getRecently(Integer.MAX_VALUE, 10000));

    }

    /**
     * 并发测试
     */
    public static void main(String[] args) {
        stressTest();
    }

    private static final int STRESS_THREADS = 100;
    private static final int RUN_DURATION = 20000;
    private static final int FINISH_DURATION = RUN_DURATION + 3000;

    /**
     * 压测
     */
    private static void stressTest() {

        //采样时长1s, 统计时长60s
        final SlidingWindowCounter counter = new SlidingWindowCounter(1000, 60) {
//            @Override
//            protected void printDebugLog(int duration, long currentTimeMillis, List<SlidingWindowArray.Element<AtomicInteger>> elements) {
//                System.out.println("SlidingWindowCounter#getRecently(" + duration + ", " + currentTimeMillis + ")");
//                for (SlidingWindowArray.Element<AtomicInteger> element : elements) {
//                    System.out.println(element);
//                }
//            }
        };

        //总计数
        final AtomicInteger times = new AtomicInteger(0);

        //计数线程
        for (int i = 0 ; i < STRESS_THREADS ; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException ignored) {
                    }
                    long time = System.currentTimeMillis();
                    while (System.currentTimeMillis() - time < RUN_DURATION) {
                        counter.addAndGet(1);
                        times.getAndIncrement();
                    }
                }
            }).start();
        }

        //等待计数完毕
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < FINISH_DURATION) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ignored) {
            }
        }

        //调用次数
        System.out.println("Invoke Total: " + times.get());
        //计数器里采集到的次数
        System.out.println("Count Total: " + counter.getTotally());

        for (int i = 1 ; i <= 60 ; i++) {
            //最近i秒内的计数值, 刚开始会有一两秒为0(因为FINISH_DURATION比RUN_DURATION大)
            System.out.println(i + ": " + counter.getRecently(i * 1000));
        }

    }

}
