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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.conversion.mapxbean;

import org.springframework.cglib.beans.BeanMap;
import sviolet.thistle.util.reflect.BeanInfoUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static sviolet.slate.common.x.conversion.mapxbean.MxbConstants.*;

/**
 * [Main processor] Map -> Bean
 *
 * @author S.Violet
 * @see MapXBean
 */
public class MapToBeanConverterImpl implements MapToBeanConverter, ConversionExceptionThrower {

    private final boolean typeMapping;
    private final boolean throwExceptionIfFails;
    private final ConversionExceptionCollector exceptionCollector;
    private boolean inspectBeanStrictly;
    private boolean propertyUpperCamelCase;

    private MapToBeanConverterImpl(boolean typeMapping,
                                   boolean throwExceptionIfFails,
                                   ConversionExceptionCollector exceptionCollector,
                                   boolean inspectBeanStrictly,
                                   boolean propertyUpperCamelCase) {
        this.typeMapping = typeMapping;
        this.throwExceptionIfFails = throwExceptionIfFails;
        this.exceptionCollector = !throwExceptionIfFails ? exceptionCollector : null;
        this.inspectBeanStrictly = inspectBeanStrictly;
        this.propertyUpperCamelCase = propertyUpperCamelCase;
    }

    /**
     * @inheritDoc
     */
    @Override
    public <T> T convert(Map<String, Object> fromMap, Class<T> toType) {
        Class<?> fromType = fromMap != null ? fromMap.getClass() : null;
        try {
            if (exceptionCollector != null) {
                exceptionCollector.onStart(fromMap, toType);
            }
            return convert0(fromMap, toType, new ConversionPath(null, fromType, toType, toType, null));
        } finally {
            if (exceptionCollector != null) {
                exceptionCollector.onFinish(fromMap, toType);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void convert(Map<String, Object> fromMap, Object toBean) {
        Class<?> fromType = fromMap != null ? fromMap.getClass() : null;
        Class<?> toType = toBean != null ? toBean.getClass() : null;
        try {
            if (exceptionCollector != null) {
                exceptionCollector.onStart(fromMap, toType);
            }
            convert0(fromMap, toBean, new ConversionPath(null, fromType, toType, toType, null));
        } finally {
            if (exceptionCollector != null) {
                exceptionCollector.onFinish(fromMap, toType);
            }
        }
    }

    /**
     * Create instance and convert
     */
    private <T> T convert0(Map<String, Object> fromMap, Class<T> toType, ConversionPath conversionPath) {
        if (toType == null) {
            return null;
        }
        //Create instance
        T to;
        try {
            to = OBJECT_INSTANTIATOR.newInstance(toType, false);
        } catch (Throwable e) {
            throwConversionException("MapXBean: Error while mapping Map to " + toType.getName() + ", map data:" + fromMap,
                    e, conversionPath);
            return null;
        }
        if (fromMap == null || fromMap.size() == 0) {
            return to;
        }
        //Convert
        convert0(fromMap, to, conversionPath);
        return to;
    }

    /**
     * Convert
     */
    private void convert0(Map<String, Object> fromMap, Object toBean, ConversionPath conversionPath) {
        if (fromMap == null || fromMap.size() <= 0 || toBean == null) {
            return;
        }
        //Create BeanMap of toBean, and get propertyInfos of toBean
        BeanMap toBeanMap;
        Map<String, BeanInfoUtils.PropertyInfo> toBeanPropertyInfos;
        try {
            toBeanMap = BeanMap.create(toBean);
            toBeanPropertyInfos = BeanInfoUtils.getPropertyInfos(toBean.getClass());
        } catch (Throwable e) {
            throwConversionException("MapXBean: Error while mapping Map to " + toBean.getClass().getName() +
                            ", map data:" + fromMap, e, conversionPath);
            return;
        }
        //Handle all properties in toBean
        for (Object keyObj : toBeanMap.keySet()) {
            //KV
            String key = String.valueOf(keyObj);
            Object value = fromMap.get(convertMapKey(key));
            if (propertyUpperCamelCase) {
                if (value == null) {
                    // Get by lowerCase
                    value = fromMap.get(key);
                    if (value == null) {
                        continue;
                    }
                }
            } else {
                if (value == null) {
                    continue;
                }
            }

            //Property info
            BeanInfoUtils.PropertyInfo toBeanPropertyInfo = toBeanPropertyInfos.get(key);
            //Check write method
            if (inspectBeanStrictly) {
                if (toBeanPropertyInfo.getReadMethod() == null || toBeanPropertyInfo.getWriteMethod() == null) {
                    continue;
                }
            } else {
                if (toBeanPropertyInfo.getWriteMethod() == null) {
                    continue;
                }
            }

            //From this type
            Class<?> valueClass = value.getClass();
            //To this type
            Class<?> expectClass = toBeanMap.getPropertyType(key);
            Type expectType = toBeanPropertyInfo.getPropertyType();

            //Create sub ConversionPath for element
            ConversionPath subConversionPath = new ConversionPath(key, valueClass, expectClass, expectType, conversionPath);

            //Convert property
            Object convertedValue;
            try {
                convertedValue = propertyConverter.convert(value, valueClass, expectClass, expectType, subConversionPath);
            } catch (Throwable e) {
                throwConversionException("MapXBean: Error while mapping Map to " + toBean.getClass().getName() +
                                ", property \"" + key + "\" mapping failed, map data:" + fromMap, e, subConversionPath);
                continue;
            }
            //Skip if null
            if (convertedValue == null) {
                continue;
            }

            //Put into bean
            try {
                toBeanMap.put(keyObj, convertedValue);
            } catch (Throwable e) {
                throwConversionException("MapXBean: Error while mapping Map to " + toBean.getClass().getName() +
                                ", putting \"" + key + "\" failed, map data:" + fromMap, e, subConversionPath);
            }
        }
    }

    private String convertMapKey(String key){
        if (!propertyUpperCamelCase || key == null || key.length() <= 0) {
            return key;
        }
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    /**
     * Convert property of Bean.
     * This method is independent, so that it can be called in the sub processor
     */
    private final PropertyOrElementConverter propertyConverter = new PropertyOrElementConverter() {
        @Override
        public Object convert(Object value, Class<?> valueClass, Class<?> expectClass, Type expectType, ConversionPath conversionPath) throws Exception {

            // 1> Collection to collection ////////////////////////////////////////////////////////////////////////

            Object convertedValue;
            try {
                // To specified collection
                convertedValue = MTB_COLLECTION_MAPPER.onConvert(value, valueClass, expectClass, expectType, false, this, OBJECT_INSTANTIATOR, MapToBeanConverterImpl.this, conversionPath);
            } catch (Throwable e) {
                throw new ConversionRuntimeException("Property convert failed, Convert from " + valueClass.getName() +
                        " to " + expectType.getTypeName() + ", in collection to collection mode", e, conversionPath);
            }
            if (convertedValue == null) {
                // Is collection to collection, but value treated as null
                return null;
            }
            if (convertedValue != MxbCollectionMapper.RESULT_NOT_COLLECTION_TO_COLLECTION) {
                //Convert succeed
                return convertedValue;
            }

            // 2> Type matched ////////////////////////////////////////////////////////////////////////////////////

            if (expectClass.isAssignableFrom(valueClass)) {
                return value;
            }

            // 3> Type mapping ////////////////////////////////////////////////////////////////////////////////////

            if (typeMapping) {
                try {
                    convertedValue = TYPE_MAPPER_CENTER.onConvert(value, expectClass, expectType, MxbTypeMapper.Cause.MAP_TO_BEAN);
                } catch (Throwable e) {
                    throw new ConversionRuntimeException("Property convert failed, Convert from " + valueClass.getName() +
                            " to " + expectType.getTypeName() + ", in type mapping mode", e, conversionPath);
                }
                if (convertedValue == null) {
                    // Converted, but value treated as null
                    return null;
                }
                if (convertedValue != MxbTypeMapperCenter.RESULT_NO_PROPER_MAPPER) {
                    // Convert succeed
                    return convertedValue;
                }
            }

            // 4> Map to bean ///////////////////////////////////////////////////////////////////////////////////

            //If it is from a Map to a writable bean
            if (isMapToBean(valueClass, expectClass)) {
                try {
                    // Recursive call
                    return MapToBeanConverterImpl.this.convert0(formatMap((Map<?, ?>) value), expectClass, conversionPath);
                } catch (Throwable e) {
                    throw new ConversionRuntimeException("Property convert failed, Convert from " + valueClass.getName() +
                            " to " + expectType.getTypeName() + ", in map to bean mode", e, conversionPath);
                }
            }

            // Failed
            throw new ConversionRuntimeException("Property convert failed, There is no way to convert from " +
                    valueClass.getName() + " to " + expectClass.getName(), null, conversionPath);
        }

        private boolean isMapToBean(Class<?> valueClass, Class<?> expectClass) {
            return Map.class.isAssignableFrom(valueClass) &&
                    !Map.class.isAssignableFrom(expectClass) &&
                    TYPE_JUDGER.isBean(expectClass, inspectBeanStrictly, true);
        }

    };

    /**
     * @inheritDoc
     */
    @Override
    public void throwConversionException(String message, Throwable cause, ConversionPath conversionPath) throws ConversionRuntimeException {
        if (throwExceptionIfFails) {
            throw new ConversionRuntimeException(message, cause, conversionPath);
        } else if (exceptionCollector != null) {
            exceptionCollector.onException(new ConversionRuntimeException(message, cause, conversionPath));
        }
    }

    /**
     * Map[?, ?] to Map[String, Object]
     */
    private Map<String, Object> formatMap(Map<?, ?> map) {
        if (map == null) {
            return new HashMap<>(0);
        }
        Map<String, Object> newMap = new HashMap<>(map.size() << 1);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            newMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return newMap;
    }

    /**
     * Builder
     */
    public static class Builder {

        private boolean typeMapping = true;
        private boolean throwExceptionIfFails = false;
        private ConversionExceptionCollector exceptionCollector;
        private boolean inspectBeanStrictly = false;
        private boolean propertyUpperCamelCase = false;

        Builder() {
        }

        /**
         * Whether to allow type mapping, use built-in type mappers (MxbTypeMapper)
         *
         * @param typeMapping true: Allow type mapping (default)
         */
        public Builder typeMapping(boolean typeMapping) {
            this.typeMapping = typeMapping;
            return this;
        }

        public Builder setTypeMapping(boolean typeMapping) {
            this.typeMapping = typeMapping;
            return this;
        }

        /**
         * Whether to throw an exception when the field mapping fails
         *
         * @param throwExceptionIfFails true: Throw exception,
         *                              false: Field left null (default)
         */
        public Builder throwExceptionIfFails(boolean throwExceptionIfFails) {
            this.throwExceptionIfFails = throwExceptionIfFails;
            return this;
        }

        public Builder setThrowExceptionIfFails(boolean throwExceptionIfFails) {
            this.throwExceptionIfFails = throwExceptionIfFails;
            return this;
        }

        /**
         * Exception collector.
         * Only valid when throwExceptionIfFails = false (that is, "error skip" mode).
         * Used to collect exceptions that were ignored during the conversion process (can be used to print logs and troubleshooting).
         * *
         * @param exceptionCollector Exception collector. null by default.
         */
        public Builder exceptionCollector(ConversionExceptionCollector exceptionCollector) {
            this.exceptionCollector = exceptionCollector;
            return this;
        }

        public Builder setExceptionCollector(ConversionExceptionCollector exceptionCollector) {
            this.exceptionCollector = exceptionCollector;
            return this;
        }

        /**
         * By default, we only require that the bean has a write method in the case of Map -> Bean, and only require
         * that the bean has a read method in the case of Bean -> Map. But if you set inspectBeanStrictly to true,
         * all properties of Bean must have both read and write methods.
         * @param inspectBeanStrictly true: Property of Bean must have both read and write methods,
         *                            false: (default)
         */
        public Builder inspectBeanStrictly(boolean inspectBeanStrictly) {
            this.inspectBeanStrictly = inspectBeanStrictly;
            return this;
        }

        public Builder setInspectBeanStrictly(boolean inspectBeanStrictly) {
            this.inspectBeanStrictly = inspectBeanStrictly;
            return this;
        }

        /**
         * By default, when Map -> Bean or Bean -> Map, the property name should be 'lowerCamelCase' at the Map side.
         * But if you set propertyUpperCamelCase to true, the property name will be 'UpperCamelCase' at the Map side.
         *
         * @param propertyUpperCamelCase true: Property name will be 'UpperCamelCase' at the Map side,
         *                               false: lowerCamelCase (default)
         */
        public Builder propertyUpperCamelCase(boolean propertyUpperCamelCase) {
            this.propertyUpperCamelCase = propertyUpperCamelCase;
            return this;
        }

        public Builder setPropertyUpperCamelCase(boolean propertyUpperCamelCase) {
            this.propertyUpperCamelCase = propertyUpperCamelCase;
            return this;
        }

        public MapToBeanConverter build() {
            return new MapToBeanConverterImpl(typeMapping,
                    throwExceptionIfFails,
                    exceptionCollector,
                    inspectBeanStrictly,
                    propertyUpperCamelCase);
        }

    }

}
