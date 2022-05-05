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

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.conversion.ByteUtils;
import com.github.shepherdviolet.glacimon.java.crypto.CertificateUtils;
import com.github.shepherdviolet.glacimon.java.crypto.DigestCipher;
import com.github.shepherdviolet.glacimon.java.crypto.RSAKeyGenerator;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import com.github.shepherdviolet.glacimon.java.net.CustomIssuersTrustManager;
import com.github.shepherdviolet.glacimon.java.net.SslUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>[SSL配置提供者] 设置客户端证书和服务端证书的受信颁发者.</p>
 *
 * <p>用途: </p>
 * <p>1.设置服务端证书的受信颁发者 </p>
 * <p>2.设置客户端证书(双向SSL) </p>
 * <p>3. ... </p>
 *
 * <p>待解决: SSL双向认证似乎有点问题, 后端如果是Springboot应用, 配置了客户端证书验证, 测试通过没问题; 后端如果是nginx, 配置了客户端证书验证,
 * 就会报错400, 原因不明...</p>
 *
 * <p>待开发: 目前只支持设置证书和私钥, 将来可以支持设置JKS/PFX文件 (keyStore.load(...)加载一下文件即可)</p>
 *
 * @author shepherdviolet
 */
public class CertAndTrustedIssuerSupplier implements SslConfigSupplier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String customServerIssuerEncoded;
    private String[] customServerIssuersEncoded;
    private X509Certificate customServerIssuer;
    private X509Certificate[] customServerIssuers;

    private String customClientCertEncoded;
    private String[] customClientCertsEncoded;
    private X509Certificate customClientCert;
    private X509Certificate[] customClientCerts;

    private String customClientCertKeyEncoded;
    private Key customClientCertKey;

    /**
     * 设置服务端证书的受信颁发者
     * setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置自定义的颁发者.
     * @param customServerIssuerEncoded 服务端证书的受信颁发者
     */
    public CertAndTrustedIssuerSupplier setCustomServerIssuerEncoded(String customServerIssuerEncoded) {
        this.customServerIssuerEncoded = customServerIssuerEncoded;
        return this;
    }

    /**
     * 设置服务端证书的受信颁发者
     * setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级2 (第二).
     * 如果一个都不设置, 表示不配置自定义的颁发者.
     * @param customServerIssuersEncoded 服务端证书的受信颁发者
     */
    public CertAndTrustedIssuerSupplier setCustomServerIssuersEncoded(String[] customServerIssuersEncoded) {
        this.customServerIssuersEncoded = customServerIssuersEncoded;
        return this;
    }

    /**
     * 设置服务端证书的受信颁发者
     * setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级3 (第三).
     * 如果一个都不设置, 表示不配置自定义的颁发者.
     * @param customServerIssuer 服务端证书的受信颁发者
     */
    public CertAndTrustedIssuerSupplier setCustomServerIssuer(X509Certificate customServerIssuer) {
        this.customServerIssuer = customServerIssuer;
        return this;
    }

    /**
     * 设置服务端证书的受信颁发者
     * setCustomServerIssuer...系列参数只需要设置一个, 同时设置时, 本参数优先级4 (最低).
     * 如果一个都不设置, 表示不配置自定义的颁发者.
     * @param customServerIssuers 服务端证书的受信颁发者
     */
    public CertAndTrustedIssuerSupplier setCustomServerIssuers(X509Certificate[] customServerIssuers) {
        this.customServerIssuers = customServerIssuers;
        return this;
    }

    /**
     * [双向SSL]设置客户端证书
     * setCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL).
     * 如果设置了客户端证书, 必须设置客户端私钥.
     *
     * @param customClientCertEncoded 客户端证书, X509 PEM BASE64
     */
    public CertAndTrustedIssuerSupplier setCustomClientCertEncoded(String customClientCertEncoded) {
        this.customClientCertEncoded = customClientCertEncoded;
        return this;
    }

    /**
     * [双向SSL]设置客户端证书链
     * setCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级2 (第二).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL).
     * 如果设置了客户端证书, 必须设置客户端私钥.
     *
     * @param customClientCertsEncoded 客户端证书链, X509 PEM BASE64
     */
    public CertAndTrustedIssuerSupplier setCustomClientCertsEncoded(String[] customClientCertsEncoded) {
        this.customClientCertsEncoded = customClientCertsEncoded;
        return this;
    }

    /**
     * [双向SSL]设置客户端证书
     * setCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级3 (第三).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL).
     * 如果设置了客户端证书, 必须设置客户端私钥.
     *
     * @param customClientCert 客户端证书
     */
    public CertAndTrustedIssuerSupplier setCustomClientCert(X509Certificate customClientCert) {
        this.customClientCert = customClientCert;
        return this;
    }

    /**
     * [双向SSL]设置客户端证书链
     * setCustomClientCert...系列参数只需要设置一个, 同时设置时, 本参数优先级4 (最低).
     * 如果一个都不设置, 表示不配置客户端证书(关闭双向SSL).
     * 如果设置了客户端证书, 必须设置客户端私钥.
     *
     * @param customClientCerts 客户端证书链
     */
    public CertAndTrustedIssuerSupplier setCustomClientCerts(X509Certificate[] customClientCerts) {
        this.customClientCerts = customClientCerts;
        return this;
    }

    /**
     * [双向SSL]设置客户端证书对应的私钥
     * setCustomClientCertKey...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置客户端证书私钥(关闭双向SSL).
     * 如果设置了客户端私钥, 必须设置客户端证书.
     *
     * @param customClientCertKeyEncoded 客户端证书, PKCS8 BASE64
     */
    public CertAndTrustedIssuerSupplier setCustomClientCertKeyEncoded(String customClientCertKeyEncoded) {
        this.customClientCertKeyEncoded = customClientCertKeyEncoded;
        return this;
    }

    /**
     * [双向SSL]设置客户端证书对应的私钥
     * setCustomClientCertKey...系列参数只需要设置一个, 同时设置时, 本参数优先级1 (最高).
     * 如果一个都不设置, 表示不配置客户端证书私钥(关闭双向SSL).
     * 如果设置了客户端私钥, 必须设置客户端证书.
     *
     * @param customClientCertKey 客户端证书
     */
    public CertAndTrustedIssuerSupplier setCustomClientCertKey(Key customClientCertKey) {
        this.customClientCertKey = customClientCertKey;
        return this;
    }

    @Override
    public SslConfig getSslConfig() {
        X509TrustManager trustManager = buildTrustManager();
        X509KeyManager keyManager = buildKeyManager();

        if (trustManager == null && keyManager == null) {
            // no ssl config
            return null;
        }

        return createSslConfig(keyManager, trustManager);
    }

    /**
     * 创建TrustManager
     *
     * 四种参数按照优先级的顺序判断, 只取其中一种参数(优先级最高的)创建TrustManager
     */
    public X509TrustManager buildTrustManager() {

        List<X509Certificate> customIssuers = new ArrayList<>();

        if (!CheckUtils.isEmptyOrBlank(customServerIssuerEncoded)) {

            // 不安全!!! 信任一切服务端证书!!!
            if ("UNSAFE-TRUST-ALL-ISSUERS".equals(customServerIssuerEncoded)) {
                return buildUnsafeTrustManager();
            }

            try {
                customIssuers.add(CertificateUtils.parseX509ToCertificate(Base64Utils.decode(customServerIssuerEncoded)));
            } catch (Throwable t) {
                throw new RuntimeException("CertAndTrustedIssuerSupplier | Error while parsing custom issuer certificate from X509 encoded: " + customServerIssuerEncoded, t);
            }

        } else if (customServerIssuersEncoded != null && customServerIssuersEncoded.length > 0) {

            for (String issuerEncoded : customServerIssuersEncoded) {
                try {
                    customIssuers.add(CertificateUtils.parseX509ToCertificate(Base64Utils.decode(issuerEncoded)));
                } catch (Throwable t) {
                    throw new RuntimeException("CertAndTrustedIssuerSupplier | Error while parsing custom issuer certificate from X509 encoded: " + issuerEncoded, t);
                }
            }

        } else if (customServerIssuer != null) {

            customIssuers.add(customServerIssuer);

        } else if (customServerIssuers != null && customServerIssuers.length > 0) {

            customIssuers.addAll(Arrays.asList(customServerIssuers));

        } else {

            // no trust manager config
            return null;

        }

        X509TrustManager trustManager;
        try {
            trustManager = CustomIssuersTrustManager.newInstance(customIssuers.toArray(new X509Certificate[0]));
        } catch (Throwable t) {
            throw new RuntimeException("CertAndTrustedIssuerSupplier | Failed to create CustomIssuersX509TrustManager instance for custom issuers", t);
        }

        return trustManager;
    }

    /**
     * 创建不安全的TrustManager, 信任所有服务端证书
     */
    private X509TrustManager buildUnsafeTrustManager() {
        logger.warn("HttpClient | Trust all issuers!!! UNSAFE !!!");
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // UNSAFE!!! Trust all issuers !!!
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // UNSAFE!!! Trust all issuers !!!
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private X509KeyManager buildKeyManager(){

        Key customKey = null;
        List<X509Certificate> customCerts = new ArrayList<>();

        if (!CheckUtils.isEmptyOrBlank(customClientCertKeyEncoded)) {

            // Only RSA is supported currently
            try {
                customKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(customClientCertKeyEncoded));
            } catch (Throwable t) {
                throw new RuntimeException("CertAndTrustedIssuerSupplier | Error while parsing custom client certificate private key from PKCS8 encoded: " + customClientCertKeyEncoded, t);
            }

        } else if (customClientCertKey != null) {

            customKey = customClientCertKey;

        }

        if (!CheckUtils.isEmptyOrBlank(customClientCertEncoded)) {

            try {
                customCerts.add(CertificateUtils.parseX509ToCertificate(Base64Utils.decode(customClientCertEncoded)));
            } catch (Throwable t) {
                throw new RuntimeException("CertAndTrustedIssuerSupplier | Error while parsing custom client certificate from X509 encoded: " + customClientCertEncoded, t);
            }

        } else if (customClientCertsEncoded != null && customClientCertsEncoded.length > 0) {

            for (String certEncoded : customClientCertsEncoded) {
                try {
                    customCerts.add(CertificateUtils.parseX509ToCertificate(Base64Utils.decode(certEncoded)));
                } catch (Throwable t) {
                    throw new RuntimeException("CertAndTrustedIssuerSupplier | Error while parsing custom client certificate from X509 encoded: " + certEncoded, t);
                }
            }

        } else if (customClientCert != null) {

            customCerts.add(customClientCert);

        } else if (customClientCerts != null && customClientCerts.length > 0) {

            customCerts.addAll(Arrays.asList(customClientCerts));

        }

        if (customKey == null && customCerts.size() <= 0) {
            return null;
        } else if (customKey == null) {
            throw new RuntimeException("CertAndTrustedIssuerSupplier | SSL mutual authentication must configure both client certificate and private key, private key is missing");
        } else if (customCerts.size() <= 0) {
            throw new RuntimeException("CertAndTrustedIssuerSupplier | SSL mutual authentication must configure both client certificate and private key, client certificate is missing");

        }

        // user cert and key to KeyStore
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("0", customKey, new char[0], customCerts.toArray(new X509Certificate[0]));
        } catch (Throwable t) {
            throw new RuntimeException("CertAndTrustedIssuerSupplier | Failed to convert custom client certificate chain and key to KeyStore", t);
        }

        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, new char[0]);
            return (X509KeyManager) keyManagerFactory.getKeyManagers()[0];
        } catch (Throwable t) {
            throw new RuntimeException("CertAndTrustedIssuerSupplier | Failed to create X509KeyManager instance for custom client certificate chain and key", t);
        }
    }

    private SslConfig createSslConfig(X509KeyManager keyManager, X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SslUtils.sslContext();
            sslContext.init(keyManager != null ? new KeyManager[]{keyManager} : null,
                    trustManager != null ? new TrustManager[]{trustManager} : null, null);
            return new SslConfig(sslContext.getSocketFactory(), trustManager);
        } catch (Throwable t) {
            throw new RuntimeException("CertAndTrustedIssuerSupplier | Failed to initialize SSLSocketFactory by keyManager and trustManager", t);
        }
    }

    @Override
    public String toString() {
        return "CertAndTrustedIssuerSupplier{" +
                "customServerIssuerEncoded='" + customServerIssuerEncoded + '\'' +
                ", customServerIssuersEncoded=" + Arrays.toString(customServerIssuersEncoded) +
                ", customServerIssuer=" + customServerIssuer +
                ", customServerIssuers=" + Arrays.toString(customServerIssuers) +
                ", customClientCertEncoded='" + customClientCertEncoded + '\'' +
                ", customClientCertsEncoded=" + Arrays.toString(customClientCertsEncoded) +
                ", customClientCert=" + customClientCert +
                ", customClientCerts=" + Arrays.toString(customClientCerts) +
                ", customClientCertKeyEncoded(SHA256)='" + desensitization(customClientCertKeyEncoded) + '\'' +
                ", customClientCertKey(SHA256)='" + desensitization(customClientCertKey) + '\'' +
                '}';
    }

    private String desensitization(String string){
        return string != null ? ByteUtils.bytesToHex(DigestCipher.digest(string.getBytes(StandardCharsets.UTF_8), DigestCipher.TYPE_SHA256)) : "null";
    }

    private String desensitization(Key key){
        return key != null ? ByteUtils.bytesToHex(DigestCipher.digest(key.getEncoded(), DigestCipher.TYPE_SHA256)) : "null";
    }

}
