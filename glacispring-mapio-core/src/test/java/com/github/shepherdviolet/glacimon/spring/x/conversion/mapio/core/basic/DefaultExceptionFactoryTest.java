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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.RuleInfo;
import org.junit.Assert;
import org.junit.Test;

public class DefaultExceptionFactoryTest {

    @Test
    public void test() {
        ExceptionFactory exceptionFactory = new DefaultExceptionFactory();

        RuleInfo dictionaryRuleInfo = new RuleInfo(Object.class.getName(), null, null, null, null, null);
        RuleInfo filterRuleInfo = new RuleInfo(Object.class.getName(), "fieldName1", "from1", "to1", "test.test.FooFilter@fooFilter", null);
        RuleInfo mapperRuleInfo = new RuleInfo(Object.class.getName(), "fieldName1", "from1", "to1", null, "@OutputMapper{dictionary=test.test.MyDict}");

        check(exceptionFactory.createRuntimeException(dictionaryRuleInfo, ErrorCode.MISSING_REQUIRED_FIELD, "key-name-1"),
                "Missing required field 'key-name-1'. Mapper rule in dictionary 'java.lang.Object'");

        check(exceptionFactory.createRuntimeException(filterRuleInfo, ErrorCode.FIELD_TYPE_NOT_MATCH_IO_ELEMENT_FILTER_EXPECT, "from1", "java.lang.Integer"),
                "Field 'from1' filter element failed, @InputElementFilter/@OutputElementFilter can only filter elements for Map / Collection, actual input type: java.lang.Integer. Mapper rule in dictionary 'java.lang.Object' on field 'fieldName1' ('from1' -> 'to1'), the filter 'test.test.FooFilter@fooFilter'");

        check(exceptionFactory.createRuntimeException(mapperRuleInfo, ErrorCode.FIELD_TYPE_NOT_MATCH_IO_MAPPER_EXPECT, "from1", "java.lang.Integer"),
                "Field 'from1' map failed, @InputMapper/@OutputMapper can only filter elements for Map, actual input type: java.lang.Integer. Mapper rule in dictionary 'java.lang.Object' on field 'fieldName1' ('from1' -> 'to1'), the sub-mapper '@OutputMapper{dictionary=test.test.MyDict}'");

        check(exceptionFactory.createRuntimeException(filterRuleInfo, ErrorCode.FIELD_TYPE_NOT_MATCH_FILTER_EXPECT, "from1", "java.util.Map", "java.util.ArrayList"),
                "Type of field 'from1' does not match what the filter expects, expect: java.util.Map, input type: java.util.ArrayList. Mapper rule in dictionary 'java.lang.Object' on field 'fieldName1' ('from1' -> 'to1'), the filter 'test.test.FooFilter@fooFilter'");

        check(exceptionFactory.createRuntimeException(filterRuleInfo, ErrorCode.FIELD_LENGTH_OUT_OF_RANGE, "from1", "0", "8", "9"),
                "Length of field 'from1' out of range [0, 8], field length: 9. Mapper rule in dictionary 'java.lang.Object' on field 'fieldName1' ('from1' -> 'to1'), the filter 'test.test.FooFilter@fooFilter'");

        check(exceptionFactory.createRuntimeException(filterRuleInfo, ErrorCode.STRING_FIELD_NOT_MATCH_REGEX, "from1", "^1(3[0-9]|4[57]|5[0-35-9]|7[6-8]|8[0-9]|70)\\d{8}$", "88888888"),
                "String field 'from1' does not match regular expression '^1(3[0-9]|4[57]|5[0-35-9]|7[6-8]|8[0-9]|70)\\d{8}$', input string: 88888888. Mapper rule in dictionary 'java.lang.Object' on field 'fieldName1' ('from1' -> 'to1'), the filter 'test.test.FooFilter@fooFilter'");

    }

    private void check(Throwable throwable, String expectErrorMsg) {
        Assert.assertEquals(expectErrorMsg, throwable.getMessage());
    }

}
