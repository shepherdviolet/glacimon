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

import com.github.shepherdviolet.glacimon.java.crypto.base.BaseBCCipher;
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPublicKey;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

/**
 * [PQC后量子加密 ML-DSA签名算法]
 * ML-DSA签名验签工具
 */
public class MlDsaCipher {

    public static final String SIGN_ALGORITHM_ML_DSA_44 = "ML-DSA-44";
    public static final String SIGN_ALGORITHM_ML_DSA_65 = "ML-DSA-65";
    public static final String SIGN_ALGORITHM_ML_DSA_87 = "ML-DSA-87";

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 使用ML-DSA私钥签名数据
     *
     * 签名长度:
     * ML-DSA-44 2420字节
     * ML-DSA-65 3309字节
     * ML-DSA-87 4627字节
     *
     * @param data 待签名数据
     * @param privateKey 私钥
     * @param signAlgorithm 签名算法 ML-DSA-44, ML-DSA-65, ML-DSA-87
     */
    public static byte[] sign(byte[] data, BCMLDSAPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return BaseBCCipher.mlDsaSign(data, privateKey, signAlgorithm);
    }

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 使用ML-DSA私钥签名输入流
     *
     * 签名长度:
     * ML-DSA-44 2420字节
     * ML-DSA-65 3309字节
     * ML-DSA-87 4627字节
     *
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param privateKey 私钥
     * @param signAlgorithm 签名算法 ML-DSA-44, ML-DSA-65, ML-DSA-87
     */
    public static byte[] sign(InputStream inputStream, BCMLDSAPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidKeyException {
        return BaseBCCipher.mlDsaSign(inputStream, privateKey, signAlgorithm);
    }

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 使用ML-DSA公钥验签
     * @param data 数据
     * @param sign 签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名算法 ML-DSA-44, ML-DSA-65, ML-DSA-87
     * @return true:验签通过
     */
    public static boolean verify(byte[] data, byte[] sign, BCMLDSAPublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return BaseBCCipher.mlDsaVerify(data, sign, publicKey, signAlgorithm);
    }

    /**
     * [PQC后量子加密 ML-DSA签名算法]
     * 使用ML-DSA公钥验签
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param sign 签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名算法 ML-DSA-44, ML-DSA-65, ML-DSA-87
     * @return true:验签通过
     */
    public static boolean verify(InputStream inputStream, byte[] sign, BCMLDSAPublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidKeyException {
        return BaseBCCipher.mlDsaVerify(inputStream, sign, publicKey, signAlgorithm);
    }

}
