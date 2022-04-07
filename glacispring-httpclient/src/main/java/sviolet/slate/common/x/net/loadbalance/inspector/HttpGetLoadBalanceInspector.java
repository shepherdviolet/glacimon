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

package sviolet.slate.common.x.net.loadbalance.inspector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.common.CloseableUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 负载均衡--HTTP GET方式探测网络状况
 *
 * @author S.Violet
 */
public class HttpGetLoadBalanceInspector implements FixedTimeoutLoadBalanceInspector, Closeable {

    private static final int HTTP_SUCCESS = 200;
    private static final long DEFAULT_TIMEOUT = 2000L;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private volatile OkHttpClient client;
    private String urlSuffix;

    private volatile boolean closed = false;

    public HttpGetLoadBalanceInspector() {
        this("", DEFAULT_TIMEOUT);
    }

    /**
     * @param urlSuffix 探测URL的后缀
     * @param timeout 探测超时时间
     */
    public HttpGetLoadBalanceInspector(String urlSuffix, long timeout) {
        this.urlSuffix = urlSuffix;
        setTimeout(timeout);
    }

    @Override
    public boolean inspect(String url, long timeout, boolean verboseLog) {
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
            response = client.newCall(request).execute();
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
    public void close() throws IOException {
        closed = true;
        closeClient(client);
    }

    private void closeClient(OkHttpClient client) {
        try {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            client.cache().close();
        } catch (Exception ignore) {
        }
    }

    /**
     * [可运行时修改(不建议频繁修改)]
     * 设置单次探测网络超时时间(必须), 建议为LoadBalancedInspectManager.setInspectInterval设置值的1/2
     */
    @Override
    public void setTimeout(long timeout){
        //除以2, 因为网络超时包括连接/写入/读取超时, 避免过长
        timeout = timeout >> 1;
        if (timeout <= 0){
            throw new IllegalArgumentException("timeout must > 1 (usually > 1000)");
        }
        OkHttpClient previous = client;
        client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
        closeClient(previous);
    }

    /**
     * [可运行时修改]
     * 设置探测页面的后缀URL
     * @param urlSuffix 探测页面后缀URL
     */
    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

    @Override
    public String toString() {
        return "HttpGetLoadBalanceInspector{" +
                "urlSuffix=" + urlSuffix + '}';
    }

}
