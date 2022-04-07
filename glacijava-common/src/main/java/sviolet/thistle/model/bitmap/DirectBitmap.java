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

import sviolet.thistle.util.common.CloseableUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <p>[非线程安全]使用直接内存的Bitmap</p>
 *
 * <p>注意!!! 这个类请谨慎使用, 内存分配在堆外, 小心内存泄露!!!</p>
 * <p>注意!!! 使用完后请调用close()方法回收内存!!!</p>
 *
 * <p>堆内存占用 = 24 byte</p>
 * <p>堆外内存占用 = 1 byte * ( 容量 / 8 )</p>
 * <p>容量: 指的是比特数, 不是指字节数</p>
 *
 * <p>一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains/computeWith无同步锁, 且不保证内存可见性(非CAS操作).</p>
 *
 * <p>特点: 数据放在堆外, 用于特殊场景; 内存占用小, bit读写速度快, bit读写不支持多线程</p>
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class DirectBitmap extends AbstractBitmap {

    //Direct buffer
    private ByteBuffer buffer;

    public DirectBitmap(int size) {
        super(size);
    }

    public DirectBitmap(byte[] data) {
        super(data);
    }

    @Override
    protected void dataAccess_init(int slotSize) {
        buffer = ByteBuffer.allocateDirect(slotSize);
    }

    @Override
    protected byte dataAccess_getSlot(int index) {
        return buffer.get(index);
    }

    @Override
    protected boolean dataAccess_putSlot(int index, byte newValue, byte oldValue) {
        buffer.put(index, newValue);
        return true;
    }

    @Override
    protected void dataAccess_extract(byte[] dst, int offset) {
        synchronized (this) {
            buffer.position(offset);
            buffer.get(dst, 0, dst.length);
        }
    }

    @Override
    protected void dataAccess_inject(byte[] src, int offset) {
        synchronized (this) {
            buffer.position(offset);
            buffer.put(src, 0, src.length);
        }
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.cleanMappedByteBuffer(buffer);
    }

}
