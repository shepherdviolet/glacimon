package com.github.shepherdviolet.glacimon.java.datastruc;

import java.util.HashMap;
import java.util.Map;

/**
 * Key忽略大小写的HashMap
 *
 * @author shepherdviolet
 */
public class IgnoreCaseHashMap<K, V> extends HashMap<K, V> {

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        if (key instanceof String) {
            key = (K) ((String) key).toLowerCase();
        }
        return super.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m != null && !m.isEmpty()) {
            for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                K key = entry.getKey();
                if (key instanceof String) {
                    key = (K) ((String) key).toLowerCase();
                }
                put(key, entry.getValue());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V putIfAbsent(K key, V value) {
        if (key instanceof String) {
            key = (K) ((String) key).toLowerCase();
        }
        return super.putIfAbsent(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (key instanceof String) {
            key = ((String) key).toLowerCase();
        }
        return super.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object key, V defaultValue) {
        if (key instanceof String) {
            key = ((String) key).toLowerCase();
        }
        return super.getOrDefault(key, defaultValue);
    }
}
