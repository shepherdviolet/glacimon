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

/**
 * <p>[非线程安全|NOT THREAD SAFE] 流式创建Map/Set/Object/List</p>
 *
 * @author shepherdviolet
 */
public interface StreamingBuildable {

    /**
     * 流式创建一个HashMap
     * @param <K> key type
     * @param <V> value type
     */
    default <K, V> StreamingBuilder.MapBuilder<K, V> buildHashMap() {
        return StreamingBuilder.hashMap();
    }

    /**
     * 流式创建一个LinkedHashMap
     * @param <K> key type
     * @param <V> value type
     */
    default <K, V> StreamingBuilder.MapBuilder<K, V> buildLinkedHashMap() {
        return StreamingBuilder.linkedHashMap();
    }

    /**
     * 流式创建一个ArrayList
     * @param <E> element type
     */
    default <E> StreamingBuilder.ListBuilder<E> buildArrayList() {
        return StreamingBuilder.arrayList();
    }

    /**
     * 流式创建一个LinkedList
     * @param <E> element type
     */
    default <E> StreamingBuilder.ListBuilder<E> buildLinkedList() {
        return StreamingBuilder.linkedList();
    }

    /**
     * 流式创建一个Set
     * @param <E> element type
     */
    default <E> StreamingBuilder.SetBuilder<E> buildHashSet() {
        return StreamingBuilder.hashSet();
    }

    /**
     * 直接创建一个ArrayList
     * @param <E> element type
     */
    default <E> List<E> buildArrayListOf(E... elements) {
        return StreamingBuilder.arrayListOf(elements);
    }

    /**
     * 直接创建一个LinkedList
     * @param <E> element type
     */
    default <E> List<E> buildLinkedListOf(E... elements) {
        return StreamingBuilder.linkedListOf(elements);
    }

    /**
     * 直接创建一个Set
     * @param <E> element type
     */
    default <E> Set<E> buildHashSetOf(E... elements) {
        return StreamingBuilder.hashSetOf(elements);
    }

}
