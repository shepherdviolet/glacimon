/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.java.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StreamingBuilderTest implements StreamingBuildable {

    @Test
    public void test() {

        Map<String, Object> map = StreamingBuilder.hashMap()
                .put("key1", "hello1")
                .put("key2", "hello2")
                .put("key3", StreamingBuilder.hashMap()
                        .put("k1", "v1")
                        .put("k2", "v2")
                        .build()
                )
                .build();

        Assertions.assertEquals("{key1=hello1, key2=hello2, key3={k1=v1, k2=v2}}", String.valueOf(map));

        map = buildLinkedHashMap()
                .put("key33", "hello11")
                .put("key22", "hello22")
                .put("key11", buildLinkedHashMap()
                        .put("k22", "v11")
                        .put("k11", "v22")
                        .build()
                )
                .build();

        Assertions.assertEquals("{key33=hello11, key22=hello22, key11={k22=v11, k11=v22}}", String.valueOf(map));

        List<String> list = StreamingBuilder.arrayList()
                .add("l1")
                .add("l2")
                .add("l3")
                .build();

        Assertions.assertEquals("[l1, l2, l3]", String.valueOf(list));

        list = buildArrayListOf("l1", "l2", "l3");

        Assertions.assertEquals("[l1, l2, l3]", String.valueOf(list));

        list = buildLinkedList()
                .add("l3")
                .add("l2")
                .add("l1")
                .build();

        Assertions.assertEquals("[l3, l2, l1]", String.valueOf(list));

        list = buildLinkedListOf("l3", "l2", "l1");

        Assertions.assertEquals("[l3, l2, l1]", String.valueOf(list));

        Set<String> set = buildHashSet()
                .add("s1")
                .add("s2")
                .build();

        Assertions.assertEquals("[s1, s2]", String.valueOf(set));

        set = buildHashSetOf("s1", "s2");

        Assertions.assertEquals("[s1, s2]", String.valueOf(set));

    }

}
