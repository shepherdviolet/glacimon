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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.RuleInfo;
import org.junit.Assert;
import org.junit.Test;

public class ArgsCachedFilterTest {

    @Test
    public void test() throws Exception {

        // 测试缓存key值用hash解决冲突问题
        StringTestFilter stringTestFilter = new StringTestFilter();
        stringTestFilter.doPreCheckArgs(new String[]{"1, 2", "3"});
        stringTestFilter.doPreCheckArgs(new String[]{"1", "2, 3"});
        Assert.assertEquals("{\n" +
                        "[1, 2, 3]@1503070 = [1, 2, 3]\n" +
                        "[1, 2, 3]@1532924 = [1, 2, 3]\n" +
                        "}"
                , stringTestFilter.printCache());

        // 测试基本的转换特性
        IntTestFilter intTestFilter = new IntTestFilter();
        intTestFilter.doPreCheckArgs(new String[]{"1", "2", "3"});
        int result = (int) intTestFilter.doFilter(7, new String[]{"1", "2", "3"}, null);
        Assert.assertEquals("{\n" +
                        "[1, 2, 3]@48 = [1, 2, 3]\n" +
                        "}"
                , intTestFilter.printCache());
        Assert.assertEquals(13, result);

    }

    private static class StringTestFilter extends ArgsCachedFilter<String> {

        @Override
        public String[] preConvertArgs(String[] args) {
            return args;
        }

        @Override
        public void preCheckArgs(String[] args) {

        }

        @Override
        public Object filter(Object element, String[] args, RuleInfo ruleInfo) {
            return element;
        }

    }

    private static class IntTestFilter extends ArgsCachedFilter<Integer> {

        @Override
        public Integer[] preConvertArgs(String[] args) {
            Integer[] intArgs = new Integer[args.length];
            for (int i = 0 ; i < args.length ; i++) {
                intArgs[i] = Integer.parseInt(args[i]);
            }
            return intArgs;
        }

        @Override
        public void preCheckArgs(Integer[] args) {

        }

        @Override
        public Object filter(Object element, Integer[] args, RuleInfo ruleInfo) {
            int result = (int) element;
            for (Integer arg : args) {
                result += arg;
            }
            return result;
        }

    }

}
