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

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * <p>[线程安全:CAS版]使用堆内存(HEAP)的Bitmap</p>
 *
 * <p>堆内存占用 = 56 byte + 4 byte * ( 容量 / 8 )</p>
 * <p>容量: 指的是比特数, 不是指字节数</p>
 *
 * <p>一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains采用CAS操作, 保证了内存可见性, put/bloomAdd使用了乐观锁,
 * 防止更新操作前的脏读, 但有极小概率写入失败(返回false). computeWith无同步锁.</p>
 * <p>注意, extract/inject会强制赋值(一般用于初始数据导入导出), extract/inject与put/bloomAdd同时进行时不保证严格的一致性.</p>
 *
 * <p>特点: 数据占内存很大(4倍)!!! bit读写速度比SyncHeapBitmap快 (但是比HeapBitmap慢), bit读写支持多线程; extract/inject性能有较大下降 (比其他版本),
 * 且extract/inject操作过程更占内存, 因为会进行数据类型转换(byte - int). </p>
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class ConcurrentHeapBitmap extends AbstractBitmap {

    private static final int RETRY_TIMES = 100;

    private AtomicIntegerArray buffer;

    public ConcurrentHeapBitmap(int size) {
        super(size);
    }

    public ConcurrentHeapBitmap(byte[] data) {
        super(data);
    }

    @Override
    protected void dataAccess_init(int slotSize) {
        buffer = new AtomicIntegerArray(slotSize);
    }

    @Override
    protected byte dataAccess_getSlot(int index) {
        return (byte) buffer.get(index);
    }

    @Override
    protected boolean dataAccess_putSlot(int index, byte newValue, byte oldValue) {
        return buffer.compareAndSet(index, oldValue, newValue);
    }

    @Override
    protected void dataAccess_extract(byte[] dst, int offset) {
        synchronized (this) {
            if (dst == null) {
                return;
            }
            for (int i = 0 ; i < dst.length ; i++) {
                dst[i] = (byte) buffer.get(offset + i);
            }
        }
    }

    @Override
    protected void dataAccess_inject(byte[] src, int offset) {
        synchronized (this) {
            if (src == null) {
                return;
            }
            for (int i = 0 ; i < src.length ; i++) {
                buffer.set(offset + i, src[i]);
            }
        }
    }

    @Override
    protected boolean putBitToSlot(int slotIndex, int slotOffset, boolean value) {
        //乐观锁
        for (int i = 0 ; i < RETRY_TIMES ; i++) {
            if (super.putBitToSlot(slotIndex, slotOffset, value)) {
                return true;
            }
        }
        return false;
    }

}
