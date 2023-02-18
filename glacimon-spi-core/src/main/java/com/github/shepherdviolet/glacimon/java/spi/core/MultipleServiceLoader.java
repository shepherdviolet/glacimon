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

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.ImplementationName;
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.ImplementationPriority;
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.MultipleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalImplementationException;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.CloseableImplementation;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.InitializableImplementation;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.ServiceProxy;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * <p>GlacimonSpi: An implementation of Java Service Provider Interface</p>
 *
 * <p>The loader for multiple-service mode.
 * multiple-service mode is used to load multiple services (has name and ordered).</p>
 *
 * @param <T> Interface of service
 * @author shepherdviolet
 */
public class MultipleServiceLoader<T> implements Uninstallable {

    public static final String CLASS_NAME = MultipleServiceLoader.class.getName();
    private static final SpiLogger LOGGER = LogUtils.getLogger();

    private final String loaderId;
    private final Class<T> interfaceClass;
    private final ClassLoader classLoader;

    private List<InstanceBuilder<T>> instanceBuilders;
    private final List<T> instanceList = new ArrayList<>(16);
    private final Map<String, T> instanceMap = new HashMap<>(16);

    private volatile boolean initialized = false;
    private volatile boolean cached = false;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Load multiple services by custom classloader.
     * multiple-service mode is used to load multiple services (has name and ordered).
     * @param interfaceClass Interface type to load
     * @param classLoader Custom classloader
     * @param serviceContextId Service context id
     */
    MultipleServiceLoader(Class<T> interfaceClass, ClassLoader classLoader, String serviceContextId) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("interfaceClass is null");
        }
        this.interfaceClass = interfaceClass;
        this.classLoader = classLoader;
        this.loaderId = serviceContextId + "-" + CommonUtils.generateLoaderId();
        load();
    }

    /**
     * <p>Get the service instance with the specified name.</p>
     *
     * WARNING: If the implementation does not exist, each get will call the fallback method to recreate an instance,
     * and it's lifecycle method will not be called, properties will not be inject.
     *
     * @param name The name of service implementation (Specified by annotation 'ImplementationName')
     * @param fallback Supply instance if no implementation definition found.
     * @return Service instance (Cached), not null
     */
    public T get(String name, Function<String, T> fallback) {
        T service = get(name);
        if (service == null) {
            return fallback.apply(name);
        }
        return service;
    }

    /**
     * <p>Get the service instance with the specified name.</p>
     *
     * WARNING: If the implementation does not exist, return null.
     *
     * @param name The name of service implementation (Specified by annotation 'ImplementationName')
     * @return Service instance (Cached), Nullable
     */
    public T get(String name) {
        if (!cached) {
            if (!initialized) {
                throw new IllegalStateException(loaderId + "|Multi--Service| The loader has not been initialized yet");
            }
            synchronized (this) {
                if (!cached) {
                    instantiate();
                    cached = true;
                }
            }
        }
        return instanceMap.get(name);
    }

    /**
     * Get all service instances, sorted by priority (Specified by annotation 'ImplementationPriority')
     * @return Service instances (ArrayList, Cached), Not null
     */
    public List<T> getAll(){
        if (!cached) {
            if (!initialized) {
                throw new IllegalStateException(loaderId + "|Multi--Service| The loader has not been initialized yet");
            }
            synchronized (this) {
                if (!cached) {
                    instantiate();
                    cached = true;
                }
            }
        }
        return new ArrayList<>(instanceList);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[multiple-service]  ")
                .append(interfaceClass.getName())
                .append("  (classloader:").append(ClassUtils.getClassLoaderName(classLoader)).append(")");
        if (instanceBuilders != null) {
            for (InstanceBuilder<?> instanceBuilder : instanceBuilders) {
                stringBuilder.append("\n>  ").append(instanceBuilder.implementationClass.getName());
                if (instanceBuilder.propertiesInjector != null) {
                    stringBuilder.append("  ").append(instanceBuilder.propertiesInjector);
                }
                if (instanceBuilder.isNameValid) {
                    stringBuilder.append("  \"").append(instanceBuilder.name).append("\"");
                }
            }
        } else {
            stringBuilder.append("\n>  No implementation");
        }
        return stringBuilder.toString();
    }

    /**
     * Set the flag of the implementation class that implements the 'CloseableImplementation' interface to true
     */
    @Override
    public void close() throws IOException {
        closed.set(true);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + "|Multi--Service| Loader closed!", null);
        }
    }

    /**
     * instantiate instances
     */
    private void instantiate() {
        //no definition
        if (instanceBuilders == null) {
            return;
        }

        int index = 0;
        List<T> instanceList = new ArrayList<>(16);
        Map<String, T> instanceMap = new HashMap<>(16);

        for (InstanceBuilder<T> instanceBuilder : instanceBuilders) {
            //create instance
            T instance;
            try {
                instance = ClassUtils.newInstance(instanceBuilder.implementationClass);
            } catch (Exception e) {
                LOGGER.error(loaderId + "|Multi--Service| Instance Create Failed! Error while instantiating class " +
                        instanceBuilder.implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
                throw new IllegalImplementationException(loaderId + "|Multi--Service| Instance Create Failed! Error while instantiating class " +
                        instanceBuilder.implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
            }
            //inject properties
            if (instanceBuilder.propertiesInjector != null) {
                try {
                    instanceBuilder.propertiesInjector.inject(instance, loaderId);
                } catch (Exception e) {
                    LOGGER.error(loaderId + "|Multi--Service| Instance Create Failed! Error while injecting properties to service " +
                            instanceBuilder.implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
                    throw new IllegalImplementationException(loaderId + "|Multi--Service| Instance Create Failed! Error while injecting properties to service " +
                            instanceBuilder.implementationClass.getName() + " (" + interfaceClass.getName() + ")", e);
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
                    LOGGER.error(loaderId + "|Multi--Service| Instance Create Failed! Error while initializing " +
                            "(invoke onServiceCreated) " + instanceBuilder.implementationClass.getName() +
                            " (" + interfaceClass.getName() + ")", t);
                    throw new IllegalImplementationException(loaderId + "|Multi--Service| Instance Create Failed! Error while initializing " +
                            "(invoke onServiceCreated) " + instanceBuilder.implementationClass.getName() +
                            " (" + interfaceClass.getName() + ")", t);
                }
            }
            //add to list
            instanceList.add(finalInstance);
            //put to map
            if (instanceBuilder.isNameValid) {
                instanceMap.put(instanceBuilder.name, finalInstance);
            }
            //log
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + "|Multi--Service| New instance: " + index++ + " " +
                        instanceBuilder.implementationClass.getName() + (finalInstance instanceof ServiceProxy ? "<CompatByProxy>" : "") +
                        (instanceBuilder.isNameValid ? ", bind name:" + instanceBuilder.name : "") +
                        (instanceBuilder.propertiesInjector != null ? ", prop:" + instanceBuilder.propertiesInjector : ""), null);
            }
        }

        this.instanceList.addAll(instanceList);
        this.instanceMap.putAll(instanceMap);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + "|Multi--Service| Instances Created! " + interfaceClass.getName() +
                    ", " + instanceBuilders.size() + " instances, caller:" + CommonUtils.getCaller(), null);
        }

    }

    /**
     * loading process
     */
    private void load(){
        //log
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(loaderId + "|Multi--Service| Loading Start: " + interfaceClass.getName() +
                    ", classloader:" + ClassUtils.getClassLoaderId(classLoader), null);
        }
        //check is interface
        if (!interfaceClass.isInterface()) {
            LOGGER.error(loaderId + "|Multi--Service| Loading Failed! Interface '" + interfaceClass.getName() +
                    "' is not an interface, classloader:" + ClassUtils.getClassLoaderId(classLoader), null);
            throw new IllegalArgumentException(loaderId + "|Multi--Service| Loading Failed! Interface '" + interfaceClass.getName() +
                    "' is not an interface, classloader:" + ClassUtils.getClassLoaderId(classLoader));
        }
        //check annotation
        MultipleServiceInterface annotation = interfaceClass.getAnnotation(MultipleServiceInterface.class);
        if (annotation == null) {
            LOGGER.error(loaderId + "|Multi--Service| Loading Failed! Missing annotation '@MultipleServiceInterface' on Interface '" +
                    interfaceClass.getName() + "', classloader:" + ClassUtils.getClassLoaderId(classLoader), null);
            throw new IllegalArgumentException(loaderId + "|Multi--Service| Loading Failed! Missing annotation '@MultipleServiceInterface' on Interface '" +
                    interfaceClass.getName() + "', classloader:" + ClassUtils.getClassLoaderId(classLoader), null);
        }
        //load definitions
        List<MultipleDefinition> definitions = DefinitionLoader.loadMultipleDefinitions(interfaceClass.getName(), classLoader, loaderId);
        //no definition
        if (definitions.size() <= 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(loaderId + "|Multi--Service| Loading Canceled! No implementation definition found in classpath for " +
                        "interface '" + interfaceClass.getName() + "', classloader:" + ClassUtils.getClassLoaderId(classLoader), null);
            }
            initialized = true;
            return;
        }
        //sort
        definitions.sort(new Comparator<MultipleDefinition>() {
            @Override
            public int compare(MultipleDefinition o1, MultipleDefinition o2) {
                int hashCompare = o1.calculateImplementationHash() - o2.calculateImplementationHash();
                if (hashCompare != 0) {
                    return hashCompare;
                }
                int rankCompare = o2.getRank() - o1.getRank();
                if (rankCompare != 0) {
                    return rankCompare;
                }
                return (o2.isDisable() ? 1 : 0) - (o1.isDisable() ? 1 : 0);
            }
        });
        //check vm option
        String removedImplClassNames = System.getProperty(Constants.VMOPT_REMOVE + interfaceClass.getName(), null);
        Set<String> removedSet = new HashSet<>();
        if (removedImplClassNames != null) {
            String[] removes = removedImplClassNames.split(",");
            for (String remove : removes) {
                removedSet.add(remove);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(loaderId + "|Multi--Service| VM Option " + Constants.VMOPT_REMOVE + " > " + remove, null);
                }
            }
        }
        //check if enabled
        List<MultipleDefinition> enabledDefinitions = new LinkedList<>();
        List<MultipleDefinition> disabledDefinitions = new LinkedList<>();
        List<MultipleDefinition> removedDefinitions = new LinkedList<>();
        String previousImpl = "";
        for (MultipleDefinition definition : definitions) {
            boolean first = false;
            //the first one after classname changed will be adopted
            if (!previousImpl.equals(definition.getImplementationType())) {
                //record previous classname
                previousImpl = definition.getImplementationType();
                //check
                if (removedSet.contains(definition.getImplementationType())) {
                    //removed
                    removedDefinitions.add(definition);
                } else if (definition.isDisable()) {
                    //disabled
                    disabledDefinitions.add(definition);
                } else {
                    //enabled
                    enabledDefinitions.add(definition);
                }
                first = true;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + "|Multi--Service| Candidate: " + (first ? "* " : "  ") +
                        (definition.isDisable() ? "-" : "+") + definition.getRank() + " " +
                        definition.getImplementationType() + ", url:" + definition.getUrl(), null);
            }
        }
        //print removed
        for (MultipleDefinition definition : removedDefinitions) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(loaderId + "|Multi--Service| Remove: " + definition.getImplementationType() +
                        ", removed by -D" + Constants.VMOPT_REMOVE + interfaceClass.getName(), null);
            }
        }
        //print disabled
        for (MultipleDefinition definition : disabledDefinitions) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + "|Multi--Service| Disable: " + definition.getImplementationType() +
                        ", disabled by rank -" + definition.getRank(), null);
            }
        }
        //no definition
        if (enabledDefinitions.size() <= 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(loaderId + "|Multi--Service| Loading Failed! No valid implementation definition " +
                        "found in classpath for interface '" + interfaceClass.getName() +
                        "', classloader:" + ClassUtils.getClassLoaderId(classLoader), null);
            }
            initialized = true;
            return;
        }
        //instance builders
        instanceBuilders = new ArrayList<>(enabledDefinitions.size());
        //create builders
        for (MultipleDefinition definition : enabledDefinitions) {
            String selectReason = "rank +" + definition.getRank() + ", url: " + definition.getUrl();
            InstanceBuilder<T> instanceBuilder = loadImplementation(definition.getImplementationType(), definition.getUrl(), selectReason);
            instanceBuilder.propertiesInjector = PropertiesLoader.load(instanceBuilder.implementationClass, classLoader, loaderId);
            instanceBuilders.add(instanceBuilder);
        }
        //sort
        instanceBuilders.sort(new Comparator<InstanceBuilder<T>>() {
            @Override
            public int compare(InstanceBuilder<T> o1, InstanceBuilder<T> o2) {
                int priorityCompare = o2.priority - o1.priority;
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return o1.calculateImplementationHash() - o2.calculateImplementationHash();
            }
        });
        //check duplicate names
        Map<String, InstanceBuilder<T>> nameMap = new HashMap<>(instanceBuilders.size());
        int index = 0;
        for (InstanceBuilder<T> instanceBuilder : instanceBuilders) {
            //check duplicate name
            InstanceBuilder<T> previousOne;
            boolean duplicateFlag = false;
            if (instanceBuilder.name != null && (previousOne = nameMap.get(instanceBuilder.name)) != null) {
                duplicateFlag = true;
                LOGGER.warn(loaderId + "|Multi--Service| WARNING!!! Duplicate @ImplementationName(\"" + instanceBuilder.name +
                        "\") of two implementation '" + previousOne.implementationClass.getName() + "' '" +
                        instanceBuilder.implementationClass.getName() + "', The first one can be get by 'get(String name)'" +
                        ", the second one can only get by 'getAll()', url1: " + previousOne.url + ", url2:" + instanceBuilder.url, null);
            }
            //log
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(loaderId + "|Multi--Service| Adopted > " + index++ + " " + instanceBuilder.implementationClass.getName() +
                        (instanceBuilder.name != null ? ", name:" + instanceBuilder.name : "") +
                        (duplicateFlag ? "(duplicated, can't get by name)" : "") +
                        ", prop:" + instanceBuilder.propertiesInjector +
                        ", priority:" + instanceBuilder.priority +
                        (LOGGER.isDebugEnabled() ? ", impl enabled by " + instanceBuilder.enabledReason : "") +
                        (LOGGER.isDebugEnabled() && instanceBuilder.propertiesInjector != null ? (", prop selected by " +
                        instanceBuilder.propertiesInjector.getSelectReason()) : ""), null);
            }
            //record name
            if (!duplicateFlag && instanceBuilder.name != null) {
                //mark as valid name
                instanceBuilder.isNameValid = true;
                nameMap.put(instanceBuilder.name, instanceBuilder);
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(loaderId + "|Multi--Service| Loading Completed! " + interfaceClass.getName() +
                    ", adopt " + instanceBuilders.size() + " implementations, classloader:" + ClassUtils.getClassLoaderId(classLoader) +
                    ", caller:" + CommonUtils.getCaller(), null);
        }
        initialized = true;
    }

    /**
     * load implementation class
     */
    @SuppressWarnings("unchecked")
    private InstanceBuilder<T> loadImplementation(String implementationClassName, String url, String enabledReason) {
        try {
            //load class
            Class<?> implClass = ClassUtils.loadClass(implementationClassName, classLoader);
            if (!interfaceClass.isAssignableFrom(implClass)) {
                LOGGER.error(loaderId + "|Multi--Service| The implementation class " + implementationClassName +
                        " is not an instance of " + interfaceClass.getName() + ", which is enabled by " + enabledReason, null);
                throw new IllegalImplementationException(loaderId + "|Multi--Service| The implementation class " + implementationClassName +
                        " is not an instance of " + interfaceClass.getName() + ", which is enabled by " + enabledReason, null);
            }
            //priority
            int priority = 0;
            ImplementationPriority implementationPriority = implClass.getAnnotation(ImplementationPriority.class);
            if (implementationPriority != null) {
                priority = implementationPriority.value();
            }
            //name
            String name = null;
            ImplementationName implementationName = implClass.getAnnotation(ImplementationName.class);
            if (implementationName != null) {
                name = implementationName.value();
            }
            return new InstanceBuilder<>((Class<T>)implClass, priority, name, url, enabledReason);
        } catch (ClassNotFoundException e) {
            LOGGER.error(loaderId + "|Multi--Service| Implementation class " + implementationClassName +
                    " not found, which is enabled by " + enabledReason, e);
            throw new IllegalDefinitionException(loaderId + "|Multi--Service| Implementation class " + implementationClassName +
                    " not found, which is enabled by " + enabledReason, e);
        }
    }

    /**
     * Information of instance
     */
    private static class InstanceBuilder<T> {

        private final Class<T> implementationClass;
        private PropertiesInjector propertiesInjector;
        private final int priority;
        private final String name;

        private final String url;
        private final String enabledReason;
        private Integer implementationHash;

        private boolean isNameValid = false;

        private InstanceBuilder(Class<T> implementationClass, int priority, String name, String url, String enabledReason) {
            this.implementationClass = implementationClass;
            this.priority = priority;
            this.name = name;
            this.url = url;
            this.enabledReason = enabledReason;
        }

        /**
         * WARNING calculate once only
         */
        private int calculateImplementationHash(){
            if (implementationClass == null) {
                return 0;
            }
            if (implementationHash == null) {
                implementationHash = implementationClass.getName().hashCode();
            }
            return implementationHash;
        }
    }

    String getLoaderId() {
        return loaderId;
    }
}
