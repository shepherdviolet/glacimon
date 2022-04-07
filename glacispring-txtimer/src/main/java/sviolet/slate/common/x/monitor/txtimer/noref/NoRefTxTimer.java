/*
 * Copyright (C) 2015-2018 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.monitor.txtimer.noref;

import sviolet.slate.common.x.monitor.txtimer.TimerContext;
import sviolet.slate.common.x.monitor.txtimer.TxTimerProvider2;

/**
 * <p>对TxTimer类无引用的NoRefTxTimer代理类</p>
 *
 * <p>本类和NoRefTxTimerFactory类对TxTimer无直接类引用, 用于类库和框架层, 可以由用户选择是否开启TxTimer, 若不开启, 则不会初始化
 * TxTimer类, 减少无用的对象创建.</p>
 *
 * @author S.Violet
 */
public interface NoRefTxTimer {

    TimerContext entry(String groupName, String transactionName);

    void exit(TimerContext timerContext);

    void exit(TimerContext timerContext, int resultCode);

    TxTimerProvider2 getProvider();

}
