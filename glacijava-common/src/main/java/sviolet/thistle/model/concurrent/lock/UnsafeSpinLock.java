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

package sviolet.thistle.model.concurrent.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 慎用!!! 不规范的使用会导致严重的问题!!!<br>
 * IT'S UNSAFE !!! Irregular use can cause serious problems !!!<br>
 *
 * 简易自旋锁, 未提供任何保护措施, 需要仔细阅读源码并谨慎使用.<br>
 *
 * <code>
 *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
 *
 *     public void setXXX(int xxx) {
 *         try {
 *             spinLock.lock();
 *             //do something ...
 *         } finally {
 *             spinLock.unlock();
 *         }
 *     }
 * </code>
 *
 * @author S.Violet
 */
public class UnsafeSpinLock implements Lock {

    private AtomicBoolean lock = new AtomicBoolean(false);
    private Thread acquiredThread;

    /**
     * Lock, the thread will be blocked. <br>
     * IT'S UNSAFE !!! Irregular use can cause serious problems !!!<br>
     *
     * <code>
     *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
     *
     *     public void setXXX(int xxx) {
     *         try {
     *             spinLock.lock();
     *             //do something ...
     *         } finally {
     *             spinLock.unlock();
     *         }
     *     }
     * </code>
     *
     */
    @Override
    public void lock(){
        while (true) {
            if (tryLock()) {
                break;
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * Unlock <br>
     * IT'S UNSAFE !!! Irregular use can cause serious problems !!!<br>
     *
     * <code>
     *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
     *
     *     public void setXXX(int xxx) {
     *         try {
     *             spinLock.lock();
     *             //do something ...
     *         } finally {
     *             spinLock.unlock();
     *         }
     *     }
     * </code>
     *
     */
    @Override
    public void unlock(){
        if (Thread.currentThread() != this.acquiredThread) {
            throw new IllegalStateException("The lock is not acquired by this thread");
        }
        this.acquiredThread = null;
        lock.set(false);
    }

    /**
     * Try to lock, return true if acquired successfully, return false if failed immediately, no blocking <br>
     *
     * <code>
     *      private UnsafeSpinLock spinLock = new UnsafeSpinLock();
     *
     *      public void setXXX(int xxx) {
     *          if (spinLock.lock()) {
     *              try {
     *                  //do something ...
     *              } finally {
     *                  spinLock.unlock();
     *              }
     *          }
     *      }
     * </code>
     *
     * @return true: Acquired the lock successfully
     */
    @Override
    public boolean tryLock() {
        if (lock.compareAndSet(false, true)) {
            acquiredThread = Thread.currentThread();
            return true;
        }
        return false;
    }

    /**
     * Deprecated!!
     * Lock, the thread will be blocked until acquired or interrupted <br>
     * IT'S UNSAFE !!! Irregular use can cause serious problems !!!<br>
     *
     * <code>
     *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
     *
     *      public void setXXX(int xxx) {
     *          try {
     *              spinLock.lockInterruptibly();
     *          } catch (InterruptedException e) {
     *              //interrupted
     *              return;
     *          }
     *          try {
     *              //do something ...
     *          } finally {
     *              spinLock.unlock();
     *          }
     *      }
     * </code>
     *
     * @throws InterruptedException Thread interrupted
     * @deprecated Although it is implemented, but it is not recommended to use
     */
    @Override
    @Deprecated
    public void lockInterruptibly() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            if (tryLock()) {
                break;
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * @deprecated Unsupported Operation
     */
    @Override
    @Deprecated
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("tryLock method is unsupported in UnsafeSpinLock");
    }

    /**
     * @deprecated Unsupported Operation
     */
    @Override
    @Deprecated
    public Condition newCondition() {
        throw new UnsupportedOperationException("newCondition method is unsupported in UnsafeSpinLock");
    }

}
