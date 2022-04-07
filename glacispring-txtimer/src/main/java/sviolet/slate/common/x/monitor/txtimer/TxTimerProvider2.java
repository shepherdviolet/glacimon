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

package sviolet.slate.common.x.monitor.txtimer;

import com.github.shepherdviolet.glaciion.api.annotation.SingleServiceInterface;

/**
 * <p>TxTimer简单的交易耗时统计 扩展点</p>
 *
 * <p>实现:耗时统计/结果输出</p>
 *
 * <p>使用扩展点之前, 请先仔细阅读文档: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md</p>
 *
 * @see TxTimer
 * @author S.Violet
 */
@SingleServiceInterface
public interface TxTimerProvider2 {

    /**
     * <p>交易开始时调用</p>
     *
     * @param groupName 组别
     * @param transactionName 交易名
     */
    TimerContext entry(String groupName, String transactionName);

    /**
     * 交易结束时调用
     *
     * @param timerContext 计数上下文
     * @param resultCode 结果码
     */
    void exit(TimerContext timerContext, int resultCode);

    /**
     * 是否启用统计功能
     * @return true 启用
     */
    boolean enabled();

    /**
     * 是否能通过TxTimer.getProvider()获取到当前实例
     * @return true 允许
     */
    boolean canBeGet();

}
