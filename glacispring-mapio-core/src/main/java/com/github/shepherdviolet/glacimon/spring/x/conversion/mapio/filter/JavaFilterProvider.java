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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FilterRuleException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * <p>JavaFilterProvider: 简易过滤器提供者, 只支持通过type查找, 简单的用反射实例化过滤器实例.</p>
 *
 * @author shepherdviolet
 */
public class JavaFilterProvider implements FilterProvider {

    private final Map<Class<? extends Filter>, Filter> typeFilterMap = new ConcurrentHashMap<>();

    @Override
    public Filter findFilter(Class<? extends Filter> type, String name) {
        if (Filter.class.equals(type)) {
            throw new FilterRuleException("MapIO | JavaFilterProvider | JavaFilterProvider does not support lookup filters by name, name: " + name +
                    ". Please set filter by type like '@...Filter(type=XxxFilter.class)'");
        }
        if (type.isInterface()) {
            throw new FilterRuleException("MapIO | JavaFilterProvider | JavaFilterProvider does not support lookup filters by interface type, type: " + type.getName() +
                    ". Make sure the filter type can be instantiated. '");
        }

        return typeFilterMap.computeIfAbsent(type, k -> {
            try {
                return type.newInstance();
            } catch (Throwable t) {
                throw new FilterRuleException("MapIO | JavaFilterProvider | JavaFilterProvider fails to instantiate filter via reflection, type: " + type.getName() +
                        ". Make sure the filter type can be instantiated (has no-argument constructor). '", t);
            }
        });

    }

}
