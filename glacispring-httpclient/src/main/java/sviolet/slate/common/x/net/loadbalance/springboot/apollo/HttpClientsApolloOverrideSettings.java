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

package sviolet.slate.common.x.net.loadbalance.springboot.apollo;

import com.ctrip.framework.apollo.Config;
import sviolet.slate.common.x.net.loadbalance.springboot.HttpClients;

import java.util.Set;

/**
 * 将Apollo客户端的Config对象包装成HttpClients.OverrideSettings, 用来实现Apollo配置中心动态调整客户端配置, 用法见文档
 *
 * @author S.Violet
 */
public class HttpClientsApolloOverrideSettings implements HttpClients.OverrideSettings {

    private Config config;

    public HttpClientsApolloOverrideSettings(Config config) {
        //持有Apollo配置
        this.config = config;
    }

    @Override
    public Set<String> getKeys() {
        //获取所有配置key
        return config.getPropertyNames();
    }

    @Override
    public String getValue(String key) {
        //根据key返回配置value, 不存在返回null
        return config.getProperty(key, null);
    }

}
