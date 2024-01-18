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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [Spring属性解密] 带缓存的属性解密器
 *
 * @author shepherdviolet
 */
public abstract class CacheCryptoPropDecryptor implements CryptoPropDecryptor {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public String decrypt(String key, String value) {
        if (key == null || value == null) {
            return value;
        }
        String plain = cache.get(value);
        if (plain != null) {
            return plain;
        }
        if (!isCipher(value)) {
            return value;
        }
        String cipher = unwrapCipher(value);
        if (cipher == null) {
            throw new CryptoPropDecryptException("Property decrypt failed, raw cipher text from method 'unwrapCipher' " +
                    "is null, key: " + key + ", cipher value: " + value);
        }
        try {
            plain = decrypt(cipher);
        } catch (Throwable t) {
            throw new CryptoPropDecryptException("Property decrypt failed, key: " + key + ", cipher value: " + value, t);
        }
        if (plain == null) {
            throw new CryptoPropDecryptException("Property decrypt failed, the decrypted plain text is null, " +
                    "key: " + key + ", cipher value: " + value);
        }
        if (plain.equals(cipher)) {
            // 没解密
            return value;
        }
        if (needCache(value) && needCache(plain)) {
            cache.put(value, plain);
        }
        return plain;
    }

    protected boolean needCache(String s) {
        return s.length() < 1024 * 1024;
    }

    protected boolean isCipher(String s) {
        return s.length() > 5 && s.startsWith("ENC(") && s.endsWith(")");
    }

    protected String unwrapCipher(String cipher) {
        return cipher.substring(4, cipher.length() - 1);
    }

    protected void wipeCache() {
        cache.clear();
    }

    /**
     * 实现解密逻辑
     * @param cipher 密文, 已经去掉前缀和后缀, 不为空
     * @return 如果不解密, 则返回密文(cipher), 不要返回null
     */
    protected abstract String decrypt(String cipher);

}
