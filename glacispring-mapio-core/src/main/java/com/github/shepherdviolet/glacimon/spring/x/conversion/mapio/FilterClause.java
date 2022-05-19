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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.RuleInfo;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.Filter;

import java.util.Arrays;

/**
 * 规则子句, 对应字典中的@InputFilter/@OutputFilter
 *
 * @author shepherdviolet
 */
class FilterClause extends RuleClause {

    private final Filter filter;
    private final String[] args;

    public FilterClause(Filter filter, String[] args, RuleInfo ruleInfo) {
        super(ruleInfo);
        this.filter = filter;
        this.args = args;
    }

    @Override
    public Object doFilter(Object value) {
        try {
            return filter.doFilter(value, args, getRuleInfo());
        } catch (RuntimeException t) {
            if (Debug.isErrorLogEnabled()) {
                Debug.addErrorTrace(", filtering field by filter '" + filter.getClass().getName() + "'" + Arrays.toString(args));
            }
            throw t;
        }
    }

    @Override
    public String toString() {
        return "FilterClause{" +
                "filter=" + filter +
                ", args=" + Arrays.toString(args) +
                '}';
    }

}