/*
 * Copyright (C) 2022-2026 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.java.crypto;

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.crypto.base.BaseBCAsymKeyGenerator;
import org.bouncycastle.jcajce.provider.asymmetric.mlkem.BCMLKEMPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mlkem.BCMLKEMPublicKey;

import java.security.KeyPair;
import java.security.spec.InvalidKeySpecException;

/**
 * [PQC后量子加密 ML-KEM密钥交换算法]
 * ML-KEM密钥生成工具
 */
public class MlKemKeyGenerator {

    public static final String KEY_ALGORITHM_ML_KEM_512 = "ML-KEM-512";
    public static final String KEY_ALGORITHM_ML_KEM_768 = "ML-KEM-768";
    public static final String KEY_ALGORITHM_ML_KEM_1024 = "ML-KEM-1024";

    /**
     * [PQC后量子加密 ML-KEM密钥交换算法]
     * 生成密钥对 ML-KEM-512
     *
     * 密钥长度:
     * ML-KEM-512:  私钥PKCS8编码byte[] 1730字节 公钥X509编码byte[] 822字节
     * ML-KEM-768:  私钥PKCS8编码byte[] 2498字节 公钥X509编码byte[] 1206字节
     * ML-KEM-1024: 私钥PKCS8编码byte[] 3266字节 公钥X509编码byte[] 1590字节
     *
     * @return 密钥对
     */
    public static MlKemKeyParamsPair generateKeyPair512() {
        KeyPair keyPair = BaseBCAsymKeyGenerator.generateMlKemKeyPair(KEY_ALGORITHM_ML_KEM_512);
        return new MlKemKeyParamsPair((BCMLKEMPublicKey) keyPair.getPublic(), (BCMLKEMPrivateKey) keyPair.getPrivate());
    }

    /**
     * [PQC后量子加密 ML-KEM密钥交换算法]
     * 生成密钥对 ML-KEM-768
     *
     * 密钥长度:
     * ML-KEM-512:  私钥PKCS8编码byte[] 1730字节 公钥X509编码byte[] 822字节
     * ML-KEM-768:  私钥PKCS8编码byte[] 2498字节 公钥X509编码byte[] 1206字节
     * ML-KEM-1024: 私钥PKCS8编码byte[] 3266字节 公钥X509编码byte[] 1590字节
     *
     * @return 密钥对
     */
    public static MlKemKeyParamsPair generateKeyPair768() {
        KeyPair keyPair = BaseBCAsymKeyGenerator.generateMlKemKeyPair(KEY_ALGORITHM_ML_KEM_768);
        return new MlKemKeyParamsPair((BCMLKEMPublicKey) keyPair.getPublic(), (BCMLKEMPrivateKey) keyPair.getPrivate());
    }

    /**
     * [PQC后量子加密 ML-KEM密钥交换算法]
     * 生成密钥对 ML-KEM-1024
     *
     * 密钥长度:
     * ML-KEM-512:  私钥PKCS8编码byte[] 1730字节 公钥X509编码byte[] 822字节
     * ML-KEM-768:  私钥PKCS8编码byte[] 2498字节 公钥X509编码byte[] 1206字节
     * ML-KEM-1024: 私钥PKCS8编码byte[] 3266字节 公钥X509编码byte[] 1590字节
     *
     * @return 密钥对
     */
    public static MlKemKeyParamsPair generateKeyPair1024() {
        KeyPair keyPair = BaseBCAsymKeyGenerator.generateMlKemKeyPair(KEY_ALGORITHM_ML_KEM_1024);
        return new MlKemKeyParamsPair((BCMLKEMPublicKey) keyPair.getPublic(), (BCMLKEMPrivateKey) keyPair.getPrivate());
    }

    /**
     * 将PKCS8私钥数据解析为私钥实例
     *
     * @param pkcs8PrivateKey PKCS8私钥数据
     */
    public static BCMLKEMPrivateKey generatePrivateKeyByPKCS8(byte[] pkcs8PrivateKey) throws InvalidKeySpecException {
        return BaseBCAsymKeyGenerator.parseMlKemPrivateKeyByPKCS8(pkcs8PrivateKey);
    }

    /**
     * 将X509公钥数据解析为公钥实例
     *
     * @param x509PublicKey X509公钥数据
     */
    public static BCMLKEMPublicKey generatePublicKeyByX509(byte[] x509PublicKey) throws InvalidKeySpecException {
        return BaseBCAsymKeyGenerator.parseMlKemPublicKeyByX509(x509PublicKey);
    }

    // Key Pair /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [PQC后量子加密 ML-KEM密钥交换算法]
     * 密钥对.
     */
    public static class MlKemKeyParamsPair {

        private final BCMLKEMPublicKey publicKey;
        private final BCMLKEMPrivateKey privateKey;

        public MlKemKeyParamsPair(BCMLKEMPublicKey publicKey, BCMLKEMPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * [常用]获取公钥实例, 用于加密通讯密钥
         */
        public BCMLKEMPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * [常用]获取私钥实例, 用于解密通讯密钥
         */
        public BCMLKEMPrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * 获取X509编码的公钥, 通常转成BASE64储存/发送:
         *
         * 密钥长度:
         * ML-KEM-512:  私钥PKCS8编码byte[] 1730字节 公钥X509编码byte[] 822字节
         * ML-KEM-768:  私钥PKCS8编码byte[] 2498字节 公钥X509编码byte[] 1206字节
         * ML-KEM-1024: 私钥PKCS8编码byte[] 3266字节 公钥X509编码byte[] 1590字节
         */
        public byte[] getX509EncodedPublicKey() {
            return publicKey.getEncoded();
        }

        /**
         * 获取PKCS8编码的私钥, 通常转成BASE64储存/发送:
         *
         * 密钥长度:
         * ML-KEM-512:  私钥PKCS8编码byte[] 1730字节 公钥X509编码byte[] 822字节
         * ML-KEM-768:  私钥PKCS8编码byte[] 2498字节 公钥X509编码byte[] 1206字节
         * ML-KEM-1024: 私钥PKCS8编码byte[] 3266字节 公钥X509编码byte[] 1590字节
         */
        public byte[] getPKCS8EncodedPrivateKey() {
            return privateKey.getEncoded();
        }

        @Override
        public String toString() {
            try {
                return "MlKemKeyParamsPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) +
                        "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
            } catch (Exception e) {
                return "MlKemKeyParamsPair\n<exception>" + e.getMessage();
            }
        }

    }

}
