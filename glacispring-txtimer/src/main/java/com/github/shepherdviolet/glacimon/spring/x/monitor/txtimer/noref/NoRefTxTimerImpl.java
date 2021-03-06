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

package com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.noref;

import com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.TimerContext;
import com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.TxTimer;
import com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.TxTimerProvider2;

/**
 * 与TxTimer关联点
 *
 * @author shepherdviolet
 */
class NoRefTxTimerImpl implements NoRefTxTimer {

    @Override
    public TimerContext entry(String groupName, String transactionName) {
        return TxTimer.entry(groupName, transactionName);
    }

    @Override
    public void exit(TimerContext timerContext) {
        TxTimer.exit(timerContext);
    }

    @Override
    public void exit(TimerContext timerContext, int resultCode) {
        TxTimer.exit(timerContext, resultCode);
    }

    @Override
    public TxTimerProvider2 getProvider() {
        return TxTimer.getProvider();
    }

}
