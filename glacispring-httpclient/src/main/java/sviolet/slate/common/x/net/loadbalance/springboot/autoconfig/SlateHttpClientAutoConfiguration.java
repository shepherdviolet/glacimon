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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import sviolet.slate.common.x.net.loadbalance.springboot.apollo.HttpClientsApolloConfig;
import sviolet.slate.common.x.net.loadbalance.springboot.apollo.HttpClientsApolloConfigWithNamespace;

/**
 * slate-http-client 自动配置 (Spring Boot)
 *
 * @author S.Violet
 */
@Configuration
@EnableConfigurationProperties
@Import({
        HttpClientsConfig.class,
        HttpClientsApolloConfig.class,
        HttpClientsApolloConfigWithNamespace.class
})
public class SlateHttpClientAutoConfiguration {

    @Bean(SlatePropertiesForHttpClient.BEAN_NAME)
    public SlatePropertiesForHttpClient slatePropertiesForHttpClient(){
        return new SlatePropertiesForHttpClient();
    }

}
