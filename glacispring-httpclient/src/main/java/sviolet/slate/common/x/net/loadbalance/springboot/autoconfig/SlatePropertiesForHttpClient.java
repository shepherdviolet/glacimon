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

package sviolet.slate.common.x.net.loadbalance.springboot.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * <p>slate-http-client 配置</p>
 * <p>配置前缀: slate.httpclient slate.httpclients</p>
 *
 * @author S.Violet
 */
@ConfigurationProperties(prefix = "slate")
public class SlatePropertiesForHttpClient {

    public static final String BEAN_NAME = "slate.httpclient.slatePropertiesForHttpClient";

    /**
     * 配置Http客户端的公共配置(总开关/Apollo动态配置等), 详见HttpClientProperties
     */
    private SlateHttpClientProperties httpclient;

    /**
     * 配置每个Http客户端的参数, key:客户端名, value:详见HttpClientSettings
     */
    private Map<String, HttpClientSettings> httpclients;

    public SlateHttpClientProperties getHttpclient() {
        return httpclient;
    }

    public void setHttpclient(SlateHttpClientProperties httpclient) {
        this.httpclient = httpclient;
    }

    public Map<String, HttpClientSettings> getHttpclients() {
        return httpclients;
    }

    public void setHttpclients(Map<String, HttpClientSettings> httpclients) {
        this.httpclients = httpclients;
    }

}
