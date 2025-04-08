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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapKeyTranslatorTest {

    @Test
    public void test() {
        Map<String, Object> fromMap = new HashMap<>();
        fromMap.put("name", "John");
        fromMap.put("age", null);

        Map<String, Object> toMap = MapKeyTranslator.keyMappings(
                "Username", "name", // from 'name' to 'Username'
                "Age", "age" // from 'age' to 'Age'
        ).translate(fromMap, MapKeyTranslator.NullStrategy.KEEP_NULL, LinkedHashMap::new);;

        Assertions.assertEquals("{Username=John, Age=null}", toMap.toString());

        toMap = MapKeyTranslator.keyMappings(
                "Username", "name",
                "Age", "age"
        ).translate(fromMap, MapKeyTranslator.NullStrategy.KEEP_NULL, LinkedHashMap::new);;

        Assertions.assertEquals("{Username=John}", toMap.toString());
    }

}
