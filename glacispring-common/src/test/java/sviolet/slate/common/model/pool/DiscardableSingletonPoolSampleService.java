package sviolet.slate.common.model.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sviolet.thistle.compat.concurrent.CompatThreadFactoryBuilder;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DiscardableSingletonPool使用示例
 *
 * 假设我们要实现一个服务, 维护几个客户端, 用来向不同的后端发送请求.
 * 1.使用DiscardableSingletonPool维护客户端实例.
 * 2.当客户端抛出BadHostException异常时, 丢弃客户端重新创建.
 * 3.设置"强制销毁丢弃超过10分钟的对象"
 * 4.设置"每小时丢弃不怎么用的对象 (50分钟没用的对象)"
 * 5.设置"每天2点丢弃所有对象"
 * 6.定时输出统计日志
 *
 * 可配置参数(Java定时方式) ---------------------------------------------------------------------------------------------
 *
 * 强制销毁功能:
 * sample-service.force-destroy-discarded-instances-after-millis: 对象被丢弃超过该时间就允许被强制销毁, 默认600000 (10分钟)
 * sample-service.notify-destroy-discarded-instances-period-millis: 定时触发"销毁器"(配合强制销毁功能), 默认600000 (10分钟)
 *
 * 定时丢弃低使用率对象功能:
 * sample-service.discard-low-usage-instances-expire-time: 对象不被使用超过该时间就允许被丢弃(视为低使用率对象), 默认3000000 (50分钟)
 * sample-service.discard-low-usage-instances-period: 定时清理低使用率的对象, 默认3600000 (60分钟)
 *
 * 定时丢弃全部功能:
 * sample-service.discard-all-everyday-at-hour: 每天在指定时间丢弃所有对象, 默认-1 (默认不启用, 若设置为2, 则表示凌晨2点左右丢弃)
 * sample-service.time-zone: 设置时区, 默认操作系统时区 (东八区可以设置"UTC+8")
 *
 * 统计信息打印功能:
 * sample-service.print-statistic-info-period: 定时输出统计日志的时间间隔, 默认600000 (10分钟)
 *
 * 可配置参数(Spring定时方式, 被注释掉了) --------------------------------------------------------------------------------
 *
 * 强制销毁功能:
 * sample-service.force-destroy-discarded-instances-after-millis: 对象被丢弃超过该时间就允许被强制销毁, 默认600000 (10分钟)
 * sample-service.notify-destroy-discarded-instances-period-millis: 定时触发"销毁器"(配合强制销毁功能), 默认600000 (10分钟)
 *
 * 定时丢弃低使用率对象功能:
 * sample-service.discard-low-usage-instances-expire-time: 对象不被使用超过该时间就允许被丢弃(视为低使用率对象), 默认3000000 (50分钟)
 * sample-service.discard-low-usage-instances-period: 定时清理低使用率的对象, 默认3600000 (60分钟)
 *
 * 定时丢弃全部功能:
 * sample-service.discard-all-client-cron: 每天在指定时间丢弃所有对象, 默认"0 0 2 * * ?" (凌晨2点)
 *
 * 统计信息打印功能:
 * sample-service.print-statistic-info-period: 定时输出统计日志的时间间隔, 默认600000 (10分钟)
 *
 * @author shepherdviolet
 */
@Service
public class DiscardableSingletonPoolSampleService
        implements DiscardableSingletonPool.InstanceManager<DiscardableSingletonPoolSampleService.Client, DiscardableSingletonPoolSampleService.ClientCreateParam> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 必要项 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * [必要: 对象池]
     *
     * 对象池
     *
     * P.S. DiscardableSingletonPoolSampleService实现了DiscardableSingletonPool.InstanceManager接口, 实现实例创建/销毁等逻辑. 也可以在内部类中实现这个接口.
     */
    private final DiscardableSingletonPool<Client, ClientCreateParam> clientPool = new DiscardableSingletonPool<>(this);

    /**
     * [必要: 对象池]
     *
     * 创建对象
     *
     * P.S. DiscardableSingletonPoolSampleService实现了DiscardableSingletonPool.InstanceManager接口, 实现实例创建/销毁等逻辑. 也可以在内部类中实现这个接口.
     *
     * @param key         对象名称
     * @param createParam 创建参数, 可为空, 由DiscardableSingletonPool#getInstanceProvider传入
     * @return 创建出来的对象
     * @throws Exception 这里抛出的异常会被封装为InstanceCreateException抛出
     */
    @Override
    public Client createInstance(String key, ClientCreateParam createParam) throws Exception {
        // 在这里实现客户端的创建
        return new Client(key, createParam);
    }

    /**
     * [必要: 对象池]
     *
     * 销毁对象, 如果这里抛出异常, 会回调onInstanceDestroyError方法
     *
     * P.S. DiscardableSingletonPoolSampleService实现了DiscardableSingletonPool.InstanceManager接口, 实现实例创建/销毁等逻辑. 也可以在内部类中实现这个接口.
     *
     * @param info     对象信息
     * @param instance 对象实例(销毁它)
     */
    @Override
    public void destroyInstance(DiscardableSingletonPool.InstanceInfo info, Client instance) throws Exception {
        // 在这里实现客户端的销毁
        instance.destroy();
    }

    /**
     * [必要: 对象池]
     *
     * 处理destroyInstance方法抛出的异常, 一般就打印日志
     *
     * P.S. DiscardableSingletonPoolSampleService实现了DiscardableSingletonPool.InstanceManager接口, 实现实例创建/销毁等逻辑. 也可以在内部类中实现这个接口.
     *
     * @param info     对象信息
     * @param instance 对象实例
     * @param t        异常
     */
    @Override
    public void onInstanceDestroyError(DiscardableSingletonPool.InstanceInfo info, Client instance, Throwable t) {
        // 一般就打个日志
        logger.error("Error when destroy Client: " + info, t);
    }

    /**
     * [必要: 正确使用对象池]
     *
     * 向指定客户端发送请求
     *
     * @param key 客户端名称
     * @param request 请求
     * @return 响应
     */
    public Response post(String key, Request request) {

        // 获取客户端创建参数
        ClientCreateParam createParam = clientCreateParams.get(key);
        if (createParam == null) {
            throw new RuntimeException("Undefined client " + key);
        }

        // 写法1: 重要!!! 用try-with-resource写法保证在使用完毕后释放引用
        // 从对象池获取客户端, 传入客户端名称和创建参数
        try (DiscardableSingletonPool.InstanceProvider<Client> instanceProvider = clientPool.getInstanceProvider(key, createParam)){
            // 从InstanceProvider获取对象实例使用
            Client client = instanceProvider.getInstance();
            try {
                // 使用客户端发送请求
                return client.post(request);
            } catch (BadHostException e) {
                // 假设BadHostException异常表示后端故障, 必须重新创建客户端实例连接
                // 所以这里丢弃客户端
                instanceProvider.discard();
                // 这里选择继续抛出异常
                throw e;
            }
        }

        // 写法2: 重要!!! 普通写法, 在使用完毕后释放引用
        // 从对象池获取客户端, 传入客户端名称和创建参数
//        DiscardableSingletonPool.InstanceProvider<Client> instanceProvider = clientPool.getInstanceProvider(key, createParam);
//        try {
//            // 从InstanceProvider获取对象实例使用
//            Client client = instanceProvider.getInstance();
//            // 使用客户端发送请求
//            return client.post(request);
//        } catch (BadHostException e) {
//            // 假设BadHostException异常表示后端故障, 必须重新创建客户端实例连接
//            // 所以这里丢弃客户端
//            instanceProvider.discard();
//            // 这里选择继续抛出异常
//            throw e;
//        } finally {
//            instanceProvider.close();
//        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 可选项 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [可选: 创建参数]
     *
     * 客户端创建参数
     */
    private Map<String, ClientCreateParam> clientCreateParams = new HashMap<>();

    /**
     * [可选: 创建参数]
     *
     * 注入所有客户端创建参数ClientCreateParam (注入所有类型为ClientCreateParam的Spring Bean)
     */
    @Autowired
    public void setClientCreateParams(Map<String, ClientCreateParam> clientCreateParams) {
        this.clientCreateParams = clientCreateParams;
    }

    /**
     * [可选: 手动全部丢弃]
     *
     * 丢弃所有客户端
     */
    public void discardAllClient() {
        clientPool.discardAll();
    }

    /**
     *  [可选: 手动统计信息打印]
     *
     *  手动输出统计信息
     */
    public void printStatisticInfo() {
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | Pool-Statistic: " + clientPool);
            logger.info("sample-service | Task-Statistic: {" +
                    "notifyDestroyDiscardedInstancesTaskCount=" + notifyDestroyDiscardedInstancesTaskCount +
                    ", discardLowUsageInstancesTaskCount=" + discardLowUsageInstancesTaskCount +
                    ", discardAllEverydayTaskCount=" + discardAllEverydayTaskCount +
                    ", discardAllEverydayCheckCount=" + discardAllEverydayCheckCount +
                    '}');
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 可选项(Java定时方式) //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // -------------------------------------------------------------------------------------------------------
    // 可选项: 通用
    // -------------------------------------------------------------------------------------------------------

    /**
     * [可选: 定时任务]
     *
     * 定时执行器(线程池)
     *
     * ------------------------------------------------------------------------------------------------------------
     *
     * 注意要设置为守护线程, 否则会影响JVM自然结束
     */
    private final ScheduledExecutorService executorService = ThreadPoolExecutorUtils.createScheduled(1,
            new CompatThreadFactoryBuilder().setNameFormat("sample-service-schedule-%d").setDaemon(true).build());

    /**
     * [可选: 定时任务统计信息]
     */
    private int notifyDestroyDiscardedInstancesTaskCount = 0;
    private int discardLowUsageInstancesTaskCount = 0;
    private int discardAllEverydayTaskCount = 0;
    private int discardAllEverydayCheckCount = 0;

    // -------------------------------------------------------------------------------------------------------
    // 可选项: 强制销毁
    // -------------------------------------------------------------------------------------------------------

    /**
     * [可选: 强制销毁] 强制销毁丢弃超过10分钟的对象.
     */
    private ScheduledFuture<?> notifyDestroyDiscardedInstancesTask;

    /**
     * [可选: 强制销毁] 强制销毁丢弃超过10分钟的对象.
     *
     * 设置对象强制销毁到期时间 (超过该时间就可以被强制销毁)
     *
     * 可配置参数:
     * sample-service.force-destroy-discarded-instances-after-millis: 对象被丢弃超过该时间就允许被强制销毁, 默认600000 (10分钟)
     * sample-service.notify-destroy-discarded-instances-period-millis: 定时触发"销毁器"(配合强制销毁功能), 默认600000 (10分钟)
     *
     * ------------------------------------------------------------------------------------------------------------
     *
     * [温馨提示] 如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一.
     * 本使用示例保证了每次使用都释放引用, 所以这个设置在这里没有意义, 供需要的人参考.
     *
     * 正常情况下, 被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁. 设置了这个参数后, "销毁器"在判断对象是否可以销毁时,
     * 追加了一种情况, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象, 无视对象的引用计数情况.
     *
     * 注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
     * 也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了.
     *
     * 所以, 这里配套了一个定时器(见setNotifyDestroyDiscardedInstancesPeriodMillis方法), 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances方法,
     * 唤醒"销毁器"将"已到期"的对象强制销毁.
     *
     */
    @Value("${sample-service.force-destroy-discarded-instances-after-millis:600000}")
    public void setForceDestroyDiscardedInstancesAfterMillis(long forceDestroyDiscardedInstancesAfterMillis){
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setForceDestroyDiscardedInstancesAfterMillis=" + forceDestroyDiscardedInstancesAfterMillis);
        }
        clientPool.setForceDestroyDiscardedInstancesAfterMillis(forceDestroyDiscardedInstancesAfterMillis);
    }

    /**
     * [可选: 强制销毁] 强制销毁丢弃超过10分钟的对象.
     *
     * 每隔10分钟通知一次"销毁器", 设置为-1表示关闭. 配合setForceDestroyDiscardedInstancesAfterMillis方法实现可靠的"强制销毁"功能.
     *
     * 可配置参数:
     * sample-service.force-destroy-discarded-instances-after-millis: 对象被丢弃超过该时间就允许被强制销毁, 默认600000 (10分钟)
     * sample-service.notify-destroy-discarded-instances-period-millis: 定时触发"销毁器"(配合强制销毁功能), 默认600000 (10分钟)
     *
     * ------------------------------------------------------------------------------------------------------------
     *
     * [温馨提示] 如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一.
     * 本使用示例保证了每次使用都释放引用, 所以这个设置在这里没有意义, 供需要的人参考.
     *
     */
    @Value("${sample-service.notify-destroy-discarded-instances-period-millis:600000}")
    public void setNotifyDestroyDiscardedInstancesPeriodMillis(long notifyDestroyDiscardedInstancesPeriodMillis) {
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setNotifyDestroyDiscardedInstancesPeriodMillis=" + notifyDestroyDiscardedInstancesPeriodMillis);
        }
        synchronized (this) {
            if (notifyDestroyDiscardedInstancesTask != null) {
                notifyDestroyDiscardedInstancesTask.cancel(false);
                notifyDestroyDiscardedInstancesTask = null;
            }
            if (notifyDestroyDiscardedInstancesPeriodMillis > 0) {
                notifyDestroyDiscardedInstancesTask = executorService.scheduleAtFixedRate(
                        () -> {
                            if (logger.isTraceEnabled()) {
                                logger.trace("sample-service | Execute NotifyDestroyDiscardedInstances, PeriodMillis=" + notifyDestroyDiscardedInstancesPeriodMillis);
                            }
                            try {
                                clientPool.notifyDestroyDiscardedInstances();
                            } catch (Throwable t) {
                                logger.error("Error when clientPool.notifyDestroyDiscardedInstances()", t);
                            }
                            notifyDestroyDiscardedInstancesTaskCount++;
                        },
                        notifyDestroyDiscardedInstancesPeriodMillis,
                        notifyDestroyDiscardedInstancesPeriodMillis,
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // 可选项: 定时丢弃低使用率对象
    // -------------------------------------------------------------------------------------------------------

    /**
     * [可选: 定时丢弃低使用率对象] 每小时丢弃不怎么用的对象 (50分钟没用的对象)
     */
    private long discardLowUsageInstancesExpireTime = -1;
    private ScheduledFuture<?> discardLowUsageInstancesTask;

    /**
     * [可选: 定时丢弃低使用率对象] 每小时丢弃不怎么用的对象 (50分钟没用的对象)
     *
     * 设置对象低使用率判定时间 (超过指定时间未使用判定为低使用率)
     *
     * 可配置参数:
     * sample-service.discard-low-usage-instances-expire-time: 对象不被使用超过该时间就允许被丢弃(视为低使用率对象), 默认3000000 (50分钟)
     * sample-service.discard-low-usage-instances-period: 定时清理低使用率的对象, 默认3600000 (60分钟)
     *
     */
    @Value("${sample-service.discard-low-usage-instances-expire-time:3000000}")
    public void setDiscardLowUsageInstancesExpireTime(long discardLowUsageInstancesExpireTime) {
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setDiscardLowUsageInstancesExpireTime=" + discardLowUsageInstancesExpireTime);
        }
        this.discardLowUsageInstancesExpireTime = discardLowUsageInstancesExpireTime;
    }

    /**
     * [可选: 定时丢弃低使用率对象] 每小时丢弃不怎么用的对象 (50分钟没用的对象)
     *
     * 设置低使用率对象检查/丢弃时间间隔, 设置为-1表示关闭
     *
     * 可配置参数:
     * sample-service.discard-low-usage-instances-expire-time: 对象不被使用超过该时间就允许被丢弃(视为低使用率对象), 默认3000000 (50分钟)
     * sample-service.discard-low-usage-instances-period: 定时清理低使用率的对象, 默认3600000 (60分钟)
     *
     */
    @Value("${sample-service.discard-low-usage-instances-period:3600000}")
    public void setDiscardLowUsageInstancesPeriod(long discardLowUsageInstancesPeriod){
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setDiscardLowUsageInstancesPeriod=" + discardLowUsageInstancesPeriod);
        }
        synchronized (this) {
            if (discardLowUsageInstancesTask != null) {
                discardLowUsageInstancesTask.cancel(false);
                discardLowUsageInstancesTask = null;
            }
            if (discardLowUsageInstancesPeriod > 0) {
                discardLowUsageInstancesTask = executorService.scheduleAtFixedRate(
                        () -> {
                            if (logger.isInfoEnabled()) {
                                logger.info("sample-service | Execute DiscardLowUsageInstances, Period=" + discardLowUsageInstancesPeriod);
                            }
                            try {
                                clientPool.discard((instance, info) -> (System.currentTimeMillis() - info.getLastUsedTimeMillis()) > discardLowUsageInstancesExpireTime);
                            } catch (Throwable t) {
                                logger.error("Error when clientPool.discard()", t);
                            }
                            discardLowUsageInstancesTaskCount++;
                        },
                        discardLowUsageInstancesPeriod,
                        discardLowUsageInstancesPeriod,
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // 可选项: 定时全部丢弃
    // -------------------------------------------------------------------------------------------------------

    /**
     * [可选: 定时全部丢弃] 每天2点多丢弃所有对象
     */
    private int discardAllEverydayAtHour = -1;
    private ZoneId zoneId = ZoneId.systemDefault();
    private ScheduledFuture<?> discardAllEverydayTask;

    /**
     * [可选: 定时全部丢弃] 每天2点丢弃所有对象
     */
    private long discardAllCheckPeriod = 900000; // 每天丢弃全部的检查间隔, 这个正常不允许修改, 这里为了测试才允许修改
    private int lastDiscardAllDay = -1;

    /**
     * [可选: 定时全部丢弃] 每天2点多丢弃所有对象
     *
     * 设置每天丢弃所有对象的大致时间(24小时制), 设置为-1表示关闭. 例如: 设置为2, 表示每天凌晨2点多丢弃所有对象, 时间不精确.
     *
     * 可配置参数:
     * sample-service.discard-all-everyday-at-hour: 每天在指定时间丢弃所有对象, 默认-1 (默认不启用, 若设置为2, 则表示凌晨2点左右丢弃)
     * sample-service.time-zone: 设置时区, 默认操作系统时区 (东八区可以设置"UTC+8")
     *
     */
    @Value("${sample-service.discard-all-everyday-at-hour:-1}")
    public void setDiscardAllEverydayAtHour(int discardAllEverydayAtHour){
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setDiscardAllEverydayAtHour=" + discardAllEverydayAtHour);
        }
        if (discardAllEverydayAtHour > 23) {
            discardAllEverydayAtHour = discardAllEverydayAtHour % 24;
        }
        synchronized (this) {
            this.discardAllEverydayAtHour = discardAllEverydayAtHour;
            if (discardAllEverydayAtHour >= 0) {
                // 启用定时任务
                if (discardAllEverydayTask == null) {
                    discardAllEverydayTask = executorService.scheduleAtFixedRate(discardAllEveryDayRunnable,
                            discardAllCheckPeriod,
                            discardAllCheckPeriod,
                            TimeUnit.MILLISECONDS);
                }
            } else {
                // 取消定时任务
                if (discardAllEverydayTask != null) {
                    discardAllEverydayTask.cancel(false);
                    discardAllEverydayTask = null;
                }
            }
        }
    }

    /**
     * [可选: 定时全部丢弃] 每天2点丢弃所有对象
     *
     * 每天丢弃所有对象
     */
    private final Runnable discardAllEveryDayRunnable = () -> {
        if (logger.isTraceEnabled()) {
            logger.trace("sample-service | Check DiscardAllEveryday");
        }
        discardAllEverydayCheckCount++;

        try {
            ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
            int currentHour = currentTime.getHour();
            int currentDay = currentTime.getDayOfYear();
            // 判断当前小时 = 指定小时 && 当前天 != 上次执行的天
            if (currentHour == discardAllEverydayAtHour && currentDay != lastDiscardAllDay) {
                if (logger.isInfoEnabled()) {
                    logger.info("sample-service | Execute DiscardAllEveryday, atHour=" + discardAllEverydayAtHour);
                }
                // 记录上次执行的天, 防止同一个小时内重复执行
                lastDiscardAllDay = currentDay;
                discardAllClient();
                discardAllEverydayTaskCount++;
            }
        } catch (Throwable t) {
            logger.error("Error when discardAllEveryDayRunnable.run()", t);
        }

    };

    /**
     * [可选: 定时全部丢弃] 每天2点丢弃所有对象
     *
     * 设置系统时区
     *
     * 可配置参数:
     * sample-service.discard-all-everyday-at-hour: 每天在指定时间丢弃所有对象, 默认-1 (默认不启用, 若设置为2, 则表示凌晨2点左右丢弃)
     * sample-service.time-zone: 设置时区, 默认操作系统时区 (东八区可以设置"UTC+8")
     *
     */
    @Value("${sample-service.time-zone:}")
    public void setZone(String zone) {
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setZone=" + zone);
        }
        if (zone == null || "".equals(zone)) {
            zoneId = ZoneId.systemDefault();
        } else {
            zoneId = ZoneId.of(zone); // 例如 "UTC+8"
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // 可选项: 统计信息打印
    // -------------------------------------------------------------------------------------------------------

    /**
     * [可选: 统计信息打印] 每隔10分钟输出统计日志
     */
    private ScheduledFuture<?> printStatisticInfoTask;

    /**
     * [可选: 统计信息打印] 每隔10分钟输出统计日志
     *
     * 设置统计日志输出时间间隔, 设置为-1表示关闭
     *
     * 可配置参数:
     * sample-service.print-statistic-info-period: 定时输出统计日志的时间间隔, 默认600000 (10分钟)
     *
     */
    @Value("${sample-service.print-statistic-info-period:600000}")
    public void setPrintStatisticInfoPeriod(long printStatisticInfoPeriod){
        if (logger.isInfoEnabled()) {
            logger.info("sample-service | setPrintStatisticInfoPeriod=" + printStatisticInfoPeriod);
        }
        synchronized (this) {
            if (printStatisticInfoTask != null) {
                printStatisticInfoTask.cancel(false);
            }
            if (printStatisticInfoPeriod > 0) {
                printStatisticInfoTask = executorService.scheduleAtFixedRate(
                        this::printStatisticInfo,
                        printStatisticInfoPeriod,
                        printStatisticInfoPeriod,
                        TimeUnit.MILLISECONDS);
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 可选项(Spring定时方式) ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



//    // -------------------------------------------------------------------------------------------------------
//    // 可选项: 强制销毁
//    // -------------------------------------------------------------------------------------------------------
//
//    /**
//     * [可选: 强制销毁] 强制销毁丢弃超过10分钟的对象.
//     *
//     * 设置对象强制销毁到期时间 (超过该时间就可以被强制销毁)
//     *
//     * 可配置参数:
//     * sample-service.force-destroy-discarded-instances-after-millis: 对象被丢弃超过该时间就允许被强制销毁, 默认600000 (10分钟)
//     * sample-service.notify-destroy-discarded-instances-period-millis: 定时触发"销毁器"(配合强制销毁功能), 默认600000 (10分钟)
//     *
//     * ------------------------------------------------------------------------------------------------------------
//     *
//     * [温馨提示] 如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一.
//     * 本使用示例保证了每次使用都释放引用, 所以这个设置在这里没有意义, 供需要的人参考.
//     *
//     * 正常情况下, 被丢弃的对象由"销毁器"负责, 在它们使用完毕后(引用计数为0)销毁. 设置了这个参数后, "销毁器"在判断对象是否可以销毁时,
//     * 追加了一种情况, 若 "当前时间 - 丢弃时间 > forceDestroyDiscardedInstancesAfterMillis" 则强制销毁对象, 无视对象的引用计数情况.
//     *
//     * 注意!! 仅仅设置这个参数并不能保证对象在"到期"后立刻被销毁, 因为"销毁器"触发有条件(见6.2). 如果对象"到期"后, 没有人触发丢弃(discard),
//     * 也没有被丢弃的对象引用计数归0, 那就只能手动触发"销毁器"了.
//     *
//     * 所以, 这里配套了一个定时器(见notifyDestroyer方法), 定期调用DiscardableSingletonPool#notifyDestroyDiscardedInstances方法,
//     * 唤醒"销毁器"将"已到期"的对象强制销毁.
//     */
//    @Value("${sample-service.force-destroy-discarded-instances-after-millis:600000}")
//    public void setForceDestroyDiscardedInstancesAfterMillis(long forceDestroyDiscardedInstancesAfterMillis) {
//        clientPool.setForceDestroyDiscardedInstancesAfterMillis(forceDestroyDiscardedInstancesAfterMillis);
//    }
//
//    /**
//     * [可选: 强制销毁] 强制销毁丢弃超过10分钟的对象.
//     *
//     * 每隔10分钟通知一次"销毁器". 配合setForceDestroyDiscardedInstancesAfterMillis方法实现可靠的"强制销毁"功能.
//     *
//     * 可配置参数:
//     * sample-service.force-destroy-discarded-instances-after-millis: 对象被丢弃超过该时间就允许被强制销毁, 默认600000 (10分钟)
//     * sample-service.notify-destroy-discarded-instances-period-millis: 定时触发"销毁器"(配合强制销毁功能), 默认600000 (10分钟)
//     *
//     * ------------------------------------------------------------------------------------------------------------
//     *
//     * [温馨提示] 如果你正确地在每次使用完对象后释放引用, 这个参数是没有必要设置的. 如果你担心自己不小心持有对象, 忘记释放引用的话, 那就设置这个参数以防万一.
//     * 本使用示例保证了每次使用都释放引用, 所以这个设置在这里没有意义, 供需要的人参考.
//     *
//     */
//    @Scheduled(fixedRateString = "${sample-service.notify-destroy-discarded-instances-period-millis:600000}")
//    public void notifyDestroyer() {
//        clientPool.notifyDestroyDiscardedInstances();
//    }
//
//    // -------------------------------------------------------------------------------------------------------
//    // 可选项: 定时丢弃低使用率对象
//    // -------------------------------------------------------------------------------------------------------
//
//    /**
//     * [可选: 定时丢弃低使用率对象] 每小时丢弃不怎么用的对象 (50分钟没用的对象)
//     */
//    private long discardLowUsageInstancesExpireTime;
//
//    /**
//     * [可选: 定时丢弃低使用率对象] 每小时丢弃不怎么用的对象 (50分钟没用的对象)
//     *
//     * 设置对象低使用率判定时间 (超过指定时间未使用判定为低使用率)
//     *
//     * 可配置参数:
//     * sample-service.discard-low-usage-instances-expire-time: 对象不被使用超过该时间就允许被丢弃(视为低使用率对象), 默认3000000 (50分钟)
//     * sample-service.discard-low-usage-instances-period: 定时清理低使用率的对象, 默认3600000 (60分钟)
//     *
//     */
//    @Value("${sample-service.discard-low-usage-instances-expire-time:3000000}")
//    public void setDiscardLowUsageInstancesExpireTime(long discardLowUsageInstancesExpireTime) {
//        this.discardLowUsageInstancesExpireTime = discardLowUsageInstancesExpireTime;
//    }
//
//    /**
//     * [可选: 定时丢弃低使用率对象] 每小时丢弃不怎么用的对象 (50分钟没用的对象)
//     *
//     * 每个60分钟丢弃不怎么用的对象
//     *
//     * 可配置参数:
//     * sample-service.discard-low-usage-instances-expire-time: 对象不被使用超过该时间就允许被丢弃(视为低使用率对象), 默认3000000 (50分钟)
//     * sample-service.discard-low-usage-instances-period: 定时清理低使用率的对象, 默认3600000 (60分钟)
//     *
//     */
//    @Scheduled(fixedRateString = "${sample-service.discard-low-usage-instances-period:3600000}")
//    public void discardLowUsageInstances() {
//        clientPool.discard((instance, info) -> (System.currentTimeMillis() - info.getLastUsedTimeMillis()) > discardLowUsageInstancesExpireTime);
//    }
//
//    // -------------------------------------------------------------------------------------------------------
//    // 可选项: 定时丢弃全部
//    // -------------------------------------------------------------------------------------------------------
//
//    /**
//     * [可选: 定时全部丢弃] 每天2点丢弃所有对象
//     *
//     * 可配置参数:
//     * sample-service.discard-all-client-cron: 每天在指定时间丢弃所有对象, 默认"0 0 2 * * ?" (凌晨2点)
//     */
//    @Scheduled(cron = "${sample-service.discard-all-client-cron:0 0 2 * * ?}")
//    public void discardAllClient() {
//        clientPool.discardAll();
//    }
//
//    // -------------------------------------------------------------------------------------------------------
//    // 可选项: 统计信息打印
//    // -------------------------------------------------------------------------------------------------------
//
//    /**
//     * [可选: 统计信息输出] 定时输出统计日志
//     *
//     * 可配置参数:
//     * sample-service.print-statistic-info-period: 定时输出统计日志的时间间隔, 默认600000 (10分钟)
//     */
//    @Scheduled(fixedRateString = "${sample-service.print-statistic-info-period:600000}")
//    public void printPoolStatisticInfo() {
//        if (logger.isInfoEnabled()) {
//            logger.info("sample-service | Pool-Statistic: " + clientPool);
//        }
//    }




    // 以下非使用示例, 仅用于测试, 切勿复制粘贴 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下非使用示例, 仅用于测试, 切勿复制粘贴 ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下非使用示例, 仅用于测试, 切勿复制粘贴 ///////////////////////////////////////////////////////////////////////////////////////////////////////////




    // [MOCK] 测试的时候比较客户端是不是真的被销毁了
    public static final AtomicInteger clientCount = new AtomicInteger(0);
    public static final AtomicInteger clientDestroyCount = new AtomicInteger(0);
    // [MOCK] 测试的时候看客户端是不是走对, 没走到别的客户端里
    public static final Map<String, AtomicInteger> clientPostCountMap = new ConcurrentHashMap<>();
    public static final Map<String, AtomicInteger> clientPostSuccessCountMap = new ConcurrentHashMap<>();

    /**
     * [MOCK] 测试的时候比较客户端是不是真的被销毁了, 测试的时候看客户端是不是走对, 没走到别的客户端里
     */
    public void printClientStatistic() {
        logger.info("sample-service | Mock-Client-Statistic: {" +
                "clientCount='" + clientCount.get() + '\'' +
                ", clientDestroyCount=" + clientDestroyCount.get() +
                ", clientPostCountMap=" + clientPostCountMap +
                ", clientPostSuccessCountMap=" + clientPostSuccessCountMap +
                '}');
    }

    /**
     * [MOCK]
     *
     * 错误方式!!! 切勿模仿!!!
     * 错误方式!!! 切勿模仿!!!
     * 错误方式!!! 切勿模仿!!!
     *
     * @param key 客户端名称
     * @param request 请求
     * @return 响应
     */
    public Response incorrectWayToPost(String key, Request request) {

        // 获取客户端创建参数
        ClientCreateParam createParam = clientCreateParams.get(key);
        if (createParam == null) {
            throw new RuntimeException("Undefined client " + key);
        }

        /*
         * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
         * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
         * 错误方式!!! 切勿模仿!!! 不及时释放引用(DiscardableSingletonPool.InstanceProvider#close)会导致对象在被丢弃后无法被销毁!!!
         *
         * 这里的代码用于测试, 实际千万别这么写!!!
         */
        DiscardableSingletonPool.InstanceProvider<Client> instanceProvider = clientPool.getInstanceProvider(key, createParam);
        Client client = instanceProvider.getInstance();
        try {
            return client.post(request);
        } catch (BadHostException e) {
            instanceProvider.discard();
            throw e;
        }

    }

    /**
     * [MOCK] 每天丢弃全部的检查间隔, 这个正常不允许修改, 这里为了测试才允许修改
     */
    public void setDiscardAllCheckPeriod(long discardAllCheckPeriod) {
        this.discardAllCheckPeriod = discardAllCheckPeriod;
    }

    /**
     * [MOCK] 客户端
     */
    public static class Client {

        private final String key;
        private final int postSuccessRate;
        private volatile boolean closed = false;

        public Client(String key, ClientCreateParam createParam) {
            // 没送就报错, 能测试createParam是否真的传入
            if (createParam == null) {
                throw new RuntimeException("createParam == null");
            }

            // 测试创建失败
            if ("error".equals(key)) {
                throw new RuntimeException("Client create failed");
            }

            this.key = key;
            this.postSuccessRate = createParam.getPostSuccessRate();

            clientCount.getAndIncrement();
        }

        /**
         * 模拟发送请求
         */
        public Response post(Request request) {
            if (closed) {
                throw new ClosedClientException("ERROR, invoke a closed client");
            }

            clientPostCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();

            if (request == null) {
                throw new RuntimeException("request == null");
            }

            // 模拟IO
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(200));
            } catch (InterruptedException ignore) {
            }

            if (closed) {
                throw new ClosedClientException("ERROR, invoke a closed client");
            }

            // 模拟后端故障, 这种错误需要丢弃客户端实例(重建)
            if (ThreadLocalRandom.current().nextInt(10000) + 1 > postSuccessRate) {
                throw new BadHostException();
            }

            // POST成功计数, 会少于post次数, 因为有失败率
            clientPostSuccessCountMap.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();

            return new Response();
        }

        /**
         * 模拟销毁
         */
        public void destroy() throws IOException {
            clientDestroyCount.getAndIncrement();
            closed = true;
        }

    }

    /**
     * [MOCK] 客户端创建参数
     */
    public static class ClientCreateParam {

        private final int postSuccessRate;

        /**
         * @param postSuccessRate [0, 10000]
         */
        public ClientCreateParam(int postSuccessRate) {
            this.postSuccessRate = postSuccessRate;
        }

        public int getPostSuccessRate() {
            return postSuccessRate;
        }

    }

    /**
     * [MOCK] 请求
     */
    public static class Request {

    }

    /**
     * [MOCK] 响应
     */
    public static class Response {

    }

    /**
     * [MOCK] 假设BadHostException异常表示后端故障, 必须重新创建客户端实例连接
     */
    public static class BadHostException extends RuntimeException {

    }

    /**
     * [MOCK] 测试有没有调用被销毁实例的情况
     */
    public static class ClosedClientException extends RuntimeException {

        public ClosedClientException(String message) {
            super(message);
        }

    }

}
