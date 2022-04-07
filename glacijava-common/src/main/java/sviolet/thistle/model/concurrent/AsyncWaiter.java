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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.model.concurrent;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.entity.common.Destroyable;
import sviolet.thistle.util.common.CloseableUtils;

/**
 * 挂起当前线程等待异步线程的结果
 *
 * @author S.Violet
 */
public class AsyncWaiter <T> implements Closeable, Destroyable {

    /**
     * 超时时间ms
     */
    private long timeout = 0;

    private Result result;
    private T value;
    private Exception exception;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * 无限等待
     */
    public AsyncWaiter() {
        this(0);
    }

    /**
     * @param timeout 超时时间(ms), 该参数<=0时无限等待
     */
    public AsyncWaiter(long timeout) {
        this.timeout = timeout;
    }

    /**
     * [异步线程]回调传回结果
     *
     * @param value 结果对象
     */
    public void callback(T value){
        lock.lock();
        try{
            //can only be called once
            if (result != null) {
                //destroy data
                if (value instanceof Destroyable){
                    ((Destroyable) value).onDestroy();
                }
                return;
            }
            this.value = value;
            this.result = Result.SUCCESS;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * [异步线程]回调传回异常
     *
     * @param e 异常
     */
    public void callback(Exception e){
        lock.lock();
        try{
            //can only be called once
            if (result != null) {
                return;
            }
            this.exception = e;
            this.result = Result.ERROR;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * [原线程]获得结果对象
     */
    public T getValue(){
        return value;
    }

    /**
     * [原线程]获得异常
     */
    public Exception getException(){
        if (exception == null){
            return new Exception("empty exception from async task");
        }
        return exception;
    }

    /**
     * [原线程]挂起当前线程等待异步线程的结果
     * @return 结果类型
     */
    public Result waitForResult(){
        final long startMillis = System.currentTimeMillis();
        try{
            lock.lock();
            if (timeout > 0) {
                while (result == null) {
                    final long remainTimeout = timeout - (System.currentTimeMillis() - startMillis);
                    if (remainTimeout <= 0) {
                        result = Result.TIMEOUT;
                        break;
                    }
                    try {
                        if (!condition.await(remainTimeout, TimeUnit.MILLISECONDS)) {
                            result = Result.TIMEOUT;
                            break;
                        }
                    } catch (InterruptedException e) {
                        result = Result.ERROR;
                        exception = e;
                        break;
                    }
                }
            } else {
                while (result == null){
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        result = Result.ERROR;
                        exception = e;
                        break;
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onDestroy() {
        try {
            close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.closeIfCloseable(this.value);
        this.value = null;
    }

    public enum Result{
        /**
         * SUCCESS
         */
        SUCCESS,

        /**
         * TIMEOUT
         */
        TIMEOUT,

        /**
         * ERROR
         */
        ERROR
    }

}
