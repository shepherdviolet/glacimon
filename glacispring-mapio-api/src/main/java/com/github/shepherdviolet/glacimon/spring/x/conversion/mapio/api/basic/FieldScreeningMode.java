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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic;

/**
 * <p>Map字段筛选模式</p>
 *
 * @author shepherdviolet
 */
public enum FieldScreeningMode {

    /**
     * <p>源Map中的字段默认都保留.</p>
     * <p>在字典规则中配置的字段, 会进行相应检查和转换, 然后放入目标Map.</p>
     * <p>在字典规则中未配置的字段, 会保持原样放入目标Map.</p>
     */
    PASS_BY_DEFAULT,

    /**
     * <p>源Map中的字段默认都丢弃.</p>
     * <p>在字典规则中配置的字段, 会进行相应检查和转换, 然后放入目标Map.</p>
     * <p>在字典规则中未配置的子弹, 会被丢弃, 不会放入目标Map.</p>
     */
    DISCARD_BY_DEFAULT
}
