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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.springboot.autoconfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.shepherdviolet.glacimon.spring.x.config.mbrproc.EnableMemberProcessor;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.DataConverter;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GsonDataConverter;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.springboot.autowired.HttpClientMemberProcessor;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.springboot.HttpClients;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * <p>HttpClients配置: 自动配置GlaciHttpClient</p>
 * <p>配置前缀: glacispring.httpclients</p>
 *
 * @author shepherdviolet
 */
@Configuration
@ConditionalOnExpression("${glacispring.httpclient.enabled:false}")
@EnableMemberProcessor(HttpClientMemberProcessor.class)//开启@HttpClient注解注入
public class HttpClientsConfig {

    /**
     * <p>自动配置HttpClients</p>
     * <p>我们可以用如下方式获得所有客户端(包括运行时动态添加的):</p>
     *
     * <pre>
     *     private GlaciHttpClient client;
     *     <code>@Autowired</code>
     *     public Constructor(HttpClients httpClients) {
     *         this.client = httpClients.get("tagname");
     *     };
     * </pre>
     */
    @Primary
    @Bean(HttpClients.HTTP_CLIENTS_NAME)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public HttpClients httpClientsContainer(
            @Qualifier(GlacispringPropertiesForHttpClient.BEAN_NAME) GlacispringPropertiesForHttpClient glacispringPropertiesForHttpClient,
            ObjectProvider<Map<String, DataConverter>> dataConverterProvider) {

        //data converter
        DataConverter dataConverter = null;
        Map<String, DataConverter> dataConverterMap = dataConverterProvider.getIfAvailable();
        if (dataConverterMap != null) {
            dataConverter = dataConverterMap.get(HttpClients.DATA_CONVERTER_NAME);
        }

        //impl
        return new HttpClientsImpl(glacispringPropertiesForHttpClient, dataConverter);
    }

    /**
     * 数据转换器(JavaBean -> byte[])
     */
    @Bean(HttpClients.DATA_CONVERTER_NAME)
    @ConditionalOnClass(name = "com.google.gson.Gson")
    @ConditionalOnMissingBean(name = HttpClients.DATA_CONVERTER_NAME)
    public DataConverter httpClientsDataConverter(){
        //默认用GSON
        return new GsonDataConverter();
    }

}
