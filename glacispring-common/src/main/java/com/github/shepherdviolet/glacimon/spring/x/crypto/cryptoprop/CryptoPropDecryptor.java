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

/**
 * [Spring属性加密] 属性解密器
 *
 * @author shepherdviolet
 */
public interface CryptoPropDecryptor {

    /**
     * 尝试解密
     * @param key 属性key
     * @param value 属性value, 需要判断它是否是加密属性
     * @return 返回属性值 (如果不需要解密, 也要返回原value)
     */
    String decrypt(String key, String value);

}