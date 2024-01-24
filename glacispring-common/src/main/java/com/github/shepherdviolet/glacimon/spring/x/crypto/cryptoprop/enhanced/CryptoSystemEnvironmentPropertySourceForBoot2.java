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
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * <p>[Spring属性解密] PropertySource包装类(实现解密逻辑), 适配SpringBoot2.0, 加强模式(或CUT_IN_ENVIRONMENT模式)专用</p>
 *
 * @author shepherdviolet
 */
public class CryptoSystemEnvironmentPropertySourceForBoot2 extends CryptoSystemEnvironmentPropertySource implements OriginLookup<String> {

    public CryptoSystemEnvironmentPropertySourceForBoot2(SystemEnvironmentPropertySource delegate, CryptoPropDecryptor decryptor) {
        super(delegate, decryptor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Origin getOrigin(String key) {
        if(getDelegate() instanceof OriginLookup) {
            Origin fromDelegate = ((OriginLookup<String>) getDelegate()).getOrigin(key);
            if (fromDelegate != null) {
                return fromDelegate;
            }
        }

        // 参考jasypt-spring-boot
        String property = resolvePropertyName(key);
        if (super.containsProperty(property)) {
            return new SystemEnvironmentOrigin(property);
        }
        return null;
    }

    @Override
    public boolean isImmutable() {
        if (getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<?>) getDelegate()).isImmutable();
        }
        return OriginLookup.super.isImmutable();
    }

    @Override
    public String getPrefix() {
        if (getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<?>) getDelegate()).getPrefix();
        }
        return OriginLookup.super.getPrefix();
    }

}