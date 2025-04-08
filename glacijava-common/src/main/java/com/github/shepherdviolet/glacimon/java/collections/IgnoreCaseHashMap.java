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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Key忽略大小写的HashMap
 *
 * @author shepherdviolet
 */
@SuppressWarnings("unchecked")
public class IgnoreCaseHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = -4802227422917361191L;

    private final KeyStyle keyStyle;

    /**
     * @param keyStyle 实际储存的key的格式
     */
    public IgnoreCaseHashMap(KeyStyle keyStyle) {
        if (keyStyle == null) {
            throw new NullPointerException("keyStyle is null");
        }
        this.keyStyle = keyStyle;
    }

    /**
     * @param keyStyle 实际储存的key的格式
     */
    public IgnoreCaseHashMap(KeyStyle keyStyle, int initialCapacity) {
        super(initialCapacity);
        if (keyStyle == null) {
            throw new NullPointerException("keyStyle is null");
        }
        this.keyStyle = keyStyle;
    }

    /**
     * @param keyStyle 实际储存的key的格式
     */
    public IgnoreCaseHashMap(KeyStyle keyStyle, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        if (keyStyle == null) {
            throw new NullPointerException("keyStyle is null");
        }
        this.keyStyle = keyStyle;
    }

    @Override
    public V put(K key, V value) {
        return super.put((K) formatKey(key), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m != null && !m.isEmpty()) {
            for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                put((K) formatKey(entry.getKey()), entry.getValue());
            }
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return super.putIfAbsent((K) formatKey(key), value);
    }

    @Override
    public V get(Object key) {
        return super.get(formatKey(key));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(formatKey(key), defaultValue);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(formatKey(key));
    }

    @Override
    public V remove(Object key) {
        return super.remove(formatKey(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(formatKey(key), value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace((K) formatKey(key), oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return super.replace((K) formatKey(key), value);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.compute((K) formatKey(key), remappingFunction);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return super.computeIfAbsent((K) formatKey(key), mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent((K) formatKey(key), remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge((K) formatKey(key), value, remappingFunction);
    }

    private Object formatKey(Object key) {
        if (key == null || !key.getClass().equals(String.class)) {
            return key;
        }
        switch (keyStyle) {
            case LOWER_CASE:
                return ((String) key).toLowerCase();
            case UPPER_CASE:
                return ((String) key).toUpperCase();
            case CAMEL:
                return toCamel((String) key);
            default:
                return key;
        }
    }

    private String toCamel(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean isNewPart = true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '-' || c == '_' || c == ' ') {
                result.append(c);
                isNewPart = true;
            } else {
                if (isNewPart) {
                    result.append(Character.toUpperCase(c));
                    isNewPart = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 实际储存的key的格式
     */
    public enum KeyStyle {

        /**
         * aaa-bbb_ccc ddd
         */
        LOWER_CASE,

        /**
         * AAA-BBB_CCC DDD
         */
        UPPER_CASE,

        /**
         * Aaa-Bbb_Ccc Ddd
         */
        CAMEL

    }

}
