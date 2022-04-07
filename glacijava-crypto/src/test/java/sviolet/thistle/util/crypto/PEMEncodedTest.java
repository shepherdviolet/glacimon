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
import sviolet.thistle.entity.exception.IllegalParamException;

import java.security.spec.InvalidKeySpecException;

public class PEMEncodedTest {

    private static final String CERTIFICATE_PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIICLTCCAZYCCQDYD8gGqlTuDjANBgkqhkiG9w0BAQUFADBbMQswCQYDVQQGEwI4\n" +
            "NjEKMAgGA1UECAwBYTEKMAgGA1UEBwwBYTEKMAgGA1UECgwBYTEKMAgGA1UECwwB\n" +
            "YTEKMAgGA1UEAwwBYTEQMA4GCSqGSIb3DQEJARYBYTAeFw0xODAyMDgwNjM5Mjha\n" +
            "Fw0xOTAyMDgwNjM5MjhaMFsxCzAJBgNVBAYTAjg2MQowCAYDVQQIDAFhMQowCAYD\n" +
            "VQQHDAFhMQowCAYDVQQKDAFhMQowCAYDVQQLDAFhMQowCAYDVQQDDAFhMRAwDgYJ\n" +
            "KoZIhvcNAQkBFgFhMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwA7DAKe4d\n" +
            "+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8evBzB3lhvNT8cmV/08Dwj8NyoJ\n" +
            "6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1XvtkxQTfIoYRSbFa2EuvC23XO\n" +
            "+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQABMA0GCSqGSIb3DQEBBQUAA4GB\n" +
            "AJWcjSH2hAkc3LOEHGUp0mQllgmEKBftoBgLRjsOz4HjTl326ajpupyrvg5edleO\n" +
            "M5+h+nFFVDHkKk6kNSOFy9SW90T3FImJ7kZ4wwzs9mDIv508A3ZAxR4y0Hf6H23q\n" +
            "tBI5c1kSszEri+lwzy4499EhNUk19o36D4brdJU2IlIy\n" +
            "-----END CERTIFICATE-----\n";

    private static final String PRIVATE_KEY_PEM = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIICWwIBAAKBgQCwA7DAKe4d+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8ev\n" +
            "BzB3lhvNT8cmV/08Dwj8NyoJ6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1X\n" +
            "vtkxQTfIoYRSbFa2EuvC23XO+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQAB\n" +
            "AoGAYVuJNqF6vkYmNuaJvOZgcJw1lzhAM462EWW9UlDwPnRkO59Wgi+91UfIVQYd\n" +
            "p83fmorOc4On0xe3jqUJZQblGvkZF3v+qH4mvOxuFc4WiStJ1kfss/3lzr33Exsr\n" +
            "PnkP7QZXvxUjJbA9i+mGNO9GCrUwnRQPxUMDdztNB9Xz/EECQQDo/DpsG4AmZ8VK\n" +
            "qspvPv8kzw3sGzhO4zBY4lUCtzOrgWR+qBclejBhdnFRLkEPooY1t0rAjBxnVxEt\n" +
            "hPUi6QY9AkEAwWbGAocdFErTEfNrN8qvh4A/g/u/hB4VVSlc1hvg/HoIo+Txt82j\n" +
            "vBnqArY0e3ND8F6ZUdsC0egmiulPTLgz2QJAXbMa9+Fzf36abPYVJfpq+G3BRqSH\n" +
            "18os5oJX+Bif0ijetsV5UZw7mubcme6FQfl2CmJl0NxIjBMLGIhxYhHfbQJABt00\n" +
            "7eYJvCyjrSFsjsBc1nxQxMhsla3TqAAd0WOP6qYSJG79vT5JL2XkDlCVMER5BtD0\n" +
            "tBkH0pdgttFtBRYMkQJABz+cacRKSJvE7j1Cnkp4tt5B0MiLWCMm7UHCoBGaj/RU\n" +
            "YUjDoBey36dCBDtK6rQF9Le0sRY/gU35J44uSymmzQ==\n" +
            "-----END RSA PRIVATE KEY-----\n";

    private static final String CERTIFICATE_BASE64 =
            "MIICLTCCAZYCCQDYD8gGqlTuDjANBgkqhkiG9w0BAQUFADBbMQswCQYDVQQGEwI4" +
            "NjEKMAgGA1UECAwBYTEKMAgGA1UEBwwBYTEKMAgGA1UECgwBYTEKMAgGA1UECwwB" +
            "YTEKMAgGA1UEAwwBYTEQMA4GCSqGSIb3DQEJARYBYTAeFw0xODAyMDgwNjM5Mjha" +
            "Fw0xOTAyMDgwNjM5MjhaMFsxCzAJBgNVBAYTAjg2MQowCAYDVQQIDAFhMQowCAYD" +
            "VQQHDAFhMQowCAYDVQQKDAFhMQowCAYDVQQLDAFhMQowCAYDVQQDDAFhMRAwDgYJ" +
            "KoZIhvcNAQkBFgFhMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwA7DAKe4d" +
            "+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8evBzB3lhvNT8cmV/08Dwj8NyoJ" +
            "6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1XvtkxQTfIoYRSbFa2EuvC23XO" +
            "+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQABMA0GCSqGSIb3DQEBBQUAA4GB" +
            "AJWcjSH2hAkc3LOEHGUp0mQllgmEKBftoBgLRjsOz4HjTl326ajpupyrvg5edleO" +
            "M5+h+nFFVDHkKk6kNSOFy9SW90T3FImJ7kZ4wwzs9mDIv508A3ZAxR4y0Hf6H23q" +
            "tBI5c1kSszEri+lwzy4499EhNUk19o36D4brdJU2IlIy";

    private static final String PRIVATE_KEY_BASE64 =
            "MIICWwIBAAKBgQCwA7DAKe4d+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8ev" +
            "BzB3lhvNT8cmV/08Dwj8NyoJ6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1X" +
            "vtkxQTfIoYRSbFa2EuvC23XO+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQAB" +
            "AoGAYVuJNqF6vkYmNuaJvOZgcJw1lzhAM462EWW9UlDwPnRkO59Wgi+91UfIVQYd" +
            "p83fmorOc4On0xe3jqUJZQblGvkZF3v+qH4mvOxuFc4WiStJ1kfss/3lzr33Exsr" +
            "PnkP7QZXvxUjJbA9i+mGNO9GCrUwnRQPxUMDdztNB9Xz/EECQQDo/DpsG4AmZ8VK" +
            "qspvPv8kzw3sGzhO4zBY4lUCtzOrgWR+qBclejBhdnFRLkEPooY1t0rAjBxnVxEt" +
            "hPUi6QY9AkEAwWbGAocdFErTEfNrN8qvh4A/g/u/hB4VVSlc1hvg/HoIo+Txt82j" +
            "vBnqArY0e3ND8F6ZUdsC0egmiulPTLgz2QJAXbMa9+Fzf36abPYVJfpq+G3BRqSH" +
            "18os5oJX+Bif0ijetsV5UZw7mubcme6FQfl2CmJl0NxIjBMLGIhxYhHfbQJABt00" +
            "7eYJvCyjrSFsjsBc1nxQxMhsla3TqAAd0WOP6qYSJG79vT5JL2XkDlCVMER5BtD0" +
            "tBkH0pdgttFtBRYMkQJABz+cacRKSJvE7j1Cnkp4tt5B0MiLWCMm7UHCoBGaj/RU" +
            "YUjDoBey36dCBDtK6rQF9Le0sRY/gU35J44uSymmzQ==";

    @Test
    public void common() throws InvalidKeySpecException, IllegalParamException {

        Assert.assertEquals(
                CERTIFICATE_PEM,
                PEMEncodeUtils.certificateToPEMEncoded(CERTIFICATE_BASE64)
        );

        Assert.assertEquals(
                PRIVATE_KEY_PEM,
                PEMEncodeUtils.rsaPrivateKeyToPEMEncoded(PRIVATE_KEY_BASE64)
        );

        Assert.assertEquals(
                CERTIFICATE_BASE64,
                PEMEncodeUtils.pemEncodedToX509EncodedString(CERTIFICATE_PEM)
        );

        Assert.assertEquals(
                PRIVATE_KEY_BASE64,
                PEMEncodeUtils.pemEncodedToX509EncodedString(PRIVATE_KEY_PEM)
        );

        RSAKeyGenerator.RSAKeyPair keyPair = RSAKeyGenerator.generateKeyPair();
        String pem = PEMEncodeUtils.rsaPrivateKeyToPEMEncoded(keyPair.getPKCS8EncodedPrivateKey());
//        System.out.println(pem);
    }

}
