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

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;

/**
 * 非对称密钥生成基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * @author S.Violet
 */
public class BaseAsymKeyGenerator {

    /**
     * 随机生成ECDSA密钥对
     * @param ecParam 椭圆类型, 例如secp256r1
     * @param keyAlgorithm 密钥算法
     * @return 密钥对
     */
    public static KeyPair generateEcKeyPair(String ecParam, String keyAlgorithm) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGen;
        keyPairGen = KeyPairGenerator.getInstance(keyAlgorithm);
        keyPairGen.initialize(new ECGenParameterSpec(ecParam));
        return keyPairGen.generateKeyPair();
    }

    /**
     * 随机生成RSA密钥对
     * @param bits 密码位数1024 2048
     * @param keyAlgorithm 密钥算法
     * @return 密钥对
     */
    public static KeyPair generateRsaKeyPair(int bits, String keyAlgorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen;
        keyPairGen = KeyPairGenerator.getInstance(keyAlgorithm);
        keyPairGen.initialize(bits);
        return keyPairGen.generateKeyPair();
    }

    /**
     * <p>根据X509格式的公钥数据生成公钥</p>
     *
     * @param x509EncodedPublicKey X509格式公钥数据
     * @param keyAlgorithm 密钥算法
     * @return 公钥
     */
    public static PublicKey parsePublicKeyByX509(byte[] x509EncodedPublicKey, String keyAlgorithm) throws InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509EncodedPublicKey);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return factory.generatePublic(keySpec);
    }

    /**
     * <p>根据PKCS8格式的私钥数据生成私钥</p>
     *
     * @param pkcs8EncodedPrivateKey PKCS8格式私钥数据
     * @return 私钥
     */
    public static PrivateKey parsePrivateKeyByPKCS8(byte[] pkcs8EncodedPrivateKey, String keyAlgorithm) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedPrivateKey);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return factory.generatePrivate(keySpec);
    }

    /**
     * 用模和指数生成RSA公钥
     *
     * @param modulus  模
     * @param exponent 指数
     */
    public static RSAPublicKey parseRsaPublicKey(BigInteger modulus, BigInteger exponent, String keyAlgorithm) throws InvalidKeySpecException {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 用模和指数生成RSA私钥
     *
     * @param modulus  模
     * @param exponent 指数
     */
    public static RSAPrivateKey parseRsaPrivateKey(BigInteger modulus, BigInteger exponent, String keyAlgorithm) throws InvalidKeySpecException {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * 已知RSA私钥获得公钥(公钥指数65537)
     * @param privateKey 私钥
     * @return 公钥(公钥指数65537)
     */
    public static RSAPublicKey parseRsaPublicKeyFromPrivate(RSAPrivateKey privateKey, String keyAlgorithm) throws InvalidKeySpecException {
        return parseRsaPublicKey(privateKey.getModulus(), new BigInteger("65537"), keyAlgorithm);
    }

    /**
     * 将私钥转为PKCS8格式的二进制数据
     * @param privateKey 私钥
     * @return PKCS8格式的私钥数据
     */
    public static byte[] encodePrivateKeyToPKCS8(PrivateKey privateKey, String keyAlgorithm) throws InvalidKeySpecException {
        if (privateKey == null){
            return null;
        }

        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return factory.getKeySpec(privateKey, PKCS8EncodedKeySpec.class).getEncoded();
    }

    /**
     * 将公钥转为X509格式的二进制数据
     * @param publicKey 公钥
     * @return X509格式的公钥数据
     */
    public static byte[] encodePublicKeyToX509(PublicKey publicKey, String keyAlgorithm) throws InvalidKeySpecException {
        if (publicKey == null){
            return null;
        }

        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return factory.getKeySpec(publicKey, X509EncodedKeySpec.class).getEncoded();
    }

}
