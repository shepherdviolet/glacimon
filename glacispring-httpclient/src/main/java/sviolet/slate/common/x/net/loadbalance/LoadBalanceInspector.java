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

package sviolet.slate.common.x.net.loadbalance;

/**
 * 负载均衡--网络状态探测器
 * @author S.Violet
 */
public interface LoadBalanceInspector {

    /**
     * <p>实现探测逻辑</p>
     *
     * <p>注意:尽量处理掉所有异常, 如果抛出异常, 视为探测失败, 程序将阻断远端</p>
     *
     * @param url 远端url
     * @param timeout 限定的探测时间(ms), 探测器必须在该时间内探测完毕, 不要过久的占用线程. 多个探测器时, 应保证总时间不超过设定值.
     * @return true:网络正常 false:网络异常
     */
    boolean inspect(String url, long timeout, boolean verboseLog);

}
