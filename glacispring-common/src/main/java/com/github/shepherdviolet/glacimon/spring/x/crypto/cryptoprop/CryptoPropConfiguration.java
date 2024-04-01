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

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor.SimpleCryptoPropDecryptor;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.propertysource.DefaultCryptoPropertySourceConverterUtils;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.propertysource.ICryptoPropertySourceConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Spring属性解密] 配置类 (For springboot)
 *
 * @author shepherdviolet
 */
@Configuration
public class CryptoPropConfiguration {
    
    /**
     * 解密器
     */
    @Bean(name = "glacispring.cryptoProp.decryptor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.decryptor")
    public CryptoPropDecryptor decryptor() {
        // 注意这个Bean无法通过@Value获取密钥, 只能在CryptoPropBeanFactoryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanFactoryPostProcessor源码.
        return new SimpleCryptoPropDecryptor();
    }

    /**
     * PropertySource转换器(切入解密逻辑)
     * 对Environment中的PropertySource进行转换, 切入解密逻辑.
     */
    @Bean(name = "glacispring.cryptoProp.cryptoPropertySourceConverter")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.cryptoPropertySourceConverter")
    public ICryptoPropertySourceConverter cryptoPropertySourceConverter(@Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor) {
        // 注意这个Bean无法通过@Value获取参数, 只能在CryptoPropBeanFactoryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanFactoryPostProcessor源码.
        return DefaultCryptoPropertySourceConverterUtils.createDefault(decryptor);
    }

    /**
     * 核心类, 使用支持解密的PropertySource替换并代理PropertySourcesPlaceholderConfigurer中的PropertySource,
     * 实现'@Value'和'XML property'中占位符(placeholder)的解密. (ApplicationContext中的Environment#getProperty
     * 或Environment#resolvePlaceholders方法不支持属性解密)
     */
    @Bean(name = "glacispring.cryptoProp.beanFactoryPostProcessor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.beanFactoryPostProcessor")
    public CryptoPropBeanFactoryPostProcessor beanFactoryPostProcessor(
            @Qualifier("glacispring.cryptoProp.cryptoPropertySourceConverter") ICryptoPropertySourceConverter cryptoPropertySourceConverter) {
        // 注意这个Bean无法通过@Value获取参数, 只能在CryptoPropBeanFactoryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanFactoryPostProcessor源码.
        return new CryptoPropBeanFactoryPostProcessor(cryptoPropertySourceConverter);
    }

}
