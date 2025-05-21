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

package com.github.shepherdviolet.glacimon.java.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaBean信息获取工具
 *
 * @author shepherdviolet
 */
public class BeanInfoUtils {

    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>(128);

    /**
     * 获取JavaBean信息, 带缓存
     *
     * @param beanClass JavaBean类
     * @throws IntrospectionException Inspect error
     */
    public static Map<String, PropertyInfo> getPropertyInfos(Class<?> beanClass) throws IntrospectionException {
        return getPropertyInfos(beanClass, true);
    }

    /**
     * 获取JavaBean信息
     *
     * @param beanClass JavaBean类
     * @param cacheEnabled true:带缓存, false:不带缓存
     * @throws IntrospectionException Inspect error
     */
    @SuppressWarnings("unchecked")
    public static Map<String, PropertyInfo> getPropertyInfos(Class<?> beanClass, boolean cacheEnabled) throws IntrospectionException {
        if (beanClass == null) {
            throw new NullPointerException("beanClass is null");
        }

        if (!cacheEnabled) {
            return getPropertyInfos0(beanClass);
        }

        Object propertyInfos = CACHE.get(beanClass);
        if (propertyInfos == null) {
            try {
                propertyInfos = getPropertyInfos0(beanClass);
                propertyInfos = Collections.unmodifiableMap((Map<?, ?>) propertyInfos);
            } catch (IntrospectionException e) {
                propertyInfos = e;
            }
            CACHE.put(beanClass, propertyInfos);
        }

        if (propertyInfos instanceof IntrospectionException) {
            throw (IntrospectionException) propertyInfos;
        }

        return (Map<String, PropertyInfo>) propertyInfos;
    }

    private static Map<String, PropertyInfo> getPropertyInfos0(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass, Object.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Map<String, PropertyInfo> propertyInfos = new HashMap<>(propertyDescriptors.length << 1);
        // Handle properties
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            // Property type
            Class<?> propertyClass = propertyDescriptor.getPropertyType();
            Type propertyType;
            // Setter / Getter
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            // Fallback
            if (propertyClass == null) {
                propertyClass = Object.class;
            }
            // Getter first
            if (readMethod != null) {
                propertyType = readMethod.getGenericReturnType();
            } else {
                // writeMethod must exist if readMethod is null
                Type[] parameterTypes = writeMethod.getGenericParameterTypes();
                // IndexedPropertyDescriptor has 2 parameters, see Introspector
                propertyType = parameterTypes[parameterTypes.length - 1];
            }
            // Get actual type of property
            propertyType = getActualType(propertyType, beanClass);
            if (propertyType == null) {
                propertyType = propertyClass;
            }
            // Name
            String propertyName = propertyDescriptor.getName();
            if (propertyName != null) {
                propertyName = propertyName.intern();
            }
            propertyInfos.put(propertyName, new PropertyInfo(
                    propertyName,
                    propertyClass,
                    propertyType,
                    readMethod,
                    writeMethod
            ));
        }
        return propertyInfos;
    }

    private static Type getActualType(Type propertyType, Class<?> beanClass) {
        if (propertyType == null) {
            return null;
        }

        // If it is a generic array Type (T[]), get the component type (T)
        Type componentType = propertyType;
        int arrayDepth = 0;
        while (componentType instanceof GenericArrayType) {
            componentType = ((GenericArrayType) componentType).getGenericComponentType();
            arrayDepth++;
        }

        // If the component type is generic, get the actual type from declaring class
        if (componentType instanceof TypeVariable && ((TypeVariable<?>) componentType).getGenericDeclaration() instanceof Class) {
            try {
                // try to find generic type in class
                componentType = GenericClassUtils.getActualTypes(beanClass, (Class<?>) ((TypeVariable<?>) componentType).getGenericDeclaration())
                        .get(((TypeVariable<?>) componentType).getName());
            } catch (GenericClassUtils.TargetGenericClassNotFoundException ignore) {
                return null;
            }
        }

        if (componentType instanceof ParameterizedType) {
            // Get actual type if type arguments as generic
            Type[] actualTypeArguments = ((ParameterizedType) componentType).getActualTypeArguments();
            boolean rebuild = false;
            for (int i = 0 ; i < actualTypeArguments.length ; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                if ((actualTypeArgument instanceof TypeVariable && ((TypeVariable<?>) actualTypeArgument).getGenericDeclaration() instanceof Class) ||
                        actualTypeArgument instanceof GenericArrayType) {
                    Type actualType = getActualType(actualTypeArgument, beanClass);
                    if (actualType != null) {
                        rebuild = true;
                        actualTypeArguments[i] = actualType;
                    }
                }
            }
            // Rebuild ParameterizedType
            if (rebuild) {
                componentType = Jdk8ParameterizedType.make((Class<?>) (
                        (ParameterizedType) componentType).getRawType(),
                        actualTypeArguments,
                        ((ParameterizedType) componentType).getOwnerType());
            }
        } else if (!(componentType instanceof Class)) {
            return null;
        }

        // If it is not a generic array Type (T[]), return directly
        if (arrayDepth <= 0) {
            return componentType;
        }

        // If it is a generic array Type (T[]), rebuild GenericArrayType
        for (int i = 0; i < arrayDepth; i++) {
            componentType = Jdk8GenericArrayType.make(componentType);
        }

        return componentType;
    }

    /**
     * JavaBean属性信息
     */
    public static class PropertyInfo {

        private final String propertyName;
        private final Class<?> propertyClass;
        private final Type propertyType;
        private final Method readMethod;
        private final Method writeMethod;

        private PropertyInfo(String propertyName, Class<?> propertyClass, Type propertyType, Method readMethod, Method writeMethod) {
            this.propertyName = propertyName;
            this.propertyClass = propertyClass;
            this.propertyType = propertyType;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
        }

        /**
         * 属性名称, 非空.
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * 属性类型, 非空.
         * 如果只有读方法, 属性类型以读方法返回类型为准.
         * 如果只有读方法, 属性类型以写方法入参类型为准.
         * 如果同时存在读写方法, 属性类型以读方法返回类型为准, 如果此时写方法入参类型不同, 则该写方法无效 (PropertyInfo#getWriteMethod返回null).
         */
        public Class<?> getPropertyClass() {
            return propertyClass;
        }

        /**
         * 属性原始类型(Type), 非空, 可能为: Class (普通类) / ParameterizedType (带泛型参数的类) / GenericArrayType (带泛型数组的类).
         */
        public Type getPropertyType() {
            return propertyType;
        }

        /**
         * 读(get/is)方法, 可能为空, 但读/写方法必有一个非空.
         */
        public Method getReadMethod() {
            return readMethod;
        }

        /**
         * 写(set)方法, 可能为空, 但读/写方法必有一个非空.
         * 如果同时存在读写方法, 属性类型以读方法返回类型为准, 如果此时写方法入参类型不同, 则该写方法无效 (PropertyInfo#getWriteMethod返回null).
         */
        public Method getWriteMethod() {
            return writeMethod;
        }

        /**
         * 获取属性值
         * @param bean Bean对象
         * @param throwOnFailure 属性获取失败时, 是否抛出异常, true:抛出异常, false:返回null
         * @throws RuntimeException 属性获取失败 (throwOnFailure=true时)
         */
        @SuppressWarnings("unchecked")
        public <T> T get(Object bean, boolean throwOnFailure) {
            if (bean == null) {
                if (throwOnFailure) {
                    throw new RuntimeException("Bean is null");
                } else {
                    return null;
                }
            }
            if (readMethod == null) {
                if (throwOnFailure) {
                    throw new RuntimeException("Property '" + propertyName +
                            "' has no read method, bean: " + bean.getClass().getName());
                } else {
                    return null;
                }
            }
            try {
                return (T) readMethod.invoke(bean);
            } catch (Throwable t) {
                if (throwOnFailure) {
                    throw new RuntimeException("Failed to get property '" + propertyName +
                            "' from bean '" + bean.getClass().getName() + "'", t);
                } else {
                    return null;
                }
            }
        }

        public void set(Object bean, Object value, boolean throwOnFailure) {
            if (bean == null) {
                if (throwOnFailure) {
                    throw new RuntimeException("Bean is null");
                } else {
                    return;
                }
            }
            if (value != null && !propertyClass.isAssignableFrom(value.getClass())) {
                if (throwOnFailure) {
                    throw new RuntimeException("Property '" + propertyName + "' in bean '" + bean.getClass().getName() +
                            "' is of type '" + propertyClass.getName() + "', but '" + value.getClass() + "' was provided");
                } else {
                    return;
                }
            }
            if (writeMethod == null) {
                if (throwOnFailure) {
                    throw new RuntimeException("Property '" + propertyName +
                            "' has no write method, bean: " + bean.getClass().getName());
                } else {
                    return;
                }
            }
            try {
                writeMethod.invoke(bean, value);
            } catch (Throwable t) {
                if (throwOnFailure) {
                    throw new RuntimeException("Failed to set property '" + propertyName +
                            "' to bean '" + bean.getClass().getName() + "'", t);
                } else {
                    return;
                }
            }
        }

        @Override
        public String toString() {
            return "PropertyInfo{" +
                    "propertyName='" + propertyName + '\'' +
                    ", propertyClass=" + propertyClass +
                    ", propertyType=" + propertyType +
                    ", readMethod=" + readMethod +
                    ", writeMethod=" + writeMethod +
                    '}';
        }
    }

}
