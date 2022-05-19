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

import java.lang.annotation.Annotation;

public class InputRule extends BaseRule {

    /**
     * <p>[可选参数] 用该名称(key)从源Map取值</p>
     *
     * <p>fromKey[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fromKey[已设置] : 源Map.get(fromKey) --> 目标Map.put(字典中的字段名, value)</p>
     */
    private final String fromKey;

    /**
     * <p>[可选参数] 源Map中是否必须存在该字段.</p>
     * <p>true : 若源Map中不存在指定字段, 则抛出异常 (默认)</p>
     * <p>false: 若源Map中不存在指定字段, 则不进行检查/转换, 也不放入目标Map</p>
     */
    private final boolean required;

    /**
     * <p>[不建议使用]覆盖字典中的字段名, 在这里相当于toKey</p>
     *
     * <p>字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
     * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名.
     * 对外的字段名没有限制, 因为可以用fromKey指定. </p>
     *
     * <p>fromKey[未设置] fieldName[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fromKey[已设置] fieldName[未设置] : 源Map.get(fromKey) --> 目标Map.put(字典中的字段名, value)</p>
     *
     * <p>fromKey[未设置] fieldName[已设置] : 源Map.get(fieldName) --> 目标Map.put(fieldName, value)</p>
     * <p>fromKey[已设置] fieldName[已设置] : 源Map.get(fromKey) --> 目标Map.put(fieldName, value)</p>
     */
    private final String fieldName;

    public InputRule(Annotation annotation, String fromKey, boolean required) {
        this(annotation, fromKey, required, null);
    }

    public InputRule(Annotation annotation, String fromKey, boolean required, String fieldName) {
        super(annotation);

        if (fromKey == null) {
            fromKey = "";
        }
        if (fieldName == null) {
            fieldName = "";
        }
        this.fromKey = fromKey;
        this.required = required;
        this.fieldName = fieldName;
    }

    public String fromKey() {
        return fromKey;
    }

    public boolean required() {
        return required;
    }

    public String fieldName() {
        return fieldName;
    }

}
