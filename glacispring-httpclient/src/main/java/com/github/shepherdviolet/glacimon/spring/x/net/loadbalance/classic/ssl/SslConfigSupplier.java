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

/**
 * <p>SSL配置提供者. 向HttpClient提供SSLSocketFactory. </p>
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
public interface SslConfigSupplier {

    /**
     * 获取SSLSocketFactory及对应的TrustManager.
     * 若SslConfig返回空表示本配置无效 (不设置SSL).
     * 若SSLSocketFactory返回空表示本配置无效 (不设置SSL).
     * 若TrustManager返回空, OkHttp3会用反射的方式清理证书链, 最好有TrustManager.
     */
    SslConfig getSslConfig();

}
