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

/**
 * Loading interfaces defined in META-INF/glacimonspi/interfaces
 *
 * @author shepherdviolet
 */
class InterfaceLoader {

    private static final SpiLogger LOGGER = LogUtils.getLogger();

    static Map<Class<?>, Boolean> load(ClassLoader classLoader, String loaderId) {
        //load definitions
        List<InterfaceDefinition> definitions = DefinitionLoader.loadInterfacesDefinitions(classLoader, loaderId);
        //loading classes
        Map<Class<?>, Boolean> classes = new LinkedHashMap<>(definitions.size());
        for (InterfaceDefinition definition : definitions) {
            try {
                Class<?> clazz = ClassUtils.loadClass(definition.getInterfaceType(), classLoader);
                if (!clazz.isInterface()) {
                    LOGGER.error(loaderId + "|Load-Interface| " + definition.getInterfaceType() +
                            " is not an interface, which is defined in " + definition.getUrl(), null);
                    throw new IllegalDefinitionException(loaderId + "|Load-Interface| " + definition.getInterfaceType() +
                            " is not an interface, which is defined in " + definition.getUrl());
                }
                if (!clazz.isAnnotationPresent(SingleServiceInterface.class) &&
                        !clazz.isAnnotationPresent(MultipleServiceInterface.class)) {
                    LOGGER.error(loaderId + "|Load-Interface| Missing annotation '@SingleServiceInterface' or '@MultipleServiceInterface' on " +
                            definition.getInterfaceType() + ", which is defined in " + definition.getUrl(), null);
                    throw new IllegalDefinitionException(loaderId + "|Load-Interface| Missing annotation '@SingleServiceInterface' or '@MultipleServiceInterface' on " +
                            definition.getInterfaceType() + ", which is defined in " + definition.getUrl(), null);
                }
                //add to linked map
                classes.put(clazz, false);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(loaderId + "|Load-Interface| Interface: " + definition.getInterfaceType() +
                            ", url:" + definition.getUrl(), null);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error(loaderId + "|Load-Interface| Interface class " + definition +
                        " not found, which is defined in " + definition.getUrl(), e);
                throw new IllegalDefinitionException(loaderId + "|Load-Interface| Interface class " + definition +
                        " not found, which is defined in " + definition.getUrl(), e);
            }
        }
        return classes;
    }

}
