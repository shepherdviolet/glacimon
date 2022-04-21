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

import okhttp3.internal.platform.Platform;

import javax.net.ssl.*;

/**
 * <p>[SSL配置提供者] 设置自定义的trustManager和keyManager.</p>
 *
 * <p>用途: </p>
 * <p>1.trustManager设置服务端证书的受信颁发者 </p>
 * <p>2.keyManager设置客户端证书(双向SSL) </p>
 * <p>3. ... </p>
 *
 * @author shepherdviolet
 */
public class KeyAndTrustManagerSupplier implements SslConfigSupplier {

    private X509KeyManager keyManager;
    private X509TrustManager trustManager;

    public KeyAndTrustManagerSupplier() {
    }

    /**
     * @param keyManager 客户端证书/私钥管理
     * @param trustManager 服务端证书信任管理
     */
    public KeyAndTrustManagerSupplier(X509KeyManager keyManager, X509TrustManager trustManager) {
        this.keyManager = keyManager;
        this.trustManager = trustManager;
    }

    public KeyAndTrustManagerSupplier setKeyManager(X509KeyManager keyManager) {
        this.keyManager = keyManager;
        return this;
    }

    public KeyAndTrustManagerSupplier setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    @Override
    public SslConfig getSslConfig() {
        if (keyManager == null && trustManager == null) {
            // no ssl config
            return null;
        }
        try {
            SSLContext sslContext = Platform.get().getSSLContext();
            sslContext.init(keyManager != null ? new KeyManager[]{keyManager} : null,
                    trustManager != null ? new TrustManager[]{trustManager} : null,
                    null);
            return new SslConfig(sslContext.getSocketFactory(), trustManager);
        } catch (Throwable t) {
            throw new RuntimeException("KeyAndTrustManagerSupplier | Failed to initialize SSLSocketFactory by keyManager and trustManager", t);
        }
    }

    @Override
    public String toString() {
        return "KeyAndTrustManagerSupplier{" +
                "keyManager=" + keyManager +
                ", trustManager=" + trustManager +
                '}';
    }
}
