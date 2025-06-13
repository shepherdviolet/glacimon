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

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class IgnoreCaseHashMapTest {

    @Test
    void testPutAndGetIgnoreCase() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("AbC", "123");
        assertEquals("123", map.get("abc"));
        assertEquals("123", map.get("ABC"));
        assertEquals("123", map.get("AbC"));
    }

    @Test
    void testKeyStylePreserved() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("HeAdEr", "val");
        Set<String> keys = map.keySet();
        assertEquals(StreamingBuilder.hashSet().add("HeAdEr").build(), keys);
    }

    @Test
    void testOverwriteWithDifferentCase() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("Key", "one");
        map.put("KEY", "two");

        assertEquals(1, map.size());
        assertEquals("two", map.get("key"));

        // 遍历 keySet 确认样式是最新的 KEY
        Set<String> keys = map.keySet();
        assertTrue(keys.contains("KEY"));
        assertEquals(StreamingBuilder.hashSet().add("KEY").build(), keys);
    }

    @Test
    void testRemoveByDifferentCase() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("SomeKey", "value");
        assertEquals("value", map.remove("SOMEkey"));
        assertTrue(map.isEmpty());
    }

    @Test
    void testPutIfAbsent() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.putIfAbsent("foo", "bar");
        map.putIfAbsent("FOO", "baz");

        assertEquals("bar", map.get("fOo"));
        assertEquals(1, map.size());
    }

    @Test
    void testReplace() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("Hello", "world");

        assertTrue(map.replace("HELLO", "world", "universe"));
        assertEquals("universe", map.get("hello"));

        assertFalse(map.replace("hello", "nope", "test"));
    }

    @Test
    void testComputeAndMerge() {
        IgnoreCaseHashMap<Integer> map = new IgnoreCaseHashMap<>();
        map.put("Count", 1);

        map.compute("COUNT", (k, v) -> v + 1);
        assertEquals(2, map.get("count"));

        map.merge("coUNT", 3, Integer::sum);
        assertEquals(5, map.get("COUNT"));
    }

    @Test
    void testNullKeyAndNullValue() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put(null, "nullkey");
        map.put("NullValue", null);

        assertNull(map.get("NullValue"));
    }

    @Test
    void testEntrySetAndValues() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("One", "1");
        map.put("TWO", "2");

        Set<String> keys = map.keySet();
        assertTrue(keys.contains("One") || keys.contains("TWO"));

        Collection<String> values = map.values();
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
    }

    @Test
    void testClearAndSize() {
        IgnoreCaseHashMap<String> map = new IgnoreCaseHashMap<>();
        map.put("A", "a");
        map.put("B", "b");

        assertEquals(2, map.size());
        map.clear();
        assertTrue(map.isEmpty());
    }

}
