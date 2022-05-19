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

public class OutputRule extends BaseRule {

    /**
     * <p>[可选参数] 字段输出到目标Map中时, 使用该名称(key)</p>
     *
     * <p>toKey[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>toKey[已设置] : 源Map.get(字典中的字段名) --> 目标Map.put(toKey, value)</p>
     */
    private final String toKey;

    /**
     * <p>[可选参数] 源Map中是否必须存在该字段.</p>
     * <p>true : 若源Map中不存在指定字段, 则抛出异常 (默认)</p>
     * <p>false: 若源Map中不存在指定字段, 则不进行检查/转换, 也不放入目标Map</p>
     */
    private final boolean required;

    /**
     * <p>[不建议使用]覆盖字典中的字段名, 在这里相当于fromKey</p>
     *
     * <p>字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
     * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名.
     * 对外的字段名没有限制, 因为可以用toKey指定. </p>
     *
     * <p>fieldName[未设置] toKey[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fieldName[未设置] toKey[已设置] : 源Map.get(字典中的字段名) --> 目标Map.put(toKey, value)</p>
     *
     * <p>fieldName[已设置] toKey[未设置] : 源Map.get(fieldName) --> 目标Map.put(fieldName, value)</p>
     * <p>fieldName[已设置] toKey[已设置] : 源Map.get(fieldName) --> 目标Map.put(toKey, value)</p>
     */
    private final String fieldName;

    public OutputRule(Annotation annotation, String toKey, boolean required) {
        this(annotation, toKey, required, null);
    }

    public OutputRule(Annotation annotation, String toKey, boolean required, String fieldName) {
        super(annotation);

        if (toKey == null) {
            toKey = "";
        }
        if (fieldName == null) {
            fieldName = "";
        }
        this.toKey = toKey;
        this.required = required;
        this.fieldName = fieldName;
    }

    public String toKey() {
        return toKey;
    }

    public boolean required() {
        return required;
    }

    public String fieldName() {
        return fieldName;
    }

}
