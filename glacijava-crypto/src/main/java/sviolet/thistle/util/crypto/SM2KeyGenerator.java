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

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.base.BaseBCAsymKeyGenerator;
import sviolet.thistle.util.crypto.base.CommonCryptoException;
import sviolet.thistle.util.crypto.base.SM2DefaultCurve;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

/**
 * <p>SM2秘钥生成工具</p><br>
 *
 * <p>BouncyCastle是使用XXXKeyParameters密钥实例的, 而JDK默认使用XXXKey密钥实例, 本工具类在加解密加解签时, 统一使用BouncyCastle
 * 的XXXKeyParameters密钥实例, 这与RSA/ECDSA工具类中使用XXXKey实例不同. 另外, 本工具类提供将密钥实例转换为JDK密钥实例的方法. </p><br>
 *
 * <p>SM2算法首先要选择一个合适的椭圆曲线, 椭圆曲线参数包含: Curve (P A B) / G-Point (X Y) / N / H , SM2算法的公钥是一个这个
 * 椭圆曲线上的点Q(X Y), 私钥是次数D, PKCS8/X509编码的私钥公钥除了Q点和次数D以外, 也包含的椭圆曲线参数. </p><br>
 *
 * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
 * <p>获取D值: ecPrivateKeyParameters.getD() </p>
 * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
 * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
 * <p>BigInteger转HexString: bigInteger.toString(16) </p>
 * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
 * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
 * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
 *
 * @author S.Violet
 */
public class SM2KeyGenerator {

    /**
     * 密钥类型:SM2
     */
    public static final String SM2_KEY_ALGORITHM = "SM2";
    public static final String EC_KEY_ALGORITHM = "EC";

    /**
     * 随机生成SM2密钥对(sm2p256v1)
     *
     * @return 密钥对(与JDK的密钥实例不同)
     */
    public static SM2KeyParamsPair generateKeyParamsPair() {
        AsymmetricCipherKeyPair asymKeyPair = BaseBCAsymKeyGenerator.generateEcKeyParamsPair(SM2DefaultCurve.DOMAIN_PARAMS, null);
        return new SM2KeyParamsPair((ECPublicKeyParameters)asymKeyPair.getPublic(), (ECPrivateKeyParameters)asymKeyPair.getPrivate());
    }

    /**
     * 将PKCS8私钥数据解析为私钥实例(与JDK的密钥实例不同)
     * @param pkcs8 PKCS8私钥数据
     */
    public static ECPrivateKeyParameters generatePrivateKeyParamsByPKCS8(byte[] pkcs8) throws InvalidKeySpecException {
        return BaseBCAsymKeyGenerator.ecPrivateKeyToEcPrivateKeyParams(
                //SM2的密钥标记为EC
                BaseBCAsymKeyGenerator.parseEcPrivateKeyByPkcs8(pkcs8, EC_KEY_ALGORITHM)
        );
    }

    /**
     * 将X509公钥数据解析为公钥实例(与JDK的密钥实例不同)
     * @param x509 X509公钥数据
     */
    public static ECPublicKeyParameters generatePublicKeyParamsByX509(byte[] x509) throws InvalidKeySpecException {
        return BaseBCAsymKeyGenerator.ecPublicKeyToEcPublicKeyParams(
                //SM2的密钥标记为EC
                BaseBCAsymKeyGenerator.parseEcPublicKeyByX509(x509, EC_KEY_ALGORITHM)
        );
    }

    /**
     * <p>根据已知的D值生成SM2私钥实例(sm2p256v1).</p>
     * <p>如果D值是Base64字符串, 转成byte[], 然后new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     * <p>如果D值是Hex字符串, 直接new BigInteger(string, 16)</p>
     * <br>
     * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
     * <p>获取D值: ecPrivateKeyParameters.getD() </p>
     * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
     * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     *
     * @param d D值
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters generatePrivateKeyParams(BigInteger d) throws CommonCryptoException {
        return BaseBCAsymKeyGenerator.parseEcPrivateKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, d);
    }

    /**
     * <p>根据已知的D值生成SM2私钥实例(sm2p256v1).</p>
     *
     * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
     * <p>获取D值: ecPrivateKeyParameters.getD() </p>
     * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
     * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     *
     * @param dBytes D值, 二进制
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters generatePrivateKeyParams(byte[] dBytes) throws CommonCryptoException {
        return BaseBCAsymKeyGenerator.parseEcPrivateKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, new BigInteger(ByteUtils.bytesToHex(dBytes), 16));
    }

    /**
     * <p>根据已知的D值生成SM2私钥实例(sm2p256v1).</p>
     *
     * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
     * <p>获取D值: ecPrivateKeyParameters.getD() </p>
     * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
     * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     *
     * @param dHex D值, 十六进制字符串(HEX)
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters generatePrivateKeyParams(String dHex) throws CommonCryptoException {
        return BaseBCAsymKeyGenerator.parseEcPrivateKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, new BigInteger(dHex, 16));
    }

    /**
     * 根据已知的坐标(ASN.1编码数据)生成SM2公钥实例(sm2p256v1)
     *
     * @param asn1Encoding 公钥坐标点(ASN.1编码数据)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParamsByASN1(byte[] asn1Encoding) throws CommonCryptoException {
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, asn1Encoding);
    }

    /**
     * <p>根据已知的坐标(X/Y)生成SM2公钥实例(sm2p256v1)</p><br>
     *
     * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
     * <p>获取D值: ecPrivateKeyParameters.getD() </p>
     * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
     * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     *
     * @param x 坐标X
     * @param y 坐标Y
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParams(BigInteger x, BigInteger y) throws CommonCryptoException {
        if (x == null || y == null) {
            throw new NullPointerException("x or y is null");
        }
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, x.toByteArray(), y.toByteArray());
    }

    /**
     * <p>根据已知的坐标(X/Y)生成SM2公钥实例(sm2p256v1)</p><br>
     *
     * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
     * <p>获取D值: ecPrivateKeyParameters.getD() </p>
     * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
     * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     *
     * @param xBytes 坐标X, 二进制数据
     * @param yBytes 坐标Y, 二进制数据
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParams(byte[] xBytes, byte[] yBytes) throws CommonCryptoException {
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, xBytes, yBytes);
    }

    /**
     * <p>根据已知的坐标(X/Y)生成SM2公钥实例(sm2p256v1)</p><br>
     *
     * <p>D值 / Q值(X/Y) 获取方法 + 各种格式转换: </p>
     * <p>获取D值: ecPrivateKeyParameters.getD() </p>
     * <p>获取Q(X)值: ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger() </p>
     * <p>获取Q(Y)值: ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger() </p>
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(ByteUtils.bytesToHex(bytes), 16). 不要new BigInteger(bytes), 因为如果bytes第一个字节大于等于0x80会变负数!!!</p>
     *
     * @param xHex 坐标X, 十六进制字符串(hex)
     * @param yHex 坐标Y, 十六进制字符串(hex)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParams(String xHex, String yHex) throws CommonCryptoException {
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, ByteUtils.hexToBytes(xHex), ByteUtils.hexToBytes(yHex));
    }


    // Encode //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 将私钥实例转换为PKCS8编码的数据
     *
     * @param privateKeyParams 私钥, BouncyCastle的XXXKeyParameters密钥实例
     * @param publicKeyParams 公钥, 可为空(但送空会导致openssl无法读取PKCS8数据), BouncyCastle的XXXKeyParameters密钥实例
     * @return 私钥的PKCS8编码数据
     */
    public static byte[] encodePrivateKeyParamsToPKCS8(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPrivateKeyParamsToEcPrivateKey(privateKeyParams, publicKeyParams, EC_KEY_ALGORITHM).getEncoded();
    }

    /**
     * 将私钥实例转换为PKCS8编码的私钥数据, openssl无法读取这种方法生成的数据, 需要用openssl请用
     * encodePrivateKeyParamsToPKCS8(ECPrivateKeyParameters, ECPublicKeyParameters)方法
     *
     * @param privateKeyParams 私钥, BouncyCastle的XXXKeyParameters密钥实例
     * @return 私钥的PKCS8编码数据
     */
    public static byte[] encodePrivateKeyParamsToPKCS8(ECPrivateKeyParameters privateKeyParams) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPrivateKeyParamsToEcPrivateKey(privateKeyParams, null, EC_KEY_ALGORITHM).getEncoded();
    }

    /**
     * 将公钥实例转换为X509编码的公钥数据
     *
     * @param publicKeyParams 公钥, BouncyCastle的XXXKeyParameters密钥实例
     * @return 公钥的X509编码数据
     */
    public static byte[] encodePublicKeyParamsToX509(ECPublicKeyParameters publicKeyParams) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPublicKeyParamsToEcPublicKey(publicKeyParams, EC_KEY_ALGORITHM).getEncoded();
    }

    // Conversion ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 将BouncyCastle的XXXKeyParameters私钥实例转换为JDK的XXXKey私钥实例, 用于与JDK加密工具适配, 或获取PKCS8编码的私钥数据
     *
     * @param privateKeyParams 私钥, BouncyCastle的XXXKeyParameters密钥实例
     * @param publicKeyParams 公钥, 可为空(但送空会导致openssl无法读取PKCS8数据), BouncyCastle的XXXKeyParameters密钥实例
     * @return JDK的XXXKey密钥实例, 可以调用ECPrivateKey.getEncoded()方法获取PKCS8编码的私钥数据(甚至进一步转为PEM等格式)
     */
    public static BCECPrivateKey privateKeyParamsToPrivateKey(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPrivateKeyParamsToEcPrivateKey(privateKeyParams, publicKeyParams, EC_KEY_ALGORITHM);
    }

    /**
     * 将BouncyCastle的XXXKeyParameters公钥实例转换为JDK的XXXKey公钥实例, 用于与JDK加密工具适配, 或获取X509编码的公钥数据
     *
     * @param publicKeyParams 公钥, BouncyCastle的XXXKeyParameters密钥实例
     * @return JDK的XXXKey密钥实例, 可以调用ECPublicKey.getEncoded()方法获取X509编码的公钥数据(甚至进一步转为PEM等格式)
     */
    public static BCECPublicKey publicKeyParamsToPublicKey(ECPublicKeyParameters publicKeyParams) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPublicKeyParamsToEcPublicKey(publicKeyParams, EC_KEY_ALGORITHM);
    }

    /**
     * 将JDK的XXXKey私钥实例转换为BouncyCastle的XXXKeyParameters私钥实例
     *
     * @param privateKey JDK的XXXKey私钥实例
     * @return BouncyCastle的XXXKeyParameters私钥实例
     */
    public static ECPrivateKeyParameters privateKeyToPrivateKeyParams(BCECPrivateKey privateKey) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPrivateKeyToEcPrivateKeyParams(privateKey);
    }

    /**
     * 将JDK的XXXKey公钥实例转换为BouncyCastle的XXXKeyParameters公钥实例
     *
     * @param publicKey JDK的XXXKey公钥实例
     * @return BouncyCastle的XXXKeyParameters公钥实例
     */
    public static ECPublicKeyParameters publicKeyToPublicKeyParams(BCECPublicKey publicKey) {
        //SM2的密钥标记为EC
        return BaseBCAsymKeyGenerator.ecPublicKeyToEcPublicKeyParams(publicKey);
    }

    /**
     * 从证书中提取SM2公钥
     * @param certificate 证书
     * @return 公钥
     */
    public static ECPublicKeyParameters parsePublicKeyParamsFromCertificate(X509Certificate certificate) {
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParamsFromCertificate(SM2DefaultCurve.DOMAIN_PARAMS, certificate);
    }

    // Key Pair /////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class SM2KeyParamsPair {

        private ECPublicKeyParameters publicKeyParams;
        private ECPrivateKeyParameters privateKeyParams;

        public SM2KeyParamsPair(ECPublicKeyParameters publicKeyParams, ECPrivateKeyParameters privateKeyParams) {
            this.publicKeyParams = publicKeyParams;
            this.privateKeyParams = privateKeyParams;
        }

        /**
         * [常用]获取公钥实例, 用于加解签操作(与JDK的密钥实例不同)
         */
        public ECPublicKeyParameters getPublicKeyParams() {
            return publicKeyParams;
        }

        /**
         * [常用]获取私钥实例, 用于加解签操作(与JDK的密钥实例不同)
         */
        public ECPrivateKeyParameters getPrivateKeyParams() {
            return privateKeyParams;
        }

        /**
         * 获取JDK的XXXKey公钥实例, 用于适配
         */
        public BCECPublicKey getJdkPublicKey() {
            return publicKeyParamsToPublicKey(publicKeyParams);
        }

        /**
         * 获取JDK的XXXKey私钥实例, 用于适配
         */
        public BCECPrivateKey getJdkPrivateKey() {
            return privateKeyParamsToPrivateKey(privateKeyParams, publicKeyParams);
        }

        /**
         * 获取公钥坐标点的ASN.1编码数据(非压缩).
         * 未压缩格式开头有个0x04, 有些加解密工具没0x04.
         */
        public byte[] getPublicASN1Encoding(){
            return publicKeyParams.getQ().getEncoded(false);
        }

        /**
         * <p>获取公钥的X值 (QX).</p>
         * <p>转byte[], ByteUtils.trimHeader(SM2KeyParamsPair.getPublicX().toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
         * <p>转Hex, SM2KeyParamsPair.getPublicX().toString(16).</p>
         */
        public BigInteger getPublicX(){
            return publicKeyParams.getQ().getAffineXCoord().toBigInteger();
        }

        /**
         * <p>获取公钥的X值 (QX).</p>
         * <p>转byte[], ByteUtils.trimHeader(SM2KeyParamsPair.getPublicX().toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
         * <p>转Hex, SM2KeyParamsPair.getPublicX().toString(16).</p>
         */
        public byte[] getPublicXBytes(){
            return ByteUtils.trimHeader(publicKeyParams.getQ().getAffineXCoord().toBigInteger().toByteArray());
        }

        /**
         * <p>获取公钥的Y值 (QY).</p>
         * <p>转byte[], ByteUtils.trimHeader(SM2KeyParamsPair.getPublicY().toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
         * <p>转Hex, SM2KeyParamsPair.getPublicY().toString(16).</p>
         */
        public BigInteger getPublicY(){
            return publicKeyParams.getQ().getAffineYCoord().toBigInteger();
        }

        /**
         * <p>获取公钥的Y值 (QY).</p>
         * <p>转byte[], ByteUtils.trimHeader(SM2KeyParamsPair.getPublicY().toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
         * <p>转Hex, SM2KeyParamsPair.getPublicY().toString(16).</p>
         */
        public byte[] getPublicYBytes(){
            return ByteUtils.trimHeader(publicKeyParams.getQ().getAffineYCoord().toBigInteger().toByteArray());
        }

        /**
         * <p>获取私钥的D值.</p>
         * <p>转byte[], ByteUtils.trimHeader(SM2KeyParamsPair.getPrivateD().toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
         * <p>转Hex, SM2KeyParamsPair.getPrivateD().toString(16).</p>
         */
        public BigInteger getPrivateD(){
            return privateKeyParams.getD();
        }

        /**
         * <p>获取私钥的D值.</p>
         * <p>转byte[], ByteUtils.trimHeader(SM2KeyParamsPair.getPrivateD().toByteArray())), 注意!!!要去掉头部的0x00!!!</p>
         * <p>转Hex, SM2KeyParamsPair.getPrivateD().toString(16).</p>
         */
        public byte[] getPrivateDBytes(){
            return ByteUtils.trimHeader(privateKeyParams.getD().toByteArray());
        }

        public byte[] getX509EncodedPublicKey() {
            return encodePublicKeyParamsToX509(publicKeyParams);
        }

        public byte[] getPKCS8EncodedPrivateKey() {
            return encodePrivateKeyParamsToPKCS8(privateKeyParams, publicKeyParams);
        }

        @Override
        public String toString() {
            try {
                return "SM2KeyParamsPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) + "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
            } catch (Exception e) {
                return "SM2KeyParamsPair\n<exception>" + e.getMessage();
            }
        }

    }
}
