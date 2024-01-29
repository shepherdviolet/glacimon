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

import com.github.shepherdviolet.glacimon.java.crypto.ECDSACipher;
import com.github.shepherdviolet.glacimon.java.crypto.ECDSAKeyGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

public class ECDSACipherTest {

    private static final String TEST_FILE = "../LICENSE";

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";
    private static final String PUBLIC = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEfcug7j3ywRhkl343yFKr8FzWyvkYlOCangIV14taWmxRqVeFDtuED7PmKHtpL/zeb39D/54c/dn+3+awriF7yA==";
    private static final String PRIVATE = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA11phMdcjlF8pa9it4J0Hai933g0qtA9C5ga8v99a73w==";

    @Test
    public void bytesSignVerify() throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        ECDSAKeyGenerator.ECKeyPair pair = ECDSAKeyGenerator.generateKeyPair();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(pair);

        byte[] sign = ECDSACipher.sign(dataBytes, pair.getPrivateKey(), ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256);

//        System.out.println(ByteUtils.bytesToHex(sign));

        Assertions.assertEquals(true, ECDSACipher.verify(dataBytes, sign, pair.getPublicKey(), ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256));

    }

    @Test
    public void fileIoSignVerify() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {

        ECPrivateKey privateKey = ECDSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(PRIVATE));
        ECPublicKey publicKey = ECDSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(PUBLIC));

        byte[] sign = ECDSACipher.signIo(new File(TEST_FILE), privateKey, ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256);

//        System.out.println(ByteUtils.bytesToHex(sign));

        Assertions.assertEquals(true, ECDSACipher.verifyIo(new File(TEST_FILE), sign, publicKey, ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256));

    }

    @Test
    public void fileNioSignVerify() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {

        ECPrivateKey privateKey = ECDSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(PRIVATE));
        ECPublicKey publicKey = ECDSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(PUBLIC));

        byte[] sign = ECDSACipher.signNio(new File(TEST_FILE), privateKey, ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256);

//        System.out.println(ByteUtils.bytesToHex(sign));

        Assertions.assertEquals(true, ECDSACipher.verifyNio(new File(TEST_FILE), sign, publicKey, ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256));

    }

}
