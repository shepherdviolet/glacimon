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

package com.github.shepherdviolet.glacimon.java.spi;

import com.github.shepherdviolet.glacimon.java.spi.core.*;

import java.util.Map;

/**
 * <p>GlacimonSpi: An implementation of Java Service Provider Interface</p>
 *
 * @author shepherdviolet
 */
public class GlacimonSpi {

    public static final String CLASS_NAME = GlacimonSpi.class.getName();

    /**
     * <p>Default service context.</p>
     * 1.Build service loader
     * 2.Cache service loader
     * 3.Preload
     */
    private static final ServiceContext SERVICE_CONTEXT = ServiceContext.getInstance(GlacimonSpi.class.getClassLoader());

    /**
     * Load single service by DEFAULT classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @return SingleServiceLoader (Cached)
     */
    public static <T> SingleServiceLoader<T> loadSingleService(Class<T> interfaceClass){
        return SERVICE_CONTEXT.loadSingleService(interfaceClass);
    }

    /**
     * Load single service by custom classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     * @return SingleServiceLoader (Cached)
     */
    public static <T> SingleServiceLoader<T> loadSingleService(Class<T> interfaceClass, ClassLoader classLoader){
        return SERVICE_CONTEXT.loadSingleService(interfaceClass, classLoader);
    }

    /**
     * Load multiple services by DEFAULT classloader.
     * multiple-service mode is used to load multiple services (has name and ordered).
     * @param interfaceClass Interface type to load
     * @return MultipleServiceLoader (Cached)
     */
    public static <T> MultipleServiceLoader<T> loadMultipleService(Class<T> interfaceClass){
        return SERVICE_CONTEXT.loadMultipleService(interfaceClass);
    }

    /**
     * Load multiple services by custom classloader.
     * multiple-service mode is used to load multiple services (has name and ordered).
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     * @return MultipleServiceLoader (Cached)
     */
    public static <T> MultipleServiceLoader<T> loadMultipleService(Class<T> interfaceClass, ClassLoader classLoader){
        return SERVICE_CONTEXT.loadMultipleService(interfaceClass, classLoader);
    }

    /**
     * Remove all loaders of specified classloader from cache.
     * @param classLoader classloader
     */
    public static void uninstall(ClassLoader classLoader) {
        SERVICE_CONTEXT.uninstall(classLoader);
    }

    /**
     * Remove all loaders of DEFAULT classloader from cache.
     */
    public static void uninstallDefaultClassloader() {
        SERVICE_CONTEXT.uninstallDefaultClassloader();
    }

    /**
     * Remove all loaders of all classloaders from cache
     */
    public static void uninstallAllClassloader() {
        SERVICE_CONTEXT.uninstallAllClassloader();
    }

    /**
     * Preload all services from specified classloader.
     * For server applications, used to discover definition errors in advance.
     * NOTICE: Preloading automatically in the Spring environment or set -Dglacimonspi.conf.preload.auto=true.
     *
     * @param classLoader classloader
     */
    public static void preload(ClassLoader classLoader) {
        SERVICE_CONTEXT.preload(classLoader);
    }

    /**
     * Preload all services from DEFAULT classloader.
     * For server applications, used to discover definition errors in advance.
     * NOTICE: Preloading automatically in the Spring environment or set -Dglacimonspi.conf.preload.auto=true.
     */
    public static void preload() {
        SERVICE_CONTEXT.preload();
    }

    /**
     * Get all preload checksums.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public static Map<String, Integer> getPreloadCheckSums() {
        return SERVICE_CONTEXT.getPreloadCheckSums();
    }

    /**
     * Get preload checksum of specified classloader.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public static Integer getPreloadCheckSum(ClassLoader classLoader) {
        return SERVICE_CONTEXT.getPreloadCheckSum(classLoader);
    }

    /**
     * Get preload checksum of DEFAULT classloader.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public static Integer getPreloadCheckSum() {
        return SERVICE_CONTEXT.getPreloadCheckSum();
    }

    private GlacimonSpi() {
    }

}
