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

package com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.noref;

import com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.TimerContext;
import com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.TxTimer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * <p>无依赖的TxTimer</p>
 *
 * <p>你可以用反射实例化本类, 用BiFunction和BiConsumer接口调用entry和exit方法.
 * 使得其他库在能够选择使用TxTimer的同时, 又可以不依赖glacispring-txtimer. (不依赖就不用)</p>
 *
 * <p>参考: com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.statistics.NoDepTxTimerProxy</p>
 *
 * @author shepherdviolet
 */
public class NoDepTxTimer implements BiFunction<String, String, Object>, BiConsumer<Object, Integer> {

    @Override
    public Object apply(String groupName, String transactionName) {
        return TxTimer.entry(groupName, transactionName);
    }

    @Override
    public void accept(Object timerContext, Integer resultCode) {
        if (!(timerContext instanceof TimerContext)) {
            return;
        }
        if (resultCode == null) {
            resultCode = 0;
        }
        TxTimer.exit((TimerContext) timerContext, resultCode);
    }

}
