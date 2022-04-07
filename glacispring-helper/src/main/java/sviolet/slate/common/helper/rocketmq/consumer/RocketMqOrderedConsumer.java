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
 * <p>RocketMQ 顺序消费者</p>
 *
 * <p>注意!!! 消费者必须保证幂等性! 任何一条消息都有可能被重复消费!</p>
 *
 * <p>https://github.com/shepherdviolet/slate/blob/master/docs/rocketmq/guide.md</p>
 *
 * <p>顺序消费有两个前提, 生产者必须将消息放进同一个队列(可以将某个TAG的消息放进同一个队列), 消费者必须消费同一个队列(订阅那个TAG).
 * MQ只保证同一个队列中的消息的顺序. 消费者如果是集群, 队列会被全局锁定(同时只有一个消费者可以消费消息), 每个消费者同时只有一个
 * 线程在消费消息.</p>
 *
 * <pre>
 *     <code>@RocketMqOrderedConsumer(</code>
 *             consumerGroup = "consumer-b",
 *             topic = "test-ordered-topic",
 *             subExpression = "tag-a || tag-b || tag-c"
 *     )
 *     public boolean test(MessageExt message){
 *         logger.info("Received MessageExt: {}", message);
 *         return true;
 *     }
 * </pre>
 *
 * @author S.Violet
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMqOrderedConsumer {

    /**
     * 消费组名
     */
    String consumerGroup();

    /**
     * Topic
     */
    String topic();

    /**
     * 订阅表达式, 例如: TagA || TagB || TagC, 默认全订阅.
     * 注意: subExpression与sqlExpression二选一, 如果都配置了则sqlExpression生效.
     */
    String subExpression() default "*";

    /**
     * 选择消息从队列头还是队列尾开始消费
     */
    ConsumeWay consumeFromWhere() default ConsumeWay.CONSUME_FROM_FIRST_OFFSET;

    /**
     * true 抛出异常时重新消费消息(SUSPEND_CURRENT_QUEUE_A_MOMENT), 默认false
     */
    boolean reconsumeWhenException() default false;

    /**
     * 字符集, 默认UTF-8
     */
    String charset() default "";

    /**
     * 指定NameServer地址. 优先级最高.
     * 优先级:
     * 1.@RocketMqConcurrentConsumer(nameServer = "...")
     * 2.启动参数slate.common.rocketmq.namesrv
     * 3.配置文件slate.common.rocketmq.namesrv
     * 4.@EnableRocketMq(defaultNameServer = "...")
     */
    String nameServer() default "";

}
