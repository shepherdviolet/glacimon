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

import org.bouncycastle.jcajce.provider.asymmetric.mlkem.BCMLKEMPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mlkem.BCMLKEMPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * [PQC后量子加密 ML-KEM密钥交换算法]
 *
 * ML-KEM测试, 注意这个算法不能用来加密任何数据, 通讯密钥只能在加密时随机生成
 */
public class MlKemCipherTest {

    @Test
    public void common() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // 生成密钥对
        MlKemKeyGenerator.MlKemKeyParamsPair keyPair512 = MlKemKeyGenerator.generateKeyPair512();
        MlKemKeyGenerator.MlKemKeyParamsPair keyPair768 = MlKemKeyGenerator.generateKeyPair768();
        MlKemKeyGenerator.MlKemKeyParamsPair keyPair1024 = MlKemKeyGenerator.generateKeyPair1024();
//        System.out.println(keyPair512);
//        System.out.println(keyPair768);
//        System.out.println(keyPair1024);

        // 解析公私钥
        BCMLKEMPrivateKey privateKey = MlKemKeyGenerator.generatePrivateKeyByPKCS8(keyPair768.getPKCS8EncodedPrivateKey());
        BCMLKEMPublicKey publicKey = MlKemKeyGenerator.generatePublicKeyByX509(keyPair768.getX509EncodedPublicKey());
//        System.out.println(privateKey);
//        System.out.println(publicKey);

        // 用公钥生成32字节通讯密钥并加密
        MlKemCipher.KeyAndEncryptedKey keyAndEncryptedKey512 = MlKemCipher.encapsulate(keyPair512.getPublicKey(), MlKemCipher.CRYPTO_ALGORITHM_ML_KEM_512);
        MlKemCipher.KeyAndEncryptedKey keyAndEncryptedKey768 = MlKemCipher.encapsulate(keyPair768.getPublicKey(), MlKemCipher.CRYPTO_ALGORITHM_ML_KEM_768);
        MlKemCipher.KeyAndEncryptedKey keyAndEncryptedKey1024 = MlKemCipher.encapsulate(keyPair1024.getPublicKey(), MlKemCipher.CRYPTO_ALGORITHM_ML_KEM_1024);
        System.out.println(keyAndEncryptedKey512);
        System.out.println(keyAndEncryptedKey768);
        System.out.println(keyAndEncryptedKey1024);

        // 用私钥解密通讯密钥
        byte[] plain = MlKemCipher.decapsulate(keyAndEncryptedKey768.getEncryptedKey(), keyPair768.getPrivateKey(), MlKemCipher.CRYPTO_ALGORITHM_ML_KEM_768);
        Assertions.assertArrayEquals(plain, keyAndEncryptedKey768.getRawKey());

    }

}
