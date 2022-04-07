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

import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.crypto.base.BaseAsymKeyGenerator;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * <p>RSA秘钥生成工具</p>
 *
 * <p>BigInteger转HexString: bigInteger.toString(16) </p>
 * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意要去掉头部的0x00</p>
 * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
 * <p>byte[]转BigInteger: new BigInteger(bytes), 有时头部没0x00变负数, 试试new BigInteger(ByteUtils.bytesToHex(bytes), 16)</p>
 *
 * @author S.Violet
 */
public class RSAKeyGenerator {

    public static final String RSA_KEY_ALGORITHM = "RSA";

    /**
     * 随机生成RSA密钥对(2048位)
     *
     * @return 密钥对
     */
    public static RSAKeyPair generateKeyPair() {
        return generateKeyPair(2048);
    }

    /**
     * 随机生成RSA密钥对
     *
     * @param bits 秘钥位数(1024/2048)
     * @return 密钥对
     */
    public static RSAKeyPair generateKeyPair(int bits) {
        KeyPair keyPair;
        try {
            keyPair = BaseAsymKeyGenerator.generateRsaKeyPair(bits, RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return new RSAKeyPair((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }

    /**
     * <p>根据X509格式的公钥数据生成公钥</p>
     *
     * @param x509EncodedPublicKey X509格式公钥数据
     * @return 公钥
     */
    public static RSAPublicKey generatePublicKeyByX509(byte[] x509EncodedPublicKey) throws InvalidKeySpecException {
        return (RSAPublicKey) BaseAsymKeyGenerator.parsePublicKeyByX509(x509EncodedPublicKey, RSA_KEY_ALGORITHM);
    }

    /**
     * <p>根据PKCS8格式的私钥数据生成私钥</p>
     *
     * @param pkcs8EncodedPrivateKey PKCS8格式私钥数据
     * @return 私钥
     */
    public static RSAPrivateKey generatePrivateKeyByPKCS8(byte[] pkcs8EncodedPrivateKey) throws InvalidKeySpecException {
        return (RSAPrivateKey) BaseAsymKeyGenerator.parsePrivateKeyByPKCS8(pkcs8EncodedPrivateKey, RSA_KEY_ALGORITHM);
    }

    /**
     * <p>用模和指数生成RSA公钥</p>
     *
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意要去掉头部的0x00</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(bytes), 有时头部没0x00变负数, 试试new BigInteger(ByteUtils.bytesToHex(bytes), 16)</p>
     *
     * @param modulus  模, 与私钥的模数一致
     * @param exponent 指数, 常用65537
     */
    public static RSAPublicKey generatePublicKey(BigInteger modulus, BigInteger exponent) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.parseRsaPublicKey(modulus, exponent, RSA_KEY_ALGORITHM);
    }

    /**
     * <p>用模和指数生成RSA私钥</p>
     *
     * <p>BigInteger转HexString: bigInteger.toString(16) </p>
     * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意要去掉头部的0x00</p>
     * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
     * <p>byte[]转BigInteger: new BigInteger(bytes), 有时头部没0x00变负数, 试试new BigInteger(ByteUtils.bytesToHex(bytes), 16)</p>
     *
     * @param modulus  模, 与公钥的模数一致
     * @param exponent 指数
     */
    public static RSAPrivateKey generatePrivateKey(BigInteger modulus, BigInteger exponent) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.parseRsaPrivateKey(modulus, exponent, RSA_KEY_ALGORITHM);
    }

    /**
     * 已知私钥获得公钥(公钥指数65537)
     * @param privateKey 私钥
     * @return 公钥(公钥指数65537)
     */
    public static RSAPublicKey parsePublicKeyFromPrivateKey(RSAPrivateKey privateKey) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.parseRsaPublicKeyFromPrivate(privateKey, RSA_KEY_ALGORITHM);
    }

    /**
     * 将私钥转为PKCS8格式的二进制数据
     * @param privateKey 私钥
     * @return PKCS8格式的私钥数据
     */
    public static byte[] encodePrivateKeyToPKCS8(RSAPrivateKey privateKey) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.encodePrivateKeyToPKCS8(privateKey, RSA_KEY_ALGORITHM);
    }

    /**
     * 将公钥转为X509格式的二进制数据
     * @param publicKey 公钥
     * @return X509格式的公钥数据
     */
    public static byte[] encodePublicKeyToX509(RSAPublicKey publicKey) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.encodePublicKeyToX509(publicKey, RSA_KEY_ALGORITHM);
    }

    public static class RSAKeyPair {

        private RSAPublicKey publicKey = null;
        private RSAPrivateKey privateKey = null;

        public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * 获取公钥
         */
        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * 获取私钥
         */
        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * 获取模数
         *
         * <p>BigInteger转HexString: bigInteger.toString(16) </p>
         * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意要去掉头部的0x00</p>
         * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
         * <p>byte[]转BigInteger: new BigInteger(bytes), 有时头部没0x00变负数, 试试new BigInteger(ByteUtils.bytesToHex(bytes), 16)</p>
         */
        public BigInteger getModulus() {
            return publicKey.getModulus();
        }

        /**
         * 获取公钥指数
         *
         * <p>BigInteger转HexString: bigInteger.toString(16) </p>
         * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意要去掉头部的0x00</p>
         * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
         * <p>byte[]转BigInteger: new BigInteger(bytes), 有时头部没0x00变负数, 试试new BigInteger(ByteUtils.bytesToHex(bytes), 16)</p>
         */
        public BigInteger getPublicExponent() {
            return publicKey.getPublicExponent();
        }

        /**
         * 获取私钥指数
         *
         * <p>BigInteger转HexString: bigInteger.toString(16) </p>
         * <p>BigInteger转byte[]: ByteUtils.trimHeader(bigInteger.toByteArray())), 注意要去掉头部的0x00</p>
         * <p>HexString转BigInteger: new BigInteger(string, 16)</p>
         * <p>byte[]转BigInteger: new BigInteger(bytes), 有时头部没0x00变负数, 试试new BigInteger(ByteUtils.bytesToHex(bytes), 16)</p>
         */
        public BigInteger getPrivateExponent() {
            return privateKey.getPrivateExponent();
        }

        public byte[] getX509EncodedPublicKey() throws InvalidKeySpecException {
            return encodePublicKeyToX509(publicKey);
        }

        public byte[] getPKCS8EncodedPrivateKey() throws InvalidKeySpecException {
            return encodePrivateKeyToPKCS8(privateKey);
        }

        @Override
        public String toString() {
            try {
                return "RSAKeyPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) + "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
            } catch (InvalidKeySpecException e) {
                return "RSAKeyPair\n<exception>" + e.getMessage();
            }
        }

    }
}
