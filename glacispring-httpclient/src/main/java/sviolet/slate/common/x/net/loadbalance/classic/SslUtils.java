/*
 * Copyright (C) 2015-2020 S.Violet
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

import okhttp3.internal.Util;
import okhttp3.internal.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.crypto.CertificateUtils;
import sviolet.thistle.util.judge.CheckUtils;
import sviolet.thistle.util.net.SimpleHostnameVerifier;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * MultiHostOkHttpClient专用工具: 自定义SSL方式
 *
 * @author S.Violet
 */
public class SslUtils {

    private static final Logger logger = LoggerFactory.getLogger(SslUtils.class);

    /**
     * <p>[可运行时修改]</p>
     * <p>给MultiHostOkHttpClient设置自定义的X509TrustManager, 用于实现自定义的SSL验证逻辑</p>
     * <p></p>
     * <p>注意, 调用这个方法会覆盖 SSLSocketFactory 和 X509TrustManager</p>
     *
     * @param multiHostOkHttpClient multiHostOkHttpClient
     * @param trustManager trustManager
     */
    public static void setX509TrustManager(MultiHostOkHttpClient multiHostOkHttpClient, X509TrustManager trustManager) {
        if (multiHostOkHttpClient == null) {
            return;
        }
        if (trustManager == null) {
            multiHostOkHttpClient.setSSLSocketFactory(null);
            multiHostOkHttpClient.setX509TrustManager(null);
            return;
        }
        try {
            SSLContext sslContext = Platform.get().getSSLContext();
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            multiHostOkHttpClient.setSSLSocketFactory(sslContext.getSocketFactory());
            multiHostOkHttpClient.setX509TrustManager(trustManager);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Error while initializing SSLSocketFactory and setting X509TrustManager to MultiHostOkHttpClient", e);
        }
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
     * @param multiHostOkHttpClient multiHostOkHttpClient
     * @param customIssuers 添加服务端的根证书为受信任的证书
     */
    public static void setCustomServerIssuers(MultiHostOkHttpClient multiHostOkHttpClient, X509Certificate[] customIssuers) throws CertificateException {
        setX509TrustManager(multiHostOkHttpClient, customIssuers != null ? CustomIssuersX509TrustManager.newInstance(customIssuers) : null);
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
     */
    public static void setCustomServerIssuer(MultiHostOkHttpClient multiHostOkHttpClient, X509Certificate customIssuer) throws CertificateException {
        setCustomServerIssuers(multiHostOkHttpClient, customIssuer != null ? new X509Certificate[]{customIssuer} : null);
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
     */
    public static void setCustomServerIssuersEncoded(MultiHostOkHttpClient multiHostOkHttpClient, String[] customIssuersEncoded) throws CertificateException {
        if (customIssuersEncoded == null || customIssuersEncoded.length <= 0) {
            setCustomServerIssuers(multiHostOkHttpClient, null);
            return;
        }
        X509Certificate[] customIssuers = new X509Certificate[customIssuersEncoded.length];
        for (int i = 0 ; i < customIssuers.length ; i++) {
            try {
                customIssuers[i] = CertificateUtils.parseX509ToCertificate(Base64Utils.decode(customIssuersEncoded[i]));
            } catch (Throwable t) {
                throw new RuntimeException("Error while parsing custom issuer certificate from X509 encoded: " + customIssuersEncoded[i], t);
            }
        }
        setCustomServerIssuers(multiHostOkHttpClient, customIssuers);
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
     */
    public static void setCustomServerIssuerEncoded(MultiHostOkHttpClient multiHostOkHttpClient, String customIssuerEncoded) throws CertificateException {
        if ("UNSAFE-TRUST-ALL-ISSUERS".equals(customIssuerEncoded)) {
            logger.warn("MultiHostOkHttpClient trust all issuers! UNSAFE !!!");
            setX509TrustManager(multiHostOkHttpClient, new X509TrustManager() {
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
            });
            return;
        }
        setCustomServerIssuersEncoded(multiHostOkHttpClient, !CheckUtils.isEmptyOrBlank(customIssuerEncoded) ? new String[]{customIssuerEncoded} : null);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     */
    public static void setVerifyServerCnByCustomHostname(MultiHostOkHttpClient multiHostOkHttpClient, String customHostname) {
        if (CheckUtils.isEmptyOrBlank(customHostname)) {
            multiHostOkHttpClient.setHostnameVerifier(null);
            return;
        }
        multiHostOkHttpClient.setHostnameVerifier(new SimpleHostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                boolean match = super.verify(hostname, session);
                if (!match) {
                    logger.error("The certificate CN of the server does not match the specified hostname '" + customHostname + "'");
                }
                return match;
            }
            @Override
            protected boolean isHostnameMatch(String hostname, String cn) {
                // 全部通过, 不校验CN
                if ("UNSAFE-TRUST-ALL-CN".equals(customHostname)) {
                    return true;
                }
                // 拿指定域名和服务端证书的CN匹配
                return super.isHostnameMatch(customHostname, cn);
            }
            @Override
            public String toString() {
                return "MultiHostOkHttpClient$SimpleHostnameVerifier{" +
                        "customHostname='" + customHostname + '\'' +
                        '}';
            }
        });
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
     */
    public static void setVerifyServerDnByCustomDn(MultiHostOkHttpClient multiHostOkHttpClient, String customDn) {
        if (CheckUtils.isEmptyOrBlank(customDn)) {
            multiHostOkHttpClient.setHostnameVerifier(null);
            return;
        }
        multiHostOkHttpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                // 全部通过, 不校验DN
                if ("UNSAFE-TRUST-ALL-DN".equals(customDn)) {
                    return true;
                }
                try {
                    Certificate[] certificates = session.getPeerCertificates();
                    if (certificates == null || certificates.length <= 0) {
                        logger.error("Server certificate not received, can not verify it's DN");
                        return false;
                    }
                    //第一个证书是站点证书
                    X509Certificate x509Certificate = (X509Certificate) certificates[0];
                    String dn = x509Certificate.getSubjectX500Principal().getName();
                    boolean match = customDn.equals(dn);
                    if (!match) {
                        logger.error("The certificate's DN '" + dn + "' of the server does not match the specified DN '" + customDn + "'");
                    }
                    return match;
                } catch (Throwable t) {
                    logger.error("Error while verifying server certificate's DN", t);
                    return false;
                }
            }
            @Override
            public String toString() {
                return "MultiHostOkHttpClient$HostnameVerifier{" +
                        "customDn='" + customDn + '\'' +
                        '}';
            }
        });
    }


    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 能够添加自定义根证书的X509TrustManager
     */
    public static class CustomIssuersX509TrustManager implements X509TrustManager {

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

            return new CustomIssuersX509TrustManager(keyStore);
        }

        /**
         * 能够添加自定义根证书的X509TrustManager
         *
         * @param customKeyStore 自定义根证书的KeyStore
         */
        public static X509TrustManager newInstance(KeyStore customKeyStore) throws CertificateException {
            return new CustomIssuersX509TrustManager(customKeyStore);
        }

        private CustomIssuersX509TrustManager(KeyStore customKeyStore) throws CertificateException {
            if (customKeyStore == null) {
                throw new IllegalArgumentException("customKeyStore is null");
            }

            // Get system TrustManager
            systemTrustManager = Util.platformTrustManager();

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
            for (int i = 0 ; i < customIssuers.length ; i++) {
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


}
