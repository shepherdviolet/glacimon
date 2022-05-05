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
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ErrorCode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.IoMode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 映射器, 对应字典.
 *
 * 1.从源Map取值
 * 2.根据规则映射/检查/转换字段
 * 2.放到目标Map中
 *
 * @author shepherdviolet
 */
class Mapper {

    // key值仅用于去重 (toKey)
    private final List<FieldRule> inputRules;

    // key值仅用于去重 (toKey)
    private final List<FieldRule> outputRules;

    private final ExceptionFactory exceptionFactory;

    private final RuleInfo ruleInfo;

    public Mapper(List<FieldRule> inputRules, List<FieldRule> outputRules, ExceptionFactory exceptionFactory, RuleInfo ruleInfo) {
        if (inputRules == null | outputRules == null | exceptionFactory == null) {
            throw new IllegalStateException("MapIO | Invalid state, inputRules == null | outputRules == null | exceptionFactory == null | ruleInfo == null");
        }
        this.inputRules = inputRules;
        this.outputRules = outputRules;
        this.exceptionFactory = exceptionFactory;
        this.ruleInfo = ruleInfo;
    }

    /**
     * do map
     */
    public void doMap(Map<String, Object> rawData, IoMode ioMode, Map<String, Object> mappedData, Set<String> mappedKey) {
        // get rules by id
        List<FieldRule> rules;
        switch (ioMode) {
            case INPUT:
                rules = inputRules;
                break;
            case OUTPUT:
                rules = outputRules;
                break;
            default:
                throw new RuntimeException("MapIO | invalid ioMode: " + ioMode);
        }

        // 遍历规则 (不要key)
        for (FieldRule rule : rules) {
            try {

                // key标记为已映射
                mappedKey.add(rule.getFromKey());

                // 取值
                Object value = rawData.get(rule.getFromKey());

                if (value == null) {
                    // 字段必要检查 (只检查非空)
                    if (rule.isRequired()) {
                        throw exceptionFactory.createRuntimeException(ruleInfo, ErrorCode.MISSING_REQUIRED_FIELD, rule.getFromKey());
                    }
                    continue;
                }

                // 字段校验/转换
                value = rule.doFilter(value);

                // 赋值
                mappedData.put(rule.getToKey(), value);

            } catch (RuntimeException t) {
                if (Debug.isErrorLogEnabled()) {
                    Debug.addErrorTrace(", on field '" + rule.getFromKey() + "' to '" + rule.getToKey() + "'");
                }
                throw t;
            }
        }
    }

    @Override
    public String toString() {
        return "Mapper{" +
                "dictionary=" + ruleInfo.getDictionaryClass() +
                ", inputRules=" + inputRules +
                ", outputRules=" + outputRules +
                '}';
    }

    /**
     * 打印映射器信息
     */
    public String printMapper() {
        StringBuilder stringBuilder = new StringBuilder("[Mapper]");
        stringBuilder.append("\n    dictionary: ").append(ruleInfo.getDictionaryClass());
        stringBuilder.append("\n    inputRules:");
        printRules(inputRules, stringBuilder);
        stringBuilder.append("\n    outputRules:");
        printRules(outputRules, stringBuilder);
        return stringBuilder.toString();
    }

    private void printRules(List<FieldRule> rules, StringBuilder stringBuilder) {
        for (FieldRule rule : rules) {
            stringBuilder.append("\n        ").append(rule.printFieldRule());
        }
    }

}