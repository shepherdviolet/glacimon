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

import com.github.shepherdviolet.glaciion.Glaciion;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 全局追踪API, Glaciion扩展点: sviolet.thistle.x.util.trace.TraceProvider.
 * 默认实现: DefaultTraceProvider.
 *
 * @author S.Violet
 */
public class Trace {

    public static final String TRACE_ID_KEY = "_trace_id_";

    private static final TraceProvider PROVIDER = Glaciion.loadSingleService(TraceProvider.class).get();

    /**
     * 重头开始追踪
     */
    public static void start(){
        PROVIDER.start();
    }

    /**
     * 重新开始追踪
     * @param customTraceId 指定新的追踪号
     */
    public static void start(String customTraceId){
        PROVIDER.start(customTraceId);
    }

    /**
     * 继续追踪
     * @param traceBaton 追踪接力信息, 如果送空则从头开始
     */
    public static void handoff(TraceBaton traceBaton){
        if (traceBaton == null) {
            PROVIDER.start();
        } else {
            PROVIDER.handoff(traceBaton.getTraceId(), traceBaton.getTraceData());
        }
    }

    /**
     * 创建一个可追踪的Runnable(自动完成接力), 用于异步追踪
     * @param runnable Runnable
     * @return 可追踪的Runnable(自动完成接力), 用于异步追踪
     */
    public static Runnable traceable(Runnable runnable){
        return new TraceableRunnable(runnable);
    }

    /**
     * 创建一个可追踪的Callable(自动完成接力), 用于异步追踪
     * @param callable Callable
     * @return 可追踪的Callable(自动完成接力), 用于异步追踪
     */
    public static <T> Callable<T> traceable(Callable<T> callable){
        return new TraceableCallable<>(callable);
    }

    /**
     * 获取追踪接力信息
     */
    public static TraceBaton getBaton(){
        return new TraceBaton(getTraceId(), getDataMap());
    }

    /**
     * 获取追踪号
     */
    public static String getTraceId(){
        String traceId = PROVIDER.getTraceId();
        if (traceId == null) {
            PROVIDER.start();
            traceId = PROVIDER.getTraceId();
        }
        return traceId;
    }

    /**
     * 获取其他追踪信息
     */
    public static String getData(String key){
        return PROVIDER.getTraceData().get(key);
    }

    /**
     * 获取其他追踪信息
     *
     * @param fallback 如果取不到则返回该值
     */
    public static String getData(String key, String fallback) {
        String result = PROVIDER.getTraceData().get(key);
        return result != null ? result : fallback;
    }

    /**
     * 设置其他追踪信息
     */
    public static String setData(String key, String value) {
        return PROVIDER.getTraceData().put(key, value);
    }

    /**
     * 获取所有其他追踪信息
     */
    public static Map<String, String> getDataMap(){
        return PROVIDER.getTraceData();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */


    private static class TraceableCallable<T> implements Callable<T> {

        private Callable<T> callable;
        private TraceBaton traceBaton;

        private TraceableCallable(Callable<T> callable) {
            this.callable = callable;
            //获取接力信息
            traceBaton = Trace.getBaton();
        }

        @Override
        public T call() throws Exception {
            //接力
            Trace.handoff(traceBaton);
            return callable.call();
        }

    }

    private static class TraceableRunnable implements Runnable {

        private Runnable runnable;
        private TraceBaton traceBaton;

        private TraceableRunnable(Runnable runnable) {
            this.runnable = runnable;
            //获取接力信息
            traceBaton = Trace.getBaton();
        }

        @Override
        public final void run() {
            //接力
            Trace.handoff(traceBaton);
            runnable.run();
        }

    }

}
