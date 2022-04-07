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

import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.crypto.base.SimpleIssuerProvider;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class SM2CertTest {

    @Test
    public void common() throws OperatorCreationException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, PKCSException, NoSuchProviderException, UnrecoverableKeyException, KeyStoreException, SignatureException, InvalidKeyException {
        //生成根证书的随机密钥对
        SM2KeyGenerator.SM2KeyParamsPair rootKeyPair = SM2KeyGenerator.generateKeyParamsPair();

        //生成根证书
        X509Certificate rootCert = AdvancedCertificateUtils.generateSm2X509RootCertificate(
                "CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN",
                3650,
                rootKeyPair.getPublicKeyParams(),
                rootKeyPair.getPrivateKeyParams());

        //简单验证证书
        AdvancedCertificateUtils.verifyCertificate(rootCert, rootKeyPair.getPublicKeyParams());

//        System.out.println(PEMEncodeUtils.certificateToPEMEncoded(AdvancedCertificateUtils.parseCertificateToEncoded(rootCert)));

        //生成用户证书的随机密钥对
        SM2KeyGenerator.SM2KeyParamsPair userKeyPair = SM2KeyGenerator.generateKeyParamsPair();

        //产生用户证书申请文件CSR
        byte[] csr = AdvancedCertificateUtils.generateSm2Csr(
                "CN=Test User, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN",
                userKeyPair.getPublicKeyParams(),
                userKeyPair.getPrivateKeyParams());

        //生成用户证书
        X509Certificate userCert = AdvancedCertificateUtils.generateSm2X509Certificate(
                csr,
                3650,
                rootCert,
                rootKeyPair.getPrivateKeyParams(),
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment),
                new GeneralName[]{
                        new GeneralName(GeneralName.dNSName, "1.host.com"),
                        new GeneralName(GeneralName.dNSName, "2.host.com")
                });

        //简单验证证书
        AdvancedCertificateUtils.verifyCertificate(userCert, rootKeyPair.getPublicKeyParams());

        //证书转为X509标准格式数据
        byte[] certX509 = AdvancedCertificateUtils.parseCertificateToEncoded(userCert);

        //证书转为PEM格式的文本(cer/crt)
        String certPEM = PEMEncodeUtils.certificateToPEMEncoded(certX509);

//        System.out.println(certPEM);
//        System.out.println(Base64Utils.encodeToString(userKeyPair.getPKCS8EncodedPrivateKey()));

        //从X509编码的证书数据中解析证书实例
        X509Certificate userCert2 = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(certX509);
        AdvancedCertificateUtils.verifyCertificate(userCert2, rootKeyPair.getPublicKeyParams());

        //不常用:组装证书链对象
        List<X509Certificate> certificateList = new ArrayList<>(2);
        certificateList.add(userCert);
        certificateList.add(rootCert);
        CertPath certPath = AdvancedCertificateUtils.generateX509CertPath(certificateList);

        //不常用:证书链PKCS7格式
        byte[] certPKCS7 = AdvancedCertificateUtils.parseCertPathToPKCS7Encoded(certPath);

//        System.out.println(Base64Utils.encodeToString(certPKCS7));

        //不常用:解析PKCS7的数据到证书链
        certPath = AdvancedCertificateUtils.parseX509PKCS7CertPath(certPKCS7);

        //将证书/证书链写入pfx/p12文件
        AdvancedPKCS12KeyStoreUtils.storeCertificateAndKeyAdvanced(
                "./out/test-case/sm2-test-all.p12",
                "123456",
                "test",
                userKeyPair.getJdkPrivateKey(),
                userCert,
                rootCert);

        //获取pfx/p12中的别名列表
        Enumeration<String> aliases = AdvancedPKCS12KeyStoreUtils.loadAliasesAdvanced(
                "./out/test-case/sm2-test-all.p12",
                "123456"
        );

//        if (aliases.hasMoreElements()) {
//            System.out.println(aliases.nextElement());
//        }

        //从pfx/p12中读取证书和私钥
        PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey = AdvancedPKCS12KeyStoreUtils.loadCertificateAndKeyAdvanced(
                "./out/test-case/sm2-test-all.p12",
                "123456",
                "test"
        );

        Assert.assertArrayEquals(new Certificate[]{userCert, rootCert}, certificateChainAndKey.getCertificateChain());
        Assert.assertEquals(userKeyPair.getJdkPrivateKey(), certificateChainAndKey.getPrivateKey());

        //证书链验证证书有效性
        AdvancedCertificateUtils.verifyCertificateByIssuers(userCert, new Date(), new SimpleIssuerProvider(Collections.singletonList(rootCert)));

    }

}
