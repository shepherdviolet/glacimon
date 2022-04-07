/*
 * Copyright (C) 2015-2019 S.Violet
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

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SignDataUtilsTest {

    @Test
    public void test() throws CMSException, CertificateException, NoSuchProviderException, IOException, OperatorCreationException {

        //签名者公私钥
        RSAKeyGenerator.RSAKeyPair keyPair = RSAKeyGenerator.generateKeyPair(2048);

        //签名者证书(这里简化为根证书)
        X509Certificate certificate = AdvancedCertificateUtils.generateRSAX509RootCertificate(
                "CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN",
                keyPair.getPublicKey(),
                keyPair.getPrivateKey(),
                3650,
                AdvancedCertificateUtils.SIGN_ALGORITHM_RSA_SHA256
        );

        //签名并产生PKCS7格式的签名数据
        byte[] signedData = AdvancedSignDataUtils.generateRsaPkcs7SignData(
                "content".getBytes(),
                new X509Certificate[]{certificate},
                certificate,
                keyPair.getPrivateKey(),
                "SHA256WITHRSA",
                "SHA256",
                true
        );

        //解析PKCS7格式的签名数据
        AdvancedSignDataUtils.JdkSignedData data = AdvancedSignDataUtils.parsePkcs7ToJdkSignedData(
                signedData
        );

//        System.out.println(data);
    }

}
