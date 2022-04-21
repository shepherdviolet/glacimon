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

import com.github.shepherdviolet.glacimon.java.net.SimpleHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSession;

/**
 * <p>使用指定的域名验证服务端证书的CN. </p>
 * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 如果你通过一个代理访问服务端, 且访问代理的域名, 这样会导致
 * 域名验证失败, 因为"客户端访问的域名与服务端证书的CN不符", 这种情况可以调用这个方法设置服务端的域名, 程序会改用指定的域名去匹配
 * 服务端证书的CN. 除此之外, 你也可以利用这个方法强制验证证书CN, 即你只信任指定CN的证书. </p>
 *
 * @author shepherdviolet
 */
public class FixedCnHostnameVerifier extends SimpleHostnameVerifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String customHostname;

    /**
     * <p>使用指定的域名验证服务端证书的CN. </p>
     * <p>默认情况下, HTTP客户端会验证访问的域名和服务端证书的CN是否匹配. 如果你通过一个代理访问服务端, 且访问代理的域名, 这样会导致
     * 域名验证失败, 因为"客户端访问的域名与服务端证书的CN不符", 这种情况可以调用这个方法设置服务端的域名, 程序会改用指定的域名去匹配
     * 服务端证书的CN. 除此之外, 你也可以利用这个方法强制验证证书CN, 即你只信任指定CN的证书. </p>
     *
     * @param customHostname 指定服务端域名 (如果设置为"UNSAFE-TRUST-ALL-CN"则不校验CN, 所有合法证书都通过, 不安全!!!), 示例: www.baidu.com
     */
    public FixedCnHostnameVerifier(String customHostname) {
        this.customHostname = customHostname;
    }

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
        return "FixedCnHostnameVerifier{" +
                "customHostname='" + customHostname + '\'' +
                '}';
    }

}
