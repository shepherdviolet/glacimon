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

package com.github.shepherdviolet.glacimon.java.conversion;

import com.github.shepherdviolet.glacimon.java.common.entity.KeyValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleKeyValueEncoderTest {

    @Test
    public void test1() throws SimpleKeyValueEncoder.DecodeException {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key", "value");
        map.put(null, "nullsvalue");
        map.put("nullskey", null);
        map.put("escape\\key", "escape\\value");
        map.put("split,key", "split,value");
        map.put("eq=key=", "=eq=value");
        map.put(" blank  key ", " blank  value ");
        map.put("\tblankkey\t", "\tblankvalue\t");
        map.put("newline\n", "\nreturn");

//        System.out.println(map);
        String encoded = SimpleKeyValueEncoder.encode(map);
//        System.out.println(encoded);

        Map<String, String> result = SimpleKeyValueEncoder.decode(encoded);
//        System.out.println(result);

        Assertions.assertEquals(map.toString(), result.toString());
    }

    @Test
    public void test2() throws SimpleKeyValueEncoder.DecodeException {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key", "value");
        map.put(null, "nullsvalue");
        map.put("nullskey", null);
        map.put("escape\\key", "escape\\value");
        map.put("split,key", "split,value");
        map.put("eq=key=", "=eq=value");
        map.put(" blank  key ", " blank  value ");
        map.put("\tblankkey\t", "\tblankvalue\t");
        map.put("newline\n", "\nreturn");

//        System.out.println(map);
        String encoded = SimpleKeyValueEncoder.encode(map, true);
//        System.out.println(encoded);

        Map<String, String> result = SimpleKeyValueEncoder.decode(encoded);
//        System.out.println(result);

        Assertions.assertEquals(map.toString(), result.toString());
    }

    @Test
    public void test3() throws SimpleKeyValueEncoder.DecodeException {
        List<KeyValue<String, String>> list = new ArrayList<>();
        list.add(new KeyValue<>("key", "value"));
        list.add(new KeyValue<>(null, "nullsvalue"));
        list.add(new KeyValue<>("nullskey", null));
        list.add(new KeyValue<>("escape\\key", "escape\\value"));
        list.add(new KeyValue<>("split,key", "split,value"));
        list.add(new KeyValue<>("eq=key=", "=eq=value"));
        list.add(new KeyValue<>(" blank  key ", " blank  value "));
        list.add(new KeyValue<>("\tblankkey\t", "\tblankvalue\t"));
        list.add(new KeyValue<>("newline\n", "\nreturn"));

//        System.out.println(map);
        String encoded = SimpleKeyValueEncoder.encode(list, true);
//        System.out.println(encoded);

        List<KeyValue<String, String>> result = SimpleKeyValueEncoder.decodeToList(encoded);
//        System.out.println(result);

        Assertions.assertEquals(list.toString(), result.toString());
    }

}
