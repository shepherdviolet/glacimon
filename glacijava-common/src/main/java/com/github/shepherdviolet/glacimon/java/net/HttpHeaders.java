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
 * <p>HTTP Headers.</p>
 * <p>HTTP请求头允许一个name对应多个value. </p>
 * <p>--------------------------------------------------------------------</p>
 * <p>单值'SingleValue'系列方法:</p>
 * <p>当使用'SingleValue'系列方法的时候, 每个name只会读取第一个value.</p>
 * <p>--------------------------------------------------------------------</p>
 * <p>连值'JoinedValue'系列方法:</p>
 * <p>'JoinedValue'系列方法利用'|'分隔符在一个String中储存多个value, 实现了在单值 Map &lt;String, String&gt; 中储存多值.</p>
 * <p>格式为:</p>
 * <p>Key=Value|Key=Value</p>
 * <p>转义符:</p>
 * <p>\|  ->  |</p>
 * <p>\\  ->  \</p>
 *
 * @author shepherdviolet
 */
public class HttpHeaders {

    /**
     * 根据单值 Map &lt;String, String&gt; 创建
     * <p>当使用'SingleValue'系列方法的时候, 每个name只会读取第一个value.</p>
     *
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
     * 根据多值 Map &lt;String, List&lt;String&gt&gt 创建
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
     * 根据 List &lt;KeyValue&lt;String, String&gt&gt 创建
     * @param list KeyValue List
     */
    public static HttpHeaders ofKeyValueList(List<KeyValue<String, String>> list) {
        HttpHeaders headers = new HttpHeaders();
        if (list == null) {
            return headers;
        }
        for (KeyValue<String, String> entry : list) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * [特殊] 根据连值 Map &lt;String, String&gt; 创建
     * <p>与ofSingleValueMap不同的是, 这个方法支持读取连值(JoinedValue), 连值是指利用'|'分隔符在一个String中储存多个value</p>
     * <p>格式为:</p>
     * <p>Key=Value|Key=Value</p>
     * <p>转义符:</p>
     * <p>\|  ->  |</p>
     * <p>\\  ->  \</p>
     *
     * @param joinedValueMap 连值Map
     */
    public static HttpHeaders ofJoinedValueMap(Map<String, String> joinedValueMap) throws IllegalEscapeException {
        HttpHeaders headers = new HttpHeaders();
        if (joinedValueMap == null) {
            return headers;
        }
        for (Map.Entry<String, String> entry : joinedValueMap.entrySet()) {
            headers.add(entry.getKey(), joinedValueToMultiValue(entry.getValue()));
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
     * @param value 值
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
     * @param values 值
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
     * @param values 值
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
     * 添加请求头, 支持连值(JoinedValue).
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * <p>与add不同的是, 这个方法支持读取连值(JoinedValue), 连值是指利用'|'分隔符在一个String中储存多个value</p>
     * <p>格式为:</p>
     * <p>Key=Value|Key=Value</p>
     * <p>转义符:</p>
     * <p>\|  ->  |</p>
     * <p>\\  ->  \</p>
     *
     * @param name name
     * @param joinedValue 连值
     * @exception IllegalEscapeException 出现非法的转义符, 转义符只支持两个: \| 和 \\
     */
    public void addJoinedValue(String name, String joinedValue) throws IllegalEscapeException {
        add(name, joinedValueToMultiValue(joinedValue));
    }

    /**
     * 设置请求头. (覆盖)
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param value 值
     */
    public void set(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        headers.put(name, StreamingBuilder.arrayList().add(value).build());
    }

    /**
     * 设置请求头. (覆盖)
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param values 值
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
     * 设置请求头. (覆盖)
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * @param name name
     * @param values 值
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
     * 设置请求头 (覆盖), 支持连值(JoinedValue).
     *
     * 注意:
     * 使用add方法添加请求头时, 指定name的原值保留, 新值与原值并存 (即使值相同).
     * 使用set方法设置请求头时, 指定name的原值被覆盖, 只存在新值.
     *
     * <p>与set不同的是, 这个方法支持读取连值(JoinedValue), 连值是指利用'|'分隔符在一个String中储存多个value</p>
     * <p>格式为:</p>
     * <p>Key=Value|Key=Value</p>
     * <p>转义符:</p>
     * <p>\|  ->  |</p>
     * <p>\\  ->  \</p>
     *
     * @param name name
     * @param joinedValue 连值
     * @exception IllegalEscapeException 出现非法的转义符, 转义符只支持两个: \| 和 \\
     */
    public void setJoinedValue(String name, String joinedValue) throws IllegalEscapeException {
        set(name, joinedValueToMultiValue(joinedValue));
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
    public String getSingleValue(String name) {
        if (name == null) {
            return null;
        }
        List<String> multiValue = headers.get(name);
        if (multiValue == null || multiValue.isEmpty()) {
            return null;
        }
        return multiValue.get(0);
    }

    /**
     * 获取指定name的值, 返回多个值
     * @param name name
     * @return 不为null, 但元素数可能为0
     */
    public List<String> getMultiValue(String name) {
        if (name == null) {
            return new ArrayList<>(0);
        }
        List<String> multiValue = headers.get(name);
        if (multiValue == null) {
            return new ArrayList<>(0);
        }
        return multiValue;
    }

    /**
     * 获取指定name的连值, 返回连值(JoinedValue)
     *
     * <p>与getSingleValue和getMultiValue不同的是, 这个方法返回连值(JoinedValue), 连值是指利用'|'分隔符在一个String中储存多个value</p>
     * <p>格式为:</p>
     * <p>Key=Value|Key=Value</p>
     * <p>转义符:</p>
     * <p>\|  ->  |</p>
     * <p>\\  ->  \</p>
     *
     * @param name name
     * @return 连值, 可能为null
     */
    public String getJoinedValue(String name) {
        if (name == null) {
            return null;
        }
        List<String> multiValue = headers.get(name);
        return multiValueToJoinedValue(multiValue);
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            names.add(entry.getKey());
        }
        return names;
    }

    /**
     * 转单值Map &lt;String, String&gt;, 注意HTTP请求头允许一个name对应多个value, 本方法返回的Map每个name只会有第一个value.
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
     * 转多值Map &lt;String, List&lt;String&gt&gt, 注意HTTP请求头允许一个name对应多个value, 本方法返回的Map每个name保留多个value.
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
     * 转键值对List &lt;KeyValue&lt;String, String&gt&gt, 注意HTTP请求头允许一个name对应多个value, 本方法返回的List每个name保留多个value.
     */
    public List<KeyValue<String, String>> toKeyValueList() {
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
     * 转连值Map &lt;String, String&gt;, 注意HTTP请求头允许一个name对应多个value, 本方法返回的Map每个name保留多个value.
     *
     * <p>与toSingleValueMap和toMultiValueMap不同的是, 这个方法返回连值(JoinedValue), 连值是指利用'|'分隔符在一个String中储存多个value</p>
     * <p>格式为:</p>
     * <p>Key=Value|Key=Value</p>
     * <p>转义符:</p>
     * <p>\|  ->  |</p>
     * <p>\\  ->  \</p>
     */
    public Map<String, String> toJoinedValueMap() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String joinedValue = multiValueToJoinedValue(entry.getValue());
            if (joinedValue != null) {
                result.put(entry.getKey(), joinedValue);
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

    // private methods /////////////////////////////////////////////////////////////////////////////////////////

    private static List<String> joinedValueToMultiValue(String joinedValue) throws IllegalEscapeException {
        if (joinedValue == null) {
            return null;
        }
        List<String> multiValue = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        boolean isEscaped = false;
        for (char c : joinedValue.toCharArray()) {
            if (isEscaped) {
                if (c == '|' || c == '\\') {
                    stringBuilder.append(c);
                } else {
                    throw new IllegalEscapeException("Illegal escape characters '\\" + c +
                            "', HttpHeaders' JoinedValue only supports two escape characters '\\\\' and '\\|', joined value: " + joinedValue);
                }
                isEscaped = false;
            } else if (c == '\\') {
                isEscaped = true;
            } else if (c == '|') {
                multiValue.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(c);
            }
        }
        if (isEscaped) {
            throw new IllegalEscapeException("Illegal escape characters '\\' (at the end of the String), " +
                    "HttpHeaders' JoinedValue only supports two escape characters '\\\\' and '\\|', joined value: " + joinedValue);
        }
        multiValue.add(stringBuilder.toString());
        return multiValue;
    }

    private static String multiValueToJoinedValue(List<String> multiValue) {
        if (multiValue == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        for (String value : multiValue) {
            if (value == null) {
                continue;
            }
            if (index++ > 0) {
                stringBuilder.append("|");
            }
            for (char c : value.toCharArray()) {
                if (c == '|' || c == '\\') {
                    stringBuilder.append('\\');
                }
                stringBuilder.append(c);
            }
        }
        if (index == 0) {
            return null;
        }
        return stringBuilder.toString();
    }

    public static class IllegalEscapeException extends Exception {

        private static final long serialVersionUID = -7273986532616199613L;

        public IllegalEscapeException(String message) {
            super(message);
        }

        public IllegalEscapeException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
