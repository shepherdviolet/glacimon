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

import com.github.shepherdviolet.glaciion.api.annotation.PropertyInject;
import com.github.shepherdviolet.glaciion.api.exceptions.IllegalImplementationException;
import com.github.shepherdviolet.glaciion.api.interfaces.SpiLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.github.shepherdviolet.glaciion.core.Constants.*;

/**
 * loading PropertiesInjector from classpath
 *
 * @author S.Violet
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
        //sort, larger first
        Collections.sort(definitions, new Comparator<PropertiesDefinition>() {
            @Override
            public int compare(PropertiesDefinition o1, PropertiesDefinition o2) {
                int priorityCompare = o2.getPriority() - o1.getPriority();
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return o1.calculatePropertiesHash() - o2.calculatePropertiesHash();
            }
        });
        //select first (highest priority)
        PropertiesDefinition selectedDefinition = definitions.get(0);
        int selectedPriority = selectedDefinition.getPriority();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + " | PropInject | Candidate: priority:" + selectedDefinition.getPriority() +
                    " properties:" + selectedDefinition.getProperties() + " url:" + selectedDefinition.getUrl(), null);
        }
        //check the others
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
        //build injector
        return buildInjectors(implementationClass, selectedDefinition, loaderId,
                PROPERTY_PRIORITY + " " + selectedPriority + ", url: " + selectedDefinition.getUrl());
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
        StringBuilder reasonPlus = new StringBuilder();
        //record finished field
        Set<String> finishedSet = new HashSet<>();
        //all methods
        Method[] methods = implementationClass.getDeclaredMethods();
        for (Method method : methods) {
            handlePropertyInjectOnMethod(implementationClass, definition, loaderId, propertiesInjector, reasonPlus, finishedSet, method);
        }
        //all fields
        Field[] fields = implementationClass.getDeclaredFields();
        for (Field field : fields) {
            handlePropertyInjectOnField(implementationClass, definition, loaderId, propertiesInjector, reasonPlus, finishedSet, field);
        }
        //set reason
        propertiesInjector.setSelectReason(selectReason + (reasonPlus.length() > 0 ? " overwritten by" + reasonPlus.toString() : ""));
        //sort
        Collections.sort(propertiesInjector.getInjectors(), new Comparator<PropertiesInjector.Injector>() {
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
        String fieldName = BeanMethodNameUtils.methodToField(method.getName());
        if (fieldName == null) {
            LOGGER.error(loaderId + " | PropInject | Illegal setter method '" +
                    method.getName() + "' in " + implementationClass.getName() +
                    ", it is not a standard setter method, correct format `setLogLevel(String s)`", null);
            throw new IllegalImplementationException(loaderId + " | PropInject | Illegal setter method '" +
                    method.getName() + "' in " + implementationClass.getName() +
                    ", it is not a standard setter method, correct format `setLogLevel(String s)`");
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(loaderId + " | PropInject | Injection point:  method '" +
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
            LOGGER.trace(loaderId + " | PropInject | Injection point:  field '" +
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
        }
    }

    private static PropertiesInjector.Injector createMethodInjector(String fieldName, Method method, String stringValue, Class<?> implementationClass, String loaderId, String propertySource){
        Object value;
        Class<?> fieldClass = method.getParameterTypes()[0];
        //parse value
        try {
            if (fieldClass.equals(String.class)) {
                value = stringValue;
            } else if (fieldClass.equals(boolean.class)) {
                value = Boolean.parseBoolean(stringValue);
            } else if (fieldClass.equals(Boolean.class)) {
                value = Boolean.valueOf(stringValue);
            } else if (fieldClass.equals(int.class)) {
                value = Integer.parseInt(stringValue);
            } else if (fieldClass.equals(Integer.class)) {
                value = Integer.valueOf(stringValue);
            } else if (fieldClass.equals(long.class)) {
                value = Long.parseLong(stringValue);
            } else if (fieldClass.equals(Long.class)) {
                value = Long.valueOf(stringValue);
            } else if (fieldClass.equals(float.class)) {
                value = Float.parseFloat(stringValue);
            } else if (fieldClass.equals(Float.class)) {
                value = Float.valueOf(stringValue);
            } else if (fieldClass.equals(double.class)) {
                value = Double.parseDouble(stringValue);
            } else if (fieldClass.equals(Double.class)) {
                value = Double.valueOf(stringValue);
            } else {
                LOGGER.error(loaderId + " | PropInject | Illegal setter method '" +
                        method.getName() + "' in " + implementationClass.getName() +
                        " ('@PropertyInject' marked), string value can not parse to " + fieldClass.getName() +
                        ", it only supports String/boolean/int/long/float/double", null);
                throw new IllegalImplementationException(loaderId + " | PropInject | Illegal setter method '" +
                        method.getName() + "' in " + implementationClass.getName() +
                        " ('@PropertyInject' marked), string value can not parse to " + fieldClass.getName() +
                        ", it only supports String/boolean/int/long/float/double", null);
            }
        } catch (IllegalImplementationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(loaderId + " | PropInject | Error while parsing string value '" +
                    stringValue + "' to " + fieldClass + ", So we can not inject property to method " +
                    implementationClass.getName() + "#" + method.getName() + ", The property comes from " +
                    propertySource, e);
            throw new IllegalArgumentException(loaderId + " | PropInject | Error while parsing string value '" +
                    stringValue + "' to " + fieldClass + ", So we can not inject property to method " +
                    implementationClass.getName() + "#" + method.getName() + ", The property comes from " +
                    propertySource, e);
        }
        //create injector
        method.setAccessible(true);
        return new PropertiesInjector.MethodInjector(fieldName, method, value);
    }

    private static PropertiesInjector.Injector createFieldInjector(String fieldName, Field field, String stringValue, Class<?> implementationClass, String loaderId, String propertySource){
        Object value;
        Class<?> fieldClass = field.getType();
        //parse value
        try {
            if (fieldClass.equals(String.class)) {
                value = stringValue;
            } else if (fieldClass.equals(boolean.class)) {
                value = Boolean.parseBoolean(stringValue);
            } else if (fieldClass.equals(Boolean.class)) {
                value = Boolean.valueOf(stringValue);
            } else if (fieldClass.equals(int.class)) {
                value = Integer.parseInt(stringValue);
            } else if (fieldClass.equals(Integer.class)) {
                value = Integer.valueOf(stringValue);
            } else if (fieldClass.equals(long.class)) {
                value = Long.parseLong(stringValue);
            } else if (fieldClass.equals(Long.class)) {
                value = Long.valueOf(stringValue);
            } else if (fieldClass.equals(float.class)) {
                value = Float.parseFloat(stringValue);
            } else if (fieldClass.equals(Float.class)) {
                value = Float.valueOf(stringValue);
            } else if (fieldClass.equals(double.class)) {
                value = Double.parseDouble(stringValue);
            } else if (fieldClass.equals(Double.class)) {
                value = Double.valueOf(stringValue);
            } else {
                LOGGER.error(loaderId + " | PropInject | Illegal field '" + fieldName + "' in " +
                        implementationClass.getName() + " ('@PropertyInject' marked), string value can not parse to " +
                        fieldClass.getName() + ", it only supports String/boolean/int/long/float/double", null);
                throw new IllegalImplementationException(loaderId + " | PropInject | Illegal field '" + fieldName + "' in " +
                        implementationClass.getName() + " ('@PropertyInject' marked), string value can not parse to " +
                        fieldClass.getName() + ", it only supports String/boolean/int/long/float/double", null);
            }
        } catch (IllegalImplementationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(loaderId + " | PropInject | Error while parsing string value '" +
                    stringValue + "' to " + fieldClass + ", So we can not inject property to field '" + fieldName + "' of " +
                    implementationClass.getName() + ", The property comes from " + propertySource, e);
            throw new IllegalArgumentException(loaderId + " | PropInject | Error while parsing string value '" +
                    stringValue + "' to " + fieldClass + ", So we can not inject property to field '" + fieldName + "' of " +
                    implementationClass.getName() + ", The property comes from " + propertySource, e);
        }
        //create injector
        field.setAccessible(true);
        return new PropertiesInjector.FieldInjector(fieldName, field, value);
    }

    private static String getVmOption(String key, String def) {
        if (key == null || "".equals(key)) {
            return def;
        }
        return System.getProperty(key, def);
    }

}
