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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.helper.rocketmq.consumer.proc;

import org.springframework.context.ApplicationContext;
import sviolet.slate.common.helper.rocketmq.consumer.RocketMqCustomConsumer;
import sviolet.slate.common.helper.rocketmq.consumer.manager.RmqConsumerManager;
import com.github.shepherdviolet.glacimon.spring.x.beanfactory.mbrproc.MemberProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 处理Bean方法上的RocketMqCustomConsumer注解
 *
 * @author S.Violet
 */
public class RmqCustomConsumerMemProc implements MemberProcessor<RocketMqCustomConsumer> {

    private volatile RmqConsumerManager consumerManager;

    @Override
    public Class<RocketMqCustomConsumer> acceptAnnotationType() {
        return RocketMqCustomConsumer.class;
    }

    @Override
    public void visitField(Object bean, String beanName, Field field, RocketMqCustomConsumer annotation, ApplicationContext applicationContext) {

    }

    @Override
    public void visitMethod(Object bean, String beanName, Method method, RocketMqCustomConsumer annotation, ApplicationContext applicationContext) {
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
