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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filters;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.ImplementationName;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.RuleInfo;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ErrorCode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactoryAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * <p>过滤器: 用正则表达式检查String字段</p>
 *
 * <p>示例</p>
 * <pre>
 *     转换: @...Filter(type = StringCheckRegex.class, args = "^1(3[0-9]|4[57]|5[0-35-9]|7[6-8]|8[0-9]|70)\d{8}$")
 * </pre>
 *
 * @author shepherdviolet
 */
@ImplementationName("StringCheckRegex")
public final class StringCheckRegex implements Filter, ExceptionFactoryAware {

    private final Map<String, Pattern> patterns = new ConcurrentHashMap<>();

    private ExceptionFactory exceptionFactory;

    /**
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    @Override
    public void doPreCheckArgs(String[] args) throws Exception {
        if (args.length != 1) {
            throw new RuntimeException("Filter 'StringCheckRegex' | args.length should be 1. " +
                    "For example: @...Filter(type = StringCheckRegex.class, args = \"^1(3[0-9]|4[57]|5[0-35-9]|7[6-8]|8[0-9]|70)\\d{8}$\")");
        }
    }

    @Override
    public Object doFilter(Object element, String[] args, RuleInfo ruleInfo) {
        if (!(element instanceof String)) {
            throw exceptionFactory.createRuntimeException(ruleInfo, ErrorCode.FIELD_TYPE_NOT_MATCH_FILTER_EXPECT,
                    ruleInfo.getFromKey(), "String", element.getClass().getName());
        }

        if (compileRegex(args[0]).matcher((String) element).matches()) {
            throw exceptionFactory.createRuntimeException(ruleInfo, ErrorCode.STRING_FIELD_NOT_MATCH_REGEX,
                    ruleInfo.getFromKey(), args[0], (String) element);
        }

        return element;
    }

    @Override
    public void setExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    private Pattern compileRegex(String regex) {
        return patterns.computeIfAbsent(regex, Pattern::compile);
    }

}
