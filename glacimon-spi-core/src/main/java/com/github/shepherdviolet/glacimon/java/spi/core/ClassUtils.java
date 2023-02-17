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

import com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalImplementationException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;

import static com.github.shepherdviolet.glacimon.java.spi.core.Constants.*;

class ClassUtils {

    static String getClassLoaderHash(ClassLoader classLoader) {
        if (classLoader == null) {
            return "000";
        }
        return String.valueOf(classLoader.hashCode() % 1000);
    }

    static String getClassLoaderName(ClassLoader classLoader) {
        if (classLoader == null) {
            return "null";
        }
        return classLoader.getClass().getName();
    }

    static String getClassLoaderId(ClassLoader classLoader){
        if (classLoader == null) {
            return "null";
        }
        return classLoader.getClass().getName() + "@" + classLoader.hashCode();
    }

    /**
     * Get current classloader (classloader of glacimonspi)
     * @return nullable
     */
    static ClassLoader getCurrentClassLoader() {
        return GlacimonSpi.class.getClassLoader();
    }

    /**
     * Get default classloader for service loading
     * @return nullable
     */
    static ClassLoader getDefaultClassLoader() {
        ClassLoader classLoader;
        if (FLAG_DEFAULT_CLASSLOADER_FROMCONTEXT) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }
        return getCurrentClassLoader();
    }

    /**
     * load class by classname
     */
    static Class<?> loadClass(String classname, ClassLoader classLoader) throws ClassNotFoundException {
        if (classLoader != null) {
            return classLoader.loadClass(classname);
        } else {
            return Class.forName(classname);
        }
    }

    /**
     * load class by classname, return null if not found
     */
    static Class<?> loadClassSafety(String classname, ClassLoader classLoader) {
        try {
            return loadClass(classname, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * load resources by resource name
     */
    static Enumeration<URL> loadResources(String resourceName, ClassLoader classLoader) throws IOException {
        if (classLoader != null) {
            return classLoader.getResources(resourceName);
        } else {
            return ClassLoader.getSystemResources(resourceName);
        }
    }

    /**
     * Load internal component
     * @param vmOption vm option name
     * @param interfaceType component interface
     * @param defaultType default implementation
     */
    static <T> T loadInternalComponent(String vmOption, Class<T> interfaceType, String defaultType) {
        String customType = System.getProperty(vmOption, "").trim();
        if ("".equals(customType)) {
            try {
                return createInternalComponent(interfaceType, defaultType);
            } catch (Exception e) {
                throw new RuntimeException("Error while loading internal component " + defaultType, e);
            }
        } else {
            try {
                return createInternalComponent(interfaceType, customType);
            } catch (Exception e) {
                throw new IllegalDefinitionException("Illegal VM option -D" + vmOption + "=" + customType + ", internal component + " + interfaceType.getName() + " loading failed", e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> T createInternalComponent(Class<T> interfaceType, String implementType) throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Constructor constructor = loadClass(implementType, getCurrentClassLoader()).getDeclaredConstructor();
        constructor.setAccessible(true);
        Object component = constructor.newInstance();
        if (!interfaceType.isAssignableFrom(component.getClass())) {
            throw new RuntimeException("Class " + implementType + " is not an instance of " + interfaceType.getName());
        }
        return (T) component;
    }

    /**
     * instantiate class
     */
    static <T> T newInstance(Class<T> implementationClass) throws Exception {
        try {
            Constructor<T> constructor = implementationClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalImplementationException("Implementation class " + implementationClass.getName() +
                    " must have a parameterless constructor", e);
        }
    }

}
