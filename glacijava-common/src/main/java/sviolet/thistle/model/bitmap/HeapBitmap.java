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

/**
 * <p>[非线程安全]使用堆内存(HEAP)的Bitmap</p>
 *
 * <p>堆内存占用 = 40 byte + 1 byte * ( 容量 / 8 )</p>
 * <p>容量: 指的是比特数, 不是指字节数</p>
 *
 * <p>一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains/computeWith无同步锁, 且不保证内存可见性(非CAS操作).</p>
 *
 * <p>特点: 内存占用小, bit读写速度快, bit读写不支持多线程</p>
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class HeapBitmap extends AbstractBitmap {

    //Heap buffer
    private byte[] data;

    public HeapBitmap(int size) {
        super(size);
    }

    public HeapBitmap(byte[] data) {
        super(data);
    }

    @Override
    protected void dataAccess_init(int slotSize) {
        data = new byte[slotSize];
    }

    @Override
    protected byte dataAccess_getSlot(int index) {
        return data[index];
    }

    @Override
    protected boolean dataAccess_putSlot(int index, byte newValue, byte oldValue) {
        data[index] = newValue;
        return true;
    }

    @Override
    protected void dataAccess_extract(byte[] dst, int offset) {
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset < 0");
        }
        if (offset >= data.length) {
            throw new ArrayIndexOutOfBoundsException("offset >= max " + data.length);
        }
        if (offset + dst.length > data.length) {
            throw new ArrayIndexOutOfBoundsException("offset + dst.length > max " + data.length);
        }
        synchronized (this) {
            System.arraycopy(data, offset, dst, 0, dst.length);
        }
    }

    @Override
    protected void dataAccess_inject(byte[] src, int offset) {
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset < 0");
        }
        if (offset >= data.length) {
            throw new ArrayIndexOutOfBoundsException("offset >= max " + data.length);
        }
        if (offset + src.length > data.length) {
            throw new ArrayIndexOutOfBoundsException("offset + src.length > max " + data.length);
        }
        synchronized (this) {
            System.arraycopy(src, 0, data, offset, src.length);
        }
    }

}
