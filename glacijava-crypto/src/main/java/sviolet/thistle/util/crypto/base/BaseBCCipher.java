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

import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.conversion.ByteUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.*;

/**
 * [Bouncy castle]加解密基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseBCCipher {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /********************************************************************************************************************************
     * SM4 : Crypto
     ********************************************************************************************************************************/

    /**
     * 加密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 加密(byte[]数据, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
        return cipher.doFinal(data);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
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
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

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
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 解密(byte[]数据, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
        return cipher.doFinal(data);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
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
     * 解密(大文件, 注意, 输入输出流会被关闭, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes, 留空默认0x0000....
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProviderUtils.getProviderName());
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed != null ? ivSeed : new byte[cipher.getBlockSize()]);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

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

    /********************************************************************************************************************************
     * SM2 : Sign Verify
     ********************************************************************************************************************************/

    /**
     * 使用SM2私钥签名数据
     * @param data 待签名数据
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param privateKeyParams 私钥
     * @return 签名数据(DER编码格式)
     */
    public static byte[] signBySM2PrivateKeyParams(byte[] data, byte[] id, ECPrivateKeyParameters privateKeyParams) throws CryptoException {
        if (data == null) {
            return null;
        }
        if (privateKeyParams == null) {
            throw new NullPointerException("privateKeyParams == null");
        }
        SM2Signer signer = new SM2Signer();
        CipherParameters cipherParameters;
        ParametersWithRandom parametersWithRandom = new ParametersWithRandom(privateKeyParams, BaseKeyGenerator.getSystemSecureRandom());
        if (id != null) {
            //set id
            cipherParameters = new ParametersWithID(parametersWithRandom, id);
        } else {
            cipherParameters = parametersWithRandom;
        }
        signer.init(true, cipherParameters);
        signer.update(data, 0, data.length);
        return signer.generateSignature();
    }

    /**
     * 使用SM2私钥签名输入流
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param privateKeyParams 私钥
     * @return 签名数据(DER编码格式)
     */
    public static byte[] signBySM2PrivateKeyParams(InputStream inputStream, byte[] id, ECPrivateKeyParameters privateKeyParams) throws CryptoException, IOException {
        if (inputStream == null) {
            return null;
        }
        if (privateKeyParams == null) {
            throw new NullPointerException("privateKeyParams == null");
        }
        try {
            SM2Signer signer = new SM2Signer();
            CipherParameters cipherParameters;
            ParametersWithRandom parametersWithRandom = new ParametersWithRandom(privateKeyParams, BaseKeyGenerator.getSystemSecureRandom());
            if (id != null) {
                //set id
                cipherParameters = new ParametersWithID(parametersWithRandom, id);
            } else {
                cipherParameters = parametersWithRandom;
            }
            signer.init(true, cipherParameters);
            //handle input stream
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                signer.update(buff, 0, size);
            }
            return signer.generateSignature();
        } finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    /**
     * 使用SM2公钥验签
     * @param data 数据
     * @param sign 签名, (DER编码格式)
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param publicKeyParams 公钥
     * @return true:验签通过
     */
    public static boolean verifyBySM2PublicKeyParams(byte[] data, byte[] sign, byte[] id, ECPublicKeyParameters publicKeyParams) {
        if (data == null) {
            return false;
        }
        if (sign == null) {
            return false;
        }
        if (publicKeyParams == null) {
            throw new NullPointerException("publicKeyParams == null");
        }
        SM2Signer signer = new SM2Signer();
        CipherParameters cipherParameters;
        if (id != null) {
            //set id
            cipherParameters = new ParametersWithID(publicKeyParams, id);
        } else {
            cipherParameters = publicKeyParams;
        }
        signer.init(false, cipherParameters);
        signer.update(data, 0, data.length);
        return signer.verifySignature(sign);
    }

    /**
     * 使用SM2公钥验签
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param sign 签名, (DER编码格式)
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param publicKeyParams 公钥
     * @return true:验签通过
     */
    public static boolean verifyBySM2PublicKeyParams(InputStream inputStream, byte[] sign, byte[] id, ECPublicKeyParameters publicKeyParams) throws IOException {
        if (inputStream == null) {
            return false;
        }
        if (sign == null) {
            return false;
        }
        if (publicKeyParams == null) {
            throw new NullPointerException("publicKeyParams == null");
        }
        try {
            SM2Signer signer = new SM2Signer();
            CipherParameters cipherParameters;
            if (id != null) {
                //set id
                cipherParameters = new ParametersWithID(publicKeyParams, id);
            } else {
                cipherParameters = publicKeyParams;
            }
            signer.init(false, cipherParameters);
            //handle input stream
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                signer.update(buff, 0, size);
            }
            return signer.verifySignature(sign);
        } finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    /********************************************************************************************************************************
     * SM2 : Crypto
     ********************************************************************************************************************************/

    /**
     * 使用SM2公钥加密(密文为C1C2C3格式, 若需要C1C3C2新格式请用工具转换)
     * 密文开头有0x04, 表示未压缩, 有些工具加密的密文前面没有0x04, 可以尝试手工增加或者去掉实现兼容.
     *
     * @param publicKeyParams SM2公钥
     * @param data 原文数据
     * @return 密文, 密文为C1C2C3格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     */
    public static byte[] encryptBySM2PublicKeyParams(byte[] data, ECPublicKeyParameters publicKeyParams) throws InvalidCipherTextException {
        if (data == null) {
            return null;
        }
        if (publicKeyParams == null) {
            throw new NullPointerException("publicKeyParams == null");
        }
        SM2Engine engine = new SM2Engine();
        ParametersWithRandom parametersWithRandom = new ParametersWithRandom(publicKeyParams, BaseKeyGenerator.getSystemSecureRandom());
        engine.init(true, parametersWithRandom);
        return engine.processBlock(data, 0, data.length);
    }

    /**
     * 使用SM2私钥解密(密文为C1C2C3格式, 若需要C1C3C2新格式请用工具转换)
     * 密文开头有0x04, 表示未压缩, 有些工具加密的密文前面没有0x04, 可以尝试手工增加或者去掉实现兼容.
     *
     * @param privateKeyParams SM2私钥
     * @param data 密文数据, 密文为C1C2C3格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * @return 原文
     */
    public static byte[] decryptBySM2PrivateKeyParams(byte[] data, ECPrivateKeyParameters privateKeyParams) throws InvalidCipherTextException {
        if (data == null) {
            return null;
        }
        if (privateKeyParams == null) {
            throw new NullPointerException("privateKeyParams == null");
        }
        SM2Engine engine = new SM2Engine();
        engine.init(false, privateKeyParams);
        return engine.processBlock(data, 0, data.length);
    }

    /********************************************************************************************************************************
     * Conversion
     ********************************************************************************************************************************/

    /**
     * 将SM2算法用于加密的密文格式, 从C1C2C3改为C1C3C2 (默认为C1C2C3).
     * C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * 密文开头有0x04, 表示未压缩, 有些工具加密的密文前面没有0x04, 可以尝试手工增加或者去掉实现兼容.
     *
     * @param cipherText C1C2C3格式的密文
     * @param c1Length C1区域长度, BaseBCAsymKeyGenerator.calculateSM2C1Length(SM2DefaultCurve.DOMAIN_PARAMS)
     * @param c3Length C3区域长度, 即SM3摘要结果长度, SM3DigestCipher.SM3_HASH_LENGTH
     * @return C1C3C2格式的密文
     */
    public static byte[] sm2CiphertextC1C2C3ToC1C3C2(byte[] cipherText, int c1Length, int c3Length){
        if (cipherText == null) {
            return null;
        }
        if (c1Length <= 0) {
            throw new IllegalArgumentException("c1Length <= 0");
        }
        if (c3Length <= 0) {
            throw new IllegalArgumentException("c3Length <= 0");
        }
        byte[] c1 = new byte[c1Length];
        System.arraycopy(cipherText, 0, c1, 0, c1.length);
        byte[] c2 = new byte[cipherText.length - c1.length - c3Length];
        System.arraycopy(cipherText, c1.length, c2, 0, c2.length);
        byte[] c3 = new byte[c3Length];
        System.arraycopy(cipherText, c1.length + c2.length, c3, 0, c3.length);
        return ByteUtils.joint(c1, c3, c2);
    }

    /**
     * 将SM2算法用于加密的密文格式, 从C1C3C2改为C1C2C3 (默认为C1C2C3).
     * C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * 密文开头有0x04, 表示未压缩, 有些工具加密的密文前面没有0x04, 可以尝试手工增加或者去掉实现兼容.
     *
     * @param cipherText C1C3C2格式的密文
     * @param c1Length C1区域长度, BaseBCAsymKeyGenerator.calculateSM2C1Length(SM2DefaultCurve.DOMAIN_PARAMS)
     * @param c3Length C3区域长度, 即SM3摘要结果长度, SM3DigestCipher.SM3_HASH_LENGTH
     * @return C1C2C3格式的密文
     */
    public static byte[] sm2CiphertextC1C3C2ToC1C2C3(byte[] cipherText, int c1Length, int c3Length){
        if (cipherText == null) {
            return null;
        }
        if (c1Length <= 0) {
            throw new IllegalArgumentException("c1Length <= 0");
        }
        if (c3Length <= 0) {
            throw new IllegalArgumentException("c3Length <= 0");
        }
        byte[] c1 = new byte[c1Length];
        System.arraycopy(cipherText, 0, c1, 0, c1.length);
        byte[] c3 = new byte[c3Length];
        System.arraycopy(cipherText, c1.length, c3, 0, c3.length);
        byte[] c2 = new byte[cipherText.length - c1.length - c3.length];
        System.arraycopy(cipherText, c1.length + c3.length, c2, 0, c2.length);
        return ByteUtils.joint(c1, c2, c3);
    }

    /**
     * 将SM2用于加密时的密文C1C2C3转为DER编码数据（SM2密码算法使用规范 GM/T 0009-2012）.
     * C1C2C3格式的密文开头有0x04, 表示未压缩, 有些工具加密的密文前面没有0x04, 可以尝试手工增加或者去掉实现兼容. DER格式前面没0x04.
     *
     * @param c1c2c3 C1C2C3格式的SM2加密密文calculateSM2CurveLength(ECDomainParameters domainParameters)
     * @param curveLength 曲线数据长度, BaseBCCryptoUtils.calculateSM2CurveLength(SM2DefaultCurve.DOMAIN_PARAMS)
     * @param digestLength SM3摘要结果长度, SM3DigestCipher.SM3_HASH_LENGTH
     * @return DER格式的密文
     */
    public static byte[] sm2CipherTextC1C2C3ToDEREncoded(byte[] c1c2c3, int curveLength, int digestLength) throws IOException {
        if (c1c2c3 == null) {
            return null;
        }

        int pos = 1;

        byte[] c1X = new byte[curveLength];
        System.arraycopy(c1c2c3, pos, c1X, 0, c1X.length);
        pos += c1X.length;

        byte[] c1Y = new byte[curveLength];
        System.arraycopy(c1c2c3, pos, c1Y, 0, c1Y.length);
        pos += c1Y.length;

        byte[] c2 = new byte[c1c2c3.length - c1X.length - c1Y.length - 1 - digestLength];
        System.arraycopy(c1c2c3, pos, c2, 0, c2.length);
        pos += c2.length;

        byte[] c3 = new byte[digestLength];
        System.arraycopy(c1c2c3, pos, c3, 0, c3.length);

        ASN1Encodable[] encodable = new ASN1Encodable[4];
        encodable[0] = new ASN1Integer(c1X);
        encodable[1] = new ASN1Integer(c1Y);
        encodable[2] = new DEROctetString(c3);
        encodable[3] = new DEROctetString(c2);
        DERSequence derSequence = new DERSequence(encodable);
        return derSequence.getEncoded(ASN1Encoding.DER);
    }

    /**
     * 将DER编码数据转为SM2用于加密时的密文C1C2C3（SM2密码算法使用规范 GM/T 0009-2012）.
     * C1C2C3格式的密文开头有0x04, 表示未压缩, 有些工具加密的密文前面没有0x04, 可以尝试手工增加或者去掉实现兼容. DER格式前面没0x04.
     *
     * @param der DER格式的密文
     * @return C1C2C3格式密文
     */
    public static byte[] derEncodedToSM2CipherTextC1C2C3(byte[] der) throws CommonCryptoException {
        if (der == null) {
            return null;
        }
        try {

            ASN1Sequence asn1Sequence = DERSequence.getInstance(der);
            byte[] c1X = ((ASN1Integer) asn1Sequence.getObjectAt(0)).getValue().toByteArray();
            byte[] c1Y = ((ASN1Integer) asn1Sequence.getObjectAt(1)).getValue().toByteArray();
            byte[] c3 = ((DEROctetString) asn1Sequence.getObjectAt(2)).getOctets();
            byte[] c2 = ((DEROctetString) asn1Sequence.getObjectAt(3)).getOctets();

            byte[] c1c2c3 = new byte[1 + c1X.length + c1Y.length + c2.length + c3.length];
            c1c2c3[0] = BaseCryptoUtils.SM2_CIPHER_TEXT_PREFIX_UNCOMPRESSED;
            int pos = 1;

            System.arraycopy(c1X, 0, c1c2c3, pos, c1X.length);
            pos += c1X.length;

            System.arraycopy(c1Y, 0, c1c2c3, pos, c1Y.length);
            pos += c1Y.length;

            System.arraycopy(c2, 0, c1c2c3, pos, c2.length);
            pos += c2.length;

            System.arraycopy(c3, 0, c1c2c3, pos, c3.length);

            return c1c2c3;

        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing DER encoded data to SM2 cipher text C1C2C3", e);
        }
    }

    /**
     * 将DER编码的SM2签名数据转为R+S(64bytes)签名数据
     * @param der DER编码的签名数据
     * @return R+S(64bytes)签名数据
     */
    public static byte[] derEncodedToSM2RsSignData(byte[] der) throws CommonCryptoException {
        if (der == null) {
            return null;
        }
        try {
            ASN1Sequence asn1Sequence = DERSequence.getInstance(der);
            byte[] r = ((ASN1Integer) asn1Sequence.getObjectAt(0)).getValue().toByteArray();
            byte[] s = ((ASN1Integer) asn1Sequence.getObjectAt(1)).getValue().toByteArray();
            r = ByteUtils.toLength(r, 32);
            s = ByteUtils.toLength(s, 32);
            byte[] result = new byte[64];
            System.arraycopy(r, 0, result, 0, r.length);
            System.arraycopy(s, 0, result, r.length, s.length);
            return result;
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing DER encoded data to SM2 RS sign data", e);
        }
    }

    /**
     * 将R+S(64bytes)签名数据转为DER编码的SM2签名数据
     * @param signData R+S(64bytes)签名数据
     * @return DER编码的签名数据
     */
    public static byte[] sm2RsSignDataToDerEncoded(byte[] signData) throws IOException {
        if (signData == null) {
            return null;
        }
        if (signData.length != 64) {
            throw new IllegalArgumentException("signData length != 64");
        }
        BigInteger r = new BigInteger(1, ByteUtils.sub(signData, 0, 32));
        BigInteger s = new BigInteger(1, ByteUtils.sub(signData, 32, 32));
        ASN1EncodableVector encodableVector = new ASN1EncodableVector();
        encodableVector.add(new ASN1Integer(r));
        encodableVector.add(new ASN1Integer(s));
        return new DERSequence(encodableVector).getEncoded(ASN1Encoding.DER);
    }

}
