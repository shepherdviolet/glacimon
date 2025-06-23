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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.springboot.apollo;

import com.ctrip.framework.apollo.Config;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.springboot.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * 将Apollo客户端的Config对象包装成HttpClients.OverrideSettings, 用来实现Apollo配置中心动态调整客户端配置, 用法见文档
 *
 * @author shepherdviolet
 */
public class HttpClientsApolloOverrideSettings implements HttpClients.OverrideSettings {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Config config;

    public HttpClientsApolloOverrideSettings(Config config) {
        //持有Apollo配置
        this.config = config;

        if (config == null) {
            logger.warn("Apollo config is disabled (so glacispring-httpclient configuration automatic update is disabled)");
        }
    }

    @Override
    public Set<String> getKeys() {
        //获取所有配置key
        if (config == null) {
            return Collections.emptySet();
        }
        return config.getPropertyNames();
    }

    @Override
    public String getValue(String key) {
        //根据key返回配置value, 不存在返回null
        if (config == null) {
            return null;
        }
        return config.getProperty(key, null);
    }

}
