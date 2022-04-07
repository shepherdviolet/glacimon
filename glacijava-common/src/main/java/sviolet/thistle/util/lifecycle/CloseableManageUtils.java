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

package sviolet.thistle.util.lifecycle;

import sviolet.thistle.util.common.CloseableUtils;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>[高级/慎用]</p>
 * <p>
 *     Closeable实例管理工具:<br>
 *     1.注册Closeable实例<br>
 *     2.将所有注册的Closeable实例销毁<br>
 * </p>
 */
public class CloseableManageUtils {

    private static final Set<Closeable> POOL = Collections.newSetFromMap(new WeakHashMap<Closeable, Boolean>());
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 注册Destroyable实例(弱引用持有)
     * @param closeable Closeable实例
     */
    public static void register(Closeable closeable){
        try {
            LOCK.lock();
            POOL.add(closeable);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 调用所有注册的Closeable实例的close()方法
     */
    public static void closeAll(){
        try {
            LOCK.lock();
            for (Closeable closeable : POOL) {
                CloseableUtils.closeQuiet(closeable);
            }
        } finally {
            LOCK.unlock();
        }
    }

}
