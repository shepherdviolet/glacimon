/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.model.bitmap;

import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>[线程安全:同步锁版]使用堆内存(HEAP)的Bitmap</p>
 *
 * <p>堆内存占用 = 64 byte + 1 byte * ( 容量 / 8 ) + 52 byte * 锁数</p>
 * <p>容量: 指的是比特数, 不是指字节数</p>
 * <p>锁数: 同步锁的数量, 越多并发性能越好, 但是要注意锁比较占内存</p>
 *
 * <p>一致性: extract/inject操作有同步锁(1), put/get/bloomAdd/bloomContains有同步锁(2), 同步锁(1)和同步锁(2)不互斥. computeWith无同步锁.</p>
 * <p>注意, extract/inject会强制赋值(一般用于初始数据导入导出), extract/inject与put/bloomAdd同时进行时不保证严格的一致性.</p>
 *
 * <p>特点: 数据占内存小, 但锁占内存较大, 适合Bitmap实例数少, 但数据量较大的场合; bit读写速度最慢, bit读写支持多线程</p>
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class SyncHeapBitmap extends HeapBitmap {

    private ReentrantLock[] locks;
    private int lockNum;

    public SyncHeapBitmap(int size) {
        this(size, 64);
    }

    public SyncHeapBitmap(byte[] data) {
        this(data, 64);
    }

    public SyncHeapBitmap(int size, int lockNum) {
        super(size);
        initLocks(lockNum);
    }

    public SyncHeapBitmap(byte[] data, int lockNum) {
        super(data);
        initLocks(lockNum);
    }

    private void initLocks(int lockNum){
        if (lockNum < 4 || lockNum > 1024) {
            throw new IllegalArgumentException("lockNum must >= 4 and <= 1024");
        }
        this.lockNum = lockNum;
        this.locks = new ReentrantLock[lockNum];
        for (int i = 0 ; i < lockNum ; i++) {
            this.locks[i] = new ReentrantLock();
        }
    }

    @Override
    protected byte dataAccess_getSlot(int slotIndex) {
        ReentrantLock lock = locks[slotIndex % lockNum];
        lock.lock();
        try {
            return super.dataAccess_getSlot(slotIndex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected boolean putBitToSlot(int slotIndex, int slotOffset, boolean value) {
        ReentrantLock lock = locks[slotIndex % lockNum];
        lock.lock();
        try {
            return super.putBitToSlot(slotIndex, slotOffset, value);
        } finally {
            lock.unlock();
        }
    }

}
