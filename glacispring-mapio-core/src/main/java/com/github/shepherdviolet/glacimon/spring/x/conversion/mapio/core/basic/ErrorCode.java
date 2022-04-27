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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic;

/**
 * <p>错误码</p>
 *
 * <p>MapIo的公共逻辑和自带的过滤器在映射Map的过程中, 会抛出一些异常. 为了让MapIo更好地适配应用工程, 我们不直接抛出异常,
 * 而是通过ExceptionFactory根据错误码创建异常, 然后抛出. </p>
 *
 * <p>应用工程可以扩展实现ExceptionFactory, 将那些异常修改为自己需要的类型, 适配自己的工程. </p>
 *
 * @author shepherdviolet
 */
public enum ErrorCode {

    // Common //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 错误, 缺少必要字段,
     * 字段是否必要由注解@Input/Output(required=true)设置, 默认为true
     *
     * error code: MISSING_REQUIRED_FIELD
     * error args[0]: 字段名(fromKey)
     */
    MISSING_REQUIRED_FIELD,

    /**
     * 错误, @InputElementFilter/@OutputElementFilter只能过滤Map或Collection类型的字段, 请确认输入字段类型,
     * 或在@InputElementFilter/@OutputElementFilter前添加@InputFilter/@OutputFilter将类型转换为Map或Collection
     *
     * error code: FIELD_TYPE_NOT_MATCH_IO_ELEMENT_FILTER_EXPECT
     * error args[0]: 字段名(fromKey)
     * error args[1]: 输入字段的类型(错误的)
     */
    FIELD_TYPE_NOT_MATCH_IO_ELEMENT_FILTER_EXPECT,

    /**
     * 错误, @InputMapper/@OutputMapper只能过滤Map类型的字段, 请确认输入字段类型,
     * 或在@InputMapper/@OutputMapper前添加@InputFilter/@OutputFilter将类型转换为Map
     *
     * error code: FIELD_TYPE_NOT_MATCH_IO_MAPPER_EXPECT
     * error args[0]: 字段名(fromKey)
     * error args[1]: 输入字段的类型(错误的)
     */
    FIELD_TYPE_NOT_MATCH_IO_MAPPER_EXPECT,

    // Default filters /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 错误, 过滤器无法处理此类型的输入字段.
     *
     * error code: FIELD_TYPE_NOT_MATCH_FILTER_EXPECT
     * error args[0]: 字段名(fromKey)
     * error args[1]: 过滤器需要的输入类型
     * error args[2]: 实际字段的类型
     */
    FIELD_TYPE_NOT_MATCH_FILTER_EXPECT,

    /**
     * 错误, 字段长度超过限制.
     *
     * error code: FIELD_LENGTH_OUT_OF_RANGE
     * error args[0]: 字段名(fromKey)
     * error args[1]: 长度下限(包含)
     * error args[2]: 长度上限制(包含)
     * error args[3]: 实际字段长度
     */
    FIELD_LENGTH_OUT_OF_RANGE,

    /**
     * 错误, String字段不符合正则表达式.
     *
     * error code: STRING_FIELD_NOT_MATCH_REGEX
     * error args[0]: 字段名(fromKey)
     * error args[1]: 正则表达式
     * error args[2]: 字段值
     */
    STRING_FIELD_NOT_MATCH_REGEX

}

