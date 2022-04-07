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

/**
 * <p>slate-http-client的客户端公共配置</p>
 *
 * <p>配置前缀: slate.httpclient</p>
 *
 * @author S.Violet
 */
public class SlateHttpClientProperties {

    /**
     * true: 启用Slate的HttpClient(总开关, 详见HttpClientsConfig), 默认false
     */
    private boolean enabled = false;

    /**
     * true: 根据Apollo配置动态调整客户端的参数(详见HttpClientsApolloConfig), 默认false
     */
    private boolean apolloSupported = false;

    /**
     * 设置Apollo配置的Namespace, 多个用逗号分隔, 默认为空(默认监听应用默认私有配置application). 如非必要, 请勿配置该参数.
     */
    private String apolloNamespace;

    /**
     * true: 开启提醒类日志(特殊情况下可以关闭), 默认true
     */
    private boolean noticeLogEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isApolloSupported() {
        return apolloSupported;
    }

    public void setApolloSupported(boolean apolloSupported) {
        this.apolloSupported = apolloSupported;
    }

    public String getApolloNamespace() {
        return apolloNamespace;
    }

    public void setApolloNamespace(String apolloNamespace) {
        this.apolloNamespace = apolloNamespace;
    }

    public boolean isNoticeLogEnabled() {
        return noticeLogEnabled;
    }

    public void setNoticeLogEnabled(boolean noticeLogEnabled) {
        this.noticeLogEnabled = noticeLogEnabled;
    }
}
