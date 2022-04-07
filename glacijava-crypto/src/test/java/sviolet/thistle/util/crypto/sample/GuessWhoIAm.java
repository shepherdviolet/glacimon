/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.thistle.util.crypto.sample;

import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import sviolet.thistle.entity.exception.IllegalParamException;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.*;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * 推断一个数据是什么 (密钥/证书)
 *
 * @author S.Violet
 */
public class GuessWhoIAm {

    private static final String CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIICIzCCAcmgAwIBAgIFAOVjNKEwCgYIKoEcz1UBg3UwajELMAkGA1UEBhMCQ04x\n" +
            "ETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xEzARBgNVBAoMCk15\n" +
            "IENvbXBhbnkxEDAOBgNVBAsMB0lUIERlcHQxEDAOBgNVBAMMB1Rlc3QgQ0EwHhcN\n" +
            "MjAwMzI1MDUyOTQ2WhcNMzAwMzIzMDUyOTQ2WjBsMQswCQYDVQQGEwJDTjERMA8G\n" +
            "A1UECAwIWmhlamlhbmcxDzANBgNVBAcMBk5pbmdibzETMBEGA1UECgwKTXkgQ29t\n" +
            "cGFueTEQMA4GA1UECwwHSVQgRGVwdDESMBAGA1UEAwwJVGVzdCBVc2VyMFkwEwYH\n" +
            "KoZIzj0CAQYIKoEcz1UBgi0DQgAEz7iOqz00CUZsjw3ugX53Oxy/HNeuOLXG6CTN\n" +
            "EGcBa01/vm+DdB8kAOser3TVUmKesUO/ZcemJC3bflDn5qaGQaNaMFgwHQYDVR0O\n" +
            "BBYEFEzXJ8cf4Q/EqKGdpHaeC+V9nVLmMB8GA1UdIwQYMBaAFIxNaw1KPBTWDSXv\n" +
            "kGIz3U/RmshOMAkGA1UdEwQCMAAwCwYDVR0PBAQDAgSQMAoGCCqBHM9VAYN1A0gA\n" +
            "MEUCIQDgaX2kNLJrEUh/y07DeLm1sBteZLzM1In0oyMpwA27MwIgeks2Fx7v0w0W\n" +
            "+5Lhxg+fsDi94VkvZtaXhObxS5yrk/0=\n" +
            "-----END CERTIFICATE-----";

    public static void main(String[] args) throws Exception {
//        guess(Base64Utils.encodeToString(RSAKeyGenerator.generateKeyPair(1024).getPKCS8EncodedPrivateKey()));
//        guess(Base64Utils.encodeToString(RSAKeyGenerator.generateKeyPair(1024).getX509EncodedPublicKey()));
//        guess(Base64Utils.encodeToString(RSAKeyGenerator.generateKeyPair(2048).getPKCS8EncodedPrivateKey()));
//        guess(Base64Utils.encodeToString(RSAKeyGenerator.generateKeyPair(2048).getX509EncodedPublicKey()));
//        guess(ByteUtils.bytesToHex(RSAKeyGenerator.generateKeyPair(2048).getPKCS8EncodedPrivateKey()));
//        guess(ByteUtils.bytesToHex(RSAKeyGenerator.generateKeyPair(2048).getX509EncodedPublicKey()));
//        guess(Base64Utils.encodeToString(SM2KeyGenerator.generateKeyParamsPair().getPKCS8EncodedPrivateKey()));
//        guess(Base64Utils.encodeToString(SM2KeyGenerator.generateKeyParamsPair().getPublicASN1Encoding()));
//        guess(Base64Utils.encodeToString(SM2KeyGenerator.generateKeyParamsPair().getX509EncodedPublicKey()));
//        guess(Base64Utils.encodeToString(ECDSAKeyGenerator.generateKeyPair().getPKCS8EncodedPrivateKey()));
//        guess(Base64Utils.encodeToString(ECDSAKeyGenerator.generateKeyPair().getX509EncodedPublicKey()));
//        guess(CERT);
    }

    public static void guess(String data){
        System.out.println("=========================================================================================");
        System.out.println("Input: " + data);
        System.out.println("------------------------------------------------------------------------");

        if (data == null) {
            System.out.println("Null!");
            return;
        }

        //去掉PEM头尾
        if (data.startsWith("-----BEGIN")) {
            try {
                data = PEMEncodeUtils.pemEncodedToX509EncodedString(data);
                System.out.println("Is PEM encoding");
            } catch (IllegalParamException ignore) {
            }
        }

        //转bytes
        byte[] dataBytes = null;
        try {
            dataBytes = ByteUtils.hexToBytes(data);
            System.out.println("Is a hex string");
        } catch (Throwable ignore) {
        }
        if (dataBytes == null) {
            try {
                dataBytes = Base64Utils.decode(data);
                System.out.println("Is a base64 string");
            } catch (Throwable ignore) {
            }
        }

        if (dataBytes == null) {
            System.out.println("Unsupported data format! (Not a hex or base64 string)");
            return;
        }

        try {
            RSAPrivateKey rsaPrivateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(dataBytes);
            System.out.println("Is a RSA Private Key");
            System.out.println("> Format:" + rsaPrivateKey.getFormat());
            System.out.println("> Algorithm:" + rsaPrivateKey.getAlgorithm());
            System.out.println("> Modulus:" + rsaPrivateKey.getModulus().toString(16).length());
            System.out.println("> Exponent:" + rsaPrivateKey.getPrivateExponent().toString(16).length());
            System.out.println("> Length:" + rsaPrivateKey.getModulus().toString(16).length() * 4);
            return;
        } catch (Throwable ignore) {
        }

        try {
            RSAPublicKey rsaPublicKey = RSAKeyGenerator.generatePublicKeyByX509(dataBytes);
            System.out.println("Is a RSA Public Key");
            System.out.println("> Format:" + rsaPublicKey.getFormat());
            System.out.println("> Algorithm:" + rsaPublicKey.getAlgorithm());
            System.out.println("> Modulus:" + rsaPublicKey.getModulus().toString(16).length());
            System.out.println("> Exponent:" + rsaPublicKey.getPublicExponent().toString(16).length());
            System.out.println("> Length:" + rsaPublicKey.getModulus().toString(16).length() * 4);
            return;
        } catch (Throwable ignore) {
        }

        try {
            ECPrivateKeyParameters ecPrivateKeyParameters = SM2KeyGenerator.generatePrivateKeyParamsByPKCS8(dataBytes);
            BCECPrivateKey bcecPrivateKey = SM2KeyGenerator.privateKeyParamsToPrivateKey(ecPrivateKeyParameters, null);
            System.out.println("Is a EC/SM2 Private Key");
            System.out.println("> Format:" + bcecPrivateKey.getFormat());
            System.out.println("> Algorithm:" + bcecPrivateKey.getAlgorithm());
            System.out.println("> Curve:" + ecPrivateKeyParameters.getParameters().getCurve());
            System.out.println("> D:" + ecPrivateKeyParameters.getD().toString(16));
            return;
        } catch (Throwable ignore) {
        }

        try {
            ECPublicKeyParameters ecPublicKeyParameters = SM2KeyGenerator.generatePublicKeyParamsByASN1(dataBytes);
            BCECPublicKey bcecPublicKey = SM2KeyGenerator.publicKeyParamsToPublicKey(ecPublicKeyParameters);
            System.out.println("Is a EC/SM2 Public Key (ASN1)");
            System.out.println("> Format:" + bcecPublicKey.getFormat());
            System.out.println("> Algorithm:" + bcecPublicKey.getAlgorithm());
            System.out.println("> Curve:" + ecPublicKeyParameters.getParameters().getCurve());
            System.out.println("> X:" + ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger().toString(16));
            System.out.println("> Y:" + ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger().toString(16));
            return;
        } catch (Throwable ignore) {
        }

        try {
            ECPublicKeyParameters ecPublicKeyParameters = SM2KeyGenerator.generatePublicKeyParamsByX509(dataBytes);
            BCECPublicKey bcecPublicKey = SM2KeyGenerator.publicKeyParamsToPublicKey(ecPublicKeyParameters);
            System.out.println("Is a EC/SM2 Public Key");
            System.out.println("> Format:" + bcecPublicKey.getFormat());
            System.out.println("> Algorithm:" + bcecPublicKey.getAlgorithm());
            System.out.println("> Curve:" + ecPublicKeyParameters.getParameters().getCurve());
            System.out.println("> X:" + ecPublicKeyParameters.getQ().getAffineXCoord().toBigInteger().toString(16));
            System.out.println("> Y:" + ecPublicKeyParameters.getQ().getAffineYCoord().toBigInteger().toString(16));
            return;
        } catch (Throwable ignore) {
        }

        try {
            X509Certificate certificate = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(dataBytes);
            if (certificate != null) {
                System.out.println("Is a X509 Certificate");
                System.out.println("Public Key (X509):" + Base64Utils.encodeToString(certificate.getPublicKey().getEncoded()));
                System.out.println("Certificate:" + certificate);
            }
            return;
        } catch (Throwable ignore) {
        }

        System.out.println("Unknown key type!");
    }

}
