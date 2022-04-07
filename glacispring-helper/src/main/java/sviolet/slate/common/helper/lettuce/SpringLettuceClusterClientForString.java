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

import io.lettuce.core.codec.StringCodec;

/**
 * 一个在Spring中使用的简单的Lettuce Redis客户端, 连接Redis Cluster集群(或单机)
 *
 * <pre>
 *     <code>@Bean</code>
 *     public SpringLettuceClusterClient<String, String> lettuceRedisClusterClient() {
 *         // "redis://192.168.1.1:6379"
 *         // "redis://password@192.168.1.1:6379"
 *         // "redis://192.168.1.1:6379,redis://192.168.1.2:6379,redis://192.168.1.3:6379"
 *         return new SpringLettuceClusterClientForString("redis://127.0.0.1:6379");
 *     }
 * </pre>
 *
 * @author shepherdviolet
 */
public class SpringLettuceClusterClientForString extends SpringLettuceClusterClientImpl<String, String> {

    /**
     * uris is required !
     */
    public SpringLettuceClusterClientForString() {
        setCodec(StringCodec.UTF8);
    }

    /**
     * @param uris "redis://192.168.1.1:6379"
     *             "redis://password@192.168.1.1:6379"
     *             "redis://192.168.1.1:6379,redis://192.168.1.2:6379,redis://192.168.1.3:6379"
     */
    public SpringLettuceClusterClientForString(String uris) {
        super(uris, StringCodec.UTF8);
    }

}
