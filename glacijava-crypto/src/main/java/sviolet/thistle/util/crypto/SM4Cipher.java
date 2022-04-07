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

package sviolet.thistle.util.crypto;

import sviolet.thistle.util.crypto.base.BaseBCCipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 
 * <p>SM4加解密工具</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 * 
 * @author S.Violet
 */
public class SM4Cipher {

    /**
     * 密钥类型:SM4
     */
    public static final String KEY_ALGORITHM = "SM4";

    /**
     * 加密算法:SM4 + ECB/PKCS5Padding填充
     */
    public static final String CRYPTO_ALGORITHM_SM4_ECB_PKCS5PADDING = "SM4/ECB/PKCS5Padding";

    /**
     * 加密算法:SM4 + CBC/PKCS5Padding填充
     */
    public static final String CRYPTO_ALGORITHM_SM4_CBC_PKCS5PADDING = "SM4/CBC/PKCS5Padding";

    /**
     * 加密算法:SM4 + CBC/PKCS7Padding填充
     */
    public static final String CRYPTO_ALGORITHM_SM4_CBC_PKCS7PADDING = "SM4/CBC/PKCS7Padding";

    /**
     * 加密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128bit)
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encrypt(byte[] data, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        return BaseBCCipher.encrypt(data, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 加密(byte[]数据, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128bit)
     * @param ivSeed iv初始化向量, 16 bytes, 例如:"1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encryptCBC(byte[] data, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        return BaseBCCipher.encryptCBC(data, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(SM4:128bit)
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encrypt(InputStream in, OutputStream out, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, NoSuchProviderException {
        BaseBCCipher.encrypt(in, out, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(SM4:128bit)
     * @param ivSeed iv初始化向量, 16 bytes, 例如:"1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encryptCBC(InputStream in, OutputStream out, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        BaseBCCipher.encryptCBC(in, out, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

    /**
     * 解密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128bit)
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decrypt(byte[] data, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        return BaseBCCipher.decrypt(data, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 解密(byte[]数据, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128bit)
     * @param ivSeed iv初始化向量, 16 bytes, "1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decryptCBC(byte[] data, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        return BaseBCCipher.decryptCBC(data, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(SM4:128bit)
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, NoSuchProviderException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        BaseBCCipher.decrypt(in, out, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(SM4:128bit)
     * @param ivSeed iv初始化向量, 16 bytes, "1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decryptCBC(InputStream in, OutputStream out, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        BaseBCCipher.decryptCBC(in, out, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

}
