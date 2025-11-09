/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.helper.rocketmq.compat;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RocketMQ新老版本兼容工具
 *
 * @author shepherdviolet
 */
public class RocketMqCompatUtils {

    /**
     * DefaultMQPushConsumer新老版本兼容
     */
    public static class DefaultMQPushConsumerCompat {

        /**
         * RocketMQ 5 以后, MessageModel的包名改掉了.
         * import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
         * import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public static void setMessageModelToBroadcasting(DefaultMQPushConsumer consumer) {
            if (consumer == null) {
                return;
            }

            Class<?> consumerClass = consumer.getClass();
            Class messageModelClass;
            try {
                messageModelClass = consumerClass.getMethod("getMessageModel").getReturnType();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Can not set MessageModel to DefaultMQPushConsumer, The type of 'MessageModel' cannot be identified.", e);
            }

            Object messageModel;
            try {
                messageModel = Enum.valueOf(messageModelClass, "BROADCASTING");
            } catch (Exception e) {
                throw new RuntimeException("Can not set MessageModel to DefaultMQPushConsumer, The enum 'MessageModel' cannot be create.", e);
            }

            try {
                Method setMethod = consumerClass.getMethod("setMessageModel", messageModelClass);
                setMethod.invoke(consumer, messageModel);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Can not set MessageModel to DefaultMQPushConsumer, failed to invoke setMessageModel method.", e);
            }


        }

    }

}
