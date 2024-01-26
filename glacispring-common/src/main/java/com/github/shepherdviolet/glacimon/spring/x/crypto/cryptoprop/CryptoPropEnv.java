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
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;

/**
 * <p>[Spring属性解密] CryptoProp内部使用的参数集合</p>
 *
 * <p>由于BeanDefinitionRegistryPostProcessor早于Bean实例化, CryptoPropBeanDefinitionRegistryPostProcessor自身和它依赖的
 * Bean无法通过@Value注入需要的参数, 我们只能从Environment和PropertySourcesPlaceholderConfigurer获取Spring启动早期的参数(属性).</p>
 *
 * @author shepherdviolet
 */
public class CryptoPropEnv {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Environment system;
    private final Properties local;

    public CryptoPropEnv(Environment system, Collection<PropertySourcesPlaceholderConfigurer> propertySourcesPlaceholderConfigurers) {
        this.system = system;

        if (propertySourcesPlaceholderConfigurers == null || propertySourcesPlaceholderConfigurers.isEmpty()) {
            local = null;
            return;
        }

        Properties properties = new Properties();
        for (PropertySourcesPlaceholderConfigurer configurer : propertySourcesPlaceholderConfigurers) {
            CollectionUtils.mergePropertiesIntoMap(invokeMergeProperties(configurer), properties);
        }
        local = properties;
    }

    public String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public String getProperty(String name) {
        String value = system != null ? system.getProperty(name) : null;
        if (value == null) {
            value = local != null ? local.getProperty(name) : null;
        }
        return value;
    }

    private Properties invokeMergeProperties(PropertySourcesPlaceholderConfigurer configurer) {
        try {
            Method method = configurer.getClass().getDeclaredMethod("mergeProperties");
            method.setAccessible(true);
            return (Properties) method.invoke(configurer);
        } catch (Throwable t) {
            logger.warn("CryptoProp | invoking method 'mergeProperties' of " + configurer.getClass().getName() + " failed", t);
            return null;
        }
    }

}
