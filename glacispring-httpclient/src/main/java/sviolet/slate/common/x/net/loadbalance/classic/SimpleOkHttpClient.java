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

package sviolet.slate.common.x.net.loadbalance.classic;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import sviolet.slate.common.x.net.loadbalance.LoadBalanceInspector;
import sviolet.slate.common.x.net.loadbalance.LoadBalancedHostManager;
import sviolet.slate.common.x.net.loadbalance.LoadBalancedInspectManager;
import sviolet.slate.common.x.net.loadbalance.inspector.HttpGetLoadBalanceInspector;
import sviolet.slate.common.x.net.loadbalance.inspector.TelnetLoadBalanceInspector;
import sviolet.thistle.util.common.CloseableUtils;

import java.io.Closeable;

/**
 * <p>简化版MultiHostOkHttpClient (Spring专用, 依赖spring-beans包)</p>
 *
 * <p>在MultiHostOkHttpClient的基础上, 封装了LoadBalancedHostManager和LoadBalancedInspectManager, 简化了配置, 免去了配置三个Bean的麻烦 <br>
 * 1.配置被简化, 如需高度定制, 请使用LoadBalancedHostManager + LoadBalancedInspectManager + MultiHostOkHttpClient <br>
 * 2.内置的LoadBalancedInspectManager采用TELNET方式探测后端(不可自定义探测方式, 但可以配置为HttpGet探测方式)<br>
 * 3.屏蔽了setHostManager()方法, 调用会抛出异常<br>
 * 4.实现了DisposableBean, 在Spring容器中会自动销毁<br>
 * </p>
 *
 * <p>Java:</p>
 *
 * <pre>{@code
 *
 *      SimpleOkHttpClient client = new SimpleOkHttpClient()
 *              .setHosts("http://127.0.0.1:8081,http://127.0.0.1:8082")
 *              .setInitiativeInspectInterval(5000L)
 *              .setMaxThreads(256)
 *              .setMaxThreadsPerHost(256)
 *              .setPassiveBlockDuration(30000L)
 *              .setConnectTimeout(3000L)
 *              .setWriteTimeout(10000L)
 *              .setReadTimeout(10000L);
 *
 * }</pre>
 *
 * <p>Spring MVC:</p>
 *
 * <pre>{@code
 *
 *  <bean id="simpleOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.SimpleOkHttpClient">
 *      <property name="hosts" value="http://127.0.0.1:8081,http://127.0.0.1:8082"/>
 *      <property name="initiativeInspectInterval" value="5000"/>
 *      <property name="maxThreads" value="256"/>
 *      <property name="maxThreadsPerHost" value="256"/>
 *      <property name="passiveBlockDuration" value="30000"/>
 *      <property name="connectTimeout" value="3000"/>
 *      <property name="writeTimeout" value="10000"/>
 *      <property name="readTimeout" value="10000"/>
 *  </bean>
 *
 * }</pre>
 *
 *
 * @author S.Violet
 * @see MultiHostOkHttpClient
 *
 */
public class SimpleOkHttpClient extends MultiHostOkHttpClient implements Closeable, InitializingBean, DisposableBean {

    private LoadBalancedHostManager hostManager = new LoadBalancedHostManager();
    private LoadBalancedInspectManager inspectManager = new LoadBalancedInspectManager(false).setHostManager(hostManager);

    public SimpleOkHttpClient() {
        super.setHostManager(hostManager);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * <p>手动开始主动探测器</p>
     * <p>若SimpleOkHttpClient在Spring中注册为Bean, 则无需调用此方法, 主动探测器会在Spring启动后自动开始.</p>
     * <p>若SimpleOkHttpClient没有被注册为Bean(直接new出来的), 则需要调用此方法开始主动探测器. </p>
     */
    public void start() {
        inspectManager.start();
    }

    @Override
    public void close() {
        CloseableUtils.closeQuiet(inspectManager);
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    public String toString() {
        return super.toString() + (inspectManager != null ? " Inspect [ " + inspectManager + " ]" : "");
    }

    /**
     * 文本方式输出当前远端列表和状态
     * @param prefix 文本前缀
     * @return 远端列表和状态
     */
    public String printHostsStatus(String prefix){
        return hostManager.printHostsStatus(prefix);
    }

    // Settings ///////////////////////////////////////////////////////////////////////////////////

    /**
     * @deprecated 禁用该方法
     */
    @Override
    @Deprecated
    public MultiHostOkHttpClient setHostManager(LoadBalancedHostManager hostManager) {
        throw new IllegalStateException("setHostManager method can not invoke in SimpleOkHttpClient");
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * 设置/刷新远端列表, 该方法可以反复调用设置新的后端(但不是同步生效)
     *
     * @param hosts 远端列表, 格式:"http://127.0.0.1:8081/,http://127.0.0.1:8082/"
     */
    public SimpleOkHttpClient setHosts(String hosts) {
        hostManager.setHosts(hosts);
        return this;
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * 设置/刷新远端列表, 该方法可以反复调用设置新的后端(但不是同步生效)
     *
     * @param hosts 远端列表
     */
    public SimpleOkHttpClient setHostArray(String[] hosts) {
        hostManager.setHostArray(hosts);
        return this;
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * 设置主动探测间隔 (主动探测器)
     * @param initiativeInspectInterval 检测间隔ms, > 0 , 建议 > 5000
     */
    public SimpleOkHttpClient setInitiativeInspectInterval(long initiativeInspectInterval) {
        inspectManager.setInspectInterval(initiativeInspectInterval);
        return this;
    }

    /**
     * [可运行时修改]
     * 如果设置为false(默认), 当所有远端都被阻断时, nextHost方法返回一个后端.
     * 如果设置为true, 当所有远端都被阻断时, nextHost方法返回null.
     */
    public SimpleOkHttpClient setReturnNullIfAllBlocked(boolean returnNullIfAllBlocked) {
        hostManager.setReturnNullIfAllBlocked(returnNullIfAllBlocked);
        return this;
    }

    /**
     * [可运行时修改]
     * 将主动探测器从TELNET型修改为HTTP-GET型
     * @param urlSuffix 探测页面URL(例如:http://127.0.0.1:8080/health, 则在此处设置/health), 设置为+telnet+则使用默认的TELNET型
     */
    public SimpleOkHttpClient setHttpGetInspector(String urlSuffix) {
        if ("+telnet+".equals(urlSuffix)) {
            inspectManager.setInspector(new TelnetLoadBalanceInspector());
        } else {
            inspectManager.setInspector(new HttpGetLoadBalanceInspector(urlSuffix, inspectManager.getInspectTimeout()));
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置自定义的主动探测器, 这个方法与setHttpGetInspector方法只能选择一个设置
     * @param inspector 自定义主动探测器, 默认为TELNET方式
     */
    public SimpleOkHttpClient setInspector(LoadBalanceInspector inspector) {
        if (inspector == null) {
            inspector = new TelnetLoadBalanceInspector();
        }
        inspectManager.setInspector(inspector);
        return this;
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * true: 主动探测器打印更多的日志, 默认false
     * @param verboseLog true: 主动探测器打印更多的日志, 默认false
     */
    public SimpleOkHttpClient setInspectorVerboseLog(boolean verboseLog) {
        inspectManager.setVerboseLog(verboseLog);
        return this;
    }

    /**
     * [线程安全/异步生效/可运行时修改]
     * true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false
     * @param verboseLog true:打印更多的调试日志, 默认关闭
     */
    @Override
    public MultiHostOkHttpClient setVerboseLog(boolean verboseLog) {
        super.setVerboseLog(verboseLog);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置客户端的标识
     * @param tag 标识
     */
    @Override
    public MultiHostOkHttpClient setTag(String tag) {
        super.setTag(tag);
        hostManager.setTag(tag);
        inspectManager.setTag(tag);
        return this;
    }

}
