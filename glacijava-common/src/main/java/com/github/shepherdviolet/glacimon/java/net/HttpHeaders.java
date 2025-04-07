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

package com.github.shepherdviolet.glacimon.java.net;

import com.github.shepherdviolet.glacimon.java.common.entity.KeyValue;
import com.github.shepherdviolet.glacimon.java.datastruc.IgnoreCaseHashMap;
import com.github.shepherdviolet.glacimon.java.misc.StreamingBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * HTTP Headers.
 *
 * 注意HTTP请求头允许一个name对应多个value, 当使用'SingleValue'系列方法的时候, 只会储存/读取第一个value.
 *
 * @author shepherdviolet
 */
public class HttpHeaders {

    /**
     * 根据单值Map创建
     * @param singleValueMap 单值Map
     */
    public static HttpHeaders ofSingleValueMap(Map<String, String> singleValueMap) {
        HttpHeaders headers = new HttpHeaders();
        if (singleValueMap == null) {
            return headers;
        }
        for (Map.Entry<String, String> entry : singleValueMap.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * 根据多值Map创建
     * @param multiValueMap 多值Map
     */
    public static HttpHeaders ofMultiValueMap(Map<String, List<String>> multiValueMap) {
        HttpHeaders headers = new HttpHeaders();
        if (multiValueMap == null) {
            return headers;
        }
        for (Map.Entry<String, List<String>> entry : multiValueMap.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * 根据List创建
     * @param list List
     */
    public static HttpHeaders ofList(List<KeyValue<String, String>> list) {
        HttpHeaders headers = new HttpHeaders();
        if (list == null) {
            return headers;
        }
        for (KeyValue<String, String> entry : list) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    private final IgnoreCaseHashMap<String, List<String>> headers = new IgnoreCaseHashMap<>(IgnoreCaseHashMap.KeyStyle.CAMEL);

    /**
     * 添加请求头.
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param value 新值
     */
    public void add(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        headers.computeIfAbsent(name, n -> new ArrayList<>()).add(value);
    }

    /**
     * 添加请求头.
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param values 新值
     */
    public void add(String name, String... values) {
        if (name == null || values == null || values.length == 0) {
            return;
        }
        List<String> list = headers.computeIfAbsent(name, n -> new ArrayList<>());
        for (String value : values) {
            if (value != null) {
                list.add(value);
            }
        }
    }

    /**
     * 添加请求头.
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param values 新值
     */
    public void add(String name, List<String> values) {
        if (name == null || values == null || values.isEmpty()) {
            return;
        }
        List<String> list = headers.computeIfAbsent(name, n -> new ArrayList<>());
        for (String value : values) {
            if (value != null) {
                list.add(value);
            }
        }
    }

    /**
     * 设置请求头.
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param value 新值
     */
    public void set(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        headers.put(name, StreamingBuilder.arrayList().add(value).build());
    }

    /**
     * 设置请求头.
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param values 新值
     */
    public void set(String name, String... values) {
        if (name == null || values == null || values.length == 0) {
            return;
        }
        List<String> list = new ArrayList<>();
        for (String value : values) {
            if (value != null) {
                list.add(value);
            }
        }
        headers.put(name, list);
    }

    /**
     * 设置请求头.
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param values 新值
     */
    public void set(String name, List<String> values) {
        if (name == null || values == null || values.isEmpty()) {
            return;
        }
        List<String> list = new ArrayList<>();
        for (String value : values) {
            if (value != null) {
                list.add(value);
            }
        }
        headers.put(name, list);
    }

    /**
     * 删除指定name的请求头
     * @param name name
     */
    public void remove(String name) {
        if (name == null) {
            return;
        }
        headers.remove(name);
    }

    /**
     * 删除所有请求头
     */
    public void removeAll() {
        headers.clear();
    }

    /**
     * 获取指定name的值, 若指定name存在多个值, 只返回第一个
     * @param name name
     * @return 可能为null
     */
    public String getValue(String name) {
        if (name == null) {
            return null;
        }
        List<String> list = headers.get(name);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取指定name的值, 返回多个值
     * @param name name
     * @return 不为null, 但元素数可能为0
     */
    public List<String> getValues(String name) {
        if (name == null) {
            return null;
        }
        List<String> list = headers.get(name);
        if (list == null) {
            return new ArrayList<>(0);
        }
        return list;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            names.add(entry.getKey());
        }
        return names;
    }

    /**
     * 转单值Map, 注意HTTP请求头允许一个name对应多个value, 本方法返回的Map只会读取第一个value.
     */
    public Map<String, String> toSingleValueMap() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return result;
    }

    /**
     * 转多值Map, 注意HTTP请求头允许一个name对应多个value, 本方法返回的Map保留多个值
     */
    public Map<String, List<String>> toMultiValueMap() {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            } else {
                result.put(entry.getKey(), new ArrayList<>(0));
            }
        }
        return result;
    }

    /**
     * 转List, 注意HTTP请求头允许一个name对应多个value, 本方法返回的List保留多个值
     */
    public List<KeyValue<String, String>> toList() {
        List<KeyValue<String, String>> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getValue() != null) {
                for (String value : entry.getValue()) {
                    if (value != null) {
                        result.add(new KeyValue<>(entry.getKey(), value));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 遍历所有的请求头.
     *
     * 示例:
     * okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
     * httpHeaders.traverse(builder::addHeader);
     */
    public void traverse(BiConsumer<String, String> consumer) {
        if (consumer == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                for (String value : entry.getValue()) {
                    if (value != null) {
                        consumer.accept(entry.getKey(), value);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return headers.toString();
    }

}
