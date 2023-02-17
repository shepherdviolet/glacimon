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

import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Inject properties to service instance
 *
 * @author shepherdviolet
 */
class PropertiesInjector {

    private final List<Injector> injectors = new LinkedList<>();
    private String selectReason;

    /**
     * inject properties to instance
     */
    void inject(Object instance, String loaderId) throws Exception {
        for (Injector injector : injectors) {
            injector.inject(instance, loaderId);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        for (int i = 0 ; i < injectors.size() ; i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(injectors.get(i));
        }
        return stringBuilder.append("}").toString();
    }

    List<Injector> getInjectors(){
        return injectors;
    }

    String getSelectReason() {
        return selectReason;
    }

    void setSelectReason(String selectReason) {
        this.selectReason = selectReason;
    }

    interface Injector {

        /**
         * inject value into field
         * @param instance bean instance
         * @param loaderId loader id
         * @throws Exception exception
         */
        void inject(Object instance, String loaderId) throws Exception;

        /**
         * get field name
         * @return field name
         */
        String getFieldName();

    }

    /**
     * Inject value by method
     */
    static class MethodInjector implements Injector {

        private static final SpiLogger LOGGER = LogUtils.getLogger();

        private final String fieldName;
        private final Method method;
        private final Object value;

        MethodInjector(String fieldName, Method method, Object value) {
            this.fieldName = fieldName;
            this.method = method;
            this.value = value;
        }

        @Override
        public void inject(Object instance, String loaderId) throws Exception {
            try {
                method.invoke(instance, value);
            } catch (Exception e) {
                throw new Exception("Error while injecting value '" + value + "' to " + fieldName + " by method " + method.getName());
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(loaderId + "|PropertyInject| Inject '" + value + "' to " + fieldName + " by method " + method.getName(), null);
            }
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return fieldName + "=" + value;
        }
    }

    /**
     * Inject value to field
     */
    static class FieldInjector implements Injector {

        private static final SpiLogger LOGGER = LogUtils.getLogger();

        private final String fieldName;
        private final Field field;
        private final Object value;

        FieldInjector(String fieldName, Field field, Object value) {
            this.fieldName = fieldName;
            this.field = field;
            this.value = value;
        }

        @Override
        public void inject(Object instance, String loaderId) throws Exception {
            try {
                field.set(instance, value);
            } catch (Exception e) {
                throw new Exception("Error while injecting value '" + value + "' to " + fieldName);
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(loaderId + "|PropertyInject| Inject '" + value + "' to " + fieldName, null);
            }
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return fieldName + "=" + value;
        }

    }

}
