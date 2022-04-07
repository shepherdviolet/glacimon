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
 * <p>Bloom Bitmap</p>
 *
 * <p>注意, 如果作为布隆过滤器使用, 建议不要使用Bitmap#put方法直接对数据进行操作!</p>
 *
 * <p>另外, Redis4.0+ 和 lettuce 支持布隆过滤器. </p>
 *
 * <p>
 *     要点:<br>
 *     1.元素只增不减(很难实现删除元素)<br>
 *     2.存在误判率, 如果返回true表示大概率存在, 返回false表示一定不存在<br>
 *     3.增加哈希算法数量(默认3个)可以减少误判率<br>
 *     4.增大Bitmap容量(size)可以减少误判率, 最大建议1000000000(10亿), 约占120M内存<br>
 *     5.要对可能塞入的元素数量做预计, 已塞入的元素数量对误判率影响非常大<br>
 * </p>
 *
 * <p>
 *     误判率(参考他人文档):<br>
 *     容量: 10亿(120Mb)  元素数: 1000万  HASH算法数: 3  误判率: < 0.0001<br>
 *     容量:   1亿(12Mb)  元素数: 1000万  HASH算法数: 3  误判率: > 0.01<br>
 * </p>
 *
 * <p>
 *     误判率(实测):<br>
 *     容量: 10亿(120Mb)  元素数: 1000万  HASH算法数: 3  误判率: < 0.00001<br>
 *     容量:   1亿(12Mb)  元素数: 1000万  HASH算法数: 3  误判率: < 0.005<br>
 *     容量:   1亿(12Mb)  元素数:  100万  HASH算法数: 3  误判率: < 0.00001<br>
 *     容量: 1000万(1Mb)  元素数:  100万  HASH算法数: 3  误判率: < 0.005<br>
 * </p>
 *
 * @author S.Violet
 */
public interface BloomBitmap extends Bitmap {

    /**
     * 将一个数据添加到布隆过滤器
     *
     * @param data 数据
     */
    void bloomAdd(byte[] data);

    /**
     * 检查一个数据是否在布隆过滤器中存在
     *
     * @param data 数据
     * @return true:存在(可能存在) false:不存在(一定不存在)
     */
    boolean bloomContains(byte[] data);

}
