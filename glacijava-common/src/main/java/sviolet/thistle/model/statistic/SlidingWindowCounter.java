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

package sviolet.thistle.model.statistic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>滑动窗口计数器</p>
 *
 * <p>计数, 并统计一定时间内的计数值, 数值不精确, 存在一定的近似计算. </p>
 *
 * <p>例如, 我们要统计最近5分钟内的交易量, 统计精度为10秒, 即samplingDuration=10000, statisticalPeriod=30,
 * (5分钟 = 10秒 * 30, 实际上程序生成了30个计数器, 每个计数器用来累加10秒钟的计数, 这样就能统计最近十分钟的计数值了).
 * 在交易发生时, 调用getAndAdd(1)/addAndGet(1)方法计数. 在需要获取计数值时, 调用get(?)获取最近一段时间内的计数值,
 * getRecently(10000)获取最近10秒钟内的计数值, getRecently(60000)获取最近一分钟内的计数值, getTotally()获取最近五分钟内的计数值. </p>
 *
 * <p>
 *     建议: <br>
 *     1.statisticalPeriod不要设置的太大, 这个值有多少, 内部就有多少个计数器 <br>
 *     2.如果只需要统计一种时间尺度, 比如最近10秒, 建议: new SlidingWindowCounter(10000, 4), getRecently(10000) <br>
 *     3.如果需要统计多种时间尺度, 比如最近10秒和最近5分钟, 建议: new SlidingWindowCounter(10000, 30), getRecently(10000), getTotally() <br>
 * </p>
 *
 * @author S.Violet
 */
public class SlidingWindowCounter {

    private final SlidingWindowArray<AtomicInteger> slidingWindowArray;

    /**
     * @param samplingDuration 采样时长, ms, 取值范围 >= 10
     * @param statisticalPeriod 统计周期, 取值范围 > 0, 统计时长 = 采样时长 * 统计周期
     */
    public SlidingWindowCounter(int samplingDuration, int statisticalPeriod) {
        this(samplingDuration, statisticalPeriod, SlidingWindowArray.DEFAULT_TIME_REVERSE_THRESHOLD);
    }

    /**
     * @param samplingDuration 采样时长, ms, 取值范围 >= 10
     * @param statisticalPeriod 统计周期, 取值范围 > 0, 统计时长 = 采样时长 * 统计周期
     * @param timeReverseThreshold 当时间倒流的情况超过该设定值, 会重置所有统计数据, 默认64, 用于应对服务器时间重设的情况
     */
    public SlidingWindowCounter(int samplingDuration, int statisticalPeriod, int timeReverseThreshold) {
        this.slidingWindowArray = new SlidingWindowArray<>(statisticalPeriod, samplingDuration, timeReverseThreshold, new SlidingWindowArray.ElementOperator<AtomicInteger>() {
            @Override
            public AtomicInteger reset(AtomicInteger element) {
                //create
                if (element == null) {
                    return new AtomicInteger(0);
                }
                //reset
                element.set(0);
                return element;
            }
        });
    }

    /**
     * 先取值后做加法
     * @param delta 增加的数字
     * @return 计算前的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    public int getAndAdd(int delta){
        return getAndAdd(delta, System.currentTimeMillis());
    }

    /**
     * 先做加法后取值
     * @param delta 增加的数字
     * @return 计算后的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    public int addAndGet(int delta) {
        return addAndGet(delta, System.currentTimeMillis());
    }

    /**
     * 先取值后做加法
     * @param delta 增加的数字
     * @param currentTimeMillis 当前时间(毫秒数), 注意, 这个时间不可以回拨, 回拨超过2个单位时间时会触发所有计数清零
     * @return 计算前的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    protected int getAndAdd(int delta, long currentTimeMillis){
        return slidingWindowArray.getElement(currentTimeMillis).getAndAdd(delta);
    }

    /**
     * 先做加法后取值
     * @param delta 增加的数字
     * @param currentTimeMillis 当前时间(毫秒数), 注意, 这个时间不可以回拨, 回拨超过2个单位时间时会触发所有计数清零
     * @return 计算后的数值(当前采样时长内的计数值, 不是整个统计周期内的计数值)
     */
    protected int addAndGet(int delta, long currentTimeMillis) {
        return slidingWindowArray.getElement(currentTimeMillis).addAndGet(delta);
    }

    /**
     * 获取最近一段时间内的计数值, 数值不精确, 存在一定的近似计算.
     * @param duration 时间
     * @return 最近一段时间内的计数值
     */
    public int getRecently(int duration) {
        return getRecently(duration, System.currentTimeMillis());
    }

    /**
     * 获取整个统计周期内的计数值, 数值不精确, 存在一定的近似计算.
     * @return 整个统计周期内的计数值
     */
    public int getTotally(){
        return getRecently(Integer.MAX_VALUE, System.currentTimeMillis());
    }

    /**
     * 获取最近一段时间内的计数值, 数值不精确, 存在一定的近似计算.
     * @param duration 时间
     * @param currentTimeMillis 当前时间(毫秒数), 注意, 这个时间不可以回拨, 回拨超过2个单位时间时会触发所有计数清零
     * @return 最近一段时间内的计数值
     */
    protected int getRecently(int duration, long currentTimeMillis) {
        //获取指定时间范围的统计信息
        List<SlidingWindowArray.Element<AtomicInteger>> elements = slidingWindowArray.getElementsAccurately(currentTimeMillis, duration);
        printDebugLog(duration, currentTimeMillis, elements);

        //数值累加
        int result = 0;
        for (SlidingWindowArray.Element<AtomicInteger> element : elements) {
            if (element.getWeight() < 1.0f) {
                //数值 * 权重 = 近似结果
                result += Math.round((float) element.getValue().get() * element.getWeight());
            } else {
                result += element.getValue().get();
            }
        }

        return result;
    }

    protected void printDebugLog(int duration, long currentTimeMillis, List<SlidingWindowArray.Element<AtomicInteger>> elements) {
        //override to print debug log
    }

}
