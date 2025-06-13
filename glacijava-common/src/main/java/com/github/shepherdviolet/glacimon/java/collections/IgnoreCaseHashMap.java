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

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Key忽略大小写的HashMap
 *
 * @author shepherdviolet
 */
@SuppressWarnings("unchecked")
public class IgnoreCaseHashMap<V> implements Map<String, V>, Serializable, Cloneable {

    private static final long serialVersionUID = -4802227422917361191L;

    private final LinkedHashMap<String, V> dataMap;
    private final HashMap<String, String> keyMap;

    private transient volatile Set<String> keySet;
    private transient volatile Collection<V> values;
    private transient volatile Set<Entry<String, V>> entrySet;

    public IgnoreCaseHashMap() {
        this(16);
    }

    public IgnoreCaseHashMap(int initialCapacity) {
        this.dataMap = new LinkedHashMap<String, V>(initialCapacity) {
            private static final long serialVersionUID = 6876543546873546876L;
            @Override
            public boolean containsKey(Object key) {
                return IgnoreCaseHashMap.this.containsKey(key);
            }
        };
        this.keyMap = new HashMap<>(initialCapacity);
    }

    private IgnoreCaseHashMap(IgnoreCaseHashMap<V> other) {
        this.dataMap = (LinkedHashMap<String, V>) other.dataMap.clone();
        this.keyMap = (HashMap<String, String>) other.keyMap.clone();
    }

    @Override
    public int size() {
        return this.dataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.dataMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return (key instanceof String && this.keyMap.containsKey(convertKey((String) key)));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.dataMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            String k = this.keyMap.get(convertKey((String) key));
            if (k != null) {
                return this.dataMap.get(k);
            }
        }
        return null;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        if (key instanceof String) {
            String k = this.keyMap.get(convertKey((String) key));
            if (k != null) {
                return this.dataMap.get(k);
            }
        }
        return defaultValue;
    }

    @Override
    public V put(String key, V value) {
        String oldKey = this.keyMap.put(convertKey(key), key);
        V oldKeyValue = null;
        if (oldKey != null && !oldKey.equals(key)) {
            oldKeyValue = this.dataMap.remove(oldKey);
        }
        V oldValue = this.dataMap.put(key, value);
        return (oldKeyValue != null ? oldKeyValue : oldValue);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        if (map.isEmpty()) {
            return;
        }
        map.forEach(this::put);
    }

    @Override
    public V putIfAbsent(String key, V value) {
        String oldKey = this.keyMap.putIfAbsent(convertKey(key), key);
        if (oldKey != null) {
            V oldKeyValue = this.dataMap.get(oldKey);
            if (oldKeyValue != null) {
                return oldKeyValue;
            } else {
                key = oldKey;
            }
        }
        return this.dataMap.putIfAbsent(key, value);
    }

    @Override
    public V computeIfAbsent(String key, Function<? super String, ? extends V> mappingFunction) {
        String oldKey = this.keyMap.putIfAbsent(convertKey(key), key);
        if (oldKey != null) {
            V oldKeyValue = this.dataMap.get(oldKey);
            if (oldKeyValue != null) {
                return oldKeyValue;
            } else {
                key = oldKey;
            }
        }
        return this.dataMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V remove(Object key) {
        if (key instanceof String) {
            String k = removeKeyMapping((String) key);
            if (k != null) {
                return this.dataMap.remove(k);
            }
        }
        return null;
    }

    @Override
    public void clear() {
        this.keyMap.clear();
        this.dataMap.clear();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keySet = this.keySet;
        if (keySet == null) {
            keySet = new KeySet(this.dataMap.keySet());
            this.keySet = keySet;
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = this.values;
        if (values == null) {
            values = new Values(this.dataMap.values());
            this.values = values;
        }
        return values;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        Set<Entry<String, V>> entrySet = this.entrySet;
        if (entrySet == null) {
            entrySet = new EntrySet(this.dataMap.entrySet());
            this.entrySet = entrySet;
        }
        return entrySet;
    }

    @Override
    public IgnoreCaseHashMap<V> clone() {
        return new IgnoreCaseHashMap<>(this);
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || this.dataMap.equals(other));
    }

    @Override
    public int hashCode() {
        return this.dataMap.hashCode();
    }

    @Override
    public String toString() {
        return this.dataMap.toString();
    }

    protected String convertKey(String key) {
        return key != null ? key.toLowerCase() : null;
    }

    private String removeKeyMapping(String key) {
        return this.keyMap.remove(convertKey(key));
    }

    private class KeySet extends AbstractSet<String> {

        private final Set<String> delegate;

        KeySet(Set<String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public boolean contains(Object key) {
            return this.delegate.contains(key);
        }

        @Override
        public Iterator<String> iterator() {
            return new KeySetIterator();
        }

        @Override
        public boolean remove(Object key) {
            return IgnoreCaseHashMap.this.remove(key) != null;
        }

        @Override
        public void clear() {
            IgnoreCaseHashMap.this.clear();
        }

        @Override
        public Spliterator<String> spliterator() {
            return this.delegate.spliterator();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            this.delegate.forEach(action);
        }

    }

    private class Values extends AbstractCollection<V> {

        private final Collection<V> delegate;

        Values(Collection<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public boolean contains(Object key) {
            return this.delegate.contains(key);
        }

        @Override
        public Iterator<V> iterator() {
            return new ValuesIterator();
        }

        @Override
        public void clear() {
            IgnoreCaseHashMap.this.clear();
        }

        @Override
        public Spliterator<V> spliterator() {
            return this.delegate.spliterator();
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            this.delegate.forEach(action);
        }

    }

    private class EntrySet extends AbstractSet<Entry<String, V>> {

        private final Set<Entry<String, V>> delegate;

        public EntrySet(Set<Entry<String, V>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public boolean contains(Object key) {
            return this.delegate.contains(key);
        }

        @Override
        public Iterator<Entry<String, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object key) {
            if (this.delegate.remove(key)) {
                removeKeyMapping(((Map.Entry<String, V>) key).getKey());
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            this.delegate.clear();
            keyMap.clear();
        }

        @Override
        public Spliterator<Entry<String, V>> spliterator() {
            return this.delegate.spliterator();
        }

        @Override
        public void forEach(Consumer<? super Entry<String, V>> action) {
            this.delegate.forEach(action);
        }

    }

    private abstract class EntryIterator<T> implements Iterator<T> {

        private final Iterator<Entry<String, V>> delegate;

        private Entry<String, V> last;

        public EntryIterator() {
            this.delegate = dataMap.entrySet().iterator();
        }

        protected Entry<String, V> nextEntry() {
            Entry<String, V> entry = this.delegate.next();
            this.last = entry;
            return entry;
        }

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public void remove() {
            this.delegate.remove();
            if (this.last != null) {
                removeKeyMapping(this.last.getKey());
                this.last = null;
            }
        }

    }

    private class KeySetIterator extends EntryIterator<String> {

        @Override
        public String next() {
            return nextEntry().getKey();
        }

    }

    private class ValuesIterator extends EntryIterator<V> {

        @Override
        public V next() {
            return nextEntry().getValue();
        }

    }

    private class EntrySetIterator extends EntryIterator<Entry<String, V>> {

        @Override
        public Entry<String, V> next() {
            return nextEntry();
        }

    }

}
