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

package sviolet.slate.common.springboot.autoconfig;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sviolet.slate.common.web.servlet.SlateServletContextListener;

import javax.servlet.ServletContextListener;

/**
 * 通用上下文监听器
 *
 * @author S.Violet
 */
@Configuration
@ConditionalOnExpression("${slate.common.servlet-context-listener-enabled:true}")
@ConditionalOnClass(javax.servlet.ServletContextListener.class)
public class SlateServletContextListenerConfig {

    @Bean("slate.httpclient.slateServletContextListener")
    public ServletContextListener slateServletContextListener() {
        return new SlateServletContextListener();
    }

}
