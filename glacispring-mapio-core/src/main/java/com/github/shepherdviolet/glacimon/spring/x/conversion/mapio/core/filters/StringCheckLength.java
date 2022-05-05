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
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ErrorCode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactoryAware;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.IntArgsConvertedFilter;

/**
 * <p>过滤器: String检查长度</p>
 *
 * <p>示例</p>
 * <pre>
 *     字段长度[8, ∞): @...Filter(type = StringCheckLength.class, args = {"8"})
 *     字段长度[0, 8]: @...Filter(type = StringCheckLength.class, args = {"0", "8"})
 * </pre>
 *
 * TODO 测试两种过滤参数
 *
 * @author shepherdviolet
 */
@ImplementationName("StringCheckLength")
public final class StringCheckLength extends IntArgsConvertedFilter implements ExceptionFactoryAware {

    private ExceptionFactory exceptionFactory;

    /**
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    @Override
    public void preCheckArgs(Integer[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new RuntimeException("Filter 'StringCheckLength' | args.length should be 1 or 2. " +
                    "For example: @...Filter(type = StringCheckLength.class, args = {\"0\", \"8\"})");
        }
    }

    @Override
    public Object filter(Object element, Integer[] args, RuleInfo ruleInfo) {
        if (!(element instanceof String)) {
            throw exceptionFactory.createRuntimeException(ruleInfo, ErrorCode.FIELD_TYPE_NOT_MATCH_FILTER_EXPECT,
                    ruleInfo.getFromKey(), "String", element.getClass().getName());
        }

        int length = ((String) element).length();
        if (length < args[0] || (args.length == 2 && length > args[1])) {
            throw exceptionFactory.createRuntimeException(ruleInfo, ErrorCode.FIELD_LENGTH_OUT_OF_RANGE,
                    ruleInfo.getFromKey(), String.valueOf(args[0]), args.length == 2 ? String.valueOf(args[1]) : "∞", String.valueOf(length));
        }
        return element;
    }

    @Override
    public void setExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

}
