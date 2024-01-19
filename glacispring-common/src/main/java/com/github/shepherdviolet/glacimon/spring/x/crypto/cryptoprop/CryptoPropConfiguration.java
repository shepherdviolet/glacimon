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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;

/**
 * [Spring属性解密] 配置类 (For springboot)
 *
 * @author shepherdviolet
 */
@Configuration
public class CryptoPropConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPropConfiguration.class);

    /**
     * 解密器
     */
    @Bean(name = "glacispring.cryptoProp.decryptor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.decryptor")
    public static CryptoPropDecryptor decryptor() {
        return new AutoConfigDecryptor();
    }

    private static final class AutoConfigDecryptor extends SimpleCryptoPropDecryptor {
        @Value("${glacispring.cryptoProp.key:}")
        @Override
        public void setKey(String rawKey) {
            super.setKey(rawKey);
        }
    }

    /**
     * 核心类, 使用支持解密的PropertySource替换并代理PropertySourcesPlaceholderConfigurer中的PropertySource,
     * 实现'@Value'和'XML property'中占位符(placeholder)的解密. (ApplicationContext中的Environment#getProperty
     * 或Environment#resolvePlaceholders方法不支持属性解密)
     */
    @Bean(name = "glacispring.cryptoProp.beanDefinitionRegistryPostProcessor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.beanDefinitionRegistryPostProcessor")
    public static CryptoPropBeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor(
            @Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor) {

        return new CryptoPropBeanDefinitionRegistryPostProcessor(decryptor);
    }

    /**
     * <p>辅助工具: 支持属性解密的Environment</p>
     *
     * <p>CryptoProp核心逻辑只支持'@Value'和'XML property'中占位符(placeholder)的解密.
     * ApplicationContext中的Environment#getProperty或Environment#resolvePlaceholders方法不支持属性解密.
     * 一般情况下不建议使用Environment获取属性, 因为它无法获取到'XML placeholder'声明的配置文件中的属性.
     * 如果一定要用Environment获取属性并解密, 请注入'CryptoPropEnvironment'并调用它的getProperty或resolvePlaceholders方法. </p>
     */
    @Bean(name = "glacispring.cryptoProp.cryptoPropEnvironment")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.cryptoPropEnvironment")
    @ConditionalOnProperty(name = "glacispring.cryptoProp.cryptoPropEnvironment", matchIfMissing = true)
    public static CryptoPropEnvironment cryptoPropEnvironment(ApplicationContext applicationContext,
                                                              @Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor) {

        Environment environment = applicationContext.getEnvironment();
        if (!(environment instanceof AbstractEnvironment)) {
            throw new RuntimeException("CryptoProp | 'Environment' in 'ApplicationContext' " +
                    "is not an instance of AbstractEnvironment, the 'CryptoPropEnvironment' cannot create." +
                    "You can disable 'CryptoPropEnvironment' bean by -Dglacispring.cryptoProp.cryptoPropEnvironment=false");
        }

        logger.info("CryptoProp | CryptoPropEnvironment Enabled (You can disable it by glacispring.cryptoProp.cryptoPropEnvironment=false)");
        return new CryptoPropEnvironment(((AbstractEnvironment) environment).getPropertySources()).setDecryptor(decryptor);
    }

}
