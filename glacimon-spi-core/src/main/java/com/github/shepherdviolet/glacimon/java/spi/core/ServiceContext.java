/*
 * Copyright (C) 2022-2023 S.Violet
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
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.MultipleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.shepherdviolet.glacimon.java.spi.core.Constants.*;

/**
 * <p>Service context.</p>
 *
 * 1.Build service loader
 * 2.Cache service loader
 * 3.Preload
 *
 * @author shepherdviolet
 */
public class ServiceContext implements Closeable {

    public static final String CLASS_NAME = ServiceContext.class.getName();
    private static final SpiLogger LOGGER = LogUtils.getLogger();

    private static final ConcurrentHashMap<String, ServiceContext> SERVICE_CONTEXT_CACHE = new ConcurrentHashMap<>();

    private final Map<String, Map<Class<?>, Boolean>> INTERFACE_CACHE = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>>> SINGLE_SERVICE_LOADERS = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CloseableConcurrentHashMap<Class<?>, MultipleServiceLoader<?>>> MULTIPLE_SERVICE_LOADERS = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Object> PRELOAD_FLAGS = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> PRELOAD_CHECK_SUMS = new ConcurrentHashMap<>();
    private final Object PRELOAD_DONE = new Object();

    /**
     * Get ServiceContext for specified classloader.
     * For advanced usage, {@link GlacimonSpi} is recommended for general occasions.
     */
    public static ServiceContext getInstance(ClassLoader classLoader) {
        String classLoaderId = ClassUtils.getClassLoaderId(classLoader);
        ServiceContext serviceContext = SERVICE_CONTEXT_CACHE.get(classLoaderId);
        if (serviceContext == null) {
            serviceContext = new ServiceContext();
            ServiceContext previous = SERVICE_CONTEXT_CACHE.putIfAbsent(classLoaderId, serviceContext);
            if (previous != null) {
                serviceContext = previous;
            }
        }
        return serviceContext;
    }

    private ServiceContext() {
    }

    /**
     * Load single service by DEFAULT classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @return SingleServiceLoader (Cached)
     */
    public <T> SingleServiceLoader<T> loadSingleService(Class<T> interfaceClass){
        return loadSingleService(interfaceClass, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Load single service by custom classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     * @return SingleServiceLoader (Cached)
     */
    public <T> SingleServiceLoader<T> loadSingleService(Class<T> interfaceClass, ClassLoader classLoader){
        //preload
        if (FLAG_PRELOAD_AUTO) {
            preload(classLoader);
        }
        //create loader
        return createSingleServiceLoader(interfaceClass, classLoader);
    }

    /**
     * Create loader (without preload)
     */
    @SuppressWarnings("unchecked")
    private <T> SingleServiceLoader<T> createSingleServiceLoader(Class<T> interfaceClass, ClassLoader classLoader) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("? | interfaceClass is null");
        }
        //get loaders from cache
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>> loaders = SINGLE_SERVICE_LOADERS.get(classloaderId);
        if (loaders == null) {
            loaders = new CloseableConcurrentHashMap<>(32);
            CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>> previous = SINGLE_SERVICE_LOADERS.putIfAbsent(classloaderId, loaders);
            if (previous != null) {
                loaders = previous;
            }
        }
        //get loader from cache
        SingleServiceLoader<T> loader = (SingleServiceLoader<T>) loaders.get(interfaceClass);
        if (loader == null) {
            Map<Class<?>, Boolean> interfaces = loadInterfaces(classLoader);
            //check if the interface has registered
            if (!interfaces.containsKey(interfaceClass)) {
                LOGGER.error("? | Interface " + interfaceClass.getName() +
                        " must be defined in " + PATH_INTERFACES + " file, See doc:" + LOG_HOME_PAGE, null);
                throw new IllegalDefinitionException("? | Interface " + interfaceClass.getName() +
                        " must be defined in " + PATH_INTERFACES + " file, See doc:" + LOG_HOME_PAGE);
            }
            //create loader
            loader = new SingleServiceLoader<>(interfaceClass, classLoader);
            SingleServiceLoader<T> previous = (SingleServiceLoader<T>) loaders.putIfAbsent(interfaceClass, loader);
            if (previous != null) {
                loader = previous;
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loader.getLoaderId() + " | Single-service Loader get from cache! " +
                    interfaceClass.getName() + ", classloader:" + classloaderId, null);
        }
        return loader;
    }

    /**
     * Load multiple services by DEFAULT classloader.
     * multiple-service mode is used to load multiple services (has name and ordered).
     * @param interfaceClass Interface type to load
     * @return MultipleServiceLoader (Cached)
     */
    public <T> MultipleServiceLoader<T> loadMultipleService(Class<T> interfaceClass){
        return loadMultipleService(interfaceClass, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Load multiple services by custom classloader.
     * multiple-service mode is used to load multiple services (has name and ordered).
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     * @return MultipleServiceLoader (Cached)
     */
    public <T> MultipleServiceLoader<T> loadMultipleService(Class<T> interfaceClass, ClassLoader classLoader){
        //preload
        if (Constants.FLAG_PRELOAD_AUTO) {
            preload(classLoader);
        }
        //create loader
        return createMultipleServiceLoader(interfaceClass, classLoader);
    }

    /**
     * Create loader (without preload)
     */
    @SuppressWarnings("unchecked")
    private <T> MultipleServiceLoader<T> createMultipleServiceLoader(Class<T> interfaceClass, ClassLoader classLoader) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("? | interfaceClass is null");
        }
        //get loaders from cache
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        CloseableConcurrentHashMap<Class<?>, MultipleServiceLoader<?>> loaders = MULTIPLE_SERVICE_LOADERS.get(classloaderId);
        if (loaders == null) {
            loaders = new CloseableConcurrentHashMap<>(32);
            CloseableConcurrentHashMap<Class<?>, MultipleServiceLoader<?>> previous = MULTIPLE_SERVICE_LOADERS.putIfAbsent(classloaderId, loaders);
            if (previous != null) {
                loaders = previous;
            }
        }
        //get loader from cache
        MultipleServiceLoader<T> loader = (MultipleServiceLoader<T>) loaders.get(interfaceClass);
        if (loader == null) {
            Map<Class<?>, Boolean> interfaces = loadInterfaces(classLoader);
            //check if the interface has registered
            if (!interfaces.containsKey(interfaceClass)) {
                LOGGER.error("? | Interface " + interfaceClass.getName() +
                        " must be defined in " + Constants.PATH_INTERFACES + " file, See doc:" + Constants.LOG_HOME_PAGE, null);
                throw new IllegalDefinitionException("? | Interface " + interfaceClass.getName() +
                        " must be defined in " + Constants.PATH_INTERFACES + " file, See doc:" + Constants.LOG_HOME_PAGE);
            }
            //create loader
            loader = new MultipleServiceLoader<>(interfaceClass, classLoader);
            MultipleServiceLoader<T> previous = (MultipleServiceLoader<T>) loaders.putIfAbsent(interfaceClass, loader);
            if (previous != null) {
                loader = previous;
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loader.getLoaderId() + " | Multiple-service Loader get from cache! " +
                    interfaceClass.getName() + ", classloader:" + classloaderId, null);
        }
        return loader;
    }

    /**
     * Loading interfaces from classloader (with cache)
     */
    private Map<Class<?>, Boolean> loadInterfaces(ClassLoader classLoader){
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        Map<Class<?>, Boolean> interfaces = INTERFACE_CACHE.get(classloaderId);
        if (interfaces == null) {
            interfaces = InterfaceLoader.load(classLoader, "?");
            INTERFACE_CACHE.put(classloaderId, interfaces);
        }
        return interfaces;
    }

    /**
     * Preload all services from DEFAULT classloader.
     * For server applications, used to discover definition errors in advance.
     * NOTICE: Preloading automatically in the Spring environment or set -Dglacimonspi.conf.preload.auto=true.
     */
    public void preload(){
        preload(ClassUtils.getDefaultClassLoader());
    }

    /**
     * Preload all services from specified classloader.
     * For server applications, used to discover definition errors in advance.
     * NOTICE: Preloading automatically in the Spring environment or set -Dglacimonspi.conf.preload.auto=true.
     *
     * @param classLoader classloader
     */
    public void preload(ClassLoader classLoader){
        //get flag
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        Object flag = PRELOAD_FLAGS.get(classloaderId);
        //return if done
        if (flag == PRELOAD_DONE) {
            return;
        }
        //init lock
        if (flag == null) {
            flag = new Object();
            Object previous = PRELOAD_FLAGS.putIfAbsent(classloaderId, flag);
            if (previous != null) {
                flag = previous;
            }
        }
        //return if done
        if (flag == PRELOAD_DONE) {
            return;
        }
        synchronized (flag) {
            if (PRELOAD_FLAGS.get(classloaderId) != PRELOAD_DONE) {
                try {
                    preload0(classLoader, classloaderId);
                } finally {
                    PRELOAD_FLAGS.put(classloaderId, PRELOAD_DONE);
                }
            }
        }
    }

    private void preload0(ClassLoader classLoader, String classloaderId){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("? | Preload | Preloading Start! classloader:" + classloaderId, null);
        }
        Map<Class<?>, Boolean> interfaces = loadInterfaces(classLoader);
        if (interfaces.size() <= 0 && LOGGER.isInfoEnabled()) {
            LOGGER.info("? | Preload | No definition found in " + Constants.PATH_INTERFACES + " files", null);
        }
        List<SingleServiceLoader<?>> singleServiceLoaders = new LinkedList<>();
        List<MultipleServiceLoader<?>> multipleServiceLoaders = new LinkedList<>();
        for (Map.Entry<Class<?>, Boolean> entry : interfaces.entrySet()) {
            Class<?> clazz = entry.getKey();
            if (clazz.isAnnotationPresent(SingleServiceInterface.class)) {
                singleServiceLoaders.add(createSingleServiceLoader(clazz, classLoader));
            }
            if (clazz.isAnnotationPresent(MultipleServiceInterface.class)) {
                multipleServiceLoaders.add(createMultipleServiceLoader(clazz, classLoader));
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("? | Preload | Preloading Complete! classloader:" + classloaderId, null);
        }
        if (LOGGER.isInfoEnabled() || Constants.FLAG_PRELOAD_CHECKSUM) {
            StringBuilder checkSumBuilder = new StringBuilder();
            for (SingleServiceLoader<?> loader : singleServiceLoaders) {
                String report = String.valueOf(loader);
                if (Constants.FLAG_PRELOAD_CHECKSUM) {
                    checkSumBuilder.append(report);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("? | Preload | Loaded " + report, null);
                }
            }
            for (MultipleServiceLoader<?> loader: multipleServiceLoaders) {
                String report = String.valueOf(loader);
                if (Constants.FLAG_PRELOAD_CHECKSUM) {
                    checkSumBuilder.append(report);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("? | Preload | Loaded " + report, null);
                }
            }
            if (Constants.FLAG_PRELOAD_CHECKSUM) {
                int checkSum = checkSumBuilder.toString().hashCode();
                PRELOAD_CHECK_SUMS.put(classloaderId, checkSum);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("? | Preload | CheckSum " + checkSum + ", classloader:" + classloaderId, null);
                }
            }
        }
    }

    /**
     * Remove all loaders of specified classloader from cache
     * @param classLoader classloader
     */
    public void uninstall(ClassLoader classLoader) {
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>> singleServiceLoaders = SINGLE_SERVICE_LOADERS.remove(classloaderId);
        CloseableConcurrentHashMap<Class<?>, MultipleServiceLoader<?>> multipleServiceLoaders = MULTIPLE_SERVICE_LOADERS.remove(classloaderId);
        INTERFACE_CACHE.remove(classloaderId);
        CommonUtils.closeQuietly(singleServiceLoaders);
        CommonUtils.closeQuietly(multipleServiceLoaders);
    }

    /**
     * Remove all loaders of DEFAULT classloader from cache
     */
    public void uninstallDefaultClassloader() {
        uninstall(ClassUtils.getDefaultClassLoader());
    }

    /**
     * Remove all loaders of all classloaders from cache
     */
    public void uninstallAllClassloader() {
        for (Map.Entry<String, CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>>> entry : SINGLE_SERVICE_LOADERS.entrySet()) {
            CommonUtils.closeQuietly(SINGLE_SERVICE_LOADERS.remove(entry.getKey()));
        }
        for (Map.Entry<String, CloseableConcurrentHashMap<Class<?>, MultipleServiceLoader<?>>> entry : MULTIPLE_SERVICE_LOADERS.entrySet()) {
            CommonUtils.closeQuietly(MULTIPLE_SERVICE_LOADERS.remove(entry.getKey()));
        }
        INTERFACE_CACHE.clear();
    }

    /**
     * Get all preload checksums.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public Map<String, Integer> getPreloadCheckSums() {
        Map<String, Integer> result = new HashMap<>(PRELOAD_CHECK_SUMS.size());
        result.putAll(PRELOAD_CHECK_SUMS);
        return result;
    }

    /**
     * Get preload checksum of specified classloader.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public Integer getPreloadCheckSum(ClassLoader classLoader) {
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        return PRELOAD_CHECK_SUMS.get(classloaderId);
    }

    /**
     * Get preload checksum of DEFAULT classloader.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public Integer getPreloadCheckSum() {
        return getPreloadCheckSum(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public void close() throws IOException {
        uninstallAllClassloader();
    }

}
