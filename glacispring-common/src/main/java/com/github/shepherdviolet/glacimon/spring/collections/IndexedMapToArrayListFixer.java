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

package com.github.shepherdviolet.glacimon.spring.collections;

import java.util.*;

/**
 * [特殊工具:请了解清楚功能后再使用]
 *
 * <pre>
 * 在Springboot中, 使用ConfigurationProperties接收yaml配置, apple这个'list'有可能会被解析为一个key为0 1 2的LinkedHashMap.
 *
 * foo:
 *   bar:
 *     apple:
 *       - a1
 *       - a2
 *       - a3
 *
 * 本工具用于将嵌套集合中的"key为0 1 2的LinkedHashMap"转为ArrayList
 * </pre>
 *
 * @author shepherdviolet
 */
@SuppressWarnings("unchecked")
public class IndexedMapToArrayListFixer {

    /**
     * 递归处理 Map 中的嵌套结构，将键为连续索引的 LinkedHashMap 转换为 ArrayList
     * @param map 输入的 Map
     * @return 处理后的 Map
     */
    public static Map<String, Object> processMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        // 简化初始容量计算，避免接近满时扩容
        Map<String, Object> resultMap = new HashMap<>(map.size() * 2);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            resultMap.put(key, processValue(value));
        }
        return resultMap;
    }

    /**
     * 递归处理 Collection 中的元素
     * @param collection 输入的 Collection
     * @return 处理后的 List
     */
    public static List<Object> processList(Collection<Object> collection) {
        if (collection == null) {
            return null;
        }
        List<Object> processedList = new ArrayList<>(collection.size());
        for (Object item : collection) {
            processedList.add(processValue(item));
        }
        return processedList;
    }

    /**
     * 处理值，如果是 Map 或 Collection 则递归处理
     * @param value 输入的值
     * @return 处理后的值
     */
    private static Object processValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> mapValue = (Map<String, Object>) value;
            if (isIndexedMap(mapValue)) {
                List<Object> list = convertIndexedMapToList(mapValue);
                return processList(list);
            } else {
                return processMap(mapValue);
            }
        } else if (value instanceof Collection) {
            Collection<Object> collectionValue = (Collection<Object>) value;
            return processList(collectionValue);
        }
        return value;
    }

    /**
     * 检查 Map 的键是否为连续索引
     * @param map 输入的 Map
     * @return 如果是连续索引返回 true，否则返回 false
     */
    private static boolean isIndexedMap(Map<String, Object> map) {
        int size = map.size();
        for (int i = 0; i < size; i++) {
            if (!map.containsKey(String.valueOf(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将键为连续索引的 Map 转换为 List
     * @param map 输入的 Map
     * @return 转换后的 List
     */
    private static List<Object> convertIndexedMapToList(Map<String, Object> map) {
        List<Object> list = new ArrayList<>(map.size());
        for (int i = 0; i < map.size(); i++) {
            list.add(map.get(String.valueOf(i)));
        }
        return list;
    }

}    