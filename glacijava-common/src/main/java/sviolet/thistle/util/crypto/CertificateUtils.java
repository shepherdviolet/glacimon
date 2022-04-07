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

package sviolet.thistle.util.crypto;

import sviolet.thistle.util.crypto.base.BaseCertificateUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.*;

/**
 * <p>证书工具</p>
 *
 * <p>更多功能见thistle-crypto-plus的AdvancedCertificateUtils</p>
 *
 * @author S.Violet
 */
public class CertificateUtils {

    /* **********************************************************************************************
     * certificate decode / encode
     ********************************************************************************************** */

    /**
     * <p>解析X509格式的证书, 返回Certificate对象, 可用来获取证书公钥实例等, JDK版本较弱.
     * 解析SM2等证书请使用thistle-crypto-plus的AdvancedCertificateUtils. </p>
     * @param certData X509格式证书数据
     */
    public static X509Certificate parseX509ToCertificate(byte[] certData) throws CertificateException {
        if (certData == null) {
            throw new NullPointerException("certData == null");
        }
        return (X509Certificate) BaseCertificateUtils.parseCertificate(new ByteArrayInputStream(certData), BaseCertificateUtils.TYPE_X509);
    }

    /**
     * <p>解析X509格式的证书, 返回Certificate对象, 可用来获取证书公钥实例等, JDK版本较弱.
     * 解析SM2等证书请使用thistle-crypto-plus的AdvancedCertificateUtils. </p>
     * @param inputStream X509格式证书数据流, 会被close掉
     */
    public static X509Certificate parseX509ToCertificate(InputStream inputStream) throws CertificateException {
        return (X509Certificate) BaseCertificateUtils.parseCertificate(inputStream, BaseCertificateUtils.TYPE_X509);
    }

    /**
     * 将证书编码为二进制数据, 适用于目前所有证书类型
     * @param certificate 证书
     * @return 二进制数据
     */
    public static byte[] parseCertificateToEncoded(Certificate certificate) throws CertificateEncodingException {
        return BaseCertificateUtils.encodeCertificate(certificate);
    }

    /**
     * 将证书链编码为PKCS7数据
     * @param certPath 证书链
     * @return 证书链的PKCS7数据
     */
    public static byte[] parseCertPathToPKCS7Encoded(CertPath certPath) throws CertificateEncodingException {
        return BaseCertificateUtils.encodeCertPath(certPath, "PKCS7");
    }

}
