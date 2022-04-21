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
 * <p>[SSL配置提供者] 直接设置自定义的SslSocketFactory. (如需设置自定义的TrustManager请用KeyAndTrustManagerSupplier). </p>
 *
 * <p>建议sslSocketFactory和trustManager一起设置, 如果只设置sslSocketFactory的话, OKHTTP会用反射的方式清理证书链.</p>
 *
 * <p>用途: </p>
 * <p>1.SSLSocketFactory设置服务端证书的受信颁发者 </p>
 * <p>2.SSLSocketFactory设置客户端证书(双向SSL) </p>
 * <p>3. ... </p>
 *
 * @author shepherdviolet
 */
public class SslSocketFactorySupplier implements SslConfigSupplier {

    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;

    public SslSocketFactorySupplier() {
    }

    /**
     * @param sslSocketFactory SSLSocketFactory 1.设置服务端证书的受信颁发者 2.设置客户端证书(双向SSL) 3. ...
     */
    public SslSocketFactorySupplier(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * @param sslSocketFactory SSLSocketFactory 1.设置服务端证书的受信颁发者 2.设置客户端证书(双向SSL) 3. ...
     * @param trustManager 非必须, 最好有这个, 不然OkHttp3会用反射的方式清理证书链
     */
    public SslSocketFactorySupplier(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
    }

    /**
     * @param sslSocketFactory SSLSocketFactory 1.设置服务端证书的受信颁发者 2.设置客户端证书(双向SSL) 3. ...
     */
    public SslSocketFactorySupplier setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    /**
     * @param trustManager 非必须, 最好有这个, 不然OkHttp3会用反射的方式清理证书链
     */
    public SslSocketFactorySupplier setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    @Override
    public SslConfig getSslConfig() {
        if (sslSocketFactory == null) {
            // no ssl config
            return null;
        }
        return new SslConfig(sslSocketFactory, trustManager);
    }

    @Override
    public String toString() {
        return "SslSocketFactorySupplier{" +
                "sslSocketFactory=" + sslSocketFactory +
                ", trustManager=" + trustManager +
                '}';
    }

}
