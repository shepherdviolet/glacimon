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

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.Test;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.*;
import sviolet.thistle.util.crypto.base.BouncyCastleProviderUtils;
import sviolet.thistle.util.crypto.base.IssuerProvider;
import sviolet.thistle.util.crypto.base.SimpleIssuerProvider;
import sviolet.thistle.util.judge.CheckUtils;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;

/**
 * 一些证书相关的示例
 *
 * @author S.Violet
 */
public class CertificateSamples {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /**
     * 解析ASN1格式数据 (DER/PEM/X509等标准的基础数据格式)
     */
    @Test
    public void parseASN1Sequence() throws IOException {
        String asn1Data = "MIICOTCCAeACAQAwgaExGDAWBgNVBAMMD1RoaXN0bGUgdGVzdCBjYTEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEVMBMGA1UECgwMVmlvbGV0IFNoZWxsMQ8wDQYDVQQHDAZOaW5nYm8xETAPBgNVBAgMCFpoZWppYW5nMQswCQYDVQQGEwJDTjElMCMGCSqGSIb3DQEJARYWc2hlcGhlcmR2aW9sZXRAMTYzLmNvbTCCATMwgewGByqGSM49AgEwgeACAQEwLAYHKoZIzj0BAQIhAP////7/////////////////////AAAAAP//////////MEQEIP////7/////////////////////AAAAAP/////////8BCAo6fqenZ9eNE1ankvPZQmn85eJ9RWrj5LdvL1BTZQOkwRBBDLEriwfGYEZX5kERmo5yZSP4wu/8mYL4XFaRYkzTHTHvDc2ovT2d5xZvc7ja2khU9Cph3zGKkdAAt8y5SE58KACIQD////+////////////////cgPfayHGBStTu/QJOdVBIwIBAQNCAASnKbIz8EtduGKxzYB25zWkmGw81zv3zaQrGf3OnmfKBe7HY433ctNFYAxRElJJc0uWLkjk7QzJK2VAnlzwzA4yoAAwCgYIKoEcz1UBg3UDRwAwRAIgS7NyaP77/tAWUgbk0ZljU3p0wwSfJoS6zdHXnSaIl5sCIEC7/93hejEde1qEby40YrKT5GCj9z//hdj0lcc3izfy";
        ASN1Sequence seq = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(Base64Utils.decode(asn1Data)));
//        System.out.println(seq);
    }

    /**
     * 解析PKCS10证书申请数据
     */
    @Test
    public void parsePKCS10CertificationRequest() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String pkcs10Data = "MIICOTCCAeACAQAwgaExGDAWBgNVBAMMD1RoaXN0bGUgdGVzdCBjYTEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEVMBMGA1UECgwMVmlvbGV0IFNoZWxsMQ8wDQYDVQQHDAZOaW5nYm8xETAPBgNVBAgMCFpoZWppYW5nMQswCQYDVQQGEwJDTjElMCMGCSqGSIb3DQEJARYWc2hlcGhlcmR2aW9sZXRAMTYzLmNvbTCCATMwgewGByqGSM49AgEwgeACAQEwLAYHKoZIzj0BAQIhAP////7/////////////////////AAAAAP//////////MEQEIP////7/////////////////////AAAAAP/////////8BCAo6fqenZ9eNE1ankvPZQmn85eJ9RWrj5LdvL1BTZQOkwRBBDLEriwfGYEZX5kERmo5yZSP4wu/8mYL4XFaRYkzTHTHvDc2ovT2d5xZvc7ja2khU9Cph3zGKkdAAt8y5SE58KACIQD////+////////////////cgPfayHGBStTu/QJOdVBIwIBAQNCAASnKbIz8EtduGKxzYB25zWkmGw81zv3zaQrGf3OnmfKBe7HY433ctNFYAxRElJJc0uWLkjk7QzJK2VAnlzwzA4yoAAwCgYIKoEcz1UBg3UDRwAwRAIgS7NyaP77/tAWUgbk0ZljU3p0wwSfJoS6zdHXnSaIl5sCIEC7/93hejEde1qEby40YrKT5GCj9z//hdj0lcc3izfy";
        JcaPKCS10CertificationRequest request = new JcaPKCS10CertificationRequest(Base64Utils.decode(pkcs10Data));
        PublicKey pk = request.getPublicKey();
//        System.out.println(pk);
    }

    /**
     * 生成PKCS10证书申请数据
     */
    @Test
    public void buildPKCS10CertificationRequest() throws NoSuchAlgorithmException, IOException, OperatorCreationException {
        SM2KeyGenerator.SM2KeyParamsPair keyPair = SM2KeyGenerator.generateKeyParamsPair();
        X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        x500NameBuilder.addRDN(BCStyle.CN, "Thistle test ca");
        x500NameBuilder.addRDN(BCStyle.OU, "Thistle group");
        x500NameBuilder.addRDN(BCStyle.O, "Violet Shell");
        x500NameBuilder.addRDN(BCStyle.L, "Ningbo");
        x500NameBuilder.addRDN(BCStyle.ST, "Zhejiang");
        x500NameBuilder.addRDN(BCStyle.C, "CN");
        x500NameBuilder.addRDN(BCStyle.EmailAddress, "shepherdviolet@163.com");
        JcaPKCS10CertificationRequestBuilder pkcs10CertificationRequestBuilder = new JcaPKCS10CertificationRequestBuilder(x500NameBuilder.build(), keyPair.getJdkPublicKey());
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SM3withSM2");
        ContentSigner contentSigner = contentSignerBuilder.build(keyPair.getJdkPrivateKey());
        PKCS10CertificationRequest pkcs10CertificationRequest = pkcs10CertificationRequestBuilder.build(contentSigner);// PKCS10的请求
//        System.out.println(Base64Utils.encodeToString(pkcs10CertificationRequest.getEncoded()));
    }

    /* ************************************************************************************************************* */

    private static final String RSA_ROOT_CERT = "MIIDcTCCAlmgAwIBAgIFAM2O7zkwDQYJKoZIhvcNAQELBQAwejELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xFTATBgNVBAoMDFZpb2xldCBTaGVsbDEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEYMBYGA1UEAwwPVGhpc3RsZSB0ZXN0IGNhMB4XDTIwMDMyMzE0NDUxM1oXDTMwMDMyMTE0NDUxM1owejELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xFTATBgNVBAoMDFZpb2xldCBTaGVsbDEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEYMBYGA1UEAwwPVGhpc3RsZSB0ZXN0IGNhMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlbGOuxe8V010Y+KPN4VxmJ3U22Qn1Iiic0+umcpGa0OtO53X1kTFXaFEsvvFlKIBEuQJrxSb49KBSnvgZQbUT8pnXEcq+8Iop1AVaGkySCmEludOypG9DocmraK4SfnP6UuC083udQnXpo6G6xmRoKEogUAwFKAWCSKkxwplEg+YxswCAe5pJbujDq91U+LR+FaFCWS0yJUulKPEnRkhr5Lmg/Ozcr0e0YTzgF+wP3jqD5DqLOpjd5C+XnswHw59lDpnSVfxDzVshmx/1kQ/1tEvhv+wMhB8RAaNZtQmKt3n9c4EuL3YthvALFq4ap6ccQh1LW5mP9OFLFS4NiOQywIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQAKi6dSE/YwRxtoaVIM6OlMypHbBTfF9vMn6ltnwh0fAB0X/kg8LY7AG//r5YGvkGgGWdN/XsQbWhPZ7i2VFgkUay+oI1u1TBwHF6Rt2+MJMD/qEbKBtB7vobIcqqs6igNK5oBDqRjyGXoF8eEj/ReAamPKUAyDIz3K/Tg8dtiKYRufHmlcOzGyvhhV4dBjiY10gpBaTm490La3Qgygt5s+lIOUm8X/6qDhf5F+CaQjCf7C7s1yOg7fNdeiEQZnGw/VKbxsaVyVKmv7gErvAy/jn+cRnhhBbW1L6nNY/AUMp5scWCzZmk6+j6QI55xj3w1WoLz/g9QULUPuuFqWH73e";
    private static final String RSA_SUBJECT_CERT = "MIIDdjCCAl6gAwIBAgIFAOtk9x0wDQYJKoZIhvcNAQELBQAwejELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xFTATBgNVBAoMDFZpb2xldCBTaGVsbDEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEYMBYGA1UEAwwPVGhpc3RsZSB0ZXN0IGNhMB4XDTIwMDMyMzE0NDUxNFoXDTMwMDMyMTE0NDUxNFowfzELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xFTATBgNVBAoMDFZpb2xldCBTaGVsbDEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEdMBsGA1UEAwwUVGhpc3RsZSB0ZXN0IHN1YmplY3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCQXMrWqVF7dGuGi5FQkoECW6YHN4+JYq1m/qKW8dbsBr39mNXCQxgpZz6Jg0lcDUfQ2g21EYUXxfnWCBkEt3VyOyxYy0ryd6mQc4mae2KvNScK5ZCd25DMBARWAEQihjeHHg4s9VRXUTojNrZz36PJZTW4NwyLi7A+QUR3vKQf0ksaFD3YxdKYd+ctkngj/+5ur/gQZG94SlCA/CAuQOkvLxbjz7lKmRivo5WuLdq28x9SLj21Qq0TOL8GrJXxYoPeZLxCivjXadpTrSREEpniUFGt9o8ludV0KLqCIa4pAe24+XLyporsfIevBQ3Tje58DfGnxmdmETs6NZiSmLevAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAD/nUoI2IzqjKzimi5tE7PBw7HxoR7Ewa4ma+1t99d39O9elTyDSl1CnZQXJyOKM6V0ddACHFK+RI6MX36ROARBQ+shGiFIQOtYjr2ohB7aHBRE5MbX294oEQJbfmyZop0nF3jNq4OVJIzTNGAzUmyR4Kk/dFrtWG3AfmCqgQE8i8Fc4zMft5MiUYtazaqVd7SVjxiJyHSwld1RUOznFbdTRJmSSiciCaxg8Rl8pjv2C8fro0bo5cMh0/blX1s2JmuB1CTpeUd+DUUIPvs7HDAIMj3FHVjVtcYP2t5onn2sIu/H5Uy0HuXkpQv8ILyEvqXmQ5FrL76g1KMhI2EBmOz8=";
    private static final String RSA_SUBJECT_PRI_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCQXMrWqVF7dGuGi5FQkoECW6YHN4+JYq1m/qKW8dbsBr39mNXCQxgpZz6Jg0lcDUfQ2g21EYUXxfnWCBkEt3VyOyxYy0ryd6mQc4mae2KvNScK5ZCd25DMBARWAEQihjeHHg4s9VRXUTojNrZz36PJZTW4NwyLi7A+QUR3vKQf0ksaFD3YxdKYd+ctkngj/+5ur/gQZG94SlCA/CAuQOkvLxbjz7lKmRivo5WuLdq28x9SLj21Qq0TOL8GrJXxYoPeZLxCivjXadpTrSREEpniUFGt9o8ludV0KLqCIa4pAe24+XLyporsfIevBQ3Tje58DfGnxmdmETs6NZiSmLevAgMBAAECggEAR71VL4fE7kzUgnApLpkfc0SIJV3+/o8DDQPNWKWl4HUZRUUGNRuSjkC4Wy3a7tvKrIcv+KNUOJQxq6Mqi6V/v9yYFSgxfnzoztgbsaFPUTJgzJF4ZHoQYtI8NWC8e0unnIXbFYx09U9wTL+Lu9mPUy3wnDL/EPnCA637kGU2Snk7cIQKWKmoDO630qLtROAa9/FUQZzp5LyRcCU6IWjRzZGfOB8TfIqEQ94GEZJS/sDHt8ewlnUZDl8RldbEzFGRLd8H8oXC5poR/auUz8PA7CuytX0hmIY1R3MpRbmY6eS7+PE19PCo/3KKX50HnFc3CeDUS5YWiRStkkJswzfcgQKBgQDtdCJeUDGHBofisE2y1dEEtckMlmj4Zkm/j5bGN7qFtkONr8k9c8ZATR3Dm0nWV3xIv6EjA6ZujD556gmZh4KK0Ws8uG44/faOd7hIj1+cyfa5RVwixah3KAafljhnj7xxkdXeZqGkPzPicYCAbVlBYKbBV/xEmjJr+oyj8I0a1wKBgQCbo07K/xVz89t6448ZoleptMfiljU6T8QmMSurSFBIggLkHphooZOkA+1wGp9P401Is8Is4bwROUCTDJk34JzFDNjg7y9B6/ZHVWd51RR0Ak3oYzJLgWXWrOhvXhXvGZKpAd77kK70gq8BiWxZIcwjAW7f+W5vRQWurubHL7/G6QKBgQDNjhRWaruFNO0bGx4X6yqnFir1/5rdNccv0kBmVUXdjHuMQxlFXlzHuzpB48MWjuNjIqh+ZCGGX1eSODyZMIqcW0+m381i/s6aZB8eiSbu1pMDrXxmCY+dnwOk0OkBFZe/BM0MWvIg+imTJEhmZMK4as/QXbdfN9DgUqO/I3UzPwKBgQCGt0GhdlCEcGTfJw+beaj1jTSjTa8/DQJUqKUK+mn7iQWlnVIA5zLbekbQjUqupKolrur5XF8kbXEWl4YcFsC705X6hS5bmjovnp4Vl7m/fKsg5pQHRTb4Lex3UXIc5v7KaYMwLxkxLdj7tI7jS9zdxATLu6S6jX0QqfW6Hfua2QKBgFFhaa6esdM7wnFswd2Qd5EVFqUDM5QcrCK7oxrlAaqfyz9YMJm7iVwWHyRmq0+Zr9lM6Om+ItjN146j0ETDnooPvSVTUB6x1VjEtAKiynU/Oniom4ThitifBvrCbZ9Mu/lECQKb4m1hcvm3llaKrQsjofQEfGZSOT94aKCH2RXx";
    private static final String SM2_ROOT_CERT = "MIICJDCCAcqgAwIBAgIFAL78zP0wCgYIKoEcz1UBg3UwajELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xEzARBgNVBAoMCk15IENvbXBhbnkxEDAOBgNVBAsMB0lUIERlcHQxEDAOBgNVBAMMB1Rlc3QgQ0EwHhcNMjAwMzIzMTQ0ODQwWhcNMzAwMzIxMTQ0ODQwWjBqMQswCQYDVQQGEwJDTjERMA8GA1UECAwIWmhlamlhbmcxDzANBgNVBAcMBk5pbmdibzETMBEGA1UECgwKTXkgQ29tcGFueTEQMA4GA1UECwwHSVQgRGVwdDEQMA4GA1UEAwwHVGVzdCBDQTBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABOPe6+fR0qBcmhVnw4ETw1g8BPqCfPfxNr8mZjm3Vp25b1plwG9+SvU4NADtdZpDjhut629IWrMuCJ+JvxwjO/GjXTBbMB0GA1UdDgQWBBTitaSsrtw2lrb/X4rQe9O+mn3SUzAfBgNVHSMEGDAWgBTitaSsrtw2lrb/X4rQe9O+mn3SUzAMBgNVHRMEBTADAQH/MAsGA1UdDwQEAwIBljAKBggqgRzPVQGDdQNIADBFAiEAl2MGwUG/dghRkZqeWH2h6cYCNwlmBfNgKW7hV17cPjkCIGl9gSDyodXO5JNhkw0wAQ/4QF05jf/soxc04UvPbU5s";
    private static final String SM2_SUBJECT_CERT = "MIICIjCCAcmgAwIBAgIFAO72IzEwCgYIKoEcz1UBg3UwajELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFpoZWppYW5nMQ8wDQYDVQQHDAZOaW5nYm8xEzARBgNVBAoMCk15IENvbXBhbnkxEDAOBgNVBAsMB0lUIERlcHQxEDAOBgNVBAMMB1Rlc3QgQ0EwHhcNMjAwMzIzMTQ0ODQwWhcNMzAwMzIxMTQ0ODQwWjBsMQswCQYDVQQGEwJDTjERMA8GA1UECAwIWmhlamlhbmcxDzANBgNVBAcMBk5pbmdibzETMBEGA1UECgwKTXkgQ29tcGFueTEQMA4GA1UECwwHSVQgRGVwdDESMBAGA1UEAwwJVGVzdCBVc2VyMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAE1854+5ftRzG7UoH7XuC3FZemvzMpIidGoI73awEQG1SkjplPwo9ZLJhXEqOZJTrao1YPt1Hx8a7XXIoPgMY52KNaMFgwHQYDVR0OBBYEFOqen16lTom42Vs3ybzChti+30WYMB8GA1UdIwQYMBaAFOK1pKyu3DaWtv9fitB7076afdJTMAkGA1UdEwQCMAAwCwYDVR0PBAQDAgSQMAoGCCqBHM9VAYN1A0cAMEQCICh8OwptcCj5j7sEjdkM77XGEs40gFwV0ybUiBqHtFzDAiBc3EFL33nJkIshiSah1QpXL941PwOZ7af17KhrjF2QfQ==";
    private static final String SM2_SUBJECT_PRI_KEY = "MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIIVoq77CDIKC4qPcnMqX7+JRtQVDKeCixffa1C/zeKwfoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAATXznj7l+1HMbtSgfte4LcVl6a/MykiJ0agjvdrARAbVKSOmU/Cj1ksmFcSo5klOtqjVg+3UfHxrtdcig+AxjnY";

    @Test
    public void checkSignByCertificatesAutomatically() throws CertificateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException, CryptoException {
        IssuerProvider<?> issuerProvider = new SimpleIssuerProvider(Arrays.asList(
                AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(RSA_ROOT_CERT)),
                AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(SM2_ROOT_CERT))));

        byte[] plainText = "hello".getBytes();

        X509Certificate rsaSubjectCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(RSA_SUBJECT_CERT));
        byte[] rsaSignature = RSACipher.sign(
                plainText,
                RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(RSA_SUBJECT_PRI_KEY)),
                rsaSubjectCert.getSigAlgName());

        checkSignByCertificates(plainText,
                rsaSignature,
                "CN=Thistle test subject, OU=Thistle group, O=Violet Shell, L=Ningbo, ST=Zhejiang, C=CN",
                rsaSubjectCert,
                issuerProvider);

        X509Certificate sm2SubjectCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(SM2_SUBJECT_CERT));
        byte[] sm2Signature = SM2Cipher.sign(
                plainText,
                null,
                SM2KeyGenerator.generatePrivateKeyParamsByPKCS8(Base64Utils.decode(SM2_SUBJECT_PRI_KEY)),
                sm2SubjectCert.getSigAlgName());

        checkSignByCertificates(plainText,
                sm2Signature,
                "CN=Test User, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN",
                sm2SubjectCert,
                issuerProvider);

    }

    private void checkSignByCertificates(byte[] plainText, byte[] signature, String userDn, X509Certificate certificate, IssuerProvider<?> issuerProvider) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (plainText == null) {
            throw new IllegalArgumentException("plainText is null");
        }
        if (signature == null) {
            throw new IllegalArgumentException("signature is null");
        }
        if (certificate == null) {
            throw new IllegalArgumentException("User certificate is null");
        }
        if (issuerProvider == null) {
            throw new IllegalArgumentException("issuerProvider is null");
        }
        // DN 不符
        if (!CheckUtils.isEmpty(userDn)) {
            X500Principal expect;
            try {
                expect = new X500Principal(userDn);
            } catch (Exception e) {
                throw new CertificateException("Illegal user DN: " + userDn, e);
            }
            if (!expect.equals(certificate.getSubjectX500Principal())) {
                throw new CertificateException("Certificate and user DN do not match, expect: " + expect + ", actual: " + certificate.getSubjectX500Principal());
            }
        }
        // 验证书
        AdvancedCertificateUtils.verifyCertificateByIssuers(certificate, new Date(), issuerProvider);
        // 区分算法, 这里简单地区分RSA和SM2
        PublicKey publicKey = certificate.getPublicKey();
        if (publicKey instanceof RSAPublicKey) {
            // RSA
            if (RSACipher.verify(plainText, signature, publicKey, certificate.getSigAlgName())) {
                return;
            }
            throw new SignatureException("Signature verify failed (RSA), plainText: " + ByteUtils.bytesToHex(plainText) + ", signature: " + ByteUtils.bytesToHex(signature));
        } else if (publicKey instanceof BCECPublicKey) {
            // ECC or SM2
            switch (certificate.getSigAlgOID()) {
                // SM3WithSM2 sha1withSM2 sha256withSM2
                case "1.2.156.10197.1.501":
                case "1.2.156.10197.1.502":
                case "1.2.156.10197.1.503":
                    if (SM2Cipher.verify(plainText, signature, null,
                            SM2KeyGenerator.publicKeyToPublicKeyParams((BCECPublicKey) publicKey), certificate.getSigAlgName())) {
                        return;
                    }
                    throw new SignatureException("Signature verify failed (SM2), plainText: " + ByteUtils.bytesToHex(plainText) + ", signature: " + ByteUtils.bytesToHex(signature));
                default:
                    break;
            }
        }
        throw new NoSuchAlgorithmException("Does not support the signature algorithm used by the certificate, certificate info:" + certificate);
    }

}
