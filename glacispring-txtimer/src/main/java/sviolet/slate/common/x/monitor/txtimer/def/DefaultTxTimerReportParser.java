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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 用于将TxTimer报告解析为原始数据
 *
 * @see DefaultTxTimerReportScanner
 * @author S.Violet
 */
public interface DefaultTxTimerReportParser {

    /* ************************************************************************************************
        接口
     ************************************************************************************************ */

    /**
     * 解析一行数据
     * @param line 一行数据
     * @return 原始数据, 如果返回空, 则表示该行无法解析
     */
    RawData parseLine(String line) throws Exception;

    /* ************************************************************************************************
        默认实现
     ************************************************************************************************ */

    /**
     * 默认报告解析器
     * @see DefaultTxTimerReportScanner
     */
    class DefaultParser implements DefaultTxTimerReportParser {

        private DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withZone(ZoneId.systemDefault());

        int TAG = 0;
        int VER = 1;

        int RAND_1 = 2;
        int START_TIME_1 = 3;
        int DURATION_1 = 4;
        int GROUP_1 = 5;
        int NAME_1 = 6;
        int RUN_CNT_1 = 7;
        int TOT_AVG_1 = 9;
        int TOT_CNT_1 = 10;
        int CURR_MIN_1 = 12;
        int CURR_MAX_1 = 13;
        int CURR_AVG_1 = 14;
        int CURR_CNT_1 = 15;

        @Override
        public RawData parseLine(String line) throws Exception {
            //忽略非'TxT|'行
            if (!line.startsWith("TxT|")) {
                return null;
            }

            //分割数据
            String[] elements = line.split("\\|", -1);

            //区分版本处理
            if ("1".equals(elements[VER])) {
                return parseLineV1(elements);
            }

            throw new Exception("Unsupported report version " + elements[VER]);
        }

        private RawData parseLineV1(String[] elements) {
            RawData rawData = new RawData();
            rawData.setVer(Integer.parseInt(elements[VER]));
            rawData.setRand(elements[RAND_1]);
            rawData.setStartTime(Instant.from(DATE_FORMAT.parse(elements[START_TIME_1])).toEpochMilli());
            rawData.setDuration(Integer.parseInt(elements[DURATION_1]));
            rawData.setGroup(elements[GROUP_1]);
            rawData.setName(elements[NAME_1]);
            rawData.setRunCnt(Integer.parseInt(elements[RUN_CNT_1]));
            rawData.setTotAvg(Integer.parseInt(elements[TOT_AVG_1]));
            rawData.setTotCnt(Integer.parseInt(elements[TOT_CNT_1]));
            rawData.setCurrMin(Integer.parseInt(elements[CURR_MIN_1]));
            rawData.setCurrMax(Integer.parseInt(elements[CURR_MAX_1]));
            rawData.setCurrAvg(Integer.parseInt(elements[CURR_AVG_1]));
            rawData.setCurrCnt(Integer.parseInt(elements[CURR_CNT_1]));
            return rawData;
        }

    }

    /* ************************************************************************************************
        内部类
     ************************************************************************************************ */

    /**
     * 报告解析器解析出来的原始数据
     */
    class RawData {
        private int ver;
        private String rand;
        private long startTime;
        private int duration;
        private String group;
        private String name;
        private int runCnt;
        private int totAvg;
        private int totCnt;
        private int currMin;
        private int currMax;
        private int currAvg;
        private int currCnt;

        public int getVer() {
            return ver;
        }

        public void setVer(int ver) {
            this.ver = ver;
        }

        public String getRand() {
            return rand;
        }

        public void setRand(String rand) {
            this.rand = rand;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRunCnt() {
            return runCnt;
        }

        public void setRunCnt(int runCnt) {
            this.runCnt = runCnt;
        }

        public int getTotAvg() {
            return totAvg;
        }

        public void setTotAvg(int totAvg) {
            this.totAvg = totAvg;
        }

        public int getTotCnt() {
            return totCnt;
        }

        public void setTotCnt(int totCnt) {
            this.totCnt = totCnt;
        }

        public int getCurrMin() {
            return currMin;
        }

        public void setCurrMin(int currMin) {
            this.currMin = currMin;
        }

        public int getCurrMax() {
            return currMax;
        }

        public void setCurrMax(int currMax) {
            this.currMax = currMax;
        }

        public int getCurrAvg() {
            return currAvg;
        }

        public void setCurrAvg(int currAvg) {
            this.currAvg = currAvg;
        }

        public int getCurrCnt() {
            return currCnt;
        }

        public void setCurrCnt(int currCnt) {
            this.currCnt = currCnt;
        }

        @Override
        public String toString() {
            return "RawData{" +
                    "ver='" + ver + '\'' +
                    ", rand='" + rand + '\'' +
                    ", startTime=" + startTime +
                    ", duration=" + duration +
                    ", group='" + group + '\'' +
                    ", name='" + name + '\'' +
                    ", runCnt=" + runCnt +
                    ", totAvg=" + totAvg +
                    ", totCnt=" + totCnt +
                    ", currMin=" + currMin +
                    ", currMax=" + currMax +
                    ", currAvg=" + currAvg +
                    ", currCnt=" + currCnt +
                    '}';
        }
    }

}
