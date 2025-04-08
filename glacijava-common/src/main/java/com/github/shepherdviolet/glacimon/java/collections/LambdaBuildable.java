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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * [JAVA8+]用Lambda表达式创建Map/Set/Object/List
 *
 * @since 1.8
 */
public interface LambdaBuildable {

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
    default <K, V> Map<K, V> buildHashMap(Consumer<Map<K, V>> supplier) {
        return LambdaBuilder.hashMap(supplier);
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
    default <K, V> Map<K, V> buildLinkedHashMap(Consumer<Map<K, V>> supplier) {
        return LambdaBuilder.linkedHashMap(supplier);
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
    default <T> Set<T> buildHashSet(Consumer<Set<T>> supplier) {
        return LambdaBuilder.hashSet(supplier);
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
    default <SrcType, DestType> Set<DestType> hashSet(Collection<SrcType> srcCollection,
                                                            Supplier<DestType> destElementSupplier,
                                                            BiConsumer<SrcType, DestType> destElementAssembler) {
        return LambdaBuilder.hashSet(srcCollection, destElementSupplier, destElementAssembler);
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
    default <T> T buildObject(Supplier<T> supplier) {
        return LambdaBuilder.object(supplier);
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
    default <T> List<T> buildArrayList(Consumer<List<T>> supplier) {
        return LambdaBuilder.arrayList(supplier);
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
    default <SrcType, DestType> List<DestType> arrayList(Collection<SrcType> srcCollection,
                                                               Supplier<DestType> destElementSupplier,
                                                               BiConsumer<SrcType, DestType> destElementAssembler) {
        return LambdaBuilder.arrayList(srcCollection, destElementSupplier, destElementAssembler);
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
    default <T> List<T> buildLinkedList(Consumer<List<T>> supplier) {
        return LambdaBuilder.linkedList(supplier);
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
    default <SrcType, DestType> List<DestType> linkedList(Collection<SrcType> srcCollection,
                                                                Supplier<DestType> destElementSupplier,
                                                                BiConsumer<SrcType, DestType> destElementAssembler) {
        return LambdaBuilder.linkedList(srcCollection, destElementSupplier, destElementAssembler);
    }

}
