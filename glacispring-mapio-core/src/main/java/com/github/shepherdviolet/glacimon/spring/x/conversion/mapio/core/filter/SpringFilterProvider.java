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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/***
 * <p>SpringFilterProvider: 通过Spring方式配置和加载过滤器.</p>
 *
 * @author shepherdviolet
 */
@Component
public class SpringFilterProvider implements FilterProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Filter> nameFilterMap;
    private Map<Class<?>, Filter> typeFilterMap;

    @Override
    public Filter findFilter(Class<? extends Filter> type, String name) {
        Filter filter;
        if (name != null) {
            // 根据名称获取过滤器
            filter = nameFilterMap.get(name);
            if (filter == null) {
                throw new NoSuchBeanDefinitionException("MapIO | SpringFilterProvider | No filter bean named '" + name +
                        "' is defined in spring application context.");
            }
            // 检查类型是否匹配
            if (!type.isAssignableFrom(filter.getClass())) {
                throw new NoSuchBeanDefinitionException("MapIO | SpringFilterProvider | The filter bean named '" + name +
                        "' (in spring application context) is not an instance of '" + type.getName() +
                        "' (the type you specified via annotation '@...Filter' series). Its type is " + filter.getClass().getName());
            }
        } else {
            // 根据类型获取过滤器 (类型必须相等)
            filter = typeFilterMap.get(type);
            if (filter == null) {
                throw new NoSuchBeanDefinitionException("MapIO | SpringFilterProvider | No filter of type '" + type.getName() +
                        "' is defined in spring application context.");
            }
        }
        return filter;
    }

    /**
     * 注入所有过滤器
     */
    @Autowired
    public void setFilters(ObjectProvider<Map<String, Filter>> filters) {
        nameFilterMap = filters.getIfAvailable(HashMap::new);
        typeFilterMap = nameFilterMap.values().stream().collect(Collectors.toMap(Filter::getClass, Function.identity()));

        if (Debug.isInfoLogEnabled() && logger.isInfoEnabled()) {
            for (Map.Entry<String, Filter> entry : nameFilterMap.entrySet()) {
                logger.info("MapIO | SpringFilterProvider | Filter loaded, name: " + entry.getKey() + ", type: " + entry.getValue().getClass().getName());
            }
        }
    }
}
