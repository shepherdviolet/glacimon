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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>默认交易耗时统计的配置</p>
 *
 * @author S.Violet
 */
public class DefaultTxTimerConfig {

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]全量日志报告输出间隔(周期), 单位:分钟, [2-∞], 默认∞(不输出全量日志)
     */
    static int reportAllInterval;
    static long reportAllIntervalMillis = Long.MAX_VALUE;
    static boolean lockReportAllInterval = false;
    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内平均耗时超过该值的交易, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    static int thresholdAvg;
    static boolean lockThresholdAvg = false;
    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内最大耗时超过该值的交易, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    static int thresholdMax;
    static boolean lockThresholdMax = false;
    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内最小耗时超过该值的交易, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    static int thresholdMin;
    static boolean lockThresholdMin = false;
    static boolean thresholdEnabled = false;
    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [高级设置]false时, 统计报告的统计周期为N分钟(默认5分钟, N为日志打印间隔), true时, 统计报告的统计周期为1分钟(日志量变大, 数据变精细), 默认false<br>
     */
    static boolean reportPrintsPerMinute = false;
    static boolean lockReportPrintsPerMinute = true;

    /* ******************************************************************************************************************* */

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]全量日志报告输出间隔(周期), 单位:分钟, [30-∞], 默认∞(不输出全量日志)
     */
    public static void setReportAllInterval(int reportAllInterval) {
        if (lockReportAllInterval) {
            logger.warn("TxTimer | Config: reportAllInterval has been locked by -Dslate.txtimer.reportall.interval, can not change");
            return;
        }
        DefaultTxTimerConfig.reportAllInterval = reportAllInterval;
        reportAllIntervalMillis = reportAllInterval * 60L * 1000L;
        logger.info("TxTimer | Config: reportAllInterval change to " + reportAllInterval);
        logger.info("TxTimer | Config: Now Full Report every " + reportAllInterval + " minutes");
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]全量日志报告输出间隔(周期), 单位:分钟, [30-∞], 默认∞(不输出全量日志)
     */
    public static void setReportAllInterval(String reportAllInterval) {
        int value;
        try {
            value = Integer.parseInt(reportAllInterval);
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing reportAllInterval " + reportAllInterval + " to int, change reportAllInterval failed", e);
            return;
        }
        setReportAllInterval(value);
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内平均耗时超过该值的记录, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    public static void setThresholdAvg(int thresholdAvg) {
        if (lockThresholdAvg) {
            logger.warn("TxTimer | Config: thresholdAvg has been locked by -Dslate.txtimer.threshold.avg, can not change");
            return;
        }
        DefaultTxTimerConfig.thresholdAvg = thresholdAvg;
        thresholdEnabled = true;
        logger.info("TxTimer | Config: thresholdAvg change to " + thresholdAvg);
        logger.info("TxTimer | Config: Now report " + reportCondition());
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内平均耗时超过该值的记录, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    public static void setThresholdAvg(String thresholdAvg) {
        int value;
        try {
            value = Integer.parseInt(thresholdAvg);
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing thresholdAvg " + thresholdAvg + " to int, change thresholdAvg failed", e);
            return;
        }
        setThresholdAvg(value);
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内最大耗时超过该值的记录, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    public static void setThresholdMax(int thresholdMax) {
        if (lockThresholdMax) {
            logger.warn("TxTimer | Config: thresholdMax has been locked by -Dslate.txtimer.threshold.max, can not change");
            return;
        }
        DefaultTxTimerConfig.thresholdMax = thresholdMax;
        thresholdEnabled = true;
        logger.info("TxTimer | Config: thresholdMax change to " + thresholdMax);
        logger.info("TxTimer | Config: Now report " + reportCondition());
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内最大耗时超过该值的记录, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    public static void setThresholdMax(String thresholdMax) {
        int value;
        try {
            value = Integer.parseInt(thresholdMax);
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing thresholdMax " + thresholdMax + " to int, change thresholdMax failed", e);
            return;
        }
        setThresholdMax(value);
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内最小耗时超过该值的记录, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    public static void setThresholdMin(int thresholdMin) {
        if (lockThresholdMin) {
            logger.warn("TxTimer | Config: thresholdMin has been locked by -Dslate.txtimer.threshold.min, can not change");
            return;
        }
        DefaultTxTimerConfig.thresholdMin = thresholdMin;
        thresholdEnabled = true;
        logger.info("TxTimer | Config: thresholdMin change to " + thresholdMin);
        logger.info("TxTimer | Config: Now report " + reportCondition());
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [基本设置]打印周期内最小耗时超过该值的记录, 单位:毫秒<br>
     * slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
     * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin<br>
     */
    public static void setThresholdMin(String thresholdMin) {
        int value;
        try {
            value = Integer.parseInt(thresholdMin);
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing thresholdMin " + thresholdMin + " to int, change thresholdMin failed", e);
            return;
        }
        setThresholdMin(value);
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [高级设置]false时, 统计报告的统计周期为N分钟(默认5分钟, N为日志打印间隔), true时, 统计报告的统计周期为1分钟(日志量变大, 数据变精细), 默认false<br>
     */
    public static void setReportPrintsPerMinute(boolean reportPrintsPerMinute) {
        if (lockReportPrintsPerMinute) {
            logger.warn("TxTimer | Config: reportPrintsPerMinute has been locked by -Dslate.txtimer.report.printpermin, can not change");
            return;
        }
        DefaultTxTimerConfig.reportPrintsPerMinute = reportPrintsPerMinute;
        logger.info("TxTimer | Config: reportPrintsPerMinute change to " + reportPrintsPerMinute);
    }

    /**
     * 可动态调整, 启动参数优先级大于动态配置<br>
     * [高级设置]false时, 统计报告的统计周期为N分钟(默认5分钟, N为日志打印间隔), true时, 统计报告的统计周期为1分钟(日志量变大, 数据变精细), 默认false<br>
     */
    public static void setReportPrintsPerMinute(String reportPrintsPerMinute) {
        boolean value;
        try {
            value = Boolean.parseBoolean(reportPrintsPerMinute);
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing reportPrintsPerMinute " + reportPrintsPerMinute + " to boolean, change reportPrintsPerMinute failed", e);
            return;
        }
        setReportPrintsPerMinute(value);
    }

    /* ******************************************************************************************************************* */

    private static final Logger logger = LoggerFactory.getLogger(DefaultTxTimerConfig.class);

    static {
        reportAllInterval = getIntFromProperty("slate.txtimer.reportall.interval", Integer.MAX_VALUE);
        if (reportAllInterval < 2) {
            throw new IllegalArgumentException("-Dslate.txtimer.reportall.interval must >= 2 (minute)");
        }
        if (reportAllInterval < Integer.MAX_VALUE) {
            lockReportAllInterval = true;
            reportAllIntervalMillis = reportAllInterval * 60L * 1000L;
            logger.debug("TxTimer | Config: reportAllInterval is locked by -Dslate.txtimer.reportall.interval=" + reportAllInterval);
            logger.info("TxTimer | Config: Full Report every " + reportAllInterval + " minutes");
        }

        thresholdAvg = getIntFromProperty("slate.txtimer.threshold.avg", Integer.MAX_VALUE);
        if (thresholdAvg < Integer.MAX_VALUE) {
            lockThresholdAvg = true;
            thresholdEnabled = true;
            logger.debug("TxTimer | Config: thresholdAvg is locked by -Dslate.txtimer.threshold.avg=" + thresholdAvg);
        }
        thresholdMax = getIntFromProperty("slate.txtimer.threshold.max", Integer.MAX_VALUE);
        if (thresholdMax < Integer.MAX_VALUE) {
            lockThresholdMax = true;
            thresholdEnabled = true;
            logger.debug("TxTimer | Config: thresholdMax is locked by -Dslate.txtimer.threshold.max=" + thresholdMax);
        }
        thresholdMin = getIntFromProperty("slate.txtimer.threshold.min", Integer.MAX_VALUE);
        if (thresholdMin < Integer.MAX_VALUE) {
            lockThresholdMin = true;
            thresholdEnabled = true;
            logger.debug("TxTimer | Config: thresholdMin is locked by -Dslate.txtimer.threshold.min=" + thresholdMin);
        }
        logger.info("TxTimer | Config: Report " + reportCondition());

        Boolean reportPrintsPerMinute = getBooleanFromProperty("slate.txtimer.report.printpermin", null);
        if (reportPrintsPerMinute == null) {
            reportPrintsPerMinute = false;
            lockReportPrintsPerMinute = false;
        }
        DefaultTxTimerConfig.reportPrintsPerMinute = reportPrintsPerMinute;
    }

    private static int getIntFromProperty(String key, int def) {
        try {
            return Integer.parseInt(System.getProperty(key, String.valueOf(def)));
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing -D" + key + " to int, using " + def + " by default", e);
            return def;
        }
    }

    private static Boolean getBooleanFromProperty(String key, Boolean def) {
        String value = System.getProperty(key);
        if (value == null) {
            return def;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            logger.error("TxTimer | Config: Error while parsing -D" + key + " to int, using " + def + " by default", e);
            return def;
        }
    }

    static String reportCondition(){
        if (thresholdEnabled) {
            return "if avg >= " + (thresholdAvg < Integer.MAX_VALUE ? thresholdAvg : "∞") +
                    " || max >= " + (thresholdMax < Integer.MAX_VALUE ? thresholdMax : "∞") +
                    " || min >= " + (thresholdMin < Integer.MAX_VALUE ? thresholdMin : "∞");
        } else {
            return "all transactions (no filter)";
        }
    }

}
