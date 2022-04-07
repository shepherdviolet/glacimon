/*
 * Copyright (C) 2015-2019 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.util.conversion;

import org.junit.Assert;
import org.junit.Test;
import sviolet.slate.common.util.common.LambdaBuildable;

public class StringPlaceHolderUtilsTest implements LambdaBuildable {

    @Test
    public void test(){
        Assert.assertEquals("data",
                StringPlaceHolderUtils.replaceStandardly("data", buildHashMap(i -> {
                })));
        Assert.assertEquals("prefix-001",
                StringPlaceHolderUtils.replaceStandardly("prefix-${id}", buildHashMap(i -> {
                    i.put("id", "001");
                })));
        Assert.assertEquals("prefix-000",
                StringPlaceHolderUtils.replaceStandardly("prefix-${id:000}", buildHashMap(i -> {
                })));
        Assert.assertEquals("prefix-${id}",
                StringPlaceHolderUtils.replaceStandardly("prefix-${id}", buildHashMap(i -> {
                })));
        Assert.assertEquals("prefix-999",
                StringPlaceHolderUtils.replaceStandardly("prefix-${id:${def}}", buildHashMap(i -> {
                    i.put("def", "999");
                })));
        Assert.assertEquals("prefix-123",
                StringPlaceHolderUtils.replaceStandardly("prefix-${${key}}", buildHashMap(i -> {
                    i.put("key", "mykey");
                    i.put("mykey", "123");
                })));
    }

}
