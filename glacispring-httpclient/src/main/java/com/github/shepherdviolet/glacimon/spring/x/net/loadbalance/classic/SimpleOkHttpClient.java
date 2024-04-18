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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic;

import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalanceInspector;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalancedHostManager;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalancedInspectManager;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.ssl.SslConfigSupplier;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector.HttpGetLoadBalanceInspector;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector.TelnetLoadBalanceInspector;
import okhttp3.CookieJar;
import okhttp3.Dns;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import com.github.shepherdviolet.glacimon.java.misc.CloseableUtils;

import javax.net.ssl.HostnameVerifier;
import java.io.Closeable;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 *  <bean id="simpleOkHttpClient" class="com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.SimpleOkHttpClient">
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
 * @author shepherdviolet
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

    // Settings for hostManager ///////////////////////////////////////////////////////////////////////////////////

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
     * 设置/刷新远端列表, 该方法可以反复调用设置新的后端(但不是同步生效)
     *
     * @param hosts 远端列表
     */
    public SimpleOkHttpClient setHostList(List<String> hosts) {
        hostManager.setHostList(hosts);
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

    // Settings for inspectManager ///////////////////////////////////////////////////////////////////////////////////

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

    // Settings for super ///////////////////////////////////////////////////////////////////////////////////

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
     * true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false
     * @param verboseLog true:打印更多的调试日志, 默认关闭
     */
    @Override
    public SimpleOkHttpClient setVerboseLog(boolean verboseLog) {
        super.setVerboseLog(verboseLog);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置客户端的标识
     * @param tag 标识
     */
    @Override
    public SimpleOkHttpClient setTag(String tag) {
        super.setTag(tag);
        hostManager.setTag(tag);
        inspectManager.setTag(tag);
        return this;
    }

    /**
     * [可运行时修改]
     * <p>[配置]设置被动检测到网络故障时阻断后端的时间</p>
     *
     * <p>当请求服务端时, 发生特定的异常或返回特定的响应码(MultiHostOkHttpClient.needBlock方法决定), 客户端会将该
     * 后端服务器的IP/PORT标记为暂不可用状态, 阻断时长就是不可用的时长, 建议比主动探测器的探测间隔大.</p>
     *
     * @param passiveBlockDuration 阻断时长ms
     */
    @Override
    public SimpleOkHttpClient setPassiveBlockDuration(long passiveBlockDuration) {
        super.setPassiveBlockDuration(passiveBlockDuration);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置MediaType
     *
     * @param mediaType 设置MediaType
     */
    @Override
    public SimpleOkHttpClient setMediaType(String mediaType) {
        super.setMediaType(mediaType);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置编码
     *
     * @param encode 编码
     */
    @Override
    public SimpleOkHttpClient setEncode(String encode) {
        super.setEncode(encode);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置HTTP请求头参数
     *
     * @param headers 请求头参数
     */
    @Override
    public SimpleOkHttpClient setHeaders(Map<String, String> headers) {
        super.setHeaders(headers);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置HTTP请求头参数, 格式: key1=value1,key2=value2,key3=value3
     * 示例: User-Agent=GlacispringHttpClient,Referer=http://github.com
     * @param headersString 请求头参数
     */
    public SimpleOkHttpClient setHeadersString(String headersString) {
        super.setHeadersString(headersString);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置阻断后的恢复期系数, 修复期时长 = blockDuration * recoveryCoefficient, 设置1则无恢复期
     *
     * @param recoveryCoefficient 阻断后的恢复期系数, >= 1
     */
    @Override
    public SimpleOkHttpClient setRecoveryCoefficient(int recoveryCoefficient) {
        super.setRecoveryCoefficient(recoveryCoefficient);
        return this;
    }

    /**
     * [可运行时修改]
     * 最大闲置连接数. 客户端会保持与服务端的连接, 保持数量由此设置决定, 直到闲置超过5分钟. 默认16
     *
     * @param maxIdleConnections 最大闲置连接数, 默认16
     */
    @Override
    public SimpleOkHttpClient setMaxIdleConnections(int maxIdleConnections) {
        super.setMaxIdleConnections(maxIdleConnections);
        return this;
    }

    /**
     * [可运行时修改]
     * 最大请求线程数(仅异步请求时有效)
     *
     * @param maxThreads 最大请求线程数, 默认256
     */
    @Override
    public SimpleOkHttpClient setMaxThreads(int maxThreads) {
        super.setMaxThreads(maxThreads);
        return this;
    }

    /**
     * [可运行时修改]
     * 对应每个后端的最大请求线程数(仅异步请求时有效)
     *
     * @param maxThreadsPerHost 对应每个后端的最大请求线程数, 默认256
     */
    @Override
    public SimpleOkHttpClient setMaxThreadsPerHost(int maxThreadsPerHost) {
        super.setMaxThreadsPerHost(maxThreadsPerHost);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置连接超时ms
     *
     * @param connectTimeout 连接超时ms
     */
    @Override
    public SimpleOkHttpClient setConnectTimeout(long connectTimeout) {
        super.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置写数据超时ms
     *
     * @param writeTimeout 写数据超时ms
     */
    @Override
    public SimpleOkHttpClient setWriteTimeout(long writeTimeout) {
        super.setWriteTimeout(writeTimeout);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置读数据超时ms
     *
     * @param readTimeout 读数据超时ms
     */
    @Override
    public SimpleOkHttpClient setReadTimeout(long readTimeout) {
        super.setReadTimeout(readTimeout);
        return this;
    }

    /**
     * [可运行时修改]
     * 设置最大读取数据长度(默认:10M)
     *
     * @param maxReadLength 设置最大读取数据长度, 单位bytes
     */
    @Override
    public SimpleOkHttpClient setMaxReadLength(long maxReadLength) {
        super.setMaxReadLength(maxReadLength);
        return this;
    }

    /**
     * [可运行时修改]
     * CookieJar
     *
     * @param cookieJar CookieJar
     */
    @Override
    public SimpleOkHttpClient setCookieJar(CookieJar cookieJar) {
        super.setCookieJar(cookieJar);
        return this;
    }

    /**
     * [可运行时修改]
     * Proxy
     *
     * @param proxy 例如127.0.0.1:8080
     * @throws IllegalArgumentException if the proxy string is invalid
     * @throws NumberFormatException    if the string does not contain a parsable integer.
     * @throws SecurityException        if a security manager is present and permission to resolve the host name is denied.
     */
    @Override
    public SimpleOkHttpClient setProxy(String proxy) {
        super.setProxy(proxy);
        return this;
    }

    /**
     * [可运行时修改]
     * Dns
     *
     * @param dns Dns
     */
    @Override
    public SimpleOkHttpClient setDns(Dns dns) {
        super.setDns(dns);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>SSL配置提供者. 向HttpClient提供SSLSocketFactory. </p>
     *
     * <p>建议sslSocketFactory和trustManager一起设置, 如果只设置sslSocketFactory的话, OKHTTP会用反射的方式清理证书链.</p>
     *
     * <p>注意: 这个方式设置SSL会覆盖setCustomServerIssuer...系列设置的参数</p>
     *
     * <p>用途: </p>
     * <p>1.设置服务端证书的受信颁发者 </p>
     * <p>2.设置客户端证书(双向SSL) </p>
     * <p>3. ... </p>
     *
     * @param sslConfigSupplier sslConfigSupplier, 例如: SslSocketFactorySupplier / KeyAndTrustManagerSupplier / CertAndTrustedIssuerSupplier
     */
    @Override
    public SimpleOkHttpClient setSslConfigSupplier(SslConfigSupplier sslConfigSupplier) {
        super.setSslConfigSupplier(sslConfigSupplier);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加服务端证书的受信颁发者, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的颁发者.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <p>setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置自定义的颁发者.</p>
     *
     * @param customIssuerEncoded 添加服务端的根证书为受信任的证书, X509 Base64 编码的证书. 如果设置为"UNSAFE-TRUST-ALL-ISSUERS",
     *                            则不校验服务端证书链, 信任一切服务端证书, 不安全!!!
     */
    @Override
    public SimpleOkHttpClient setCustomServerIssuerEncoded(String customIssuerEncoded) {
        super.setCustomServerIssuerEncoded(customIssuerEncoded);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加服务端证书的受信颁发者, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的颁发者.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <p>setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级2 (第二).
     * 如果一个都不设置, 表示不配置自定义的颁发者.</p>
     *
     * @param customIssuersEncoded 添加服务端的根证书为受信任的证书, X509 Base64 编码的证书
     */
    @Override
    public SimpleOkHttpClient setCustomServerIssuersEncoded(String[] customIssuersEncoded) {
        super.setCustomServerIssuersEncoded(customIssuersEncoded);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加服务端证书的受信颁发者, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的颁发者.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <p>setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级3 (第三).
     * 如果一个都不设置, 表示不配置自定义的颁发者.</p>
     *
     * @param customIssuer 添加服务端的根证书为受信任的证书
     */
    @Override
    public SimpleOkHttpClient setCustomServerIssuer(X509Certificate customIssuer) {
        super.setCustomServerIssuer(customIssuer);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加服务端证书的受信颁发者, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的颁发者.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <p>setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级4 (最低).
     * 如果一个都不设置, 表示不配置自定义的颁发者.</p>
     *
     * @param customIssuers 添加服务端的根证书为受信任的证书
     */
    @Override
    public SimpleOkHttpClient setCustomServerIssuers(X509Certificate[] customIssuers) {
        super.setCustomServerIssuers(customIssuers);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加客户端证书, 用于双向SSL.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <psetCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL). 如果设置了客户端证书, 必须设置客户端私钥.</p>
     *
     * @param customClientCertEncoded 客户端证书, X509 Base64 编码的证书
     */
    @Override
    public SimpleOkHttpClient setCustomClientCertEncoded(String customClientCertEncoded) {
        super.setCustomClientCertEncoded(customClientCertEncoded);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加客户端证书链, 用于双向SSL.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <psetCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级2 (第二).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL). 如果设置了客户端证书, 必须设置客户端私钥.</p>
     *
     * @param customClientCertsEncoded 客户端证书链, X509 Base64 编码的证书
     */
    @Override
    public SimpleOkHttpClient setCustomClientCertsEncoded(String[] customClientCertsEncoded) {
        super.setCustomClientCertsEncoded(customClientCertsEncoded);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加客户端证书, 用于双向SSL.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <psetCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级3 (第三).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL). 如果设置了客户端证书, 必须设置客户端私钥.</p>
     *
     * @param customClientCert 客户端证书
     */
    @Override
    public SimpleOkHttpClient setCustomClientCert(X509Certificate customClientCert) {
        super.setCustomClientCert(customClientCert);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加客户端证书链, 用于双向SSL.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <psetCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级4 (最低).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL). 如果设置了客户端证书, 必须设置客户端私钥.</p>
     *
     * @param customClientCerts 客户端证书链
     */
    @Override
    public SimpleOkHttpClient setCustomClientCerts(X509Certificate[] customClientCerts) {
        super.setCustomClientCerts(customClientCerts);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加客户端证书私钥, 用于双向SSL.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <p>setCustomClientCertKey...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置客户端证书私钥(关闭双向SSL). 如果设置了客户端私钥, 必须设置客户端证书.</p>
     *
     * @param customClientCertKeyEncoded 客户端证书私钥, 目前仅支持RSA私钥, PKCS8 BASE64
     */
    @Override
    public SimpleOkHttpClient setCustomClientCertKeyEncoded(String customClientCertKeyEncoded) {
        super.setCustomClientCertKeyEncoded(customClientCertKeyEncoded);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>添加客户端证书私钥, 用于双向SSL.</p>
     * <p></p>
     * <p>注意: 这个方式设置SSL会覆盖setSslConfigSupplier设置的参数</p>
     * <p>setCustomClientCertKey...系列参数只需要设置一个, 同时设置时, 本参数优先级2 (最低).
     * 如果一个都不设置, 表示不配置客户端证书私钥(关闭双向SSL). 如果设置了客户端私钥, 必须设置客户端证书.</p>
     *
     * @param customClientCertKey 客户端证书私钥, 目前仅支持RSA私钥
     */
    @Override
    public SimpleOkHttpClient setCustomClientCertKey(Key customClientCertKey) {
        super.setCustomClientCertKey(customClientCertKey);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>设置自定义的服务端域名验证逻辑</p>
     * <p></p>
     * <p>1.如果你通过一个代理访问服务端, 且访问代理的域名, 请调用{@link MultiHostOkHttpClient#setVerifyServerCnByCustomHostname}
     * 设置服务端域名, 程序会改用指定的域名去匹配服务端证书的CN. 也可以调用{@link MultiHostOkHttpClient#setVerifyServerDnByCustomDn}
     * 匹配证书的全部DN信息. </p>
     *
     * <p>注意: 这个方式设置SSL会覆盖setVerifyServer...系列设置的参数</p>
     *
     * @param hostnameVerifier hostnameVerifier
     */
    @Override
    public SimpleOkHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        super.setHostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>使用指定的域名验证服务端证书的CN. </p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 如果你通过一个代理访问服务端, 且访问代理的域名, 这样会导致
     * 域名验证失败, 因为"客户端访问的域名与服务端证书的CN不符", 这种情况可以调用这个方法设置服务端的域名, 程序会改用指定的域名去匹配
     * 服务端证书的CN. 除此之外, 你也可以利用这个方法强制验证证书CN, 即你只信任指定CN的证书. </p>
     * <p></p>
     * <p>注意: 这个方式设置域名校验会覆盖setHostnameVerifier设置的参数</p>
     * <p>setVerifyServer...系列参数只需要设置一个, 同时设置时, 后设置的生效.</p>
     *
     * @param customHostname 指定服务端域名, 示例: www.baidu.com. 如果设置为"UNSAFE-TRUST-ALL-CN"则不校验CN, 所有合法证书都通过, 不安全!!!.
     *                       如果设置为null或""则取消设置.
     */
    @Override
    public SimpleOkHttpClient setVerifyServerCnByCustomHostname(String customHostname) {
        super.setVerifyServerCnByCustomHostname(customHostname);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>使用指定的域名验证服务端证书的DN. </p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 你可以利用这个方法强制验证证书DN, 即你只信任指定DN的证书. </p>
     * <p></p>
     * <p>注意: 这个方式设置域名校验会覆盖setHostnameVerifier设置的参数</p>
     * <p>setVerifyServer...系列参数只需要设置一个, 同时设置时, 后设置的生效.</p>
     *
     * @param customDn 指定服务端证书DN DN示例: CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\, Ltd,OU=service operation department,L=beijing,ST=beijing,C=CN.
     *                 如果设置为"UNSAFE-TRUST-ALL-DN"则不校验DN, 所有合法证书都通过, 不安全!!!, 如果设置为null或""则取消设置.
     */
    @Override
    public SimpleOkHttpClient setVerifyServerDnByCustomDn(String customDn) {
        super.setVerifyServerDnByCustomDn(customDn);
        return this;
    }

    /**
     * [可运行时修改]
     * <p>[配置]数据转换器, 用于将beanBody设置的JavaBean转换为byte[], 和将返回报文byte[]转换为JavaBean</p>
     *
     * @param dataConverter
     */
    @Override
    public SimpleOkHttpClient setDataConverter(DataConverter dataConverter) {
        super.setDataConverter(dataConverter);
        return this;
    }

    /**
     * [可运行时修改]
     * 当HTTP返回码为指定返回码时, 阻断后端
     *
     * @param codes 指定需要阻断的返回码, 例如:403,404
     */
    @Override
    public SimpleOkHttpClient setHttpCodeNeedBlock(String codes) {
        super.setHttpCodeNeedBlock(codes);
        return this;
    }

    /**
     * [可运行时修改]
     * 当异常为指定类型时, 阻断后端
     *
     * @param throwableTypes 指定需要阻断的异常类型, 例如:com.package.BarException,com.package.FooException
     */
    @Override
    public SimpleOkHttpClient setThrowableNeedBlock(Set<Class<? extends Throwable>> throwableTypes) {
        super.setThrowableNeedBlock(throwableTypes);
        return this;
    }

    /**
     * [可运行时修改]
     * 当异常为指定类型时, 阻断后端
     *
     * @param throwableTypes 指定需要阻断的异常类型, 例如:com.package.BarException,com.package.FooException
     */
    @Override
    public SimpleOkHttpClient setThrowableNeedBlock(String throwableTypes) {
        super.setThrowableNeedBlock(throwableTypes);
        return this;
    }

    /**
     * [可运行时修改]
     * 启用/禁用TxTimer统计请求耗时(暂时只支持同步方式), 默认禁用
     *
     * @param enabled
     */
    @Override
    public SimpleOkHttpClient setTxTimerEnabled(boolean enabled) {
        super.setTxTimerEnabled(enabled);
        return this;
    }

    /**
     * [可运行时修改]
     * 打印更多的日志, 细粒度配置, 默认全打印<br>
     * <p>
     * VERBOSE_LOG_CONFIG_ALL:{@value VERBOSE_LOG_CONFIG_ALL}<br>
     * VERBOSE_LOG_CONFIG_REQUEST_INPUTS:{@value VERBOSE_LOG_CONFIG_REQUEST_INPUTS}<br>
     * VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY:{@value VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY}<br>
     * VERBOSE_LOG_CONFIG_RAW_URL:{@value VERBOSE_LOG_CONFIG_RAW_URL}<br>
     * VERBOSE_LOG_CONFIG_RESPONSE_CODE:{@value VERBOSE_LOG_CONFIG_RESPONSE_CODE}<br>
     *
     * @param verboseLogConfig 详细配置
     */
    @Override
    public SimpleOkHttpClient setVerboseLogConfig(int verboseLogConfig) {
        super.setVerboseLogConfig(verboseLogConfig);
        return this;
    }

    /**
     * [可运行时修改]
     * 日志打印细粒度配置, 默认全打印<br>
     * <p>
     * LOG_CONFIG_ALL:{@value LOG_CONFIG_ALL}<br>
     * LOG_CONFIG_REAL_URL:{@value LOG_CONFIG_REAL_URL}<br>
     * LOG_CONFIG_BLOCK:{@value LOG_CONFIG_BLOCK}<br>
     *
     * @param logConfig 详细配置
     */
    @Override
    public SimpleOkHttpClient setLogConfig(int logConfig) {
        super.setLogConfig(logConfig);
        return this;
    }

    /**
     * [可运行时修改]
     * true: 开启简易的请求日志追踪(请求日志追加4位数追踪号), 默认false<br>
     *
     * @param requestTraceEnabled true: 开启简易的请求日志追踪(请求日志追加4位数追踪号), 默认false
     */
    @Override
    public SimpleOkHttpClient setRequestTraceEnabled(boolean requestTraceEnabled) {
        super.setRequestTraceEnabled(requestTraceEnabled);
        return this;
    }
}
