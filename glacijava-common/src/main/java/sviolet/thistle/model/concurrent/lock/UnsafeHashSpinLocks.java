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

/**
 * 慎用!!! 不规范的使用会导致严重的问题!!!<br>
 * IT'S UNSAFE !!! Irregular use can cause serious problems !!!<br>
 *
 * 产生指定数量的同步锁, 根据字符串的哈希获取锁对象, 这样可以把同步代码块分散, 提高并发性能<br>
 *
 * <code>
 *     private UnsafeHashSpinLocks spinLocks = new UnsafeHashSpinLocks();
 *
 *     public void put(String key, Object value) {
 *         UnsafeSpinLock spinLock = spinLocks.getLock(key);
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
public class UnsafeHashSpinLocks extends AbstractHashLocks<UnsafeSpinLock> {

    public UnsafeHashSpinLocks() {
    }

    public UnsafeHashSpinLocks(int hashLockNum) {
        super(hashLockNum);
    }

    @Override
    UnsafeSpinLock[] newArray(int hashLockNum) {
        return new UnsafeSpinLock[hashLockNum];
    }

    @Override
    UnsafeSpinLock newLock() {
        return new UnsafeSpinLock();
    }

}
