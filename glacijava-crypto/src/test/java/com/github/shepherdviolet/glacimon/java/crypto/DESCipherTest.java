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

import com.github.shepherdviolet.glacimon.java.crypto.DESCipher;
import com.github.shepherdviolet.glacimon.java.crypto.DESKeyGenerator;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class DESCipherTest {

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";

    /**
     * byte[]加解密
     */
    @Test
    public void bytesCrypto() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateDes64();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = DESCipher.encrypt(dataBytes, key, DESCipher.CRYPTO_ALGORITHM_DES_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = DESCipher.decrypt(encrypted, key, DESCipher.CRYPTO_ALGORITHM_DES_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * byte[]加解密, CBC填充
     */
    @Test
    public void bytesCryptoCBC() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateShaKey64("wowowo".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = DESCipher.encryptCBC(dataBytes, key, "12345678".getBytes(), DESCipher.CRYPTO_ALGORITHM_DES_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = DESCipher.decryptCBC(encrypted, key, "12345678".getBytes(), DESCipher.CRYPTO_ALGORITHM_DES_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCrypto() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateDes64(new SecureRandom());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DESCipher.encrypt(in, out, key, DESCipher.CRYPTO_ALGORITHM_DES_ECB_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        DESCipher.decrypt(in, out, key, DESCipher.CRYPTO_ALGORITHM_DES_ECB_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCryptoCBC() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateShaKey64("wowowo".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DESCipher.encryptCBC(in, out, key, "12345678".getBytes(), DESCipher.CRYPTO_ALGORITHM_DES_CBC_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        DESCipher.decryptCBC(in, out, key, "12345678".getBytes(), DESCipher.CRYPTO_ALGORITHM_DES_CBC_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

}
