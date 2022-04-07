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

package sviolet.slate.common.util.common;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * [JAVA8+]用Lambda表达式创建Map/Set/Object/List
 *
 * @since 1.8
 */
public class LambdaBuilder {

    /**
     * 创建HashMap
     *
     * <pre>
     *         Map<String, Object> map = LambdaBuilder.hashMap(i -> {
     *             i.put("a", "b");
     *             i.put("c", "d");
     *         });
     * </pre>
     *
     * @since 1.8
     */
    public static <K, V> Map<K, V> hashMap(Consumer<Map<K, V>> supplier) {
        Map<K, V> map = new HashMap<>(16);
        if (supplier == null) {
            return map;
        }
        supplier.accept(map);
        return map;
    }

    /**
     * 创建LinkedHashMap
     *
     * <pre>
     *         Map<String, Object> map = LambdaBuilder.linkedHashMap(i -> {
     *             i.put("a", "b");
     *             i.put("c", "d");
     *         });
     * </pre>
     *
     * @since 1.8
     */
    public static <K, V> Map<K, V> linkedHashMap(Consumer<Map<K, V>> supplier) {
        Map<K, V> map = new LinkedHashMap<>(16);
        if (supplier == null) {
            return map;
        }
        supplier.accept(map);
        return map;
    }

    /**
     * 创建HashSet
     *
     * <pre>
     *         Set<String> set = LambdaBuilder.hashSet(i -> {
     *             i.add("a");
     *             i.add("c");
     *         });
     * </pre>
     *
     * @since 1.8
     */
    public static <T> Set<T> hashSet(Consumer<Set<T>> supplier) {
        Set<T> set = new HashSet<>(16);
        if (supplier == null) {
            return set;
        }
        supplier.accept(set);
        return set;
    }

    /**
     * 创建Object
     *
     * <pre>
     *         Bean bean = LambdaBuilder.object(() -> {
     *             Bean obj = new Bean();
     *             obj.setName("123");
     *             obj.setId("456");
     *             return obj;
     *         });
     * </pre>
     *
     * @since 1.8
     */
    public static <T> T object(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 创建ArrayList, 一般情况下用Arrays.asList
     *
     * <pre>
     *     Arrays.asList(
     *          item1,
     *          item2
     *     );
     * </pre>
     *
     * <pre>
     *         List<String> list = LambdaBuilder.arrayList(i -> {
     *            i.add("a");
     *            i.add("b");
     *         });
     * </pre>
     *
     * @since 1.8
     */
    public static <T> List<T> arrayList(Consumer<List<T>> supplier) {
        List<T> list = new ArrayList<>();
        if (supplier == null) {
            return list;
        }
        supplier.accept(list);
        return list;
    }

    /**
     * 创建LinkedList, 一般情况下用Arrays.asList
     *
     * <pre>
     *         List<String> list = LambdaBuilder.linkedList(i -> {
     *            i.add("a");
     *            i.add("b");
     *         });
     * </pre>
     *
     * @since 1.8
     */
    public static <T> List<T> linkedList(Consumer<List<T>> supplier) {
        List<T> list = new LinkedList<>();
        if (supplier == null) {
            return list;
        }
        supplier.accept(list);
        return list;
    }
}
