//package sviolet.slate.common.model.pool;
//
//import ch.qos.logback.classic.Level;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import sviolet.slate.common.helper.logback.LogbackHelper;
//import sviolet.slate.common.util.common.LambdaBuilder;
//import sviolet.thistle.compat.concurrent.CompatThreadFactoryBuilder;
//import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * DiscardableSingletonPool测试
// *
// *
// */
//public class DiscardableSingletonPoolTest {
//
//    private static final Logger logger = LoggerFactory.getLogger(DiscardableSingletonPoolTest.class);
//
//    private static final ScheduledExecutorService executorService = ThreadPoolExecutorUtils.createScheduled(1,
//            new CompatThreadFactoryBuilder().setNameFormat("mock-schedule-%d").setDaemon(true).build());
//
//    public static void main(String[] args) {
//
//        LogbackHelper.setLevel("root", Level.ALL);
//
//        test1();
////        test2();
////        test3();
////        test4();
////        test5();
////        test6();
////        test9();
//
//    }
//
//    // 测试的时候看客户端是不是走对, 没走到别的客户端里, 和service的统计信息对比即可
//    private static final Map<String, AtomicInteger> clientPostCountMap = new ConcurrentHashMap<>();
//
//    // 测试的时候看看是否存在调用被"销毁"的实例的情况, 正常应该是0
//    private static final AtomicInteger postToClosedClient = new AtomicInteger(0);
//
//
//    /**
//     * 无特殊设置, 正常压测, 20%失败率, 看看discard/destroy逻辑是否正常
//     * 对比Tester统计信息里, 每个id的发送量, 和Mock-Client统计信息里的是否相符, 看看会不会发错client
//     * 对比Mock-Client和Pool统计信息, 看看Pool统计信息里创建次数, 是否和Mock-Client统计信息里实际创建次数相等
//     * 对比Mock-client和Pool统计信息, 看看Pool统计信息里丢弃/销毁的数量, 是否和Mock-Client统计信息里实际销毁次数相等
//     * 观察pool统计信息, 看看所有丢弃的是否都被销毁
//     * 观察tester统计信息, postToClosedClient是不是0 (不为0说明有调用被销毁的实例)
//     */
//    private static void test1() {
//        // 80成功率
//        DiscardableSingletonPoolSampleService service = createService(8000);
//        // 5s一次打印统计信息
//        setPrintStatistic(service, 5000L);
//        // 20s一次丢弃全部 (测试稳定性)
//        configDiscardAllForTest(service, 20000L);
//
//        // 跑2分钟
//        long time = System.currentTimeMillis();
//        long duration = 120L * 1000L;
//
//        // 多线程
//        for (int i = 0 ; i < 128 ; i++) {
//            final int id = i;
//            new Thread(() -> {
//                while (System.currentTimeMillis() - time < duration) {
//                    // 计算key
//                    String key = String.valueOf(id % 4);
//                    // 统计
//                    clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//                    // 发送请求
//                    post(service, key);
//                }
//            }).start();
//        }
//
//        // 等待结束
//        while (System.currentTimeMillis() - time < duration + 2000L) {
//            try {
//                //noinspection BusyWait
//                Thread.sleep(1000L);
//            } catch (InterruptedException ignore) {
//            }
//        }
//
//        // 打印日志
//        printStatistic(service);
//        logger.info("Finished");
//
//    }
//
//    /**
//     * 设置了20秒一次全部丢弃, 查看全部丢弃功能是否正常, 总共会丢6次, 4*6=24个实例
//     */
//    private static void test2() {
//        // 100成功率
//        DiscardableSingletonPoolSampleService service = createService(10000);
//        // 20s一次丢弃全部 (测试功能)
//        configDiscardAllForTest(service, 20000L);
//
//        // 跑2分钟
//        long time = System.currentTimeMillis();
//        long duration = 120L * 1000L;
//
//        // 多线程
//        for (int i = 0 ; i < 4 ; i++) {
//            final int id = i;
//            new Thread(() -> {
//                while (System.currentTimeMillis() - time < duration) {
//                    // 这里慢慢跑
//                    try {
//                        //noinspection BusyWait
//                        Thread.sleep(1000L);
//                    } catch (InterruptedException ignore) {
//                    }
//                    // 计算key
//                    String key = String.valueOf(id % 4);
//                    // 统计
//                    clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//                    // 发送请求
//                    post(service, key);
//                }
//            }).start();
//        }
//
//        // 等待结束同时打印统计日志
//        while (System.currentTimeMillis() - time < duration + 2000L) {
//            try {
//                //noinspection BusyWait
//                Thread.sleep(5000L);
//            } catch (InterruptedException ignore) {
//            }
//            printStatistic(service);
//        }
//
//        logger.info("Finished");
//
//    }
//
//    /**
//     * [LowUsage功能测试]
//     * 设置了60秒检查一次, 50s没用的实例被销毁 (0和1被丢弃并销毁)
//     */
//    private static void test3() {
//        // 100成功率
//        DiscardableSingletonPoolSampleService service = createService(10000);
//        // 60s检查一次, 丢弃50s没用的对象
//        configDiscardLowUsageInstances(service, 60000L);
//
//        // 跑2分钟
//        long time = System.currentTimeMillis();
//        long duration = 120L * 1000L;
//
//        // 往客户端0-1发送1次请求
//        for (int i = 0 ; i < 2 ; i++) {
//            // 计算key
//            String key = String.valueOf(i % 4);
//            // 统计
//            clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//            // 发送请求
//            post(service, key);
//        }
//
//        // 持续往客户端2-3发送请求
//        for (int i = 2 ; i < 4 ; i++) {
//            final int id = i;
//            new Thread(() -> {
//                while (System.currentTimeMillis() - time < duration) {
//                    // 慢慢发
//                    try {
//                        //noinspection BusyWait
//                        Thread.sleep(1000L);
//                    } catch (InterruptedException ignore) {
//                    }
//                    // 计算key
//                    String key = String.valueOf(id % 4);
//                    // 统计
//                    clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//                    // 发送请求
//                    post(service, key);
//                }
//            }).start();
//        }
//
//        // 等待结束同时打印统计日志
//        while (System.currentTimeMillis() - time < duration + 2000L) {
//            try {
//                //noinspection BusyWait
//                Thread.sleep(10000L);
//            } catch (InterruptedException ignore) {
//            }
//            printStatistic(service);
//        }
//
//        logger.info("Finished");
//
//    }
//
//    /**
//     * [ForceDestroy功能测试]
//     * 用错误的方式发送请求, 故意在对象使用完毕后不释放引用, 测试"强制销毁"功能是否正常
//     * 最后销毁2个, 强制销毁2个
//     */
//    private static void test4() {
//        // 100成功率
//        DiscardableSingletonPoolSampleService service = createService(10000);
//        // 15s检查一次, 强制销毁被丢弃超过30s的对象
//        configForceDestroy(service, 30000L);
//
//        // 跑70秒
//        long time = System.currentTimeMillis();
//        long duration = 70L * 1000L;
//
//        // 往客户端0-1发送1次请求
//        for (int i = 0 ; i < 2 ; i++) {
//            // 计算key
//            String key = String.valueOf(i % 4);
//            // 统计
//            clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//            /*
//             * 错误方式发送请求
//             *
//             * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
//             * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
//             * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
//             *
//             * 这里的代码用于测试, 实际千万别这么写!!!
//             */
//            incorrectWayToPost(service, key);
//        }
//
//        // 错误的方式发送后, 把它们全丢弃了, 如果没设置强制销毁的话, 这些对象会一直呆在"丢弃池"里不会被销毁, 但是这个测试里设置了强制销毁, 所以在指定时间后会被销毁
//        service.discardAllClient();
//
//        // 等待结束同时打印统计日志
//        while (System.currentTimeMillis() - time < duration + 2000L) {
//            try {
//                //noinspection BusyWait
//                Thread.sleep(10000L);
//            } catch (InterruptedException ignore) {
//            }
//            printStatistic(service);
//        }
//
//        logger.info("Finished");
//
//    }
//
//    /**
//     * [DiscardAllEveryday功能测试]
//     * [有点麻烦要改时间]
//     * 测试每天全部丢弃功能, 这个测试有点麻烦要改系统时间
//     * 1.修改到01:59, 启动程序等待约两分钟, 看看是否全部丢弃, 再等待一分钟, 不应该触发第二次全部丢弃
//     * 2.修改到其他时间, 启动程序等待约两分钟, 不应该触发全部丢弃
//     *
//     * 可以搜索'Discard-'观察日志
//     */
//    private static void test5() {
//        // 100成功率
//        DiscardableSingletonPoolSampleService service = createService(10000);
//        // 设置每天凌晨2点全部丢弃一次
//        configDiscardAllEveryday(service, 2);
//
//        // 跑2分钟
//        long time = System.currentTimeMillis();
//        long duration = 240L * 1000L;
//
//        // 等待结束, 同时慢慢发请求, 同时打印统计日志
//        while (System.currentTimeMillis() - time < duration + 2000L) {
//            // 往客户端0-3发送1次请求
//            for (int i = 0 ; i < 4 ; i++) {
//                // 计算key
//                String key = String.valueOf(i % 4);
//                // 统计
//                clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//                // 发送请求
//                post(service, key);
//            }
//            // 等待
//            try {
//                //noinspection BusyWait
//                Thread.sleep(10000L);
//            } catch (InterruptedException ignore) {
//            }
//            printStatistic(service);
//        }
//
//        logger.info("Finished");
//    }
//
//    /**
//     * 疯狂地设置定时任务, 观察会不会出现定时任务变多的问题 (正常来说改变定时时间后, 每个定时任务还是只有一份)
//     *      0.启动
//     *      1.观察日志"Execute NotifyDestroyDiscardedInstances", 稳定后应该是每10s一次
//     *      2.观察日志"Execute DiscardLowUsageInstances", 稳定后应该是20s一次
//     *      3.观察日志"Check DiscardAllEveryday", 稳定后应该是60s一次
//     */
//    private static void test6() {
//        // 100成功率
//        DiscardableSingletonPoolSampleService service = createService(10000);
//
//        // 跑2分钟
//        long time = System.currentTimeMillis();
//        long duration = 120L * 1000L;
//
//        // 多线程
//        for (int i = 0 ; i < 4 ; i++) {
//            final int id = i;
//            new Thread(() -> {
//                while (System.currentTimeMillis() - time < duration) {
//                    // 这里慢慢跑
//                    try {
//                        //noinspection BusyWait
//                        Thread.sleep(1000L);
//                    } catch (InterruptedException ignore) {
//                    }
//                    // 计算key
//                    String key = String.valueOf(id % 4);
//                    // 统计
//                    clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
//                    // 发送请求
//                    post(service, key);
//                }
//            }).start();
//        }
//
//        new Thread(() -> {
//            for (int i = 1000 ; i <= 10000 ; i+=1000) {
//                // 测试1
//                configForceDestroy(service, i);
//                // 测试2
//                configDiscardLowUsageInstances(service, 10000 + i);
//                // 测试3
//                configDiscardAllEveryday(service, i % 24);
//            }
//        }).start();
//
//        // 等待结束同时打印统计日志
//        while (System.currentTimeMillis() - time < duration + 2000L) {
//            try {
//                //noinspection BusyWait
//                Thread.sleep(5000L);
//            } catch (InterruptedException ignore) {
//            }
//            printStatistic(service);
//        }
//
//        logger.info("Finished");
//    }
//
//    /**
//     * 测试能否正确抛出对象创建异常
//     */
//    private static void test9() {
//        DiscardableSingletonPoolSampleService service = createService(10000);
//        //这个会制造创建异常
//        service.setClientCreateParams(LambdaBuilder.hashMap(m -> {
//            m.put("error", new DiscardableSingletonPoolSampleService.ClientCreateParam(10000)); // 测试创建异常能否正常抛出
//        }));
//        //捕获期望中的异常
//        try {
//            post(service, "error");
//        } catch (DiscardableSingletonPool.InstanceCreateException e) {
//            // 这里抛异常才正确
//            logger.info("抛出了正确的异常");
//            return;
//        }
//        throw new RuntimeException("没有正确抛出异常");
//    }
//
//
//
//    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//    /**
//     * 创建服务
//     * @param postSuccessRate post成功率, [0, 10000]
//     */
//    private static DiscardableSingletonPoolSampleService createService(int postSuccessRate) {
//        DiscardableSingletonPoolSampleService service = new DiscardableSingletonPoolSampleService();
//
//        // 模拟注入创建参数
//        service.setClientCreateParams(LambdaBuilder.hashMap(m -> {
//            m.put("0", new DiscardableSingletonPoolSampleService.ClientCreateParam(postSuccessRate));
//            m.put("1", new DiscardableSingletonPoolSampleService.ClientCreateParam(postSuccessRate));
//            m.put("2", new DiscardableSingletonPoolSampleService.ClientCreateParam(postSuccessRate));
//            m.put("3", new DiscardableSingletonPoolSampleService.ClientCreateParam(postSuccessRate));
//        }));
//
//        return service;
//    }
//
//    /**
//     * 发送请求
//     */
//    private static void post(DiscardableSingletonPoolSampleService service, String id) {
//        try {
//            service.post(id, new DiscardableSingletonPoolSampleService.Request());
//        } catch (DiscardableSingletonPoolSampleService.ClosedClientException e) {
//            // 不该出现的异常, 有的话说明被销毁的对象被调用了
//            postToClosedClient.getAndIncrement();
//        } catch (Throwable t) {
//            logger.error("Post error, id: " + id, t);
//        }
//    }
//
//    /**
//     * 错误方式发送请求
//     *
//     * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
//     * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
//     * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
//     *
//     * 这里的代码用于测试, 实际千万别这么写!!!
//     */
//    private static void incorrectWayToPost(DiscardableSingletonPoolSampleService service, String id) {
//        try {
//            service.incorrectWayToPost(id, new DiscardableSingletonPoolSampleService.Request());
//        } catch (DiscardableSingletonPoolSampleService.ClosedClientException e) {
//            // 不该出现的异常, 有的话说明被销毁的对象被调用了
//            postToClosedClient.getAndIncrement();
//        } catch (Throwable t) {
//            logger.error("Post error, id: " + id, t);
//        }
//    }
//
//    /**
//     * 打印统计信息
//     */
//    private static void printStatistic(DiscardableSingletonPoolSampleService service) {
//        // pool 统计信息
//        service.printStatisticInfo();
//        // mock client 统计信息
//        service.printClientStatistic();
//        // tester 统计信息
//        logger.info("sample-service | Tester-Statistic: {" +
//                "clientPostCountMap=" + clientPostCountMap +
//                ", postToClosedClient=" + postToClosedClient.get() +
//                '}');
//    }
//
//    /**
//     * 定时打印统计信息
//     */
//    @SuppressWarnings("SameParameterValue")
//    private static void setPrintStatistic(DiscardableSingletonPoolSampleService service, long printPeriod) {
//        // 定时输出统计信息
//        executorService.scheduleAtFixedRate(() -> {
//            // pool 统计信息
//            service.printStatisticInfo();
//            // mock client 统计信息
//            service.printClientStatistic();
//        }, printPeriod, printPeriod, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * 配置定时全部丢弃 (每天一次, 指定小时)
//     */
//    @SuppressWarnings("SameParameterValue")
//    private static void configDiscardAllEveryday(DiscardableSingletonPoolSampleService service, int discardAllEverydayAtHour) {
//        // 每天丢弃全部的检查间隔, 这个正常不允许修改(默认15分钟), 这里为了测试才允许修改, 改成1分钟
//        service.setDiscardAllCheckPeriod(60000L);
//        // 定时丢弃所有对象
//        service.setDiscardAllEverydayAtHour(discardAllEverydayAtHour);
//    }
//
//    /**
//     * 配置定时全部丢弃 (测试, 周期型)
//     */
//    @SuppressWarnings("SameParameterValue")
//    private static void configDiscardAllForTest(DiscardableSingletonPoolSampleService service, long discardAllPeriod) {
//        // 定时丢弃所有对象
//        executorService.scheduleAtFixedRate(service::discardAllClient, discardAllPeriod, discardAllPeriod, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * 配置定时丢弃不怎么用的对象
//     */
//    @SuppressWarnings("SameParameterValue")
//    private static void configDiscardLowUsageInstances(DiscardableSingletonPoolSampleService service, long discardLowUsageInstancesPeriod) {
//        // 定时丢弃不怎么用的对象 (50s没用的对象)
//        service.setDiscardLowUsageInstancesExpireTime(discardLowUsageInstancesPeriod - 10000L);
//        service.setDiscardLowUsageInstancesPeriod(discardLowUsageInstancesPeriod);
//    }
//
//    /**
//     * 强制销毁丢弃超过30s的对象
//     */
//    @SuppressWarnings("SameParameterValue")
//    private static void configForceDestroy(DiscardableSingletonPoolSampleService service, long forceDestroyPeriod) {
//        // 强制销毁丢弃超过30s的对象.
//        service.setForceDestroyDiscardedInstancesAfterMillis(forceDestroyPeriod);
//        // 模拟定时30秒一次触发"销毁器"
//        service.setNotifyDestroyDiscardedInstancesPeriodMillis(forceDestroyPeriod);
//    }
//
//}
