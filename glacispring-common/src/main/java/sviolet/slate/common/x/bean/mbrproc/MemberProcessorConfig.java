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

package sviolet.slate.common.x.bean.mbrproc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * <p>SpringBean成员处理器核心配置</p>
 *
 * <p>
 * 说明:<br>
 * 1.用于对SpringContext中所有的Bean成员(Field/Method)进行处理, 处理时机为Bean装配阶段(BeanPostProcessor)<br>
 * 2.可以实现Bean成员的自定义注入/变换/代理替换等<br>
 * 3.该注解允许多次声明, 声明不同的处理器处理不同的注解
 * </p>
 *
 * @author S.Violet
 */
@Configuration
public class MemberProcessorConfig {

    private static final Logger logger = LoggerFactory.getLogger(MemberProcessorConfig.class);

    @Bean("slate.common.memberProcessorBeanPostProcessor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static BeanPostProcessor memberProcessorBeanPostProcessor(){
        if (logger.isInfoEnabled()) {
            logger.info("MemberProcessor | Enabled");
        }
        return new MemberProcessorBeanPostProcessor(MemberProcessorSelector.annotationAttributesList);
    }

}
