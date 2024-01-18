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

import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;

import java.security.interfaces.RSAPrivateKey;

/**
 * [Spring属性加密] 默认属性解密器
 *
 * @author shepherdviolet
 */
public class DefaultCryptoPropDecryptor extends CacheCryptoPropDecryptor {

    private String key;

    private CryptoType cryptoType;
    private byte[] symKey;
    private RSAPrivateKey asymKey;

    public DefaultCryptoPropDecryptor() {
    }

    public DefaultCryptoPropDecryptor(String key) {
        setKey(key);
    }

    @Override
    protected String decrypt(String cipher) {
        return null;
    }

    /**
     * 密码:
     *   对称加密:
     *     密钥字符串: sym:IPRGkutx3FfsCYty
     *     密钥文件路径: sym:file:/home/yourname/crypto_prop_key.txt
     *     密钥类路径: sym:classpath:config/crypto_prop_key.txt
     *   非对称加密:
     *     私钥字符串: asym:MIICdwIB......KM2wnjk1ZY= (DER格式)
     *     私钥文件路径: asym:file:/home/yourname/crypto_prop_private.pem (PEM格式)
     *     私钥类路径: asym:classpath:config/crypto_prop_private.pem (PEM格式)
     */
    public void setKey(String key) {
        this.key = key;

        if (CheckUtils.isEmptyOrBlank(key)) {
            cryptoType = CryptoType.NULL;
            symKey = null;
            asymKey = null;
            return;
        }

        if (key.startsWith("sym:classpath:")) {
            cryptoType = CryptoType.SYM;
            // TODO
        } else if (key.startsWith("sym:file:")) {
            cryptoType = CryptoType.SYM;

        } else if (key.startsWith("sym:")) {
            cryptoType = CryptoType.SYM;

        } else if (key.startsWith("asym:classpath:")) {
            cryptoType = CryptoType.ASYM;

        } else if (key.startsWith("asym:file:")) {
            cryptoType = CryptoType.ASYM;

        } else if (key.startsWith("sym:")) {
            cryptoType = CryptoType.ASYM;

        } else {
            throw new CryptoPropDecryptException("Illegal key prefix (Must be sym: / sym:file: / sym:classpath: / " +
                    "asym: / asym:file: / asym:classpath:), your key: " + hidePartially(key) + " (hide partially)");
        }

    }

    protected static String hidePartially(String key) {
        if (key.length() < 2) {
            return key;
        }
        StringBuilder stringBuilder = new StringBuilder(key.substring(0, (key.length() - key.length() / 2)));
        while (stringBuilder.length() < key.length()) {
            stringBuilder.append("*");
        }
        return stringBuilder.toString();
    }

    public enum CryptoType {

        NULL,
        SYM,
        ASYM

    }

}
