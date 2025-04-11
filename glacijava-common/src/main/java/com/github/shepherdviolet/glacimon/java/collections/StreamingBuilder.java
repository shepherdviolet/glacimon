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
public class StreamingBuilder {

    /**
     * 流式创建一个HashMap
     * @param <K> key type
     * @param <V> value type
     */
    public static <K, V> MapBuilder<K, V> hashMap() {
        return new MapBuilder<>(new HashMap<>());
    }

    /**
     * 流式创建一个LinkedHashMap
     * @param <K> key type
     * @param <V> value type
     */
    public static <K, V> MapBuilder<K, V> linkedHashMap() {
        return new MapBuilder<>(new LinkedHashMap<>());
    }

    /**
     * 流式创建一个ArrayList
     * @param <E> element type
     */
    public static <E> ListBuilder<E> arrayList() {
        return new ListBuilder<>(new ArrayList<>());
    }

    /**
     * 流式创建一个LinkedList
     * @param <E> element type
     */
    public static <E> ListBuilder<E> linkedList() {
        return new ListBuilder<>(new LinkedList<>());
    }

    /**
     * 流式创建一个Set
     * @param <E> element type
     */
    public static <E> SetBuilder<E> hashSet() {
        return new SetBuilder<>(new HashSet<>());
    }

    /**
     * 直接创建一个ArrayList
     * @param <E> element type
     */
    public static <E> List<E> arrayListOf(E... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * 直接创建一个LinkedList
     * @param <E> element type
     */
    public static <E> List<E> linkedListOf(E... elements) {
        return new LinkedList<>(Arrays.asList(elements));
    }

    /**
     * 直接创建一个Set
     * @param <E> element type
     */
    public static <E> Set<E> hashSetOf(E... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    // MapBuilder ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流式创建一个Map
     * @param <K> key type
     * @param <V> value type
     */
    public static class MapBuilder<K, V> {

        private Map<K, V> map;

        private MapBuilder(Map<K, V> map) {
            if (map == null) {
                map = new LinkedHashMap<>();
            }
            this.map  = map;
        }

        public MapBuilder<K, V> put(K key, V value) {
            if (this.map == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one map object, do not call the value method after build!");
            }
            map.put(key, value);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <K, V> Map<K, V> build() {
            if (this.map == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one map object, do not call the build method twice!");
            }
            Map<K, V> map = (Map<K, V>) this.map;
            this.map = null;
            return map;
        }

    }

    // ListBuilder ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流式创建一个List
     * @param <E> element type
     */
    public static class ListBuilder<E> {

        private List<E> collection;

        private ListBuilder(List<E> collection) {
            if (collection == null) {
                collection = new ArrayList<>();
            }
            this.collection = collection;
        }

        public ListBuilder<E> add(E element) {
            collection.add(element);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <E> List<E> build() {
            if (this.collection == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one collection object, do not call the build method twice!");
            }
            List<E> collection = (List<E>) this.collection;
            this.collection = null;
            return collection;
        }

    }

    // SetBuilder ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流式创建一个Set
     * @param <E> element type
     */
    public static class SetBuilder<E> {

        private Set<E> collection;

        private SetBuilder(Set<E> collection) {
            if (collection == null) {
                collection = new HashSet<>();
            }
            this.collection = collection;
        }

        public SetBuilder<E> add(E element) {
            collection.add(element);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <E> Set<E> build() {
            if (this.collection == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one collection object, do not call the build method twice!");
            }
            Set<E> collection = (Set<E>) this.collection;
            this.collection = null;
            return collection;
        }

    }

}
