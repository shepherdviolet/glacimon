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

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.monitor.txtimer.TimerContext;
import sviolet.slate.common.x.monitor.txtimer.noref.NoRefTxTimer;
import sviolet.slate.common.x.monitor.txtimer.noref.NoRefTxTimerFactory;
import sviolet.slate.common.x.net.loadbalance.LoadBalancedHostManager;
import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.judge.CheckUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>支持均衡负载的OkHttpClient(简单的示例模板, 建议自行实现)</p>
 *
 * <p>Java:</p>
 *
 * <pre>{@code
 *
 *      LoadBalancedHostManager hostManager = new LoadBalancedHostManager()
 *              .setHostArray(new String[]{
 *                  "http://127.0.0.1:8080",
 *                  "http://127.0.0.1:8081"
 *              });
 *
 *      LoadBalancedInspectManager inspectManager = new LoadBalancedInspectManager()
 *              .setHostManager(hostManager)
 *              .setInspectInterval(5000L)
 *              .setInspector(new TelnetLoadBalanceInspector());
 *
 *      MultiHostOkHttpClient client = new MultiHostOkHttpClient()
 *              .setHostManager(hostManager)
 *              .setMaxThreads(256)
 *              .setMaxThreadsPerHost(256)
 *              .setPassiveBlockDuration(30000L)
 *              .setConnectTimeout(3000L)
 *              .setWriteTimeout(10000L)
 *              .setReadTimeout(10000L);
 *
 * }</pre>
 *
 * <p>Spring MVC: 注册了SlateServletContextListener的场合</p>
 *
 * <pre>{@code
 *
 *  <bean id="loadBalancedHostManager" class="sviolet.slate.common.x.net.loadbalance.LoadBalancedHostManager">
 *      <property name="hosts" value="http://127.0.0.1:8081,http://127.0.0.1:8082"/>
 *  </bean>
 *
 *  <bean id="loadBalancedInspector" class="sviolet.slate.common.x.net.loadbalance.LoadBalancedInspectManager">
 *      <property name="hostManager" ref="loadBalancedHostManager"/>
 *      <property name="inspectInterval" value="5000"/>
 *  </bean>
 *
 *  <bean id="multiHostOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.MultiHostOkHttpClient">
 *      <property name="hostManager" ref="loadBalancedHostManager"/>
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
 * <p>Spring MVC: 没注册SlateServletContextListener的场合, 需要设置destroy-method="close"</p>
 *
 * <pre>{@code
 *
 *  <bean id="loadBalancedHostManager" class="sviolet.slate.common.x.net.loadbalance.LoadBalancedHostManager">
 *      <property name="hosts" value="http://127.0.0.1:8081,http://127.0.0.1:8082"/>
 *  </bean>
 *
 *  <bean id="loadBalancedInspector" class="sviolet.slate.common.x.net.loadbalance.LoadBalancedInspectManager"
 *      destroy-method="close">
 *      <property name="hostManager" ref="loadBalancedHostManager"/>
 *      <property name="inspectInterval" value="5000"/>
 *  </bean>
 *
 *  <bean id="multiHostOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.MultiHostOkHttpClient">
 *      <property name="hostManager" ref="loadBalancedHostManager"/>
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
 * @author S.Violet
 */
public class MultiHostOkHttpClient {

    //普通日志:全开
    public static final int LOG_CONFIG_ALL = 0xFFFFFFFF;
    //普通日志:全关
    public static final int LOG_CONFIG_NONE = 0x00000000;
    //普通日志:实际请求的URL(不带参数)
    public static final int LOG_CONFIG_REAL_URL = 0x00000001;
    //普通日志:阻断日志
    public static final int LOG_CONFIG_BLOCK = 0x00000010;
    //普通日志:默认
    public static final int LOG_CONFIG_DEFAULT = LOG_CONFIG_ALL;

    //额外日志:全开
    public static final int VERBOSE_LOG_CONFIG_ALL = 0xFFFFFFFF;
    //额外日志:全关
    public static final int VERBOSE_LOG_CONFIG_NONE = 0x00000000;
    //额外日志:输入参数(默认关)
    public static final int VERBOSE_LOG_CONFIG_REQUEST_INPUTS = 0x00000001;
    //额外日志:请求报文体(最高可读性)
    public static final int VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY= 0x00000010;
    //额外信息:请求URL(带参数, 且参数未转码)
    public static final int VERBOSE_LOG_CONFIG_RAW_URL= 0x00000100;
    //额外日志:响应码
    public static final int VERBOSE_LOG_CONFIG_RESPONSE_CODE = 0x00001000;
    //额外日志:默认
    public static final int VERBOSE_LOG_CONFIG_DEFAULT = VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY |
                                                        VERBOSE_LOG_CONFIG_RAW_URL |
                                                        VERBOSE_LOG_CONFIG_RESPONSE_CODE;

    private static final String LOG_PREFIX = "HttpClient | ";

    private static final long PASSIVE_BLOCK_DURATION = 30000L;
    private static final String MEDIA_TYPE = "application/json;charset=utf-8";
    private static final String ENCODE = "utf-8";
    private static final String TXTIMER_GROUP_SEND = "MultiHostOkHttpClient-Send-";
    private static final String TXTIMER_GROUP_CONNECT = "MultiHostOkHttpClient-Connect-";

    private static final Logger logger = LoggerFactory.getLogger(MultiHostOkHttpClient.class);
    private static final AtomicInteger requestCounter = new AtomicInteger(0);

    private volatile OkHttpClient okHttpClient;
    private LoadBalancedHostManager hostManager;

    private Settings settings = new Settings();
    private volatile boolean refreshSettings = false;
    private volatile Exception clientCreateException;
    private AtomicBoolean settingsLock = new AtomicBoolean(false);
    private SettingsSpinLock settingsSpinLock = new SettingsSpinLock();

    private NoRefTxTimer txTimer;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 请求 ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>创建POST请求, 请求创建过程非线程安全, 请勿多线程操作同一个请求</p>
     *
     * <p>https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/invoke-sync.md</p>
     * <p>https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/invoke-async.md</p>
     *
     * @param urlSuffix 请求的url后缀, 不含协议/域名/端口, 例如/user/add.json. 如果是一个完整的URL, 可以借助HttpUrl.parse(url)
     *                  获取域名端口后面的请求路径(pathSegments)和请求参数(query).
     */
    public Request post(String urlSuffix) {
        return new Request(this, urlSuffix, true,
                settings.requestTraceEnabled ? requestCounter.getAndIncrement() & 0x00000FFF : Integer.MAX_VALUE);
    }

    /**
     * <p>创建GET请求, 请求创建过程非线程安全, 请勿多线程操作同一个请求</p>
     *
     * <p>https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/invoke-sync.md</p>
     * <p>https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/invoke-async.md</p>
     *
     * @param urlSuffix 请求的url后缀, 不含协议/域名/端口, 例如/user/add.json. 如果是一个完整的URL, 可以借助HttpUrl.parse(url)
     *                  获取域名端口后面的请求路径(pathSegments)和请求参数(query).
     */
    public Request get(String urlSuffix) {
        return new Request(this, urlSuffix, false,
                settings.requestTraceEnabled ? requestCounter.getAndIncrement() & 0x00000FFF : Integer.MAX_VALUE);
    }

    /**
     * 请求(该对象非线程安全, 请勿多线程操作同一个对象)
     */
    public static class Request {

        //status
        private WeakReference<MultiHostOkHttpClient> clientReference;
        private boolean isSend = false;
        private int requestId;

        //basic
        private String urlSuffix;
        private boolean isPost = false;
        private Map<String, String> headers;
        private Map<String, Object> urlParams;

        //body
        private byte[] body;
        private Map<String, Object> formBody;
        private Object beanBody;
        private RequestBody customBody;

        //senior
        private boolean autoClose = true;
        private long passiveBlockDuration = -1;
        private String mediaType;
        private String encode;
        private DataConverter dataConverter;
        private Stub stub = new Stub();

        private Request(MultiHostOkHttpClient client, String urlSuffix, boolean isPost, int requestId) {
            this.clientReference = new WeakReference<>(client);
            this.urlSuffix = urlSuffix;
            this.isPost = isPost;
            this.requestId = requestId;
        }

        /**
         * <p>[配置]URL参数, 即HTTP请求中URL后面跟随的?key=value&key=value</p>
         */
        public Request urlParams(Map<String, Object> urlParams) {
            this.urlParams = urlParams;
            return this;
        }

        /**
         * <p>[配置]添加一个URL参数, 即HTTP请求中URL后面跟随的?key=value&key=value</p>
         */
        public Request urlParam(String key, Object value) {
            if (this.urlParams == null) {
                this.urlParams = new HashMap<>(8);
            }
            this.urlParams.put(key, value);
            return this;
        }

        /**
         * <p>[配置]POST请求专用: 请求报文体, byte[]类型</p>
         */
        public Request body(byte[] body) {
            if (!isPost) {
                throw new IllegalArgumentException("You can not set body in GET request");
            }
            this.body = body;
            this.formBody = null;
            this.beanBody = null;
            this.customBody = null;
            return this;
        }

        /**
         * <p>[配置]POST请求专用: 请求报文体, 表单</p>
         */
        public Request formBody(Map<String, Object> formBody) {
            if (!isPost) {
                throw new IllegalArgumentException("You can not set body in GET request");
            }
            this.body = null;
            this.formBody = formBody;
            this.beanBody = null;
            this.customBody = null;
            return this;
        }

        /**
         * <p>[配置]POST请求专用: 请求报文体, JavaBean <br>
         * 注意: 必须配置DataConverter, 否则发送时会报错</p>
         */
        public Request beanBody(Object beanBody) {
            if (!isPost) {
                throw new IllegalArgumentException("You can not set body in GET request");
            }
            this.body = null;
            this.formBody = null;
            this.beanBody = beanBody;
            this.customBody = null;
            return this;
        }

        /**
         * <p>
         *     [配置]POST请求专用: 请求报文体, OkHttp RequestBody<br>
         *     用这个会使得Request.mediaType()无效, 会由Builder().setType()指定.
         * </p>
         *
         * <pre>
         *     //Multipart Form 示例
         *     //文件
         *     RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), fileOrData);
         *     //Multipart请求体
         *     RequestBody requestBody = new MultipartBody.Builder()
         *             .setType(MultipartBody.FORM)
         *             .addFormDataPart("file", "file_name", fileBody)
         *             .addFormDataPart("param1", value1)
         *             .addFormDataPart("param2", value2)
         *             .build();
         * </pre>
         */
        public Request customBody(RequestBody customBody) {
            if (!isPost) {
                throw new IllegalArgumentException("You can not set body in GET request");
            }
            this.body = null;
            this.formBody = null;
            this.beanBody = null;
            this.customBody = customBody;
            return this;
        }

        /**
         * <p>[配置]HTTP请求头参数, 客户端配置和此处配置的均生效(此处配置优先)</p>
         */
        public Request httpHeaders(Map<String, String> httpHeaders) {
            this.headers = httpHeaders;
            return this;
        }

        /**
         * <p>[配置]添加一个HTTP请求头参数, 客户端配置和此处配置的均生效(此处配置优先)</p>
         */
        public Request httpHeader(String key, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>(8);
            }
            this.headers.put(key, value);
            return this;
        }

        /**
         * <p>[配置]设置被动检测到网络故障时阻断后端的时间, 客户端配置和此处配置的均生效(此处配置优先)</p>
         *
         * <p>当请求服务端时, 发生特定的异常或返回特定的响应码(MultiHostOkHttpClient.needBlock方法决定), 客户端会将该
         * 后端服务器的IP/PORT标记为暂不可用状态, 而阻断时长是不可用的时长</p>
         *
         * @param passiveBlockDuration 阻断时长ms
         */
        public Request passiveBlockDuration(long passiveBlockDuration) {
            this.passiveBlockDuration = passiveBlockDuration;
            return this;
        }

        /**
         * <p>[配置]报文体MediaType, 客户端配置和此处配置的均生效(此处配置优先)</p>
         */
        public Request mediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        /**
         * <p>[配置]字符编码, 客户端配置和此处配置的均生效(此处配置优先)</p>
         */
        public Request encode(String encode) {
            this.encode = encode;
            return this;
        }

        /**
         * <p>[配置]数据转换器, 用于将beanBody设置的JavaBean转换为byte[], 和将返回报文byte[]转换为JavaBean <br>
         * 客户端配置和此处配置的均生效(此处配置优先)</p>
         */
        public Request dataConverter(DataConverter dataConverter) {
            this.dataConverter = dataConverter;
            return this;
        }

        /**
         * <p>[配置]异步请求专用: 配置响应实例(或输入流)是否在回调方法onSucceed结束后自动关闭, 默认true</p>
         *
         * <p>注意:同步请求返回的ResponsePackage/InputStream是必须手动关闭的!!!</p>
         *
         * <p>
         * 当autoClose=true时, onSucceed方法回调结束后, ResponsePackage/InputStream会被自动关闭, 无需手动调用close方法. 适用于
         * 响应数据在回调方法中处理完的场合.<br>
         * 当autoClose=false时, onSucceed方法回调结束后, ResponsePackage/InputStream不会自动关闭, 需要手动调用ResponsePackage.close()关闭,
         * 注意!!! 适用于响应数据需要交由其他的线程处理, 或暂时持有的场合使用.
         * </p>
         */
        public Request autoClose(boolean autoClose){
            this.autoClose = autoClose;
            return this;
        }

        /**
         * <p>[配置]该次请求的连接超时, 单位ms</p>
         */
        public Request connectTimeout(int connectTimeout) {
            this.stub.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * <p>[配置]该次请求的写数据超时, 单位ms</p>
         */
        public Request writeTimeout(int writeTimeout) {
            this.stub.writeTimeout = writeTimeout;
            return this;
        }

        /**
         * <p>[配置]该次请求的读数据超时, 单位ms</p>
         */
        public Request readTimeout(int readTimeout) {
            this.stub.readTimeout = readTimeout;
            return this;
        }

        /**
         * <p>[请求发送]同步请求并获取Bean返回,
         * 如果响应码不为2XX, 会抛出HttpRejectException异常.<br>
         * 注意: 必须配置DataConverter, 否则会报错</p>
         *
         * @return 响应, 可能为null
         * @throws NoHostException       当前没有可发送的后端(网络请求发送前的异常, 准备阶段异常)
         * @throws RequestBuildException 请求初始化异常(通常是网络请求发送前的异常, 准备阶段异常)
         * @throws IOException           网络通讯异常(通常是网络请求发送中的异常)
         * @throws HttpRejectException   Http请求拒绝异常(网络请求发送后的异常, HTTP响应码不为2XX)
         */
        public <T> T sendForBean(Class<T> type) throws NoHostException, RequestBuildException, HttpRejectException, IOException {
            MultiHostOkHttpClient client = getClient();
            if (client == null) {
                throw new RequestBuildException("Missing MultiHostOkHttpClient instance, has been destroyed (cleaned by gc)");
            }
            NoRefTxTimer txTimer = client.txTimer;
            if (txTimer != null) {
                try (TimerContext timerContext = txTimer.entry(TXTIMER_GROUP_SEND + client.settings.tag, urlSuffix)) {
                    return client.responseToBean(client.requestSend(this), type, this);
                }
            } else {
                return client.responseToBean(client.requestSend(this), type, this);
            }
        }

        /**
         * [请求发送]同步请求并获取byte[]返回,
         * 如果响应码不为2XX, 会抛出HttpRejectException异常
         *
         * @return 响应, 可能为null
         * @throws NoHostException       当前没有可发送的后端(网络请求发送前的异常, 准备阶段异常)
         * @throws RequestBuildException 请求初始化异常(通常是网络请求发送前的异常, 准备阶段异常)
         * @throws IOException           网络通讯异常(通常是网络请求发送中的异常)
         * @throws HttpRejectException   Http请求拒绝异常(网络请求发送后的异常, HTTP响应码不为2XX)
         */
        public byte[] sendForBytes() throws NoHostException, RequestBuildException, HttpRejectException, IOException {
            MultiHostOkHttpClient client = getClient();
            if (client == null) {
                throw new RequestBuildException("Missing MultiHostOkHttpClient instance, has been destroyed (cleaned by gc)");
            }
            NoRefTxTimer txTimer = client.txTimer;
            if (txTimer != null) {
                try (TimerContext timerContext = txTimer.entry(TXTIMER_GROUP_SEND + client.settings.tag, urlSuffix)) {
                    return client.responseToBytes(client.requestSend(this));
                }
            } else {
                return client.responseToBytes(client.requestSend(this));
            }
        }

        /**
         * [请求发送]同步请求并获取InputStream返回,
         * 如果响应码不为2XX, 会抛出HttpRejectException异常
         *
         * @return 响应, 可能为null, InputStream用完后必须手动关闭!!!
         * @throws NoHostException       当前没有可发送的后端(网络请求发送前的异常, 准备阶段异常)
         * @throws RequestBuildException 请求初始化异常(通常是网络请求发送前的异常, 准备阶段异常)
         * @throws IOException           网络通讯异常(通常是网络请求发送中的异常)
         * @throws HttpRejectException   Http请求拒绝异常(网络请求发送后的异常, HTTP响应码不为2XX)
         */
        public InputStream sendForInputStream() throws NoHostException, RequestBuildException, HttpRejectException, IOException {
            MultiHostOkHttpClient client = getClient();
            if (client == null) {
                throw new RequestBuildException("Missing MultiHostOkHttpClient instance, has been destroyed (cleaned by gc)");
            }
            NoRefTxTimer txTimer = client.txTimer;
            if (txTimer != null) {
                try (TimerContext timerContext = txTimer.entry(TXTIMER_GROUP_SEND + client.settings.tag, urlSuffix)) {
                    return client.responseToInputStream(client.requestSend(this));
                }
            } else {
                return client.responseToInputStream(client.requestSend(this));
            }
        }

        /**
         * [请求发送]同步请求并获取ResponsePackage返回,
         * 如果响应码不为2XX, 会抛出HttpRejectException异常,
         * 该方法不会根据maxReadLength限定最大读取长度
         * @return 响应, 可能为null, ResponsePackage用完后必须手动关闭!!!
         * @throws NoHostException       当前没有可发送的后端(网络请求发送前的异常, 准备阶段异常)
         * @throws RequestBuildException 请求初始化异常(通常是网络请求发送前的异常, 准备阶段异常)
         * @throws IOException           网络通讯异常(通常是网络请求发送中的异常)
         * @throws HttpRejectException   Http请求拒绝异常(网络请求发送后的异常, HTTP响应码不为2XX)
         */
        public ResponsePackage send() throws NoHostException, RequestBuildException, IOException, HttpRejectException {
            MultiHostOkHttpClient client = getClient();
            if (client == null) {
                throw new RequestBuildException("Missing MultiHostOkHttpClient instance, has been destroyed (cleaned by gc)");
            }
            NoRefTxTimer txTimer = client.txTimer;
            if (txTimer != null) {
                try (TimerContext timerContext = txTimer.entry(TXTIMER_GROUP_CONNECT + client.settings.tag, urlSuffix)) {
                    return client.requestSend(this);
                }
            } else {
                return client.requestSend(this);
            }
        }

        /**
         * [请求发送]异步请求,
         * 如果响应码不为2XX, 会回调onErrorAfterSend()方法给出HttpRejectException异常 <br>
         * 注意: 必须配置DataConverter, 否则发送时会报错</p>
         * @param callback 回调函数{@link BeanCallback}
         */
        public Stub enqueue(BeanCallback<?> callback) {
            enqueue((ResponsePackageCallback)callback);
            return stub;
        }

        /**
         * [请求发送]异步请求,
         * 如果响应码不为2XX, 会回调onErrorAfterSend()方法给出HttpRejectException异常,
         * @param callback 回调函数{@link BytesCallback}
         */
        public Stub enqueue(BytesCallback callback) {
            enqueue((ResponsePackageCallback)callback);
            return stub;
        }

        /**
         * [请求发送]异步请求,
         * 如果响应码不为2XX, 会回调onErrorAfterSend()方法给出HttpRejectException异常,
         * @param callback 回调函数{@link InputStreamCallback}
         */
        public Stub enqueue(InputStreamCallback callback) {
            enqueue((ResponsePackageCallback)callback);
            return stub;
        }

        /**
         * [请求发送]异步请求,
         * 如果响应码不为2XX, 会回调onErrorAfterSend()方法给出HttpRejectException异常,
         * 该方法不会根据maxReadLength限定最大读取长度
         * @param callback 回调函数{@link BytesCallback}/{@link InputStreamCallback}/{@link ResponsePackageCallback}
         */
        public Stub enqueue(ResponsePackageCallback callback) {
            MultiHostOkHttpClient client = getClient();
            if (client == null) {
                callback.onErrorBeforeSend(new RequestBuildException("Missing MultiHostOkHttpClient instance, has been destroyed (cleaned by gc)"));
                return stub;
            }
            client.requestEnqueue(this, callback);
            return stub;
        }

        private MultiHostOkHttpClient getClient(){
            MultiHostOkHttpClient client = clientReference.get();
            if (client == null) {
                logger.error("Missing MultiHostOkHttpClient instance, has been destroyed (cleaned by gc), data:" + this);
            }
            return client;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "urlSuffix='" + urlSuffix + '\'' +
                    ", isPost=" + isPost +
                    ", headers=" + headers +
                    ", urlParams=" + urlParams +
                    ", body=" + ByteUtils.bytesToHex(body) +
                    ", formBody=" + formBody +
                    ", beanBody=" + beanBody +
                    ", customBody=" + customBody +
                    ", passiveBlockDuration=" + passiveBlockDuration +
                    ", mediaType='" + mediaType + '\'' +
                    ", encode='" + encode + '\'' +
                    ", dataConverter=" + dataConverter +
                    '}';
        }
    }

    private ResponsePackage requestSend(Request request) throws NoHostException, RequestBuildException, HttpRejectException, IOException {
        if (request.isSend) {
            throw new IllegalStateException("MultiHostOkHttpClient.Request can only send once!");
        }
        request.isSend = true;

        if (request.isPost) {
            return syncPost(request);
        } else {
            return syncGet(request);
        }
    }

    private void requestEnqueue(Request request, ResponsePackageCallback callback) {
        if (request.isSend) {
            throw new IllegalStateException("MultiHostOkHttpClient.Request can only send once!");
        }
        request.isSend = true;

        if (request.isPost) {
            asyncPost(request, callback);
        } else {
            asyncGet(request, callback);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Sync ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private <T> T responseToBean(ResponsePackage responsePackage, Class<T> type, Request request) throws NoHostException, RequestBuildException, IOException, HttpRejectException {
        DataConverter dataConverter = request.dataConverter != null ? request.dataConverter : settings.dataConverter;
        if (dataConverter == null) {
            //强制关闭
            try {
                responsePackage.close();
            } catch (Throwable ignore) {
            }
            throw new RequestConvertException("No DataConverter set, you must set dataConverter before sendForBean()");
        }
        byte[] responseData = responseToBytes(responsePackage);
        try {
            return dataConverter.convert(responseData, type);
        } catch (Exception e) {
            throw new ResponseConvertException("Error while convert byte[] to bean", e);
        }
    }

    private byte[] responseToBytes(ResponsePackage responsePackage) throws NoHostException, RequestBuildException, IOException, HttpRejectException {
        //返回空
        if (responsePackage == null || responsePackage.body() == null) {
            return null;
        }
        try {
            //限定读取长度
            if (settings.maxReadLength > 0 && responsePackage.body().contentLength() > settings.maxReadLength){
                throw new IOException("Response contentLength is out of limit, contentLength:" + responsePackage.body().contentLength() + ", limit:" + settings.maxReadLength);
            }
            //返回二进制数据
            return responsePackage.body().bytes();
        } finally {
            //返回byte[]类型时自动关闭
            try {
                responsePackage.close();
            } catch (Throwable ignore) {
            }
        }
    }

    private InputStream responseToInputStream(ResponsePackage responsePackage) throws NoHostException, RequestBuildException, IOException, HttpRejectException {
        //返回空
        if (responsePackage == null || responsePackage.body() == null) {
            return null;
        }
        //限定读取长度
        if (settings.maxReadLength > 0 && responsePackage.body().contentLength() > settings.maxReadLength){
            try {
                responsePackage.body().close();
            } catch (Throwable ignore) {
            }
            throw new IOException("Response contentLength is out of limit, contentLength:" + responsePackage.body().contentLength() + ", limit:" + settings.maxReadLength);
        }
        //返回二进制数据
        return responsePackage.body().byteStream();
    }

    private ResponsePackage syncPost(Request request) throws NoHostException, RequestBuildException, IOException, HttpRejectException {
        //获取远端
        LoadBalancedHostManager.Host host = fetchHost();

        printPostInputsLog(request, host);
        printUrlLog(request, host);

        //装配Request
        okhttp3.Request okRequest;
        try {
            okRequest = buildPostRequest(host.getUrl(), request, settings);
        } catch (Throwable t) {
            throw new RequestBuildException("Error while building request", t);
        }
        if (okRequest == null) {
            throw new RequestBuildException("Null request built");
        }

        if (logger.isInfoEnabled() && CheckUtils.isFlagMatch(settings.logConfig, LOG_CONFIG_REAL_URL)) {
            logger.info(genLogPrefix(settings.tag, request) + "POST: real-url:" + okRequest.url().toString());
        }

        //请求
        return syncCall(host, okRequest, request);
    }

    private ResponsePackage syncGet(Request request) throws NoHostException, RequestBuildException, IOException, HttpRejectException {
        //获取远端
        LoadBalancedHostManager.Host host = fetchHost();

        printGetInputsLog(request, host);
        printUrlLog(request, host);

        //装配Request
        okhttp3.Request okRequest;
        try {
            okRequest = buildGetRequest(host.getUrl(), request, settings);
        } catch (Throwable t) {
            throw new RequestBuildException("Error while building request", t);
        }
        if (okRequest == null) {
            throw new RequestBuildException("Null request built");
        }

        if (logger.isInfoEnabled() && CheckUtils.isFlagMatch(settings.logConfig, LOG_CONFIG_REAL_URL)) {
            logger.info(genLogPrefix(settings.tag, request) + "GET: real-url:" + okRequest.url().toString());
        }

        //请求
        return syncCall(host, okRequest, request);
    }

    private ResponsePackage syncCall(LoadBalancedHostManager.Host host, okhttp3.Request okRequest, Request request) throws RequestBuildException, IOException, HttpRejectException {
        //后端是否健康
        boolean isOk = true;
        //被动阻断时长
        long passiveBlockDuration = request.passiveBlockDuration >= 0 ? request.passiveBlockDuration : settings.passiveBlockDuration;
        try {
            //同步请求
            Response response = getOkHttpClient().newCall(okRequest).execute();
            printResponseCodeLog(request, response);
            //Http拒绝
            if (!isSucceed(response)) {
                CloseableUtils.closeQuiet(response);
                throw new HttpRejectException(response.code(), response.message());
            }
            //报文体
            return ResponsePackage.newInstance(request, response);
        } catch (Throwable t) {
            if (needBlock(t, settings)) {
                //网络故障阻断后端
                isOk = false;
                if (logger.isInfoEnabled() && CheckUtils.isFlagMatch(settings.logConfig, LOG_CONFIG_BLOCK)){
                    logger.info(genLogPrefix(settings.tag, request) + "Bad host " + host.getUrl() + ", block for " + passiveBlockDuration + " ms, passive block, recoveryCoefficient " + settings.recoveryCoefficient);
                }
            }
            if (t instanceof  IOException ||
                    t instanceof HttpRejectException) {
                throw t;
            } else {
                throw new RequestBuildException("Error while request build ?", t);
            }
        } finally {
            //反馈健康状态
            host.feedback(isOk, passiveBlockDuration, settings.recoveryCoefficient);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Async //////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void asyncPost(Request request, ResponsePackageCallback callback) {

        callback.setContext(settings, request);

        try {
            //获取远端
            LoadBalancedHostManager.Host host = fetchHost();

            printPostInputsLog(request, host);
            printUrlLog(request, host);

            //装配Request
            okhttp3.Request okRequest;
            try {
                okRequest = buildPostRequest(host.getUrl(), request, settings);
            } catch (Throwable t) {
                throw new RequestBuildException("Error while building request", t);
            }
            if (okRequest == null) {
                throw new RequestBuildException("Null request built");
            }

            if (logger.isInfoEnabled() && CheckUtils.isFlagMatch(settings.logConfig, LOG_CONFIG_REAL_URL)) {
                logger.info(genLogPrefix(settings.tag, request) + "POST: real-url:" + okRequest.url().toString());
            }

            //请求
            asyncCall(host, okRequest, request, callback);
        } catch (NoHostException | RequestBuildException e) {
            callback.onErrorBeforeSend(e);
        }
    }

    private void asyncGet(Request request, ResponsePackageCallback callback) {

        callback.setContext(settings, request);

        try {
            //获取远端
            LoadBalancedHostManager.Host host = fetchHost();

            printGetInputsLog(request, host);
            printUrlLog(request, host);

            //装配Request
            okhttp3.Request okRequest;
            try {
                okRequest = buildGetRequest(host.getUrl(), request, settings);
            } catch (Throwable t) {
                throw new RequestBuildException("Error while building request", t);
            }
            if (okRequest == null) {
                throw new RequestBuildException("Null request built");
            }

            if (logger.isInfoEnabled() && CheckUtils.isFlagMatch(settings.logConfig, LOG_CONFIG_REAL_URL)) {
                logger.info(genLogPrefix(settings.tag, request) + "GET: real-url:" + okRequest.url().toString());
            }

            //请求
            asyncCall(host, okRequest, request, callback);
        } catch (NoHostException | RequestBuildException e) {
            callback.onErrorBeforeSend(e);
        }
    }

    private void asyncCall(final LoadBalancedHostManager.Host host, okhttp3.Request okRequest, final Request request, final ResponsePackageCallback callback)  {
        //异步请求
        try {
            getOkHttpClient().newCall(okRequest).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    printResponseCodeLog(request, response);
                    //Http拒绝
                    if (!isSucceed(response)) {
                        CloseableUtils.closeQuiet(response);
                        Exception exception = new HttpRejectException(response.code(), response.message());
                        tryBlock(exception);
                        callback.onErrorAfterSend(exception);
                        return;
                    }
                    //反馈健康(反馈健康无需计算阻断时长)
                    host.feedback(true, 0);
                    //报文体
                    try {
                        callback.onSucceed(ResponsePackage.newInstance(request, response));
                        //自动关闭
                        if (request.autoClose) {
                            CloseableUtils.closeQuiet(response);
                        }
                    } catch (Exception e) {
                        //处理onSucceed
                        callback.errorOnSucceedProcessing(e);
                        //强制关闭
                        CloseableUtils.closeQuiet(response);
                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    tryBlock(e);
                    callback.onErrorAfterSend(e);
                }
                private void tryBlock(Exception e){
                    if (needBlock(e, settings)) {
                        //网络故障阻断后端
                        long passiveBlockDuration = request.passiveBlockDuration >= 0 ? request.passiveBlockDuration : settings.passiveBlockDuration;
                        //反馈异常
                        host.feedback(false, passiveBlockDuration, settings.recoveryCoefficient);
                        if (logger.isInfoEnabled() && CheckUtils.isFlagMatch(settings.logConfig, LOG_CONFIG_BLOCK)) {
                            logger.info(genLogPrefix(settings.tag, request) + "Bad host " + host.getUrl() + ", block for " + passiveBlockDuration + " ms, passive block, recoveryCoefficient " + settings.recoveryCoefficient);
                        }
                    } else {
                        //反馈健康(反馈健康无需计算阻断时长)
                        host.feedback(true, 0);
                    }
                }
            });
        } catch (Exception t) {
            callback.onErrorBeforeSend(new RequestBuildException("Error while request build ?", t));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 私有逻辑 //////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private LoadBalancedHostManager.Host fetchHost() throws NoHostException {
        LoadBalancedHostManager.Host host = hostManager.nextHost();
        if (host == null){
            throw new NoHostException("No host");
        }
        return host;
    }

    private OkHttpClient getOkHttpClient(){
        //客户端创建错误后, 不再重试
        if (clientCreateException != null) {
            throw new IllegalStateException("Client create error", clientCreateException);
        }
        //一次检查
        while (okHttpClient == null || refreshSettings) {
            //自旋锁
            if (!settingsLock.get() && settingsLock.compareAndSet(false, true)) {
                try {
                    //二次检查e
                    if (okHttpClient == null || refreshSettings) {
                        OkHttpClient client = createOkHttpClient(settings);
                        okHttpClient = client;
                        refreshSettings = false;
                        //跳出循环, 避免因为refreshSettings反复被改为true反复由同一个线程创建客户端
                        break;
                    }
                } catch (Exception e) {
                    clientCreateException = e;
                    throw e;
                } finally {
                    //解锁
                    settingsLock.set(false);
                }
            } else {
                Thread.yield();
            }
        }
        return okHttpClient;
    }

    private void printPostInputsLog(Request request, LoadBalancedHostManager.Host host) {
        if (!logger.isDebugEnabled() || !CheckUtils.isFlagMatch(settings.verboseLogConfig, VERBOSE_LOG_CONFIG_REQUEST_INPUTS)) {
            return;
        }

        String bodyLog;
        if (request.body != null) {
            bodyLog = ", body(hex):" + ByteUtils.bytesToHex(request.body);
        } else if (request.formBody != null) {
            bodyLog = ", formBody:" + request.formBody;
        } else if (request.beanBody != null) {
            bodyLog = ", beanBody:" + request.beanBody;
        } else if (request.customBody != null) {
            bodyLog = ", customBody:" + request.customBody;
        } else {
            bodyLog = ", body: null";
        }
        logger.debug(genLogPrefix(settings.tag, request) + "POST: url:" + host.getUrl() + ", suffix:" + request.urlSuffix + ", urlParams:" + request.urlParams + bodyLog);
    }

    private void printPostStringBodyLog(Request request, byte[] parsedData) {
        if (settings.verboseLog) {
            if (!logger.isInfoEnabled()) {
                return;
            }
        } else {
            if (!logger.isDebugEnabled()) {
                return;
            }
        }

        if (!CheckUtils.isFlagMatch(settings.verboseLogConfig, VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY)){
            return;
        }

        if (request.body != null) {
            try {
                logger.info(genLogPrefix(settings.tag, request) + "POST: string-body:" + new String(request.body, settings.encode));
            } catch (Exception e) {
                logger.warn(genLogPrefix(settings.tag, request) + "Error while printing string body", e);
            }
        } else if (request.formBody != null) {
            logger.info(genLogPrefix(settings.tag, request) + "POST: string-body(form):" + request.formBody);
        } else if (request.beanBody != null && parsedData != null) {
            try {
                logger.info(genLogPrefix(settings.tag, request) + "POST: string-body(bean):" + new String(parsedData, settings.encode));
            } catch (Exception e) {
                logger.warn(genLogPrefix(settings.tag, request) + "Error while printing string body", e);
            }
        } else if (request.customBody != null) {
            logger.info(genLogPrefix(settings.tag, request) + "POST: string-body: multipart data can not be print");
        } else {
            logger.info(genLogPrefix(settings.tag, request) + "POST: string-body: null");
        }
    }

    private void printGetInputsLog(Request request, LoadBalancedHostManager.Host host) {
        if (!logger.isDebugEnabled() || !CheckUtils.isFlagMatch(settings.verboseLogConfig, VERBOSE_LOG_CONFIG_REQUEST_INPUTS)) {
            return;
        }
        logger.debug(genLogPrefix(settings.tag, request) + "GET: url:" + host.getUrl() + ", suffix:" + request.urlSuffix + ", urlParams:" + request.urlParams);
    }

    private void printUrlLog(Request request, LoadBalancedHostManager.Host host) {
        if (!logger.isDebugEnabled() || !CheckUtils.isFlagMatch(settings.verboseLogConfig, VERBOSE_LOG_CONFIG_RAW_URL)) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder("raw-url:" + host.getUrl() + request.urlSuffix);
        if (request.urlParams != null && request.urlParams.size() > 0) {
            stringBuilder.append("?");
            int i = 0;
            for (Map.Entry<String, Object> entry : request.urlParams.entrySet()) {
                if (i++ > 0) {
                    stringBuilder.append("&");
                }
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=");
                stringBuilder.append(entry.getValue());
            }

        }
        logger.debug(genLogPrefix(settings.tag, request) + stringBuilder.toString());
    }

    private void printResponseCodeLog(Request request, Response response) {
        if (settings.verboseLog) {
            if (!logger.isInfoEnabled()) {
                return;
            }
        } else {
            if (!logger.isDebugEnabled()) {
                return;
            }
        }
        if (!CheckUtils.isFlagMatch(settings.verboseLogConfig, VERBOSE_LOG_CONFIG_RESPONSE_CODE)) {
            return;
        }
        logger.info(genLogPrefix(settings.tag, request) + "Response: code:" + response.code() + ", message:" + response.message());
    }

    private String genLogPrefix(String tag, Request request){
        if (request.requestId == Integer.MAX_VALUE) {
            return tag;
        }
        return tag + request.requestId + " ";
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 可复写逻辑 //////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 初始化OkHttpClient实例(复写本方法实现自定义的逻辑)
     * @return OkHttpClient实例
     */
    @SuppressWarnings("deprecation")
    protected OkHttpClient createOkHttpClient(Settings settings){

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(settings.maxThreads);
        dispatcher.setMaxRequestsPerHost(settings.maxThreadsPerHost);

        ConnectionPool connectionPool = new ConnectionPool(settings.maxIdleConnections, 5, TimeUnit.MINUTES);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(settings.connectTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(settings.writeTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(settings.readTimeout, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool)
                .dispatcher(dispatcher);

        builder.addInterceptor(new Interceptor(){
            @Override
            public Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request();
                if (request.tag() instanceof Stub) {
                    Stub stub = (Stub) request.tag();
                    if (stub.connectTimeout > 0) {
                        chain = chain.withConnectTimeout(stub.connectTimeout, TimeUnit.MILLISECONDS);
                    }
                    if (stub.writeTimeout > 0) {
                        chain = chain.withWriteTimeout(stub.writeTimeout, TimeUnit.MILLISECONDS);
                    }
                    if (stub.readTimeout > 0) {
                        chain = chain.withReadTimeout(stub.readTimeout, TimeUnit.MILLISECONDS);
                    }
                }
                return chain.proceed(request);
            }
        });

        if (settings.cookieJar != null) {
            builder.cookieJar(settings.cookieJar);
        }
        if (settings.proxy != null) {
            builder.proxy(settings.proxy);
        }
        if (settings.dns != null) {
            builder.dns(settings.dns);
        }
        if (settings.sslSocketFactory != null) {
            if (settings.x509TrustManager != null) {
                // 最好两个都有, 不然OkHttp3会用反射的方式清理证书链
                builder.sslSocketFactory(settings.sslSocketFactory, settings.x509TrustManager);
            } else {
                builder.sslSocketFactory(settings.sslSocketFactory);
            }
        }
        if (settings.hostnameVerifier != null) {
            builder.hostnameVerifier(settings.hostnameVerifier);
        }

        return builder.build();
    }

    /**
     * 根据URL和报文体组POST请求(复写本方法实现自定义的逻辑)
     * @param url 由LoadBalancedHostManager选择出的远端url(前缀)
     * @param request 请求参数
     * @param settings 客户端配置
     * @return Request
     * @throws RequestBuildException 构建异常
     */
    protected okhttp3.Request buildPostRequest(String url, Request request, Settings settings) throws RequestBuildException{
        if (request.urlSuffix == null) {
            request.urlSuffix = "";
        }
        HttpUrl httpUrl = HttpUrl.parse(url + request.urlSuffix);
        if (httpUrl == null){
            throw new RequestBuildException("Invalid url:" + url + request.urlSuffix);
        }

        String encode = request.encode != null ? request.encode : settings.encode;

        if (request.urlParams != null){
            HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();
            for (Map.Entry<String, Object> param : request.urlParams.entrySet()) {
                try {
                    httpUrlBuilder.addEncodedQueryParameter(param.getKey(),
                            URLEncoder.encode(param.getValue() != null ? param.getValue().toString() : "", encode));
                } catch (UnsupportedEncodingException e) {
                    throw new RequestBuildException("Error while encode urlParams to url format", e);
                }
            }
            httpUrl = httpUrlBuilder.build();
        }

        RequestBody requestBody;
        if (request.body != null) {
            //bytes
            printPostStringBodyLog(request, null);
            requestBody = RequestBody.create(MediaType.parse(request.mediaType != null ? request.mediaType : settings.mediaType), request.body);
        } else if (request.formBody != null) {
            //form
            printPostStringBodyLog(request, null);
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Map.Entry<String, Object> param : request.formBody.entrySet()) {
                try {
                    formBuilder.addEncoded(param.getKey(),
                            URLEncoder.encode(param.getValue() != null ? param.getValue().toString() : "", encode));
                } catch (UnsupportedEncodingException e) {
                    throw new RequestBuildException("Error while encode formBody to url format", e);
                }
            }
            requestBody = formBuilder.build();
        } else if (request.beanBody != null) {
            //bean
            DataConverter dataConverter = request.dataConverter != null ? request.dataConverter : settings.dataConverter;
            if (dataConverter == null) {
                throw new RequestConvertException("No DataConverter set, you must set dataConverter before send/enqueue a beanBody");
            }
            byte[] requestBodyBytes;
            try {
                requestBodyBytes = dataConverter.convert(request.beanBody);
            } catch (Exception e) {
                throw new RequestConvertException("Error while convert bean to byte[]", e);
            }
            printPostStringBodyLog(request, requestBodyBytes);
            requestBody = RequestBody.create(MediaType.parse(request.mediaType != null ? request.mediaType : settings.mediaType), requestBodyBytes);
        } else if (request.customBody != null) {
            //custom
            printPostStringBodyLog(request, null);
            requestBody = request.customBody;
        }else {
            //null
            requestBody = RequestBody.create(MediaType.parse(request.mediaType != null ? request.mediaType : settings.mediaType), new byte[0]);
        }

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .tag(request.stub);

        Map<String, String> headers = settings.headers;
        if (headers != null){
            for (Map.Entry<String, String> entry : headers.entrySet()){
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (request.headers != null){
            for (Map.Entry<String, String> entry : request.headers.entrySet()){
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    /**
     * 根据URL和报文体组GET请求(复写本方法实现自定义的逻辑)
     * @param url 由LoadBalancedHostManager选择出的远端url(前缀)
     * @param request 请求参数
     * @param settings 客户端配置
     * @return Request
     * @throws RequestBuildException 构建异常
     */
    protected okhttp3.Request buildGetRequest(String url, Request request, Settings settings) throws RequestBuildException{
        if (request.urlSuffix == null) {
            request.urlSuffix = "";
        }
        HttpUrl httpUrl = HttpUrl.parse(url + request.urlSuffix);
        if (httpUrl == null){
            throw new RequestBuildException("Invalid url:" + url + request.urlSuffix);
        }

        String encode = request.encode != null ? request.encode : settings.encode;

        if (request.urlParams != null){
            HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();
            for (Map.Entry<String, Object> param : request.urlParams.entrySet()) {
                try {
                    httpUrlBuilder.addEncodedQueryParameter(param.getKey(),
                            URLEncoder.encode(param.getValue() != null ? param.getValue().toString() : "", encode));
                } catch (UnsupportedEncodingException e) {
                    throw new RequestBuildException("Error while encode to url format", e);
                }
            }
            httpUrl = httpUrlBuilder.build();
        }

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(httpUrl)
                .get()
                .tag(request.stub);

        Map<String, String> headers = settings.headers;
        if (headers != null){
            for (Map.Entry<String, String> entry : headers.entrySet()){
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (request.headers != null){
            for (Map.Entry<String, String> entry : request.headers.entrySet()){
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    /**
     * 判断该异常是否需要阻断后端, 返回true阻断
     */
    protected boolean needBlock(Throwable t, Settings settings) {
        return t instanceof ConnectException ||
                t instanceof SocketTimeoutException ||
                t instanceof UnknownHostException ||
                (t instanceof HttpRejectException && settings.httpCodeNeedBlock.contains(((HttpRejectException) t).getResponseCode()));
    }

    /**
     * 判断HTTP请求是否成功, 返回true成功
     */
    protected boolean isSucceed(Response response) {
        return response.isSuccessful();
    }

    public String getTag(){
        return settings.rawTag;
    }

    @Override
    public String toString() {
        return (hostManager != null ? hostManager.printHostsStatus("Hosts [") + " ] " : "") +
                "Settings [ " + settings + " ]";
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 配置 //////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 客户端配置
     */
    public static class Settings {

        private long passiveBlockDuration = PASSIVE_BLOCK_DURATION;
        private String mediaType = MEDIA_TYPE;
        private String encode = ENCODE;
        private Map<String, String> headers;
        private boolean verboseLog = false;
        private int verboseLogConfig = VERBOSE_LOG_CONFIG_DEFAULT;
        private int logConfig = LOG_CONFIG_DEFAULT;
        private int recoveryCoefficient = 10;

        private int maxIdleConnections = 16;
        private int maxThreads = 256;
        private int maxThreadsPerHost = 256;
        private long connectTimeout = 3000L;
        private long writeTimeout = 10000L;
        private long readTimeout = 10000L;
        private long maxReadLength = 10L * 1024L * 1024L;
        private CookieJar cookieJar;
        private Proxy proxy;
        private Dns dns;
        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager x509TrustManager;
        private HostnameVerifier hostnameVerifier;
        private DataConverter dataConverter;
        private String tag = LOG_PREFIX;
        private String rawTag = "";
        private Set<Integer> httpCodeNeedBlock = new HashSet<>(8);

        private boolean requestTraceEnabled = false;

        private Settings(){
        }

        @Override
        public String toString() {
            return "passiveBlockDuration=" + passiveBlockDuration +
                    ", recoveryCoefficient=" + recoveryCoefficient +
                    ", maxIdleConnections=" + maxIdleConnections +
                    ", maxThreads=" + maxThreads +
                    ", maxThreadsPerHost=" + maxThreadsPerHost +
                    ", connectTimeout=" + connectTimeout +
                    ", writeTimeout=" + writeTimeout +
                    ", readTimeout=" + readTimeout +
                    ", maxReadLength=" + maxReadLength +
                    ", headers=" + headers +
                    ", mediaType='" + mediaType + '\'' +
                    ", encode='" + encode + '\'' +
                    ", verboseLog=" + verboseLog +
                    ", verboseLogConfig=" + verboseLogConfig +
                    ", logConfig=" + logConfig +
                    ", cookieJar=" + cookieJar +
                    ", proxy=" + proxy +
                    ", dns=" + dns +
                    ", sslSocketFactory=" + sslSocketFactory +
                    ", x509TrustManager=" + x509TrustManager +
                    ", hostnameVerifier=" + hostnameVerifier +
                    ", dataConverter=" + dataConverter +
                    ", httpCodeNeedBlock=" + httpCodeNeedBlock;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 响应实例 //////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 响应包
     */
    public static class ResponsePackage implements Closeable {

        private int code;
        private String message;
        private boolean isRedirect;
        private ResponseBody body;
        private Headers headers;
        private String requestId;

        private static ResponsePackage newInstance(Request request, Response response) {
            if (response == null || response.body() == null) {
                return null;
            }
            return new ResponsePackage(request, response);
        }

        private ResponsePackage(Request request, Response response) {
            code = response.code();
            message = response.message();
            isRedirect = response.isRedirect();
            body = response.body();
            headers = response.headers();
            requestId = request.requestId == Integer.MAX_VALUE ? "" : String.valueOf(request.requestId);
        }

        public int code() {
            return code;
        }

        public String message() {
            return message;
        }

        public boolean isRedirect() {
            return isRedirect;
        }

        public ResponseBody body() {
            return body;
        }

        public Headers headers() {
            return headers;
        }

        public String requestId() {
            return requestId;
        }

        @Override
        public void close(){
            CloseableUtils.closeQuiet(body);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 异步方式的Callback /////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 请求回调(通用)
     */
    public static abstract class ResponsePackageCallback {

        /**
         * <p>请求成功</p>
         *
         * <p>注意: ResponsePackage实例是需要关闭的(close), 但我们提供autoClose配置, 详见Request#autoClose(boolean)</p>
         *
         * <p>
         * 当autoClose=true时, onSucceed方法回调结束后, ResponsePackage/InputStream会被自动关闭, 无需手动调用close方法. 适用于
         * 响应数据在回调方法中处理完的场合.<br>
         * 当autoClose=false时, onSucceed方法回调结束后, ResponsePackage/InputStream不会自动关闭, 需要手动调用ResponsePackage.close()关闭,
         * 注意!!! 适用于响应数据需要交由其他的线程处理, 或暂时持有的场合使用.
         * </p>
         *
         * @param responsePackage 响应包, 可能为null
         */
        protected abstract void onSucceed(ResponsePackage responsePackage) throws Exception;

        /**
         * 请求前发生异常
         * @param e {@link RequestBuildException}:请求前发生异常, {@link NoHostException}:未配置后端地址或所有后端地址均不可用
         */
        protected abstract void onErrorBeforeSend(Exception e);

        /**
         * 请求后发生异常
         * @param e {@link HttpRejectException}:后端Http拒绝(返回码不为200), {@link IOException}:通讯异常
         */
        protected abstract void onErrorAfterSend(Exception e);

        /**
         * 回调方法onSucceed执行时如果抛出异常, 会回调该方法处理异常, 默认转交onErrorAfterSend方法处理
         */
        protected void errorOnSucceedProcessing(Exception e){
            onErrorAfterSend(e);
        }

        void setContext(Settings settings, Request request) {
            //do nothing
        }

    }

    /**
     * 请求回调(获得byte[]响应体)
     */
    public static abstract class BytesCallback extends ResponsePackageCallback {

        private Settings settings;

        /**
         * <p>请求成功</p>
         *
         * @param body 响应, 可能为null
         */
        public abstract void onSucceed(byte[] body) throws Exception;

        @Override
        public final void onSucceed(ResponsePackage responsePackage) throws Exception {
            byte[] bytes = null;
            try {
                if (responsePackage != null && responsePackage.body() != null) {
                    //限定读取长度
                    if (settings.maxReadLength > 0 && responsePackage.body().contentLength() > settings.maxReadLength) {
                        throw new IOException("Response contentLength is out of limit, contentLength:" + responsePackage.body().contentLength() + ", limit:" + settings.maxReadLength);
                    }
                    //返回二进制数据
                    bytes = responsePackage.body().bytes();
                }
            } catch (IOException e) {
                onErrorAfterSend(e);
                return;
            } finally {
                //byte[]类型返回时, 强制关闭(无论autoClose是什么配置)
                if (responsePackage != null){
                    try {
                        responsePackage.close();
                    } catch (Throwable ignore) {
                    }
                }
            }
            onSucceed(bytes);
        }

        @Override
        void setContext(Settings settings, Request request) {
            this.settings = settings;
        }

    }

    /**
     * 请求回调(获得InputStream响应体)
     */
    public static abstract class InputStreamCallback extends ResponsePackageCallback {

        private Settings settings;

        /**
         * <p>请求成功</p>
         *
         * <p>注意: InputStream实例是需要关闭的(close), 但我们提供autoClose配置, 详见Request#autoClose(boolean)</p>
         *
         * <p>
         * 当autoClose=true时, onSucceed方法回调结束后, ResponsePackage/InputStream会被自动关闭, 无需手动调用close方法. 适用于
         * 响应数据在回调方法中处理完的场合.<br>
         * 当autoClose=false时, onSucceed方法回调结束后, ResponsePackage/InputStream不会自动关闭, 需要手动调用ResponsePackage.close()关闭,
         * 注意!!! 适用于响应数据需要交由其他的线程处理, 或暂时持有的场合使用.
         * </p>
         *
         * @param inputStream 响应, 可能为null
         */
        public abstract void onSucceed(InputStream inputStream) throws Exception;

        @Override
        public final void onSucceed(ResponsePackage responsePackage) throws Exception {
            //返回空
            if (responsePackage == null || responsePackage.body() == null) {
                onSucceed((InputStream) null);
                return;
            }
            //限定读取长度
            if (settings.maxReadLength > 0 && responsePackage.body().contentLength() > settings.maxReadLength) {
                //长度超过限制时, 强制关闭(无论autoClose是什么配置)
                try {
                    responsePackage.close();
                } catch (Throwable ignore) {
                }
                onErrorAfterSend(new IOException("Response contentLength is out of limit, contentLength:" + responsePackage.body().contentLength() + ", limit:" + settings.maxReadLength));
                return;
            }
            //返回二进制数据
            onSucceed(responsePackage.body().byteStream());
        }

        @Override
        void setContext(Settings settings, Request request) {
            this.settings = settings;
        }

    }

    /**
     * 请求回调(获得JavaBean响应体)
     */
    public static abstract class BeanCallback <T> extends BytesCallback {

        private Request request;
        private Settings settings;

        /**
         * <p>请求成功</p>
         *
         * <p>JavaBean的类型有BeanCallback的泛型决定</p>
         *
         * @param bean 响应, 可能为null
         */
        public abstract void onSucceed(T bean) throws Exception;

        @SuppressWarnings("unchecked")
        @Override
        public final void onSucceed(byte[] body) throws Exception {
            DataConverter dataConverter = request.dataConverter != null ? request.dataConverter : settings.dataConverter;
            if (dataConverter == null) {
                throw new ResponseConvertException("No DataConverter set, you must set dataConverter before enqueue a beanBody");
            }
            //当前类的父类(BeanCallback实现类的父类), 即MultiHostOkHttpClient$BeanCallback
            Type superType = getClass().getGenericSuperclass();
            if (!(superType instanceof ParameterizedType)) {
                //MultiHostOkHttpClient$BeanCallback有泛型, 因此这里的superType必然是ParameterizedType实例
                //P.S.泛型类的实现类getGenericSuperclass返回ParameterizedType实例
                //P.S.非泛型类的实现类getGenericSuperclass返回Class实例
                throw new IllegalStateException("FATAL: superType is not an instance of ParameterizedType!");
            }
            //获取第0个泛型类型, 即T的实际类型
            Type generic0Type = ((ParameterizedType)superType).getActualTypeArguments()[0];
            //P.S.在getActualTypeArguments返回的类型数组中, 泛型类是ParameterizedType实例, 非泛型类是Class实例
            if (generic0Type instanceof ParameterizedType) {
                //如果第0个泛型类型(T的实际类型)是泛型类, 则generic0Type是ParameterizedType实例
                //使用getRawType方法取原始类型用于类型转换
                //例如T为Map<String, Object>时, 只取Map类型
                onSucceed(dataConverter.convert(body, (Class<T>) ((ParameterizedType) generic0Type).getRawType()));
            } else {
                //如果第0个泛型类型(T的实际类型)不是泛型类, 则generic0Type是Class实例, 直接转为Class<T>即可
                //例如T类Map时, 直接类型转换为Class<Map>即可
                onSucceed(dataConverter.convert(body, (Class<T>) generic0Type));
            }
        }

        @Override
        void setContext(Settings settings, Request request) {
            super.setContext(settings, request);
            this.request = request;
            this.settings = settings;
        }

    }

    /**
     * 持有该对象可以发起请求取消操作(异步)
     */
    public static class Stub {

        private int connectTimeout = -1;
        private int writeTimeout = -1;
        private int readTimeout = -1;

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 客户端设置 ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 设置远端管理器(必须)
     * @param hostManager 远端管理器
     */
    public MultiHostOkHttpClient setHostManager(LoadBalancedHostManager hostManager) {
        this.hostManager = hostManager;
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
    public MultiHostOkHttpClient setPassiveBlockDuration(long passiveBlockDuration) {
        if (passiveBlockDuration < 0) {
            passiveBlockDuration = 0;
        }
        settings.passiveBlockDuration = passiveBlockDuration;
        return this;
    }

    /**
     * [可运行时修改]
     * 设置MediaType
     * @param mediaType 设置MediaType
     */
    public MultiHostOkHttpClient setMediaType(String mediaType) {
        settings.mediaType = mediaType;
        return this;
    }

    /**
     * [可运行时修改]
     * 设置编码
     * @param encode 编码
     */
    public MultiHostOkHttpClient setEncode(String encode) {
        settings.encode = encode;
        return this;
    }

    /**
     * [可运行时修改]
     * 设置HTTP请求头参数
     * @param headers 请求头参数
     */
    public MultiHostOkHttpClient setHeaders(Map<String, String> headers) {
        settings.headers = headers;
        return this;
    }

    /**
     * [可运行时修改]
     * 设置阻断后的恢复期系数, 修复期时长 = blockDuration * recoveryCoefficient, 设置1则无恢复期
     * @param recoveryCoefficient 阻断后的恢复期系数, >= 1
     */
    public MultiHostOkHttpClient setRecoveryCoefficient(int recoveryCoefficient) {
        if (recoveryCoefficient < 1) {
            recoveryCoefficient = 1;
        }
        settings.recoveryCoefficient = recoveryCoefficient;
        return this;
    }

    /**
     * [可运行时修改]
     * 最大闲置连接数. 客户端会保持与服务端的连接, 保持数量由此设置决定, 直到闲置超过5分钟. 默认16
     * @param maxIdleConnections 最大闲置连接数, 默认16
     */
    public MultiHostOkHttpClient setMaxIdleConnections(int maxIdleConnections) {
        if (maxIdleConnections < 0) {
            maxIdleConnections = 0;
        }
        try {
            settingsSpinLock.lock();
            settings.maxIdleConnections = maxIdleConnections;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 最大请求线程数(仅异步请求时有效)
     * @param maxThreads 最大请求线程数, 默认256
     */
    public MultiHostOkHttpClient setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            maxThreads = 1;
        }
        try {
            settingsSpinLock.lock();
            settings.maxThreads = maxThreads;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 对应每个后端的最大请求线程数(仅异步请求时有效)
     * @param maxThreadsPerHost 对应每个后端的最大请求线程数, 默认256
     */
    public MultiHostOkHttpClient setMaxThreadsPerHost(int maxThreadsPerHost) {
        if (maxThreadsPerHost < 1) {
            maxThreadsPerHost = 1;
        }
        try {
            settingsSpinLock.lock();
            settings.maxThreadsPerHost = maxThreadsPerHost;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置连接超时ms
     * @param connectTimeout 连接超时ms
     */
    public MultiHostOkHttpClient setConnectTimeout(long connectTimeout) {
        if (connectTimeout < 0) {
            connectTimeout = 0;
        }
        try {
            settingsSpinLock.lock();
            settings.connectTimeout = connectTimeout;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置写数据超时ms
     * @param writeTimeout 写数据超时ms
     */
    public MultiHostOkHttpClient setWriteTimeout(long writeTimeout) {
        if (writeTimeout < 0) {
            writeTimeout = 0;
        }
        try {
            settingsSpinLock.lock();
            settings.writeTimeout = writeTimeout;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置读数据超时ms
     * @param readTimeout 读数据超时ms
     */
    public MultiHostOkHttpClient setReadTimeout(long readTimeout) {
        if (readTimeout < 0) {
            readTimeout = 0;
        }
        try {
            settingsSpinLock.lock();
            settings.readTimeout = readTimeout;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 设置最大读取数据长度(默认:10M)
     * @param maxReadLength 设置最大读取数据长度, 单位bytes
     */
    public MultiHostOkHttpClient setMaxReadLength(long maxReadLength){
        if (maxReadLength < 1024) {
            maxReadLength = 1024;
        }
        settings.maxReadLength = maxReadLength;
        return this;
    }

    /**
     * [可运行时修改]
     * CookieJar
     * @param cookieJar CookieJar
     */
    public MultiHostOkHttpClient setCookieJar(CookieJar cookieJar) {
        try {
            settingsSpinLock.lock();
            settings.cookieJar = cookieJar;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * Proxy
     * @param proxy 例如127.0.0.1:8080
     * @throws IllegalArgumentException if the proxy string is invalid
     * @throws NumberFormatException  if the string does not contain a parsable integer.
     * @throws SecurityException if a security manager is present and permission to resolve the host name is denied.
     */
    public MultiHostOkHttpClient setProxy(String proxy) {
        Proxy proxyObj = null;
        if (proxy == null){
            throw new IllegalArgumentException("Invalid proxy string \"" + proxy + "\", correct \"X.X.X.X:XXX\", example \"127.0.0.1:8080\"");
        }
        int index = proxy.indexOf(":");
        if (index <= 0 || index >= proxy.length() - 1){
            throw new IllegalArgumentException("Invalid proxy string \"" + proxy + "\", correct \"X.X.X.X:XXX\", example \"127.0.0.1:8080\"");
        }
        try {
            proxyObj = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                    proxy.substring(0, index),
                    Integer.parseInt(proxy.substring(index + 1))));
        } catch (Throwable t){
            throw new IllegalArgumentException("Invalid proxy string \"" + proxy + "\", correct \"X.X.X.X:XXX\", example \"127.0.0.1:8080\"");
        }
        try {
            settingsSpinLock.lock();
            settings.proxy = proxyObj;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * [可运行时修改]
     * Dns
     * @param dns Dns
     */
    public MultiHostOkHttpClient setDns(Dns dns) {
        try {
            settingsSpinLock.lock();
            settings.dns = dns;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>SSLSocketFactory, 请配套X509TrustManager一起设置.</p>
     * <p></p>
     * <p>1.如果需要添加自定义的根证书, 用于验证自签名的服务器, 请调用{@link SslUtils#setCustomServerIssuers}
     * {@link SslUtils#setCustomServerIssuer} {@link SslUtils#setCustomServerIssuersEncoded}
     * {@link SslUtils#setCustomServerIssuerEncoded} 方法设置自定义的根证书</p>
     * <p></p>
     * <p>2.如果需要实现自定义证书验证逻辑, 请调用{@link SslUtils#setX509TrustManager}设置自己的X509TrustManager</p>
     *
     * @param sslSocketFactory SSLSocketFactory
     */
    public MultiHostOkHttpClient setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        try {
            settingsSpinLock.lock();
            settings.sslSocketFactory = sslSocketFactory;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>X509TrustManager, 请配套SSLSocketFactory一起设置</p>
     * <p></p>
     * <p>1.如果需要添加自定义的根证书, 用于验证自签名的服务器, 请调用{@link SslUtils#setCustomServerIssuers}
     * {@link SslUtils#setCustomServerIssuer} {@link SslUtils#setCustomServerIssuersEncoded}
     * {@link SslUtils#setCustomServerIssuerEncoded} 方法设置自定义的根证书</p>
     * <p></p>
     * <p>2.如果需要实现自定义证书验证逻辑, 请调用{@link SslUtils#setX509TrustManager}设置自己的X509TrustManager</p>
     *
     * @param x509TrustManager x509TrustManager
     */
    public MultiHostOkHttpClient setX509TrustManager(X509TrustManager x509TrustManager) {
        try {
            settingsSpinLock.lock();
            settings.x509TrustManager = x509TrustManager;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>给MultiHostOkHttpClient添加自定义的根证书, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的证书.</p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 SSLSocketFactory 和 X509TrustManager</p>
     * <p></p>
     * <p>如果需要实现自定义证书验证逻辑, 请调用{@link SslUtils#setX509TrustManager}设置自己的X509TrustManager</p>
     *
     * @param customIssuers 添加服务端的根证书为受信任的证书
     * @deprecated Use SslUtils instead
     */
    @Deprecated
    public MultiHostOkHttpClient setCustomServerIssuers(X509Certificate[] customIssuers) {
        try {
            SslUtils.setCustomServerIssuers(this, customIssuers);
        } catch (Throwable t) {
            throw new RuntimeException("Error while setting custom issuers", t);
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>给MultiHostOkHttpClient添加自定义的根证书, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的证书.</p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 SSLSocketFactory 和 X509TrustManager</p>
     * <p></p>
     * <p>如果需要实现自定义证书验证逻辑, 请调用{@link SslUtils#setX509TrustManager}设置自己的X509TrustManager</p>
     *
     * @param customIssuer 添加服务端的根证书为受信任的证书
     * @deprecated Use SslUtils instead
     */
    @Deprecated
    public MultiHostOkHttpClient setCustomServerIssuer(X509Certificate customIssuer) {
        try {
            SslUtils.setCustomServerIssuer(this, customIssuer);
        } catch (Throwable t) {
            throw new RuntimeException("Error while setting custom issuers", t);
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>给MultiHostOkHttpClient添加自定义的根证书, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的证书.</p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 SSLSocketFactory 和 X509TrustManager</p>
     * <p></p>
     * <p>如果需要实现自定义证书验证逻辑, 请调用{@link SslUtils#setX509TrustManager}设置自己的X509TrustManager</p>
     *
     * @param customIssuersEncoded 添加服务端的根证书为受信任的证书, X509 Base64 编码的证书
     * @deprecated Use SslUtils instead
     */
    @Deprecated
    public MultiHostOkHttpClient setCustomServerIssuersEncoded(String[] customIssuersEncoded) {
        try {
            SslUtils.setCustomServerIssuersEncoded(this, customIssuersEncoded);
        } catch (Throwable t) {
            throw new RuntimeException("Error while setting custom issuers", t);
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>给MultiHostOkHttpClient添加自定义的根证书, 用于验证自签名的服务器.</p>
     * <p>如果我们访问的服务端的证书是自己签发的, 根证书不合法, 可以用这个方法, 添加服务端的根证书为受信任的证书.</p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 SSLSocketFactory 和 X509TrustManager</p>
     * <p></p>
     * <p>如果需要实现自定义证书验证逻辑, 请调用{@link SslUtils#setX509TrustManager}设置自己的X509TrustManager</p>
     *
     * @param customIssuerEncoded 添加服务端的根证书为受信任的证书, X509 Base64 编码的证书. 如果设置为"UNSAFE-TRUST-ALL-ISSUERS",
     *                            则不校验服务端证书链, 信任一切服务端证书, 不安全!!!
     * @deprecated Use SslUtils instead
     */
    @Deprecated
    public MultiHostOkHttpClient setCustomServerIssuerEncoded(String customIssuerEncoded) {
        try {
            SslUtils.setCustomServerIssuerEncoded(this, customIssuerEncoded);
        } catch (Throwable t) {
            throw new RuntimeException("Error while setting custom issuers", t);
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>设置自定义的服务端域名验证逻辑</p>
     * <p></p>
     * <p>1.如果你通过一个代理访问服务端, 且访问代理的域名, 请调用{@link SslUtils#setVerifyServerCnByCustomHostname}
     * 设置服务端域名, 程序会改用指定的域名去匹配服务端证书的CN. 也可以调用{@link SslUtils#setVerifyServerDnByCustomDn}
     * 匹配证书的全部DN信息. </p>
     *
     * @param hostnameVerifier hostnameVerifier
     */
    public MultiHostOkHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        try {
            settingsSpinLock.lock();
            settings.hostnameVerifier = hostnameVerifier;
        } finally {
            settingsSpinLock.unlock();
        }
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>使用指定的域名验证服务端证书的CN. </p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 如果你通过一个代理访问服务端, 且访问代理的域名, 这样会导致
     * 域名验证失败, 因为"客户端访问的域名与服务端证书的CN不符", 这种情况可以调用这个方法设置服务端的域名, 程序会改用指定的域名去匹配
     * 服务端证书的CN. 除此之外, 你也可以利用这个方法强制验证证书CN, 即你只信任指定CN的证书. </p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 HostnameVerifier</p>
     *
     * @param customHostname 指定服务端域名 (如果设置为"UNSAFE-TRUST-ALL-CN"则不校验CN, 所有合法证书都通过, 不安全!!!), 示例: www.baidu.com
     * @deprecated Use SslUtils instead
     */
    @Deprecated
    public MultiHostOkHttpClient setVerifyServerCnByCustomHostname(String customHostname) {
        SslUtils.setVerifyServerCnByCustomHostname(this, customHostname);
        return this;
    }

    /**
     * <p>[可运行时修改]</p>
     * <p>使用指定的域名验证服务端证书的DN. </p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 你可以利用这个方法强制验证证书DN, 即你只信任指定DN的证书. </p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 HostnameVerifier</p>
     *
     * @param customDn 指定服务端证书DN (如果设置为"UNSAFE-TRUST-ALL-DN"则不校验DN, 所有合法证书都通过, 不安全!!!), DN示例:
     *                 CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\, Ltd,OU=service operation department,L=beijing,ST=beijing,C=CN
     * @deprecated Use SslUtils instead
     */
    @Deprecated
    public MultiHostOkHttpClient setVerifyServerDnByCustomDn(String customDn) {
        SslUtils.setVerifyServerDnByCustomDn(this, customDn);
        return this;
    }

    /**
     * [可运行时修改]
     * <p>[配置]数据转换器, 用于将beanBody设置的JavaBean转换为byte[], 和将返回报文byte[]转换为JavaBean</p>
     */
    public MultiHostOkHttpClient setDataConverter(DataConverter dataConverter) {
        settings.dataConverter = dataConverter;
        return this;
    }

    /**
     * [可运行时修改]
     * 当HTTP返回码为指定返回码时, 阻断后端
     * @param codes 指定需要阻断的返回码, 例如:403,404
     */
    public MultiHostOkHttpClient setHttpCodeNeedBlock(String codes) {
        if (CheckUtils.isEmptyOrBlank(codes)) {
            settings.httpCodeNeedBlock = new HashSet<>(8);
            return this;
        }
        try {
            String[] codeArray = codes.split(",");
            Set<Integer> newSet = new HashSet<>(8);
            for (String code : codeArray) {
                newSet.add(Integer.parseInt(code));
            }
            settings.httpCodeNeedBlock = newSet;
        } catch (Throwable t) {
            throw new RuntimeException("Invalid httpCodeNeedBlock " + codes, t);
        }
        return this;
    }

    /**
     * [可运行时修改]
     * 启用/禁用TxTimer统计请求耗时(暂时只支持同步方式), 默认禁用
     */
    public MultiHostOkHttpClient setTxTimerEnabled(boolean enabled){
        if (enabled && txTimer == null) {
            txTimer = NoRefTxTimerFactory.newInstance();
        } else if (!enabled){
            txTimer = null;
        }
        return this;
    }

    /**
     * 设置客户端的标识
     * @param tag 标识
     */
    public MultiHostOkHttpClient setTag(String tag) {
        settings.tag = tag != null ? LOG_PREFIX + tag + "> " : LOG_PREFIX;
        settings.rawTag = tag;
        return this;
    }

    /**
     * [可运行时修改]
     * true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false
     * @param verboseLog true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false
     */
    public MultiHostOkHttpClient setVerboseLog(boolean verboseLog) {
        settings.verboseLog = verboseLog;
        return this;
    }

    /**
     * [可运行时修改]
     * 打印更多的日志, 细粒度配置, 默认全打印<br>
     *
     * VERBOSE_LOG_CONFIG_ALL:{@value VERBOSE_LOG_CONFIG_ALL}<br>
     * VERBOSE_LOG_CONFIG_REQUEST_INPUTS:{@value VERBOSE_LOG_CONFIG_REQUEST_INPUTS}<br>
     * VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY:{@value VERBOSE_LOG_CONFIG_REQUEST_STRING_BODY}<br>
     * VERBOSE_LOG_CONFIG_RAW_URL:{@value VERBOSE_LOG_CONFIG_RAW_URL}<br>
     * VERBOSE_LOG_CONFIG_RESPONSE_CODE:{@value VERBOSE_LOG_CONFIG_RESPONSE_CODE}<br>
     *
     * @param verboseLogConfig 详细配置
     */
    public MultiHostOkHttpClient setVerboseLogConfig(int verboseLogConfig) {
        settings.verboseLogConfig = verboseLogConfig;
        return this;
    }

    /**
     * [可运行时修改]
     * 日志打印细粒度配置, 默认全打印<br>
     *
     * LOG_CONFIG_ALL:{@value LOG_CONFIG_ALL}<br>
     * LOG_CONFIG_REAL_URL:{@value LOG_CONFIG_REAL_URL}<br>
     * LOG_CONFIG_BLOCK:{@value LOG_CONFIG_BLOCK}<br>
     *
     * @param logConfig 详细配置
     */
    public MultiHostOkHttpClient setLogConfig(int logConfig) {
        settings.logConfig = logConfig;
        return this;
    }

    /**
     * [可运行时修改]
     * true: 开启简易的请求日志追踪(请求日志追加4位数追踪号), 默认false<br>
     *
     * @param requestTraceEnabled true: 开启简易的请求日志追踪(请求日志追加4位数追踪号), 默认false
     */
    public MultiHostOkHttpClient setRequestTraceEnabled(boolean requestTraceEnabled) {
        settings.requestTraceEnabled = requestTraceEnabled;
        return this;
    }

    private class SettingsSpinLock {

        private void lock(){
            while (true) {
                if (!settingsLock.get() && settingsLock.compareAndSet(false, true)) {
                    break;
                } else {
                    Thread.yield();
                }
            }
        }

        private void unlock(){
            //标记为需要更新
            refreshSettings = true;
            //清除异常
            clientCreateException = null;
            //解锁
            settingsLock.set(false);
        }

    }

}
