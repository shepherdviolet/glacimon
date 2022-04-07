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

import java.lang.reflect.Method;

/**
 * 消费方法调用器工厂:
 * 1.根据方法参数选择如何调用该方法, 如何预处理消息(类型转换)
 * 2.可以自定义实现该工厂, 扩展类型支持, 或者实现事件拦截
 *
 * @author S.Violet
 */
public interface RmqConsumerMethodInvokerFactory {

    String BEAN_NAME = "slate.common.rocketMqConsumerMethodInvokerFactory";

    /**
     * 根据绑定方法参数类型产生对应的方法调用器
     * @param acceptType 绑定方法的参数类型
     * @param bean Bean实例
     * @param method 方法
     * @param charset 字符集
     * @return 方法调用器(数据类型转换/拦截等)
     */
    RmqConsumerMethodInvoker newInvoker(Class<?> acceptType, Object bean, Method method, String charset);

}
