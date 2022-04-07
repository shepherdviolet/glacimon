/*
 * Copyright (C) 2015-2020 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.reflect;

import sviolet.thistle.compat.reflect.CompatGenericArrayType;
import sviolet.thistle.compat.reflect.CompatParameterizedType;

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
 * Bean info utils
 *
 * @author S.Violet
 */
public class BeanInfoUtils {

    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>(128);

    /**
     * Get property info of Java Bean, With cache
     *
     * @param beanClass Bean class
     * @throws IntrospectionException Inspect error
     */
    public static Map<String, PropertyInfo> getPropertyInfos(Class<?> beanClass) throws IntrospectionException {
        return getPropertyInfos(beanClass, true);
    }

    /**
     * Get property info of Java Bean
     *
     * @param beanClass Bean class
     * @param cacheEnabled Is cache enabled
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
            // Setter first
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
                componentType = CompatParameterizedType.make((Class<?>) (
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
            componentType = CompatGenericArrayType.make(componentType);
        }

        return componentType;
    }

    public static class PropertyInfo {

        private String propertyName;
        private Class<?> propertyClass;
        private Type propertyType;
        private Method readMethod;
        private Method writeMethod;

        private PropertyInfo(String propertyName, Class<?> propertyClass, Type propertyType, Method readMethod, Method writeMethod) {
            this.propertyName = propertyName;
            this.propertyClass = propertyClass;
            this.propertyType = propertyType;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
        }

        /**
         * Bean property name, Not null
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * Class of property, Not null
         */
        public Class<?> getPropertyClass() {
            return propertyClass;
        }

        /**
         * Generic type of property, Not null, instance of Class / ParameterizedType / GenericArrayType
         */
        public Type getPropertyType() {
            return propertyType;
        }

        /**
         * Read method, Nullable, But one of the two methods must not be null
         */
        public Method getReadMethod() {
            return readMethod;
        }

        /**
         * Write method, Nullable, But one of the two methods must not be null
         */
        public Method getWriteMethod() {
            return writeMethod;
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
