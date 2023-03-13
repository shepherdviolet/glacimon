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

import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;
import com.github.shepherdviolet.glacimon.java.misc.DateTimeUtils;
import com.github.shepherdviolet.glacimon.java.test.LoadRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Grocer 测试
 */
public class GrocerTest {


    public static void main(String[] args) throws InterruptedException {

        // 功能测试 ////////////////////////////////////////////////////////////////////////////////////////////////////

//        test_wakeupIntervalOnFetch_errorDuration(); // 测试 wakeupIntervalOnFetch 和 errorDuration 有效性.
//        test_sweep(); // 测试清理机制

        // 性能测试: 模拟序列号发号场景 //////////////////////////////////////////////////////////////////////////////////

//        c200_p200__t200_i400_ft2000(); // 模拟序列号发号场景, 缓存容量比建议值低50%
//        c300_p200__t200_i400_ft2000(); // 模拟序列号发号场景, 缓存容量比建议值低25%
        c400_p200__t200_i400_ft2000(); // [基准性能测试] 模拟序列号发号场景, 缓存容量按建议值设置

        // 压力测试:补货耗时的影响 ///////////////////////////////////////////////////////////////////////////////////////

//        c40_p800__t100_i0_ft2000();
//        c40_p400__t100_i0_ft2000();
//        c40_p200__t100_i0_ft2000();
//        c40_p100__t100_i0_ft2000();

        // 压力测试:缓存容量的影响 ///////////////////////////////////////////////////////////////////////////////////////

//        c100_p800__t100_i0_ft2000();
//        c1000_p800__t100_i0_ft2000();

    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 功能测试
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 测试 wakeupIntervalOnFetch 和 errorDuration 有效性.
     * 需要测试两轮!!!
     * 第一轮预期: 注释掉 //.setWakeupIntervalOnFetch(5000L) 这行, 程序运行5秒钟后出现"Error has been added",
     * 注意观察第一条([ERROR] Purchase failed), 这条记录中的occurred时间和printed时间应该相差约1秒(也有可能小于1秒, 可以多测几次),
     * 这说明wakeupIntervalOnFetch有效, 注意观察最后一条([ERROR] Purchase failed), 故障持续时间应该是30秒
     * 第一轮预期: 不要注释 .setWakeupIntervalOnFetch(5000L) 这行, 程序运行5秒钟后出现"Error has been added",
     * 注意观察第一条([ERROR] Purchase failed), 这条记录中的occurred时间和printed时间应该相差约5秒(也有可能小于5秒, 可以多测几次),
     * 这说明wakeupIntervalOnFetch有效, 注意观察最后一条([ERROR] Purchase failed), 故障持续时间应该是30秒
     */
    private static void test_wakeupIntervalOnFetch_errorDuration() throws InterruptedException {
        AtomicLong purchaseDuration = new AtomicLong(200);
        AtomicInteger errorTimes = new AtomicInteger(0);
        AtomicLong errorUtil = new AtomicLong(0);
        Grocer<String, MyException> grocer = createGrocerWithError(purchaseDuration, 30000, errorTimes, errorUtil, true)
//                .setWakeupIntervalOnFetch(5000L)// 注意!!! 注意!!! 注意!!! 第一轮测试注掉这行, 第二轮测试不要注释这行
                .setCacheSize(4)// 缓存设为最小, 使得设置错误后, 尽快耗尽缓存, 进入线程挂起等待
                .setMaxPurchaseQuantity(4)// 限制缺货增大补货数量的机制, 使得设置错误后, 尽快耗尽缓存, 进入线程挂起等待
                .setElementExpireSeconds(30);
        LoadRunner client = createLoadRunner(grocer, "1", 60000, 5000)
                .setPrintError(true)
                .setWaitAfterError(500L)// 如果fetch出错了, 就等一会再fetch (防止错误日志刷太快)
                .setStartupDelay(500L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(1)// 一个fetch线程容易观察现象
                .setIntervalMillis(0)// 必须设置间隔0才容易测试出预期的结果, 因为在设置错误后, 缓存中还有元素, 等取完触发补货(补货失败)时, fetch线程很可能没有在等待队列, 这样就测不出被挂住直到wakeupIntervalOnFetch超时了
                .start();

        Thread.sleep(5 * 1000L);
        errorTimes.set(1);
        System.out.println("Error has been added " + DateTimeUtils.currentDateTimeString());

        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 测试清理机制.
     * 预期: 高速获取元素5秒后, 进入慢速获取状态(每秒钟1次), 等待第一次清扫日志, 在清扫日志中, 可以观察到上千个元素被过期掉了(因为之
     * 前缺货状态Grocer增加了补货量的关系), 留下40个元素; 等待第二次清扫日志, 在清扫日志中, 可以观察到有10个元素被过期了, 因为半分钟
     * 只消费了30个, 但缓冲容量是40, 为了保证元素存在时间小于有效期限制, 需要清理10个才行.
     */
    private static void test_sweep() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(200, true)
                .setElementExpireSeconds(30);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 5000)
                .setStartupDelay(500L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();

        Thread.sleep(5 * 1000L);
        client.setMaxThreadNum(1).setIntervalMillis(1000);
        System.out.println("Set fetch thread num to 1, interval 1000");

        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 性能测试: 模拟序列号发号场景
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 模拟序列号发号场景, fetch线程200, fetch间隔400ms,
     * 缓存容量比建议值低50%
     *
     * 期望: 平均耗时svgSpent越低越好, 实测6000-22000us
     * 期望: 失败率failureRate低, 实测0.0
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c200_p200__t200_i400_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(200L, true)
                .setCacheSize(200)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(200)
                .setIntervalMillis(400)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 模拟序列号发号场景, fetch线程200, fetch间隔400ms,
     * 缓存容量比建议值低25%
     *
     * 期望: 平均耗时svgSpent越低越好, 实测3000-13000us
     * 期望: 失败率failureRate低, 实测0.0
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c300_p200__t200_i400_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(200L, true)
                .setCacheSize(300)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(200)
                .setIntervalMillis(400)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 模拟序列号发号场景, fetch线程200, fetch间隔400ms,
     * 缓存容量按建议值设置: 2 * fetch平均速率(每秒) * 补货平均耗时(秒) / 补货阈值(purchaseThresholdPercent)
     *
     * 期望: 平均耗时svgSpent越低越好, 实测500-8000us
     * 期望: 失败率failureRate低, 实测0.0
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c400_p200__t200_i400_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(200L, true)
                .setCacheSize(400)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(200)
                .setIntervalMillis(400)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 压力测试: 补货耗时的影响
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 补货耗时的影响, 补货耗时800ms
     * 期望: 每次的补货数<Purchase>虽然会波动, 但整体上涨, 最大10000
     * 期望: 平均耗时svgSpent逐渐降低, 最初十秒实测约 126171us, 稳定后实测约 15420us
     * 期望: 失败率failureRate低, 最初十秒实测约 0.0368, 稳定后实测约 0.0005
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c40_p800__t100_i0_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(800L, true)
                .setCacheSize(40)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 补货耗时的影响, 补货耗时400ms
     * 期望: 每次的补货数<Purchase>虽然会波动, 但整体上涨, 最大10000
     * 期望: 平均耗时svgSpent逐渐降低, 最初十秒实测约 44352us, 稳定后实测约 5589us
     * 期望: 失败率failureRate低, 最初十秒实测约 0.0039, 稳定后实测约 0.00001
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c40_p400__t100_i0_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(400L, true)
                .setCacheSize(40)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 补货耗时的影响, 补货耗时200ms
     * 期望: 每次的补货数<Purchase>虽然会波动, 但整体上涨, 最大10000
     * 期望: 平均耗时svgSpent逐渐降低, 最初十秒实测约 15108us, 稳定后实测约 2904us
     * 期望: 失败率failureRate低, 最初十秒实测约 0.0, 稳定后实测约 0.0
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c40_p200__t100_i0_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(200L, true)
                .setCacheSize(40)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 补货耗时的影响, 补货耗时100ms
     * 期望: 每次的补货数<Purchase>虽然会波动, 但整体上涨, 最大10000
     * 期望: 平均耗时svgSpent逐渐降低, 最初十秒实测约 4659us, 稳定后实测约 1250us
     * 期望: 失败率failureRate低, 最初十秒实测约 0.0, 稳定后实测约 0.0
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c40_p100__t100_i0_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(100L, true)
                .setCacheSize(40)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 压力测试: 缓存容量的影响
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 缓存容量的影响, 缓存容量100
     * 期望: 每次的补货数<Purchase>虽然会波动, 但整体上涨, 最大10000
     * 期望: 平均耗时svgSpent逐渐降低, 最初十秒实测约 72724us, 稳定后实测约 9533us
     * 期望: 失败率failureRate低, 最初十秒实测约 0.0177, 稳定后实测约 0.00016
     * 期望: 观察"Statistic report"状况良好
     */
    private static void c100_p800__t100_i0_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(800L, true)
                .setCacheSize(100)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    /**
     * 缓存容量的影响, 缓存容量1000
     * 期望: 每次的补货数<Purchase>虽然会波动, 但整体上涨, 最大10000
     * 期望: 平均耗时svgSpent逐渐降低, 最初十秒实测约 15324us, 稳定后实测约 16702us
     * 期望: 失败率failureRate低, 最初十秒实测约 0.0010, 稳定后实测约 0.0013
     * 期望: 观察"Statistic report"状况良好
     * NOTE: 缓存大了稳定后反而性能下降, 因为CPU核心数有限(双核), 一次补货10000个前面fetch消费的慢, 第二次会补货几百个, 又不够了出现
     * 间歇性地等待
     */
    private static void c1000_p800__t100_i0_ft2000() throws InterruptedException {
        Grocer<String, MyException> grocer = createGrocerNoError(800L, true)
                .setCacheSize(1000)
                .setPurchaseThresholdPercent(0.5f)
                .setPurchaseThreadNum(16)
                .setMaxPurchaseQuantity(10000)
                .setElementExpireSeconds(60);
        LoadRunner client = createLoadRunner(grocer, "1", 2000, 10000)
                .setStartupDelay(1000L)
                .setCreateThreadDelay(0)
                .setMaxThreadNum(100)
                .setIntervalMillis(0)
                .start();
        Thread.sleep(10 * 60 * 1000L);
        client.close();
        grocer.close();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 通用逻辑
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Grocer<String, MyException> createGrocerNoError(long purchaseDuration, boolean printLog) {
        return createGrocerNoError(new AtomicLong(purchaseDuration), printLog);
    }

    private static Grocer<String, MyException> createGrocerNoError(AtomicLong purchaseDuration, boolean printLog) {
        return new Grocer<>(new Grocer.Purchaser<String, MyException>() {
            @Override
            public Grocer.Goods<String, MyException> purchase(String key, int quantity) {
                try {
                    Thread.sleep(purchaseDuration.get());
                } catch (InterruptedException ignore) {
                }
                List<String> elements = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    elements.add("FOO");
                }
                if (printLog) {
                    System.out.println("<Purchase> " + quantity);
                }
                return Grocer.Goods.success(elements);
            }
        });
    }

    private static Grocer<String, MyException> createGrocerWithError(AtomicLong purchaseDuration,
                                                                     long errorDuration, AtomicInteger errorTimes, AtomicLong errorUntil,
                                                                     boolean printLog) {
        return new Grocer<>(new Grocer.Purchaser<String, MyException>() {
            @Override
            public Grocer.Goods<String, MyException> purchase(String key, int quantity) {
                try {
                    Thread.sleep(purchaseDuration.get());
                } catch (InterruptedException ignore) {
                }
                int eTimes = eTimes = errorTimes.getAndDecrement();
                if (eTimes > 0) {
                    return Grocer.Goods.fail(new MyException("Purchase failed (" + DateTimeUtils.currentDateTimeString() + " occurred)"), errorDuration);
                } else  {
                    errorTimes.getAndIncrement();
                }
                if (System.currentTimeMillis() < errorUntil.get()) {
                    return Grocer.Goods.fail(new MyException("Purchase failed (" + DateTimeUtils.currentDateTimeString() + " occurred)"), errorDuration);
                }
                List<String> elements = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    elements.add("FOO");
                }
                if (printLog) {
                    System.out.println("<Purchase> " + quantity);
                }
                return Grocer.Goods.success(elements);
            }
        });
    }

    private static FetchLoadRunner createLoadRunner(Grocer<?, ?> grocer, String fetchKey, int fetchTimeout, long printInterval) {
        return new FetchLoadRunner(grocer, fetchKey, fetchTimeout, printInterval);
    }

    private static class FetchLoadRunner extends LoadRunner implements LoadRunner.Task {

        private final Grocer<?, ?> grocer;
        private final String fetchKey;
        private final int fetchTimeout;
        private boolean printError = false;
        private long waitAfterError = 0;

        private final AtomicLong fetchTotalTimeSpent = new AtomicLong(0);
        private final AtomicLong fetchReturnCounter = new AtomicLong(0);
        private final AtomicLong fetchErrorCounter = new AtomicLong(0);

        private final ScheduledExecutorService printThreadPool = ThreadPoolExecutorUtils.createScheduled(1, "FetchLoadRunner-pring-%s");

        public FetchLoadRunner(Grocer<?, ?> grocer, String fetchKey, int fetchTimeout, long printInterval) {
            super.setTask(this);
            this.grocer = grocer;
            this.fetchKey = fetchKey;
            this.fetchTimeout = fetchTimeout;

            if (printInterval > 0) {
                printThreadPool.scheduleAtFixedRate(() -> {
                    long returnCounter = fetchReturnCounter.get();
                    long errorCounter = fetchErrorCounter.get();
                    long timeSpent = fetchTotalTimeSpent.get();
                    long counter = returnCounter + errorCounter;
                    double averageSpent = (double)timeSpent / (double)counter;
                    double failureRate = (double)errorCounter / (double)counter;
                    System.out.println("<Fetch> [" + DateTimeUtils.currentDateTimeString() + "] avgSpent:" + averageSpent + "us, failureRate:" + failureRate);
                }, printInterval, printInterval, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public LoadRunner setTask(Task task) {
            throw new UnsupportedOperationException("Unsupported method setTask");
        }

        @Override
        public void close() {
            super.close();
            try {
                printThreadPool.shutdownNow();
            } catch (Throwable ignore) {
            }
        }

        @Override
        public void onExecute(int id) throws Throwable {
            long time = System.nanoTime();
            try {
                grocer.fetch(fetchKey, fetchTimeout);
                fetchReturnCounter.getAndIncrement();
                fetchTotalTimeSpent.getAndAdd((System.nanoTime() - time) / 1000L);
            } catch (Throwable t) {
                fetchErrorCounter.getAndIncrement();
                fetchTotalTimeSpent.getAndAdd((System.nanoTime() - time) / 1000L);
                if (printError) {
                    System.out.println("[ERROR] " + t.getMessage() + " (" + DateTimeUtils.currentDateTimeString() + " printed)");
                }
                if (waitAfterError > 0L) {
                    Thread.sleep(waitAfterError);
                }
            }
        }

        @Override
        public void onException(int id, Throwable t) {
        }

        public FetchLoadRunner setPrintError(boolean printError) {
            this.printError = printError;
            return this;
        }

        public FetchLoadRunner setWaitAfterError(long waitAfterError) {
            this.waitAfterError = waitAfterError;
            return this;
        }

        public long getFetchTotalTimeSpent() {
            return fetchTotalTimeSpent.get();
        }

        public long getFetchReturnCounter() {
            return fetchReturnCounter.get();
        }

        public long getFetchErrorCounter() {
            return fetchErrorCounter.get();
        }
    }

    private static class MyException extends Exception {
        public MyException(String message) {
            super(message);
        }
        public MyException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
