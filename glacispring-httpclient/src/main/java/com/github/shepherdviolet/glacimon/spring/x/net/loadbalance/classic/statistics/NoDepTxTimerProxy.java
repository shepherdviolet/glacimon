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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * 无依赖的TxTimer代理
 *
 * @author shepherdviolet
 */
public class NoDepTxTimerProxy {

    private static final Logger logger = LoggerFactory.getLogger(NoDepTxTimerProxy.class);

    private static volatile BiFunction<String, String, Object> entryFunction;
    private static volatile BiConsumer<Object, Integer> exitFunction;

    /**
     * <p>交易开始时调用</p>
     *
     * <code>
     *  Object context = NoDepTxTimerProxy.entry("Entrance", "TestService");
     *  try {
     *      // 交易逻辑 ......
     *  } finally {
     *      NoDepTxTimerProxy.exit(context);
     *  }
     * </code>
     *
     * @param groupName 组别
     * @param transactionName 交易名
     */
    public static Object entry(String groupName, String transactionName) {
        init();
        return entryFunction.apply(groupName, transactionName);
    }

    /**
     * 交易结束时调用
     *
     * <code>
     *  Object context = NoDepTxTimerProxy.entry("Entrance", "TestService");
     *  try {
     *      // 交易逻辑 ......
     *  } finally {
     *      NoDepTxTimerProxy.exit(context);
     *  }
     * </code>
     *
     * @param timerContext 处理结果编码
     */
    public static void exit(Object timerContext) {
        init();
        exitFunction.accept(timerContext, 0);
    }

    /**
     * 交易结束时调用
     *
     * <code>
     *  Object context = NoDepTxTimerProxy.entry("Entrance", "TestService");
     *  try {
     *      // 交易逻辑 ......
     *  } finally {
     *      NoDepTxTimerProxy.exit(context, code);
     *  }
     * </code>
     *
     * @param timerContext 处理结果编码
     */
    public static void exit(Object timerContext, int resultCode){
        init();
        exitFunction.accept(timerContext, resultCode);
    }

    @SuppressWarnings("unchecked")
    private static void init() {
        if (entryFunction == null || exitFunction == null) {
            synchronized (NoDepTxTimerProxy.class) {
                if (entryFunction == null || exitFunction == null) {
                    try {
                        Object noDepTxTimer = Class.forName("com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.noref.NoDepTxTimer").newInstance();
                        entryFunction = (BiFunction<String, String, Object>) noDepTxTimer;
                        exitFunction = (BiConsumer<Object, Integer>) noDepTxTimer;
                    } catch (Exception e) {
                        logger.warn("TxTimer | TxTimer disabled. If you want to use TxTimer to count the time consumption of the GlaciHttpClient, please add the dependency 'glacispring-txtimer'", e);
                        entryFunction = DUMMY_TIMER;
                        exitFunction = DUMMY_TIMER;
                    }
                }
            }
        }

    }

    private static final Object DUMMY_CONTEXT = new Object();
    private static final DummyNoDepTxTimer DUMMY_TIMER = new DummyNoDepTxTimer();

    private static class DummyNoDepTxTimer implements BiFunction<String, String, Object>, BiConsumer<Object, Integer> {

        @Override
        public void accept(Object o, Integer integer) {
        }

        @Override
        public Object apply(String s, String s2) {
            return DUMMY_CONTEXT;
        }

    }

}
