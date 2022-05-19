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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>字段输入规则: 标记该字段需要从源Map输入.</p>
 *
 * @author shepherdviolet
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {

    /**
     * <p>[可选参数] 用该名称(key)从源Map取值</p>
     *
     * <p>fromKey[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fromKey[已设置] : 源Map.get(fromKey) --> 目标Map.put(字典中的字段名, value)</p>
     */
    String fromKey() default "";

    /**
     * <p>[可选参数] 源Map中是否必须存在该字段.</p>
     * <p>true : 若源Map中不存在指定字段, 则抛出异常 (默认)</p>
     * <p>false: 若源Map中不存在指定字段, 则不进行检查/转换, 也不放入目标Map</p>
     */
    boolean required() default true;

}
