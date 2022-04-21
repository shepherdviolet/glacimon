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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.ssl;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * <p>SSL配置. 内含sslSocketFactory和trustManager. </p>
 *
 * <p>建议sslSocketFactory和trustManager一起设置, 如果只设置sslSocketFactory的话, OKHTTP会用反射的方式清理证书链.</p>
 *
 * <p>用途: </p>
 * <p>1.设置服务端证书的受信颁发者 </p>
 * <p>2.设置客户端证书(双向SSL) </p>
 * <p>3. ... </p>
 *
 * @author shepherdviolet
 */
public class SslConfig {

    private final SSLSocketFactory sslSocketFactory;
    private final X509TrustManager trustManager;

    public SslConfig(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
    }

    /**
     * 获取SSLSocketFactory, 若返回空表示本配置无效 (不设置SSL).
     */
    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * 获取SSLSocketFactory对应的TrustManager, 非必须.
     * 最好有这个, 不然OkHttp3会用反射的方式清理证书链.
     */
    public X509TrustManager getTrustManager() {
        return trustManager;
    }

}
