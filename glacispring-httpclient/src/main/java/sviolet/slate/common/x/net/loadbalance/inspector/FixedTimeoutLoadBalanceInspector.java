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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.net.loadbalance.inspector;

import sviolet.slate.common.x.net.loadbalance.LoadBalanceInspector;

/**
 * 负载均衡--网络状态探测器--固定超时时间版.
 * 如果主动探测器无法在探测时实时指定超时时间, 可以实现该接口, 探测管理器会在自身探测间隔变化时, 调用setTimeout方法修改主动探测器
 * 的超时时间
 *
 * @author S.Violet
 */
public interface FixedTimeoutLoadBalanceInspector extends LoadBalanceInspector {

    /**
     * 探测管理器会在自身探测间隔变化时, 调用setTimeout方法修改主动探测器的超时时间
     * @param timeout 超时时间 ms
     */
    void setTimeout(long timeout);

}
