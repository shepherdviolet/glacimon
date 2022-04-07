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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.net;

import sviolet.thistle.util.judge.CheckUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

/**
 * 简易的HostnameVerifier
 *
 * 1.实现hostname与CN的匹配
 * 2.实现hostname与subjectAlternativeNames的匹配
 * 3.支持通配符域名(*)
 *
 * @author S.Violet
 */
public class SimpleHostnameVerifier implements HostnameVerifier {

    private static final Integer DNS_NAME = 2;

    /**
     * @param hostname 实际访问的域名
     * @param session SSL会话, 可以从中获取证书
     */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        try {
            if (CheckUtils.isEmptyOrBlank(hostname)) {
                return false;
            }

            Certificate[] certificates = session.getPeerCertificates();
            if (certificates == null || certificates.length <= 0) {
                return false;
            }

            //第一个证书是站点证书
            X509Certificate x509Certificate = (X509Certificate) certificates[0];
            String dn = x509Certificate.getSubjectX500Principal().getName();
            String cn = getCn(dn);

            //验证CN与域名是否相符
            if (isHostnameMatch(hostname, cn)) {
                return true;
            }

            //获取subjectAlternativeNames
            Collection<List<?>> subjectAlternativeNames = x509Certificate.getSubjectAlternativeNames();
            if (subjectAlternativeNames == null) {
                return false;
            }

            //遍历subjectAlternativeNames
            for (List<?> subjectAlternativeName : subjectAlternativeNames) {
                //正常的格式类似于[2, *.test.com], 2表示该项是DNS Name, 后面是域名
                if (subjectAlternativeName == null ||
                        subjectAlternativeName.size() != 2 ||
                        !DNS_NAME.equals(subjectAlternativeName.get(0))) {
                    continue;
                }
                if (isHostnameMatch(hostname, String.valueOf(subjectAlternativeName.get(1)))) {
                    return true;
                }
            }

        } catch (Throwable ignored) {
        }

        return false;
    }

    protected String getCn(String dn) {
        if (CheckUtils.isEmptyOrBlank(dn)) {
            return null;
        }

        int cnStart = dn.indexOf("CN=");
        if (cnStart < 0) {
            cnStart = dn.indexOf("cn=");
        }
        if (cnStart < 0) {
            return null;
        }

        int cnEnd = dn.indexOf(',', cnStart);
        if (cnEnd < 0) {
            cnEnd = dn.length();
        }

        return dn.substring(cnStart + 3, cnEnd).trim();
    }

    /**
     * @param hostname 实际访问的域名
     * @param cn 证书的CN或者subjectAlternativeNames
     * @return true: 实际访问的域名与证书声明的域名相符, false: 不符, 会继续匹配其他的subjectAlternativeNames
     */
    protected boolean isHostnameMatch(String hostname, String cn) {
        if (CheckUtils.isEmptyOrBlank(cn)) {
            return false;
        }
        if (cn.charAt(0) == '*') {
            cn = cn.substring(1);
            return hostname.endsWith(cn);
        } else {
            return hostname.equals(cn);
        }
    }

}