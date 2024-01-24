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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropDecryptor;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

public class CryptoSystemEnvironmentPropertySource extends SystemEnvironmentPropertySource implements ICryptoPropertySource<Map<String, Object>> {

    private final SystemEnvironmentPropertySource delegate;
    private final CryptoPropDecryptor decryptor;

    public CryptoSystemEnvironmentPropertySource(SystemEnvironmentPropertySource delegate, CryptoPropDecryptor decryptor) {
        super(delegate.getName(), delegate.getSource());
        this.delegate = delegate;
        this.decryptor = decryptor;
    }

    @Override
    public Object getProperty(String name) {
        Object value = delegate.getProperty(name);
        // 如果属性值是String则尝试解密
        if (value instanceof String) {
            return decryptor.decrypt(name, (String) value);
        }
        return value;
    }

    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return delegate;
    }

}