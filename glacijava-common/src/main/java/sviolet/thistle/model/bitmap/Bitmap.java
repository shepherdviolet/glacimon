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

import java.io.Closeable;

/**
 * <p>Bitmap</p>
 *
 * <p>Bitmap能够精确的判断一个元素是否在集合中存在, 但元素的主键必须为不重复的数字, 且数字有界. </p>
 *
 * @author S.Violet
 */
public interface Bitmap extends Closeable, AutoCloseable {

    /**
     * 取值(一个bit)
     * @param bitIndex 索引位置(比特位置)
     * @return true / false
     */
    boolean get(int bitIndex);

    /**
     * 存值(一个bit)
     * @param bitIndex 索引位置(比特位置)
     * @param value 值
     * @return true: 存值成功, false: 存值失败(部分实现中可能会返回false, 比如ConcurrentHeapBitmap)
     */
    boolean put(int bitIndex, boolean value);

    /**
     * 抽取(复制)全部数据
     */
    byte[] extractAll();

    /**
     * 抽取(复制)部分数据
     * @param dst 抽取的数据存放到这里
     * @param byteOffset 起始位置(注意, 这个不是比特位置, 是字节位置; 这是内部Bitmap的位置, 不是入参dst的位置, dst始终会从0位开始填充直至结束)
     */
    void extract(byte[] dst, int byteOffset);

    /**
     * 导入数据
     * @param src 需要被导入的数据
     * @param byteOffset 起始位置(注意, 这个不是比特位置, 是字节位置; 这是内部Bitmap的位置, 不是入参src的位置, src始终会从0位开始读取直至结束)
     */
    void inject(byte[] src, int byteOffset);

    /**
     * Bitmap的总比特长度(bit容量)
     */
    int size();

    /**
     * <p>将当前Bitmap和指定Bitmap(第一个参数)的每个字节按顺序通过computeFunction(第三个参数)计算, 并将结果保存到resultBitmap中(第二个参数).
     * 要求三个Bitmap容量必须一致.</p>
     *
     * <p>注意!!! 该方法非线程安全, 禁止拿三个正在变化的Bitmap做计算, 数据会出现异常, 例如: 结果中前半段是老数据, 后半段是新数据.
     * 如果resultBitmap正在变化, 还有可能出现写入失败的情况. </p>
     *
     * <p>用途示例:</p>
     * <p>两个Bitmap按位异或: bitmap1.computeWith(bitmap2, resultBitmap, (b1, b2) -> (byte) (b1 ^ b2));</p>
     *
     * @param computeWith 参与计算的Bitmap, 要求三个Bitmap容量必须一致.
     * @param resultBitmap 结果保存在这个Bitmap(数据会被覆盖), 要求三个Bitmap容量必须一致.
     * @param computeFunction 计算逻辑
     */
    void computeWith(Bitmap computeWith, Bitmap resultBitmap, ComputeFunction computeFunction);

    /**
     * 两个Bitmap进行计算的逻辑接口
     */
    interface ComputeFunction {

        /**
         * @param b1 第一个Bitmap(当前Bitmap)中的一个字节(相同位置)
         * @param b2 第二个Bitmap(方法第一个参数)中的一个字节(相同位置)
         * @return 结果, 存入resultBitmap中对应位置(方法第二个参数)
         */
        byte compute(byte b1, byte b2);

        /**
         * 两个Bitmap按位异或
         */
        static ComputeFunction XOR = new ComputeFunction() {
            @Override
            public byte compute(byte b1, byte b2) {
                return (byte) (b1 ^ b2);
            }
        };

        /**
         * 两个Bitmap按位与
         */
        static ComputeFunction AND = new ComputeFunction() {
            @Override
            public byte compute(byte b1, byte b2) {
                return (byte) (b1 & b2);
            }
        };

        static ComputeFunction OR = new ComputeFunction() {
            @Override
            public byte compute(byte b1, byte b2) {
                return (byte) (b1 | b2);
            }
        };

    }

}
