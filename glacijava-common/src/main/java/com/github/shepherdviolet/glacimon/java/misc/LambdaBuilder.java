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

package com.github.shepherdviolet.glacimon.java.misc;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * [JAVA8+]用Lambda表达式创建Map/Set/Object/List
 *
 * @since 1.8
 */
public class LambdaBuilder {

    /**
     * 创建HashMap
     *
     * <pre><code>
     *         Map<String, Object> map = LambdaBuilder.hashMap(m -> {
     *             m.put("a", "b");
     *             m.put("c", "d");
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <K, V> Map<K, V> hashMap(Consumer<Map<K, V>> assembler) {
        Map<K, V> map = new HashMap<>(16);
        if (assembler == null) {
            return map;
        }
        assembler.accept(map);
        return map;
    }

    /**
     * 创建LinkedHashMap
     *
     * <pre><code>
     *         Map<String, Object> map = LambdaBuilder.linkedHashMap(m -> {
     *             m.put("a", "b");
     *             m.put("c", "d");
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <K, V> Map<K, V> linkedHashMap(Consumer<Map<K, V>> assembler) {
        Map<K, V> map = new LinkedHashMap<>(16);
        if (assembler == null) {
            return map;
        }
        assembler.accept(map);
        return map;
    }

    /**
     * 创建HashSet
     *
     * <pre><code>
     *         Set<String> set = LambdaBuilder.hashSet(s -> {
     *             s.add("a");
     *             s.add("c");
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <T> Set<T> hashSet(Consumer<Set<T>> assembler) {
        Set<T> set = new HashSet<>(16);
        if (assembler == null) {
            return set;
        }
        assembler.accept(set);
        return set;
    }

    /**
     * 根据源Collection创建HashSet, 实现元素转换
     *
     * <pre><code>
     *         Set<Map<String, Object>> destList = LambdaBuilder.linkedList(srcList, HashMap::new, (src, dest) -> {
     *             dest.put("aaa", src.get("AAA"));
     *             dest.put("bbb", src.get("BBB"));
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <SrcType, DestType> Set<DestType> hashSet(Collection<SrcType> srcCollection,
                                                                Supplier<DestType> destElementSupplier,
                                                                BiConsumer<SrcType, DestType> destElementAssembler) {
        if (destElementSupplier == null) {
            throw new IllegalArgumentException("destElementSupplier must not be null");
        }
        if (destElementAssembler == null) {
            throw new IllegalArgumentException("destElementAssembler must not be null");
        }
        if (srcCollection == null) {
            return new HashSet<>();
        }
        return srcCollection.stream().map(src -> {
            DestType dest = destElementSupplier.get();
            destElementAssembler.accept(src, dest);
            return dest;
        }).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 创建Object
     *
     * <pre><code>
     *         Bean bean = LambdaBuilder.object(() -> {
     *             Bean obj = new Bean();
     *             obj.setName("123");
     *             obj.setId("456");
     *             return obj;
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <T> T object(Supplier<T> assembler) {
        return assembler.get();
    }

    /**
     * 创建ArrayList, 一般情况下用Arrays.asList
     *
     * <pre><code>
     *     Arrays.asList(
     *          item1,
     *          item2
     *     );
     * </code></pre>
     *
     * <pre><code>
     *         List<String> list = LambdaBuilder.arrayList(l -> {
     *            l.add("a");
     *            l.add("b");
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <T> List<T> arrayList(Consumer<List<T>> assembler) {
        List<T> list = new ArrayList<>();
        if (assembler == null) {
            return list;
        }
        assembler.accept(list);
        return list;
    }

    /**
     * 根据源Collection创建ArrayList, 实现元素转换
     *
     * <pre><code>
     *         List<Map<String, Object>> destList = LambdaBuilder.arrayList(srcList, HashMap::new, (src, dest) -> {
     *             dest.put("aaa", src.get("AAA"));
     *             dest.put("bbb", src.get("BBB"));
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <SrcType, DestType> List<DestType> arrayList(Collection<SrcType> srcCollection,
                                                             Supplier<DestType> destElementSupplier,
                                                             BiConsumer<SrcType, DestType> destElementAssembler) {
        if (destElementSupplier == null) {
            throw new IllegalArgumentException("destElementSupplier must not be null");
        }
        if (destElementAssembler == null) {
            throw new IllegalArgumentException("destElementAssembler must not be null");
        }
        if (srcCollection == null) {
            return new ArrayList<>();
        }
        return srcCollection.stream().map(src -> {
            DestType dest = destElementSupplier.get();
            destElementAssembler.accept(src, dest);
            return dest;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 创建LinkedList, 一般情况下用Arrays.asList
     *
     * <pre><code>
     *         List<String> list = LambdaBuilder.linkedList(l -> {
     *            l.add("a");
     *            l.add("b");
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <T> List<T> linkedList(Consumer<List<T>> assembler) {
        List<T> list = new LinkedList<>();
        if (assembler == null) {
            return list;
        }
        assembler.accept(list);
        return list;
    }

    /**
     * 根据源Collection创建LinkedList, 实现元素转换
     *
     * <pre><code>
     *         List<Map<String, Object>> destList = LambdaBuilder.linkedList(srcList, HashMap::new, (src, dest) -> {
     *             dest.put("aaa", src.get("AAA"));
     *             dest.put("bbb", src.get("BBB"));
     *         });
     * </code></pre>
     *
     * @since 1.8
     */
    public static <SrcType, DestType> List<DestType> linkedList(Collection<SrcType> srcCollection,
                                                               Supplier<DestType> destElementSupplier,
                                                               BiConsumer<SrcType, DestType> destElementAssembler) {
        if (destElementSupplier == null) {
            throw new IllegalArgumentException("destElementSupplier must not be null");
        }
        if (destElementAssembler == null) {
            throw new IllegalArgumentException("destElementAssembler must not be null");
        }
        if (srcCollection == null) {
            return new LinkedList<>();
        }
        return srcCollection.stream().map(src -> {
            DestType dest = destElementSupplier.get();
            destElementAssembler.accept(src, dest);
            return dest;
        }).collect(Collectors.toCollection(LinkedList::new));
    }

}
