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
 * 映射模式
 *
 * @author shepherdviolet
 */
public enum IoMode {

    /**
     * 输入: 根据字典类中的@Input...系列注解声明的映射规则, 从源Map中获取字段, 校验并转换, 然后返回目标Map
     */
    INPUT,

    /**
     * 输出: 根据字典类中的@Output...系列注解声明的映射规则, 从源Map中获取字段, 校验并转换, 然后返回目标Map
     */
    OUTPUT

}
