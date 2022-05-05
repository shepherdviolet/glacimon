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
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ErrorCode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactory;

import java.util.*;

/**
 * 规则子句, 对应字典中的@InputElementFilter/@OutputElementFilter
 *
 * TODO 测试map的key是其他类型的情况
 *
 * @author shepherdviolet
 */
class ElementFilterClause extends RuleClause {

    private final Filter filter;
    private final String[] args;
    private final boolean keepOrder;

    private final ExceptionFactory exceptionFactory;

    public ElementFilterClause(Filter filter, String[] args, boolean keepOrder,
                               ExceptionFactory exceptionFactory, RuleInfo ruleInfo) {
        super(ruleInfo);

        this.filter = filter;
        this.args = args;
        this.keepOrder = keepOrder;

        this.exceptionFactory = exceptionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object doFilter(Object value) {
        try {
            if (value instanceof Map) {
                return filterMap((Map<Object, Object>) value);
            } else if (value instanceof Collection) {
                return filterCollection((Collection<Object>) value);
            } else {
                throw exceptionFactory.createRuntimeException(getRuleInfo(), ErrorCode.FIELD_TYPE_NOT_MATCH_IO_ELEMENT_FILTER_EXPECT,
                        getRuleInfo().getFromKey(), value != null ? value.getClass().getName() : "null");
            }
        } catch (RuntimeException t) {
            if (Debug.isErrorLogEnabled()) {
                Debug.addErrorTrace(", filtering elements by filter '" + filter.getClass().getName() + "'" + Arrays.toString(args));
            }
            throw t;
        }
    }

    private Object filterMap(Map<Object, Object> map) {
        Map<Object, Object> result = keepOrder ?
                new LinkedHashMap<>(map.size() * 2) :
                new HashMap<>(map.size() * 2);

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), filter.doFilter(entry.getValue(), args, getRuleInfo()));
        }

        return result;
    }

    private Object filterCollection(Collection<Object> collection) {
        List<Object> result = new ArrayList<>(collection.size());

        for (Object element : collection) {
            result.add(filter.doFilter(element, args, getRuleInfo()));
        }

        return result;
    }

    @Override
    public String toString() {
        return "ElementFilterClause{" +
                "filter=" + filter +
                ", args=" + Arrays.toString(args) +
                ", keepOrder=" + keepOrder +
                '}';
    }

}