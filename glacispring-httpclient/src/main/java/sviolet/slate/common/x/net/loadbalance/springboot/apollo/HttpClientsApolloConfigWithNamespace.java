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
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import sviolet.slate.common.x.net.loadbalance.springboot.HttpClients;
import sviolet.slate.common.x.net.loadbalance.springboot.autoconfig.SlatePropertiesForHttpClient;
import sviolet.thistle.util.judge.CheckUtils;

/**
 * <p>HttpClients阿波罗动态配置: 支持在Apollo配置中心上动态调整客户端配置</p>
 * <p>配置前缀: slate.httpclient</p>
 *
 * @author S.Violet
 */
@Configuration
@ConditionalOnExpression("${slate.httpclient.enabled:false} " +
        "&& ${slate.httpclient.apollo-support:false} " +
        "&& !'${slate.httpclient.apollo-namespace:<null/>}'.equals(\"<null/>\") " +
        "&& !'${slate.httpclient.apollo-namespace:<null/>}'.equals(\"application\") " +
        "&& '${slate.httpclient.apollo-namespace:<null/>}'.length() > 0")
@ConditionalOnClass(Config.class)
public class HttpClientsApolloConfigWithNamespace {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientsApolloConfigWithNamespace.class);

    private HttpClients httpClients;

    //构造注入确保第一时间获得实例
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public HttpClientsApolloConfigWithNamespace(HttpClients httpClients, SlatePropertiesForHttpClient properties) {
        this.httpClients = httpClients;
        logger.info("HttpClients Apollo | Custom namespace mode");
        String[] namespaces = properties.getHttpclient().getApolloNamespace().split(",");
        for (String namespace : namespaces) {
            if (CheckUtils.isEmptyOrBlank(namespace)) {
                continue;
            }
            namespace = namespace.trim();
            final Config config = ConfigService.getConfig(namespace);
            //检查配置是否获取到
            if (checkConfig(namespace, config)) {
                continue;
            }
            //监听配置变化
            config.addChangeListener(new ConfigChangeListener() {
                @Override
                public void onChange(ConfigChangeEvent changeEvent) {
                    HttpClientsApolloConfigWithNamespace.this.httpClients.settingsOverride(new HttpClientsApolloOverrideSettings(config));
                }
            });
            //启动时先更新一次配置
            HttpClientsApolloConfigWithNamespace.this.httpClients.settingsOverride(new HttpClientsApolloOverrideSettings(config));
            logger.info("HttpClients Apollo | Listening client config changes from apollo, namespace: " + namespace);
        }
    }

    private boolean checkConfig(String namespace, Config config) {

        //兼容老版本没有getSourceType方法
        try {
            Config.class.getMethod("getSourceType");
        } catch (Exception e) {
            return false;
        }

        if (config == null || config.getSourceType() == ConfigSourceType.NONE) {
            logger.error("HttpClients Apollo | Namespace '" + namespace + "' not found !!! Listening failure !!!");
            return true;
        }
        if (config.getSourceType() == ConfigSourceType.LOCAL) {
            logger.error("HttpClients Apollo | Namespace '" + namespace + "' in local cache only !!! Listening failure !!!");
            return true;
        }

        return false;
    }

}
