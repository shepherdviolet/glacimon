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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.springboot.autoconfig;

import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient;

import java.util.Arrays;

/**
 * <p>Http客户端实例参数</p>
 *
 * <p>配置前缀: glacispring.httpclients</p>
 *
 * @author shepherdviolet
 */
public class HttpClientSettings {

    /**
     * [可运行时修改]
     * 设置远端列表: 逗号分隔格式. 若hosts和hostList同时设置, 则只有hosts配置生效.
     * 例如:
     * hosts: http://localhost:8080,http://localhost:8081
     */
    private String hosts = "";

    /**
     * [可运行时修改]
     * 设置远端列表: 列表格式. 若hosts和hostList同时设置, 则只有hosts配置生效.
     * 例如:
     * hostList:
     *  - http://localhost:8080
     *  - http://localhost:8081
     */
    private String[] hostList = new String[0];

    /**
     * [可运行时修改]
     * 设置主动探测间隔 (主动探测器); 若设置成<=0, 则暂停主动探测(暂停特性:2025.0.1+)
     */
    private long initiativeInspectInterval = 5000L;

    /**
     * [可运行时修改]
     * 如果设置为false(默认), 当所有远端都被阻断时, nextHost方法返回一个后端.
     * 如果设置为true, 当所有远端都被阻断时, nextHost方法返回null.
     */
    private boolean returnNullIfAllBlocked = false;

    /**
     * [可运行时修改]
     * 将主动探测器从默认的TELNET型修改为HTTP-GET型
     * urlSuffix 探测页面URL(例如:http://127.0.0.1:8080/health, 则在此处设置/health), 设置为+telnet+则使用默认的TELNET型, 设置+disable+禁用主动探测
     */
    private String httpGetInspectorUrlSuffix = "+telnet+";

    /**
     * [可运行时修改]
     * <p>设置被动检测到网络故障时阻断后端的时间, 单位ms</p>
     *
     * <p>当请求服务端时, 发生特定的异常或返回特定的响应码(GlaciHttpClient.needBlock方法决定), 客户端会将该
     * 后端服务器的IP/PORT标记为暂不可用状态, 阻断时长就是不可用的时长, 建议比主动探测器的探测间隔大.</p>
     */
    private long passiveBlockDuration = 30000L;

    /**
     * [可运行时修改]
     * 设置MediaType
     */
    private String mediaType = "application/json;charset=utf-8";

    /**
     * [可运行时修改]
     * 设置编码
     */
    private String encode = "utf-8";

    /**
     * [可运行时修改]
     * 设置HTTP请求头参数
     */
    private String headers;

    /**
     * [可运行时修改]
     * 设置阻断后的恢复期系数, 修复期时长 = blockDuration * recoveryCoefficient, 设置1则无恢复期
     */
    private int recoveryCoefficient = 10;

    /**
     * [可运行时修改]
     * 最大闲置连接数. 若设置为0(默认), 每次重新解析域名+重新建立连接, 性能差, 但支持动态域名解析. 若设置为正整数(例如16), 会复用连接池中的连接, 性能强, 但若DNS域名解析记录更新, 可能会向原IP发送请求.
     */
    private int maxIdleConnections = 0;

    /**
     * [可运行时修改]
     * 最大请求线程数(仅异步请求时有效)
     */
    private int maxThreads = 256;

    /**
     * [可运行时修改]
     * 对应每个后端的最大请求线程数(仅异步请求时有效)
     */
    private int maxThreadsPerHost = 256;

    /**
     * [可运行时修改]
     * 设置连接超时ms
     */
    private long connectTimeout = 3000L;

    /**
     * [可运行时修改]
     * 设置写数据超时ms
     */
    private long writeTimeout = 10000L;

    /**
     * [可运行时修改]
     * 设置读数据超时ms
     */
    private long readTimeout = 10000L;

    /**
     * [可运行时修改]
     * 设置最大读取数据长度(默认:10M), 单位bytes
     */
    private long maxReadLength = 10L * 1024L * 1024L;

    /**
     * [可运行时修改]
     * 当HTTP返回码为指定返回码时, 阻断后端
     * codes 指定需要阻断的返回码, 例如:403,404
     */
    private String httpCodeNeedBlock;

    /**
     * [可运行时修改]
     * 当异常为指定类型时, 阻断后端.
     * 指定需要阻断的异常类型, 例如:com.package.BarException,com.package.FooException
     */
    private String throwableNeedBlock;

    /**
     * [可运行时修改]
     * 启用/禁用TxTimer统计请求耗时(只支持同步方式), 默认禁用, 需手动依赖glacispring-txtimer
     */
    private boolean txTimerEnabled = false;

    /**
     * [可运行时修改]
     * true: 开启简易的请求日志追踪(请求日志追加4位数追踪号), 默认false<br>
     */
    private boolean requestTraceEnabled = false;

    /**
     * <p>[可运行时修改]</p>
     * <p>给GlaciHttpClient设置服务端证书的受信颁发者, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的颁发者.</p>
     * <p></p>
     * <p>1.该参数优先级高于customServerIssuersEncoded, 同时设置该参数生效</p>
     * <p>2.设置这个参数会覆盖GlaciHttpClient#setSslConfigSupplier</p>
     */
    private String customServerIssuerEncoded;

    /**
     * <p>[可运行时修改]</p>
     * <p>给GlaciHttpClient设置服务端证书的受信颁发者, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的颁发者.</p>
     * <p></p>
     * <p>1.该参数优先级低于customServerIssuerEncoded, 同时设置该参数无效</p>
     * <p>2.设置这个参数会覆盖GlaciHttpClient#setSslConfigSupplier</p>
     */
    private String[] customServerIssuersEncoded;

    /**
     * <p>[可运行时修改]</p>
     * <p>[双向SSL]设置客户端证书</p>
     * <p></p>
     * <p>1.该参数优先级高于 customClientCertsEncoded, 同时设置该参数生效</p>
     * <p>2.如果一个都不设置, 表示不配置客户端证书(关闭双向SSL)</p>
     * <p>3.如果设置了客户端证书, 必须设置客户端私钥.</p>
     */
    private String customClientCertEncoded;

    /**
     * <p>[可运行时修改]</p>
     * <p>[双向SSL]设置客户端证书链</p>
     * <p></p>
     * <p>1.该参数优先级低于 customClientCertEncoded, 同时设置该参数无效</p>
     * <p>2.如果一个都不设置, 表示不配置客户端证书(关闭双向SSL)</p>
     * <p>3.如果设置了客户端证书, 必须设置客户端私钥.</p>
     */
    private String[] customClientCertsEncoded;

    /**
     * <p>[可运行时修改]</p>
     * <p>[双向SSL]设置客户端证书对应的私钥</p>
     * <p></p>
     * <p>1.如果设置了客户端私钥, 必须设置客户端证书.</p>
     */
    private String customClientCertKeyEncoded;

    /**
     * <p>[可运行时修改]</p>
     * <p>使用指定的域名验证服务端证书的DN. 如果设置为"UNSAFE-TRUST-ALL-DN"则不校验DN, 所有合法证书都通过, 不安全!!!</p>
     * <p>示例: CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\, Ltd,OU=service operation department,L=beijing,ST=beijing,C=CN</p>
     * <p></p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 你可以利用这个方法强制验证证书DN, 即你只信任指定DN的证书. </p>
     * <p></p>
     * <p>1.该参数优先级高于verifyServerCnByCustomHostname, 同时设置该参数生效</p>
     * <p>2.设置这个参数会覆盖GlaciHttpClient#setHostnameVerifier</p>
     */
    public String verifyServerDnByCustomDn;

    /**
     * <p>[可运行时修改]</p>
     * <p>使用指定的域名验证服务端证书的CN. 如果设置为"UNSAFE-TRUST-ALL-CN"则不校验CN, 所有合法证书都通过, 不安全!!!</p>
     * <p>示例: www.baidu.com</p>
     * <p></p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 如果你通过一个代理访问服务端, 且访问代理的域名, 这样会导致
     * 域名验证失败, 因为"客户端访问的域名与服务端证书的CN不符", 这种情况可以调用这个方法设置服务端的域名, 程序会改用指定的域名去匹配
     * 服务端证书的CN. 除此之外, 你也可以利用这个方法强制验证证书CN, 即你只信任指定CN的证书. </p>
     * <p></p>
     * <p>1.该参数优先级低于verifyServerDnByCustomDn, 同时设置该参数无效</p>
     * <p>2.设置这个参数会覆盖GlaciHttpClient#setHostnameVerifier</p>
     */
    public String verifyServerCnByCustomHostname;

    /**
     * [可运行时修改]
     * </p>配置自定义Dns</p>
     * <p>参数采用SimpleKeyValueEncoder格式, 详见: https://github.com/shepherdviolet/glacimon/blob/master/docs/kvencoder/guide.md</p>
     * <p></p>
     * <p>参数说明:</p>
     * <p>ip: DNS服务地址, 必输; 多个地址使用'|'分割, 例如: ip=8.8.8.8|114.114.114.114</p>
     * <p>resolveTimeoutSeconds: 域名解析超时时间(秒), 可选, 默认5s</p>
     * <p>preferIpv6: true:Ipv6优先, false:Ipv4优先, 可选, 默认false</p>
     * <p>minTtlSeconds: 最小TTL(秒), 可选, 默认20; 实际TTL为max(服务器返回TTL, 该参数值)</p>
     * <p>maxTtlSeconds: 最大TTL(秒), 可选, 默认300; 实际TTL为min(服务器返回TTL, 该参数值)</p>
     * <p>errorTtlSeconds: 域名解析错误时的TTL(秒), 可选, 默认0</p>
     * <p>updMinIntervalSec: 后台自动更新最小间隔(秒), 可选, 默认5; 程序会在TTL到期前自动更新域名解析记录, 这是更新线程最小间隔.</p>
     * <p>updMaxIntervalSec: 后台自动更新最大间隔(秒), 可选, 默认3600; 程序会在TTL到期前自动更新域名解析记录, 这是更新线程最大间隔.</p>
     * <p>isBackgroundUpdate: true:启用后台自动更新, false:关闭后台自动更新, 可选, 默认true</p>
     * <p>reportIntervalSec: DNS解析报告打印间隔(秒), 可选, 默认3600; 程序会在日志中打印DNS解析相关统计信息</p>
     * <p>stopUpdAftFails: 域名解析失败指定次数后, 停止自动更新, 可选, 默认5; 仅影响自动更新, 不影响同步解析</p>
     * <p>stopUpdAftIdleSec: 域名未使用指定时间(秒)后, 停止自动更新, 可选, 默认1200; 仅影响自动更新, 不影响同步解析</p>
     * <p></p>
     * <p>参数格式: key1=value1,key2=value2</p>
     * <p></p>
     * <p>示例(设置一个DNS): ip=8.8.8.8</p>
     * <p>示例(设置多个DNS): ip=8.8.8.8|114.114.114.114</p>
     * <p>示例(设置解析超时时间): ip=8.8.8.8,resolveTimeoutSeconds=5</p>
     * <p>示例(设置ipv6是否优先): ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false</p>
     * <p>示例(设置最大最小TTL): ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false,minTtlSeconds=30,maxTtlSeconds=300</p>
     * <p>示例(设置是否后台自动更新): ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false,isBackgroundUpdate=true</p>
     */
    public String dnsDescription;

    /**
     * <p>[可运行时修改]</p>
     * <p>日志:请求URL, 默认true</p>
     */
    private boolean logPrintUrl = true;

    /**
     * <p>[可运行时修改]</p>
     * <p>日志:阻断日志, 默认true</p>
     */
    private boolean logPrintBlock = true;

    /**
     * <p>[可运行时修改]</p>
     * <p>日志:请求/响应报文体: 支持"byte[]/Bean/表单"请求, 支持"byte[]/Bean"响应, 不支持"RequestBody"请求, 不支持"InputStream/ResponsePackage"响应,
     * 默认false</p>
     */
    private boolean logPrintPayload = false;

    /**
     * <p>[可运行时修改]</p>
     * <p>日志:响应码, 默认false</p>
     */
    private boolean logPrintStatusCode = false;

    /**
     * <p>[可运行时修改]</p>
     * <p>日志:输入参数, 默认false</p>
     */
    private boolean logPrintInputs = false;

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String[] getHostList() {
        return hostList;
    }

    public void setHostList(String[] hostList) {
        this.hostList = hostList;
    }

    public long getInitiativeInspectInterval() {
        return initiativeInspectInterval;
    }

    public void setInitiativeInspectInterval(long initiativeInspectInterval) {
        this.initiativeInspectInterval = initiativeInspectInterval;
    }

    public boolean isReturnNullIfAllBlocked() {
        return returnNullIfAllBlocked;
    }

    public void setReturnNullIfAllBlocked(boolean returnNullIfAllBlocked) {
        this.returnNullIfAllBlocked = returnNullIfAllBlocked;
    }

    public String getHttpGetInspectorUrlSuffix() {
        return httpGetInspectorUrlSuffix;
    }

    public void setHttpGetInspectorUrlSuffix(String httpGetInspectorUrlSuffix) {
        this.httpGetInspectorUrlSuffix = httpGetInspectorUrlSuffix;
    }

    public long getPassiveBlockDuration() {
        return passiveBlockDuration;
    }

    public void setPassiveBlockDuration(long passiveBlockDuration) {
        this.passiveBlockDuration = passiveBlockDuration;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public int getRecoveryCoefficient() {
        return recoveryCoefficient;
    }

    public void setRecoveryCoefficient(int recoveryCoefficient) {
        this.recoveryCoefficient = recoveryCoefficient;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxThreadsPerHost() {
        return maxThreadsPerHost;
    }

    public void setMaxThreadsPerHost(int maxThreadsPerHost) {
        this.maxThreadsPerHost = maxThreadsPerHost;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getMaxReadLength() {
        return maxReadLength;
    }

    public void setMaxReadLength(long maxReadLength) {
        this.maxReadLength = maxReadLength;
    }

    public String getHttpCodeNeedBlock() {
        return httpCodeNeedBlock;
    }

    public void setHttpCodeNeedBlock(String httpCodeNeedBlock) {
        this.httpCodeNeedBlock = httpCodeNeedBlock;
    }

    public String getThrowableNeedBlock() {
        return throwableNeedBlock;
    }

    public void setThrowableNeedBlock(String throwableNeedBlock) {
        this.throwableNeedBlock = throwableNeedBlock;
    }

    public boolean isTxTimerEnabled() {
        return txTimerEnabled;
    }

    public void setTxTimerEnabled(boolean txTimerEnabled) {
        this.txTimerEnabled = txTimerEnabled;
    }

    public boolean isRequestTraceEnabled() {
        return requestTraceEnabled;
    }

    public void setRequestTraceEnabled(boolean requestTraceEnabled) {
        this.requestTraceEnabled = requestTraceEnabled;
    }

    public String getCustomServerIssuerEncoded() {
        return customServerIssuerEncoded;
    }

    public void setCustomServerIssuerEncoded(String customServerIssuerEncoded) {
        this.customServerIssuerEncoded = customServerIssuerEncoded;
    }

    public String[] getCustomServerIssuersEncoded() {
        return customServerIssuersEncoded;
    }

    public void setCustomServerIssuersEncoded(String[] customServerIssuersEncoded) {
        this.customServerIssuersEncoded = customServerIssuersEncoded;
    }

    public String getCustomClientCertEncoded() {
        return customClientCertEncoded;
    }

    public void setCustomClientCertEncoded(String customClientCertEncoded) {
        this.customClientCertEncoded = customClientCertEncoded;
    }

    public String[] getCustomClientCertsEncoded() {
        return customClientCertsEncoded;
    }

    public void setCustomClientCertsEncoded(String[] customClientCertsEncoded) {
        this.customClientCertsEncoded = customClientCertsEncoded;
    }

    public String getCustomClientCertKeyEncoded() {
        return customClientCertKeyEncoded;
    }

    public void setCustomClientCertKeyEncoded(String customClientCertKeyEncoded) {
        this.customClientCertKeyEncoded = customClientCertKeyEncoded;
    }

    public String getVerifyServerDnByCustomDn() {
        return verifyServerDnByCustomDn;
    }

    public void setVerifyServerDnByCustomDn(String verifyServerDnByCustomDn) {
        this.verifyServerDnByCustomDn = verifyServerDnByCustomDn;
    }

    public String getVerifyServerCnByCustomHostname() {
        return verifyServerCnByCustomHostname;
    }

    public void setVerifyServerCnByCustomHostname(String verifyServerCnByCustomHostname) {
        this.verifyServerCnByCustomHostname = verifyServerCnByCustomHostname;
    }

    public String getDnsDescription() {
        return dnsDescription;
    }

    public void setDnsDescription(String dnsDescription) {
        this.dnsDescription = dnsDescription;
    }

    public boolean isLogPrintUrl() {
        return logPrintUrl;
    }

    public void setLogPrintUrl(boolean logPrintUrl) {
        this.logPrintUrl = logPrintUrl;
    }

    public boolean isLogPrintBlock() {
        return logPrintBlock;
    }

    public void setLogPrintBlock(boolean logPrintBlock) {
        this.logPrintBlock = logPrintBlock;
    }

    public boolean isLogPrintPayload() {
        return logPrintPayload;
    }

    public void setLogPrintPayload(boolean logPrintPayload) {
        this.logPrintPayload = logPrintPayload;
    }

    public boolean isLogPrintStatusCode() {
        return logPrintStatusCode;
    }

    public void setLogPrintStatusCode(boolean logPrintStatusCode) {
        this.logPrintStatusCode = logPrintStatusCode;
    }

    public boolean isLogPrintInputs() {
        return logPrintInputs;
    }

    public void setLogPrintInputs(boolean logPrintInputs) {
        this.logPrintInputs = logPrintInputs;
    }

    @Override
    public String toString() {
        return "HttpClientSettings{" +
                "hosts='" + hosts + '\'' +
                ", hostList=" + Arrays.toString(hostList) +
                ", initiativeInspectInterval=" + initiativeInspectInterval +
                ", returnNullIfAllBlocked=" + returnNullIfAllBlocked +
                ", httpGetInspectorUrlSuffix='" + httpGetInspectorUrlSuffix + '\'' +
                ", passiveBlockDuration=" + passiveBlockDuration +
                ", mediaType='" + mediaType + '\'' +
                ", encode='" + encode + '\'' +
                ", headers='" + headers + '\'' +
                ", recoveryCoefficient=" + recoveryCoefficient +
                ", maxIdleConnections=" + maxIdleConnections +
                ", maxThreads=" + maxThreads +
                ", maxThreadsPerHost=" + maxThreadsPerHost +
                ", connectTimeout=" + connectTimeout +
                ", writeTimeout=" + writeTimeout +
                ", readTimeout=" + readTimeout +
                ", maxReadLength=" + maxReadLength +
                ", httpCodeNeedBlock='" + httpCodeNeedBlock + '\'' +
                ", throwableNeedBlock='" + throwableNeedBlock + '\'' +
                ", txTimerEnabled=" + txTimerEnabled +
                ", requestTraceEnabled=" + requestTraceEnabled +
                ", customServerIssuerEncoded='" + customServerIssuerEncoded + '\'' +
                ", customServerIssuersEncoded=" + Arrays.toString(customServerIssuersEncoded) +
                ", customClientCertEncoded='" + customClientCertEncoded + '\'' +
                ", customClientCertsEncoded=" + Arrays.toString(customClientCertsEncoded) +
                ", customClientCertKeyEncoded='" + customClientCertKeyEncoded + '\'' +
                ", verifyServerDnByCustomDn='" + verifyServerDnByCustomDn + '\'' +
                ", verifyServerCnByCustomHostname='" + verifyServerCnByCustomHostname + '\'' +
                ", dnsDescription='" + dnsDescription + '\'' +
                ", logPrintUrl=" + logPrintUrl +
                ", logPrintBlock=" + logPrintBlock +
                ", logPrintPayload=" + logPrintPayload +
                ", logPrintStatusCode=" + logPrintStatusCode +
                ", logPrintInputs=" + logPrintInputs +
                '}';
    }

}
