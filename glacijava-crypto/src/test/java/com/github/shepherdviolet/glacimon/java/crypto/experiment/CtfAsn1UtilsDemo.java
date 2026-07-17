/*
 * Copyright (C) 2022-2026 S.Violet
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

package com.github.shepherdviolet.glacimon.java.crypto.experiment;

import com.github.shepherdviolet.glacimon.java.crypto.AdvancedCertificateUtils;
import com.github.shepherdviolet.glacimon.java.crypto.PEMEncodeUtils;
import com.github.shepherdviolet.glacimon.java.crypto.RSAKeyGenerator;

import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * [CTF用] ASN.1格式的密钥/证书高容忍解析工具 (DER / PEM / X509 / PKCS#1/#8) 示例
 */
public class CtfAsn1UtilsDemo {

    private static final String BROKEN_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEogIBAAKCAQByn01nVFqQyYoT3THJq4uogyEK97gusUaALtcMTCnm/uo609/j\n" +
            "3krnRezGTdXR3hRQAmGc8WElDYctk05Vkyf6qDQxAhjKck+v1Yp17r+5l3MFNN/+\n" +
            "NcdtiGP6+U2hxxaCEPrFirh7BRNBWdFXpThuU1ZacWkvSyjTtjpO6qqjsScOptEJ\n" +
            "jxXaYl8ifyGCeri9ccez16CDCSKP7veCxGUNJXsO1EQekofRennvqIu9Kwk3ZB6r\n" +
            "AZt86z2rXmhVNtNYLQILPzdszydA7xno5v0msUdVyYaY1/lUkcry9VIy0RviwmzQ\n" +
            "ed3dGSs31uJr0szJMw5fF5LzYnG4rSifXIJxAgMBAAECggEAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAKBgQDJMTmt1O4If2JR7q4H5eiEL082ZBM7+A1TCy78U7u8DLytl+Hy30zt\n" +
            "LXRmbeOB1dqj51dnBoAqCx9AaEgrBLj292Ng8K7HtWiU+tfPunhqd8GNO2/wWlRg\n" +
            "6skWOdknJITerWrkkRZNhnMwdXjN1zWBkpZyMRbi4B30bxDBRwm+KwKBgQCR2NiQ\n" +
            "ooQZNYKbHFcmwYkEmNXBkGAdfFKjeKGn4ZP6ifre1gN5NfByDkEs9i1oeHAAS218\n" +
            "etbmJU7INivfLc1R9r3qIGW1gvj6TQy4sphQi6NtoAGSuUPNPy+QoY978bR4uyPh\n" +
            "Uqpb1qHHkOnvhGXkvyYEuAko30tTUOsgXLDP0wKBgQCdywW52G54K7ApIlFL0H10\n" +
            "bsZpmkObnQiWQQcwz1tGOZchbyW/HnNu8V+Bll4AzoEUW7SpEOgkEWUJVsCDPMj0\n" +
            "qUJAHYR3DUhgz/vC6DTZC+O5qQw6Lh5PhNUBoY02reWn38seSMx9MR3Wu8trZYaJ\n" +
            "gTaNiQKyHaRguVsiu1xg0QKBgFgPR+M8XOojpvIkkHJ0FFjUNuwiUgY7lGHjaifF\n" +
            "SeXR/ckiCwLakI0tEiklkpErduSWpkqsmKhpCkJUgvTD4N6GTnDYktCffdkTQIUc\n" +
            "QF8RkOGV5J1Egy2f4wY+pjW94KuswqM/mGDPHOs0EituE1+kEj5zKASnngF2MCFg\n" +
            "FdnFAoGAcIKrYP8KO/Bj1gNuvsduFgJsAdLPtmDAHORb3/fxGbCMnvVkYSsDSv+p\n" +
            "qcmOd6Yj/Nc8ma0KfeJ6xUsAbelllPOweV1Jk6hGkTd7lhiiPCO563QbFQztog8n\n" +
            "XTBAyYTiRFXnmN7TOAc+BblC90Tg1FBmbDmcWGdgF2gvqsueCK0=\n" +
            "-----END RSA PRIVATE KEY-----\n";

    public static void main(String[] args) throws Exception {

        String newPrivateKeyPem = PEMEncodeUtils.rsaPrivateKeyToPEMEncoded(RSAKeyGenerator.generateKeyPair(2048).getPKCS8EncodedPrivateKey());
        String newPublicKeyPem = PEMEncodeUtils.rsaPrivateKeyToPEMEncoded(RSAKeyGenerator.generateKeyPair(2048).getX509EncodedPublicKey());

        RSAKeyGenerator.RSAKeyPair rootKeyPair = RSAKeyGenerator.generateKeyPair(2048);
        X509Certificate rootCertificate = AdvancedCertificateUtils.generateRSAX509RootCertificate(
                "CN=Glacijava test ca, OU=Glacijava group, O=Violet Shell, L=Ningbo, ST=Zhejiang, C=CN",
                rootKeyPair.getPublicKey(),
                rootKeyPair.getPrivateKey(),
                3650,
                AdvancedCertificateUtils.SIGN_ALGORITHM_RSA_SHA256
        );
        String certPem = PEMEncodeUtils.certificateToPEMEncoded(AdvancedCertificateUtils.parseCertificateToEncoded(rootCertificate));

        Map<String, Object> result;

//        System.out.println("\n\n====从文件读取公私钥证书 -> 原始数据==================================================================\n\n");
//        result = CtfAsn1Utils.parse(new File("D:\\\\__Download\\\\虚机中转\\\\cert.pem"));
//        System.out.println(result);

        System.out.println("\n\n====从文本解析公私钥证书 -> 原始数据==================================================================\n\n");
        result = CtfAsn1Utils.parse(BROKEN_PRIVATE_KEY); // 损坏的非JAVA格式
        System.out.println(result);

        System.out.println("\n\n====从文本解析公私钥证书 -> 原始数据==================================================================\n\n");
        result = CtfAsn1Utils.parse(newPrivateKeyPem); // 正常的JAVA格式
        System.out.println(result);

        System.out.println("\n\n====一键解析公私钥证书 -> 自动推测==================================================================\n\n");
        CtfAsn1Guesser.guess(BROKEN_PRIVATE_KEY);

        System.out.println("\n\n====一键解析公私钥证书 -> 自动推测==================================================================\n\n");
        CtfAsn1Guesser.guess(newPrivateKeyPem);

        System.out.println("\n\n====一键解析公私钥证书 -> 自动推测==================================================================\n\n");
        CtfAsn1Guesser.guess(newPublicKeyPem);

        System.out.println("\n\n====一键解析公私钥证书 -> 自动推测==================================================================\n\n");
        CtfAsn1Guesser.guess(certPem);

    }

}
