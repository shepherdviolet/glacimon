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
 *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
 *                 "Username", "name",
 *                 "Age", "age"
 *         ).translate(fromMap);
 *
 *         // toMap: {Username=John, Age=null}
 * }
 * </pre>
 */
// 注意, 此处Map使用raw类型是为了兼容性, 若使用泛型不管是<K, V>还是<String, Object>还是<?, ?>均无法满足所有情况
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapKeyTranslator {

    private final List<KeyMapping> keyMappingList;
    private final NullStrategy nullStrategy;

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
     *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap);
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param keyMappings 键映射规则，格式为 toKey1, fromKey1, toKey2, fromKey2, ...
     * @return 一个新的 MapKeyTranslator 实例
     */
    public static MapKeyTranslator keyMappings(NullStrategy nullStrategy, String... keyMappings) {
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
        return new MapKeyTranslator(keyMappingList, nullStrategy);
    }

    private MapKeyTranslator(List<KeyMapping> keyMappingList, NullStrategy nullStrategy) {
        if (keyMappingList == null) {
            keyMappingList = Collections.emptyList();
        }
        if (nullStrategy == null) {
            nullStrategy = NullStrategy.KEEP_NULL;
        }
        this.keyMappingList = keyMappingList;
        this.nullStrategy = nullStrategy;
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
     *         Map<String, Object> toMap = new HashMap<>();
     *
     *         // from 'name' to 'Username'   Equivalent to toMap.put("Username", fromMap.get("name"))
     *         // from 'age' to 'Age'         Equivalent to toMap.put("Age", fromMap.get("age"))
     *         MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap, toMap);
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param fromMap         源 Map，包含要转换的键值对
     */
    public void translate(Map fromMap, Map toMap) {
        translate(fromMap, () -> toMap);
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
     *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap);
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param fromMap         源 Map，包含要转换的键值对
     * @return 转换后的目标 Map (HashMap)
     */
    public <K, V> Map<K, V> translate(Map fromMap) {
        return translate(fromMap, (Supplier<Map>) null);
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
     *         Map<String, Object> toMap = MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
     *                 "Username", "name",
     *                 "Age", "age"
     *         ).translate(fromMap, LinkedHashMap::new);
     *
     *         // toMap: {Username=John, Age=null}
     * }
     * </pre>
     *
     * @param fromMap         源 Map，包含要转换的键值对
     * @param toMapSupplier   目标 Map 的 Supplier，用于自定义目标 Map 的创建方式。如果为 null，则默认使用 HashMap
     * @return 转换后的目标 Map
     */
    public <K, V> Map<K, V> translate(Map fromMap, Supplier<Map> toMapSupplier) {
        Map toMap = null;
        if (toMapSupplier != null) {
            toMap = toMapSupplier.get();
        }
        if (toMap == null) {
            toMap = new HashMap<>();
        }
        if (fromMap == null) {
            return toMap;
        }
        for (KeyMapping keyMapping : keyMappingList) {
            Object value;
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

        private final Object toKey;
        private final Object fromKey;

        public KeyMapping(Object toKey, Object fromKey) {
            this.toKey = toKey;
            this.fromKey = fromKey;
        }

        public Object toKey() {
            return toKey;
        }

        public Object fromKey() {
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