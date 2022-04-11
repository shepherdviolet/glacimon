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

import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

class DefinitionLoader {

    private static final SpiLogger LOGGER = LogUtils.getLogger();

    private static final Set<String> FILE_EXCLUSIONS = new HashSet<>();

    static {
        //parse exclusions
        String exclusions = System.getProperty(Constants.VMOPT_EXCLUDE_FILE, null);
        if (exclusions != null) {
            //split by ,
            String[] exclusionArray = exclusions.split(",");
            for (String exclusion : exclusionArray) {
                if (!CommonUtils.isEmptyOrBlank(exclusion)) {
                    FILE_EXCLUSIONS.add(exclusion.trim());
                }
            }
        }
    }

    /**
     * Load interface definitions from classpath
     * @param classLoader classpath
     * @param loaderId id of service loader (for log)
     */
    static List<InterfaceDefinition> loadInterfacesDefinitions(ClassLoader classLoader, final String loaderId) {
        final Set<InterfaceDefinition> result = new HashSet<>();
        loadFiles(Constants.PATH_INTERFACES, classLoader, loaderId, new DefinitionVisitor() {
            @Override
            public void visitFileStart(String url) {
            }
            @Override
            public void visitDefinition(String name, String value, String url) {
                result.add(new InterfaceDefinition(name, url));
            }
            @Override
            public void visitFileEnd() {
            }
        });
        List<InterfaceDefinition> sorted = new ArrayList<>(result);
        Collections.sort(sorted, new Comparator<InterfaceDefinition>() {
            @Override
            public int compare(InterfaceDefinition o1, InterfaceDefinition o2) {
                return o1.calculateInterfaceHash() - o2.calculateInterfaceHash();
            }
        });
        return sorted;
    }

    /**
     * Load single-service definitions from classpath
     * @param interfaceType interface type
     * @param classLoader classloader
     * @param loaderId id of service loader (for log)
     */
    static List<SingleDefinition> loadSingleDefinitions(final String interfaceType, ClassLoader classLoader, final String loaderId){
        final List<SingleDefinition> result = new LinkedList<>();
        loadFiles(Constants.PATH_SINGLE_SERVICE + interfaceType, classLoader, loaderId, new DefinitionVisitor() {
            @Override
            public void visitFileStart(String url) {
            }
            @Override
            public void visitDefinition(String name, String value, String url) {
                //check name
                if (CommonUtils.isEmpty(name)) {
                    throw new IllegalDefinitionException(loaderId + " | Illegal single-service definition, find a property with empty key, correct format is 'sample.SampleServiceImpl -1', url:" + url + ", see:" + Constants.LOG_HOME_PAGE, null);
                }
                //parse priority
                int priority = 0;
                if (!CommonUtils.isEmpty(value)) {
                    try {
                        priority = Integer.parseInt(value);
                    } catch (Exception e) {
                        throw new IllegalDefinitionException(loaderId + " | Error while parsing priority of single-service, at " + name + " " + value + ", url:" + url + ", see:" + Constants.LOG_HOME_PAGE, e);
                    }
                }
                //add
                result.add(new SingleDefinition(interfaceType, name, priority, url));
            }
            @Override
            public void visitFileEnd() {
            }
        });
        return result;
    }

    /**
     * Load multiple-service definitions from classpath
     * @param interfaceType interface type
     * @param classLoader classloader
     * @param loaderId id of service loader (for log)
     */
    static List<MultipleDefinition> loadMultipleDefinitions(final String interfaceType, ClassLoader classLoader, final String loaderId){
        final List<MultipleDefinition> result = new LinkedList<>();
        loadFiles(Constants.PATH_MULTIPLE_SERVICE + interfaceType, classLoader, loaderId, new DefinitionVisitor() {
            @Override
            public void visitFileStart(String url) {
            }
            @Override
            public void visitDefinition(String name, String value, String url) {
                //check name
                if (CommonUtils.isEmpty(name)) {
                    throw new IllegalDefinitionException(loaderId + " | Illegal multiple-service definition, find a property with empty key, correct format is '+sample.SampleServiceImpl' or '-sample.SampleServiceImpl' (+/- can be multiple), url:" + url + ", see:" + Constants.LOG_HOME_PAGE, null);
                }
                //parse rank and isDisable
                boolean isRemove;
                int rank = 1;
                char c = name.charAt(0);
                if (c == '+') {
                    //starts with +
                    isRemove = false;
                    for (int i = 1 ; i < name.length() ; i++) {
                        c = name.charAt(i);
                        if (c == '+') {
                            rank++;
                            continue;
                        } else if (c == '-') {
                            throw new IllegalDefinitionException(loaderId + " | Illegal multiple-service definition, when the first char is +, it must be followed by +, correct format is '++sample.SampleServiceImpl' (+ can be multiple), url:" + url + ", see:" + Constants.LOG_HOME_PAGE, null);
                        } else {
                            break;
                        }
                    }
                } else if (c == '-') {
                    //starts with -
                    isRemove = true;
                    for (int i = 1 ; i < name.length() ; i++) {
                        c = name.charAt(i);
                        if (c == '-') {
                            rank++;
                            continue;
                        } else if (c == '+') {
                            throw new IllegalDefinitionException(loaderId + " | Illegal multiple-service definition, when the first char is -, it must be followed by -, correct format is '--sample.SampleServiceImpl' (- can be multiple), url:" + url + ", see:" + Constants.LOG_HOME_PAGE, null);
                        } else {
                            break;
                        }
                    }
                } else {
                    //starts with other char
                    throw new IllegalDefinitionException(loaderId + " | Illegal multiple-service definition, a property is not starts with +/-, correct format is '+sample.SampleServiceImpl' or '-sample.SampleServiceImpl' (+/- can be multiple), url:" + url + ", see:" + Constants.LOG_HOME_PAGE, null);
                }
                if (rank > name.length() - 1) {
                    throw new IllegalDefinitionException(loaderId + " | Illegal multiple-service definition, property key cannot only have +/- signs, correct format is '+sample.SampleServiceImpl' or '-sample.SampleServiceImpl' (+/- can be multiple), url:" + url + ", see:" + Constants.LOG_HOME_PAGE, null);
                }
                //add
                result.add(new MultipleDefinition(isRemove, interfaceType, name.substring(rank), rank, url));
            }
            @Override
            public void visitFileEnd() {
            }
        });
        return result;
    }

    /**
     * Load properties definitions from classpath
     * @param bindType implementation type
     * @param classLoader classloader
     * @param loaderId id of service loader (for log)
     */
    static List<PropertiesDefinition> loadPropertiesDefinitions(final String bindType, ClassLoader classLoader, final String loaderId){
        final List<PropertiesDefinition> result = new LinkedList<>();
        loadFiles(Constants.PATH_PROPERTIES + bindType, classLoader, loaderId, new DefinitionVisitor() {
            PropertiesDefinition definition;
            @Override
            public void visitFileStart(String url) {
                //new definition
                definition = new PropertiesDefinition(bindType, url);
            }
            @Override
            public void visitDefinition(String name, String value, String url) {
                //add property
                definition.getProperties().put(name, value);
            }
            @Override
            public void visitFileEnd() {
                //parse priority
                String priorityStr = definition.getProperties().remove(Constants.PROPERTY_PRIORITY);
                if (priorityStr != null) {
                    try {
                        definition.setPriority(Integer.parseInt(priorityStr));
                    } catch (Exception e) {
                        throw new IllegalDefinitionException(loaderId + " | Error while parsing " + Constants.PROPERTY_PRIORITY + " of properties file, url:" + definition.getUrl() + ", see:" + Constants.LOG_HOME_PAGE, e);
                    }
                }
                //add definition
                result.add(definition);
            }
        });
        return result;
    }

    /**
     * Load all files from classpath
     * @param resourcePath classpath of files
     * @param classLoader classloader
     * @param loaderId id of service loader (for log)
     * @param visitor data visitor
     */
    private static void loadFiles(String resourcePath, ClassLoader classLoader, String loaderId, DefinitionVisitor visitor){
        //get resource urls
        Enumeration<URL> urls;
        try {
            urls = ClassUtils.loadResources(resourcePath, classLoader);
        } catch (Exception e) {
            LOGGER.error(loaderId + " | Error while loading files from classpath " + resourcePath, e);
            throw new RuntimeException(loaderId + " | Error while loading files from classpath " + resourcePath, e);
        }
        if (urls == null || !urls.hasMoreElements()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(loaderId + " | No file in classpath " + resourcePath, null);
            }
            return;
        }
        //reading files
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                //print hash
                String hash = null;
                if (LOGGER.isTraceEnabled()) {
                    hash = CommonUtils.digest(url.openStream(), "MD5");
                    LOGGER.trace(loaderId + " | Loading file " + url + ", md5:" + hash, null);
                }
                //exclude file by vm option
                if (FILE_EXCLUSIONS.size() > 0) {
                    if (hash == null) {
                        hash = CommonUtils.digest(url.openStream(), "MD5");
                    }
                    if (FILE_EXCLUSIONS.contains(hash)) {
                        LOGGER.warn(loaderId + " | Exclude file " + url + " by -D" + Constants.VMOPT_EXCLUDE_FILE, null);
                        continue;
                    }
                }
                //parse file
                Properties properties = new Properties();
                InputStream inputStream = null;
                try {
                    properties.load(inputStream = url.openStream());
                } finally {
                    CommonUtils.closeQuietly(inputStream);
                }
                Enumeration<?> names = properties.propertyNames();
                if (!names.hasMoreElements()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(loaderId + " | No property in file " + url, null);
                    }
                    continue;
                }
                //to raw definition info
                String urlString = String.valueOf(url);
                visitor.visitFileStart(urlString);
                while (names.hasMoreElements()) {
                    String name = String.valueOf(names.nextElement());
                    visitor.visitDefinition(name, properties.getProperty(name, ""), urlString);
                }
                visitor.visitFileEnd();
            } catch (IllegalDefinitionException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.error(loaderId + " | Error while loading file " + url, e);
                throw new RuntimeException(loaderId + " | Error while loading file " + url, e);
            }
        }
    }

    private interface DefinitionVisitor {

        /**
         * visit file start
         * @param url url
         */
        void visitFileStart(String url);

        /**
         * visit definition
         * @param name name
         * @param value value
         * @param url url
         */
        void visitDefinition(String name, String value, String url);

        /**
         * visit file end
         */
        void visitFileEnd();

    }

}
