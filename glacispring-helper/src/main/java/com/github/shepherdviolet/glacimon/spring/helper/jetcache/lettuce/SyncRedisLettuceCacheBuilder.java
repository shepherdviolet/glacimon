/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.helper.jetcache.lettuce;

import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.MultiGetResult;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCache;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * JetCache的Lettuce方式原生是异步的, 性能好, 但对于对JetCache不了解的人, 如果拿JetCache去存一致性要求的数据时,
 * 会因为异步发生问题, 因此这个CacheBuilder牺牲性能, 将Redis操作改为同步, 依赖: com.alicp.jetcache:jetcache-starter-redis-lettuce
 *
 * @author shepherdviolet
 */
public class SyncRedisLettuceCacheBuilder extends RedisLettuceCacheBuilder<SyncRedisLettuceCacheBuilder> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static SyncRedisLettuceCacheBuilder createSyncRedisLettuceCacheBuilder() {
        return new SyncRedisLettuceCacheBuilder();
    }

    @SuppressWarnings("unchecked")
    public SyncRedisLettuceCacheBuilder() {
        buildFunc(config -> {
            RedisLettuceCacheConfig lettuceCacheConfig = (RedisLettuceCacheConfig) config;

            /*
                同步化
             */
            return new RedisLettuceCache(lettuceCacheConfig) {
                @Override
                protected CacheResult do_PUT(Object key, Object value, long expireAfterWrite, TimeUnit timeUnit) {
                    CacheResult result = super.do_PUT(key, value, expireAfterWrite, timeUnit);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce put failed: ");
                    return result;
                }

                @Override
                protected CacheResult do_PUT_ALL(Map map, long expireAfterWrite, TimeUnit timeUnit) {
                    CacheResult result = super.do_PUT_ALL(map, expireAfterWrite, timeUnit);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce put all failed: ");
                    return result;
                }

                @Override
                protected CacheGetResult do_GET(Object key) {
                    CacheGetResult result = super.do_GET(key);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce get failed: ");
                    return result;
                }

                @Override
                protected MultiGetResult do_GET_ALL(Set keys) {
                    MultiGetResult result = super.do_GET_ALL(keys);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce get all failed: ");
                    return result;
                }

                @Override
                protected CacheResult do_REMOVE(Object key) {
                    CacheResult result = super.do_REMOVE(key);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce remove failed: ");
                    return result;
                }

                @Override
                protected CacheResult do_REMOVE_ALL(Set keys) {
                    CacheResult result = super.do_REMOVE_ALL(keys);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce remove all failed: ");
                    return result;
                }

                @Override
                protected CacheResult do_PUT_IF_ABSENT(Object key, Object value, long expireAfterWrite, TimeUnit timeUnit) {
                    CacheResult result = super.do_PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
                    result.getResultCode();
                    printErrorLog(result, "Lettuce put if absent failed: ");
                    return result;
                }

                private void printErrorLog(CacheResult result, String prompt) {
                    CacheResultCode resultCode = result.getResultCode();
                    if (resultCode == CacheResultCode.FAIL) {
                        try {
                            result.future().toCompletableFuture().get(10, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | TimeoutException e) {
                            SyncRedisLettuceCacheBuilder.this.logger.warn(prompt + result.getMessage());
                        } catch (ExecutionException e) {
                            SyncRedisLettuceCacheBuilder.this.logger.warn(prompt + result.getMessage(), e.getCause());
                        }
                    }
                }
            };
        });
    }
}
