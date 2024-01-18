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

import org.springframework.core.env.PropertiesPropertySource;

/**
 * <p>[Spring属性解密] 支持解密的PropertiesPropertySource</p>
 *
 * <p>用于代理并替换PropertySourcesPlaceholderConfigurer中名为'localProperties'的PropertySource.</p>
 *
 * @author shepherdviolet
 */
public class CryptoPropertiesPropertySource extends PropertiesPropertySource {

    private final PropertiesPropertySource provider;
    private final CryptoPropDecryptor encryptor;

    public CryptoPropertiesPropertySource(PropertiesPropertySource provider, CryptoPropDecryptor encryptor) {
        // 沿用原有的name和source
        super(provider.getName(), provider.getSource());
        this.provider = provider;
        this.encryptor = encryptor;
    }

    @Override
    public String[] getPropertyNames() {
        return provider.getPropertyNames();
    }

    @Override
    public Object getProperty(String name) {
        Object value = provider.getProperty(name);
        // 如果属性值是String则尝试解密
        if (value instanceof String) {
            return encryptor.decrypt(name, (String) value);
        }
        return value;
    }

    @Override
    public boolean containsProperty(String name) {
        return provider.containsProperty(name);
    }

}