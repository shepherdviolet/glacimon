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
 * 提供验证所需的证书颁发者. 可选实现, 中间CA允许客户端上送(issuerProviderParameter), 但根证书必须由服务端限定.
 *
 * @author S.Violet
 */
public class RootIssuerProvider implements IssuerProvider<List<? extends X509Certificate>> {

    private final Map<String, X509Certificate> issuers;

    /**
     * @param rootIssuers 根证书(由服务端限定)
     */
    public RootIssuerProvider(List<? extends X509Certificate> rootIssuers) {
        if (rootIssuers == null) {
            this.issuers = Collections.emptyMap();
            return;
        }
        this.issuers = new HashMap<>(rootIssuers.size() << 1);
        for (X509Certificate issuer : rootIssuers) {
            if (issuer == null) {
                continue;
            }
            this.issuers.put(issuer.getSubjectX500Principal().getName(), issuer);
        }
    }

    /**
     * @param dn DN信息
     * @param caIssuers 客户端上送的中间CA证书, 不允许存在根证书
     */
    @Override
    public X509Certificate findIssuer(String dn, List<? extends X509Certificate> caIssuers) throws CertificateException {
        // 服务端限定的证书优先
        X509Certificate issuer = issuers.get(dn);
        if (issuer != null) {
            return issuer;
        }
        // 从客户端上送的中间CA证书中查找
        if (caIssuers == null) {
            return null;
        }
        for (X509Certificate caIssuer : caIssuers) {
            String issuerDn = caIssuer.getSubjectX500Principal().getName();
            if (issuerDn.equals(dn)) {
                // 不允许客户端上送根证书
                if (issuerDn.equals(caIssuer.getIssuerX500Principal().getName())) {
                    throw new CertificateException("The client is not allowed to send the root certificate to the server, the illegal ca certificate:" + caIssuer);
                }
                return caIssuer;
            }
        }
        //找不到
        return null;
    }

}
