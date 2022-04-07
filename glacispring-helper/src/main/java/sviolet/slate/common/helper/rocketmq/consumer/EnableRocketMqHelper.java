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

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用RocketMQ助手:
 * 1.使用注解声明消费者
 *
 * @author S.Violet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RmqHelperSelector.class})
public @interface EnableRocketMqHelper {

    /**
     * 指定NameServer地址. 优先级最低.
     * 优先级:
     * 1.@RocketMqConcurrentConsumer(nameServer = "...") / @RocketMqOrderedConsumer(nameServer = "...")
     * 2.启动参数slate.common.rocketmq.namesrv
     * 3.配置文件slate.common.rocketmq.namesrv
     * 4.@EnableRocketMq(defaultNameServer = "...")
     */
    String defaultNameServer() default "localhost:9876";

}
