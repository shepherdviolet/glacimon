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
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * <p>[Spring属性解密] PropertySource包装类(实现解密逻辑)</p>
 *
 * @author shepherdviolet
 */
public class CryptoEnumerablePropertySource<T> extends EnumerablePropertySource<T> implements ICryptoPropertySource<T> {

    private final EnumerablePropertySource<T> delegate;
    private final CryptoPropDecryptor decryptor;

    public CryptoEnumerablePropertySource(EnumerablePropertySource<T> delegate, CryptoPropDecryptor decryptor) {
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
    public String[] getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }

}