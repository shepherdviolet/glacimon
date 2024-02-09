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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor;

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.crypto.*;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropConstants;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.entity.CryptoPropCommonException;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.entity.CryptoPropDecryptException;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.entity.CryptoPropEncryptException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * [Spring属性解密] 简易版加解密工具类 (只支持aes和rsa算法, 不支持协议扩展) (配套SimpleCryptoPropDecryptor)
 *
 * @author shepherdviolet
 */
public class SimpleCryptoPropUtils extends CommonCryptoPropUtils {

    private static final byte[] CBC_IV = "1234567812345678".getBytes(StandardCharsets.UTF_8);

    public static final String KEY_NULL = "null";
    public static final DecryptKey DECRYPT_KEY_NULL = new DecryptKey(null, Algorithm.NULL, null, null, null, null);
    public static final EncryptKey ENCRYPT_KEY_NULL = new EncryptKey(null, Algorithm.NULL, null, null, null, null);

    /**
     * 解密 (输入密文带前后缀)
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
     *
     * @param cipher 密文 (密文带前后缀)
     * @param key    密钥
     */
    public static String unwrapAndDecrypt(String cipher, String key) {
        return unwrapAndDecrypt(cipher, parseDecryptKey(key));
    }

    /**
     * 解密 (输入密文带前后缀)
     *
     * @param cipher 密文 (密文带前后缀)
     * @param key    解密密钥
     */
    public static String unwrapAndDecrypt(String cipher, DecryptKey key) {
        if (CommonCryptoPropUtils.isCipher(cipher)) {
            cipher = CommonCryptoPropUtils.unwrapCipher(cipher);
        }
        return decrypt(cipher, key);
    }

    /**
     * 解密 (输入密文不带前后缀)
     *
     * @param cipher 密文 (密文不带前后缀)
     * @param key    解密密钥
     */
    static String decrypt(String cipher, DecryptKey key) {
        try {
            switch (key.getAlgorithm()) {
                case AES:
                    return new String(AESCipher.decryptCBC(
                            Base64Utils.decode(cipher),
                            key.getAesKey(),
                            CBC_IV,
                            AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING
                    ), StandardCharsets.UTF_8);
                case RSA:
                    return new String(RSACipher.decrypt(
                            Base64Utils.decode(cipher),
                            key.getRsaKey(),
                            RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING
                    ), StandardCharsets.UTF_8);
                case NULL:
                default:
                    return cipher;
            }
        } catch (Throwable t) {
            throw new CryptoPropDecryptException("Property decrypt failed, cipher: " + cipher + ", your key: " + hidePartially(key.getRawKey()) + " (hide partially)", t);
        }
    }

    /**
     * 加密 (返回密文带前后缀)
     * <pre>
     * 对称加密:
     * 密钥字符串: aes:IPRGkutx3FfsCYty (BASE64)
     * 密钥文件路径: aes:file:/home/yourname/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 密钥类路径: aes:classpath:config/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 非对称加密:
     * 公钥字符串: rsa:MIICdwIB......KM2wnjk1ZY= (DER格式)
     * 公钥文件路径: rsa:file:/home/yourname/crypto_prop_public.pem (PEM格式, 文件里不要有多余的换行)
     * 公钥类路径: rsa:classpath:config/crypto_prop_public.pem (PEM格式, 文件里不要有多余的换行)
     * </pre>
     *
     * @param plain 明文
     * @param key   密钥
     * @return 密文, 带前后缀
     */
    public static String encryptAndWrap(String plain, String key) {
        return encryptAndWrap(plain, parseEncryptKey(key));
    }

    /**
     * 加密 (返回密文带前后缀)
     *
     * @param plain 明文
     * @param key   加密密钥
     * @return 密文, 带前后缀
     */
    public static String encryptAndWrap(String plain, EncryptKey key) {
        try {
            switch (key.getAlgorithm()) {
                case AES:
                    return CommonCryptoPropUtils.wrapCipher(Base64Utils.encodeToString(AESCipher.encryptCBC(
                            plain.getBytes(StandardCharsets.UTF_8),
                            key.getAesKey(),
                            CBC_IV,
                            AESCipher.CRYPTO_ALGORITHM_AES_CBC_PKCS5PADDING)));
                case RSA:
                    return CommonCryptoPropUtils.wrapCipher(Base64Utils.encodeToString(RSACipher.encrypt(
                            plain.getBytes(StandardCharsets.UTF_8),
                            key.getRsaKey(),
                            RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING)));
                case NULL:
                default:
                    throw new IllegalArgumentException("Key algorithm is null, can not do encryption");
            }
        } catch (Throwable t) {
            throw new CryptoPropEncryptException("Property encrypt failed, plain: " + plain + ", your key: " + hidePartially(key.getRawKey()) + " (hide partially)", t);
        }
    }

    /**
     * <p>解析解密密钥</p>
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
     *
     * @param rawKey 密钥
     * @return 解密密钥实例
     * @throws CryptoPropDecryptException 异常
     */
    public static DecryptKey parseDecryptKey(String rawKey) {
        if (isKeyNull(rawKey)) {
            return DECRYPT_KEY_NULL;
        }

        Algorithm algorithm;
        byte[] aesKey = null;
        String aesKeyString = null;
        RSAPrivateKey rsaKey = null;
        String rsaKeyString = null;

        if (rawKey.startsWith("aes:classpath:")) {
            String path = rawKey.substring("aes:classpath:".length());
            try (InputStream inputStream = SimpleCryptoPropUtils.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found in classpath, path: " + rawKey.substring("aes:classpath:".length()));
                }
                aesKeyString = trimAndRemoveLineBreaks(readFromInputStream(inputStream));
                aesKey = Base64Utils.decode(aesKeyString);
                algorithm = Algorithm.AES;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (BASE64 file from classpath), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("aes:file:")) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(rawKey.substring("aes:file:".length())))) {
                aesKeyString = trimAndRemoveLineBreaks(readFromInputStream(inputStream));
                aesKey = Base64Utils.decode(aesKeyString);
                algorithm = Algorithm.AES;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (BASE64 file from filesystem), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("aes:")) {
            try {
                aesKeyString = rawKey.substring("aes:".length());
                aesKey = Base64Utils.decode(aesKeyString);
                algorithm = Algorithm.AES;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (BASE64 text), your key: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)", t);
            }
        } else if (rawKey.startsWith("rsa:classpath:")) {
            String path = rawKey.substring("rsa:classpath:".length());
            try (InputStream inputStream = SimpleCryptoPropUtils.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found in classpath, path: " + rawKey.substring("rsa:classpath:".length()));
                }
                rsaKeyString = PEMEncodeUtils.pemEncodedToX509EncodedString(readFromInputStream(inputStream));
                rsaKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(rsaKeyString));
                algorithm = Algorithm.RSA;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (PEM file from classpath), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("rsa:file:")) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(rawKey.substring("rsa:file:".length())))) {
                rsaKeyString = PEMEncodeUtils.pemEncodedToX509EncodedString(readFromInputStream(inputStream));
                rsaKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(rsaKeyString));
                algorithm = Algorithm.RSA;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (PEM file from filesystem), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("rsa:")) {
            try {
                rsaKeyString = rawKey.substring("rsa:".length());
                rsaKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(rsaKeyString));
                algorithm = Algorithm.RSA;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (DER text), your key: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)", t);
            }
        } else {
            throw new CryptoPropCommonException("Illegal key prefix (Must be aes: / aes:file: / aes:classpath: / " +
                    "rsa: / rsa:file: / rsa:classpath:), your decryption key: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)");
        }

        return new DecryptKey(rawKey, algorithm, aesKey, aesKeyString, rsaKey, rsaKeyString);
    }

    /**
     * <p>解析加密密钥</p>
     * <pre>
     * 对称加密:
     * 密钥字符串: aes:IPRGkutx3FfsCYty (BASE64)
     * 密钥文件路径: aes:file:/home/yourname/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 密钥类路径: aes:classpath:config/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 非对称加密:
     * 公钥字符串: rsa:MIICdwIB......KM2wnjk1ZY= (DER格式)
     * 公钥文件路径: rsa:file:/home/yourname/crypto_prop_public.pem (PEM格式, 文件里不要有多余的换行)
     * 公钥类路径: rsa:classpath:config/crypto_prop_public.pem (PEM格式, 文件里不要有多余的换行)
     * </pre>
     *
     * @param rawKey 密钥
     * @return 加密密钥实例
     * @throws CryptoPropDecryptException 异常
     */
    public static EncryptKey parseEncryptKey(String rawKey) {
        if (isKeyNull(rawKey)) {
            return ENCRYPT_KEY_NULL;
        }

        Algorithm algorithm;
        byte[] aesKey = null;
        String aesKeyString = null;
        RSAPublicKey rsaKey = null;
        String rsaKeyString = null;

        if (rawKey.startsWith("aes:classpath:")) {
            String path = rawKey.substring("aes:classpath:".length());
            try (InputStream inputStream = SimpleCryptoPropUtils.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found in classpath, path: " + rawKey.substring("aes:classpath:".length()));
                }
                aesKeyString = trimAndRemoveLineBreaks(readFromInputStream(inputStream));
                aesKey = Base64Utils.decode(aesKeyString);
                algorithm = Algorithm.AES;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (BASE64 file from classpath), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("aes:file:")) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(rawKey.substring("aes:file:".length())))) {
                aesKeyString = trimAndRemoveLineBreaks(readFromInputStream(inputStream));
                aesKey = Base64Utils.decode(aesKeyString);
                algorithm = Algorithm.AES;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (BASE64 file from filesystem), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("aes:")) {
            try {
                aesKeyString = rawKey.substring("aes:".length());
                aesKey = Base64Utils.decode(aesKeyString);
                algorithm = Algorithm.AES;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (BASE64 text), your key: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)", t);
            }
        } else if (rawKey.startsWith("rsa:classpath:")) {
            String path = rawKey.substring("rsa:classpath:".length());
            try (InputStream inputStream = SimpleCryptoPropUtils.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File not found in classpath, path: " + rawKey.substring("rsa:classpath:".length()));
                }
                rsaKeyString = PEMEncodeUtils.pemEncodedToX509EncodedString(readFromInputStream(inputStream));
                rsaKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(rsaKeyString));
                algorithm = Algorithm.RSA;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (PEM file from classpath), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("rsa:file:")) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(rawKey.substring("rsa:file:".length())))) {
                rsaKeyString = PEMEncodeUtils.pemEncodedToX509EncodedString(readFromInputStream(inputStream));
                rsaKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(rsaKeyString));
                algorithm = Algorithm.RSA;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (PEM file from filesystem), your key: " + rawKey, t);
            }
        } else if (rawKey.startsWith("rsa:")) {
            try {
                rsaKeyString = rawKey.substring("rsa:".length());
                rsaKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(rsaKeyString));
                algorithm = Algorithm.RSA;
            } catch (Throwable t) {
                throw new CryptoPropCommonException("Key load failed (DER text), your key: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)", t);
            }
        } else {
            throw new CryptoPropCommonException("Illegal key prefix (Must be aes: / aes:file: / aes:classpath: / " +
                    "rsa: / rsa:file: / rsa:classpath:), your encryption key: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)");
        }

        return new EncryptKey(rawKey, algorithm, aesKey, aesKeyString, rsaKey, rsaKeyString);
    }

    /**
     * 生成AES密钥(128位)
     */
    public static String generateAesKey() {
        return Base64Utils.encodeToString(AESKeyGenerator.generateAes128());
    }

    /**
     * 生成RSA密钥(1024位)
     */
    public static RsaKeyPair generateRsaKeyPair() {
        try {
            RSAKeyGenerator.RSAKeyPair keyPair = RSAKeyGenerator.generateKeyPair(1024);
            String privateKeyDer = Base64Utils.encodeToString(keyPair.getPKCS8EncodedPrivateKey());
            String publicKeyDer = Base64Utils.encodeToString(keyPair.getX509EncodedPublicKey());
            return new RsaKeyPair(privateKeyDer,
                    PEMEncodeUtils.rsaPrivateKeyToPEMEncoded(privateKeyDer),
                    publicKeyDer,
                    PEMEncodeUtils.rsaPublicKeyToPEMEncoded(publicKeyDer));
        } catch (Throwable t) {
            throw new CryptoPropCommonException("Key generate failed", t);
        }
    }

    public static boolean isKeyNull(String rawKey) {
        return CheckUtils.isEmptyOrBlank(rawKey) || KEY_NULL.equals(rawKey);
    }

    public static String hidePartially(String key) {
        if (key == null) {
            return null;
        }
        if ("true".equals(System.getProperty(CryptoPropConstants.OPTION_PRINT_KEY_UNSAFE))) {
            return key;
        }

        float percent = key.contains("file:") || key.contains("classpath:") ? 0.2f : 0.5f;
        int hideNum = (int) (key.length() * percent);
        if (hideNum < 3) {
            hideNum = 3;
        }
        if (hideNum > key.length() - 3) {
            hideNum = key.length() - 3;
        }
        if (hideNum < 0) {
            hideNum = 0;
        }

        StringBuilder stringBuilder = new StringBuilder(key.substring(0, (key.length() - hideNum)));
        while (stringBuilder.length() < key.length()) {
            stringBuilder.append("*");
        }
        return stringBuilder.toString();
    }

    private static String trimAndRemoveLineBreaks(String s) {
        if (s == null) {
            return s;
        }
        return s.replaceAll("\n", "").replaceAll("\r", "").trim();
    }


    private static String readFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int length;
        while ((length = inputStream.read(buff)) >= 0) {
            outputStream.write(buff, 0, length);
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    public enum Algorithm {

        NULL,
        AES,
        RSA

    }

    public static class RsaKeyPair {

        private final String privateKeyDer;
        private final String privateKeyPem;
        private final String publicKeyDer;
        private final String publicKeyPem;

        public RsaKeyPair(String privateKeyDer, String privateKeyPem, String publicKeyDer, String publicKeyPem) {
            this.privateKeyDer = privateKeyDer;
            this.privateKeyPem = privateKeyPem;
            this.publicKeyDer = publicKeyDer;
            this.publicKeyPem = publicKeyPem;
        }

        public String getPrivateKeyDer() {
            return privateKeyDer;
        }

        public String getPrivateKeyPem() {
            return privateKeyPem;
        }

        public String getPublicKeyDer() {
            return publicKeyDer;
        }

        public String getPublicKeyPem() {
            return publicKeyPem;
        }

        @Override
        public String toString() {
            return "[privateKeyDer]\n" + privateKeyDer +
                    "\n[publicKeyDer]\n" + publicKeyDer +
                    "\n[privateKeyPem]\n" + privateKeyPem +
                    "\n[publicKeyPem]\n" + publicKeyPem +
                    "\n[privateKeyDer sha256]\n" + sha256(privateKeyDer) +
                    "\n[publicKeyDer sha256]\n" + sha256(publicKeyDer);
        }
    }

    public static class DecryptKey {

        private final String rawKey;
        private final Algorithm algorithm;
        private final byte[] aesKey;
        private final String aesKeyString;
        private final RSAPrivateKey rsaKey;
        private final String rsaKeyString;

        public DecryptKey(String rawKey, Algorithm algorithm, byte[] aesKey, String aesKeyString, RSAPrivateKey rsaKey,  String rsaKeyString) {
            if (algorithm == null) {
                algorithm = Algorithm.NULL;
            }
            this.rawKey = rawKey;
            this.algorithm = algorithm;
            this.aesKey = aesKey;
            this.aesKeyString = aesKeyString;
            this.rsaKey = rsaKey;
            this.rsaKeyString = rsaKeyString;
        }

        public String getRawKey() {
            return rawKey;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public byte[] getAesKey() {
            return aesKey;
        }

        public RSAPrivateKey getRsaKey() {
            return rsaKey;
        }

        public String getAesKeyString() {
            return aesKeyString;
        }

        public String getRsaKeyString() {
            return rsaKeyString;
        }
    }

    public static class EncryptKey {

        private final String rawKey;
        private final Algorithm algorithm;
        private final byte[] aesKey;
        private final String aesKeyString;
        private final RSAPublicKey rsaKey;
        private final String rsaKeyString;

        public EncryptKey(String rawKey, Algorithm algorithm, byte[] aesKey, String aesKeyString, RSAPublicKey rsaKey, String rsaKeyString) {
            if (algorithm == null) {
                algorithm = Algorithm.NULL;
            }
            this.rawKey = rawKey;
            this.algorithm = algorithm;
            this.aesKey = aesKey;
            this.aesKeyString = aesKeyString;
            this.rsaKey = rsaKey;
            this.rsaKeyString = rsaKeyString;
        }

        public String getRawKey() {
            return rawKey;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public byte[] getAesKey() {
            return aesKey;
        }

        public RSAPublicKey getRsaKey() {
            return rsaKey;
        }

        public String getAesKeyString() {
            return aesKeyString;
        }

        public String getRsaKeyString() {
            return rsaKeyString;
        }
    }

}
