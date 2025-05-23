/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector.FixedTimeoutLoadBalanceInspector;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector.TelnetLoadBalanceInspector;
import com.github.shepherdviolet.glacimon.java.concurrent.GuavaThreadFactoryBuilder;
import com.github.shepherdviolet.glacimon.java.misc.CloseableUtils;
import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>均衡负载--网络状态探测管理器</p>
 *
 * <p>请在服务停止的时候调用close()方法销毁本实例, 以释放线程池.</p>
 *
 * <pre>{@code
 *      //实例化
 *      LoadBalancedInspectManager inspectManager = new LoadBalancedInspectManager()
 *              //设置要探测的远端管理器(必须)
 *              .setHostManager(hostManager)
 *              //探测间隔(阻断时长为该值的两倍, 探测超时为该值的1/2)
 *              .setInspectInterval(5000L)
 *              //设置探测器
 *              .setInspector(new TelnetLoadBalanceInspector());
 * }</pre>
 *
 * <pre>{@code
 *      //重要:关闭探测器(停止线程)
 *      inspectManager.close();
 * }</pre>
 *
 * @author shepherdviolet
 */
public class LoadBalancedInspectManager implements Closeable {

    public static final long DEFAULT_INSPECT_INTERVAL = 5000L;

    private static final String LOG_PREFIX = "LoadBalance | ";

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String tag = LOG_PREFIX;

    private LoadBalancedHostManager hostManager;
    private volatile List<LoadBalanceInspector> inspectors = new ArrayList<>(1);

    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean closed = new AtomicBoolean(false);
    private volatile boolean pause = false;

    private long inspectInterval = DEFAULT_INSPECT_INTERVAL;
    private long inspectTimeout = DEFAULT_INSPECT_INTERVAL / 2;
    private long blockDuration = DEFAULT_INSPECT_INTERVAL * 2;

    private ExecutorService dispatchThreadPool = ThreadPoolExecutorUtils.createFixed(1,
            new GuavaThreadFactoryBuilder().setNameFormat("Glacispring-LBInspect-Dispatch-%s").setDaemon(true).build());
    private ExecutorService inspectThreadPool = ThreadPoolExecutorUtils.createCached(0,
            Integer.MAX_VALUE, 60, "Glacispring-LBInspect-Inspect-%s");

    private final Object intervalLock = new Object();

    /**
     * 自动开始探测(无需调用start()方法手动开启)
     */
    public LoadBalancedInspectManager() {
        this(true);
    }

    /**
     * @param autoStart true:自动开始探测(无需调用start()方法手动开启) false:不自动开始探测(需要调用start()方法手动开启)
     */
    public LoadBalancedInspectManager(boolean autoStart) {
        //默认telnet探测器
        inspectors.add(new TelnetLoadBalanceInspector());
        //自动开始
        if (autoStart) {
            start();
        }
    }

    /**
     * 若构造方法autoStart=false时, 需要手动调用该方法开始探测
     */
    public void start(){
        if (started.compareAndSet(false, true)) {
            //开始探测
            dispatchStart();
        }
    }

    /**
     * 关闭探测器(关闭调度线程)
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        started.set(true);
        try {
            dispatchThreadPool.shutdownNow();
        } catch (Throwable ignore){
        }
        try {
            inspectThreadPool.shutdownNow();
        } catch (Throwable ignore){
        }
    }

    /**
     * 设置远端管理器(必须)
     * @param hostManager 远端管理器
     */
    public LoadBalancedInspectManager setHostManager(LoadBalancedHostManager hostManager) {
        this.hostManager = hostManager;
        return this;
    }

    /**
     * [可运行时修改]
     * 设置网络状态探测器, 如果不设置默认为telnet探测器
     * @param inspector 探测器
     */
    public LoadBalancedInspectManager setInspector(LoadBalanceInspector inspector){
        List<LoadBalanceInspector> newInspectors = new ArrayList<>(1);
        newInspectors.add(inspector);
        List<LoadBalanceInspector> oldInspectors = this.inspectors;
        this.inspectors = newInspectors;
        for (LoadBalanceInspector oldInspector : oldInspectors) {
            CloseableUtils.closeIfCloseable(oldInspector);
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置网络状态探测器, 如果不设置默认为telnet探测器
     * @param inspectors 探测器
     */
    public LoadBalancedInspectManager setInspectors(List<LoadBalanceInspector> inspectors) {
        List<LoadBalanceInspector> oldInspectors = this.inspectors;
        this.inspectors = inspectors;
        for (LoadBalanceInspector oldInspector : oldInspectors) {
            CloseableUtils.closeIfCloseable(oldInspector);
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置探测间隔
     * @param inspectInterval 探测间隔(ms), 最小1000, 建议5000及以上; 若设置成<=0, 则暂停主动探测(暂停特性:2025.0.1+)
     */
    public LoadBalancedInspectManager setInspectInterval(long inspectInterval) {
        // <=0时暂停探测
        if (inspectInterval <= 0) {
            if (!pause) {
                logger.info(tag + "Inspect: Inspection pause (inspectInterval is set to <= 0)");
            }
            pause = true;
            inspectInterval = 600000L;
        } else {
            if (pause) {
                logger.info(tag + "Inspect: Inspection resume (inspectInterval is set to > 0)");
            }
            pause = false;
        }
        // 最小1000
        if (inspectInterval < 1000L){
            inspectInterval = 1000L;
        }
        //探测间隔
        this.inspectInterval = inspectInterval;
        //探测超时
        this.inspectTimeout = inspectInterval / 2;
        //故障时远端被阻断的时间
        this.blockDuration = inspectInterval * 2;

        //更新探测器的超时时间
        List<LoadBalanceInspector> inspectors = LoadBalancedInspectManager.this.inspectors;
        if (inspectors != null) {
            for (LoadBalanceInspector inspector : inspectors) {
                if (inspector instanceof FixedTimeoutLoadBalanceInspector) {
                    ((FixedTimeoutLoadBalanceInspector) inspector).setTimeout(inspectTimeout);
                }
            }
        }

        synchronized (intervalLock) {
            intervalLock.notifyAll();
        }

        return this;
    }

    /**
     * 设置客户端的标识
     * @param tag 标识
     */
    public LoadBalancedInspectManager setTag(String tag) {
        this.tag = tag != null ? LOG_PREFIX + tag + "> " : LOG_PREFIX;
        return this;
    }

    @Override
    public String toString() {
        return "inspectors=" + inspectors +
                ", inspectInterval=" + inspectInterval +
                ", inspectTimeout=" + inspectTimeout +
                ", blockDuration=" + blockDuration +
                ", pause=" + pause;
    }

    public long getInspectInterval() {
        return inspectInterval;
    }

    public long getInspectTimeout() {
        return inspectTimeout;
    }

    public long getBlockDuration() {
        return blockDuration;
    }

    protected boolean isBlockIfInspectorError(){
        return true;
    }

    /**
     * 调度线程启动
     */
    private void dispatchStart() {
        dispatchThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(inspectInterval);
                } catch (InterruptedException ignored) {
                }
                if (logger.isInfoEnabled()) {
                    logger.info(tag + "InspectManager Start: " + LoadBalancedInspectManager.this);
                }
                if (hostManager == null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(tag + "InspectManager has no hostManager, unable to inspect hosts");
                    }
                }
                LoadBalancedHostManager hostManager;
                LoadBalancedHostManager.Host[] hostArray;
                while (!closed.get()){
                    //间隔
                    synchronized (intervalLock) {
                        try {
                            intervalLock.wait(inspectInterval);
                        } catch (InterruptedException ignored) {}
                    }
                    //暂停主动探测
                    if (pause) {
                        continue;
                    }
                    //持有当前的hostManager
                    hostManager = LoadBalancedInspectManager.this.hostManager;
                    //检查是否配置
                    if (hostManager == null){
                        if (logger.isTraceEnabled()) {
                            logger.trace(tag + "InspectManager has no hostManager, skip inspect");
                        }
                        continue;
                    }
                    //获取远端列表
                    hostArray = hostManager.getHostArray();
                    if (hostArray.length <= 0){
                        if (logger.isTraceEnabled()) {
                            logger.trace(tag + "InspectManager has no hosts, skip inspect");
                        }
                        continue;
                    }
                    //打印当前远端状态
                    if (logger.isTraceEnabled()) {
                        logger.trace(hostManager.printHostsStatus(tag + "Hosts status (before inspect):"));
                    } else if (logger.isInfoEnabled() && hostManager.hasBlockedHost()) {
                        logger.info(hostManager.printHostsStatus(tag + "Hosts status (before inspect):"));
                    }
                    //探测所有远端
                    for (LoadBalancedHostManager.Host host : hostArray){
                        inspect(host);
                    }
                }
                if (logger.isInfoEnabled()) {
                    logger.info(tag + "InspectManager Closed: " + LoadBalancedInspectManager.this);
                }
            }
        });
    }

    /**
     * 开始异步探测
     */
    private void inspect(final LoadBalancedHostManager.Host host) {
        inspectThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (logger.isTraceEnabled()) {
                    logger.trace(tag + "Inspect: inspecting " + host.getUrl());
                }
                //持有探测器
                List<LoadBalanceInspector> inspectors = LoadBalancedInspectManager.this.inspectors;
                if (inspectors == null){
                    if (logger.isTraceEnabled()) {
                        logger.trace(tag + "Inspect: no inspectors, skip inspect");
                    }
                    return;
                }
                //只要有一个探测器返回false, 就阻断远端
                boolean block = false;
                for (LoadBalanceInspector inspector : inspectors){
                    /*
                     * 注意:探测器必须在指定的timeout时间内探测完毕, 不要过久的占用线程,
                     * 尽量处理掉所有异常, 如果抛出异常, 视为探测失败, 阻断远端
                     */
                    try {
                        if (!inspector.inspect(host.getUrl(), inspectTimeout)) {
                            block = true;
                            break;
                        }
                    } catch (Throwable t) {
                        if (logger.isErrorEnabled()){
                            logger.error(tag + "Inspect: Un-captured error occurred while inspecting, url " + host.getUrl() + ", in " + inspector.getClass(), t);
                        }
                        if (isBlockIfInspectorError()) {
                            block = true;
                            break;
                        }
                    }
                }
                //阻断(无恢复期)
                if (block){
                    host.feedback(false, blockDuration, 1);
                    if (logger.isWarnEnabled()) {
                        logger.warn(tag + "Inspect: Bad host " + host.getUrl() + ", block for " + blockDuration + " ms (Initiative block)");
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(tag + "Inspect: inspected " + host.getUrl());
                }
            }
        });
    }

}
