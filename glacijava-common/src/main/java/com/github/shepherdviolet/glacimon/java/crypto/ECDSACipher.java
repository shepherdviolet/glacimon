/*
 * Copyright (C) 2022-2022 S.Violet
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

import com.github.shepherdviolet.glacimon.java.crypto.base.BaseCipher;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;

/**
 * <p>ECDSA签名验签工具</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * <p>
 * 性能测试:
 * 安卓单线程/secp256r1
 * 密钥产生:0.3ms 签名:0.8ms 验签:1.7ms
 * </p>
 *
 * @author shepherdviolet
 */
public class ECDSACipher {

    /**
     * 签名算法:SHA256withECDSA
     */
    public static final String SIGN_ALGORITHM_ECDSA_SHA256 = "SHA256withECDSA";

    /**
     * 创建签名的实例
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     */
    public static Signature generateSignatureInstance(PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        return BaseCipher.generateSignatureInstance(privateKey, signAlgorithm);
    }

    /**
     * 创建验签的实例
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     */
    public static Signature generateSignatureInstance(PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        return BaseCipher.generateSignatureInstance(publicKey, signAlgorithm);
    }

    /**
     * 创建验签的实例
     * @param certificate 证书
     * @param signAlgorithm 签名逻辑
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     */
    public static Signature generateSignatureInstance(Certificate certificate, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        return BaseCipher.generateSignatureInstance(certificate, signAlgorithm);
    }

    /**
     * 用私钥对信息生成数字签名<p>
     *  
     * @param data 需要签名的数据
     * @param privateKey 私钥(ECDSA)
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *  
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */  
    public static byte[] sign(byte[] data, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        return BaseCipher.sign(data, privateKey, signAlgorithm);
    }

    /**
     * <p>用私钥对信息生成数字签名</p>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥(ECDSA)
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static byte[] sign(File file, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        return BaseCipher.sign(file, privateKey, signAlgorithm);
    }

    /**
     * <p>用公钥验证数字签名</p>
     *  
     * @param data 被签名的数据
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *  
     * @return true:数字签名有效
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     *  
     */  
    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        return BaseCipher.verify(data, sign, publicKey, signAlgorithm);
    }

    /**
     * <p>用公钥验证数字签名</p>
     *
     * @param file 被签名的文件
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return true:数字签名有效
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static boolean verify(File file, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        return BaseCipher.verify(file, sign, publicKey, signAlgorithm);
    }

}
