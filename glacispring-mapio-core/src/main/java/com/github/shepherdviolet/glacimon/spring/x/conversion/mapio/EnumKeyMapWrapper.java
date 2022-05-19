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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio;

import java.util.*;

/**
 * <p>包装一个Map, 使之能够使用枚举作为KEY存取值 (实际上就是把枚举值转String处理).</p>
 * <p>注意! 操作本包装类时, 原始Map中的数据会同时修改, 因为实际上包装类操作的就是原始Map. </p>
 * <p>注意! 如果枚举字段上用@FieldName注解覆盖了字段名, 则必须用String作为KEY存取值, 本包装类不会根据@FieldName注解映射字段名. </p>
 *
 * @author shepherdviolet
 */
public class EnumKeyMapWrapper<V> implements Map<String, V> {

    // static /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 包装一个Map, 使之能够使用枚举作为KEY存取值
     */
    public static <V> EnumKeyMapWrapper<V> wrap(Map<String, V> map) {
        return new EnumKeyMapWrapper<>(map);
    }

    /**
     * 解除包装, 返回原始Map (如果送入的map是其他类型的话, 保持原样返回)
     */
    public static <V> Map<String, V> unwrap(Map<String, V> map) {
        if (map instanceof EnumKeyMapWrapper) {
            return ((EnumKeyMapWrapper<V>) map).unwrap();
        }
        return map;
    }

    // exclusive //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 解除包装, 返回原始Map
     */
    public Map<String, V> unwrap() {
        return provider;
    }

    /**
     * 判断KEY是否存在 (枚举KEY)
     */
    public boolean containsKey(Enum<?> key) {
        if (key == null) {
            return containsKey((Object) null);
        }
        return containsKey(key.toString());
    }

    /**
     * 取值 (枚举KEY)
     */
    public V get(Enum<?> key) {
        if (key == null) {
            return get((Object) null);
        }
        return get(key.toString());
    }

    /**
     * 存值 (枚举KEY)
     */
    public V put(Enum<?> key, V value) {
        if (key == null) {
            return put((String) null, value);
        }
        return put(key.toString(), value);
    }

    /**
     * 移除值 (枚举KEY)
     */
    public V remove(Enum<?> key) {
        if (key == null) {
            return remove((Object) null);
        }
        return remove(key.toString());
    }

    // wrapper ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Map<String, V> provider;

    private EnumKeyMapWrapper(Map<String, V> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        this.provider = map;
    }

    @Override
    public int size() {
        return provider.size();
    }

    @Override
    public boolean isEmpty() {
        return provider.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return provider.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return provider.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return provider.get(key);
    }

    @Override
    public V put(String key, V value) {
        return provider.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return provider.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        provider.putAll(m);
    }

    @Override
    public void clear() {
        provider.clear();
    }

    @Override
    public Set<String> keySet() {
        return provider.keySet();
    }

    @Override
    public Collection<V> values() {
        return provider.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return provider.entrySet();
    }

}
