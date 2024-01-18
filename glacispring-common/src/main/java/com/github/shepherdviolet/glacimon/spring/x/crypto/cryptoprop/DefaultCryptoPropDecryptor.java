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

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.crypto.AESCipher;
import com.github.shepherdviolet.glacimon.java.crypto.PEMEncodeUtils;
import com.github.shepherdviolet.glacimon.java.crypto.RSACipher;
import com.github.shepherdviolet.glacimon.java.crypto.RSAKeyGenerator;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;

/**
 * [Spring属性解密] 默认属性解密器
 *
 * @author shepherdviolet
 */
public class DefaultCryptoPropDecryptor extends CacheCryptoPropDecryptor {

    public static final String KEY_NULL = "null";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String key;

    private CryptoType cryptoType;
    private byte[] aesKey;
    private RSAPrivateKey rsaKey;

    public DefaultCryptoPropDecryptor() {
        logger.info("CryptoProp | DefaultCryptoPropDecryptor Enabled");
    }

    public DefaultCryptoPropDecryptor(String key) {
        logger.info("CryptoProp | DefaultCryptoPropDecryptor Enabled");
        setKey(key);
    }

    @Override
    protected String decrypt(String cipher) {
        try {
            switch (cryptoType) {
                case AES:
                    return new String(AESCipher.decryptCBC(
                            Base64Utils.decode(cipher),
                            aesKey,
                            "1234567812345678".getBytes(StandardCharsets.UTF_8),
                            AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING
                    ), StandardCharsets.UTF_8);
                case RSA:
                    return new String(RSACipher.decrypt(
                            Base64Utils.decode(cipher),
                            rsaKey,
                            RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING));
                case NULL:
                default:
                    return cipher;
            }
        } catch (Throwable t) {
            throw new CryptoPropDecryptException("Property decrypt failed, cipher: " + cipher + ", your key: " + hidePartially(key) + " (hide partially)", t);
        }
    }

    /**
     * <pre>
     * 对称加密:
     * 密钥字符串: aes:IPRGkutx3FfsCYty (BASE64)
     * 密钥文件路径: aes:file:/home/yourname/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 密钥类路径: aes:classpath:config/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 非对称加密:
     * 私钥字符串: rsa:MIICdwIB......KM2wnjk1ZY= (DER格式)
     * 私钥文件路径: rsa:file:/home/yourname/crypto_prop_private.pem (PEM格式, 文件里不要有多余的换行)
     * 私钥类路径: rsa:classpath:config/crypto_prop_private.pem (PEM格式, 文件里不要有多余的换行)
     * </pre>
     * @param key 密钥
     */
    public synchronized void setKey(String key) {

        if (key == null && this.key == null) {
            return;
        }
        if (key != null && key.equals(this.key)) {
            return;
        }

        if (CheckUtils.isEmptyOrBlank(key) || KEY_NULL.equals(key)) {
            this.key = key;
            this.cryptoType = CryptoType.NULL;
            this.aesKey = null;
            this.rsaKey = null;
            wipeCache();
            return;
        }

        CryptoType cryptoType;
        byte[] aesKey;
        RSAPrivateKey rsaKey;

        if (key.startsWith("aes:classpath:")) {
            try (InputStream inputStream = getClass().getResourceAsStream(key.substring("aes:classpath:".length()))) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found in classpath, path: " + key.substring("aes:classpath:".length()));
                }
                aesKey = Base64Utils.decode(readFromInputStream(inputStream));
                rsaKey = null;
                cryptoType = CryptoType.AES;
                logger.info("CryptoProp | Key set to: " + key);
            } catch (Throwable t) {
                throw new CryptoPropDecryptException("Key load failed, your key: " + key, t);
            }
        } else if (key.startsWith("aes:file:")) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(key.substring("aes:file:".length())))) {
                aesKey = Base64Utils.decode(readFromInputStream(inputStream));
                rsaKey = null;
                cryptoType = CryptoType.AES;
                logger.info("CryptoProp | Key set to: " + key);
            } catch (Throwable t) {
                throw new CryptoPropDecryptException("Key load failed, your key: " + key, t);
            }
        } else if (key.startsWith("aes:")) {
            try {
                aesKey = Base64Utils.decode(key.substring("aes:".length()));
                rsaKey = null;
                cryptoType = CryptoType.AES;
                logger.info("CryptoProp | Key set to: " + hidePartially(key) + " (hide partially)");
            } catch (Throwable t) {
                throw new CryptoPropDecryptException("Key load failed, your key: " + hidePartially(key) + " (hide partially)", t);
            }
        } else if (key.startsWith("rsa:classpath:")) {
            try (InputStream inputStream = getClass().getResourceAsStream(key.substring("rsa:classpath:".length()))) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found in classpath, path: " + key.substring("rsa:classpath:".length()));
                }
                aesKey = null;
                rsaKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(PEMEncodeUtils.pemEncodedToX509EncodedBytes(readFromInputStream(inputStream)));
                cryptoType = CryptoType.RSA;
                logger.info("CryptoProp | Key set to: " + key);
            } catch (Throwable t) {
                throw new CryptoPropDecryptException("Key load failed, your key: " + key, t);
            }
        } else if (key.startsWith("rsa:file:")) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(key.substring("rsa:file:".length())))) {
                aesKey = null;
                rsaKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(PEMEncodeUtils.pemEncodedToX509EncodedBytes(readFromInputStream(inputStream)));
                cryptoType = CryptoType.RSA;
                logger.info("CryptoProp | Key set to: " + key);
            } catch (Throwable t) {
                throw new CryptoPropDecryptException("Key load failed, your key: " + key, t);
            }
        } else if (key.startsWith("rsa:")) {
            try {
                aesKey = null;
                rsaKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(key.substring("rsa:".length())));
                cryptoType = CryptoType.RSA;
                logger.info("CryptoProp | Key set to: " + hidePartially(key) + " (hide partially)");
            } catch (Throwable t) {
                throw new CryptoPropDecryptException("Key load failed, your key: " + hidePartially(key) + " (hide partially)", t);
            }
        } else {
            throw new CryptoPropDecryptException("Illegal key prefix (Must be aes: / aes:file: / aes:classpath: / " +
                    "rsa: / rsa:file: / rsa:classpath:), your key: " + hidePartially(key) + " (hide partially)");
        }

        this.key = key;
        this.aesKey = aesKey;
        this.rsaKey = rsaKey;
        this.cryptoType = cryptoType;
        wipeCache();

    }

    protected String hidePartially(String key) {
        if (key.length() < 2) {
            return key;
        }
        StringBuilder stringBuilder = new StringBuilder(key.substring(0, (key.length() - key.length() / 2)));
        while (stringBuilder.length() < key.length()) {
            stringBuilder.append("*");
        }
        return stringBuilder.toString();
    }

    protected String readFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int length;
        while ((length = inputStream.read(buff)) >= 0) {
            outputStream.write(buff, 0, length);
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    public enum CryptoType {

        NULL,
        AES,
        RSA

    }

}
