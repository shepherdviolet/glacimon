/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.x.util.trace;

import sviolet.thistle.util.conversion.SimpleKeyValueEncoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static sviolet.thistle.x.util.trace.Trace.TRACE_ID_KEY;

/**
 * 追踪接力信息, 用于从一个线程携带到另一个线程, 或从一个进程携带到另一个进程
 *
 * @author S.Violet
 */
public class TraceBaton implements Serializable {

    private static final long serialVersionUID = 7281984723734878656L;

    private String traceId;
    private Map<String, String> traceData;

    TraceBaton(String traceId, Map<String, String> traceData) {
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
        map.put(TRACE_ID_KEY, traceId);
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
        String traceId = map.remove(TRACE_ID_KEY);
        return new TraceBaton(traceId, map);
    }

}
