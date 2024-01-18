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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.*;

import java.util.Map;

import static org.springframework.context.support.PropertySourcesPlaceholderConfigurer.LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * [Spring属性解密] BeanDefinitionRegistryPostProcessor
 *
 * @author shepherdviolet
 */
public class CryptoPropBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware, EnvironmentAware {

    // APOLLO客户端添加的PropertySourcesPlaceholderConfigurer的BeanName. 当Apollo客户端认为Context中没有定义...Configurer的时候就会强制添加一个, 某些情况下我们需要将它移除
    private static final String PLACEHOLDER_CONFIGURER_NAME_ADDED_BY_APOLLO = "org.springframework.context.support.PropertySourcesPlaceholderConfigurer";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CryptoPropDecryptor decryptor;

    private ApplicationContext applicationContext;
    private Environment environment;

    public CryptoPropBeanDefinitionRegistryPostProcessor(CryptoPropDecryptor decryptor) {
        logger.info("CryptoProp | CryptoPropBeanDefinitionRegistryPostProcessor Enabled");
        this.decryptor = decryptor;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        /*
         * **********************************************************************************************************
         * 删除Apollo添加的可能多余的PropertySourcesPlaceholderConfigurer
         * **********************************************************************************************************
         * 在com.ctrip.framework.apollo.spring.config.ConfigPropertySourcesProcessor源码中, 如果发现BeanDefinitionRegistry
         * 里没有PropertySourcesPlaceholderConfigurer类型的BeanDefinition, 就会创建一个默认的.
         * 问题是, 如果你在用@Configuration标注的配置类中, 自己定义一个PropertySourcesPlaceholderConfigurer, Apollo的程序将无法判断,
         * 它还是认为上下文中没有, 还是会创建一个默认的. 这就导致PropertySourcesPlaceholderConfigurer有两个重复了.
         * PropertySourcesDeducer中如果发现上下文中有两个PropertySourcesPlaceholderConfigurer, 会告警并回退到使用Environment的
         * PropertySources来代替PropertySourcesPlaceholderConfigurer#getAppliedPropertySources返回的PropertySources.
         * 因此, 本BeanDefinitionRegistryPostProcessor的优先级被设置在APOLLO之后, 如果APOLLO创建了多余的
         * PropertySourcesPlaceholderConfigurer, 就尝试先把它删掉, 如果删掉后没有其他的PropertySourcesPlaceholderConfigurer了,
         * 再把它加回来......
         */
        BeanDefinition placeholderConfigurerAddedByApollo = null;
        if (registry.containsBeanDefinition(PLACEHOLDER_CONFIGURER_NAME_ADDED_BY_APOLLO)) {
            placeholderConfigurerAddedByApollo = registry.getBeanDefinition(PLACEHOLDER_CONFIGURER_NAME_ADDED_BY_APOLLO);
            registry.removeBeanDefinition(PLACEHOLDER_CONFIGURER_NAME_ADDED_BY_APOLLO);
            logger.debug("CryptoProp | Remove 'PropertySourcesPlaceholderConfigurer' registered by APOLLO");
        }

        /* **********************************************************************************************************
         * 从applicationContext中取出所有的PropertySourcesPlaceholderConfigurer
         * **********************************************************************************************************
         * 按照常理, BeanDefinitionRegistryPostProcessor里不应该随便地实例化(装配)Bean, PropertySourcesPlaceholderConfigurer里
         * 的PropertySource替换操作也应该在BeanFactoryPostProcessor#postProcessBeanFactory里执行.
         * 但是, Mybatis那么干了, org.mybatis.spring.mapper.MapperScannerConfigurer是个BeanDefinitionRegistryPostProcessor,
         * 优先级比BeanFactoryPostProcessor高, 而且它还提前装配了PropertyResourceConfigurer并执行了它们的postProcessBeanFactory
         * 方法. Mybatis提前初始化了PropertyResourceConfigurer, 使得我们的替换操作无法成功.
         * 因此, 我们只能把替换操作提前到BeanDefinitionRegistryPostProcessor中进行(且优先级比mybatis的高), 反正Mybatis也会提前装配
         * PropertyResourceConfigurer, 我们提前PropertySourcesPlaceholderConfigurer也不会更糟糕.
         */
        Map<String, PropertySourcesPlaceholderConfigurer> configurers = applicationContext.getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false, false);
        if (configurers.isEmpty()) {
            // 如果没有PropertySourcesPlaceholderConfigurer实例, 尝试加回Apollo添加的那个
            if (placeholderConfigurerAddedByApollo != null) {
                registry.registerBeanDefinition(PLACEHOLDER_CONFIGURER_NAME_ADDED_BY_APOLLO, placeholderConfigurerAddedByApollo);
                configurers = applicationContext.getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false, false);
                logger.debug("CryptoProp | Re-register 'PropertySourcesPlaceholderConfigurer' by APOLLO");
            }
            if (configurers.isEmpty()) {
                if ("true".equals(environment.getProperty("glacispring.cryptoProp.ignoreException"))) {
                    logger.warn("CryptoProp | WARNING! Bean of type 'PropertySourcesPlaceholderConfigurer' " +
                            "cannot be found in the spring application context, the 'CryptoProp' cannot work.");
                    return;
                }
                throw new RuntimeException("CryptoProp | WARNING! Bean of type 'PropertySourcesPlaceholderConfigurer' " +
                        "cannot be found in the spring application context, the 'CryptoProp' cannot work." +
                        "You can skip this Exception by -Dglacispring.cryptoProp.ignoreException=true");
            }
        }

        /* **********************************************************************************************************
         * 替换PropertySourcesPlaceholderConfigurer中的PropertySource, 使得它们支持解密
         * **********************************************************************************************************
         * 在在mybatis提早装载PropertySourcesPlaceholderConfigurer前, 替换掉PropertySource
         */
        for (PropertySourcesPlaceholderConfigurer configurer : configurers.values()) {
            logger.debug("CryptoProp | Replace 'PropertySource's in '" + configurer.getClass().getName() + "'");
            // 先执行postProcessBeanFactory, 创建内部的PropertySource
            ConfigurableListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            configurer.postProcessBeanFactory(beanFactory);
            // 取出PropertySources, getAppliedPropertySources拿出来的和PropertySources是同一个实例
            PropertySources propertySources = configurer.getAppliedPropertySources();
            // propertySources必须是MutablePropertySources
            if (!(propertySources instanceof MutablePropertySources)) {
                if (configurers.isEmpty()) {
                    if ("true".equals(environment.getProperty("glacispring.cryptoProp.ignoreException"))) {
                        logger.warn("CryptoProp | 'propertySources' in 'PropertySourcesPlaceholderConfigurer' " +
                                "is not an instance of MutablePropertySources, the parameter replacement function cannot work.");
                        return;
                    }
                    throw new RuntimeException("CryptoProp | 'propertySources' in 'PropertySourcesPlaceholderConfigurer' " +
                            "is not an instance of MutablePropertySources, the parameter replacement function cannot work." +
                            "You can skip this Exception by -Dglacispring.cryptoProp.ignoreException=true");
                }
            }
            // 替换掉每一个PropertySource (其实就两个, 为了以防万一就都处理一下)
            MutablePropertySources mutablePropertySources = (MutablePropertySources) propertySources;
            for (PropertySource<?> propertySource : mutablePropertySources) {
                if (LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME.equals(propertySource.getName()) && propertySource instanceof PropertiesPropertySource) {
                    // localProperties不太一样, 用CryptoPropertiesPropertySource
                    mutablePropertySources.replace(propertySource.getName(), new CryptoPropertiesPropertySource((PropertiesPropertySource) propertySource, decryptor));
                } else {
                    // environmentProperties和其他类型都用CryptoPropertySource
                    mutablePropertySources.replace(propertySource.getName(), new CryptoPropertySource(propertySource, decryptor));
                }
            }
            // 再执行一次postProcessBeanFactory, 更新一下
            configurer.setPropertySources(configurer.getAppliedPropertySources());//多此一举的操作, 这俩本来就是同一个实例. 不过, 万一哪天PropertySourcesPlaceholderConfigurer逻辑改了呢?
            configurer.postProcessBeanFactory(beanFactory);
        }
        logger.info("CryptoProp | Enabled");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            /*
             * 按照常理, BeanDefinitionRegistryPostProcessor里不应该随便地实例化(装配)Bean, PropertySourcesPlaceholderConfigurer里
             * 的PropertySource替换操作也应该在BeanFactoryPostProcessor#postProcessBeanFactory里执行.
             * 但是, Mybatis那么干了, org.mybatis.spring.mapper.MapperScannerConfigurer是个BeanDefinitionRegistryPostProcessor,
             * 优先级比BeanFactoryPostProcessor高, 而且它还提前装配了PropertyResourceConfigurer并执行了它们的postProcessBeanFactory
             * 方法. Mybatis提前初始化了PropertyResourceConfigurer, 使得我们的替换操作无法成功.
             * 因此, 我们只能把替换操作提前到BeanDefinitionRegistryPostProcessor中进行(且优先级比mybatis的高), 反正Mybatis也会提前装配
             * PropertyResourceConfigurer, 我们提前PropertySourcesPlaceholderConfigurer也不会更糟糕.
             */
    }

    @Override
    public int getOrder() {
        /*
         * 在com.ctrip.framework.apollo.spring.config.ConfigPropertySourcesProcessor(优先级HIGHEST_PRECEDENCE)之后,
         * 为了删除Apollo创建的可能多余的PropertySourcesPlaceholderConfigurer
         */
        /*
         * 在org.mybatis.spring.mapper.MapperScannerConfigurer(优先级0)之前,
         * 一定要比mybatis早, 如果呗mybatis提前初始化了PropertySourcesPlaceholderConfigurer, 就没法替换了
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

}