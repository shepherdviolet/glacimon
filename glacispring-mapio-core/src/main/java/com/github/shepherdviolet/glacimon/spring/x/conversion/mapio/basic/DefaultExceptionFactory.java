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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic;

import java.util.Arrays;

/**
 * <p>异常工厂默认实现: MapIo的公共逻辑和自带的过滤器在映射Map的过程中, 会抛出一些异常, 默认类型是RuntimeException. (默认实现: DefaultExceptionFactory)</p>
 *
 * @author shepherdviolet
 */
public class DefaultExceptionFactory implements ExceptionFactory {

    @Override
    public RuntimeException createRuntimeException(RuleInfo ruleInfo, ErrorCode errorCode, String... errorArgs) {
        switch (errorCode) {
            /*
             * 公共错误
             */
            case MISSING_REQUIRED_FIELD:
                return newException("Missing required field '" + errorArgs[0] + "'. " + ruleInfo);
            case FIELD_TYPE_NOT_MATCH_IO_ELEMENT_FILTER_EXPECT:
                return newException("Field '" + errorArgs[0] + "' filter element failed, @InputElementFilter/@OutputElementFilter can only filter elements for Map / Collection, actual input type: " + errorArgs[1] + ". " + ruleInfo);
            case FIELD_TYPE_NOT_MATCH_IO_MAPPER_EXPECT:
                return newException("Field '" + errorArgs[0] + "' map failed, @InputMapper/@OutputMapper can only filter elements for Map, actual input type: " + errorArgs[1] + ". " + ruleInfo);
            /*
             * 默认过滤器错误
             */
            case FIELD_TYPE_NOT_MATCH_FILTER_EXPECT:
                return newException("Type of field '" + errorArgs[0] + "' does not match what the filter expects, expect: " + errorArgs[1] + ", input type: " + errorArgs[2] + ". " + ruleInfo);
            case FIELD_LENGTH_OUT_OF_RANGE:
                return newException("Length of field '" + errorArgs[0] + "' out of range [" + errorArgs[1] + ", " + errorArgs[2] + "], field length: " + errorArgs[3] + ". " + ruleInfo);
            case STRING_FIELD_NOT_MATCH_REGEX:
                return newException("String field '" + errorArgs[0] + "' does not match regular expression '" + errorArgs[1] + "', input string: " + errorArgs[2] + ". " + ruleInfo);
        }
        return newException("Unknown error " + errorCode + Arrays.toString(errorArgs) + ". " + ruleInfo);
    }

    protected RuntimeException newException(String errorMsg){
        return new RuntimeException(errorMsg);
    }

}
