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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance;

import java.io.Closeable;

/**
 * 负载均衡--网络状态探测器
 * @author shepherdviolet
 */
public interface LoadBalanceInspector extends Closeable {

    /**
     * <p>实现探测逻辑</p>
     *
     * <p>注意:尽量处理掉所有异常, 如果抛出异常, 视为探测失败, 程序将阻断远端</p>
     *
     * @param url 远端url
     * @return true:网络正常 false:网络异常
     */
    boolean inspect(String url);

    void setTimeout(long timeout);

    void refreshSettings();

}
