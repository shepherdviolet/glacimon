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

import com.github.shepherdviolet.glaciion.api.annotation.NewMethod;
import com.github.shepherdviolet.glaciion.api.annotation.SingleServiceInterface;
import com.github.shepherdviolet.glaciion.api.interfaces.CompatibleApproach;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * [Glaciion扩展点]全局追踪实现接口.
 * 默认实现: DefaultTraceProvider.
 *
 * @author S.Violet
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

}
