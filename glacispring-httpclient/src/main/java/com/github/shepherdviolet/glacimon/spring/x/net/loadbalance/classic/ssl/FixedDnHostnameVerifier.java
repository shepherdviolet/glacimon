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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * <p>使用指定的域名验证服务端证书的DN. </p>
 * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 你可以利用这个方法强制验证证书DN, 即你只信任指定DN的证书. </p>
 *
 * @author shepherdviolet
 */
public class FixedDnHostnameVerifier implements HostnameVerifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String customDn;

    /**
     * <p>使用指定的域名验证服务端证书的DN. </p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 你可以利用这个方法强制验证证书DN, 即你只信任指定DN的证书. </p>
     *
     * @param customDn 指定服务端证书DN (如果设置为"UNSAFE-TRUST-ALL-DN"则不校验DN, 所有合法证书都通过, 不安全!!!), DN示例:
     *                 CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\, Ltd,OU=service operation department,L=beijing,ST=beijing,C=CN
     */
    public FixedDnHostnameVerifier(String customDn) {
        this.customDn = customDn;
    }

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
        return "FixedDnHostnameVerifier{" +
                "customDn='" + customDn + '\'' +
                '}';
    }
}
