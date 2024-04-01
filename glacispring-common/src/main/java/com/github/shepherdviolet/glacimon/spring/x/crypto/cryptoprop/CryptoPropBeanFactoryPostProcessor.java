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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.*;

import java.util.Map;

/**
 * [Spring属性解密] BeanFactoryPostProcessor
 *
 * @author shepherdviolet
 */
public class CryptoPropBeanFactoryPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered, ApplicationContextAware, EnvironmentAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ICryptoPropertySourceConverter propertySourceConverter;

    private ApplicationContext applicationContext;
    private Environment environment;

    public CryptoPropBeanFactoryPostProcessor() {
        // 使用默认Decryptor
        this(new SimpleCryptoPropDecryptor());
    }

    public CryptoPropBeanFactoryPostProcessor(CryptoPropDecryptor decryptor) {
        // 使用默认Converter
        this(DefaultCryptoPropertySourceConverterUtils.createDefault(decryptor));
    }

    public CryptoPropBeanFactoryPostProcessor(ICryptoPropertySourceConverter propertySourceConverter) {
        this.propertySourceConverter = propertySourceConverter;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("CryptoProp | CryptoPropBeanFactoryPostProcessor Enabled");
        initEnvironment();
        convertPropertySources();
    }

    /**
     * 由于BeanFactoryPostProcessor早于Bean实例化, CryptoPropBeanFactoryPostProcessor自身和它依赖的
     * Bean无法通过@Value注入需要的参数, 我们只能从Environment和PropertySourcesPlaceholderConfigurer获取Spring启动早期的参数(属性).
     * CryptoPropBeanFactoryPostProcessor会创建一个CryptoPropEnv, 传递给它依赖的Bean, 供它们获取需要的参数.
     */
    private void initEnvironment() {
        if (propertySourceConverter == null) {
            throw new RuntimeException("CryptoProp | 'ICryptoPropertySourceConverter' is not configured in ApplicationContext");
        }
        Map<String, PropertySourcesPlaceholderConfigurer> configurers = applicationContext.getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false, false);
        propertySourceConverter.setEnv(new CryptoPropEnv(environment, configurers.values()));
    }

    /**
     * 将Environment中的PropertySources转换为支持解密的代理类
     */
    public void convertPropertySources() {
        if (propertySourceConverter == null) {
            throw new RuntimeException("CryptoProp | 'ICryptoPropertySourceConverter' is not configured in ApplicationContext");
        }

        if (!(environment instanceof ConfigurableEnvironment)) {
            if ("true".equals(System.getProperty(CryptoPropConstants.OPTION_IGNORE_EXCEPTION))) {
                logger.warn("CryptoProp | 'environment' in ApplicationContext " +
                        "is not an instance of ConfigurableEnvironment, the crypto properties decryption feature cannot work.");
                return;
            }
            throw new RuntimeException("CryptoProp | 'environment' in ApplicationContext " +
                    "is not an instance of ConfigurableEnvironment, the crypto properties decryption feature cannot work." +
                    ignoreExceptionPrompt());
        }

        // 将Environment中的PropertySources转换为支持解密的代理类
        propertySourceConverter.convertPropertySources(((ConfigurableEnvironment)environment).getPropertySources());
    }

    @Override
    public int getOrder() {
        /*
         * 在com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor(优先级HIGHEST_PRECEDENCE)之后,
         * 为了在Apollo往environment.getPropertySources()添加PropertySource后代理所有的PropertySource (执行postProcessBeanFactory方法时).
         */
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected String ignoreExceptionPrompt() {
        return "You can temporarily skip this exception by -D" + CryptoPropConstants.OPTION_IGNORE_EXCEPTION + "=true";
    }

}