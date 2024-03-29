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

import com.github.shepherdviolet.glacimon.java.crypto.AESCipher;
import com.github.shepherdviolet.glacimon.java.crypto.AESKeyGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class AESCipherTest {

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";

    /**
     * byte[]加解密
     */
    @Test
    public void bytesCrypto() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = AESKeyGenerator.generateAes128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = AESCipher.encrypt(dataBytes, key, AESCipher.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = AESCipher.decrypt(encrypted, key, AESCipher.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assertions.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * byte[]加解密, CBC填充
     */
    @Test
    public void bytesCryptoCBC() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = AESKeyGenerator.generateAes128("cbcbc".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = AESCipher.encryptCBC(dataBytes, key, "1234567890123456".getBytes(), AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = AESCipher.decryptCBC(encrypted, key, "1234567890123456".getBytes(), AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assertions.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCrypto() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = AESKeyGenerator.generateShaKey128("key".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AESCipher.encrypt(in, out, key, AESCipher.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        AESCipher.decrypt(in, out, key, AESCipher.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assertions.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCryptoCBC() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = AESKeyGenerator.generateShaKey128("key".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AESCipher.encryptCBC(in, out, key, "1234567890123456".getBytes(), AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        AESCipher.decryptCBC(in, out, key, "1234567890123456".getBytes(), AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assertions.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * byte[]加解密, GCM模式 + 256位, 256位要求JDK8_162以上!!!
     */
    @Test
    public void bytesCryptoGcm() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = AESKeyGenerator.generateAes256();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = AESCipher.encryptGCM(
                dataBytes,
                "timestamp or others".getBytes(),
                "1234567890123456".getBytes(),
                128,
                key,
                AESCipher.CRYPTO_ALGORITHM_AES_GCM_NOPADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = AESCipher.decryptGCM(
                encrypted,
                "timestamp or others".getBytes(),
                "1234567890123456".getBytes(),
                128,
                key,
                AESCipher.CRYPTO_ALGORITHM_AES_GCM_NOPADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));
//        System.out.println(new String(decrypted, "UTF-8"));

        Assertions.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

}
