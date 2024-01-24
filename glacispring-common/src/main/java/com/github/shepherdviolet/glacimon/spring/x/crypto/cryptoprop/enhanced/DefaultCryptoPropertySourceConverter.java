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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.env.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>[Spring属性解密] PropertySource转换器(切入解密逻辑), 加强模式(或CUT_IN_ENVIRONMENT模式)专用</p>
 *
 * <p>加强模式(或CUT_IN_ENVIRONMENT模式)用这个转换器, 对Environment中的PropertySource进行转换, 切入解密逻辑.</p>
 *
 * @author shepherdviolet
 */
public class DefaultCryptoPropertySourceConverter implements ICryptoPropertySourceConverter {

    // 这些PropertySource不转换
    private static final List<String> DEFAULT_SKIP_PROPERTY_SOURCE_CLASSES = Arrays.asList(
            "org.springframework.core.env.PropertySource$StubPropertySource",
            "org.springframework.boot.context.properties.source.ConfigurationPropertySourcesPropertySource"
    );

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final CryptoPropDecryptor decryptor;
    private final Set<String> skipPropertySourceClasses;
    private boolean interceptByProxy;

    /**
     * @param decryptor 解密器
     * @param skipPropertySourceClasses 指定哪些PropertySource不转换(多个用','分割)
     * @param interceptByProxy true:优先使用代理切入, false:使用包装类切入
     */
    public DefaultCryptoPropertySourceConverter(CryptoPropDecryptor decryptor, String skipPropertySourceClasses, boolean interceptByProxy) {
        this.decryptor = decryptor;
        this.skipPropertySourceClasses = new HashSet<>(DEFAULT_SKIP_PROPERTY_SOURCE_CLASSES);
        this.interceptByProxy = interceptByProxy;

        // 指定哪些PropertySource不转换
        if (skipPropertySourceClasses != null && !skipPropertySourceClasses.isEmpty()) {
            String[] classNames = skipPropertySourceClasses.split(",");
            for (String className : classNames) {
                className = className.trim();
                if (className.isEmpty()) {
                    continue;
                }
                this.skipPropertySourceClasses.add(className);
            }
        }
    }

    @Override
    public void convertPropertySources(MutablePropertySources propertySources) {
        if (propertySources == null) {
            return;
        }
        for (PropertySource<?> propertySource : propertySources) {
            // 已经切入过的跳过
            if (propertySource instanceof ICryptoPropertySource) {
                continue;
            }
            // 指定PropertySource不转换
            if (skipPropertySourceClasses.contains(propertySource.getClass().getName())) {
                logger.info("CryptoProp | Enhanced | Skip PropertySource, name: " + propertySource.getName() + ", class: " + propertySource.getClass().getName());
                continue;
            }
            // 转换
            PropertySource<?> cryptoPropertySource = interceptByProxy ? proxyPropertySource(propertySource) : wrapPropertySource(propertySource);
            // 替换
            propertySources.replace(cryptoPropertySource.getName(), cryptoPropertySource);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> PropertySource<T> proxyPropertySource(PropertySource<T> propertySource) {
        // 有些PropertySource是final类型的, 没办法做代理, 只能用包装模式, 例如: CommandLinePropertySource/OriginTrackedMapPropertySource
        if (CommandLinePropertySource.class.isAssignableFrom(propertySource.getClass())
                || Modifier.isFinal(propertySource.getClass().getModifiers())) {
            return wrapPropertySource(propertySource);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(propertySource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(ICryptoPropertySource.class);
        proxyFactory.setTarget(propertySource);
        proxyFactory.addAdvice(new CryptoPropertySourceMethodInterceptor<>(propertySource, decryptor));
        logger.info("CryptoProp | Enhanced | PropertySource '" + propertySource.getName() + "' " + propertySource.getClass().getName() + " proxied");
        return (PropertySource<T>) proxyFactory.getProxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> PropertySource<T> wrapPropertySource(PropertySource<T> propertySource) {
        PropertySource<T> cryptoPropertySource;
        if (propertySource instanceof SystemEnvironmentPropertySource) {
            cryptoPropertySource = (PropertySource<T>) new CryptoSystemEnvironmentPropertySource((SystemEnvironmentPropertySource) propertySource, decryptor);
        } else if (propertySource instanceof MapPropertySource) {
            cryptoPropertySource = (PropertySource<T>) new CryptoMapPropertySource((MapPropertySource) propertySource, decryptor);
        } else if (propertySource instanceof EnumerablePropertySource) {
            cryptoPropertySource = new CryptoEnumerablePropertySource<>((EnumerablePropertySource) propertySource, decryptor);
        } else {
            cryptoPropertySource = new CryptoPropertySource<>(propertySource, decryptor);
        }
        logger.info("CryptoProp | Enhanced | PropertySource '" + propertySource.getName() + "' " + propertySource.getClass().getName() + " wrapped to " + cryptoPropertySource.getClass().getName());
        return cryptoPropertySource;
    }

    protected CryptoPropDecryptor getDecryptor() {
        return decryptor;
    }

}
