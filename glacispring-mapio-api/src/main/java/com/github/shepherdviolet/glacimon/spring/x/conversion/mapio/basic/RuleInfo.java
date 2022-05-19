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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic;

/**
 * <p>过滤规则信息, 用于打印日志/输出错误信息</p>
 *
 * @author shepherdviolet
 */
public class RuleInfo {

    private final String dictionaryClass;
    private final String fieldName;
    private final String fromKey;
    private final String toKey;
    private final String filterName;
    private final String subMapper;

    public RuleInfo(String dictionaryClass, String fieldName,
                    String fromKey, String toKey,
                    String filterName, String subMapper) {
        this.dictionaryClass = dictionaryClass;
        this.fieldName = fieldName;
        this.fromKey = fromKey;
        this.toKey = toKey;
        this.filterName = filterName;
        this.subMapper = subMapper;
    }

    /**
     * <p>当前执行的规则所在的字典类</p>
     */
    public String getDictionaryClass() {
        return dictionaryClass;
    }

    /**
     * <p>当前执行规则所在的字段(字典类下的字段名), 可空</p>
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * <p>正在用该名称(key)从源Map取值, 可空</p>
     */
    public String getFromKey() {
        return fromKey;
    }

    /**
     * <p>正在使用该名称(key)将字段输出到目标Map中时, 可空</p>
     */
    public String getToKey() {
        return toKey;
    }

    /**
     * <p>当前执行的过滤器名, 可空</p>
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * <p>当前执行的子映射器(@...Mapper), 可空</p>
     */
    public String getSubMapper() {
        return subMapper;
    }

    @Override
    public String toString() {
        String string = "Mapper rule in dictionary '" + dictionaryClass + "'";
        if (fieldName != null) {
            string += " on field '" + fieldName + "'";
            if (fromKey != null && toKey != null) {
                string += " ('" + fromKey + "' -> '" + toKey + "')";
            }
        }
        if (filterName != null) {
            string += ", the filter '" + filterName + "'";
        }
        if (subMapper != null) {
            string += ", the sub-mapper '" + subMapper + "'";
        }
        return string;
    }

}
