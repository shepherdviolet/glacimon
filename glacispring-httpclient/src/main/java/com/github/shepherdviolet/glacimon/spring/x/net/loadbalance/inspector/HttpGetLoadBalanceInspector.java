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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector;

import com.github.shepherdviolet.glacimon.java.misc.CloseableUtils;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalanceInspector;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.ssl.SslConfig;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 负载均衡--HTTP GET方式探测网络状况
 *
 * @author shepherdviolet
 */
public class HttpGetLoadBalanceInspector implements LoadBalanceInspector {

    private static final int HTTP_SUCCESS = 200;
    private static final long DEFAULT_TIMEOUT = 2500L;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final GlaciHttpClient.Settings settings;
    private volatile long timeout = DEFAULT_TIMEOUT;
    private volatile String urlSuffix = "";

    private volatile OkHttpClient okHttpClient;
    private volatile boolean refreshSettings = false;
    private volatile Exception clientCreateException;
    private volatile boolean closed = false;

    public HttpGetLoadBalanceInspector(GlaciHttpClient.Settings settings) {
        this.settings = settings;
    }

    @Override
    public boolean inspect(String url) {
        if (closed) {
            //被销毁的探测器始终返回探测成功
            return true;
        }
        //组装request
        Request request;
        Response response = null;
        try {
            request = new Request.Builder()
                    .url(url + urlSuffix)
                    .get()
                    .build();
        } catch (Throwable t) {
            if (logger.isErrorEnabled()){
                logger.error("Inspect: invalid url " + url + urlSuffix, t);
            }
            //探测的URL异常视为后端异常
            return false;
        }
        //GET请求
        try {
            response = getOkHttpClient().newCall(request).execute();
            if (response.code() == HTTP_SUCCESS){
                return true;
            }
        } catch (Throwable t) {
            if (logger.isTraceEnabled()){
                logger.trace("Inspect: error, url " + url + urlSuffix, t);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Inspect: error, url " + url + urlSuffix + ", error message:" + t.getMessage() + ", set level to trace for more");
            }
        } finally {
            CloseableUtils.closeQuiet(response);
        }
        if (closed) {
            //被销毁的探测器始终返回探测成功
            return true;
        }
        return false;
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        closeClient(okHttpClient);
    }

    /**
     * [可运行时修改(不建议频繁修改)]
     * 设置单次探测网络超时时间(必须), 建议为LoadBalancedInspectManager.setInspectInterval设置值的1/2
     */
    @Override
    public void setTimeout(long timeout){
        if (closed) {
            return;
        }
        //除以2, 因为网络超时包括连接/写入/读取超时, 避免过长
        timeout = timeout >> 1;
        if (timeout <= 0){
            throw new IllegalArgumentException("timeout must > 1 (usually > 1000)");
        }

        synchronized (this) {
            this.timeout = timeout;
            //标记为需要更新
            refreshSettings = true;
            //清除异常
            clientCreateException = null;
        }
    }

    @Override
    public void refreshSettings() {
        if (closed) {
            return;
        }
        synchronized (this) {
            //标记为需要更新
            refreshSettings = true;
            //清除异常
            clientCreateException = null;
        }
    }

    /**
     * [可运行时修改]
     * 设置探测页面的后缀URL
     * @param urlSuffix 探测页面后缀URL
     */
    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

    private OkHttpClient getOkHttpClient(){
        //客户端创建错误后, 不再重试
        if (clientCreateException != null) {
            throw new IllegalStateException("Client create error", clientCreateException);
        }
        //一次检查
        if (okHttpClient == null || refreshSettings) {
            //自旋锁
            OkHttpClient previous = null;
            synchronized (this) {
                try {
                    //二次检查e
                    if (okHttpClient == null || refreshSettings) {
                        previous = okHttpClient;
                        okHttpClient = createOkHttpClient();
                        refreshSettings = false;
                    }
                } catch (Exception e) {
                    clientCreateException = e;
                    throw e;
                }
            }
            closeClient(previous);
        }
        return okHttpClient;
    }

    /**
     * 初始化OkHttpClient实例(复写本方法实现自定义的逻辑)
     * @return OkHttpClient实例
     */
    @SuppressWarnings("deprecation")
    protected OkHttpClient createOkHttpClient(){

        if (settings == null) {
            ConnectionPool connectionPool = new ConnectionPool(0, 5, TimeUnit.MINUTES);
            return new OkHttpClient.Builder()
                    .connectTimeout(timeout / 2, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout / 2, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .connectionPool(connectionPool)
                    .build();
        }

        ConnectionPool connectionPool = new ConnectionPool(settings.getMaxIdleConnections(), 5, TimeUnit.MINUTES);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeout / 2, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout / 2, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool);

        if (settings.getCookieJar() != null) {
            builder.cookieJar(settings.getCookieJar());
        }
        if (settings.getProxy() != null) {
            builder.proxy(settings.getProxy());
        }
        if (settings.getDns() != null) {
            builder.dns(settings.getDns());
        }

        if (settings.getSslConfigSupplier() != null){
            SslConfig sslConfig = settings.getSslConfigSupplier().getSslConfig();
            if (sslConfig != null && sslConfig.getSslSocketFactory() != null) {
                if (sslConfig.getTrustManager() != null) {
                    // 最好两个都有, 不然OkHttp3会用反射的方式清理证书链
                    builder.sslSocketFactory(sslConfig.getSslSocketFactory(), sslConfig.getTrustManager());
                } else {
                    builder.sslSocketFactory(sslConfig.getSslSocketFactory());
                }
            }
        }

        if (settings.getHostnameVerifier() != null) {
            builder.hostnameVerifier(settings.getHostnameVerifier());
        }

        return builder.build();
    }

    private void closeClient(OkHttpClient client) {
        if (client == null) {
            return;
        }
        try {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            client.cache().close();
        } catch (Exception ignore) {
        }
    }

    @Override
    public String toString() {
        return "HttpGetLoadBalanceInspector{" +
                "urlSuffix=" + urlSuffix + '}';
    }

}
