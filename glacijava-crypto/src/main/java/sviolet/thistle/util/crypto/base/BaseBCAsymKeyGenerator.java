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

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * [Bouncy castle]非对称密钥生成基本逻辑<p>
 * <p>
 * Not recommended for direct use<p>
 * <p>
 * 不建议直接使用<p>
 *
 * @author S.Violet
 */
public class BaseBCAsymKeyGenerator {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /**
     * 随机生成ECC/SM2密钥对, domainParameters是椭圆曲线参数, 需要:椭圆曲线/G点/N(order)/H(cofactor)
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param secureRandom     默认送空
     * @return 密钥对(与JDK的密钥实例不同)
     */
    public static AsymmetricCipherKeyPair generateEcKeyParamsPair(ECDomainParameters domainParameters, SecureRandom secureRandom) {
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParameters,
                secureRandom != null ? secureRandom : BaseKeyGenerator.getSystemSecureRandom());
        ECKeyPairGenerator keyPairGen = new ECKeyPairGenerator();
        keyPairGen.init(keyGenParams);
        return keyPairGen.generateKeyPair();
    }

    /**
     * 将BouncyCastle的XXXKeyParameters私钥实例转换为JDK的XXXKey私钥实例, 用于与JDK加密工具适配, 或获取PKCS8编码的私钥数据
     *
     * @param privateKeyParams 私钥, BouncyCastle的XXXKeyParameters密钥实例
     * @param publicKeyParams 公钥, 可为空(但送空会导致openssl无法读取PKCS8数据), BouncyCastle的XXXKeyParameters密钥实例
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey密钥实例, 可以调用ECPrivateKey.getEncoded()方法获取PKCS8编码的私钥数据(甚至进一步转为PEM等格式)
     */
    public static BCECPrivateKey ecPrivateKeyParamsToEcPrivateKey(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams, String keyAlgorithm) {
        if (privateKeyParams == null) {
            throw new RuntimeException("privateKeyParams == null");
        }
        ECDomainParameters domainParameters = privateKeyParams.getParameters();
        ECParameterSpec parameterSpec = new ECParameterSpec(
                domainParameters.getCurve(),
                domainParameters.getG(),
                domainParameters.getN(),
                domainParameters.getH());
        BCECPublicKey publicKey = null;
        if (publicKeyParams != null) {
            publicKey = new BCECPublicKey(keyAlgorithm, publicKeyParams, parameterSpec, BouncyCastleProvider.CONFIGURATION);
        }
        return new BCECPrivateKey(keyAlgorithm, privateKeyParams, publicKey, parameterSpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 将BouncyCastle的XXXKeyParameters公钥实例转换为JDK的XXXKey公钥实例, 用于与JDK加密工具适配, 或获取X509编码的公钥数据
     *
     * @param publicKeyParams 公钥, BouncyCastle的XXXKeyParameters密钥实例
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey密钥实例, 可以调用ECPublicKey.getEncoded()方法获取X509编码的公钥数据(甚至进一步转为PEM等格式)
     */
    public static BCECPublicKey ecPublicKeyParamsToEcPublicKey(ECPublicKeyParameters publicKeyParams, String keyAlgorithm) {
        if (publicKeyParams == null) {
            throw new RuntimeException("publicKeyParams == null");
        }
        ECDomainParameters domainParameters = publicKeyParams.getParameters();
        ECParameterSpec parameterSpec = new ECParameterSpec(
                domainParameters.getCurve(),
                domainParameters.getG(),
                domainParameters.getN(),
                domainParameters.getH());
        return new BCECPublicKey(keyAlgorithm, publicKeyParams, parameterSpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 将JDK的XXXKey私钥实例转换为BouncyCastle的XXXKeyParameters私钥实例
     *
     * @param privateKey JDK的XXXKey私钥实例
     * @return BouncyCastle的XXXKeyParameters私钥实例
     */
    public static ECPrivateKeyParameters ecPrivateKeyToEcPrivateKeyParams(BCECPrivateKey privateKey) {
        if (privateKey == null) {
            throw new RuntimeException("privateKey == null");
        }
        ECParameterSpec parameterSpec = privateKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(
                parameterSpec.getCurve(),
                parameterSpec.getG(),
                parameterSpec.getN(),
                parameterSpec.getH());
        return new ECPrivateKeyParameters(privateKey.getD(), domainParameters);
    }

    /**
     * 将JDK的XXXKey公钥实例转换为BouncyCastle的XXXKeyParameters公钥实例
     *
     * @param publicKey JDK的XXXKey公钥实例
     * @return BouncyCastle的XXXKeyParameters公钥实例
     */
    public static ECPublicKeyParameters ecPublicKeyToEcPublicKeyParams(BCECPublicKey publicKey) {
        if (publicKey == null) {
            throw new RuntimeException("publicKey == null");
        }
        ECParameterSpec parameterSpec = publicKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(
                parameterSpec.getCurve(),
                parameterSpec.getG(),
                parameterSpec.getN(),
                parameterSpec.getH());
        return new ECPublicKeyParameters(publicKey.getQ(), domainParameters);
    }

    /**
     * 将PKCS8数据解析为JDK的XXXKey私钥实例, 用于再转化为BouncyCastle的XXXKeyParameters私钥实例
     *
     * @param pkcs8 PKCS8私钥数据
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey私钥实例
     */
    public static BCECPrivateKey parseEcPrivateKeyByPkcs8(byte[] pkcs8, String keyAlgorithm) throws InvalidKeySpecException{
        if (pkcs8 == null) {
            throw new RuntimeException("pkcs8 == null");
        }
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm, BouncyCastleProviderUtils.getProviderName());
            return (BCECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将X509数据解析为JDK的XXXKey公钥实例, 用于再转化为BouncyCastle的XXXKeyParameters公钥实例
     *
     * @param x509 X509公钥数据
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey公钥实例
     */
    public static BCECPublicKey parseEcPublicKeyByX509(byte[] x509, String keyAlgorithm) throws InvalidKeySpecException {
        if (x509 == null) {
            throw new RuntimeException("x509 == null");
        }
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm, BouncyCastleProviderUtils.getProviderName());
            return (BCECPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 根据已知的D值生成ECC/SM2私钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param d D值
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters parseEcPrivateKeyParams(ECDomainParameters domainParameters, BigInteger d) throws CommonCryptoException {
        if (d == null) {
            throw new NullPointerException("d == null");
        }
        try {
            return new ECPrivateKeyParameters(d, domainParameters);
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing D to privateKeyParameters", e);
        }
    }

    /**
     * 根据已知的坐标点(ASN.1编码数据)生成ECC/SM2公钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param pointASN1Encoding 公钥坐标点(ASN.1编码数据)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters parseEcPublicKeyParams(ECDomainParameters domainParameters, byte[] pointASN1Encoding) throws CommonCryptoException {
        if (pointASN1Encoding == null) {
            throw new RuntimeException("pointASN1Encoding == null");
        }
        try {
            //将ASN.1编码的数据转为ECPoint实例
            ECPoint point = domainParameters.getCurve().decodePoint(pointASN1Encoding);
            return new ECPublicKeyParameters(point, domainParameters);
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing ASN.1 point to publicKeyParameters", e);
        }
    }

    /**
     * 根据已知的坐标(X/Y)生成ECC/SM2公钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param xBytes 坐标X, 字节形式(bigInteger.toByteArray()获得)
     * @param yBytes 坐标Y, 字节形式(bigInteger.toByteArray()获得)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters parseEcPublicKeyParams(ECDomainParameters domainParameters, byte[] xBytes, byte[] yBytes) throws CommonCryptoException {
        try {
            //将ASN.1编码的数据转为ECPoint实例
            return parseEcPublicKeyParams(domainParameters, BaseCryptoUtils.pointToASN1Encoding(xBytes, yBytes));
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing point (X/Y) to publicKeyParameters", e);
        }
    }

    /**
     * 从X509Certificate证书实例中提取JDK的XXXKey公钥实例
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param certificate 证书
     * @return JDK的XXXKey公钥实例
     */
    public static BCECPublicKey parseEcPublicKeyFromCertificate(ECDomainParameters domainParameters, X509Certificate certificate){
        if (certificate == null) {
            throw new NullPointerException("certificate == null");
        }
        PublicKey publicKey = certificate.getPublicKey();
        if (!(publicKey instanceof ECPublicKey)) {
            throw new NullPointerException("certificate is not an certificate of EC algorithm, the public key is not a ECPublicKey instance");
        }
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        ECParameterSpec parameterSpec = new ECParameterSpec(
                domainParameters.getCurve(),
                domainParameters.getG(),
                domainParameters.getN(),
                domainParameters.getH());
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(ecPublicKey.getQ(), parameterSpec);
        return new BCECPublicKey(ecPublicKey.getAlgorithm(), pubKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 从X509Certificate证书实例中提取BouncyCastle的XXXKeyParameters公钥实例
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param certificate 证书
     * @return BouncyCastle的XXXKeyParameters公钥实例
     */
    public static ECPublicKeyParameters parseEcPublicKeyParamsFromCertificate(ECDomainParameters domainParameters, X509Certificate certificate){
        if (certificate == null) {
            throw new NullPointerException("certificate == null");
        }
        PublicKey publicKey = certificate.getPublicKey();
        if (!(publicKey instanceof ECPublicKey)) {
            throw new NullPointerException("certificate is not an certificate of EC algorithm, the public key is not a ECPublicKey instance");
        }
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        return new ECPublicKeyParameters(ecPublicKey.getQ(), domainParameters);
    }

    /**
     * 将私钥PKCS8格式数据转为SEC1标准数据.
     * openssl d2i_ECPrivateKey函数要求的DER编码的私钥也是SEC1标准的, 这个方法能生成一个openssl可以识别的的ECC私钥.
     * openssl能识别PKCS1标准的RSA私钥, SEC1标准的ECC私钥.
     * @param pkcs8 私钥的PKCS8格式数据, ECPrivateKey.getEncoded()
     * @return 私钥的SEC1标准数据
     */
    public static byte[] encodePkcs8ToSec1(byte[] pkcs8) throws IOException {
        return PrivateKeyInfo.getInstance(pkcs8)
                .parsePrivateKey()
                .toASN1Primitive()
                .getEncoded();
    }

    /**
     * 将私钥SEC1标准数据转为PKCS8格式数据.
     * openssl d2i_ECPrivateKey函数要求的DER编码的私钥也是SEC1标准的, 这个方法能将openssl可以识别的的ECC私钥转为PKCS8格式.
     * openssl能识别PKCS1标准的RSA私钥, SEC1标准的ECC私钥.
     * @param ecParameterSpec SM2DefaultCurve.EC_PARAM_SPEC_FOR_SEC1
     * @param sec1 SEC1标准的私钥数据
     * @return PKCS8标准的私钥数据
     */
    public static byte[] encodeSec1ToPkcs8(java.security.spec.ECParameterSpec ecParameterSpec, byte[] sec1) throws IOException {
        X962Parameters x962Parameters = ecParameterSpecToX962Parameters(ecParameterSpec, false);
        ASN1OctetString asn1OctetString = new DEROctetString(sec1);
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(0));//version
        vector.add(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, x962Parameters));//algorithm
        vector.add(asn1OctetString);
        DERSequence sequence = new DERSequence(vector);
        return sequence.getEncoded(ASN1Encoding.DER);
    }

    /**
     * 特殊: 将ECParameterSpec转为X962Parameters, 用于将SEC1标准私钥数据转为PKCS8
     * @param ecParameterSpec SM2DefaultCurve.EC_PARAM_SPEC_FOR_SEC1
     * @param withCompression 默认false
     */
    public static X962Parameters ecParameterSpecToX962Parameters(java.security.spec.ECParameterSpec ecParameterSpec, boolean withCompression) {
        if (ecParameterSpec == null) {
            return new X962Parameters(DERNull.INSTANCE);
        }
        if (ecParameterSpec instanceof ECNamedCurveSpec) {
            ASN1ObjectIdentifier asn1ObjectIdentifier = ECUtil.getNamedCurveOid(((ECNamedCurveSpec)ecParameterSpec).getName());
            if (asn1ObjectIdentifier == null) {
                asn1ObjectIdentifier = new ASN1ObjectIdentifier(((ECNamedCurveSpec)ecParameterSpec).getName());
            }
            return new X962Parameters(asn1ObjectIdentifier);
        }
        ECCurve ecCurve = EC5Util.convertCurve(ecParameterSpec.getCurve());
        java.security.spec.ECPoint ecPoint = ecParameterSpec.getGenerator();
        X9ECParameters x9ECParameters = new X9ECParameters(
                ecCurve,
                new X9ECPoint(ecCurve.createPoint(ecPoint.getAffineX(), ecPoint.getAffineY()), withCompression),
                ecParameterSpec.getOrder(),
                BigInteger.valueOf(ecParameterSpec.getCofactor()),
                ecParameterSpec.getCurve().getSeed());
        return new X962Parameters(x9ECParameters);
    }

}
