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

package com.github.shepherdviolet.glacimon.java.spi.api.annotation;

import java.lang.annotation.*;

/**
 * Marked on the setter method or field of implementation class, allow parameter to be injected through it.
 * The setter method must conform to the JavaBean specification.
 * The method parameter type / field type can be String/boolean/int/long/float/double.
 *
 * @author shepherdviolet
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PropertyInject {

    /**
     * If this parameter is set, it will first get the value from the vm option (System#getProperty),
     * if it does not exist, then get value from the properties file (META-INF/glacimonspi/properties/...).
     *
     * E.g. The value of this parameter is 'sample.log.enabled', and this annotation is mark on method 'setLogEnabled',
     * the value of `META-INF/glacimonspi/properties/sample.ServiceImpl` is 'logEnabled=true'.
     * 1. It will first looking for '-Dsample.log.enabled' in vm options.
     * 2. If not exists, looking for '-Dglacimonspi.property.sample.ServiceImpl.logEnabled' in vm options.
     * 3. If not exists, looking for 'logEnabled' in file `META-INF/glacimonspi/properties/sample.ServiceImpl`.
     *
     * @return vm option key
     */
    String getVmOptionFirst() default "";

}
