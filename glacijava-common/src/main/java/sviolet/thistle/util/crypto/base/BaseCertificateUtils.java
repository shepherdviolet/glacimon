/*
 * Copyright (C) 2015-2018 S.Violet
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

import java.io.InputStream;
import java.security.cert.*;

/**
 * 证书处理基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * @author S.Violet
 */
public class BaseCertificateUtils {

    public static final String TYPE_X509 = "X.509";

    /* **********************************************************************************************
     * certificate decode / encode
     ********************************************************************************************** */

    /**
     * <p>解析证书, 返回Certificate对象, 可用来获取证书公钥实例等, JDK版本较弱. 解析SM2等证书请使用BaseBCCertificateUtils. </p>
     * @param inputStream 证书数据流, 会被close掉
     * @param type 证书数据格式, 例如X.509
     * @return 如果type是X.509, 可以强制类型转换为X509Certificate
     */
    public static Certificate parseCertificate(InputStream inputStream, String type) throws CertificateException {
        try {
            CertificateFactory factory = CertificateFactory.getInstance(type);
            return factory.generateCertificate(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 将证书编码为二进制数据, 适用于目前所有证书类型
     * @param certificate 证书
     * @return 二进制数据
     */
    public static byte[] encodeCertificate(Certificate certificate) throws CertificateEncodingException {
        if (certificate == null){
            return null;
        }
        return certificate.getEncoded();
    }

    /**
     * 将证书链编码为二进制数据
     * @param certPath 证书链
     * @param encoding 编码, PKCS7
     * @return 证书链的数据
     */
    public static byte[] encodeCertPath(CertPath certPath, String encoding) throws CertificateEncodingException {
        if (certPath == null) {
            return null;
        }
        return certPath.getEncoded(encoding);
    }

}
