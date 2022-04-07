/*
 * Copyright (C) 2015-2017 S.Violet
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

import sviolet.thistle.util.crypto.base.BaseCipher;

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
 * @author S.Violet
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
     * <p>用私钥对信息生成数字签名, 根据运行时环境选择使用NIO或IO方式</p>
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
     * <p>用私钥对信息生成数字签名(NIO)</p>
     *
     * 注意:非安卓平台使用该方法前, 请使用FileUtils.isMappedByteBufferCanClean()判断MappedByteBuffer是否能被手动回收,
     * 如果isMappedByteBufferCanClean返回false, 建议使用signIo, 否则操作后, 文件将在一段时间内无法被读写删除<br/>
     *
     * 注意:安卓平台API11以上使用, API10以下会很慢<br/>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static byte[] signNio(File file, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        return BaseCipher.signNio(file, privateKey, signAlgorithm);
    }

    /**
     * <p>用私钥对信息生成数字签名(IO)</p>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static byte[] signIo(File file, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        return BaseCipher.signIo(file, privateKey, signAlgorithm);
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
     * <p>用公钥验证数字签名, 根据运行时环境选择使用NIO或IO方式</p>
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

    /**
     * <p>用公钥验证数字签名(NIO)</p>
     *
     * 注意:非安卓平台使用该方法前, 请使用FileUtils.isMappedByteBufferCanClean()判断MappedByteBuffer是否能被手动回收,
     * 如果isMappedByteBufferCanClean返回false, 建议使用signIo, 否则操作后, 文件将在一段时间内无法被读写删除<br/>
     *
     * 注意:安卓平台API11以上使用, API10以下会很慢<br/>
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
     *
     */
    public static boolean verifyNio(File file, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        return BaseCipher.verifyNio(file, sign, publicKey, signAlgorithm);
    }

    /**
     * <p>用公钥验证数字签名(IO)</p>
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
     *
     */
    public static boolean verifyIo(File file, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        return BaseCipher.verifyIo(file, sign, publicKey, signAlgorithm);
    }

}
