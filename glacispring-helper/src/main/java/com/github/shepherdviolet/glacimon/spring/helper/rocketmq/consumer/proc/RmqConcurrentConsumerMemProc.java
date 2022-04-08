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

package com.github.shepherdviolet.glacimon.spring.helper.rocketmq.consumer.proc;

import com.github.shepherdviolet.glacimon.spring.helper.rocketmq.consumer.manager.RmqConsumerManager;
import org.springframework.context.ApplicationContext;
import com.github.shepherdviolet.glacimon.spring.helper.rocketmq.consumer.RocketMqConcurrentConsumer;
import com.github.shepherdviolet.glacimon.spring.x.beanfactory.mbrproc.MemberProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 处理Bean方法上的RocketMqConcurrentConsumer注解
 *
 * @author shepherdviolet
 */
public class RmqConcurrentConsumerMemProc implements MemberProcessor<RocketMqConcurrentConsumer> {

    private volatile RmqConsumerManager consumerManager;

    @Override
    public Class<RocketMqConcurrentConsumer> acceptAnnotationType() {
        return RocketMqConcurrentConsumer.class;
    }

    @Override
    public void visitField(Object bean, String beanName, Field field, RocketMqConcurrentConsumer annotation, ApplicationContext applicationContext) {

    }

    @Override
    public void visitMethod(Object bean, String beanName, Method method, RocketMqConcurrentConsumer annotation, ApplicationContext applicationContext) {
        //交由RocketMqConsumerManager处理
        try {
            getConsumerManager(applicationContext).registerMethod(bean, beanName, method, annotation);
        } catch (Exception e) {
            throw new RuntimeException("Error while register RocketMQ consumer to method " + bean.getClass().getName() +
                    "#" + method.getName(), e);
        }
    }

    private RmqConsumerManager getConsumerManager(ApplicationContext applicationContext) {
        if (consumerManager == null) {
            synchronized (this) {
                if (consumerManager == null) {
                    consumerManager = applicationContext.getBean(RmqConsumerManager.BEAN_NAME, RmqConsumerManager.class);
                }
            }
        }
        return consumerManager;
    }

}
