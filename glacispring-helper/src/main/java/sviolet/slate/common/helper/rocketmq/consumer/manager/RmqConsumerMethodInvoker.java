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

import org.apache.rocketmq.common.message.MessageExt;

/**
 * RocketMQ 消费方法调用器:
 * 预处理消息, 调用绑定的消费方法(注解标记的方法)
 *
 * @author S.Violet
 */
public interface RmqConsumerMethodInvoker {

    /**
     * 实现将MessageExt转为方法所需的数据类型, 然后调用方法的逻辑
     * @param messageExt 消息
     */
    Object invoke(MessageExt messageExt) throws Exception;

}
