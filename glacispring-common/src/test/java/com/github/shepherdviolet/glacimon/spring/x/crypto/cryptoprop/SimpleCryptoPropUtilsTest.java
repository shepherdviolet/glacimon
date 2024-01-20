/*
 * Copyright (C) 2022-2024 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop;

import org.junit.Assert;
import org.junit.Test;

/**
 * SimpleCryptoPropUtils测试
 */
public class SimpleCryptoPropUtilsTest {

    @Test
    public void test() {

        // 生成密钥
//        System.out.println("====AES密钥=========================================================");
//        System.out.println("aesKey:\n" + SimpleCryptoPropUtils.generateAesKey());
//        System.out.println("====RSA密钥=========================================================");
//        System.out.println(SimpleCryptoPropUtils.generateRsaKeyPair());
//        System.out.println("===================================================================");

        // 密文样例
//        System.out.println(SimpleCryptoPropUtils.encryptAndWrap("test-message",
//                "aes:KrIjtliPM3MIlHPh+l3ylA=="));
//        System.out.println(SimpleCryptoPropUtils.encryptAndWrap("test-message",
//                "rsa:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCNdKzQtr8SdsQmOdshUSO3S2/PWR2KZJ9FM5xcIDcMMueGXBSrfy3Y8yf3WLZOmfMPWRu0Mhza1PQzDsceq/AvRAx06WOOB7VdYQPj7Z6+A196sAIHPBkakWAzUanak36dpYzAQCDp2IG6tQGBJOELsqrSkJI42wKgGTyBceeM9QIDAQAB"));

        // SimpleCryptoPropUtils AES加解密测试
        Assert.assertEquals("test-message-1",
                SimpleCryptoPropUtils.unwrapAndDecrypt(SimpleCryptoPropUtils.encryptAndWrap(
                        "test-message-1", "aes:KrIjtliPM3MIlHPh+l3ylA=="), "aes:KrIjtliPM3MIlHPh+l3ylA=="));
        Assert.assertEquals("test-message-1",
                SimpleCryptoPropUtils.unwrapAndDecrypt(SimpleCryptoPropUtils.encryptAndWrap(
                        "test-message-1", "aes:classpath:data/cryptoprop-key.txt"), "aes:classpath:data/cryptoprop-key.txt"));
//        Assert.assertEquals("test-message-1",
//                SimpleCryptoPropUtils.unwrapAndDecrypt(SimpleCryptoPropUtils.encryptAndWrap(
//                        "test-message-1", "aes:file:../glacispring-common/src/test/resources/data/cryptoprop-key.txt"),
//                        "aes:file:../glacispring-common/src/test/resources/data/cryptoprop-key.txt"));

        // SimpleCryptoPropUtils RSA加解密测试
        Assert.assertEquals("test-message-2",
                SimpleCryptoPropUtils.unwrapAndDecrypt(SimpleCryptoPropUtils.encryptAndWrap(
                        "test-message-2", "rsa:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCNdKzQtr8SdsQmOdshUSO3S2/PWR2KZJ9FM5xcIDcMMueGXBSrfy3Y8yf3WLZOmfMPWRu0Mhza1PQzDsceq/AvRAx06WOOB7VdYQPj7Z6+A196sAIHPBkakWAzUanak36dpYzAQCDp2IG6tQGBJOELsqrSkJI42wKgGTyBceeM9QIDAQAB"),
                        "rsa:MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI10rNC2vxJ2xCY52yFRI7dLb89ZHYpkn0UznFwgNwwy54ZcFKt/LdjzJ/dYtk6Z8w9ZG7QyHNrU9DMOxx6r8C9EDHTpY44HtV1hA+Ptnr4DX3qwAgc8GRqRYDNRqdqTfp2ljMBAIOnYgbq1AYEk4QuyqtKQkjjbAqAZPIFx54z1AgMBAAECgYBqTcttklhnZM+ltocCM7r2jX96QItdrJ6w38dayG72AO9TXrG96/alep6HWKiwyysJVrrmIV7j6XOXRzzGxQnbJNbvD4C/39S/pll02PdUfNLtWeMVUVAHGb9QEOWF7xXD8cLUCsp+gU4LINGHGe56a2nSczo/z/IShFisW7HewQJBAPMIlAjbd/16H90CUH0QVyzG46fL3t1D+2NTNGnvfBCpIi9XGuTYyMC89H8jxEjnsWAUmiPwA6Tc2gtaMwLtHhECQQCVALf5Cl5cKtQA8lhdHtpqy58m8Ga7e44lImAhUTj0DiuGuW7/0T3/zGcIdZv9pI2UTVjZLqNqZacqleuMJGylAkEAwHZ+pTUIpRfdu+xlSWVzY+ZtyyhMafW4U0RFMc+R9K+8frkAd1KmSNxa04TDoOi7M1edafBdMmYj1vGrjBmzwQJAb3UFKQ4ffXQv97FQdf/BREeiel8ziaSntJFdNS7rmmwLFREavdNIPFMq80H+eKIhocCl6Hehl9IIVKumccNBXQJBAIWl/nETIB1tuln4JZGfyHxte0JBfwohjYUgsNEUVkBMOI31TbQg3QyPOsLAy+l0zKbacx5E2Ad/xfQ6d0WMLKE="));
        Assert.assertEquals("test-message-2",
                SimpleCryptoPropUtils.unwrapAndDecrypt(SimpleCryptoPropUtils.encryptAndWrap(
                        "test-message-2", "rsa:classpath:data/cryptoprop-public-key.pem"),
                        "rsa:classpath:data/cryptoprop-private-key.pem"));
//        Assert.assertEquals("test-message-2",
//                SimpleCryptoPropUtils.unwrapAndDecrypt(SimpleCryptoPropUtils.encryptAndWrap(
//                        "test-message-2", "rsa:file:../glacispring-common/src/test/resources/data/cryptoprop-public-key.pem"),
//                        "rsa:file:../glacispring-common/src/test/resources/data/cryptoprop-private-key.pem"));

        // SimpleCryptoPropDecryptor 解密测试
        SimpleCryptoPropDecryptor decryptor = new SimpleCryptoPropDecryptor("aes:KrIjtliPM3MIlHPh+l3ylA==");
        Assert.assertEquals("test-message-3",
                decryptor.decrypt("somekey", SimpleCryptoPropUtils.encryptAndWrap("test-message-3", "aes:KrIjtliPM3MIlHPh+l3ylA==")));

        decryptor.setKey("rsa:classpath:data/cryptoprop-private-key.pem");
        Assert.assertEquals("test-message-4",
                decryptor.decrypt("somekey", SimpleCryptoPropUtils.encryptAndWrap("test-message-4", "rsa:classpath:data/cryptoprop-public-key.pem")));

        // 密钥隐藏测试
//        String key = "aes:1234567890123456";
//        while (key.length() > 0) {
//            System.out.println(SimpleCryptoPropUtils.hidePartially(key));
//            key = key.substring(0, key.length() - 1);
//        }
//        key = "aes:classpath:1234567890123456";
//        while (key.length() > 0) {
//            System.out.println(SimpleCryptoPropUtils.hidePartially(key));
//            key = key.substring(0, key.length() - 1);
//        }

    }

}
