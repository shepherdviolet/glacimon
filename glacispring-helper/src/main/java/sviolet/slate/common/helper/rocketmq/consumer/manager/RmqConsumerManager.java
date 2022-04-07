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

package sviolet.slate.common.helper.rocketmq.consumer.manager;

import org.springframework.beans.factory.DisposableBean;
import sviolet.slate.common.helper.rocketmq.consumer.RocketMqConcurrentConsumer;
import sviolet.slate.common.helper.rocketmq.consumer.RocketMqCustomConsumer;
import sviolet.slate.common.helper.rocketmq.consumer.RocketMqOrderedConsumer;

import java.lang.reflect.Method;

/**
 * RocketMQ Consumer管理器:
 * 创建Consumer, 与指定方法绑定, 实现消息处理和方法调用逻辑
 *
 * @author S.Violet
 */
public interface RmqConsumerManager extends DisposableBean {

    String BEAN_NAME = "slate.common.rocketMqConsumerManager";

    /**
     * 与RocketMqCustomConsumer注解的方法绑定(自定义消费者)
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @param method 方法
     * @param annotation 注解
     */
    void registerMethod(Object bean, String beanName, Method method, RocketMqCustomConsumer annotation) throws Exception;

    /**
     * 与RocketMqConcurrentConsumer注解的方法绑定(普通并发消费者)
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @param method 方法
     * @param annotation 注解
     */
    void registerMethod(Object bean, String beanName, Method method, RocketMqConcurrentConsumer annotation) throws Exception;

    /**
     * 与RocketMqOrderedConsumer注解的方法绑定(顺序消费者)
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @param method 方法
     * @param annotation 注解
     */
    void registerMethod(Object bean, String beanName, Method method, RocketMqOrderedConsumer annotation) throws Exception;

}
