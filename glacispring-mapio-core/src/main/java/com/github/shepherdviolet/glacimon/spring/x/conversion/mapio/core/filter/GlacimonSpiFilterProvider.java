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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter;

import com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi;
import com.github.shepherdviolet.glacimon.java.spi.core.MultipleServiceLoader;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.FilterRuleException;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * <p>GlacimonSpiFilterProvider: 通过GlacimonSpi方式配置和加载过滤器.</p>
 *
 * @author shepherdviolet
 */
public class GlacimonSpiFilterProvider implements FilterProvider {

    private final MultipleServiceLoader<Filter> serviceLoader;
    private final Map<Class<? extends Filter>, Filter> typeFilterMap = new HashMap<>();

    public GlacimonSpiFilterProvider() {
        // 用GlacimonSpi加载过滤器
        serviceLoader = GlacimonSpi.loadMultipleService(Filter.class);

        // type -> Filter
        List<Filter> filters = serviceLoader.getAll();
        for (Filter filter : filters) {
            typeFilterMap.put(filter.getClass(), filter);
        }
    }

    @Override
    public Filter findFilter(Class<? extends Filter> type, String name) {
        Filter filter;
        if (name != null) {
            // 根据名称获取过滤器
            filter = serviceLoader.get(name);
            if (filter == null) {
                throw new FilterRuleException("MapIO | GlacimonSpiFilterProvider | No filter named '" + name +
                        "' is defined. (Filters are defined by GlacimonSpi in JavaFilterProvider)");
            }
            // 检查类型是否匹配
            if (!type.isAssignableFrom(filter.getClass())) {
                throw new FilterRuleException("MapIO | GlacimonSpiFilterProvider | The filter named '" + name +
                        "' is not an instance of '" + type.getName() +
                        "' (the type you specified via annotation '@...Filter' series). Its type is " + filter.getClass().getName());
            }
        } else {
            // 根据类型获取过滤器 (类型必须相等)
            filter = typeFilterMap.get(type);
            if (filter == null) {
                throw new FilterRuleException("MapIO | GlacimonSpiFilterProvider | No filter of type '" + type.getName() +
                        "' is defined. (Filters are defined by GlacimonSpi in JavaFilterProvider)");
            }
        }
        return filter;
    }

}
