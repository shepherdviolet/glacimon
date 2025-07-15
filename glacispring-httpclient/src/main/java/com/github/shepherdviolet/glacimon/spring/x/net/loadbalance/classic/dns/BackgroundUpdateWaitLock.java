/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.dns;

import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundUpdateWaitLock {

    public static final BackgroundUpdateWaitLock DUMMY = new BackgroundUpdateWaitLock() {
        @Override
        public void await(long timeoutMillis) {
            throw new UnsupportedOperationException("await method is not implemented");
        }

        @Override
        public void signalAll() {
        }

        @Override
        public void enterIdle() {
        }

        @Override
        public void exitIdle() {
        }
    };

    private final Object lock = new Object();
    private final AtomicBoolean idle = new AtomicBoolean(false);

    /**
     * 等待
     */
    public void await(long timeoutMillis) {
        synchronized (lock) {
            try {
                lock.wait(timeoutMillis);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * 通知
     */
    public void signalAll() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * 进入IDLE状态
     */
    public void enterIdle() {
        idle.set(true);
    }

    /**
     * 退出IDLE状态 (如果之前idle=true, 则通知)
     */
    public void exitIdle() {
        if (idle.compareAndSet(true, false)) {
            signalAll();
        }
    }

}
