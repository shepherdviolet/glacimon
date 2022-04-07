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
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.thistle.util.judge.CheckUtils;
import sviolet.thistle.util.reflect.MethodCaller;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 负载均衡--远端URL管理器
 *
 * <pre>{@code
 *      //实例化
 *      LoadBalancedHostManager hostManager = new LoadBalancedHostManager()
 *              //设置/刷新远端清单(线程安全/异步更新)
 *              .setHostArray(new String[]{
 *                  "http://www.baidu.com",
 *                  "http://127.0.0.1:8080",
 *                  "http://127.0.0.1:8081"
 *              });
 * }</pre>
 *
 * @author S.Violet
 */
public class LoadBalancedHostManager {

    private static final String LOG_PREFIX = "LoadBalance | ";
    private static final long WARNING_THRESHOLD = 4L;
    private static final boolean WARNING_DISABLED;

    static {
        WARNING_DISABLED = "true".equals(System.getProperty("slate.loadbalance.warndisabled", "false"));
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String tag = LOG_PREFIX;

    private AtomicInteger mainCounter = new AtomicInteger(0);
    private AtomicInteger refugeCounter = new AtomicInteger(0);

    private volatile Host[] hostArray = new Host[0];
    private Map<String, Integer> hostIndexMap = new HashMap<>(0);

    private boolean returnNullIfAllBlocked = false;

    private ThreadLocal<Long> hostChangeTime = new ThreadLocal<>();

    /**
     * [线程安全的]
     * @return 获取一个远端
     */
    public Host nextHost(){

        check();

        Host[] hostArray = this.hostArray;

        if (hostArray.length <= 0){
            return null;
        } else if (hostArray.length == 1){
            return hostArray[0];
        }

        long currentTimeMillis = System.currentTimeMillis();
        int mainCount = mainCounter.getAndIncrement() % hostArray.length;
        Host host = hostArray[mainCount];

        if (!host.isBlocked(currentTimeMillis)) {
            return host;
        }

        int refugeCount = refugeCounter.getAndIncrement() % hostArray.length;

        for (int i = 0 ; i < hostArray.length ; i++) {
            host = hostArray[refugeCount];
            if (!host.isBlocked(currentTimeMillis)) {
                return host;
            }
            refugeCount = (refugeCount + 1) % hostArray.length;
        }

        return returnNullIfAllBlocked ? null : hostArray[mainCount];

    }

    private void check() {
        if (WARNING_DISABLED) {
            return;
        }
        Long previousHostChangeTime = hostChangeTime.get();
        if (previousHostChangeTime != null) {
            if (System.currentTimeMillis() - previousHostChangeTime < WARNING_THRESHOLD && logger.isErrorEnabled()) {
                logger.error(LOG_PREFIX + "ERROR at " + getCallerInfo() + "!!! DO NOT set the hosts before requesting! Because the new hosts will " +
                        "not take effect immediately! It will send the request to the old hosts! See doc: " +
                        "https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/guide.md");
            }
        }
    }

    private final Collection<String> startsWithSkips = Arrays.asList(
            "sviolet.slate.common.x.net.loadbalance.",
            "org.springframework.cglib.proxy.Proxy$ProxyImpl$$"
    );

    private String getCallerInfo() {
        MethodCaller methodCaller = MethodCaller.getMethodCaller(null, startsWithSkips);
        if (methodCaller != null) {
            return methodCaller.getCallerClass() + "#" + methodCaller.getCallerMethodName();
        }
        return "";
    }

    /*****************************************************************************************************************
     * settings
     */

    private ExecutorService settingThreadPool = ThreadPoolExecutorUtils.createLazy(60L, "Slate-LBHostManager-Set-%d");
    private AtomicReference<List<String>> newSettings = new AtomicReference<>(null);

    private volatile boolean initialized = false;
    private volatile boolean updating = false;
    private AtomicBoolean initLock = new AtomicBoolean(false);

    /**
     * [线程安全/异步生效/可运行时修改]
     * 设置/刷新远端列表, 该方法可以反复调用设置新的后端(但不是同步生效)
     *
     * @param hosts 远端列表, 格式:"http://127.0.0.1:8081/,http://127.0.0.1:8082/"
     */
    public LoadBalancedHostManager setHosts(String hosts){
        if (CheckUtils.isEmptyOrBlank(hosts)){
            setHostList(new ArrayList<>(0));
            return this;
        }
        setHostArray(hosts.split(","));
        return this;
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * 设置/刷新远端列表, 该方法可以反复调用设置新的后端(但不是同步生效)
     *
     * @param hosts 远端列表
     */
    public LoadBalancedHostManager setHostArray(String[] hosts) {
        if (hosts == null || hosts.length <= 0){
            setHostList(new ArrayList<String>(0));
        } else {
            setHostList(Arrays.asList(hosts));
        }
        return this;
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * 设置/刷新远端列表, 该方法可以反复调用设置新的后端(但不是同步生效)
     *
     * @param hosts 远端列表
     */
    public LoadBalancedHostManager setHostList(List<String> hosts){
        if (hosts == null){
            hosts = new ArrayList<>(0);
        }

        //剔除空数据
        for (int i = 0 ; i < hosts.size() ; i++) {
            if (CheckUtils.isEmptyOrBlank(hosts.get(i))){
                hosts.remove(i);
                i--;
            }
        }

        //初始化耗时短, 用自旋锁
        while (!initialized){
            if (!initLock.get() && initLock.compareAndSet(false, true)) {
                settingInstall(hosts);
                initialized = true;
                if (logger.isInfoEnabled()) {
                    logger.info(printHostsStatus(tag + "Set hosts:"));
                }
                return this;
            } else {
                Thread.yield();
            }
        }

        newSettings.set(hosts);
        settingThreadPool.execute(settingInstallTask);

        //record host change time
        hostChangeTime.set(System.currentTimeMillis());
        return this;
    }

    /**
     * [可运行时修改]
     * 如果设置为false(默认), 当所有远端都被阻断时, nextHost方法返回一个后端.
     * 如果设置为true, 当所有远端都被阻断时, nextHost方法返回null.
     */
    public LoadBalancedHostManager setReturnNullIfAllBlocked(boolean returnNullIfAllBlocked) {
        this.returnNullIfAllBlocked = returnNullIfAllBlocked;
        return this;
    }

    /**
     * 设置客户端的标识
     * @param tag 标识
     */
    public LoadBalancedHostManager setTag(String tag) {
        this.tag = tag != null ? LOG_PREFIX + tag + "> " : LOG_PREFIX;
        return this;
    }

    /**
     * 获得当前远端列表和状态
     * @return value=true:可用, value=false:不可用
     */
    public Map<String, Boolean> getHostsStatus(){
        Host[] hostArray = this.hostArray;

        if (hostArray.length <= 0){
            return new HashMap<>(0);
        }

        long currentTimeMillis = System.currentTimeMillis();

        Map<String, Boolean> status = new HashMap<>(hostArray.length);
        for (Host host : hostArray){
            status.put(host.getUrl(), !host.isBlocked(currentTimeMillis));
        }
        return status;
    }

    @Override
    public String toString() {
        return printHostsStatus(null);
    }

    /**
     * 文本方式输出当前远端列表和状态
     * @param prefix 文本前缀
     * @return 远端列表和状态
     */
    public String printHostsStatus(String prefix){
        if (updating || newSettings.get() != null) {
            return (prefix != null ? prefix : "") + " Hosts updating";
        }

        Host[] hostArray = this.hostArray;

        StringBuilder stringBuilder = new StringBuilder(prefix != null ? prefix : "");

        if (hostArray.length <= 0){
            stringBuilder.append(" No host");
            return stringBuilder.toString();
        }

        long currentTimeMillis = System.currentTimeMillis();

        for (Host host : hostArray){
            stringBuilder.append(" ");
            stringBuilder.append(host.getUrl());
            stringBuilder.append(host.isBlocked(currentTimeMillis) ? "(bad)" : "(ok)");
        }
        return stringBuilder.toString();
    }

    Host[] getHostArray(){
        return this.hostArray;
    }

    private Runnable settingInstallTask = new Runnable() {
        @Override
        public void run() {
            boolean updated = false;
            List<String> newSettings;
            while ((newSettings = LoadBalancedHostManager.this.newSettings.getAndSet(null)) != null){
                settingInstall(newSettings);
                updated = true;
            }
            if (updated && logger.isInfoEnabled()) {
                logger.info(printHostsStatus(tag + "Set new hosts:"));
            }
        }
    };

    private void settingInstall(List<String> newSettings) {
        updating = true;

        Host[] hostArray = LoadBalancedHostManager.this.hostArray;

        int newSize = newSettings.size();
        Host[] newHostArray = new Host[newSize];
        Map<String, Integer> newHostIndexMap = new HashMap<>(newSize);

        for (int i = 0 ; i < newSize ; i++){

            //trim
            String newUrl = newSettings.get(i).trim();
            Integer oldIndex = hostIndexMap.get(newUrl);

            if (oldIndex != null){
                try {
                    newHostArray[i] = new Host(newUrl, hostArray[oldIndex].blockUntil, hostArray[oldIndex].recoveryUntil, hostArray[oldIndex].recoveryGate);
                } catch (Throwable ignore){
                    newHostArray[i] = new Host(newUrl, new AtomicLong(0), new AtomicLong(0), new AtomicInteger(0));
                }
            } else {
                newHostArray[i] = new Host(newUrl, new AtomicLong(0), new AtomicLong(0), new AtomicInteger(0));
            }

            newHostIndexMap.put(newUrl, i);

        }

        LoadBalancedHostManager.this.hostArray = newHostArray;
        hostIndexMap = newHostIndexMap;

        updating = false;
    }

    public static class Host {

        private String url;//URL
        private AtomicLong blockUntil;//阻断至
        private AtomicLong recoveryUntil;//恢复期至
        private AtomicInteger recoveryGate;//恢复期限流

        private Host(String url, AtomicLong blockUntil, AtomicLong recoveryUntil, AtomicInteger recoveryGate) {
            this.url = url;
            this.blockUntil = blockUntil;
            this.recoveryUntil = recoveryUntil;
            this.recoveryGate = recoveryGate;
        }

        /**
         * @return URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * 反馈后端健康状态(无阻断恢复期)
         * @param isOk true:后端健康 false:后端异常(需要阻断)
         * @param blockDuration (后端异常时)阻断时长, ms
         */
        public void feedback(boolean isOk, long blockDuration) {
            feedback(isOk, blockDuration, 1);
        }

        /**
         * 反馈后端健康状态
         * @param isOk true:后端健康 false:后端异常(需要阻断)
         * @param blockDuration (后端异常时)阻断时长, ms
         * @param recoveryCoefficient 阻断后的恢复期系数, 修复期时长 = blockDuration * recoveryCoefficient, 设置1则无恢复期
         */
        public void feedback(boolean isOk, long blockDuration, int recoveryCoefficient) {
            if (isOk) {
                release();
            } else {
                block(blockDuration, recoveryCoefficient);
            }
        }

        /**
         * 放行
         */
        private void release(){
            //解除阻断恢复期的流量限制
            this.recoveryGate.set(Integer.MIN_VALUE);
        }

        /**
         * 阻断
         *
         * @param blockDuration 阻断的时长, ms
         * @param recoveryCoefficient 阻断后的恢复期系数, 修复期时长 = 阻断时长 * recoveryCoefficient, 设置1则无恢复期
         */
        private void block(long blockDuration, int recoveryCoefficient){
            //最小1, 即无恢复期
            if (recoveryCoefficient < 1) {
                recoveryCoefficient = 1;
            }
            //当前时间
            long currentTime = System.currentTimeMillis();
            //阻断至
            long blockUntil = currentTime + blockDuration;
            if (blockUntil > this.blockUntil.get()){
                this.blockUntil.set(blockUntil);
            }
            //恢复期至
            long recoveryUntil = currentTime + blockDuration * recoveryCoefficient;
            if (recoveryUntil > this.recoveryUntil.get()) {
                this.recoveryUntil.set(recoveryUntil);
            }
            //恢复期流量重置(仅允许通过一次)
            this.recoveryGate.set(0);
        }

        /**
         * 该远端是否被阻断
         * @param currentTimeMillis 当前时间戳
         * @return true:被阻断(不可用), false:未阻断(可用)
         */
        private boolean isBlocked(long currentTimeMillis){
            //阻断期一律返回阻断
            if (currentTimeMillis < blockUntil.get()) {
                return true;
            }
            //恢复期限流
            if (currentTimeMillis < recoveryUntil.get()) {
                //阻断后, 恢复期只能放行一次
                //若恢复期的请求成功, 则有release方法释放流量控制
                if (recoveryGate.incrementAndGet() > 1) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Host<" + url + ">";
        }
    }

}
