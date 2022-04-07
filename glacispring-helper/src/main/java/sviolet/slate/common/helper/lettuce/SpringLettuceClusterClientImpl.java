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

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.codec.RedisCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.judge.CheckUtils;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 一个在Spring中使用的简单的Lettuce Redis客户端, 连接Redis Cluster集群(或单机).
 *
 * Lettuce用法请参考官方文档, Connection和Commands都是线程安全的, Connection无需手动关闭(保持连接).
 *
 * <pre>
 *     <code>@Bean</code>
 *     public SpringLettuceClusterClient<String, String> lettuceRedisClusterClient() {
 *         // "redis://192.168.1.1:6379"
 *         // "redis://password@192.168.1.1:6379"
 *         // "redis://192.168.1.1:6379,redis://192.168.1.2:6379,redis://192.168.1.3:6379"
 *         return new SpringLettuceClusterClientImpl<>("redis://127.0.0.1:6379", StringCodec.UTF8);
 *     }
 * </pre>
 *
 * @author shepherdviolet
 */
public class SpringLettuceClusterClientImpl<K, V> implements SpringLettuceClusterClient<K, V>, InitializingBean, DisposableBean, BeanNameAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String beanName = "unknown";

    private AbstractRedisClient redisClient;
    private StatefulConnection<K, V> statefulConnection;
    private RedisClusterCommands<K, V> syncCommand;
    private RedisClusterAsyncCommands<K, V> asyncCommand;

    private String uris;
    private long defaultTimeoutMillis = -1L;
    private RedisCodec<K, V> codec;
    private RedisConnectionStateListener redisConnectionStateListener;

    /**
     * uris and codec is required !
     */
    public SpringLettuceClusterClientImpl() {
    }

    /**
     * @param uris "redis://192.168.1.1:6379"
     *             "redis://password@192.168.1.1:6379"
     *             "redis://192.168.1.1:6379,redis://192.168.1.2:6379,redis://192.168.1.3:6379"
     * @param codec StringCodec.UTF8 / ByteArrayCodec.INSTANCE
     */
    public SpringLettuceClusterClientImpl(String uris, RedisCodec<K, V> codec) {
        setUris(uris);
        setCodec(codec);
    }

    /**
     * 获取单例的Redis连接 (线程安全), 请勿手动关闭连接
     */
    public StatefulConnection<K, V> getConnection(){
        return statefulConnection;
    }

    /**
     * 获取单例的Redis同步Command (线程安全)
     */
    public RedisClusterCommands<K, V> syncCommands() {
        return syncCommand;
    }

    /**
     * 获取单例的Redis异步Command (线程安全)
     */
    public RedisClusterAsyncCommands<K, V> asyncCommands() {
        return asyncCommand;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (codec == null) {
            throw new IllegalArgumentException("SpringLettuceRedisClient " + beanName + " | Property 'codec' is required, but it's null");
        }
        redisClient = createClient(parseUri(uris));
        if (redisClient == null) {
            throw new IllegalStateException("SpringLettuceRedisClient " + beanName + " | Create redis client failed, return null");
        }
        if (defaultTimeoutMillis > 0) {
            redisClient.setDefaultTimeout(Duration.ofMillis(defaultTimeoutMillis));
        }
        if (redisConnectionStateListener != null) {
            redisClient.addListener(redisConnectionStateListener);
        }
        /*
         * 为了统一 单机Redis 和 Cluster Redis
         * Command统一使用Cluster系列的 (Cluster系列的API比单机的少)
         */
        if (redisClient instanceof RedisClient) {
            statefulConnection = ((RedisClient) redisClient).connect(codec);
            syncCommand = ((StatefulRedisConnection<K, V>) statefulConnection).sync();
            asyncCommand = ((StatefulRedisConnection<K, V>) statefulConnection).async();
        } else if (redisClient instanceof RedisClusterClient) {
            statefulConnection = ((RedisClusterClient) redisClient).connect(codec);
            syncCommand = ((StatefulRedisClusterConnection<K, V>) statefulConnection).sync();
            asyncCommand = ((StatefulRedisClusterConnection<K, V>) statefulConnection).async();
        } else {
            throw new IllegalStateException("SpringLettuceRedisClient " + beanName + " | Illegal redisClient type " + redisClient.getClass().getName());
        }
    }

    protected AbstractRedisClient createClient(List<RedisURI> uriList) {
        if (uriList.size() == 1) {
            RedisClient client = RedisClient.create(uriList.get(0));
            client.setOptions(ClientOptions.builder()
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .build());
            return client;
        } else {
            /*
             * 避免Redis集群拓扑变化时报出错误: Connection to ?:? not allowed. This connection point is not known in the cluster view
             * 开启集群拓扑刷新(topologyRefreshOptions): 当服务端拓扑发生变化时, 短时间内还会出现连接错误, 刷新后才恢复
             * 关闭集群节点验证(validateClusterNodeMembership): 当服务端拓扑发生变化时, 由于不验证, 能够更快恢复(但是, 官方默认开启验证, 应该是有某种原因的, 所以这里默认不用这个方案)
             */
            RedisClusterClient client = RedisClusterClient.create(uriList);
            client.setOptions(ClusterClientOptions.builder()
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                            .enablePeriodicRefresh()//开启定期刷新, 默认关闭
                            .refreshPeriod(Duration.ofMinutes(1))//默认1分钟
                            .enableAdaptiveRefreshTrigger()//开启拓扑刷新, 默认关闭
                            .enableAllAdaptiveRefreshTriggers()//启用全部拓扑刷新定时器
                            .build())
//                    .validateClusterNodeMembership(false)//直接关闭集群节点检查, 默认true(避免Redis集群拓扑变化时报出错误: Connection to ?:? not allowed. This connection point is not known in the cluster view)
                    .build());
            return client;
        }
    }

    private List<RedisURI> parseUri(String uris){
        logger.info("SpringLettuceRedisClient " + beanName + " | Connection URIs: " + uris);
        if (CheckUtils.isEmpty(uris)) {
            throw new IllegalArgumentException("SpringLettuceRedisClient " + beanName + " | Connection URIs is null or empty");
        }
        List<String> uriList = Stream.of(uris.split(","))
                .map(String::trim)
                .filter(CheckUtils::notEmpty)
                .collect(Collectors.toList());
        logger.info("SpringLettuceRedisClient " + beanName + " | Connection URI List: " + uriList);
        return uriList.stream()
                .map(u -> RedisURI.create(URI.create(u)))
                .collect(Collectors.toList());
    }

    @Override
    public void destroy() throws Exception {
        CloseableUtils.closeQuiet(statefulConnection);

        if (redisClient != null) {
            try {
                redisClient.shutdown();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * @param uris "redis://192.168.1.1:6379"
     *             "redis://password@192.168.1.1:6379"
     *             "redis://192.168.1.1:6379,redis://192.168.1.2:6379,redis://192.168.1.3:6379"
     */
    public void setUris(String uris) {
        this.uris = uris;
    }

    /**
     * @param codec StringCodec.UTF8 / ByteArrayCodec.INSTANCE
     */
    public void setCodec(RedisCodec<K, V> codec) {
        this.codec = codec;
    }

    public void setDefaultTimeoutMillis(long defaultTimeoutMillis) {
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

    public void setRedisConnectionStateListener(RedisConnectionStateListener redisConnectionStateListener) {
        this.redisConnectionStateListener = redisConnectionStateListener;
    }

}
