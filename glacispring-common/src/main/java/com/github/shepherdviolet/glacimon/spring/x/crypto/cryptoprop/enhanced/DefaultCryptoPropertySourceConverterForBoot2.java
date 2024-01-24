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
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

public class DefaultCryptoPropertySourceConverterForBoot2 extends DefaultCryptoPropertySourceConverter {

    public DefaultCryptoPropertySourceConverterForBoot2(CryptoPropDecryptor decryptor, String skipPropertySourceClasses) {
        super(decryptor, skipPropertySourceClasses);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> PropertySource<T> wrapPropertySource(PropertySource<T> propertySource) {
        PropertySource<T> cryptoPropertySource;
        if (propertySource instanceof SystemEnvironmentPropertySource) {
            cryptoPropertySource = (PropertySource<T>) new CryptoSystemEnvironmentPropertySourceForBoot2((SystemEnvironmentPropertySource) propertySource, getDecryptor());
        } else if (propertySource instanceof MapPropertySource) {
            cryptoPropertySource = (PropertySource<T>) new CryptoMapPropertySourceForBoot2((MapPropertySource) propertySource, getDecryptor());
        } else if (propertySource instanceof EnumerablePropertySource) {
            cryptoPropertySource = new CryptoEnumerablePropertySourceForBoot2<>((EnumerablePropertySource) propertySource, getDecryptor());
        } else {
            cryptoPropertySource = new CryptoPropertySourceForBoot2<>(propertySource, getDecryptor());
        }
        return cryptoPropertySource;
    }

}
