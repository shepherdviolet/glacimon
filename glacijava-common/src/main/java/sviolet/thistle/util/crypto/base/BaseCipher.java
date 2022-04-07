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

package sviolet.thistle.util.crypto.base;

import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.common.PlatformUtils;
import sviolet.thistle.util.file.FileUtils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * 加解密基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
// About suppressed warnings: It's a util, the algorithm type should not be restricted
@SuppressWarnings({"lgtm[java/weak-cryptographic-algorithm]"})
public class BaseCipher {

    /**
     * 加密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        if (data == null){
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 加密(byte[]数据, 使用CBC模式时需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, AES 16 bytes, DES 8bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        if (data == null){
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        IvParameterSpec iv = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * 加密(byte[]数据, 使用GCM模式)
     *
     * @param data 数据
     * @param aad 附加验证数据(Additional Authenticated Data)
     * @param iv 初始化向量, AES 16 bytes, DES 8bytes
     * @param tagLength 附加验证数据标签长度(bit), 32, 64, 96, 104, 112, 120, 128
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法, AES
     * @param cryptoAlgorithm 加密算法/填充算法, AES/GCM/NoPadding
     */
    public static byte[] encryptGCM(byte[] data, byte[] aad, byte[] iv, int tagLength, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        if (data == null){
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        cipher.updateAAD(aad);
        return cipher.doFinal(data);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用CBC模式时需要用该方法并指定iv初始化向量)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, AES 16 bytes, DES 8bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            IvParameterSpec iv = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用GCM模式)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param aad 附加验证数据(Additional Authenticated Data)
     * @param iv 初始化向量, AES 16 bytes, DES 8bytes
     * @param tagLength 附加验证数据标签长度(bit), 32, 64, 96, 104, 112, 120, 128
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法, AES
     * @param cryptoAlgorithm 加密算法/填充算法, AES/GCM/NoPadding
     */
    public static void encryptGCM(InputStream in, OutputStream out, byte[] aad, byte[] iv, int tagLength, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(aad);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 解密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        if (data == null){
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 解密(byte[]数据, CBC模式需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, AES 16 bytes, DES 8bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        if (data == null){
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        IvParameterSpec iv = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * 解密(byte[]数据, 使用GCM模式)
     *
     * @param data 数据
     * @param aad 附加验证数据(Additional Authenticated Data)
     * @param iv 初始化向量, AES 16 bytes, DES 8bytes
     * @param tagLength 附加验证数据标签长度(bit), 32, 64, 96, 104, 112, 120, 128
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法, AES
     * @param cryptoAlgorithm 加密算法/填充算法, AES/GCM/NoPadding
     */
    public static byte[] decryptGCM(byte[] data, byte[] aad, byte[] iv, int tagLength, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        if (data == null){
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        cipher.updateAAD(aad);
        return cipher.doFinal(data);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭, CBC模式需要用该方法并指定iv初始化向量)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, AES 16 bytes, DES 8bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            IvParameterSpec iv = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭, 使用GCM模式)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param aad 附加验证数据(Additional Authenticated Data)
     * @param iv 初始化向量, AES 16 bytes, DES 8bytes
     * @param tagLength 附加验证数据标签长度(bit), 32, 64, 96, 104, 112, 120, 128
     * @param key 秘钥(AES:128/256bit, DES:64/192bit)
     * @param keyAlgorithm 秘钥算法, AES
     * @param cryptoAlgorithm 加密算法/填充算法, AES/GCM/NoPadding
     */
    public static void decryptGCM(InputStream in, OutputStream out, byte[] aad, byte[] iv, int tagLength, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(aad);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /********************************************************************************************************************************
     ********************************************************************************************************************************
     *
     * RSA / ECDSA : Sign Verify
     *
     ********************************************************************************************************************************
     ********************************************************************************************************************************/

    /**
     * 创建签名的实例
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑
     */
    public static Signature generateSignatureInstance(PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initSign(privateKey);
        return signature;
    }

    /**
     * 创建验签的实例
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑
     */
    public static Signature generateSignatureInstance(PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initVerify(publicKey);
        return signature;
    }

    /**
     * 创建验签的实例
     * @param certificate 证书
     * @param signAlgorithm 签名逻辑
     */
    public static Signature generateSignatureInstance(Certificate certificate, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initVerify(certificate);
        return signature;
    }

    /**
     * 用私钥对信息生成数字签名<p>
     *
     * @param data 需要签名的数据
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑
     *
     * @return 数字签名
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        if (data == null){
            return null;
        }
        Signature signature = generateSignatureInstance(privateKey, signAlgorithm);
        signature.update(data);
        return signature.sign();
    }

    /**
     * <p>用私钥对信息生成数字签名, 根据运行时环境选择使用NIO或IO方式</p>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑
     *
     * @return 数字签名
     */
    public static byte[] sign(File file, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK){
            //安卓API11以上使用NIO, API10以下会很慢
            if (PlatformUtils.ANDROID_VERSION < CryptoConstants.ANDROID_API11){
                return signIo(file, privateKey, signAlgorithm);
            } else {
                return signNio(file, privateKey, signAlgorithm);
            }
        }
        //能手动回收MappedByteBuffer则使用NIO
        if (FileUtils.isMappedByteBufferCanClean()){
            return signNio(file, privateKey, signAlgorithm);
        } else {
            return signIo(file, privateKey, signAlgorithm);
        }
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
     * @param signAlgorithm 签名逻辑
     *
     * @return 数字签名
     */
    public static byte[] signNio(File file, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        FileChannel channel = null;
        MappedByteBuffer byteBuffer = null;
        try {
            inputStream = new FileInputStream(file);
            channel = inputStream.getChannel();
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            Signature signature = generateSignatureInstance(privateKey, signAlgorithm);
            signature.update(byteBuffer);
            return signature.sign();
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (channel != null){
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
            //尝试将MappedByteBuffer回收, 解决后续文件无法被读写删除的问题
            FileUtils.cleanMappedByteBuffer(byteBuffer);
        }
    }

    /**
     * <p>用私钥对信息生成数字签名(IO)</p>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑
     *
     * @return 数字签名
     */
    public static byte[] signIo(File file, PrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            Signature signature = generateSignatureInstance(privateKey, signAlgorithm);
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                signature.update(buff, 0, size);
            }
            return signature.sign();
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * <p>用公钥验证数字签名</p>
     *
     * @param data 被签名的数据
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑
     *
     * @return true:数字签名有效
     *
     */
    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        if (data == null){
            return false;
        }
        Signature signature = generateSignatureInstance(publicKey, signAlgorithm);
        signature.update(data);
        return signature.verify(sign);
    }

    /**
     * <p>用公钥验证数字签名, 根据运行时环境选择使用NIO或IO方式</p>
     *
     * @param file 被签名的文件
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑
     *
     * @return true:数字签名有效
     */
    public static boolean verify(File file, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK){
            //安卓API11以上使用NIO, API10以下会很慢
            if (PlatformUtils.ANDROID_VERSION < CryptoConstants.ANDROID_API11){
                return verifyIo(file, sign, publicKey, signAlgorithm);
            } else {
                return verifyNio(file, sign, publicKey, signAlgorithm);
            }
        }
        //能手动回收MappedByteBuffer则使用NIO
        if (FileUtils.isMappedByteBufferCanClean()){
            return verifyNio(file, sign, publicKey, signAlgorithm);
        } else {
            return verifyIo(file, sign, publicKey, signAlgorithm);
        }
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
     * @param signAlgorithm 签名逻辑
     *
     * @return true:数字签名有效
     */
    public static boolean verifyNio(File file, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        FileChannel channel = null;
        MappedByteBuffer byteBuffer = null;
        try {
            inputStream = new FileInputStream(file);
            channel = inputStream.getChannel();
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            Signature signature = generateSignatureInstance(publicKey, signAlgorithm);
            signature.update(byteBuffer);
            return signature.verify(sign);
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (channel != null){
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
            //尝试将MappedByteBuffer回收, 解决后续文件无法被读写删除的问题
            FileUtils.cleanMappedByteBuffer(byteBuffer);
        }
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
     */
    public static boolean verifyIo(File file, byte[] sign, PublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            Signature signature = generateSignatureInstance(publicKey, signAlgorithm);
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                signature.update(buff, 0, size);
            }
            return signature.verify(sign);
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /********************************************************************************************************************************
     ********************************************************************************************************************************
     *
     * RSA : Crypto
     *
     ********************************************************************************************************************************
     ********************************************************************************************************************************/

    /**
     * <p>私钥解密</p>
     *
     * @param data 已加密数据
     * @param privateKey 私钥
     * @param cryptoAlgorithm 加密算法/填充方式
     *
     * @return 解密的数据
     */
    public static byte[] decryptByRSAPrivateKey(byte[] data, RSAPrivateKey privateKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

        if (data == null){
            return null;
        }

        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        int dataLength = data.length;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int offset = 0;
        byte[] buffer;
        //解密块和密钥等长
        int blockSize = privateKey.getModulus().bitLength() / 8;

        // 对数据分段解密
        while (dataLength - offset > 0) {
            if (dataLength - offset > blockSize) {
                buffer = cipher.doFinal(data, offset, blockSize);
            } else {
                buffer = cipher.doFinal(data, offset, dataLength - offset);
            }
            outputStream.write(buffer, 0, buffer.length);
            offset += blockSize;
        }
        return outputStream.toByteArray();
    }

    /**
     * <p>公钥加密</p>
     *
     * @param data 源数据
     * @param publicKey 公钥
     * @param cryptoAlgorithm 加密算法/填充方式
     *
     * @return 加密后的数据
     */
    public static byte[] encryptByRSAPublicKey(byte[] data, RSAPublicKey publicKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

        if (data == null){
            return null;
        }

        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int dataLength = data.length;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buffer;
        //加密块比密钥长度小11
        int blockSize = publicKey.getModulus().bitLength() / 8 - 11;

        // 对数据分段加密
        while (dataLength - offSet > 0) {
            if (dataLength - offSet > blockSize) {
                buffer = cipher.doFinal(data, offSet, blockSize);
            } else {
                buffer = cipher.doFinal(data, offSet, dataLength - offSet);
            }
            outputStream.write(buffer, 0, buffer.length);
            offSet += blockSize;
        }
        return outputStream.toByteArray();
    }

    /**
     * <p>公钥解密</p>
     *
     * @param data 已加密数据
     * @param publicKey 公钥
     * @param cryptoAlgorithm 加密算法/填充方式
     *
     * @return 解密的数据
     */
    public static byte[] decryptByRSAPublicKey(byte[] data, RSAPublicKey publicKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

        if (data == null){
            return null;
        }

        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        int dataLength = data.length;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buffer;
        //解密块和密钥等长
        int blockSize = publicKey.getModulus().bitLength() / 8;

        // 对数据分段解密
        while (dataLength - offSet > 0) {
            if (dataLength - offSet > blockSize) {
                buffer = cipher.doFinal(data, offSet, blockSize);
            } else {
                buffer = cipher.doFinal(data, offSet, dataLength - offSet);
            }
            outputStream.write(buffer, 0, buffer.length);
            offSet += blockSize;
        }
        return outputStream.toByteArray();
    }

    /**
     * <p>私钥加密</p>
     *
     * @param data 源数据
     * @param privateKey 私钥
     * @param cryptoAlgorithm 加密算法/填充方式
     *
     * @return 加密后的数据
     */
    public static byte[] encryptByRSAPrivateKey(byte[] data, RSAPrivateKey privateKey, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

        if (data == null){
            return null;
        }

        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        int dataLength = data.length;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buffer;
        //加密块比密钥长度小11
        int blockSize = privateKey.getModulus().bitLength() / 8 - 11;

        // 对数据分段加密
        while (dataLength - offSet > 0) {
            if (dataLength - offSet > blockSize) {
                buffer = cipher.doFinal(data, offSet, blockSize);
            } else {
                buffer = cipher.doFinal(data, offSet, dataLength - offSet);
            }
            outputStream.write(buffer, 0, buffer.length);
            offSet += blockSize;
        }
        return outputStream.toByteArray();
    }

}
