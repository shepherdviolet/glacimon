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

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.PropertyInject;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalDefinitionException;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalImplementationException;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import static com.github.shepherdviolet.glacimon.java.spi.core.Constants.*;

/**
 * loading PropertiesInjector from classpath
 *
 * @author shepherdviolet
 */
class PropertiesLoader {

    private static final SpiLogger LOGGER = LogUtils.getLogger();

    /**
     * load properties for implementation class
     */
    static PropertiesInjector load(Class<?> implementationClass, ClassLoader classLoader, String loaderId){
        //load definitions
        List<PropertiesDefinition> definitions = DefinitionLoader.loadPropertiesDefinitions(implementationClass.getName(), classLoader, loaderId);
        //no definition
        if (definitions.size() == 0) {
            checkAnnotations(implementationClass, loaderId);
            return null;
        }
        //log
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | PropInject | Loading properties for " + implementationClass.getName(), null);
        }
        //only one
        if (definitions.size() == 1) {
            PropertiesDefinition definition = definitions.get(0);
            return buildInjectors(implementationClass, definition, loaderId,
                    PROPERTY_PRIORITY + " " + definition.getPriority() + ", url: " + definition.getUrl());
        }
        //more than one
        //sort by priority
        sortDefinitions(definitions);
        //select first (highest priority)
        PropertiesDefinition selectedDefinition = definitions.get(0);
        int selectedPriority = selectedDefinition.getPriority();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | PropInject | Candidate: priority:" + selectedDefinition.getPriority() +
                    " properties:" + selectedDefinition.getProperties() + " url:" + selectedDefinition.getUrl(), null);
        }
        //check the others
        checkUnselectedDefinitions(definitions, selectedDefinition, selectedPriority, loaderId);
        //build injector
        return buildInjectors(implementationClass, selectedDefinition, loaderId,
                PROPERTY_PRIORITY + " " + selectedPriority + ", url: " + selectedDefinition.getUrl());
    }

    /**
     * sort by priority
     */
    private static void sortDefinitions(List<PropertiesDefinition> definitions) {
        definitions.sort(new Comparator<PropertiesDefinition>() {
            @Override
            public int compare(PropertiesDefinition o1, PropertiesDefinition o2) {
                int priorityCompare = o2.getPriority() - o1.getPriority();
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return o1.calculatePropertiesHash() - o2.calculatePropertiesHash();
            }
        });
    }

    /**
     * Check unchecked definitions
     */
    private static void checkUnselectedDefinitions(List<PropertiesDefinition> definitions, PropertiesDefinition selectedDefinition, int selectedPriority, String loaderId) {
        for (int i = 1; i < definitions.size(); i++) {
            PropertiesDefinition definition = definitions.get(i);
            if (definition.getPriority() >= selectedPriority){
                //duplicate priority
                if (!String.valueOf(definition.getProperties()).equals(String.valueOf(selectedDefinition.getProperties()))) {
                    //duplicate priority with different properties
                    LOGGER.warn(loaderId + " | WARNING!!! PropInject | Duplicate " + PROPERTY_PRIORITY +
                            " '" + selectedPriority + "' of two properties '" + selectedDefinition.getProperties() + "' '" +
                            definition.getProperties() + "', The first one will be adopted, the second one will be abandoned, url1: " +
                            selectedDefinition.getUrl() + ", url2:" + definition.getUrl(), null);
                }
            } else if (!LOGGER.isDebugEnabled()) {
                //lower priority (if log level is below debug, break)
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Candidate: priority:" + definition.getPriority() +
                        " properties:" + definition.getProperties() + " url:" + definition.getUrl(), null);
            }
        }
    }

    /**
     * build injectors
     */
    private static void checkAnnotations(Class<?> implementationClass, String loaderId){
        //all methods
        Method[] methods = implementationClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PropertyInject.class)) {
                PropertyInject propertyInject = method.getAnnotation(PropertyInject.class);
                if (propertyInject.required()) {
                    LOGGER.error(loaderId + " | PropInject | Missing required properties definition file for method '" +
                            method.getName() + "' in " + implementationClass.getName() +
                            ", you should add properties definition file in " + Constants.PATH_PROPERTIES + implementationClass.getName(), null);
                    throw new IllegalDefinitionException(loaderId + " | PropInject | Missing required properties definition file for method '" +
                            method.getName() + "' in " + implementationClass.getName() +
                            ", you should add properties definition file in " + Constants.PATH_PROPERTIES + implementationClass.getName());
                }
            }
        }
        //all fields
        Field[] fields = implementationClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(PropertyInject.class)) {
                PropertyInject propertyInject = field.getAnnotation(PropertyInject.class);
                if (propertyInject.required()) {
                    LOGGER.error(loaderId + " | PropInject | Missing required properties definition file for field '" +
                            field.getName() + "' in " + implementationClass.getName() +
                            ", you should add properties definition file in " + Constants.PATH_PROPERTIES + implementationClass.getName(), null);
                    throw new IllegalDefinitionException(loaderId + " | PropInject | Missing required properties definition file for field '" +
                            field.getName() + "' in " + implementationClass.getName() +
                            ", you should add properties definition file in " + Constants.PATH_PROPERTIES + implementationClass.getName());
                }
            }
        }
    }

    /**
     * build injectors
     */
    private static PropertiesInjector buildInjectors(Class<?> implementationClass, PropertiesDefinition definition, String loaderId, String selectReason){
        //log
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | PropInject | Selected: " + definition.getProperties() +
                    ", selected by " + selectReason, null);
        }
        //injector
        PropertiesInjector propertiesInjector = new PropertiesInjector();
        //more reason
        StringBuilder reason = new StringBuilder();
        //record finished field
        Set<String> finishedSet = new HashSet<>();
        //all methods
        Method[] methods = implementationClass.getDeclaredMethods();
        for (Method method : methods) {
            handlePropertyInjectOnMethod(implementationClass, definition, loaderId, propertiesInjector, reason, finishedSet, method);
        }
        //all fields
        Field[] fields = implementationClass.getDeclaredFields();
        for (Field field : fields) {
            handlePropertyInjectOnField(implementationClass, definition, loaderId, propertiesInjector, reason, finishedSet, field);
        }
        //set reason
        propertiesInjector.setSelectReason(selectReason + (reason.length() > 0 ? " overwritten by" + reason : ""));
        //sort
        propertiesInjector.getInjectors().sort(new Comparator<PropertiesInjector.Injector>() {
            @Override
            public int compare(PropertiesInjector.Injector o1, PropertiesInjector.Injector o2) {
                return o1.getFieldName().hashCode() - o2.getFieldName().hashCode();
            }
        });
        return propertiesInjector;
    }

    /**
     * build injectors : handle @PropertyInject on method
     */
    private static void handlePropertyInjectOnMethod(Class<?> implementationClass, PropertiesDefinition definition, String loaderId, PropertiesInjector propertiesInjector, StringBuilder reasonPlus, Set<String> finishedSet, Method method) {
        //check annotation
        PropertyInject propertyInject = method.getAnnotation(PropertyInject.class);
        if (propertyInject == null){
            return;
        }
        //check method name
        if (!method.getName().startsWith("set") ||
                method.getParameterTypes().length != 1) {
            LOGGER.error(loaderId + " | PropInject | Illegal setter method '" + method.getName() +
                    "' in " + implementationClass.getName() + ", it must starts with 'set' and has only one parameter", null);
            throw new IllegalImplementationException(loaderId + " | PropInject | Illegal setter method '" + method.getName() +
                    "' in " + implementationClass.getName() + ", it must starts with 'set' and has only one parameter");
        }
        String fieldName = BeanUtils.methodToField(method.getName());
        //check field name
        if (fieldName == null) {
            LOGGER.error(loaderId + " | PropInject | Illegal setter method '" +
                    method.getName() + "' in " + implementationClass.getName() +
                    ", it is not a standard setter method, correct format `setLogLevel(String s)`", null);
            throw new IllegalImplementationException(loaderId + " | PropInject | Illegal setter method '" +
                    method.getName() + "' in " + implementationClass.getName() +
                    ", it is not a standard setter method, correct format `setLogLevel(String s)`");
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(loaderId + " | PropInject | Injection point: method '" +
                    method.getName() + "'", null);
        }
        //get value from annotation specified vm option
        String valueFromAnnotation = getVmOption(propertyInject.getVmOptionFirst(), null);
        //get value from vm option
        String vmOptionKey = VMOPT_PROPERTY + implementationClass.getName() + "." + fieldName;
        String valueFromVmOption = System.getProperty(vmOptionKey, null);
        //get value from definition
        String valueFromDefinition = definition.getProperties().get(fieldName);
        if (valueFromAnnotation != null) {
            //value from annotation specified vm option
            propertiesInjector.getInjectors().add(createMethodInjector(fieldName, method, valueFromAnnotation,
                    implementationClass, loaderId, "-D" + propertyInject.getVmOptionFirst()));
            finishedSet.add(fieldName);
            reasonPlus.append(" -D").append(propertyInject.getVmOptionFirst());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Effective property: " + fieldName + " = '" +
                        (valueFromDefinition != null ? valueFromDefinition : "") + "' -> '" + valueFromAnnotation +
                        "', Inject by method, Overwritten by -D" + propertyInject.getVmOptionFirst(), null);
            }
        } else if (valueFromVmOption != null) {
            //value from vm option
            propertiesInjector.getInjectors().add(createMethodInjector(fieldName, method, valueFromVmOption,
                    implementationClass, loaderId, "-D" + vmOptionKey));
            finishedSet.add(fieldName);
            reasonPlus.append(" -D").append(vmOptionKey);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Effective property: " + fieldName + " = '" +
                        (valueFromDefinition != null ? valueFromDefinition : "") + "' -> '" + valueFromVmOption +
                        "', Inject by method, Overwritten by -D" + vmOptionKey, null);
            }
        } else if (valueFromDefinition != null) {
            //value from definition
            propertiesInjector.getInjectors().add(createMethodInjector(fieldName, method, valueFromDefinition,
                    implementationClass, loaderId, definition.getUrl()));
            finishedSet.add(fieldName);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Effective property: " + fieldName + " = '" +
                        valueFromDefinition + "', Inject by method", null);
            }
        } else if (propertyInject.required()) {
            LOGGER.error(loaderId + " | PropInject | Missing required property for method '" +
                    method.getName() + "' in " + implementationClass.getName() + ", it can be set by" +
                    (CommonUtils.isEmptyOrBlank(propertyInject.getVmOptionFirst()) ? "" : " -D" + propertyInject.getVmOptionFirst()) +
                    " -D" + vmOptionKey + ", or '" + fieldName + "' in definition " + definition.getUrl(), null);
            throw new IllegalDefinitionException(loaderId + " | PropInject | Missing required property for method '" +
                    method.getName() + "' in " + implementationClass.getName() + ", it can be set by" +
                    (CommonUtils.isEmptyOrBlank(propertyInject.getVmOptionFirst()) ? "" : " -D" + propertyInject.getVmOptionFirst()) +
                    " -D" + vmOptionKey + ", or '" + fieldName + "' in definition " + definition.getUrl());
        }
    }

    /**
     * build injectors : handle @PropertyInject on field
     */
    private static void handlePropertyInjectOnField(Class<?> implementationClass, PropertiesDefinition definition, String loaderId, PropertiesInjector propertiesInjector, StringBuilder reasonPlus, Set<String> finishedSet, Field field) {
        //check annotation
        PropertyInject propertyInject = field.getAnnotation(PropertyInject.class);
        if (propertyInject == null){
            return;
        }
        //check if inject by method
        String fieldName = field.getName();
        if (finishedSet.contains(fieldName)) {
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(loaderId + " | PropInject | Injection point: field '" +
                    fieldName + "'", null);
        }
        //get value from annotation specified vm option
        String valueFromAnnotation = getVmOption(propertyInject.getVmOptionFirst(), null);
        //get value from vm option
        String vmOptionKey = VMOPT_PROPERTY + implementationClass.getName() + "." + fieldName;
        String valueFromVmOption = System.getProperty(vmOptionKey, null);
        //get value from definition
        String valueFromDefinition = definition.getProperties().get(fieldName);
        if (valueFromAnnotation != null) {
            //value from annotation specified vm option
            propertiesInjector.getInjectors().add(createFieldInjector(fieldName, field, valueFromAnnotation,
                    implementationClass, loaderId, "-D" + propertyInject.getVmOptionFirst()));
            finishedSet.add(fieldName);
            reasonPlus.append(" -D").append(propertyInject.getVmOptionFirst());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Effective property: " + fieldName + " = '" +
                        (valueFromDefinition != null ? valueFromDefinition : "") + "' -> '" + valueFromAnnotation +
                        "', Inject by field, Overwritten by -D" + propertyInject.getVmOptionFirst(), null);
            }
        } else if (valueFromVmOption != null) {
            //value from vm option
            propertiesInjector.getInjectors().add(createFieldInjector(fieldName, field, valueFromVmOption,
                    implementationClass, loaderId, "-D" + vmOptionKey));
            finishedSet.add(fieldName);
            reasonPlus.append(" -D").append(vmOptionKey);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Effective property: " + fieldName + " = '" +
                        (valueFromDefinition != null ? valueFromDefinition : "") + "' -> '" + valueFromVmOption +
                        "', Inject by field, Overwritten by -D" + vmOptionKey, null);
            }
        } else if (valueFromDefinition != null) {
            //value from definition
            propertiesInjector.getInjectors().add(createFieldInjector(fieldName, field, valueFromDefinition,
                    implementationClass, loaderId, definition.getUrl()));
            finishedSet.add(fieldName);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(loaderId + " | PropInject | Effective property: " + fieldName + " = '" +
                        valueFromDefinition + "', Inject by field", null);
            }
        } else if (propertyInject.required()) {
            LOGGER.error(loaderId + " | PropInject | Missing required property for field '" +
                    fieldName + "' in " + implementationClass.getName() + ", it can be set by" +
                    (CommonUtils.isEmptyOrBlank(propertyInject.getVmOptionFirst()) ? "" : " -D" + propertyInject.getVmOptionFirst()) +
                    " -D" + vmOptionKey + ", or '" + fieldName + "' in definition " + definition.getUrl(), null);
            throw new IllegalDefinitionException(loaderId + " | PropInject | Missing required property for field '" +
                    fieldName + "' in " + implementationClass.getName() + ", it can be set by" +
                    (CommonUtils.isEmptyOrBlank(propertyInject.getVmOptionFirst()) ? "" : " -D" + propertyInject.getVmOptionFirst()) +
                    " -D" + vmOptionKey + ", or '" + fieldName + "' in definition " + definition.getUrl());
        }
    }

    private static PropertiesInjector.Injector createMethodInjector(String fieldName, Method method, String stringValue, Class<?> implementationClass, String loaderId, String propertySource){
        Class<?> toType = method.getParameterTypes()[0];

        //parse value
        Object value = parseValue(stringValue, toType,
                () -> {
                    LOGGER.error(loaderId + " | PropInject | Illegal setter method '" +
                            method.getName() + "' in " + implementationClass.getName() +
                            " ('@PropertyInject' marked), string value can not parse to " + toType.getName() +
                            ", it only supports String/boolean/int/long/float/double", null);
                    throw new IllegalImplementationException(loaderId + " | PropInject | Illegal setter method '" +
                            method.getName() + "' in " + implementationClass.getName() +
                            " ('@PropertyInject' marked), string value can not parse to " + toType.getName() +
                            ", it only supports String/boolean/int/long/float/double", null);
                },
                throwable -> {
                    LOGGER.error(loaderId + " | PropInject | Error while parsing string value '" +
                            stringValue + "' to " + toType + ", So we can not inject property to method " +
                            implementationClass.getName() + "#" + method.getName() + ", The property comes from " +
                            propertySource, throwable);
                    throw new IllegalArgumentException(loaderId + " | PropInject | Error while parsing string value '" +
                            stringValue + "' to " + toType + ", So we can not inject property to method " +
                            implementationClass.getName() + "#" + method.getName() + ", The property comes from " +
                            propertySource, throwable);
                });

        //create injector
        method.setAccessible(true);
        return new PropertiesInjector.MethodInjector(fieldName, method, value);
    }

    private static PropertiesInjector.Injector createFieldInjector(String fieldName, Field field, String stringValue, Class<?> implementationClass, String loaderId, String propertySource){
        Class<?> toType = field.getType();

        //parse value
        Object value = parseValue(stringValue, toType,
                () -> {
                    LOGGER.error(loaderId + " | PropInject | Illegal field '" + fieldName + "' in " +
                            implementationClass.getName() + " ('@PropertyInject' marked), string value can not parse to " +
                            toType.getName() + ", it only supports String/boolean/int/long/float/double", null);
                    throw new IllegalImplementationException(loaderId + " | PropInject | Illegal field '" + fieldName + "' in " +
                            implementationClass.getName() + " ('@PropertyInject' marked), string value can not parse to " +
                            toType.getName() + ", it only supports String/boolean/int/long/float/double", null);
                },
                throwable -> {
                    LOGGER.error(loaderId + " | PropInject | Error while parsing string value '" +
                            stringValue + "' to " + toType + ", So we can not inject property to field '" + fieldName + "' of " +
                            implementationClass.getName() + ", The property comes from " + propertySource, throwable);
                    throw new IllegalArgumentException(loaderId + " | PropInject | Error while parsing string value '" +
                            stringValue + "' to " + toType + ", So we can not inject property to field '" + fieldName + "' of " +
                            implementationClass.getName() + ", The property comes from " + propertySource, throwable);
                });

        //create injector
        field.setAccessible(true);
        return new PropertiesInjector.FieldInjector(fieldName, field, value);
    }

    private static Object parseValue(String stringValue, Class<?> toType, Runnable onNotSupports, Consumer<Throwable> onParseFailed) {
        Object value;
        try {
            if (toType.equals(String.class)) {
                value = stringValue;
            } else if (toType.equals(boolean.class)) {
                value = Boolean.parseBoolean(stringValue);
            } else if (toType.equals(Boolean.class)) {
                value = Boolean.valueOf(stringValue);
            } else if (toType.equals(int.class)) {
                value = Integer.parseInt(stringValue);
            } else if (toType.equals(Integer.class)) {
                value = Integer.valueOf(stringValue);
            } else if (toType.equals(long.class)) {
                value = Long.parseLong(stringValue);
            } else if (toType.equals(Long.class)) {
                value = Long.valueOf(stringValue);
            } else if (toType.equals(float.class)) {
                value = Float.parseFloat(stringValue);
            } else if (toType.equals(Float.class)) {
                value = Float.valueOf(stringValue);
            } else if (toType.equals(double.class)) {
                value = Double.parseDouble(stringValue);
            } else if (toType.equals(Double.class)) {
                value = Double.valueOf(stringValue);
            } else {
                onNotSupports.run();
                return null;
            }
        } catch (IllegalImplementationException e) {
            throw e;
        } catch (Exception e) {
            onParseFailed.accept(e);
            return null;
        }
        return value;
    }

    private static String getVmOption(String key, String def) {
        if (key == null || "".equals(key)) {
            return def;
        }
        return System.getProperty(key, def);
    }

}
