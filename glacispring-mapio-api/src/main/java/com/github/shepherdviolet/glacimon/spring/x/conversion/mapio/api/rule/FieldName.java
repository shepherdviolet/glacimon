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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>[不建议使用]字段输入规则: 覆盖字典中的字段名, 对于@Input来说相当于toKey, 对于@Output来说相当于fromKey.</p>
 * <p></p>
 * <p>字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
 * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名. 对外的字段名没有限制, 因为可以用@Input的fromKey
 * 和@Output的toKey指定. </p>
 * <p></p>
 * <p>Input时:</p>
 * <p>fromKey[未设置] fieldName[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
 * <p>fromKey[已设置] fieldName[未设置] : 源Map.get(fromKey) --> 目标Map.put(字典中的字段名, value)</p>
 * <p>fromKey[未设置] fieldName[已设置] : 源Map.get(fieldName) --> 目标Map.put(fieldName, value)</p>
 * <p>fromKey[已设置] fieldName[已设置] : 源Map.get(fromKey) --> 目标Map.put(fieldName, value)</p>
 * <p></p>
 * <p>Output时:</p>
 * <p>fieldName[未设置] toKey[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
 * <p>fieldName[未设置] toKey[已设置] : 源Map.get(字典中的字段名) --> 目标Map.put(toKey, value)</p>
 * <p>fieldName[已设置] toKey[未设置] : 源Map.get(fieldName) --> 目标Map.put(fieldName, value)</p>
 * <p>fieldName[已设置] toKey[已设置] : 源Map.get(fieldName) --> 目标Map.put(toKey, value)</p>
 *
 * @author shepherdviolet
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface FieldName {

    /**
     * <p>[不建议使用]覆盖字典中的字段名, 对于@Input来说相当于toKey, 对于@Output来说相当于fromKey.</p>
     * <p></p>
     * <p>字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
     * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名. 对外的字段名没有限制, 因为可以用@Input的fromKey
     * 和@Output的toKey指定. </p>
     * <p></p>
     * <p>Input时:</p>
     * <p>fromKey[未设置] fieldName[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fromKey[已设置] fieldName[未设置] : 源Map.get(fromKey) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fromKey[未设置] fieldName[已设置] : 源Map.get(fieldName) --> 目标Map.put(fieldName, value)</p>
     * <p>fromKey[已设置] fieldName[已设置] : 源Map.get(fromKey) --> 目标Map.put(fieldName, value)</p>
     * <p></p>
     * <p>Output时:</p>
     * <p>fieldName[未设置] toKey[未设置] : 源Map.get(字典中的字段名) --> 目标Map.put(字典中的字段名, value)</p>
     * <p>fieldName[未设置] toKey[已设置] : 源Map.get(字典中的字段名) --> 目标Map.put(toKey, value)</p>
     * <p>fieldName[已设置] toKey[未设置] : 源Map.get(fieldName) --> 目标Map.put(fieldName, value)</p>
     * <p>fieldName[已设置] toKey[已设置] : 源Map.get(fieldName) --> 目标Map.put(toKey, value)</p>
     */
    String value();

}
