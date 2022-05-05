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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.rule;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter;

import java.lang.annotation.Annotation;

public class OutputFilterRule extends OrderedRule {

    /**
     * 过滤器类型, type和name至少设置一个, 否则无效
     */
    private final Class<? extends Filter> type;

    /**
     * 过滤器名称, type和name至少设置一个, 否则无效
     */
    private final String name;

    /**
     * 过滤参数, 可选
     */
    private final String[] args;

    public OutputFilterRule(Annotation annotation, int order,
                            Class<? extends Filter> type, String name, String[] args) {
        super(annotation, order);

        if (type == null) {
            type = Filter.class;
        }
        if (name == null) {
            name = "";
        }
        if (args == null) {
            args = new String[0];
        }
        this.type = type;
        this.name = name;
        this.args = args;
    }

    public Class<? extends Filter> type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String[] args() {
        return args;
    }


}
