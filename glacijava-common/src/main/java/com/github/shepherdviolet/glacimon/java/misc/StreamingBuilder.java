/*
 * Copyright (C) 2022-2023 S.Violet
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>[非线程安全|NOT THREAD SAFE] 流式创建Map/Set/Object/List</p>
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
     * 流式创建一个Map
     * @param <K> key type
     * @param <V> value type
     */
    private static class MapBuilder<K, V> implements MapKeySetter<K, V>, MapValueSetter<K, V> {

        private Map<K, V> map;
        private K key;

        private MapBuilder(Map<K, V> map) {
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
        public Map<K, V> build() {
            if (this.map == null) {
                throw new IllegalStateException("Each StreamingBuilder can only create one map object, do not call the build method twice!");
            }
            Map<K, V> map = this.map;
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

}
