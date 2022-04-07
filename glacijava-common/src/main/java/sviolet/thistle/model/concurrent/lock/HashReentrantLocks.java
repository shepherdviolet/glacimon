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

import java.util.concurrent.locks.ReentrantLock;

/**
 * 产生指定数量的同步锁, 根据字符串的哈希获取锁对象, 这样可以把同步代码块分散, 提高并发性能
 *
 * @author S.Violet
 */
public class HashReentrantLocks extends AbstractHashLocks<ReentrantLock> {

    public HashReentrantLocks() {
    }

    public HashReentrantLocks(int hashLockNum) {
        super(hashLockNum);
    }

    @Override
    ReentrantLock[] newArray(int hashLockNum) {
        return new ReentrantLock[hashLockNum];
    }

    @Override
    ReentrantLock newLock() {
        return new ReentrantLock();
    }

}
