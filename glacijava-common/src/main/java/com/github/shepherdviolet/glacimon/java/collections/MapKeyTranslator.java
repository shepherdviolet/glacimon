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

import java.util.*;
import java.util.function.Supplier;

/**
 * MapKeyTranslator 类用于将一个源 Map 中的键值对根据指定的键映射规则转换到一个目标 Map 中。
 * 它支持多种空值处理策略，并且可以自定义目标 Map 的创建方式。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 *         Map<String, Object> fromMap = new HashMap<>();
 *         fromMap.put("name", "John");
 *         fromMap.put("age", null);
 *
 *         // from 'name' to 'Username'   Equivalent to toMap.put("Username", fromMap.get("name"))
 *         // from 'age' to 'Age'         Equivalent to toMap.put("Age", fromMap.get("age"))
 *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(
 *                 "Username", "name",
 *                 "Age", "age"
 *         ).translate(fromMap, MapKeyTranslator.NullStrategy.KEEP_NULL);;
 *
 *         // toMap: {Username=John, Age=null}
 * }
 * </pre>
 */
public class MapKeyTranslator {

    private final List<KeyMapping> keyMappingList;

    /**
     * 根据传入的键映射规则创建一个 MapKeyTranslator 实例。
     * 键映射规则以可变参数形式传入，格式为 toKey1, fromKey1, toKey2, fromKey2, ...
     * 如果最后一个参数没有对应的 fromKey，则会创建一个 KeyMappingWithoutFrom 实例。
     *
     * <p>使用示例：
     * <pre>
     * {@code
     *         Map<String, Object> fromMap = new HashMap<>();
     *         fromMap.put("name", "John");
     *         fromMap.put("age", null);
     *
     *         // from 'name' to 'Username'   Equivalent to toMap.put("Username", fromMap.get("name"))
     *         // from 'age' to 'Age'         Equivalent to toMap.put("Age", fromMap.get("age"))
     *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap, MapKeyTranslator.NullStrategy.KEEP_NULL);;
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param keyMappings 键映射规则，格式为 toKey1, fromKey1, toKey2, fromKey2, ...
     * @return 一个新的 MapKeyTranslator 实例
     */
    public static MapKeyTranslator keyMappings(String... keyMappings) {
        List<KeyMapping> keyMappingList = new ArrayList<>(keyMappings.length);
        String toKey = null;
        int index = 0;
        for (String key : keyMappings) {
            if (++index % 2 == 1) {
                toKey = key;
            } else {
                keyMappingList.add(new KeyMapping(toKey, key));
            }
        }
        if (++index % 2 == 0) {
            keyMappingList.add(new KeyMappingWithoutFrom(toKey));
        }
        return new MapKeyTranslator(keyMappingList);
    }

    private MapKeyTranslator(List<KeyMapping> keyMappingList) {
        this.keyMappingList = keyMappingList;
    }

    /**
     * 根据键映射规则将源 Map 中的键值对转换到目标 Map 中，并根据指定的空值处理策略处理空值。
     * 可以通过 Supplier 自定义目标 Map 的创建方式。
     *
     * <p>使用示例：
     * <pre>
     * {@code
     *         Map<String, Object> fromMap = new HashMap<>();
     *         fromMap.put("name", "John");
     *         fromMap.put("age", null);
     *
     *         // from 'name' to 'Username'   Equivalent to toMap.put("Username", fromMap.get("name"))
     *         // from 'age' to 'Age'         Equivalent to toMap.put("Age", fromMap.get("age"))
     *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap, MapKeyTranslator.NullStrategy.KEEP_NULL);;
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param <V>             源 Map 和目标 Map 中值的类型
     * @param fromMap         源 Map，包含要转换的键值对
     * @param nullStrategy    空值处理策略，指定当从源 Map 中获取的值为 null 时的处理方式
     * @return 转换后的目标 Map (HashMap)
     */
    public <V> Map<String, V> translate(Map<String, V> fromMap, NullStrategy nullStrategy) {
        return translate(fromMap, nullStrategy, null);
    }

    /**
     * 根据键映射规则将源 Map 中的键值对转换到目标 Map 中，并根据指定的空值处理策略处理空值。
     * 可以通过 Supplier 自定义目标 Map 的创建方式。
     *
     * <p>使用示例：
     * <pre>
     * {@code
     *         Map<String, Object> fromMap = new HashMap<>();
     *         fromMap.put("name", "John");
     *         fromMap.put("age", null);
     *
     *         // from 'name' to 'Username'   Equivalent to toMap.put("Username", fromMap.get("name"))
     *         // from 'age' to 'Age'         Equivalent to toMap.put("Age", fromMap.get("age"))
     *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap, MapKeyTranslator.NullStrategy.KEEP_NULL, LinkedHashMap::new);;
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param <V>             源 Map 和目标 Map 中值的类型
     * @param fromMap         源 Map，包含要转换的键值对
     * @param nullStrategy    空值处理策略，指定当从源 Map 中获取的值为 null 时的处理方式
     * @param toMapSupplier   目标 Map 的 Supplier，用于自定义目标 Map 的创建方式。如果为 null，则默认使用 HashMap
     * @return 转换后的目标 Map
     */
    @SuppressWarnings("unchecked")
    public <V> Map<String, V> translate(Map<String, V> fromMap, NullStrategy nullStrategy, Supplier<Map<String, Object>> toMapSupplier) {
        Map<String, V> toMap = null;
        if (toMapSupplier != null) {
            toMap = (Map<String, V>) toMapSupplier.get();
        }
        if (toMap == null) {
            toMap = new HashMap<>();
        }
        if (fromMap == null) {
            return toMap;
        }
        if (nullStrategy == null) {
            nullStrategy = NullStrategy.KEEP_NULL;
        }
        for (KeyMapping keyMapping : keyMappingList) {
            V value;
            if (keyMapping instanceof KeyMappingWithoutFrom) {
                value = null;
            } else {
                value = fromMap.get(keyMapping.fromKey);
            }
            if (value == null) {
                switch (nullStrategy) {
                    case SKIP_NULL:
                        break;
                    case THROW_ON_NULL:
                        throw new RuntimeException("The value of key '" + keyMapping.fromKey + "' in the fromMap is null");
                    case KEEP_NULL:
                    default:
                        toMap.put(keyMapping.toKey, null);
                }
            } else {
                toMap.put(keyMapping.toKey, value);
            }
        }
        return toMap;
    }

    /**
     * 表示键映射规则的内部类，包含源键和目标键。
     */
    private static class KeyMapping {

        private String toKey;
        private String fromKey;

        public KeyMapping(String toKey, String fromKey) {
            this.toKey = toKey;
            this.fromKey = fromKey;
        }

        public String toKey() {
            return toKey;
        }

        public String fromKey() {
            return fromKey;
        }
    }

    /**
     * 表示没有源键的键映射规则的内部类，继承自 KeyMapping。
     */
    private static class KeyMappingWithoutFrom extends KeyMapping {

        public KeyMappingWithoutFrom(String toKey) {
            super(toKey, null);
        }

    }

    /**
     * 从 fromMap 中取到空值时的处理策略。
     */
    public enum NullStrategy {
        /**
         * 保留空值。若 fromMap.get(fromKey) == null，则 toMap.put(toKey, null).
         */
        KEEP_NULL,

        /**
         * 跳过空值。若 fromMap.get(fromKey) == null，则不向 toMap 赋值.
         */
        SKIP_NULL,

        /**
         * 遇空值抛出异常。若 fromMap.get(fromKey) == null，则抛出异常
         */
        THROW_ON_NULL
    }

}