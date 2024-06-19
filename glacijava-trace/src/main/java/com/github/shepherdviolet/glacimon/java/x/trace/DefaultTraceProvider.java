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

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.PropertyInject;
import com.github.shepherdviolet.glacimon.java.misc.UuidUtils;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.InitializableImplementation;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>全局追踪默认实现</p>
 *
 * <p>
 *     1.追踪号和其他追踪信息保存在ThreadLocal中. <br>
 *     2.如果应用依赖SLF4J, 追踪号还会存入MDC, KEY为"_trace_id_"(可通过SPI机制修改), 可以打印在日志中. <br>
 * </p>
 *
 * @author shepherdviolet
 */
public class DefaultTraceProvider implements TraceProvider, InitializableImplementation {

    private TraceIdProvider traceIdProvider;
    private ThreadLocal<Map<String, String>> traceData = new ThreadLocal<>();

    /**
     * 追踪号压缩(URL-Safe Base64编码, 并删除末尾==)
     */
    @PropertyInject(getVmOptionFirst = "glacijava.trace.trace-id-compressed")
    private boolean traceIdCompressed = true;

    /**
     * 追踪号Key, 存入接力信息时追踪号的Key值, 存入MDC时追踪号的Key值
     */
    @PropertyInject(getVmOptionFirst = "glacijava.trace.trace-id-key")
    private String traceIdKey = "_trace_id_";

    /**
     * Service instance creating complete
     */
    @Override
    public void onServiceCreated() {
        TraceIdProvider traceIdProvider;
        try {
            //尝试用SLF4J存追踪号
            Class.forName("org.slf4j.Logger");
            traceIdProvider = (TraceIdProvider) Class.forName("com.github.shepherdviolet.glacimon.java.x.trace.Slf4jTraceIdProvider").newInstance();
        } catch (Exception e) {
            traceIdProvider = new LocalTraceIdProvider();
        }
        traceIdProvider.setTraceIdKey(traceIdKey);
        this.traceIdProvider = traceIdProvider;
    }

    @Override
    public void start() {
        handoff(null, null);
    }

    @Override
    public void start(String customTraceId) {
        handoff(customTraceId, null);
    }

    @Override
    public void handoff(String traceId, Map<String, String> data) {
        //generate id if not exists
        if (CheckUtils.isEmpty(traceId)) {
            traceId = generateTraceID();
        }
        //put id into MDC
        traceIdProvider.set(traceId);
        //put data into thread local
        traceData.set(data);
    }

    protected String generateTraceID() {
        if (traceIdCompressed) {
            return UuidUtils.newStringUuidCompressed();
        }
        return UuidUtils.newStringUuidWithoutDash();
    }

    @Override
    public String getTraceId() {
        return traceIdProvider.get();
    }

    @Override
    public Map<String, String> getTraceData() {
        Map<String, String> data = traceData.get();
        if (data == null) {
            data = new HashMap<>();
            traceData.set(data);
        }
        return data;
    }

    /**
     * 追踪号Key, 存入接力信息时追踪号的Key值, 存入MDC时追踪号的Key值
     */
    @Override
    public String getTraceIdKey() {
        return traceIdKey;
    }

}
