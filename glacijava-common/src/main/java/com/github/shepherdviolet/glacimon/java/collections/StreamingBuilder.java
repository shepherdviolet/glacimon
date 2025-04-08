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
 * <pre>
 *
 * </pre>
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @author shepherdviolet
 */
public class StreamingBuilder<K, V> {

    /**
     * 流式创建一个HashMap
     * @param <K> key type
     * @param <V> value type
     */
    public static <K, V> MapKeySetter<K, V> hashMap() {
        return new MapBuilder<>(new HashMap<>());
    }

    /**
     * 流式创建一个LinkedHashMap
     * @param <K> key type
     * @param <V> value type
     */
    public static <K, V> MapKeySetter<K, V> linkedHashMap() {
        return new MapBuilder<>(new LinkedHashMap<>());
    }

    /**
     * 流式创建一个ArrayList
     * @param <E> element type
     */
    public static <E> ListElementAdder<E> arrayList() {
        return new ListBuilder<>(new ArrayList<>());
    }

    /**
     * 流式创建一个LinkedList
     * @param <E> element type
     */
    public static <E> ListElementAdder<E> linkedList() {
        return new ListBuilder<>(new LinkedList<>());
    }

    /**
     * 流式创建一个Set
     * @param <E> element type
     */
    public static <E> SetElementAdder<E> hashSet() {
        return new SetBuilder<>(new HashSet<>());
    }

    // MapBuilder ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流式创建一个Map
     * @param <K> key type
     * @param <V> value type
     */
    public static class MapBuilder<K, V> implements MapKeySetter<K, V>, MapValueSetter<K, V> {

        private Map<K, V> map;
        private K key;

        public MapBuilder(Map<K, V> map) {
            if (map == null) {
                map = new LinkedHashMap<>();
            }
            this.map  = map;
        }

        @Override
        public MapValueSetter<K, V> key(K key) {
            if (this.map == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one map object, do not call the key method after build!");
            }
            this.key = key;
            return this;
        }

        @Override
        public MapKeySetter<K, V> value(V value) {
            if (this.map == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one map object, do not call the value method after build!");
            }
            map.put(key, value);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> Map<K, V> build() {
            if (this.map == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one map object, do not call the build method twice!");
            }
            Map<K, V> map = (Map<K, V>) this.map;
            this.map = null;
            this.key = null;
            return map;
        }

    }

    /**
     * 流式创建一个Map
     * @param <K> key type
     * @param <V> value type
     */
    public interface MapKeySetter<K, V> {

        /**
         * 设置key
         */
        MapValueSetter<K, V> key(K key);

        /**
         * 创建Map, 这个方法只能被调用一次, 否则会抛出异常
         * @throws IllegalStateException 每个MapBuilder只能创建一个Map对象, 不要调用build方法两次
         */
        <K, V> Map<K, V> build();

    }

    /**
     * 流式创建一个Map
     * @param <K> key type
     * @param <V> value type
     */
    public interface MapValueSetter<K, V> {

        /**
         * 设置value
         */
        MapKeySetter<K, V> value(V value);

    }

    // ListBuilder ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流式创建一个List
     * @param <E> element type
     */
    public static class ListBuilder<E> implements ListElementAdder<E> {

        private List<E> collection;

        public ListBuilder(List<E> collection) {
            if (collection == null) {
                collection = new ArrayList<>();
            }
            this.collection = collection;
        }

        @Override
        public ListElementAdder<E> add(E element) {
            collection.add(element);
            return this;
        }

        @Override
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

    /**
     * 流式创建一个List
     * @param <E> element type
     */
    public interface ListElementAdder<E> {

        /**
         * 添加一个元素
         */
        ListElementAdder<E> add(E element);

        /**
         * 创建List, 这个方法只能被调用一次, 否则会抛出异常
         * @throws IllegalStateException 每个ListBuilder只能创建一个Collection对象, 不要调用build方法两次
         */
        <E> List<E> build();

    }

    // SetBuilder ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 流式创建一个Set
     * @param <E> element type
     */
    public static class SetBuilder<E> implements SetElementAdder<E> {

        private Set<E> collection;

        public SetBuilder(Set<E> collection) {
            if (collection == null) {
                collection = new HashSet<>();
            }
            this.collection = collection;
        }

        @Override
        public SetElementAdder<E> add(E element) {
            collection.add(element);
            return this;
        }

        @Override
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

    /**
     * 流式创建一个Set
     * @param <E> element type
     */
    public interface SetElementAdder<E> {

        /**
         * 添加一个元素
         */
        SetElementAdder<E> add(E element);

        /**
         * 创建Set, 这个方法只能被调用一次, 否则会抛出异常
         * @throws IllegalStateException 每个SetBuilder只能创建一个Collection对象, 不要调用build方法两次
         */
        <E> Set<E> build();

    }

}
