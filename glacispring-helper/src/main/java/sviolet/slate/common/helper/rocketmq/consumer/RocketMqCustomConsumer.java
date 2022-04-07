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

import java.lang.annotation.*;

/**
 * <p>RocketMQ 将自定义消费者绑定到指定方法</p>
 *
 * <p>注意!!! 消费者必须保证幂等性! 任何一条消息都有可能被重复消费!</p>
 *
 * <p>https://github.com/shepherdviolet/slate/blob/master/docs/rocketmq/guide.md</p>
 *
 * <pre>
 *     <code>@Bean(name = "consumerBeanName", destroyMethod = "shutdown")</code>
 *     public DefaultMQPushConsumer customConsumer() throws MQClientException {
 *         DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer-custom");
 *         consumer.setNamesrvAddr(nameServer);
 *         consumer.subscribe("custom-topic", "*");
 *         //注意!!! consumer不要启动, 也不要绑定listener, 因为这些操作会在绑定方法时自动进行
 *         return consumer;
 *     }
 * </pre>
 *
 * <pre>
 *     <code>@RocketMqCustomConsumer(</code>
 *             consumerBeanName = "consumerBeanName",
 *             isOrdered = false
 *     )
 *     public boolean slateCustomConsumerTest(MessageExt message) {
 *         logger.info("Received String: {}", message);
 *         return true;
 *     }
 * </pre>
 *
 * @author S.Violet
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMqCustomConsumer {

    /**
     * DefaultMQPushConsumer Bean Name
     */
    String consumerBeanName();

    /**
     * true 顺序消费 false 并发消费
     */
    boolean isOrdered();

    /**
     * true 抛出异常时重新消费消息(RECONSUME_LATER), 默认false
     */
    boolean reconsumeWhenException() default false;

    /**
     * 字符集, 默认UTF-8
     */
    String charset() default "";

}
