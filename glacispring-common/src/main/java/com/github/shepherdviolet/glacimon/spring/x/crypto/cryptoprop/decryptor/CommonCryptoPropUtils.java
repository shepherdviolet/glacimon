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

/**
 * [Spring属性解密] 通用工具类
 *
 * @author shepherdviolet
 */
public class CommonCryptoPropUtils {

    public static final String CIPHER_PREFIX = "CIPHER(";
    public static final String CIPHER_SUFFIX = ")";

    /**
     * 判断是否为密文
     */
    public static boolean isCipher(String s) {
        return s.length() > CIPHER_PREFIX.length() + CIPHER_SUFFIX.length()
                && s.startsWith(CIPHER_PREFIX)
                && s.endsWith(CIPHER_SUFFIX);
    }

    /**
     * 解除密文包装 (删掉前缀后缀)
     */
    public static String unwrapCipher(String cipher) {
        return cipher.substring(CIPHER_PREFIX.length(), cipher.length() - CIPHER_SUFFIX.length());
    }

    /**
     * 密文包装 (追加前缀后缀)
     */
    public static String wrapCipher(String cipher) {
        return CIPHER_PREFIX + cipher + CIPHER_SUFFIX;
    }

}
