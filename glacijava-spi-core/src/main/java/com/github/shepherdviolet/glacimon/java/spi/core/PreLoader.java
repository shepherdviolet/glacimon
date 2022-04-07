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
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Preload all services from specified classloader
 *
 * @author S.Violet
 */
public class PreLoader {

    public static final String CLASS_NAME = PreLoader.class.getName();
    private static final SpiLogger LOGGER = LogUtils.getLogger();

    private static final ConcurrentHashMap<String, Object> PRELOAD_FLAGS = new ConcurrentHashMap<>();
    private static final Object DONE = new Object();

    private static final ConcurrentHashMap<String, Integer> CHECK_SUMS = new ConcurrentHashMap<>();

    /**
     * Preload all services from DEFAULT classloader.
     * For server applications, used to discover definition errors in advance.
     * NOTICE: Preloading automatically in the Spring environment or set -Dglacimonspi.conf.preload.auto=true.
     */
    public static void preload(){
        preload(ClassUtils.getDefaultClassLoader());
    }

    /**
     * Preload all services from specified classloader.
     * For server applications, used to discover definition errors in advance.
     * NOTICE: Preloading automatically in the Spring environment or set -Dglacimonspi.conf.preload.auto=true.
     *
     * @param classLoader classloader
     */
    public static void preload(ClassLoader classLoader){
        //get flag
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        Object flag = PRELOAD_FLAGS.get(classloaderId);
        //return if done
        if (flag == DONE) {
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
        if (flag == DONE) {
            return;
        }
        synchronized (flag) {
            if (PRELOAD_FLAGS.get(classloaderId) != DONE) {
                try {
                    preload0(classLoader, classloaderId);
                } finally {
                    PRELOAD_FLAGS.put(classloaderId, DONE);
                }
            }
        }
    }

    /**
     * Get all preload checksums.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public static Map<String, Integer> getCheckSums() {
        Map<String, Integer> result = new HashMap<>(CHECK_SUMS.size());
        result.putAll(CHECK_SUMS);
        return result;
    }

    /**
     * Get preload checksum of specified classloader.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public static Integer getCheckSum(ClassLoader classLoader) {
        String classloaderId = ClassUtils.getClassLoaderId(classLoader);
        return CHECK_SUMS.get(classloaderId);
    }

    /**
     * Get preload checksum of DEFAULT classloader.
     * Used to determine if the definition has been changed (Someone added or deleted the service without knowing it.)
     */
    public static Integer getCheckSum() {
        return getCheckSum(ClassUtils.getDefaultClassLoader());
    }

    private static void preload0(ClassLoader classLoader, String classloaderId){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("? | Preload | Preloading Start! classloader:" + classloaderId, null);
        }
        Map<Class<?>, Boolean> interfaces = InterfaceLoader.get(classLoader);
        if (interfaces.size() <= 0 && LOGGER.isInfoEnabled()) {
            LOGGER.info("? | Preload | No definition found in " + Constants.PATH_INTERFACES + " files", null);
        }
        List<SingleServiceLoader<?>> singleServiceLoaders = new LinkedList<>();
        List<MultipleServiceLoader<?>> multipleServiceLoaders = new LinkedList<>();
        for (Map.Entry<Class<?>, Boolean> entry : interfaces.entrySet()) {
            Class<?> clazz = entry.getKey();
            if (clazz.isAnnotationPresent(SingleServiceInterface.class)) {
                singleServiceLoaders.add(SingleServiceLoader.createLoader(clazz, classLoader));
            }
            if (clazz.isAnnotationPresent(MultipleServiceInterface.class)) {
                multipleServiceLoaders.add(MultipleServiceLoader.createLoader(clazz, classLoader));
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
                CHECK_SUMS.put(classloaderId, checkSum);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("? | Preload | CheckSum " + checkSum + ", classloader:" + classloaderId, null);
                }
            }
        }
    }

    private PreLoader(){
    }

}
