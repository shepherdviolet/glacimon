/*
 * Copyright (C) 2015-2018 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 并发工具<p/>
 *
 * @author S.Violet
 */
public class ConcurrentUtils {

    /**
     * 复制一个Collection的快照, 防止并发处理时, 发生异常
     * 注意: 需要加锁, 否则会报ConcurrentModificationException异常(ConcurrentHashMap等线程安全的类无需再加锁)
     *
     * @param source 原数据
     * @param <T> 数据类型
     * @return 快照
     */
    public static <T> List<T> getSnapShot(Collection<T> source) {
        if (source == null){
            return null;
        }
        List<T> snap = new ArrayList<>(source.size());
        for (T item : source) {
            snap.add(item);
        }
        return snap;
    }

    /**
     * 复制一个Map的快照, 防止并发处理时, 发生异常
     * 注意: 需要加锁, 否则会报ConcurrentModificationException异常(ConcurrentHashMap等线程安全的类无需再加锁)
     *
     * @param source 原数据
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 快照
     */
    public static <K, V> Map<K, V> getSnapShot(Map<K, V> source){
        if (source == null){
            return null;
        }
        Map<K, V> snap = new HashMap<>(source.size());
        for (Map.Entry<K, V> entry : source.entrySet()){
            snap.put(entry.getKey(), entry.getValue());
        }
        return snap;
    }

}
