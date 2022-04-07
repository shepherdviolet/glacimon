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

/**
 * 等同于RocketMQ的ConsumeFromWhere, 选择消息从队列头还是队列尾开始消费
 *
 * @author S.Violet
 */
public enum ConsumeWay {

    /**
     * 从队列头开始消费(默认)
     */
    CONSUME_FROM_FIRST_OFFSET,

    /**
     * 从队列尾开始消费
     */
    CONSUME_FROM_LAST_OFFSET,

    /**
     * 根据时间戳消费
     */
    CONSUME_FROM_TIMESTAMP,

}
