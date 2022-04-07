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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * <p>RSA加密工具</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * <p>PC端JDK默认加密填充方式为RSA/None/PKCS1Padding，Android默认为RSA/None/NoPadding</p>
 *
 * <p>
 * 性能测试:
 * 安卓单线程/RSA2048
 * 密钥产生:766.3ms 签名:19.8ms 验签:0.7ms
 * </p>
 *
 * @author S.Violet
 */
public class RSACipher {

    /**
     * 签名算法:MD5withRSA
     */
    public static final String SIGN_ALGORITHM_RSA_MD5 = "MD5withRSA";

    /**
     * 签名算法:SHA1withRSA
     */
    public static final String SIGN_ALGORITHM_RSA_SHA1 = "SHA1withRSA";

    /**
     * 签名算法:SHA256withRSA
     */
    public static final String SIGN_ALGORITHM_RSA_SHA256 = "SHA256withRSA";

    /**
     * 加密算法:RSA + ECB/PKCS1Padding
     */
    public static final String CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";

    /**
     * 加密算法:RSA + ECB无填充
     */
    public static final String CRYPTO_ALGORITHM_RSA_ECB_NOPADDING = "RSA/ECB/NoPadding";

    /**
     * 加密算法:RSA + 无填充
     */
    public static final String CRYPTO_ALGORITHM_RSA_NONE_NOPADDING = "RSA/None/NoPadding";

    /**
     * 加密算法:RSA + 无填充
     */
    public static final String CRYPTO_ALGORITHM_RSA = "RSA";

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
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * 如果isMappedByteBufferCanClean返回false, 建议使用verifyIo, 否则操作后, 文件将在一段时间内无法被读写删除<br/>
     *
     * 注意:安卓平台API11以上使用, API10以下会很慢<br/>
     *
     * @param file 被签名的文件
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
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

    /**
     * <p>私钥解密</p>
     *
     * @param data 已加密数据
     * @param privateKey 私钥
     * @param cryptoAlgorithm 加密算法/填充方式: RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1 / RSACipher.CRYPTO_ALGORITHM_RSA_ECB_NOPADDING
     *
     * @return 解密的数据
     * @throws NoSuchPaddingException 填充方式无效(cryptoAlgorithm)
     * @throws NoSuchAlgorithmException 加密算法无效(cryptoAlgorithm)
     * @throws InvalidKeyException 无效的私钥
     * @throws BadPaddingException 填充错误(密码错误)
     * @throws IllegalBlockSizeException 无效的块大小(密码错误?)
     * @throws IOException IO错误
     */
    public static byte[] decrypt(byte[] data, RSAPrivateKey privateKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        return BaseCipher.decryptByRSAPrivateKey(data, privateKey, cryptoAlgorithm);
    }

    /**
     * <p>公钥加密</p>
     *
     * @param data 源数据
     * @param publicKey 公钥
     * @param cryptoAlgorithm 加密算法/填充方式: RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1 / RSACipher.CRYPTO_ALGORITHM_RSA_ECB_NOPADDING
     *
     * @return 加密后的数据
     * @throws NoSuchPaddingException 填充方式无效(cryptoAlgorithm)
     * @throws NoSuchAlgorithmException 加密算法无效(cryptoAlgorithm)
     * @throws InvalidKeyException 无效的私钥
     * @throws BadPaddingException 填充错误(密码错误?)
     * @throws IllegalBlockSizeException 无效的块大小(密码错误?)
     * @throws IOException IO错误
     */
    public static byte[] encrypt(byte[] data, RSAPublicKey publicKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        return BaseCipher.encryptByRSAPublicKey(data, publicKey, cryptoAlgorithm);
    }

    /**
     * <p>公钥解密</p>
     *
     * @param data 已加密数据
     * @param publicKey 公钥
     * @param cryptoAlgorithm 加密算法/填充方式: RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1 / RSACipher.CRYPTO_ALGORITHM_RSA_ECB_NOPADDING
     *
     * @return 解密的数据
     * @throws NoSuchPaddingException 填充方式无效(cryptoAlgorithm)
     * @throws NoSuchAlgorithmException 加密算法无效(cryptoAlgorithm)
     * @throws InvalidKeyException 无效的私钥
     * @throws BadPaddingException 填充错误(密码错误)
     * @throws IllegalBlockSizeException 无效的块大小(密码错误?)
     * @throws IOException IO错误
     */
    public static byte[] decrypt(byte[] data, RSAPublicKey publicKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        return BaseCipher.decryptByRSAPublicKey(data, publicKey, cryptoAlgorithm);
    }

    /**
     * <p>私钥加密</p>
     *
     * @param data 源数据
     * @param privateKey 私钥
     * @param cryptoAlgorithm 加密算法/填充方式: RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1 / RSACipher.CRYPTO_ALGORITHM_RSA_ECB_NOPADDING
     *
     * @return 加密后的数据
     * @throws NoSuchPaddingException 填充方式无效(cryptoAlgorithm)
     * @throws NoSuchAlgorithmException 加密算法无效(cryptoAlgorithm)
     * @throws InvalidKeyException 无效的私钥
     * @throws BadPaddingException 填充错误(密码错误)
     * @throws IllegalBlockSizeException 无效的块大小(密码错误?)
     * @throws IOException IO错误
     */
    public static byte[] encrypt(byte[] data, RSAPrivateKey privateKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        return BaseCipher.encryptByRSAPrivateKey(data, privateKey, cryptoAlgorithm);
    }
}
