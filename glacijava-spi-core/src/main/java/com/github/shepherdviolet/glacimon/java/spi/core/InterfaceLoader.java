/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.java.spi.core;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.MultipleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loading interfaces defined in META-INF/glacimonspi/interfaces
 *
 * @author shepherdviolet
 */
class InterfaceLoader {

    private static final SpiLogger LOGGER = LogUtils.getLogger();

    private static final Map<String, Map<Class<?>, Boolean>> INTERFACE_CACHE = new ConcurrentHashMap<>();

    /**
     * Loading interfaces from classloader (with cache)
     */
    static Map<Class<?>, Boolean> get(ClassLoader classLoader){
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        Map<Class<?>, Boolean> interfaces = INTERFACE_CACHE.get(classloaderId);
        if (interfaces == null) {
            interfaces = InterfaceLoader.load(classLoader, "?");
            INTERFACE_CACHE.put(classloaderId, interfaces);
        }
        return interfaces;
    }

    /**
     * remove cache
     */
    static void uninstall(ClassLoader classLoader) {
        INTERFACE_CACHE.remove(ClassUtils.getClassLoaderId(classLoader));
    }

    static Map<Class<?>, Boolean> load(ClassLoader classLoader, String loaderId) {
        //load definitions
        List<InterfaceDefinition> definitions = DefinitionLoader.loadInterfacesDefinitions(classLoader, loaderId);
        //loading classes
        Map<Class<?>, Boolean> classes = new LinkedHashMap<>(definitions.size());
        for (InterfaceDefinition definition : definitions) {
            try {
                Class<?> clazz = ClassUtils.loadClass(definition.getInterfaceType(), classLoader);
                if (!clazz.isInterface()) {
                    LOGGER.error(loaderId + " | Interface register | " + definition.getInterfaceType() +
                            " must be an interface, which is defined in " + definition.getUrl(), null);
                    throw new IllegalDefinitionException(loaderId + " | Interface register | " + definition.getInterfaceType() +
                            " must be an interface, which is defined in " + definition.getUrl());
                }
                if (!clazz.isAnnotationPresent(SingleServiceInterface.class) &&
                        !clazz.isAnnotationPresent(MultipleServiceInterface.class)) {
                    LOGGER.error(loaderId + " | Interface register | " + definition.getInterfaceType() +
                            " must have an annotation '@SingleServiceInterface' or '@MultipleServiceInterface'" +
                            ", which is defined in " + definition.getUrl(), null);
                    throw new IllegalDefinitionException(loaderId + " | Interface register | " + definition.getInterfaceType() +
                            " must have an annotation '@SingleServiceInterface' or '@MultipleServiceInterface'" +
                            ", which is defined in " + definition.getUrl());
                }
                //add to linked map
                classes.put(clazz, false);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(loaderId + " | Interface register | Loaded interface " + definition.getInterfaceType() +
                            ", url:" + definition.getUrl(), null);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error(loaderId + " | Interface register | Interface class " + definition +
                        " not found, which is defined in " + definition.getUrl(), e);
                throw new IllegalDefinitionException(loaderId + " | Interface register | Interface class " + definition +
                        " not found, which is defined in " + definition.getUrl(), e);
            }
        }
        return classes;
    }

}
