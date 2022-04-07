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

package sviolet.slate.common.model.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.concurrent.ConcurrentUtils;
import sviolet.thistle.util.conversion.DateTimeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 单服务用过期更新缓存
 *
 * @param <T> 内容类型
 * @author S.Violet
 */
public abstract class ExpirableCache <T> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, ElementWrapper<T>> map = new ConcurrentHashMap<>();
    private ReentrantLock elementWrapperLock = new ReentrantLock();

    private long doUpdateTimeout = 5000L;
    private long doCheckInterval = 500L;

    /**
     * 缓存中获取数据, 若数据不存在或已过期则阻塞线程等待更新完成后返回
     * @param key key
     * @return 数据, 可为空
     */
    public T get(String key){
        if (key == null){
            throw new RuntimeException("key is null");
        }

        long currentTime = System.currentTimeMillis();

        //get element wrapper
        ElementWrapper<T> elementWrapper = getWrapper(key);

        //update after maturity, it will blocking current thread
        if (currentTime > elementWrapper.expireTime.get()) {
            return update(key, elementWrapper, currentTime);
        }

        if (logger.isDebugEnabled()){
            logger.debug("ExpirableCache get \"" + key + "\", value:" + String.valueOf(elementWrapper.element));
        }

        //return
        return elementWrapper.element;
    }

    private ElementWrapper<T> getWrapper(String key){
        ElementWrapper<T> elementWrapper = map.get(key);
        if (elementWrapper == null) {
            try {
                elementWrapperLock.lock();
                elementWrapper = map.get(key);
                if (elementWrapper == null) {
                    elementWrapper = new ElementWrapper<>();
                    elementWrapper.key = key;
                    map.put(key, elementWrapper);
                }
            } finally {
                elementWrapperLock.unlock();
            }
        }
        return elementWrapper;
    }

    /**
     * 强制更新指定key的缓存, 会阻塞线程等待更新完毕
     * @param key key
     * @return 新值
     */
    public T forceUpdate(String key){
        if (key == null){
            throw new RuntimeException("key is null");
        }

        if (logger.isInfoEnabled()){
            logger.info("ExpirableCache force update \"" + key + "\"");
        }

        long currentTime = System.currentTimeMillis();

        ElementWrapper<T> elementWrapper = getWrapper(key);

        return update(key, elementWrapper, currentTime);
    }

    /**
     * 强制更新所有缓存, 会阻塞线程等待更新完毕
     */
    public void forceUpdateAll(){
        Map<String, ElementWrapper<T>> snap = ConcurrentUtils.getSnapShot(map);
        for (ElementWrapper<T> wrapper : snap.values()){
            forceUpdate(wrapper.key);
        }
    }

    private T update(String key, ElementWrapper<T> elementWrapper, long currentTime){
        //record update times
        int updateCount = elementWrapper.updateCounter.get();
        //only one thread can do update
        int concurrentCount = elementWrapper.updateConcurrentCounter.incrementAndGet();
        if (concurrentCount > 1){
            elementWrapper.updateConcurrentCounter.decrementAndGet();

            //waiting for update

            if (logger.isInfoEnabled()){
                logger.info("ExpirableCache waiting \"" + key + "\"");
            }

            //Wait for the update to complete until timeout
            while (updateCount == elementWrapper.updateCounter.get() &&
                    System.currentTimeMillis() < currentTime + doUpdateTimeout) {
                synchronized (elementWrapper) {
                    try {
                        elementWrapper.wait(doCheckInterval);
                    } catch (InterruptedException e) {
                        if (logger.isErrorEnabled()){
                            logger.error("ExpirableCache waiting \"" + key + "\" interrupted", e);
                        }
                        return null;
                    }
                }
            }
            //update success
            if (updateCount != elementWrapper.updateCounter.get()) {
                if (logger.isDebugEnabled()){
                    logger.debug("ExpirableCache wait \"" + key + "\" succeed, value:" + String.valueOf(elementWrapper.element));
                } else if (logger.isInfoEnabled()){
                    logger.info("ExpirableCache wait \"" + key + "\" succeed");
                }

                return elementWrapper.element;
            }

            //update fail
            if (logger.isInfoEnabled()){
                logger.info("ExpirableCache wait \"" + key + "\" failed");
            }
            return null;
        }

        //update process
        try {
            UpdateResult<T> result = onUpdate(key);

            //new element and new expire time
            elementWrapper.element = result.element;
            elementWrapper.expireTime.set(currentTime + result.expireThreshold);

            if (logger.isDebugEnabled()){
                logger.debug("ExpirableCache update \"" + key + "\" succeed, expire time " + DateTimeUtils.millisToDateTimeString(elementWrapper.expireTime.get()) + ", value:" + String.valueOf(elementWrapper.element));
            } else if (logger.isInfoEnabled()){
                logger.info("ExpirableCache update \"" + key + "\" succeed, expire time " + DateTimeUtils.millisToDateTimeString(elementWrapper.expireTime.get()));
            }
        } catch (Throwable t) {
            //update failed
            long interval = onError(key, t);
            if (interval < 0) {
                interval = 0;
            }

            //no element
            elementWrapper.element = null;
            //retry after interval
            elementWrapper.expireTime.set(currentTime + interval);

            if (logger.isDebugEnabled()){
                logger.debug("ExpirableCache update \"" + key + "\" failed, update interval " + interval, t);
            } else if (logger.isInfoEnabled()){
                logger.info("ExpirableCache update \"" + key + "\" failed, update interval " + interval);
            }
        } finally {
            elementWrapper.updateConcurrentCounter.decrementAndGet();
            elementWrapper.updateCounter.incrementAndGet();
            synchronized (elementWrapper){
                elementWrapper.notifyAll();
            }
        }

        return elementWrapper.element;
    }

    /**
     * 实现根据key更新数据的逻辑,
     * 如果更新失败, 请抛出异常(会由onError方法处理)
     * @param key key
     * @return UpdateResult
     */
    protected abstract UpdateResult<T> onUpdate(String key);

    /**
     * 处理onUpdate()方法中抛出的异常,
     * 返回值表示失败后, 下次重新尝试更新的间隔,
     * 例如: 1000ms, 表示本次更新失败后, 至少一秒钟以后才会更新数据, 一秒以内会持续返回null
     * @param key key
     * @param t 异常
     * @return 下次重试的间隔, 默认0
     */
    protected abstract long onError(String key, Throwable t);

    public long getDoUpdateTimeout() {
        return doUpdateTimeout;
    }

    /**
     * 更新超时时间
     * @param doUpdateTimeout 超时时间ms
     */
    public void setDoUpdateTimeout(long doUpdateTimeout) {
        this.doUpdateTimeout = doUpdateTimeout;
    }

    public long getDoCheckInterval() {
        return doCheckInterval;
    }

    /**
     * 检查是否更新完成的间隔时间
     * @param doCheckInterval 检查间隔时间ms
     */
    public void setDoCheckInterval(long doCheckInterval) {
        this.doCheckInterval = doCheckInterval;
    }

    private static class ElementWrapper <T> {

        private String key;
        private volatile T element;
        private AtomicLong expireTime = new AtomicLong(0L);
        private AtomicInteger updateCounter = new AtomicInteger(0);
        private AtomicInteger updateConcurrentCounter = new AtomicInteger(0);

    }

    /**
     * 更新结果
     * @param <T> 数据类型
     */
    public static class UpdateResult <T> {

        private T element;
        private long expireThreshold;

        /**
         * expireThreshold指的是数据的有效期, 单位毫秒(ms).
         * 例如: 7200 * 1000, 表示数据有效期为2个小时, 即在两个小时后, 程序会调用onUpdate()更新数据
         *
         * @param element 数据
         * @param expireThreshold 有效期ms
         */
        public UpdateResult(T element, long expireThreshold) {
            this.element = element;
            this.expireThreshold = expireThreshold;
        }
    }

}
