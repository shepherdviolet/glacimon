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
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPublicKey;

import java.security.KeyPair;
import java.security.spec.InvalidKeySpecException;

/**
 * [PQC后量子加密 ML-DSA签名算法]
 * ML-DSA密钥生成工具
 */
public class MlDsaKeyGenerator {

    public static final String KEY_ALGORITHM_ML_DSA_44 = "ML-DSA-44";
    public static final String KEY_ALGORITHM_ML_DSA_65 = "ML-DSA-65";
    public static final String KEY_ALGORITHM_ML_DSA_87 = "ML-DSA-87";

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 生成密钥对 ML-DSA-44
     *
     * 密钥长度:
     * ML-DSA-44:  私钥PKCS8编码byte[] 2626字节 公钥X509编码byte[] 1334字节
     * ML-DSA-65:  私钥PKCS8编码byte[] 4098字节 公钥X509编码byte[] 1974字节
     * ML-DSA-87: 私钥PKCS8编码byte[] 4962字节 公钥X509编码byte[] 2614字节
     *
     * @return 密钥对
     */
    public static MlDsaKeyParamsPair generateKeyPair44() {
        KeyPair keyPair = BaseBCAsymKeyGenerator.generateMlDsaKeyPair(KEY_ALGORITHM_ML_DSA_44);
        return new MlDsaKeyParamsPair((BCMLDSAPublicKey) keyPair.getPublic(), (BCMLDSAPrivateKey) keyPair.getPrivate());
    }

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 生成密钥对 ML-DSA-65
     *
     * 密钥长度:
     * ML-DSA-44:  私钥PKCS8编码byte[] 2626字节 公钥X509编码byte[] 1334字节
     * ML-DSA-65:  私钥PKCS8编码byte[] 4098字节 公钥X509编码byte[] 1974字节
     * ML-DSA-87: 私钥PKCS8编码byte[] 4962字节 公钥X509编码byte[] 2614字节
     *
     * @return 密钥对
     */
    public static MlDsaKeyParamsPair generateKeyPair65() {
        KeyPair keyPair = BaseBCAsymKeyGenerator.generateMlDsaKeyPair(KEY_ALGORITHM_ML_DSA_65);
        return new MlDsaKeyParamsPair((BCMLDSAPublicKey) keyPair.getPublic(), (BCMLDSAPrivateKey) keyPair.getPrivate());
    }

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 生成密钥对 ML-DSA-87
     *
     * 密钥长度:
     * ML-DSA-44:  私钥PKCS8编码byte[] 2626字节 公钥X509编码byte[] 1334字节
     * ML-DSA-65:  私钥PKCS8编码byte[] 4098字节 公钥X509编码byte[] 1974字节
     * ML-DSA-87: 私钥PKCS8编码byte[] 4962字节 公钥X509编码byte[] 2614字节
     *
     * @return 密钥对
     */
    public static MlDsaKeyParamsPair generateKeyPair87() {
        KeyPair keyPair = BaseBCAsymKeyGenerator.generateMlDsaKeyPair(KEY_ALGORITHM_ML_DSA_87);
        return new MlDsaKeyParamsPair((BCMLDSAPublicKey) keyPair.getPublic(), (BCMLDSAPrivateKey) keyPair.getPrivate());
    }

    /**
     * 将PKCS8私钥数据解析为私钥实例
     *
     * @param pkcs8PrivateKey PKCS8私钥数据
     */
    public static BCMLDSAPrivateKey generatePrivateKeyByPKCS8(byte[] pkcs8PrivateKey) throws InvalidKeySpecException {
        return BaseBCAsymKeyGenerator.parseMlDsaPrivateKeyByPKCS8(pkcs8PrivateKey);
    }

    /**
     * 将X509公钥数据解析为公钥实例
     *
     * @param x509PublicKey X509公钥数据
     */
    public static BCMLDSAPublicKey generatePublicKeyByX509(byte[] x509PublicKey) throws InvalidKeySpecException {
        return BaseBCAsymKeyGenerator.parseMlDsaPublicKeyByX509(x509PublicKey);
    }

    // Key Pair /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 密钥对.
     */
    public static class MlDsaKeyParamsPair {

        private final BCMLDSAPublicKey publicKey;
        private final BCMLDSAPrivateKey privateKey;

        public MlDsaKeyParamsPair(BCMLDSAPublicKey publicKey, BCMLDSAPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * [常用]获取公钥实例, 用于验签
         */
        public BCMLDSAPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * [常用]获取私钥实例, 用于签名
         */
        public BCMLDSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * 获取X509编码的公钥, 通常转成BASE64储存/发送:
         *
         * 密钥长度:
         * ML-DSA-44:  私钥PKCS8编码byte[] 2626字节 公钥X509编码byte[] 1334字节
         * ML-DSA-65:  私钥PKCS8编码byte[] 4098字节 公钥X509编码byte[] 1974字节
         * ML-DSA-87: 私钥PKCS8编码byte[] 4962字节 公钥X509编码byte[] 2614字节
         */
        public byte[] getX509EncodedPublicKey() {
            return publicKey.getEncoded();
        }

        /**
         * 获取PKCS8编码的私钥, 通常转成BASE64储存/发送:
         *
         * 密钥长度:
         * ML-DSA-44:  私钥PKCS8编码byte[] 2626字节 公钥X509编码byte[] 1334字节
         * ML-DSA-65:  私钥PKCS8编码byte[] 4098字节 公钥X509编码byte[] 1974字节
         * ML-DSA-87: 私钥PKCS8编码byte[] 4962字节 公钥X509编码byte[] 2614字节
         */
        public byte[] getPKCS8EncodedPrivateKey() {
            return privateKey.getEncoded();
        }

        @Override
        public String toString() {
            try {
                return "MlDsaKeyParamsPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) +
                        "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
            } catch (Exception e) {
                return "MlDsaKeyParamsPair\n<exception>" + e.getMessage();
            }
        }

    }

}
