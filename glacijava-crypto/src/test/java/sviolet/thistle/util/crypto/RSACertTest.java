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

import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.conversion.DateTimeUtils;
import sviolet.thistle.util.crypto.base.IssuerProvider;
import sviolet.thistle.util.crypto.base.RootIssuerProvider;
import sviolet.thistle.util.crypto.base.SimpleIssuerProvider;
import sviolet.thistle.util.crypto.base.X500NameWrapper;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class RSACertTest {

    @Test
    public void common() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, SignatureException, InvalidKeySpecException {

        RSAKeyGenerator.RSAKeyPair rootKeyPair = RSAKeyGenerator.generateKeyPair(2048);

        X509Certificate rootCertificate = AdvancedCertificateUtils.generateRSAX509RootCertificate(
                "CN=Thistle test ca, OU=Thistle group, O=Violet Shell, L=Ningbo, ST=Zhejiang, C=CN",
                rootKeyPair.getPublicKey(),
                rootKeyPair.getPrivateKey(),
                3650,
                AdvancedCertificateUtils.SIGN_ALGORITHM_RSA_SHA256
        );

        AdvancedCertificateUtils.verifyCertificate(rootCertificate, rootKeyPair.getPublicKey());
//        System.out.println(Base64Utils.encodeToString(AdvancedCertificateUtils.parseCertificateToEncoded(rootCertificate)));

        PKCS12KeyStoreUtils.storeCertificateAndKey(
                "./out/test-case/pkcs12-test-ca.p12",
                "000000",
                "Thistle test ca alias",
                null,
                rootCertificate);

        RSAKeyGenerator.RSAKeyPair subjectKeyPair = RSAKeyGenerator.generateKeyPair(2048);

        X509Certificate subjectCertificate = AdvancedCertificateUtils.generateRSAX509Certificate(
                "CN=Thistle test subject, OU=Thistle group, O=Violet Shell, L=Ningbo, ST=Zhejiang, C=CN",
                subjectKeyPair.getPublicKey(),
                3650,
                AdvancedCertificateUtils.SIGN_ALGORITHM_RSA_SHA256,
                rootCertificate,
                rootKeyPair.getPrivateKey());

        AdvancedCertificateUtils.verifyCertificate(subjectCertificate, rootKeyPair.getPublicKey());
//        System.out.println(Base64Utils.encodeToString(AdvancedCertificateUtils.parseCertificateToEncoded(subjectCertificate)));
//        System.out.println(Base64Utils.encodeToString(subjectKeyPair.getPKCS8EncodedPrivateKey()));

        PKCS12KeyStoreUtils.storeCertificateAndKey(
                "./out/test-case/pkcs12-test.p12",
                "000000",
                "Thistle test subject alias",
                subjectKeyPair.getPrivateKey(),
                subjectCertificate);

        PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey = PKCS12KeyStoreUtils.loadCertificateAndKey(
                "./out/test-case/pkcs12-test-ca.p12",
                "000000",
                "Thistle test ca alias"
        );

        Assert.assertArrayEquals(new Certificate[]{rootCertificate}, certificateChainAndKey.getCertificateChain());
        Assert.assertNull(certificateChainAndKey.getPrivateKey());

        PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey2 = PKCS12KeyStoreUtils.loadCertificateAndKey(
                "./out/test-case/pkcs12-test.p12",
                "000000",
                "Thistle test subject alias"
        );

        Assert.assertArrayEquals(new Certificate[]{subjectCertificate}, certificateChainAndKey2.getCertificateChain());
        Assert.assertEquals(subjectKeyPair.getPrivateKey(), certificateChainAndKey2.getPrivateKey());

    }

    private static final String CERT = "MIIJoDCCCIigAwIBAgIMOAdzA8kea9OdhhhSMA0GCSqGSIb3DQEBCwUAMGYxCzAJ\n" +
            "BgNVBAYTAkJFMRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTwwOgYDVQQDEzNH\n" +
            "bG9iYWxTaWduIE9yZ2FuaXphdGlvbiBWYWxpZGF0aW9uIENBIC0gU0hBMjU2IC0g\n" +
            "RzIwHhcNMjAwMTEzMDMwMjA1WhcNMjAwNjI1MDUzMTAyWjCBpzELMAkGA1UEBhMC\n" +
            "Q04xEDAOBgNVBAgTB2JlaWppbmcxEDAOBgNVBAcTB2JlaWppbmcxJTAjBgNVBAsT\n" +
            "HHNlcnZpY2Ugb3BlcmF0aW9uIGRlcGFydG1lbnQxOTA3BgNVBAoTMEJlaWppbmcg\n" +
            "QmFpZHUgTmV0Y29tIFNjaWVuY2UgVGVjaG5vbG9neSBDby4sIEx0ZDESMBAGA1UE\n" +
            "AxMJYmFpZHUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtMa/\n" +
            "2lMgD+pA87hSF2Y7NgGNErSZDdObbBhTsRkIsPpzRz4NOnlieGEuVDxJfFbawL5h\n" +
            "VdVCcGoQvvW9jWSWIQCTYwmHtxm6DiA+SchT7QKPRgHroQeTc7vt8bPJ4vvd8Dkq\n" +
            "g630QZi8huq6dKim49DlxY6zC7LSrJF0Dv+AECM2YmUItIf1VwwlxwDY9ahduDNB\n" +
            "pypf2/pwniG7rkIWZgdp/hwmKoEPq3Pj1lIgpG2obNRmSKRv8mgKxWWhTr8EekBD\n" +
            "HNN1+3WsGdZKNQVuz9Vl0UTKawxYBMSFTx++LDLR8cYo+/kmNrVt+suWoqDQvPhR\n" +
            "3wdEvY9vZ8DUr9nNwwIDAQABo4IGCjCCBgYwDgYDVR0PAQH/BAQDAgWgMIGgBggr\n" +
            "BgEFBQcBAQSBkzCBkDBNBggrBgEFBQcwAoZBaHR0cDovL3NlY3VyZS5nbG9iYWxz\n" +
            "aWduLmNvbS9jYWNlcnQvZ3Nvcmdhbml6YXRpb252YWxzaGEyZzJyMS5jcnQwPwYI\n" +
            "KwYBBQUHMAGGM2h0dHA6Ly9vY3NwMi5nbG9iYWxzaWduLmNvbS9nc29yZ2FuaXph\n" +
            "dGlvbnZhbHNoYTJnMjBWBgNVHSAETzBNMEEGCSsGAQQBoDIBFDA0MDIGCCsGAQUF\n" +
            "BwIBFiZodHRwczovL3d3dy5nbG9iYWxzaWduLmNvbS9yZXBvc2l0b3J5LzAIBgZn\n" +
            "gQwBAgIwCQYDVR0TBAIwADBJBgNVHR8EQjBAMD6gPKA6hjhodHRwOi8vY3JsLmds\n" +
            "b2JhbHNpZ24uY29tL2dzL2dzb3JnYW5pemF0aW9udmFsc2hhMmcyLmNybDCCAzsG\n" +
            "A1UdEQSCAzIwggMuggliYWlkdS5jb22CDGJhaWZ1YmFvLmNvbYIMd3d3LmJhaWR1\n" +
            "LmNughB3d3cuYmFpZHUuY29tLmNugg9tY3QueS5udW9taS5jb22CC2Fwb2xsby5h\n" +
            "dXRvggZkd3ouY26CCyouYmFpZHUuY29tgg4qLmJhaWZ1YmFvLmNvbYIRKi5iYWlk\n" +
            "dXN0YXRpYy5jb22CDiouYmRzdGF0aWMuY29tggsqLmJkaW1nLmNvbYIMKi5oYW8x\n" +
            "MjMuY29tggsqLm51b21pLmNvbYINKi5jaHVhbmtlLmNvbYINKi50cnVzdGdvLmNv\n" +
            "bYIPKi5iY2UuYmFpZHUuY29tghAqLmV5dW4uYmFpZHUuY29tgg8qLm1hcC5iYWlk\n" +
            "dS5jb22CDyoubWJkLmJhaWR1LmNvbYIRKi5mYW55aS5iYWlkdS5jb22CDiouYmFp\n" +
            "ZHViY2UuY29tggwqLm1pcGNkbi5jb22CECoubmV3cy5iYWlkdS5jb22CDiouYmFp\n" +
            "ZHVwY3MuY29tggwqLmFpcGFnZS5jb22CCyouYWlwYWdlLmNugg0qLmJjZWhvc3Qu\n" +
            "Y29tghAqLnNhZmUuYmFpZHUuY29tgg4qLmltLmJhaWR1LmNvbYISKi5iYWlkdWNv\n" +
            "bnRlbnQuY29tggsqLmRsbmVsLmNvbYILKi5kbG5lbC5vcmeCEiouZHVlcm9zLmJh\n" +
            "aWR1LmNvbYIOKi5zdS5iYWlkdS5jb22CCCouOTEuY29tghIqLmhhbzEyMy5iYWlk\n" +
            "dS5jb22CDSouYXBvbGxvLmF1dG+CEioueHVlc2h1LmJhaWR1LmNvbYIRKi5iai5i\n" +
            "YWlkdWJjZS5jb22CESouZ3ouYmFpZHViY2UuY29tgg4qLnNtYXJ0YXBwcy5jboIN\n" +
            "Ki5iZHRqcmN2LmNvbYIMKi5oYW8yMjIuY29tggwqLmhhb2thbi5jb22CDyoucGFl\n" +
            "LmJhaWR1LmNvbYISY2xpY2suaG0uYmFpZHUuY29tghBsb2cuaG0uYmFpZHUuY29t\n" +
            "ghBjbS5wb3MuYmFpZHUuY29tghB3bi5wb3MuYmFpZHUuY29tghR1cGRhdGUucGFu\n" +
            "LmJhaWR1LmNvbTAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwHwYDVR0j\n" +
            "BBgwFoAUlt5h8b0cFilTHMDMfTuDAEDmGnwwHQYDVR0OBBYEFHa15tZJ+Pg26nWp\n" +
            "bV5NVVs3XP3HMIIBAwYKKwYBBAHWeQIEAgSB9ASB8QDvAHUAxlKg7EjOs/yrFwmS\n" +
            "xDqHQTMJ6ABlomJSQBujNioXxWUAAAFvnNofVQAABAMARjBEAiAEGvTIGSAUi0RW\n" +
            "jvpirj6tNuI42Rr3FWlIOzgEoKrOdgIgS0qKQlN5xGmtBHa5cmY9Vn6misB03+Rq\n" +
            "IoQiiiPkwkcAdgCyHgXMi6LNiiBOh2b5K7mKJSBna9r6cOeySVMt74uQXgAAAW+c\n" +
            "2h+WAAAEAwBHMEUCIQC88tB1BegEZ4l4LTDg7bMF8eLy6FcebQB47VU/jWMDYQIg\n" +
            "R2IWitWPefw95Fi5MgBM4v2gL+SUSFmyzyFYFjyGQ+cwDQYJKoZIhvcNAQELBQAD\n" +
            "ggEBALo5ZNb3iUkXrpRUMFofmwe2KuMc13eLVMmqeplg+1IuSnGGJsUCnK0TgvaB\n" +
            "K0FZZyJj4U9KTXOPGgQdx8Ts7/anSkFlXivSxCoS70PUEpJyxiIhdJwaHz9tn4h5\n" +
            "4kAXwWnNpoBKmGcRhMWTlxgArZ4NbpVn4QuWjoBwO3tNgmBzP1+/I1pbZWc+BP/W\n" +
            "Sx58WmnBSNEAkjX9NFE68K4Hx/EZDKwOUkeDNxAmuxDDNbZ/+56t9PCRvXW2aUGu\n" +
            "mt7G8HdKRcTLAv/RVm4kP9DC0wf4UmjvvtcEk4nBQejK6MIzz7WaW4zsh7JzjDFo\n" +
            "LLkKGKMibPjJqZskrQGEs2iurzU=";

    private static final String CA_CERT = "MIIEaTCCA1GgAwIBAgILBAAAAAABRE7wQkcwDQYJKoZIhvcNAQELBQAwVzELMAkG\n" +
            "A1UEBhMCQkUxGTAXBgNVBAoTEEdsb2JhbFNpZ24gbnYtc2ExEDAOBgNVBAsTB1Jv\n" +
            "b3QgQ0ExGzAZBgNVBAMTEkdsb2JhbFNpZ24gUm9vdCBDQTAeFw0xNDAyMjAxMDAw\n" +
            "MDBaFw0yNDAyMjAxMDAwMDBaMGYxCzAJBgNVBAYTAkJFMRkwFwYDVQQKExBHbG9i\n" +
            "YWxTaWduIG52LXNhMTwwOgYDVQQDEzNHbG9iYWxTaWduIE9yZ2FuaXphdGlvbiBW\n" +
            "YWxpZGF0aW9uIENBIC0gU0hBMjU2IC0gRzIwggEiMA0GCSqGSIb3DQEBAQUAA4IB\n" +
            "DwAwggEKAoIBAQDHDmw/I5N/zHClnSDDDlM/fsBOwphJykfVI+8DNIV0yKMCLkZc\n" +
            "C33JiJ1Pi/D4nGyMVTXbv/Kz6vvjVudKRtkTIso21ZvBqOOWQ5PyDLzm+ebomchj\n" +
            "SHh/VzZpGhkdWtHUfcKc1H/hgBKueuqI6lfYygoKOhJJomIZeg0k9zfrtHOSewUj\n" +
            "mxK1zusp36QUArkBpdSmnENkiN74fv7j9R7l/tyjqORmMdlMJekYuYlZCa7pnRxt\n" +
            "Nw9KHjUgKOKv1CGLAcRFrW4rY6uSa2EKTSDtc7p8zv4WtdufgPDWi2zZCHlKT3hl\n" +
            "2pK8vjX5s8T5J4BO/5ZS5gIg4Qdz6V0rvbLxAgMBAAGjggElMIIBITAOBgNVHQ8B\n" +
            "Af8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADAdBgNVHQ4EFgQUlt5h8b0cFilT\n" +
            "HMDMfTuDAEDmGnwwRwYDVR0gBEAwPjA8BgRVHSAAMDQwMgYIKwYBBQUHAgEWJmh0\n" +
            "dHBzOi8vd3d3Lmdsb2JhbHNpZ24uY29tL3JlcG9zaXRvcnkvMDMGA1UdHwQsMCow\n" +
            "KKAmoCSGImh0dHA6Ly9jcmwuZ2xvYmFsc2lnbi5uZXQvcm9vdC5jcmwwPQYIKwYB\n" +
            "BQUHAQEEMTAvMC0GCCsGAQUFBzABhiFodHRwOi8vb2NzcC5nbG9iYWxzaWduLmNv\n" +
            "bS9yb290cjEwHwYDVR0jBBgwFoAUYHtmGkUNl8qJUC99BM00qP/8/UswDQYJKoZI\n" +
            "hvcNAQELBQADggEBAEYq7l69rgFgNzERhnF0tkZJyBAW/i9iIxerH4f4gu3K3w4s\n" +
            "32R1juUYcqeMOovJrKV3UPfvnqTgoI8UV6MqX+x+bRDmuo2wCId2Dkyy2VG7EQLy\n" +
            "XN0cvfNVlg/UBsD84iOKJHDTu/B5GqdhcIOKrwbFINihY9Bsrk8y1658GEV1BSl3\n" +
            "30JAZGSGvip2CTFvHST0mdCF/vIhCPnG9vHQWe3WVjwIKANnuvD58ZAWR65n5ryA\n" +
            "SOlCdjSXVWkkDoPWoC209fN5ikkodBpBocLTJIg1MGCUF7ThBCIxPTsvFwayuJ2G\n" +
            "K1pp74P1S8SqtCr4fKGxhZSM9AyHDPSsQPhZSZg=";

    private static final String ROOT_CERT = "MIIDdTCCAl2gAwIBAgILBAAAAAABFUtaw5QwDQYJKoZIhvcNAQEFBQAwVzELMAkG\n" +
            "A1UEBhMCQkUxGTAXBgNVBAoTEEdsb2JhbFNpZ24gbnYtc2ExEDAOBgNVBAsTB1Jv\n" +
            "b3QgQ0ExGzAZBgNVBAMTEkdsb2JhbFNpZ24gUm9vdCBDQTAeFw05ODA5MDExMjAw\n" +
            "MDBaFw0yODAxMjgxMjAwMDBaMFcxCzAJBgNVBAYTAkJFMRkwFwYDVQQKExBHbG9i\n" +
            "YWxTaWduIG52LXNhMRAwDgYDVQQLEwdSb290IENBMRswGQYDVQQDExJHbG9iYWxT\n" +
            "aWduIFJvb3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDaDuaZ\n" +
            "jc6j40+Kfvvxi4Mla+pIH/EqsLmVEQS98GPR4mdmzxzdzxtIK+6NiY6arymAZavp\n" +
            "xy0Sy6scTHAHoT0KMM0VjU/43dSMUBUc71DuxC73/OlS8pF94G3VNTCOXkNz8kHp\n" +
            "1Wrjsok6Vjk4bwY8iGlbKk3Fp1S4bInMm/k8yuX9ifUSPJJ4ltbcdG6TRGHRjcdG\n" +
            "snUOhugZitVtbNV4FpWi6cgKOOvyJBNPc1STE4U6G7weNLWLBYy5d4ux2x8gkasJ\n" +
            "U26Qzns3dLlwR5EiUWMWea6xrkEmCMgZK9FGqkjWZCrXgzT/LCrBbBlDSgeF59N8\n" +
            "9iFo7+ryUp9/k5DPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8E\n" +
            "BTADAQH/MB0GA1UdDgQWBBRge2YaRQ2XyolQL30EzTSo//z9SzANBgkqhkiG9w0B\n" +
            "AQUFAAOCAQEA1nPnfE920I2/7LqivjTFKDK1fPxsnCwrvQmeU79rXqoRSLblCKOz\n" +
            "yj1hTdNGCbM+w6DjY1Ub8rrvrTnhQ7k4o+YviiY776BQVvnGCv04zcQLcFGUl5gE\n" +
            "38NflNUVyRRBnMRddWQVDf9VMOyGj/8N7yy5Y0b2qvzfvGn9LhJIZJrglfCm7ymP\n" +
            "AbEVtQwdpf5pLGkkeB6zpxxxYu7KyJesF12KwvhHhm4qxFYxldBniYUr+WymXUad\n" +
            "DKqC5JlR3XC321Y9YeRq4VzW9v493kHMB65jUr9TU/Qr6cf9tveCX4XSQRjbgbME\n" +
            "HMUfpIBvFSDJ3gyICh3WZlXi/EjJKSZp4A==";

    @Test
    public void dnToX500Name() throws CertificateException, NoSuchProviderException, IOException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X500NameWrapper x500NameWrapper = AdvancedCertificateUtils.dnToX500Name(cert.getSubjectX500Principal());
        Assert.assertEquals("baidu.com",
                x500NameWrapper.getObject(BCStyle.CN));
        Assert.assertEquals("CN",
                x500NameWrapper.getObject(BCStyle.C));
        Assert.assertArrayEquals(new String[]{"aaa", "bbb", "ccc"},
                AdvancedCertificateUtils.dnToX500Name("CN=aaa+CN=bbb, CN=ccc").getObjects(BCStyle.CN).toArray());
    }

    @Test
    public void getDomainNamesFromCertificate() throws CertificateException, NoSuchProviderException, IOException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        Assert.assertEquals("[baidu.com, baidu.com, baifubao.com, www.baidu.cn, www.baidu.com.cn, " +
                        "mct.y.nuomi.com, apollo.auto, dwz.cn, *.baidu.com, *.baifubao.com, *.baidustatic.com, " +
                        "*.bdstatic.com, *.bdimg.com, *.hao123.com, *.nuomi.com, *.chuanke.com, *.trustgo.com, " +
                        "*.bce.baidu.com, *.eyun.baidu.com, *.map.baidu.com, *.mbd.baidu.com, *.fanyi.baidu.com, " +
                        "*.baidubce.com, *.mipcdn.com, *.news.baidu.com, *.baidupcs.com, *.aipage.com, *.aipage.cn, " +
                        "*.bcehost.com, *.safe.baidu.com, *.im.baidu.com, *.baiducontent.com, *.dlnel.com, " +
                        "*.dlnel.org, *.dueros.baidu.com, *.su.baidu.com, *.91.com, *.hao123.baidu.com, " +
                        "*.apollo.auto, *.xueshu.baidu.com, *.bj.baidubce.com, *.gz.baidubce.com, *.smartapps.cn, " +
                        "*.bdtjrcv.com, *.hao222.com, *.haokan.com, *.pae.baidu.com, click.hm.baidu.com, " +
                        "log.hm.baidu.com, cm.pos.baidu.com, wn.pos.baidu.com, update.pan.baidu.com]",
                String.valueOf(AdvancedCertificateUtils.getDomainNamesFromCertificate(cert)));
    }

    @Test
    public void verifyCertificateByIssuers0() throws CertificateException, NoSuchProviderException, ParseException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X509Certificate caCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CA_CERT));
        X509Certificate rootCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(ROOT_CERT));
        // 根证书 和 CA证书都由服务端限定, 客户端上送自己的证书
        AdvancedCertificateUtils.verifyCertificateByIssuers(cert, DateTimeUtils.stringToDate("2020-05-21", "yyyy-MM-dd"), new SimpleIssuerProvider(Arrays.asList(caCert, rootCert)));
    }

    @Test
    public void verifyCertificateByIssuers1() throws CertificateException, NoSuchProviderException, ParseException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X509Certificate caCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CA_CERT));
        // CA证书由服务端限定, 且服务端把CA证书当根证书用, 客户端上送自己的证书
        AdvancedCertificateUtils.verifyCertificateByIssuers(cert, DateTimeUtils.stringToDate("2020-05-21", "yyyy-MM-dd"),
                new SimpleIssuerProvider(Collections.singletonList(new IssuerProvider.ActAsRoot(caCert))));
    }

    @Test
    public void verifyCertificateByIssuers2() throws CertificateException, NoSuchProviderException, ParseException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X509Certificate caCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CA_CERT));
        X509Certificate rootCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(ROOT_CERT));
        // 根证书由服务端限定, 客户端上送自己的证书和CA证书
        AdvancedCertificateUtils.verifyCertificateByIssuers(cert, DateTimeUtils.stringToDate("2020-05-21", "yyyy-MM-dd"), new RootIssuerProvider(Collections.singletonList(rootCert)), Collections.singletonList(caCert));
    }

//    @Test
    @Test(expected = CertificateException.class)
    public void verifyCertificateByIssuersError0() throws CertificateException, NoSuchProviderException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X509Certificate rootCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(ROOT_CERT));
        AdvancedCertificateUtils.verifyCertificateByIssuers(cert, new Date(), new RootIssuerProvider(Collections.singletonList(rootCert)), null);
    }

//    @Test
    @Test(expected = CertificateException.class)
    public void verifyCertificateByIssuersError1() throws CertificateException, NoSuchProviderException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X509Certificate caCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CA_CERT));
        AdvancedCertificateUtils.verifyCertificateByIssuers(cert, new Date(), new RootIssuerProvider(null), Collections.singletonList(caCert));
    }

//    @Test
    @Test(expected = CertificateException.class)
    public void verifyCertificateByIssuersError2() throws CertificateException, NoSuchProviderException {
        X509Certificate cert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CERT));
        X509Certificate caCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(CA_CERT));
        X509Certificate rootCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(ROOT_CERT));
        AdvancedCertificateUtils.verifyCertificateByIssuers(cert, new Date(), new RootIssuerProvider(null), Arrays.asList(caCert, rootCert));
    }

//    @Test
    @Test(expected = CertificateException.class)
    public void verifyCertificateByIssuersError3() throws CertificateException, NoSuchProviderException {
        X509Certificate rootCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(Base64Utils.decode(ROOT_CERT));
        AdvancedCertificateUtils.verifyCertificateByIssuers(rootCert, new Date(), new SimpleIssuerProvider(null));
    }

}
