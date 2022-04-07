/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.slate.common.helper.lettuce;

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

/**
 * 一个在Spring中使用的简单的Lettuce Redis客户端, 连接Redis Cluster集群(或单机).
 *
 * Lettuce用法请参考官方文档, Connection和Commands都是线程安全的, Connection无需手动关闭(保持连接).
 *
 * @author shepherdviolet
 */
public interface SpringLettuceClusterClient <K, V> {

    /**
     * 获取单例的Redis同步Command (线程安全)
     */
    RedisClusterCommands<K, V> syncCommands();

    /**
     * 获取单例的Redis异步Command (线程安全)
     */
    RedisClusterAsyncCommands<K, V> asyncCommands();

    /**
     * 获取单例的Redis连接 (线程安全), 请勿手动关闭连接
     */
    StatefulConnection<K, V> getConnection();

}
