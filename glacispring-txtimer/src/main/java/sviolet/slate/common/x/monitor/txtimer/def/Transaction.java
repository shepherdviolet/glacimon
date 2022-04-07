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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider2.*;

class Transaction {

    private DefaultTxTimerProvider2 provider;

    //完成计数(总计)
    AtomicInteger finishCount = new AtomicInteger(0);
    //执行中计数
    AtomicInteger runningCount = new AtomicInteger(0);

    //用于粗略地估算总平均耗时
    long averageElapseTotal = 0;

    //将统计单元按分钟划分, 预置并重复使用
    private Unit[] units;

    Transaction(DefaultTxTimerProvider2 provider) {
        this.provider = provider;

        //创建统计报告时长+6的统计单元, 每分钟一个单元
        units = new Unit[provider.reportInterval + 6];
        for (int i = 0 ; i < units.length ; i++) {
            units[i] = new Unit(provider);
        }
    }

    /**
     * 开始交易
     */
    void running(){
        //执行中+1
        runningCount.incrementAndGet();
        //提醒日志输出线程工作
        provider.reporter.notifyReport();
    }

    /**
     * 完成交易
     */
    void finish(long currentTime, long elapse) {
        //执行中-1
        runningCount.decrementAndGet();
        //完成计数+1
        finishCount.incrementAndGet();
        //根据当前时间获得统计单元, 记录耗时
        getUnit(currentTime).record(elapse);
        //提醒日志输出线程工作
        provider.reporter.notifyReport();
    }

    /**
     * 获取统计单元
     */
    Unit getUnit(long currentTime){
        //当前时间的分钟数
        long currentMinute = currentTime / MINUTE_MILLIS;
        //当前时间分钟数除以统计单元数, 商数, 商数用于识别统计单元是否翻篇
        long quotient = currentMinute / units.length;
        //当前时间分钟数模统计单元数, 余数, 余数用于确定该时间记录到哪个统计单元
        int remainder = (int) (currentMinute % units.length);
        //用余数获得统计单元
        Unit unit = units[remainder];
        //尝试翻篇, 若一个统计单元是旧的, 则重置统计单元的数据, 用于当前时间的统计
        unit.turnOver(currentTime, quotient);
        return unit;
    }

    /**
     * 根据时间段获取单元, 用于报告输出
     */
    List<Unit> getUnits(long startTime, long endTime){
        //计算开始时间的余数
        long startMinute = startTime / MINUTE_MILLIS;
        int startRemainder = (int) (startMinute % units.length);
        //计算截止时间的余数
        long endMinute = endTime / MINUTE_MILLIS;
        int endRemainder = (int) (endMinute % units.length);

        List<Unit> unitList = new ArrayList<>(units.length);
        if (startRemainder < endRemainder) {
            //如果连续
            for (int i = startRemainder ; i < endRemainder ; i++) {
                unitList.add(units[i]);
            }
        } else if (startRemainder > endRemainder){
            //如果不连续(后半段到前半段)
            for (int i = startRemainder ; i < units.length ; i++) {
                unitList.add(units[i]);
            }
            for (int i = 0 ; i < endRemainder ; i++) {
                unitList.add(units[i]);
            }
        }
        return unitList;
    }

}
