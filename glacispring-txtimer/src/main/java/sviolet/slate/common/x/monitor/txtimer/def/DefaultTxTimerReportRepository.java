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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.monitor.txtimer.def;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 用于储存TxTimer报告数据的仓库接口
 *
 * @param <R> 结果数据类型
 *
 * @see DefaultTxTimerReportScanner
 * @author S.Violet
 */
public interface DefaultTxTimerReportRepository <R> {

    /* ************************************************************************************************
        常量
     ************************************************************************************************ */

    int MINUTE_MILLIS = 60 * 1000;

    /* ************************************************************************************************
        接口
     ************************************************************************************************ */

    /**
     * 添加一条原始数据到仓库
     * @param rawData 原始数据
     */
    void add(DefaultTxTimerReportParser.RawData rawData) throws Exception;

    /**
     * 获取结果数据
     */
    R getData();

    /* ************************************************************************************************
        默认实现
     ************************************************************************************************ */

    /**
     * [非线程安全 | Not Thread Safe] 默认仓库(仓库的默认实现)
     * @see DefaultTxTimerReportScanner
     */
    class DefaultRepository implements DefaultTxTimerReportRepository<Map<String, Map<String, TreeMap<Long, Map<String, DefaultData>>>>> {

        //group -> name -> time -> random -> DefaultData
        private Map<String, Map<String, TreeMap<Long, Map<String, DefaultData>>>> data = new HashMap<>(16);

        @Override
        @SuppressWarnings({"lgtm[java/integer-multiplication-cast-to-long]"})
        public void add(DefaultTxTimerReportParser.RawData rawData) throws Exception{
            //get startTime & endTime
            long startTime = rawData.getStartTime();
            int durationMinutes = rawData.getDuration() / MINUTE_MILLIS;

            //截取数据, 并换算成每分钟的值
            double count = (double) rawData.getCurrCnt() / (double) durationMinutes;
            int avgElapse = rawData.getCurrAvg();
            int maxElapse = rawData.getCurrMax();
            int minElapse = rawData.getCurrMin();

            //跳过没交易的记录
            if (count <= 0d) {
                return;
            }

            //保存数据
            for (int i = 0; i < durationMinutes; i++) {
                // About suppressed warnings: 'i * MINUTE_MILLIS' will not cause overflow, because their product will not be greater than 'rawData.getDuration()' (it's an integer)
                DefaultData element = getElement(rawData.getGroup(), rawData.getName(), startTime + i * MINUTE_MILLIS, rawData.getRand());
                element.count = count;
                element.avgElapse = avgElapse;
                element.maxElapse = maxElapse;
                element.minElapse = minElapse;
            }
        }

        /**
         * 获取指定组名/名称/时间的记录
         */
        private DefaultData getElement(String group, String name, long time, String random){
            //重要, 减少内存开销
            String group0 = group.intern();
            String name0 = name.intern();
            String random0 = random.intern();
            //获取或创建元素
            return data.computeIfAbsent(group0, k -> new HashMap<>(256))
                    .computeIfAbsent(name0, k -> new TreeMap<>())
                    .computeIfAbsent(time, k -> new HashMap<>(8))
                    .computeIfAbsent(random0, k -> new DefaultData(group0, name0, time, random0));
        }

        /**
         * [非线程安全 | Not Thread Safe] 获取数据, 请务必在报告处理完毕后获取, 因为非线程安全,
         * group -> name -> time -> random -> DefaultData
         */
        @Override
        public Map<String, Map<String, TreeMap<Long, Map<String, DefaultData>>>> getData(){
            return data;
        }

    }

    /**
     * 默认仓库的数据
     */
    class DefaultData {
        private String group;
        private String name;
        private long time;
        private String random;
        private double count = 0f;
        private int avgElapse = 0;
        private int maxElapse = Integer.MIN_VALUE;
        private int minElapse = Integer.MAX_VALUE;

        private DefaultData(String group, String name, long time, String random) {
            this.group = group;
            this.name = name;
            this.time = time;
            this.random = random;
        }

        /**
         * 组名
         */
        public String getGroup() {
            return group;
        }

        /**
         * 交易名
         */
        public String getName() {
            return name;
        }

        /**
         * 记录时间(起始时间, 时长60s)
         */
        public long getTime() {
            return time;
        }

        /**
         * 随机数, 用于标识报告产生的进程, 不严格, 通常用来去重, 或判断问题出在哪个进程
         */
        public String getRandom() {
            return random;
        }

        /**
         * 交易数量
         */
        public double getCount() {
            return count;
        }

        /**
         * 平均耗时
         */
        public int getAvgElapse() {
            return avgElapse;
        }

        /**
         * 最大耗时
         */
        public int getMaxElapse() {
            return maxElapse;
        }

        /**
         * 最小耗时
         */
        public int getMinElapse() {
            return minElapse;
        }

        @Override
        public String toString() {
            return "DefaultData{" +
                    "group='" + group + '\'' +
                    ", name='" + name + '\'' +
                    ", time=" + time +
                    ", count=" + count +
                    ", avgElapse=" + avgElapse +
                    ", maxElapse=" + maxElapse +
                    ", minElapse=" + minElapse +
                    '}';
        }
    }

}
