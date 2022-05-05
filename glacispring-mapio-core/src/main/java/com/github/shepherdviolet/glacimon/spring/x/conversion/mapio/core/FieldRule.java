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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.RuleInfo;

import java.util.List;

/**
 * 字段规则, 对应字典中的字段规则.
 *
 * 1.源key
 * 2.规则子句
 * 2.目标Key
 *
 * @author shepherdviolet
 */
class FieldRule {

    private final String fromKey;

    private final String toKey;

    private final boolean required;

    private final List<RuleClause> clauses;

    private final RuleInfo ruleInfo;

    public FieldRule(String fromKey, String toKey, boolean required, List<RuleClause> clauses, RuleInfo ruleInfo) {
        this.fromKey = fromKey;
        this.toKey = toKey;
        this.required = required;
        this.clauses = clauses;
        this.ruleInfo = ruleInfo;
    }

    /**
     * 过滤字段
     */
    public Object doFilter(Object value) {
        for (RuleClause filter : clauses) {
            value = filter.doFilter(value);
        }
        return value;
    }

    public String getFromKey() {
        return fromKey;
    }

    public String getToKey() {
        return toKey;
    }

    public boolean isRequired() {
        return required;
    }

    public List<RuleClause> getClauses() {
        return clauses;
    }

    @Override
    public String toString() {
        return "FieldRule{" +
                "fromKey='" + fromKey + '\'' +
                ", toKey='" + toKey + '\'' +
                ", required=" + required +
                ", filters=" + clauses +
                '}';
    }

    public String printFieldRule() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fromKey='").append(fromKey).append('\'')
                .append(", toKey='").append(toKey).append('\'')
                .append(", required=").append(required);

        for (RuleClause filter : clauses) {
            stringBuilder.append("\n            ").append(filter);
        }

        return stringBuilder.toString();
    }

}