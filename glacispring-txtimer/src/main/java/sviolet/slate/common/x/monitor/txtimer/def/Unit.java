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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.monitor.txtimer.def;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider2.MINUTE_MILLIS;

class Unit {

    private DefaultTxTimerProvider2 provider;

    //开始时间
    AtomicLong startTime = new AtomicLong(0);
    //时间商数(重要), 1.确定统计单元的版本, 2.判断是否需要翻篇, 3.判断是否更新, 4.竞争该值获得重置权
    AtomicLong timeQuotient = new AtomicLong(0);
    //完成计数
    AtomicInteger finishCount = new AtomicInteger(0);
    //总耗时
    AtomicLong totalElapse = new AtomicLong(0);
    //最大耗时
    AtomicLong maxElapse = new AtomicLong(Long.MIN_VALUE);
    //最小耗时
    AtomicLong minElapse = new AtomicLong(Long.MAX_VALUE);

    Unit(DefaultTxTimerProvider2 provider) {
        this.provider = provider;
    }

    /**
     * 尝试翻篇
     */
    boolean turnOver(long startTime, long quotient){
        long previousQuotient;
        while (true) {
            //获取之前的商数
            previousQuotient = timeQuotient.get();
            //如果当前商数等于或小于之前商数, 则不翻篇
            if (quotient <= previousQuotient) {
                return false;
            }
            //若当前商数大于之前商数, 尝试更新商数
            if (timeQuotient.compareAndSet(previousQuotient, quotient)) {
                //更新成功则重置统计单元
                this.startTime.set((startTime / MINUTE_MILLIS) * MINUTE_MILLIS);
                this.minElapse.set(Long.MAX_VALUE);
                this.maxElapse.set(Long.MIN_VALUE);
                this.totalElapse.set(0);
                this.finishCount.set(0);
                return true;
            }
        }
    }

    /**
     * 记录耗时
     */
    void record(long elapse){
        //完成+1
        finishCount.incrementAndGet();
        //耗时累计
        totalElapse.addAndGet(elapse);
        //max elapse
        for (int i = 0 ; i < provider.updateAttempts; i++) {
            long previous = maxElapse.get();
            if (elapse > previous) {
                if (maxElapse.compareAndSet(previous, elapse)) {
                    break;
                }
            }
        }
        //min elapse
        for (int i = 0 ; i < provider.updateAttempts; i++) {
            long previous = minElapse.get();
            if (elapse < previous) {
                if (minElapse.compareAndSet(previous, elapse)) {
                    break;
                }
            }
        }
    }

}
