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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector;

import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalanceInspector;

import java.io.IOException;

/**
 * 负载均衡--不探测网络状况(默认返回健康)
 *
 * @author shepherdviolet
 */
public class EmptyLoadBalanceInspector implements LoadBalanceInspector {

    @Override
    public boolean inspect(String url) {
        // Do nothing
        return true;
    }

    @Override
    public void setTimeout(long timeout) {
        // Do nothing
    }

    @Override
    public void refreshSettings() {
        // Do nothing
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }

    @Override
    public String toString() {
        return "EmptyLoadBalanceInspector{}";
    }

}
