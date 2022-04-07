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
 * <p>RocketMQ 普通并发消费者</p>
 *
 * <p>注意!!! 消费者必须保证幂等性! 任何一条消息都有可能被重复消费!</p>
 *
 * <p>https://github.com/shepherdviolet/slate/blob/master/docs/rocketmq/guide.md</p>
 *
 * <pre>
 *     <code>@RocketMqConcurrentConsumer(</code>
 *             consumerGroup = "consumer-a",
 *             topic = "test-topic",
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
public @interface RocketMqConcurrentConsumer {

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
     * 使用SQL92过滤消息. 需Broker配置支持. 例如: a between 0 and 3
     * 参考: http://rocketmq.apache.org/docs/filter-by-sql92-example/
     * 注意: subExpression与sqlExpression二选一, 如果都配置了则sqlExpression生效.
     */
    String sqlExpression() default "";

    /**
     * 选择消息从队列头还是队列尾开始消费
     */
    ConsumeWay consumeFromWhere() default ConsumeWay.CONSUME_FROM_FIRST_OFFSET;

    /**
     * 是否以广播方式订阅
     */
    boolean isBroadcast() default false;

    /**
     * 最小线程数
     */
    int threadMin() default 4;

    /**
     * 最大线程数,
     * 这个参数目前无效, 请设置threadMin, 见https://github.com/apache/rocketmq-spring/issues/184
     */
    int threadMax() default 8;

    /**
     * true 抛出异常时重新消费消息(RECONSUME_LATER), 默认false
     */
    boolean reconsumeWhenException() default false;

    /**
     * 字符集, 默认UTF-8
     */
    String charset() default "";

    /**
     * 指定NameServer地址. 优先级最高.
     * 优先级:
     * 1.@RocketMqConcurrentConsumer(nameServer = "...") / @RocketMqOrderedConsumer(nameServer = "...")
     * 2.启动参数slate.common.rocketmq.namesrv
     * 3.配置文件slate.common.rocketmq.namesrv
     * 4.@EnableRocketMq(defaultNameServer = "...")
     */
    String nameServer() default "";

}
