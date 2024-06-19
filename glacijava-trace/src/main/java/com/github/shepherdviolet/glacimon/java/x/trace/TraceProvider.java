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

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.NewMethod;
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.CompatibleApproach;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * [GlacimonSpi扩展点]全局追踪实现接口.
 * 默认实现: DefaultTraceProvider.
 *
 * @author shepherdviolet
 */
@SingleServiceInterface
public interface TraceProvider {

    /**
     * 重新开始追踪
     */
    void start();

    /**
     * 重新开始追踪
     * @param customTraceId 指定新的追踪号
     */
    @NewMethod(compatibleApproach = StartMethodCompat.class)
    void start(String customTraceId);

    /**
     * 继续追踪
     */
    void handoff(String traceId, Map<String, String> data);

    /**
     * 获取追踪号
     */
    String getTraceId();

    /**
     * 获取所有追踪数据, 这个方法禁止返回null
     */
    Map<String, String> getTraceData();

    /**
     * 追踪号Key, 存入接力信息时追踪号的Key值, 存入MDC时追踪号的Key值
     */
    @NewMethod(compatibleApproach = GetTraceIdKeyMethodCompat.class)
    String getTraceIdKey();

    /**
     * 兼容新增的start(String)方法
     */
    class StartMethodCompat implements CompatibleApproach {
        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] params) throws Throwable {
            //当调用start(String)时, 实际上会调用start()
            ((TraceProvider)serviceInstance).start();
            return null;
        }
    }

    /**
     * 兼容新增的getTraceIdKey()方法
     */
    class GetTraceIdKeyMethodCompat implements CompatibleApproach {
        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] params) throws Throwable {
            return "_trace_id_";
        }
    }

}
