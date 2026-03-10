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
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * [PQC后量子加密 ML-DSA密钥交换算法]
 *
 * ML-DSA测试, 签名验签
 */
public class MlDsaCipherTest {

    @Test
    public void common() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException, IOException {
        // 生成密钥对
        MlDsaKeyGenerator.MlDsaKeyParamsPair keyPair44 = MlDsaKeyGenerator.generateKeyPair44();
        MlDsaKeyGenerator.MlDsaKeyParamsPair keyPair65 = MlDsaKeyGenerator.generateKeyPair65();
        MlDsaKeyGenerator.MlDsaKeyParamsPair keyPair87 = MlDsaKeyGenerator.generateKeyPair87();
//        System.out.println(keyPair44);
//        System.out.println(keyPair65);
//        System.out.println(keyPair87);
//        System.out.println(keyPair44.getPrivateKey().getEncoded().length + " " +  keyPair44.getPublicKey().getEncoded().length);
//        System.out.println(keyPair65.getPrivateKey().getEncoded().length + " " +  keyPair65.getPublicKey().getEncoded().length);
//        System.out.println(keyPair87.getPrivateKey().getEncoded().length + " " +  keyPair87.getPublicKey().getEncoded().length);

        // 解析公私钥
        BCMLDSAPrivateKey privateKey = MlDsaKeyGenerator.generatePrivateKeyByPKCS8(keyPair65.getPKCS8EncodedPrivateKey());
        BCMLDSAPublicKey publicKey = MlDsaKeyGenerator.generatePublicKeyByX509(keyPair65.getX509EncodedPublicKey());
//        System.out.println(privateKey);
//        System.out.println(publicKey);

        byte[] sign44 = MlDsaCipher.sign("hellocipher".getBytes(), keyPair44.getPrivateKey(), MlDsaCipher.SIGN_ALGORITHM_ML_DSA_44);
        byte[] sign65 = MlDsaCipher.sign("hellocipher".getBytes(), keyPair65.getPrivateKey(), MlDsaCipher.SIGN_ALGORITHM_ML_DSA_65);
        byte[] sign87 = MlDsaCipher.sign(new ByteArrayInputStream("hellocipher".getBytes()), keyPair87.getPrivateKey(), MlDsaCipher.SIGN_ALGORITHM_ML_DSA_87);
//        System.out.println(sign44.length + " " + Base64Utils.encodeToString(sign44));
//        System.out.println(sign65.length + " " + Base64Utils.encodeToString(sign65));
//        System.out.println(sign87.length + " " + Base64Utils.encodeToString(sign87));

        Assertions.assertTrue(MlDsaCipher.verify("hellocipher".getBytes(), sign44, keyPair44.getPublicKey(), MlDsaCipher.SIGN_ALGORITHM_ML_DSA_44));
        Assertions.assertTrue(MlDsaCipher.verify("hellocipher".getBytes(), sign65, keyPair65.getPublicKey(), MlDsaCipher.SIGN_ALGORITHM_ML_DSA_65));
        Assertions.assertTrue(MlDsaCipher.verify(new ByteArrayInputStream("hellocipher".getBytes()), sign87, keyPair87.getPublicKey(), MlDsaCipher.SIGN_ALGORITHM_ML_DSA_87));

    }

}
