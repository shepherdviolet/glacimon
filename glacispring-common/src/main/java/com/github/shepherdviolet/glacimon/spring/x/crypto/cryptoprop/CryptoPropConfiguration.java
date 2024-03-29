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
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced.DefaultCryptoPropertySourceConverterUtils;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced.ICryptoPropertySourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 解密器
     */
    @Bean(name = "glacispring.cryptoProp.decryptor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.decryptor")
    public CryptoPropDecryptor decryptor() {
        // 注意这个Bean无法通过@Value获取密钥, 只能在CryptoPropBeanDefinitionRegistryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanDefinitionRegistryPostProcessor源码.
        return new SimpleCryptoPropDecryptor();
    }

    /**
     * PropertySource转换器(切入解密逻辑), 加强模式(或CUT_IN_ENVIRONMENT模式)专用
     * 加强模式(或CUT_IN_ENVIRONMENT模式)用这个转换器, 对Environment中的PropertySource进行转换, 切入解密逻辑.
     */
    @Bean(name = "glacispring.cryptoProp.enhancedModePropertySourceConverter")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.enhancedModePropertySourceConverter")
    public ICryptoPropertySourceConverter enhancedModePropertySourceConverter(@Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor) {
        // 注意这个Bean无法通过@Value获取参数, 只能在CryptoPropBeanDefinitionRegistryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanDefinitionRegistryPostProcessor源码.
        return DefaultCryptoPropertySourceConverterUtils.createDefault(decryptor);
    }

    /**
     * 核心类, 使用支持解密的PropertySource替换并代理PropertySourcesPlaceholderConfigurer中的PropertySource,
     * 实现'@Value'和'XML property'中占位符(placeholder)的解密. (ApplicationContext中的Environment#getProperty
     * 或Environment#resolvePlaceholders方法不支持属性解密)
     */
    @Bean(name = "glacispring.cryptoProp.beanDefinitionRegistryPostProcessor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.beanDefinitionRegistryPostProcessor")
    public CryptoPropBeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor(
            @Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor,
            @Qualifier("glacispring.cryptoProp.enhancedModePropertySourceConverter") ICryptoPropertySourceConverter enhancedModePropertySourceConverter
    ) {

        // 注意这个Bean无法通过@Value获取参数, 只能在CryptoPropBeanDefinitionRegistryPostProcessor中
        // 从Environment和PropertySourcesPlaceholderConfigurer中获取参数.
        // 详见CryptoPropBeanDefinitionRegistryPostProcessor源码.
        return new CryptoPropBeanDefinitionRegistryPostProcessor(decryptor, enhancedModePropertySourceConverter);
    }

//    /**
//     * 密钥没必要支持运行时动态配置
//     * 因为CryptoPropDecryptor本身无法通过@Value获取属性, 所以我们增加一个DecryptorKeyUpdater, 实现运行时接收新密钥.
//     */
//    @Bean(name = "glacispring.cryptoProp.decryptorKeyUpdater")
//    public DecryptorKeyUpdater decryptorKeyUpdater(@Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor) {
//        return new DecryptorKeyUpdater(decryptor);
//    }
//
//    public static final class DecryptorKeyUpdater {
//
//        private final CryptoPropDecryptor decryptor;
//
//        public DecryptorKeyUpdater(CryptoPropDecryptor decryptor) {
//            this.decryptor = decryptor;
//        }
//
//        @Value("${" + OPTION_DECRYPT_KEY + ":}")
//        public void updateKey(String key) {
//            if (decryptor instanceof SimpleCryptoPropDecryptor) {
//                ((SimpleCryptoPropDecryptor) decryptor).setKey(key);
//            }
//        }
//
//    }

}
