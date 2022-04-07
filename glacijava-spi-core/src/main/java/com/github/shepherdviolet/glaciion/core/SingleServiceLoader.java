/*
 * Copyright (C) 2019-2019 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glaciion
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glaciion.core;

import com.github.shepherdviolet.glaciion.api.annotation.SingleServiceInterface;
import com.github.shepherdviolet.glaciion.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glaciion.api.exceptions.IllegalImplementationException;
import com.github.shepherdviolet.glaciion.api.interfaces.CloseableImplementation;
import com.github.shepherdviolet.glaciion.api.interfaces.InitializableImplementation;
import com.github.shepherdviolet.glaciion.api.interfaces.ServiceProxy;
import com.github.shepherdviolet.glaciion.api.interfaces.SpiLogger;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.shepherdviolet.glaciion.core.ClassUtils.getClassLoaderId;
import static com.github.shepherdviolet.glaciion.core.Constants.*;

/**
 * <p>Glaciion: An implementation of Java Service Provider Interface</p>
 *
 * <p>The loader for single-service mode.
 * single-service mode is used when only one service implementation is required.</p>
 *
 * @param <T> Interface of service
 * @author S.Violet
 */
public class SingleServiceLoader<T> implements Closeable {

    public static final String CLASS_NAME = SingleServiceLoader.class.getName();
    private static final SpiLogger LOGGER = LogUtils.getLogger();

    private static final ConcurrentHashMap<String, CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>>> LOADER_CACHE = new ConcurrentHashMap<>();

    /**
     * Load single service by DEFAULT classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @return SingleServiceLoader (Cached)
     */
    public static <T> SingleServiceLoader<T> load(Class<T> interfaceClass){
        return load(interfaceClass, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Load single service by custom classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     * @return SingleServiceLoader (Cached)
     */
    public static <T> SingleServiceLoader<T> load(Class<T> interfaceClass, ClassLoader classLoader){
        //preload
        if (FLAG_PRELOAD_AUTO) {
            PreLoader.preload(classLoader);
        }
        //create loader
        return createLoader(interfaceClass, classLoader);
    }

    /**
     * Create loader (without preload)
     */
    static <T> SingleServiceLoader<T> createLoader(Class<T> interfaceClass, ClassLoader classLoader) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("? | interfaceClass is null");
        }
        //get loaders from cache
        String classloaderId = getClassLoaderId(classLoader);
        CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>> loaders = LOADER_CACHE.get(classloaderId);
        if (loaders == null) {
            loaders = new CloseableConcurrentHashMap<>(32);
            CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>> previous = LOADER_CACHE.putIfAbsent(classloaderId, loaders);
            if (previous != null) {
                loaders = previous;
            }
        }
        //get loader from cache
        SingleServiceLoader<T> loader = (SingleServiceLoader<T>) loaders.get(interfaceClass);
        if (loader == null) {
            Map<Class<?>, Boolean> interfaces = InterfaceLoader.get(classLoader);
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
            LOGGER.debug(loader.loaderId + " | Single-service Loader get from cache! " +
                    interfaceClass.getName() + ", classloader:" + classloaderId, null);
        }
        return loader;
    }

    /**
     * Remove all loaders of specified classloader from cache
     * @param classLoader classloader
     */
    public static Map<Class<?>, SingleServiceLoader<?>> uninstall(ClassLoader classLoader) {
        String classloaderId = getClassLoaderId(classLoader);
        CloseableConcurrentHashMap<Class<?>, SingleServiceLoader<?>> loaders = LOADER_CACHE.remove(classloaderId);
        //close
        if (loaders != null) {
            try {
                loaders.close();
            } catch (IOException ignore) {
            }
        }
        return loaders;
    }

    /**
     * Remove all loaders of DEFAULT classloader from cache
     */
    public static Map<Class<?>, SingleServiceLoader<?>> uninstallDefaultClassloader() {
        return uninstall(ClassUtils.getDefaultClassLoader());
    }

    private final String loaderId = CommonUtils.generateLoaderId();
    private final Class<T> interfaceClass;
    private final ClassLoader classLoader;

    private Class<T> implementationClass;
    private PropertiesInjector propertiesInjector;
    private volatile T instance;

    private volatile boolean initialized = false;
    private volatile boolean cached = false;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Load single service by custom classloader.
     * single-service mode is used when only one service implementation is required.
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     */
    private SingleServiceLoader(Class<T> interfaceClass, ClassLoader classLoader) {
        this.interfaceClass = interfaceClass;
        this.classLoader = classLoader;
        load();
    }

    /**
     * Get the service instance
     * @return Service instance (Cached), Nullable
     */
    public T get(){
        if (!cached) {
            if (!initialized) {
                throw new IllegalStateException(loaderId + " | The loader has not been initialized yet");
            }
            synchronized (this) {
                if (!cached) {
                    instantiate();
                    cached = true;
                }
            }
        }
        return instance;
    }

    @Override
    public String toString() {
        return "[single-service] " + interfaceClass.getName() + " :\n> " +
                (implementationClass != null ? implementationClass.getName() : "No implementation") +
                (propertiesInjector != null ? " " + propertiesInjector : "");
    }

    /**
     * Set the flag of the implementation class that implements the 'CloseableImplementation' interface to true
     */
    @Override
    public void close() throws IOException {
        closed.set(true);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | Single-service Loader Closed!", null);
        }
    }

    /**
     * instantiate instance
     */
    private void instantiate() {
        //no definition
        if (implementationClass == null) {
            return;
        }
        //create instance
        T instance;
        try {
            instance = InstantiationUtils.newInstance(implementationClass);
        } catch (Exception e) {
            LOGGER.error(loaderId + " | Single-service Instance Create Failed! Error while instantiating class " +
                    implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
            throw new IllegalImplementationException(loaderId + " | Single-service Instance Create Failed! Error while instantiating class " +
                    implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
        }
        //inject properties
        if (propertiesInjector != null) {
            try {
                propertiesInjector.inject(instance, loaderId);
            } catch (Exception e) {
                LOGGER.error(loaderId + " | Single-service Instance Create Failed! Error while injecting properties to service " +
                        implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
                throw new IllegalImplementationException(loaderId + " | Single-service Instance Create Failed! Error while injecting properties to service " +
                        implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
            }
        }
        //set close flag
        if (instance instanceof CloseableImplementation) {
            ((CloseableImplementation) instance).setCloseFlag(closed);
        }
        //build proxy if needed
        T finalInstance = ProxyUtils.buildProxyIfNeeded(classLoader, interfaceClass, instance, loaderId);
        //creating completed
        if (instance instanceof InitializableImplementation) {
            try {
                ((InitializableImplementation) instance).onServiceCreated();
            } catch (Throwable t) {
                LOGGER.error(loaderId + " | Single-service Instance Create Failed! Error while initializing (invoke onServiceCreated) " +
                        implementationClass.getName() + " (" + interfaceClass.getName() + ")", t);
                throw new IllegalImplementationException(loaderId + " | Single-service Instance Create Failed! Error while initializing (invoke onServiceCreated) " +
                        implementationClass.getName() + " (" + interfaceClass.getName() + ")", t);
            }
        }
        //set instance
        this.instance = finalInstance;
        //log
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(loaderId + " | Single-service Instance Created! " +
                    interfaceClass.getName() + ", impl:" + implementationClass.getName() +
                    (finalInstance instanceof ServiceProxy ? "<CompatByProxy>" : "") +
                    (propertiesInjector != null ? ", prop:" + propertiesInjector : "") +
                    ", caller:" + CommonUtils.getCaller(), null);
        }
    }

    /**
     * loading process
     */
    private void load(){
        String selectReason;
        //log
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | Single-service Loading Start: " + interfaceClass.getName() +
                    ", classloader:" + getClassLoaderId(classLoader), null);
        }
        //check is interface
        if (!interfaceClass.isInterface()) {
            LOGGER.error(loaderId + " | " + interfaceClass.getName() +
                    " must be an interface", null);
            throw new IllegalArgumentException(loaderId + " | " + interfaceClass.getName() +
                    " must be an interface");
        }
        //check annotation
        SingleServiceInterface annotation = interfaceClass.getAnnotation(SingleServiceInterface.class);
        if (annotation == null) {
            LOGGER.error(loaderId + " | " + interfaceClass.getName() +
                    " must have an annotation '@SingleServiceInterface'", null);
            throw new IllegalArgumentException(loaderId + " | " + interfaceClass.getName() +
                    " must have an annotation '@SingleServiceInterface'");
        }
        //check vm option: -Dglaciion.select.<interface-class>
        String selectedImplClassName = System.getProperty(VMOPT_SELECT + interfaceClass.getName(), null);
        if (selectedImplClassName != null) {
            //load selected
            selectReason = "-D" + VMOPT_SELECT + interfaceClass.getName() + "=" + selectedImplClassName;
            loadImplementation(selectedImplClassName,selectReason);
        } else {
            //load from definitions
            selectReason = loadDefinitions();
        }
        //no properties
        if (implementationClass == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(loaderId + " | Single-service Loading Failed! " + interfaceClass.getName() +
                        ", implementation not found, classloader:" + getClassLoaderId(classLoader), null);
            }
            initialized = true;
            return;
        }
        //load properties
        propertiesInjector = PropertiesLoader.load(implementationClass, classLoader, loaderId);
        //log
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(loaderId + " | Single-service Loading Completed! " + interfaceClass.getName() +
                    ", impl:" + implementationClass.getName() +
                    (propertiesInjector != null ? ", prop:" + propertiesInjector : "") +
                    ", classloader:" + getClassLoaderId(classLoader) +
                    (LOGGER.isDebugEnabled() ? ", impl selected by " + selectReason : "") +
                    (LOGGER.isDebugEnabled() && propertiesInjector != null ? ", prop selected by " + propertiesInjector.getSelectReason() : ""), null);
        }
        initialized = true;
    }

    /**
     * load definition files
     */
    private String loadDefinitions() {
        //load definitions
        List<SingleDefinition> definitions = DefinitionLoader.loadSingleDefinitions(interfaceClass.getName(), classLoader, loaderId);
        //no definition
        if (definitions.size() <= 0) {
            LOGGER.warn(loaderId + " | No definitions found in classpath, no single-service will be loaded from this loader", null);
            return null;
        }
        //only one
        if (definitions.size() == 1) {
            SingleDefinition definition = definitions.get(0);
            String selectReason = "priority " + definition.getPriority() + ", url: " + definition.getUrl();
            loadImplementation(definition.getImplementationType(), selectReason);
            return selectReason;
        }
        //more than one
        //sort, larger first
        Collections.sort(definitions, new Comparator<SingleDefinition>() {
            @Override
            public int compare(SingleDefinition o1, SingleDefinition o2) {
                int priorityCompare = o2.getPriority() - o1.getPriority();
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return o1.calculateImplementationHash() - o2.calculateImplementationHash();
            }
        });
        //select first (highest priority)
        SingleDefinition selectedDefinition = definitions.get(0);
        int selectedPriority = selectedDefinition.getPriority();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | Candidate: priority:" + selectedDefinition.getPriority() +
                    " impl:" + selectedDefinition.getImplementationType() + " url:" + selectedDefinition.getUrl(), null);
        }
        //check the others
        for (int i = 1; i < definitions.size(); i++) {
            SingleDefinition definition = definitions.get(i);
            if (definition.getPriority() >= selectedPriority){
                //duplicate priority
                if (!definition.getImplementationType().equals(selectedDefinition.getImplementationType())) {
                    //duplicate priority with different implementation
                    LOGGER.warn(loaderId + " | WARNING!!! Duplicate priority '" + selectedPriority + "' of two implementation '" +
                            selectedDefinition.getImplementationType() + "' '" + definition.getImplementationType() +
                            "', The first one will be adopted, the second one will be abandoned, url1: " +
                            selectedDefinition.getUrl() + ", url2:" + definition.getUrl(), null);
                }
            } else if (!LOGGER.isDebugEnabled()) {
                //lower priority (if log level is below debug, break)
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | Candidate: priority:" + definition.getPriority() +
                        " impl:" + definition.getImplementationType() + " url:" + definition.getUrl(), null);
            }
        }
        //load selected
        String selectReason = "priority " + selectedDefinition.getPriority() + ", url: " + selectedDefinition.getUrl();
        loadImplementation(selectedDefinition.getImplementationType(), selectReason);
        return selectReason;
    }

    /**
     * load implementation class
     */
    private void loadImplementation(String implementationClassName, String selectReason) {
        try {
            //load class
            Class<?> implClass = ClassUtils.loadClass(implementationClassName, classLoader);
            if (!interfaceClass.isAssignableFrom(implClass)) {
                LOGGER.error(loaderId + " | The implementation class " + implementationClassName +
                        " is not an instance of " + interfaceClass.getName() + ", which is selected by " + selectReason, null);
                throw new IllegalImplementationException(loaderId + " | The implementation class " + implementationClassName +
                        " is not an instance of " + interfaceClass.getName() + ", which is selected by " + selectReason, null);
            }
            //ok
            this.implementationClass = (Class<T>) implClass;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | Selected: " + implementationClassName +
                        ", selected by " + selectReason, null);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(loaderId + " | Implementation class " + implementationClassName +
                    " not found, which is selected by " + selectReason, e);
            throw new IllegalDefinitionException(loaderId + " | Implementation class " + implementationClassName +
                    " not found, which is selected by " + selectReason, e);
        }
    }

}
