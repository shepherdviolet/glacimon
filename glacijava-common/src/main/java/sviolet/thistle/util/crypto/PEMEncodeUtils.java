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

import sviolet.thistle.entity.exception.IllegalParamException;
import sviolet.thistle.util.conversion.Base64Utils;

/**
 * 证书/密钥PEM格式转换工具
 *
 * @author S.Violet
 */
public class PEMEncodeUtils {

    private static final int MAX_DATA_LENGTH = 1024 * 1024;

    private static final String NEWLINE = "\n";

    private static final String HEAD_PREFIX = "-----BEGIN ";
    private static final String HEAD_SUFFIX = "-----\n";
    private static final String TAIL_PREFIX = "-----END ";
    private static final String TAIL_SUFFIX = "-----\n";

    private static final int HEAD_TAIL_LENGTH = HEAD_PREFIX.length() + HEAD_SUFFIX.length() + TAIL_PREFIX.length() + TAIL_SUFFIX.length();

    /**
     * 将证书/密钥Base64 String转换为PEM格式, 即加上标题, 并每64字符换行
     * <p>
     * 例如:
     * <p>
     * <pre>{@code
     *  -----BEGIN RSA PRIVATE KEY-----
     *  MIICWwIBAAKBgQCwA7DAKe4d+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8ev
     *  BzB3lhvNT8cmV/08Dwj8NyoJ6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1X
     *  vtkxQTfIoYRSbFa2EuvC23XO+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQAB
     *  AoGAYVuJNqF6vkYmNuaJvOZgcJw1lzhAM462EWW9UlDwPnRkO59Wgi+91UfIVQYd
     *  p83fmorOc4On0xe3jqUJZQblGvkZF3v+qH4mvOxuFc4WiStJ1kfss/3lzr33Exsr
     *  PnkP7QZXvxUjJbA9i+mGNO9GCrUwnRQPxUMDdztNB9Xz/EECQQDo/DpsG4AmZ8VK
     *  qspvPv8kzw3sGzhO4zBY4lUCtzOrgWR+qBclejBhdnFRLkEPooY1t0rAjBxnVxEt
     *  hPUi6QY9AkEAwWbGAocdFErTEfNrN8qvh4A/g/u/hB4VVSlc1hvg/HoIo+Txt82j
     *  vBnqArY0e3ND8F6ZUdsC0egmiulPTLgz2QJAXbMa9+Fzf36abPYVJfpq+G3BRqSH
     *  18os5oJX+Bif0ijetsV5UZw7mubcme6FQfl2CmJl0NxIjBMLGIhxYhHfbQJABt00
     *  7eYJvCyjrSFsjsBc1nxQxMhsla3TqAAd0WOP6qYSJG79vT5JL2XkDlCVMER5BtD0
     *  tBkH0pdgttFtBRYMkQJABz+cacRKSJvE7j1Cnkp4tt5B0MiLWCMm7UHCoBGaj/RU
     *  YUjDoBey36dCBDtK6rQF9Le0sRY/gU35J44uSymmzQ==
     *  -----END RSA PRIVATE KEY-----
     * }</pre>
     *
     * @param content 证书/密钥Base64 String
     * @param title   标题
     * @return PEM格式的证书/秘钥字符
     */
    public static String toPEMEncoded(String content, String title) {
        if (title == null) {
            throw new NullPointerException("title is null");
        }
        if (content == null) {
            return null;
        }
        if (content.length() > MAX_DATA_LENGTH) {
            throw new RuntimeException("content length out of limit, current:" + content.length() + ", limit:" + MAX_DATA_LENGTH);
        }

        int lineNum = (content.length() >> 6) + 1;

        StringBuilder stringBuilder = new StringBuilder(
                content.length() +
                        lineNum +
                        (title.length() << 1) +
                        HEAD_TAIL_LENGTH);
        stringBuilder.append(HEAD_PREFIX);
        stringBuilder.append(title);
        stringBuilder.append(HEAD_SUFFIX);

        int line = 0;
        int start;
        for (; line < lineNum - 1; line++) {
            start = line << 6;
            stringBuilder.append(content, start, start + 64);
            stringBuilder.append(NEWLINE);
        }

        start = line << 6;
        if (start < content.length() - 1) {
            stringBuilder.append(content, start, content.length());
            stringBuilder.append(NEWLINE);
        }

        stringBuilder.append(TAIL_PREFIX);
        stringBuilder.append(title);
        stringBuilder.append(TAIL_SUFFIX);
        return stringBuilder.toString();
    }

    /**
     * 将证书/密钥二进制转换为PEM格式, 即加上标题, 并每64字符换行
     * <p>
     * 例如:
     * <p>
     * <pre>{@code
     *  -----BEGIN RSA PRIVATE KEY-----
     *  MIICWwIBAAKBgQCwA7DAKe4d+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8ev
     *  BzB3lhvNT8cmV/08Dwj8NyoJ6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1X
     *  vtkxQTfIoYRSbFa2EuvC23XO+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQAB
     *  AoGAYVuJNqF6vkYmNuaJvOZgcJw1lzhAM462EWW9UlDwPnRkO59Wgi+91UfIVQYd
     *  p83fmorOc4On0xe3jqUJZQblGvkZF3v+qH4mvOxuFc4WiStJ1kfss/3lzr33Exsr
     *  PnkP7QZXvxUjJbA9i+mGNO9GCrUwnRQPxUMDdztNB9Xz/EECQQDo/DpsG4AmZ8VK
     *  qspvPv8kzw3sGzhO4zBY4lUCtzOrgWR+qBclejBhdnFRLkEPooY1t0rAjBxnVxEt
     *  hPUi6QY9AkEAwWbGAocdFErTEfNrN8qvh4A/g/u/hB4VVSlc1hvg/HoIo+Txt82j
     *  vBnqArY0e3ND8F6ZUdsC0egmiulPTLgz2QJAXbMa9+Fzf36abPYVJfpq+G3BRqSH
     *  18os5oJX+Bif0ijetsV5UZw7mubcme6FQfl2CmJl0NxIjBMLGIhxYhHfbQJABt00
     *  7eYJvCyjrSFsjsBc1nxQxMhsla3TqAAd0WOP6qYSJG79vT5JL2XkDlCVMER5BtD0
     *  tBkH0pdgttFtBRYMkQJABz+cacRKSJvE7j1Cnkp4tt5B0MiLWCMm7UHCoBGaj/RU
     *  YUjDoBey36dCBDtK6rQF9Le0sRY/gU35J44uSymmzQ==
     *  -----END RSA PRIVATE KEY-----
     * }</pre>
     *
     * @param content 证书/密钥二进制
     * @param title   标题
     * @return PEM格式的证书/秘钥字符
     */
    public static String toPEMEncoded(byte[] content, String title) {
        if (content == null){
            return null;
        }
        //no chinese, only base64 chars
        return toPEMEncoded(Base64Utils.encodeToString(content), title);
    }

    public static String rsaPrivateKeyToPEMEncoded(String content) {
        return toPEMEncoded(content, "RSA PRIVATE KEY");
    }

    public static String rsaPrivateKeyToPEMEncoded(byte[] content) {
        return toPEMEncoded(content, "RSA PRIVATE KEY");
    }

    public static String rsaPublicKeyToPEMEncoded(String content) {
        return toPEMEncoded(content, "RSA PUBLIC KEY");
    }

    public static String rsaPublicKeyToPEMEncoded(byte[] content) {
        return toPEMEncoded(content, "RSA PUBLIC KEY");
    }

    public static String certificateToPEMEncoded(String content) {
        return toPEMEncoded(content, "CERTIFICATE");
    }

    public static String certificateToPEMEncoded(byte[] content) {
        return toPEMEncoded(content, "CERTIFICATE");
    }

    public static String sm2PrivateKeyToPEMEncoded(String content) {
        return toPEMEncoded(content, "EC PRIVATE KEY");
    }

    public static String sm2PrivateKeyToPEMEncoded(byte[] content) {
        return toPEMEncoded(content, "EC PRIVATE KEY");
    }

    public static String sm2PublicKeyToPEMEncoded(String content) {
        return toPEMEncoded(content, "EC PUBLIC KEY");
    }

    public static String sm2PublicKeyToPEMEncoded(byte[] content) {
        return toPEMEncoded(content, "EC PUBLIC KEY");
    }

    /**
     * 将PEM格式的文本转换为X509 Base64 String, 即砍掉标题, 去除换行
     * <p>
     * 例如:
     * <p>
     * <pre>{@code
     *  -----BEGIN RSA PRIVATE KEY-----
     *  MIICWwIBAAKBgQCwA7DAKe4d+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8ev
     *  BzB3lhvNT8cmV/08Dwj8NyoJ6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1X
     *  vtkxQTfIoYRSbFa2EuvC23XO+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQAB
     *  AoGAYVuJNqF6vkYmNuaJvOZgcJw1lzhAM462EWW9UlDwPnRkO59Wgi+91UfIVQYd
     *  p83fmorOc4On0xe3jqUJZQblGvkZF3v+qH4mvOxuFc4WiStJ1kfss/3lzr33Exsr
     *  PnkP7QZXvxUjJbA9i+mGNO9GCrUwnRQPxUMDdztNB9Xz/EECQQDo/DpsG4AmZ8VK
     *  qspvPv8kzw3sGzhO4zBY4lUCtzOrgWR+qBclejBhdnFRLkEPooY1t0rAjBxnVxEt
     *  hPUi6QY9AkEAwWbGAocdFErTEfNrN8qvh4A/g/u/hB4VVSlc1hvg/HoIo+Txt82j
     *  vBnqArY0e3ND8F6ZUdsC0egmiulPTLgz2QJAXbMa9+Fzf36abPYVJfpq+G3BRqSH
     *  18os5oJX+Bif0ijetsV5UZw7mubcme6FQfl2CmJl0NxIjBMLGIhxYhHfbQJABt00
     *  7eYJvCyjrSFsjsBc1nxQxMhsla3TqAAd0WOP6qYSJG79vT5JL2XkDlCVMER5BtD0
     *  tBkH0pdgttFtBRYMkQJABz+cacRKSJvE7j1Cnkp4tt5B0MiLWCMm7UHCoBGaj/RU
     *  YUjDoBey36dCBDtK6rQF9Le0sRY/gU35J44uSymmzQ==
     *  -----END RSA PRIVATE KEY-----
     * }</pre>
     *
     * @param pemEncoded PEM格式的文本
     * @return X509 Base64 String
     */
    public static String pemEncodedToX509EncodedString(String pemEncoded) throws IllegalParamException {
        if (pemEncoded == null) {
            return null;
        }
        int titleStart = pemEncoded.indexOf("-----BEGIN");
        int titleEnd = pemEncoded.indexOf("-----", titleStart + 10);
        int tailStart = pemEncoded.indexOf("-----END", titleEnd + 5);
        if (titleStart < 0 || titleStart >= titleEnd || titleEnd >= tailStart) {
            throw new IllegalParamException("Invalid pem encoded text:" + pemEncoded);
        }
        return pemEncoded.substring(titleEnd + 5, tailStart)
                .replaceAll("\\r", "")
                .replaceAll("\\n", "");
    }

    /**
     * 将PEM格式的文本转换为X509二进制数据, 即砍掉标题, 去除换行
     * <p>
     * 例如:
     * <p>
     * <pre>{@code
     *  -----BEGIN RSA PRIVATE KEY-----
     *  MIICWwIBAAKBgQCwA7DAKe4d+kwbt4T5i8vKP4SF0zju3BgDBW3CUlyfLvtTM8ev
     *  BzB3lhvNT8cmV/08Dwj8NyoJ6XLGegEsOYdQl7qsoJCm1uQbnI0M59Pg05/V1T1X
     *  vtkxQTfIoYRSbFa2EuvC23XO+FAp4vwia0jR7P2vHHN5zqg/GUDom5xwtQIDAQAB
     *  AoGAYVuJNqF6vkYmNuaJvOZgcJw1lzhAM462EWW9UlDwPnRkO59Wgi+91UfIVQYd
     *  p83fmorOc4On0xe3jqUJZQblGvkZF3v+qH4mvOxuFc4WiStJ1kfss/3lzr33Exsr
     *  PnkP7QZXvxUjJbA9i+mGNO9GCrUwnRQPxUMDdztNB9Xz/EECQQDo/DpsG4AmZ8VK
     *  qspvPv8kzw3sGzhO4zBY4lUCtzOrgWR+qBclejBhdnFRLkEPooY1t0rAjBxnVxEt
     *  hPUi6QY9AkEAwWbGAocdFErTEfNrN8qvh4A/g/u/hB4VVSlc1hvg/HoIo+Txt82j
     *  vBnqArY0e3ND8F6ZUdsC0egmiulPTLgz2QJAXbMa9+Fzf36abPYVJfpq+G3BRqSH
     *  18os5oJX+Bif0ijetsV5UZw7mubcme6FQfl2CmJl0NxIjBMLGIhxYhHfbQJABt00
     *  7eYJvCyjrSFsjsBc1nxQxMhsla3TqAAd0WOP6qYSJG79vT5JL2XkDlCVMER5BtD0
     *  tBkH0pdgttFtBRYMkQJABz+cacRKSJvE7j1Cnkp4tt5B0MiLWCMm7UHCoBGaj/RU
     *  YUjDoBey36dCBDtK6rQF9Le0sRY/gU35J44uSymmzQ==
     *  -----END RSA PRIVATE KEY-----
     * }</pre>
     *
     * @param pemEncoded PEM格式的文本
     * @return X509 byte[]
     */
    public static byte[] pemEncodedToX509EncodedBytes(String pemEncoded) throws IllegalParamException {
        return Base64Utils.decode(pemEncodedToX509EncodedString(pemEncoded));
    }

}