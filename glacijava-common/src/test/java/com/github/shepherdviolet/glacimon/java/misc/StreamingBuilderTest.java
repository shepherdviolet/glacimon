/*
 * Copyright (C) 2022-2023 S.Violet
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

package com.github.shepherdviolet.glacimon.java.misc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class StreamingBuilderTest implements StreamingBuildable {

    @Test
    public void test() {

        Map<String, Object> map = StreamingBuilder.hashMap()
                .key("key1").value("hello1")
                .key("key2").value("hello2")
                .key("key3").value(
                        StreamingBuilder.hashMap()
                                .key("k1").value("v1")
                                .key("k2").value("v2")
                                .build()
                )
                .build();

        Assert.assertEquals("{key1=hello1, key2=hello2, key3={k1=v1, k2=v2}}", String.valueOf(map));

        map = buildLinkedHashMap()
                .key("key11").value("hello11")
                .key("key22").value("hello22")
                .key("key33").value(
                        buildLinkedHashMap()
                                .key("k11").value("v11")
                                .key("k22").value("v22")
                                .build()
                )
                .build();

        Assert.assertEquals("{key11=hello11, key22=hello22, key33={k11=v11, k22=v22}}", String.valueOf(map));

    }

}
