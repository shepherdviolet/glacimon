/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.slate.common.helper.rocketmq.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sviolet.slate.common.helper.rocketmq.consumer.manager.*;
import sviolet.slate.common.helper.rocketmq.consumer.proc.RmqConcurrentConsumerMemProc;
import sviolet.slate.common.helper.rocketmq.consumer.proc.RmqCustomConsumerMemProc;
import sviolet.slate.common.helper.rocketmq.consumer.proc.RmqOrderedConsumerMemProc;
import sviolet.slate.common.x.bean.mbrproc.EnableMemberProcessor;

/**
 * RocketMQ Helper 配置类
 *
 * @author S.Violet
 */
@Configuration
@EnableMemberProcessor({
        RmqConcurrentConsumerMemProc.class,
        RmqOrderedConsumerMemProc.class,
        RmqCustomConsumerMemProc.class,
})
public class RmqHelperConfig {

    private static final Logger logger = LoggerFactory.getLogger(RmqHelperConfig.class);

    /**
     * RocketMQ Consumer管理器:
     * 创建Consumer, 与指定方法绑定, 实现消息处理和方法调用逻辑
     */
    @Bean(RmqConsumerManager.BEAN_NAME)
    @ConditionalOnMissingBean(name = RmqConsumerManager.BEAN_NAME)
    public RmqConsumerManager consumerManager(){
        if (logger.isInfoEnabled()) {
            logger.info("RocketMQ Helper Enabled");
        }
        return new RmqConsumerManagerImpl(RmqHelperSelector.annotationAttributes);
    }

    /**
     * 消费方法调用器工厂:
     * 1.根据方法参数选择如何调用该方法, 如何预处理消息(类型转换)
     * 2.可以自定义实现该工厂, 扩展类型支持, 或者实现事件拦截
     */
    @Bean(RmqConsumerMethodInvokerFactory.BEAN_NAME)
    @ConditionalOnMissingBean(name = RmqConsumerMethodInvokerFactory.BEAN_NAME)
    public RmqConsumerMethodInvokerFactory consumerMethodInvokerFactory(){
        return new RmqConsumerMethodInvokerFactoryImpl();
    }

}
