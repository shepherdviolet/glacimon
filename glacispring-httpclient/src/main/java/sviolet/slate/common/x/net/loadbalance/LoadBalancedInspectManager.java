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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.net.loadbalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.net.loadbalance.inspector.FixedTimeoutLoadBalanceInspector;
import sviolet.slate.common.x.net.loadbalance.inspector.TelnetLoadBalanceInspector;
import sviolet.thistle.compat.concurrent.CompatThreadFactoryBuilder;
import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.thistle.util.lifecycle.CloseableManageUtils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>均衡负载--网络状态探测管理器</p>
 *
 * <p>
 *     注意!!!!!!<br>
 *     1.如果你是Servlet项目, 可以注册sviolet.slate.common.helperx.servlet.SlateServletContextListener监听器, 监听器会帮你
 *     自动销毁本探测器.<br>
 *     2.如果不是Servlet项目或没有注册监听器, 请在服务停止的时候调用close()方法销毁本实例, 以释放线程池.<br>
 * </p>
 *
 * <pre>{@code
 *      //实例化
 *      LoadBalancedInspectManager inspectManager = new LoadBalancedInspectManager()
 *              //设置要探测的远端管理器(必须)
 *              .setHostManager(hostManager)
 *              //探测间隔(阻断时长为该值的两倍, 探测超时为该值的1/2)
 *              .setInspectInterval(5000L)
 *              //设置探测器
 *              .setInspector(new TelnetLoadBalanceInspector())
 *              //允许输出调试日志
 *              .setVerboseLog(true);
 * }</pre>
 *
 * <pre>{@code
 *      //重要:关闭探测器(停止线程)
 *      inspectManager.close();
 * }</pre>
 *
 * @author S.Violet
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
    private boolean verboseLog = false;

    private long inspectInterval = DEFAULT_INSPECT_INTERVAL;
    private long inspectTimeout = DEFAULT_INSPECT_INTERVAL / 2;
    private long blockDuration = DEFAULT_INSPECT_INTERVAL * 2;

    private ExecutorService dispatchThreadPool = ThreadPoolExecutorUtils.createFixed(1,
            new CompatThreadFactoryBuilder().setNameFormat("Slate-LBInspect-Dispatch-%d").setDaemon(true).build());
    private ExecutorService inspectThreadPool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 60, "Slate-LBInspect-Inspect-%d");

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
        //注册到管理器, 便于集中销毁
        CloseableManageUtils.register(this);
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
     * @param inspectInterval 检测间隔ms, 必须 > 1000 , 建议 > 5000
     */
    public LoadBalancedInspectManager setInspectInterval(long inspectInterval) {
        if (inspectInterval < 1000){
            inspectInterval = 1000;
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
        return this;
    }

    /**
     * @param verboseLog true:打印更多的调试日志, 默认关闭
     */
    public LoadBalancedInspectManager setVerboseLog(boolean verboseLog) {
        this.verboseLog = verboseLog;
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
                ", verboseLog=" + verboseLog;
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
                LoadBalancedHostManager hostManager;
                LoadBalancedHostManager.Host[] hostArray;
                while (!closed.get()){
                    //间隔
                    try {
                        Thread.sleep(inspectInterval);
                    } catch (InterruptedException ignored) {
                    }
                    //持有当前的hostManager
                    hostManager = LoadBalancedInspectManager.this.hostManager;
                    //检查是否配置
                    if (hostManager == null){
                        if (logger.isDebugEnabled()) {
                            logger.debug(tag + "InspectManager has no hostManager, skip inspect");
                        }
                        continue;
                    }
                    //获取远端列表
                    hostArray = hostManager.getHostArray();
                    if (hostArray.length <= 0){
                        if (logger.isDebugEnabled()) {
                            logger.debug(tag + "InspectManager has no hosts, skip inspect");
                        }
                        continue;
                    }
                    //打印当前远端状态
                    if (logger.isDebugEnabled()) {
                        logger.debug(hostManager.printHostsStatus(tag + "Hosts status (before inspect):"));
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
                    if (logger.isDebugEnabled()) {
                        logger.debug(tag + "Inspect: no inspectors, skip inspect");
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
                        if (!inspector.inspect(host.getUrl(), inspectTimeout, verboseLog)) {
                            block = true;
                            break;
                        }
                    } catch (Throwable t) {
                        if (logger.isErrorEnabled()){
                            logger.error(tag + "Inspect: Un-captured error, url " + host.getUrl() + ", in " + inspector.getClass(), t);
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
                        logger.warn(tag + "Inspect: Bad host " + host.getUrl() + ", block for " + blockDuration + " ms, initiative block");
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(tag + "Inspect: inspected " + host.getUrl());
                }
            }
        });
    }

}
