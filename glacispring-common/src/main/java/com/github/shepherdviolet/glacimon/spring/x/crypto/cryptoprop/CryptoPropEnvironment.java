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

import org.springframework.core.env.*;

/**
 * <p>[Spring属性加密] 辅助工具: 支持属性解密的Environment</p>
 *
 * <p>CryptoProp核心逻辑只支持'@Value'和'XML property'中占位符(placeholder)的解密.
 * ApplicationContext中的Environment#getProperty或Environment#resolvePlaceholders方法不支持属性解密.
 * 一般情况下不建议使用Environment获取属性, 因为它无法获取到'XML placeholder'声明的配置文件中的属性.
 * 如果一定要用Environment获取属性并解密, 请注入'CryptoPropEnvironment'并调用它的getProperty或resolvePlaceholders方法. </p>
 *
 * @author shepherdviolet
 */
public class CryptoPropEnvironment extends AbstractEnvironment {

    public CryptoPropEnvironment(MutablePropertySources propertySources) {
        super(propertySources);
    }

    @Override
    protected ConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources) {
        // 使用特制的PropertySourcesPropertyResolver
        return new CryptoPropPropertyResolver(propertySources);
    }

    /**
     * 设置解密器
     * @param decryptor 解密器
     */
    public CryptoPropEnvironment setDecryptor(CryptoPropDecryptor decryptor) {
        ((CryptoPropPropertyResolver) getPropertyResolver()).setDecryptor(decryptor);
        return this;
    }

    /**
     * 特制的PropertySourcesPropertyResolver, 实现属性解密
     */
    public static class CryptoPropPropertyResolver extends PropertySourcesPropertyResolver {

        private CryptoPropDecryptor decryptor;

        public CryptoPropPropertyResolver(PropertySources propertySources) {
            super(propertySources);
        }

        @Override
        protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
            T result = super.getProperty(key, targetValueType, resolveNestedPlaceholders);
            // 如果属性值是String, 就送给解密器尝试解密
            if (this.decryptor != null && result instanceof String) {
                return (T) decryptor.decrypt(key, (String) result);
            }
            return result;
        }

        @Override
        protected void logKeyFound(String key, PropertySource<?> propertySource, Object value) {
            // 不打印日志
        }

        public void setDecryptor(CryptoPropDecryptor decryptor) {
            this.decryptor = decryptor;
        }

    }

}