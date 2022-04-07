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

import org.junit.Assert;
import org.junit.Test;

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

public class SM4CipherTest {

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";

    /**
     * byte[]加解密
     */
    @Test
    public void bytesCrypto() throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = SM4KeyGenerator.generate128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = SM4Cipher.encrypt(dataBytes, key, SM4Cipher.CRYPTO_ALGORITHM_SM4_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = SM4Cipher.decrypt(encrypted, key, SM4Cipher.CRYPTO_ALGORITHM_SM4_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * byte[]加解密, CBC填充
     */
    @Test
    public void bytesCryptoCBC() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        //生成密钥
        byte[] key = SM4KeyGenerator.generate128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        //加密
        byte[] encrypted = SM4Cipher.encryptCBC(dataBytes, key, "1234567890123456".getBytes(), SM4Cipher.CRYPTO_ALGORITHM_SM4_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        //解密
        byte[] decrypted = SM4Cipher.decryptCBC(encrypted, key, "1234567890123456".getBytes(), SM4Cipher.CRYPTO_ALGORITHM_SM4_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCrypto() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        //生成密钥
        byte[] key = SM4KeyGenerator.generate128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        //加密(支持大文件)
        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SM4Cipher.encrypt(in, out, key, SM4Cipher.CRYPTO_ALGORITHM_SM4_ECB_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        //解密(支持大文件)
        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        SM4Cipher.decrypt(in, out, key, SM4Cipher.CRYPTO_ALGORITHM_SM4_ECB_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCryptoCBC() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        //生成密钥
        byte[] key = SM4KeyGenerator.generate128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        //加密CBC填充(支持大文件)
        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SM4Cipher.encryptCBC(in, out, key, "1234567890123456".getBytes(), SM4Cipher.CRYPTO_ALGORITHM_SM4_CBC_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        //解密CBC填充(支持大文件)
        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        SM4Cipher.decryptCBC(in, out, key, "1234567890123456".getBytes(), SM4Cipher.CRYPTO_ALGORITHM_SM4_CBC_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

}
