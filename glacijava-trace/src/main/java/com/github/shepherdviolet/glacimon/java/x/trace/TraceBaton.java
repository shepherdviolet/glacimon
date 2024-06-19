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

package com.github.shepherdviolet.glacimon.java.x.trace;

import com.github.shepherdviolet.glacimon.java.conversion.SimpleKeyValueEncoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 追踪接力信息, 用于从一个线程携带到另一个线程, 或从一个进程携带到另一个进程
 *
 * @author shepherdviolet
 */
public class TraceBaton implements Serializable {

    private static final long serialVersionUID = 7281984723734878656L;
    private static final String KEY_OF_TRACE_ID_KEY = "_trace_id_key_";

    private String traceIdKey;
    private String traceId;
    private Map<String, String> traceData;

    TraceBaton(String traceIdKey, String traceId, Map<String, String> traceData) {
        this.traceIdKey = traceIdKey;
        this.traceId = traceId;
        this.traceData = traceData;
    }

    String getTraceId() {
        return traceId;
    }

    Map<String, String> getTraceData() {
        return traceData;
    }

    /**
     * 转成String格式数据
     */
    @Override
    public String toString() {
        Map<String, String> map;
        if (traceData != null) {
            map = new HashMap<>(traceData);
        } else {
            map = new HashMap<>(4);
        }
        map.put(KEY_OF_TRACE_ID_KEY, traceIdKey);
        map.put(traceIdKey, traceId);
        return SimpleKeyValueEncoder.encode(map);
    }

    /**
     * 将String解析为TraceBaton
     * @param batonData String格式数据
     * @return TraceBaton
     * @throws InvalidBatonException 数据格式错误
     */
    public static TraceBaton fromString(String batonData) throws InvalidBatonException {
        Map<String, String> map;
        try {
            map = SimpleKeyValueEncoder.decode(batonData);
        } catch (SimpleKeyValueEncoder.DecodeException e) {
            throw new InvalidBatonException("Error while parsing TraceBaton from string data, data:" + batonData, e);
        }
        String traceIdKey = map.remove(KEY_OF_TRACE_ID_KEY);
        if (traceIdKey == null) {
            throw new InvalidBatonException("Invalid TraceBaton data, '" + KEY_OF_TRACE_ID_KEY + "' is missing, data:" + batonData);
        }
        String traceId = map.remove(traceIdKey);
        if (traceId == null) {
            throw new InvalidBatonException("Invalid TraceBaton data, '" + traceIdKey + "' is missing, data:" + batonData);
        }
        return new TraceBaton(traceIdKey, traceId, map);
    }

}
