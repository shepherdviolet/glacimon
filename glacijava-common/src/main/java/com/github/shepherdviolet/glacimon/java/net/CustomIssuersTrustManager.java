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

package com.github.shepherdviolet.glacimon.java.net;

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.crypto.CertificateUtils;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * 能够添加自定义根证书的X509TrustManager
 *
 * @author shepherdviolet
 */
public class CustomIssuersTrustManager implements X509TrustManager {

    private final X509TrustManager systemTrustManager;
    private final X509TrustManager customTrustManager;
    private final X509Certificate[] acceptedIssuers;
    private final String[] customIssuersEncoded;

    /**
     * 能够添加自定义根证书的X509TrustManager
     *
     * @param customIssuers 自定义根证书
     */
    public static X509TrustManager newInstance(X509Certificate[] customIssuers) throws CertificateException {
        if (customIssuers == null) {
            throw new IllegalArgumentException("customIssuers is null");
        }

        // X509Certificates to KeyStore
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            int i = 0;
            for (Certificate certificate : customIssuers) {
                keyStore.setCertificateEntry(String.valueOf(i++), certificate);
            }
        } catch (Exception e) {
            throw new CertificateException("Error while converting X509Certificates to KeyStore", e);
        }

        return new CustomIssuersTrustManager(keyStore);
    }

    /**
     * 能够添加自定义根证书的X509TrustManager
     *
     * @param customKeyStore 自定义根证书的KeyStore
     */
    public static X509TrustManager newInstance(KeyStore customKeyStore) throws CertificateException {
        return new CustomIssuersTrustManager(customKeyStore);
    }

    private CustomIssuersTrustManager(KeyStore customKeyStore) throws CertificateException {
        if (customKeyStore == null) {
            throw new IllegalArgumentException("customKeyStore is null");
        }

        // Get system TrustManager
        systemTrustManager = SslUtils.platformTrustManager();

        // Build TrustManager by KeyStore
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(customKeyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            customTrustManager = (X509TrustManager) trustManagers[0];
        } catch (Exception e) {
            throw new CertificateException("Error while building TrustManager by X509Certificates (KeyStore)", e);
        }

        X509Certificate[] systemIssuers = systemTrustManager.getAcceptedIssuers();
        X509Certificate[] customIssuers = customTrustManager.getAcceptedIssuers();
        acceptedIssuers = new X509Certificate[systemIssuers.length + customIssuers.length];
        System.arraycopy(customIssuers, 0, acceptedIssuers, 0, customIssuers.length);
        System.arraycopy(systemIssuers, 0, acceptedIssuers, customIssuers.length, systemIssuers.length);

        // For log
        customIssuersEncoded = new String[customIssuers.length];
        for (int i = 0; i < customIssuers.length; i++) {
            customIssuersEncoded[i] = Base64Utils.encodeToString(CertificateUtils.parseCertificateToEncoded(customIssuers[i]));
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // Custom first
        CertificateException throwable;
        try {
            customTrustManager.checkClientTrusted(chain, authType);
            // Verified
            return;
        } catch (CertificateException t) {
            throwable = t;
        }
        // System
        try {
            systemTrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException t) {
            throwable.addSuppressed(t);
            throw throwable;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // Custom first
        CertificateException throwable;
        try {
            customTrustManager.checkServerTrusted(chain, authType);
            // Verified
            return;
        } catch (CertificateException t) {
            throwable = t;
        }
        // System
        try {
            systemTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException t) {
            throwable.addSuppressed(t);
            throw throwable;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }

    @Override
    public String toString() {
        return "CustomIssuersX509TrustManager{" +
                Arrays.toString(customIssuersEncoded) +
                '}';
    }

}