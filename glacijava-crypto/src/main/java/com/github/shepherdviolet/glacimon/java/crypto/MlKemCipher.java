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

import com.github.shepherdviolet.glacimon.java.conversion.ByteUtils;
import com.github.shepherdviolet.glacimon.java.crypto.base.BaseBCCipher;
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.provider.asymmetric.mlkem.BCMLKEMPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mlkem.BCMLKEMPublicKey;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * [PQC后量子加密 ML-KEM密钥交换算法]
 * ML-KEM加密工具
 */
public class MlKemCipher {

    public static final String CRYPTO_ALGORITHM_ML_KEM_512 = "ML-KEM-512";
    public static final String CRYPTO_ALGORITHM_ML_KEM_768 = "ML-KEM-768";
    public static final String CRYPTO_ALGORITHM_ML_KEM_1024 = "ML-KEM-1024";

    /**
     * [PQC后量子加密 ML-KEM密钥交换算法]
     * 密钥封装: 随机生成32字节通讯密钥并使用公钥加密.
     * ML_KEM算法仅用于交换通讯密钥(对称), 且密钥明文不允许指定, 必须随机生成.
     *
     * @param publicKey    接收方的公钥
     * @param cryptoAlgorithm 算法名称, ML-KEM-512, ML-KEM-768, ML-KEM-1024
     * @return 生成的32字节通讯密钥明文 = KeyAndEncryptedKey.getRawKey(); 生成的768/1088/1568字节通讯密钥密文:  KeyAndEncryptedKey.getEncryptedKey();
     */
    public static KeyAndEncryptedKey encapsulate(BCMLKEMPublicKey publicKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        SecretKeyWithEncapsulation secretKeyWithEncapsulation = BaseBCCipher.mlKemEncapsulate(publicKey, cryptoAlgorithm);
        return new KeyAndEncryptedKey(secretKeyWithEncapsulation.getEncoded(), secretKeyWithEncapsulation.getEncapsulation());
    }

    /**
     * [PQC后量子加密 ML-KEM密钥交换算法]
     * 密钥解封装: 接收方使用自己的私钥，从768/1088/1568字节密文中恢复出 32 字节的通讯密钥。
     *
     * @param encryptedKey  768/1088/1568字节通讯密钥密文
     * @param privateKey    接收方的私钥
     * @param cryptoAlgorithm  算法名称, ML-KEM-512, ML-KEM-768, ML-KEM-1024
     * @return 32字节通讯密钥明文
     */
    public static byte[] decapsulate(byte[] encryptedKey, BCMLKEMPrivateKey privateKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        return BaseBCCipher.mlKemDecapsulate(encryptedKey, privateKey, cryptoAlgorithm);
    }

    /**
     * 通讯密钥明文(key)和密文(encapsulation)
     */
    public static class KeyAndEncryptedKey {

        private final byte[] key;
        private final byte[] encryptedKey;

        public KeyAndEncryptedKey(byte[] key, byte[] encryptedKey) {
            this.key = key;
            this.encryptedKey = encryptedKey;
        }

        /**
         * 明文通讯密钥, 32字节
         */
        public byte[] getRawKey() {
            return key;
        }

        /**
         * 通讯密钥密文, 768/1088/1568字节.
         *
         * ML-KEM-512:  768字节
         * ML-KEM-768:  1088字节
         * ML-KEM-1024: 1568字节
         */
        public byte[] getEncryptedKey() {
            return encryptedKey;
        }

        @Override
        public String toString() {
            return "KeyAndEncryptedKey{" +
                    "key=" + ByteUtils.bytesToHex(key) +
                    ", encryptedKey=" + ByteUtils.bytesToHex(encryptedKey) +
                    ", key.length=" + key.length +
                    ", encryptedKey.length=" + encryptedKey.length +
                    '}';
        }
    }

}
