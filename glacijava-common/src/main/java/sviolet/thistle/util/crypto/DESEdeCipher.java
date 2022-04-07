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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * DESede加密工具(3DES)
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * Created by S.Violet on 2016/12/20.
 *
 * @author S.Violet
 */
public class DESEdeCipher {

    /**
     * 密钥类型:DESede
     */
    public static final String KEY_ALGORITHM = "DESede";

    /**
     * 加密算法:DESede + ECB/PKCS5Padding
     */
    public static final String CRYPTO_ALGORITHM_DES_EDE_ECB_PKCS5PADDING = "DESede/ECB/PKCS5Padding";

    /**
     * 加密算法:DESede + ECB无填充
     */
    public static final String CRYPTO_ALGORITHM_DES_EDE_ECB_NOPADDING = "DESede/ECB/NoPadding";

    /**
     * 加密算法:DESede + CBC/PKCS5Padding
     */
    public static final String CRYPTO_ALGORITHM_DES_EDE_CBC_PKCS5PADDING = "DESede/CBC/PKCS5Padding";

    /**
     * 加密算法:DESede + CBC无填充
     */
    public static final String CRYPTO_ALGORITHM_DES_EDE_CBC_NOPADDING = "DESede/CBC/NoPadding";

    /**
     * 加密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] encrypt(byte[] data, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        return BaseCipher.encrypt(data, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 加密(byte[]数据, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param ivSeed iv初始化向量, 例如:"1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] encryptCBC(byte[] data, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        return BaseCipher.encryptCBC(data, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void encrypt(InputStream in, OutputStream out, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        BaseCipher.encrypt(in, out, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param ivSeed iv初始化向量, 例如:"1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void encryptCBC(InputStream in, OutputStream out, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        BaseCipher.encryptCBC(in, out, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

    /**
     * 解密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] decrypt(byte[] data, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        return BaseCipher.decrypt(data, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 解密(byte[]数据, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param ivSeed iv初始化向量, "1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static byte[] decryptCBC(byte[] data, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        return BaseCipher.decryptCBC(data, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        BaseCipher.decrypt(in, out, key, KEY_ALGORITHM, cryptoAlgorithm);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit), 64bit为DES, 192bit为DESEDE, 若为16bytes秘钥， 则在后面补上前8bytes(AAAAAAAABBBBBBBB -> AAAAAAAABBBBBBBBAAAAAAAA)
     * @param ivSeed iv初始化向量, "1234567812345678".getBytes("UTF-8"), 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void decryptCBC(InputStream in, OutputStream out, byte[] key, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        //自动将两倍长DesEde密钥转为三倍长(如果是两倍长的话)
        key = DESKeyGenerator.doubleDesEdeKeyToTriple(key);
        BaseCipher.decryptCBC(in, out, key, KEY_ALGORITHM, ivSeed, cryptoAlgorithm);
    }

}
