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

package com.github.shepherdviolet.glacimon.java.spi.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CloseableConcurrentHashMap
 *
 * @author shepherdviolet
 */
class CloseableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> implements Closeable {

    CloseableConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    private volatile boolean closed = false;

    @Override
    public void close() throws IOException {
        //mark as closed
        closed = true;
        //close all
        for (Map.Entry<?, ?> entry : entrySet()) {
            closeQuietly(entry.getValue());
        }
    }

    @Override
    public V put(K key, V value) {
        //close new
        if (closed) {
            closeQuietly(value);
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        //close new
        if (closed) {
            for (Map.Entry<?, ?> entry : entrySet()) {
                closeQuietly(entry.getValue());
            }
        }
        super.putAll(m);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        //close new
        if (closed) {
            closeQuietly(value);
        }
        return super.putIfAbsent(key, value);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private static void closeQuietly(Object obj) {
        if (obj instanceof Uninstallable) {
            CommonUtils.closeQuietly((Uninstallable) obj);
        } else if (obj instanceof Closeable) {
            CommonUtils.closeQuietly((Closeable) obj);
        }
    }

}
