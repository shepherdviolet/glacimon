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

/**
 * 规则子句, 对应字典中的每一行注解规则
 *
 * @author shepherdviolet
 */
abstract class RuleClause {

    private final RuleInfo ruleInfo;

    public RuleClause(RuleInfo ruleInfo) {
        this.ruleInfo = ruleInfo;
    }

    public abstract Object doFilter(Object value);

    public RuleInfo getRuleInfo() {
        return ruleInfo;
    }

}