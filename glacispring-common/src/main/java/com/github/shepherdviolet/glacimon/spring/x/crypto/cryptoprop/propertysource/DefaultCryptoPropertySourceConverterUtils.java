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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.propertysource;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropDecryptor;

public class DefaultCryptoPropertySourceConverterUtils {

    /**
     * 创建默认的ICryptoPropertySourceConverter
     */
    public static ICryptoPropertySourceConverter createDefault(CryptoPropDecryptor decryptor) {
        // 区分springboot2.0项目和其他spring项目
        boolean isBoot2 = true;
        try {
            Class.forName("org.springframework.boot.origin.OriginLookup");
        } catch (Throwable ignore) {
            isBoot2 = false;
        }

        // 注意这个Bean无法通过@Value获取参数, 只能在CryptoPropBeanFactoryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanFactoryPostProcessor源码.
        if (isBoot2) {
            return new DefaultCryptoPropertySourceConverterForBoot2(decryptor);
        }
        return new DefaultCryptoPropertySourceConverter(decryptor);
    }

}
