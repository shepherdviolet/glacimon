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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filters;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.ImplementationName;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.RuleInfo;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.Filter;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ErrorCode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ExceptionFactoryAware;

import java.util.ArrayList;
import java.util.Map;

/**
 * <p>过滤器: 将Map转换为List, 取key作为元素</p>
 *
 * <p>示例</p>
 * <pre>
 *     转换: @...Filter(type = MapKeysToList.class)
 * </pre>
 *
 * @author shepherdviolet
 */
@ImplementationName("MapKeysToList")
public final class MapKeysToList implements Filter, ExceptionFactoryAware {

    private ExceptionFactory exceptionFactory;

    /**
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    @Override
    public void doPreCheckArgs(String[] args) throws Exception {
        // do nothing
    }

    @Override
    public Object doFilter(Object element, String[] args, RuleInfo ruleInfo) {
        if (!(element instanceof Map)) {
            throw exceptionFactory.createRuntimeException(ruleInfo, ErrorCode.FIELD_TYPE_NOT_MATCH_FILTER_EXPECT,
                    ruleInfo.getFromKey(), "java.util.Map", element.getClass().getName());
        }

        return new ArrayList<>(((Map<?, ?>) element).keySet());
    }

    @Override
    public void setExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

}
