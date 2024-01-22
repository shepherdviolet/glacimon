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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 解密器
     */
    @Bean(name = "glacispring.cryptoProp.decryptor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.decryptor")
    public CryptoPropDecryptor decryptor(Environment environment) {
        // 这里无法通过@Value获取密钥, 只能用Environment#getProperty获取,
        // 因为BeanDefinitionRegistryPostProcessor执行过早, 它依赖的Bean无法通过@Value获取属性.
        // Apollo配置中心的属性Environment#getProperty也能拿到, 但是, 无法在运行时接收新密钥 (密钥变更后需要重启应用).
        // 为了使SimpleCryptoPropDecryptor运行时接收新密钥, 下面加了一个DecryptorKeyUpdater.
        return new SimpleCryptoPropDecryptor(environment.getProperty("glacispring.crypto-prop.key", "")) {
            @Override
            protected void printLogWhenKeyNull(String name, String value) {
                logger.warn("CryptoProp | Can not decrypt cipher '" + value + "', because the decrypt key 'glacispring.crypto-prop.key' is null");
            }
        };
    }

    /**
     * 因为CryptoPropDecryptor本身无法通过@Value获取属性, 所以我们增加一个DecryptorKeyUpdater, 实现运行时接收新密钥.
     */
    @Bean(name = "glacispring.cryptoProp.decryptorKeyUpdater")
    public DecryptorKeyUpdater decryptorKeyUpdater(@Qualifier("glacispring.cryptoProp.decryptor") CryptoPropDecryptor decryptor) {
        return new DecryptorKeyUpdater(decryptor);
    }

    public static final class DecryptorKeyUpdater {

        private final CryptoPropDecryptor decryptor;

        public DecryptorKeyUpdater(CryptoPropDecryptor decryptor) {
            this.decryptor = decryptor;
        }

        @Value("${glacispring.crypto-prop.key:}")
        public void updateKey(String key) {
            if (decryptor instanceof SimpleCryptoPropDecryptor) {
                ((SimpleCryptoPropDecryptor) decryptor).setKey(key);
            }
        }

    }

    /**
     * 核心类, 使用支持解密的PropertySource替换并代理PropertySourcesPlaceholderConfigurer中的PropertySource,
     * 实现'@Value'和'XML property'中占位符(placeholder)的解密. (ApplicationContext中的Environment#getProperty
     * 或Environment#resolvePlaceholders方法不支持属性解密)
     */
    @Bean(name = "glacispring.cryptoProp.beanDefinitionRegistryPostProcessor")
    @ConditionalOnMissingBean(name = "glacispring.cryptoProp.beanDefinitionRegistryPostProcessor")
    public CryptoPropBeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor(
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
    public CryptoPropEnvironment cryptoPropEnvironment(ApplicationContext applicationContext,
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
