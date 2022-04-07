/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.slate.common.helper.jedis.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import sviolet.thistle.util.judge.CheckUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * <p>单节点Redis分布式重入锁, 依赖redis.clients:jedis</p>
 *
 * <p>注意:<br>
 * 1.该锁适合单节点Redis, 集群Redis请使用RedLock.<br>
 * 2.Redis挂掉时, 获取/释放锁, 会抛出SingleRedisLock.LockException, 或超时.<br>
 * 3.获取锁时, expireSeconds参数非常重要. 该参数决定了redis中key的有效期, 相当于锁的有效期. 一旦锁内事务执行时间超过了该设定值,
 * 就会有其他进程同时获得该锁. 因此, 该分布式锁适用于非耗时操作, expireSeconds应设置为较大的值, 避免出现超时的情况. 同时expireSeconds
 * 也不能设置的太大, 持有锁的进程意外终止时, 锁要等到过期才能释放给其他进程.</p>
 *
 * <pre>
 *      //实例化jedis
 *      <code>@Bean</code>
 *      JedisPool jedisPool(){
 *          JedisPoolConfig config = new JedisPoolConfig();
 *          config.setMaxTotal(5);
 *          config.setMaxIdle(2);
 *          config.setMinIdle(1);
 *          config.setTestOnBorrow(true);
 *          return new JedisPool(config, "127.0.0.1", 6379, 3000, null);
 *      }
 *      //实例化SingleRedisLock
 *      <code>@Bean</code>
 *      public SingleRedisLock simpleRedisLock(JedisPool jedisPool){
 *          return new SingleRedisLock(jedisPool);
 *      }
 * </pre>
 *
 * <pre>
 *      try {
 *          //获取锁, 锁名1, 有效期60s, 等待时间1s, 重试时间0.01s
 *          singleRedisLock.lock("1", 60, 1000L, 10L);
 *
 *          //处理非耗时事务
 *
 *      } catch (SingleRedisLock.TimeoutException e) {
 *          //处理锁获取超时
 *      } finally {
 *          //重要: 释放锁
 *          singleRedisLock.unLock("1");
 *      }
 * </pre>
 *
 * @author S.Violet
 */
public class SingleRedisLock {

    private static final String KEY_PREFIX = "Khaki-SingleRedisLock-";
    private static final String SCRIPT_LOCK = "local token = redis.call('get', KEYS[1]); if token == false then redis.call('set', KEYS[1], KEYS[2]); redis.call('expire', KEYS[1], KEYS[3]); return 1; else if token == KEYS[2] then redis.call('expire', KEYS[1], KEYS[3]); return 1; else return 0; end end";
    private static final String SCRIPT_UNLOCK = "if redis.call('get', KEYS[1]) == KEYS[2] then return redis.call('del', KEYS[1]); else return 0; end";
    private static final Long SUCCEED_FLAG = 1L;

    private final List<String> emptyArgv2 = Arrays.asList("", "");
    private final List<String> emptyArgv3 = Arrays.asList("", "", "");
    private final ThreadLocal<String> tokens = new ThreadLocal<>();

    private JedisPool jedisPool;

    public SingleRedisLock() {
    }

    public SingleRedisLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 获得锁, 获取失败会阻塞线程直到超时(waitTimeoutMillis), 重试间隔为checkIntervalMillis
     * @param key 锁名称
     * @param expireSeconds 锁有效期, 单位:秒. expireSeconds参数非常重要. 该参数决定了redis中key的有效期, 相当于锁的有效期.
     *                     一旦锁内事务执行时间超过了该设定值, 就会有其他进程同时获得该锁. 因此, 该分布式锁适用于非耗时操作,
     *                      expireSeconds应设置为较大的值, 避免出现超时的情况. 同时expireSeconds也不能设置的太大, 持有锁的
     *                      进程意外终止时, 锁要等到过期才能释放给其他进程.
     * @param waitTimeoutMillis 最大等待时间, 单位:毫秒. 获取失败会阻塞线程直到超时.
     * @param checkIntervalMillis 获取失败重试间隔, 单位:毫秒.
     * @throws TimeoutException 锁获取超时
     */
    public void lock(String key, int expireSeconds, long waitTimeoutMillis, long checkIntervalMillis) throws TimeoutException {
        if (waitTimeoutMillis <= 0) {
            throw new IllegalArgumentException("waitTimeout must > 0");
        }
        if (checkIntervalMillis <= 0 || checkIntervalMillis >= waitTimeoutMillis) {
            throw new IllegalArgumentException("checkInterval must > 0 and < waitTimeout");
        }
        long deadLine = System.currentTimeMillis() + waitTimeoutMillis;
        long leftTime;
        while((leftTime = deadLine - System.currentTimeMillis()) > 0) {
            if (tryLock(key, expireSeconds)) {
                return;
            }
            try {
                Thread.sleep(Math.min(checkIntervalMillis, leftTime));
            } catch (InterruptedException e) {
                throw new TimeoutException("Interrupted by signal", e);
            }
        }
        throw new TimeoutException("Try lock timeout, waitTimeout:" + waitTimeoutMillis);
    }

    /**
     * 尝试获得锁, 获取失败立即返回false
     * @param key 锁名称
     * @param expireSeconds 锁有效期, 单位:秒. expireSeconds参数非常重要. 该参数决定了redis中key的有效期, 相当于锁的有效期.
     *                     一旦锁内事务执行时间超过了该设定值, 就会有其他进程同时获得该锁. 因此, 该分布式锁适用于非耗时操作,
     *                      expireSeconds应设置为较大的值, 避免出现超时的情况. 同时expireSeconds也不能设置的太大, 持有锁的
     *                      进程意外终止时, 锁要等到过期才能释放给其他进程.
     * @return 获取失败立即返回false
     */
    public boolean tryLock(String key, int expireSeconds) {
        check(key);
        if (expireSeconds <= 0) {
            throw new IllegalArgumentException("expireSeconds must > 0");
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return SUCCEED_FLAG.equals(jedis.eval(SCRIPT_LOCK, Arrays.asList(KEY_PREFIX + key, getToken(), Integer.toString(expireSeconds)), emptyArgv3));
        } catch (Throwable t) {
            throw new LockException(t);
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Throwable ignore){
                }
            }
        }
    }

    /**
     * 释放锁
     * @param key 锁名称
     * @return 通常忽略返回值, 返回false表示解锁失败(锁已过期, 或过期后已被其他进程获得)
     */
    public boolean unLock(String key) {
        check(key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return SUCCEED_FLAG.equals(jedis.eval(SCRIPT_UNLOCK, Arrays.asList(KEY_PREFIX + key, getToken()), emptyArgv2));
        } catch (Throwable t) {
            throw new LockException(t);
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Throwable ignore){
                }
            }
        }
    }

    private void check(String key) {
        if (jedisPool == null) {
            throw new RuntimeException("jedisPool instance is null");
        }
        if (CheckUtils.isEmptyOrBlank(key)) {
            throw new IllegalArgumentException("key is null or empty");
        }
    }

    private String getToken() {
        String token = tokens.get();
        if (token == null) {
            token = genToken();
            tokens.set(token);
        }
        return token;
    }

    protected String genToken(){
        return UUID.randomUUID().toString();
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 调用jedis访问redis时的异常
     */
    public static class LockException extends RuntimeException {
        private static final long serialVersionUID = 9044380487813963206L;
        public LockException(Throwable cause) {
            super("Error while SingleRedisLock locking / unlocking", cause);
        }
    }

    /**
     * lock等待超时
     */
    public static class TimeoutException extends Exception {
        private static final long serialVersionUID = 4075952941701103494L;
        public TimeoutException(String message) {
            super(message);
        }
        public TimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
