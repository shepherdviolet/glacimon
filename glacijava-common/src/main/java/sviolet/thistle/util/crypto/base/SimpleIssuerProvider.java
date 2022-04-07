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

package sviolet.thistle.util.crypto.base;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供验证所需的证书颁发者. 简单实现, 从指定的集合中查找证书颁发者(包括根证书和中间CA)
 *
 * @author S.Violet
 */
public class SimpleIssuerProvider implements IssuerProvider<Object> {

    private final Map<String, X509Certificate> issuers;

    /**
     * @param issuers 固定的颁发者集合
     */
    public SimpleIssuerProvider(List<? extends X509Certificate> issuers) {
        if (issuers == null) {
            this.issuers = Collections.emptyMap();
            return;
        }
        this.issuers = new HashMap<>(issuers.size() << 1);
        for (X509Certificate issuer : issuers) {
            if (issuer == null) {
                continue;
            }
            this.issuers.put(issuer.getSubjectX500Principal().getName(), issuer);
        }
    }

    /**
     * @param dn DN信息
     * @param useless 这里不用
     */
    @Override
    public X509Certificate findIssuer(String dn, Object useless) throws CertificateException {
        return issuers.get(dn);
    }

}
