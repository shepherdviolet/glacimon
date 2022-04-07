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

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.Selector;
import sviolet.thistle.util.crypto.base.BaseBCCertificateUtils;
import sviolet.thistle.util.crypto.base.BaseCertificateUtils;
import sviolet.thistle.util.crypto.base.BouncyCastleProviderUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Collection;

/**
 * 签名数据工具
 *
 * @author S.Violet
 */
public class AdvancedSignDataUtils {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /**
     * 解析PKCS7格式的签名数据为JDK格式的实例, 该方法用于处理最简单的情况: 只有一个签名者, 且证书符合X509标准
     *
     * @param pkcs7SignData PKCS7格式的签名数据
     */
    public static JdkSignedData parsePkcs7ToJdkSignedData(byte[] pkcs7SignData) throws CMSException, IOException, CertificateException, NoSuchProviderException {
        CMSSignedData signedData = new CMSSignedData(pkcs7SignData);
        JdkSignedData result = new JdkSignedData();
        //只取第一个签名值
        if (signedData.getSignerInfos() != null) {
            for (SignerInformation signerInformation : signedData.getSignerInfos().getSigners()) {
                result.signature = signerInformation.getSignature();
                break;
            }
        }
        //取所有证书
        if (signedData.getCertificates() != null) {
            Collection<X509CertificateHolder> certificateHolders = signedData.getCertificates().getMatches(SELECT_ALL_CERT);
            if (certificateHolders != null) {
                result.certificates = new X509Certificate[certificateHolders.size()];
                int i = 0;
                for (X509CertificateHolder certificateHolder : certificateHolders) {
                    result.certificates[i++] = (X509Certificate) BaseBCCertificateUtils.parseCertificateByBouncyCastle(
                            new ByteArrayInputStream(certificateHolder.getEncoded()),
                            BaseCertificateUtils.TYPE_X509
                    );
                }
            }
        }
        //签名前数据
        if (signedData.getSignedContent() != null) {
            if (signedData.getSignedContent().getContent() instanceof byte[]) {
                result.content = (byte[]) signedData.getSignedContent().getContent();
            }
        }
        return result;
    }

    private static final Selector<X509CertificateHolder> SELECT_ALL_CERT = new Selector<X509CertificateHolder>() {
        @Override
        public boolean match(X509CertificateHolder holder) {
            return true;
        }

        @Override
        public Object clone() {
            return SELECT_ALL_CERT;
        }
    };

    /**
     * [RSA]生成PKCS7格式的签名数据(输入JDK格式的实例)
     * @param content            签名前数据(原文)
     * @param certificates       证书(可选)
     * @param signerCertificate  签名者证书
     * @param signerPrivateKey   签名者私钥
     * @param signatureAlgorithm 签名算法, 例如: SHA256WITHRSA
     * @param digestAlgorithm    摘要算法, 例如: SHA256
     * @param containsContent    true: 签名数据中包含签名前数据(原文)
     * @return PKCS7格式的签名数据
     */
    public static byte[] generateRsaPkcs7SignData(
            byte[] content,
            X509Certificate[] certificates,
            X509Certificate signerCertificate,
            RSAPrivateKey signerPrivateKey,
            String signatureAlgorithm,
            String digestAlgorithm,
            boolean containsContent) throws OperatorCreationException, CertificateEncodingException, IOException, CMSException {

        CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();

        //添加证书
        if (certificates != null) {
            for (X509Certificate certificate : certificates) {
                cmsSignedDataGenerator.addCertificate(new X509CertificateHolder(certificate.getEncoded()));
            }
        }

        //签名者信息
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(
                new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm),
                new DefaultDigestAlgorithmIdentifierFinder().find(digestAlgorithm))
                .build(
                        new RSAKeyParameters(
                                true,
                                signerPrivateKey.getModulus(),
                                signerPrivateKey.getPrivateExponent())
                );

        //添加签名者信息
        cmsSignedDataGenerator.addSignerInfoGenerator(
                new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                        .setDirectSignature(true)
                        .build(contentSigner, new X509CertificateHolder(signerCertificate.getEncoded())));

        //签名并返回数据
        return cmsSignedDataGenerator.generate(
                new CMSProcessableByteArray(content),
                containsContent
        ).getEncoded();
    }

    /**
     * JDK格式的签名数据
     *
     * @author S.Violet
     */
    public static class JdkSignedData {

        private byte[] signature;
        private byte[] content;
        private X509Certificate[] certificates;

        /**
         * 签名数据
         *
         * @return 签名数据
         */
        public byte[] getSignature() {
            return signature;
        }

        /**
         * 签名前数据
         *
         * @return 签名前数据, 可为空
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * 证书(JDK格式)
         *
         * @return 证书(JDK格式)
         */
        public X509Certificate[] getCertificates() {
            return certificates;
        }

        @Override
        public String toString() {
            return "JdkSignedData{" +
                    "\nsignature=" + Arrays.toString(signature) +
                    "\ncontent=" + Arrays.toString(content) +
                    "\ncertificates=" + Arrays.toString(certificates) +
                    "\n}";
        }
    }

}
