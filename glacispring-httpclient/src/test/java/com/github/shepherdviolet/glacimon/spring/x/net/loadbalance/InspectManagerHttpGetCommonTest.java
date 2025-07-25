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

import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient;

/**
 * 主动探测器--HTTP GET方式测试案例
 *
 * 测试要点:
 * 1.需要后端
 * 2.启停后端服务, 判断是否能正确探测到
 */
public class InspectManagerHttpGetCommonTest {

    public static void main(String[] args) {

        LoadBalancedHostManager hostManager = new LoadBalancedHostManager();
        hostManager.setHostArray(new String[]{
                "http://www.baidu.com",
                "http://127.0.0.1:8080",
                "http://127.0.0.1:8081"
        });

        LoadBalancedInspectManager inspectManager = new LoadBalancedInspectManager(hostManager);
        inspectManager.setInspectInterval(5000L);
        inspectManager.setInspectMode("/get");

    }

}
