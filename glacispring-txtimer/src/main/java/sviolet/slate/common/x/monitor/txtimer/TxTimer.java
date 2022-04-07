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

package sviolet.slate.common.x.monitor.txtimer;

import com.github.shepherdviolet.glaciion.Glaciion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * <p>简单的交易耗时统计, 提供默认实现, 也可以用Glaciion SPI扩展</p>
 *
 * <p>日志前缀:TxTimer</p>
 *
 * <p>启动参数:<br>
 *      -Dslate.txtimer.enabled=true 启用统计, true开启, false关闭, 默认开启<br>
 * </p>
 *
 * <p>默认实现 ********************************************************************************************</p>
 *
 * <p>
 *     1.默认实现了交易耗时的统计, 并通过日志定时输出报告. <br>
 *     2.可以使用Glaciion SPI替换实现, 替换实现后下面的参数无效. <br>
 * </p>
 *
 * <p>默认实现的启动参数(不可动态修改):<br>
 *      -Dslate.txtimer.report.interval=5 日志报告输出间隔, 单位分钟, [2-60], 默认5 <br>
 *      -Dslate.txtimer.pagelines=20 日志每次输出的最大行数, 大于该行数会分页, 默认20 <br>
 * </p>
 *
 * <p>默认实现的启动参数(可动态修改):<br>
 *      -Dslate.txtimer.reportall.interval=60 全量日志报告输出间隔(周期), 单位:分钟, [2-∞], 默认∞(不输出全量日志)<br>
 *      -Dslate.txtimer.threshold.avg=2000 打印周期内平均耗时超过该值的交易, 单位:毫秒<br>
 *      -Dslate.txtimer.threshold.max=10000 打印周期内最大耗时超过该值的交易, 单位:毫秒<br>
 *      -Dslate.txtimer.threshold.min=1000 打印周期内最小耗时超过该值的交易, 单位:毫秒<br>
 * </p>
 *
 * <p>slate.txtimer.threshold系列参数均未配置, 则输出全部交易的报告. 若设置了任意一个, 则只有满足条件的交易才输出:
 * avg >= thresholdAvg || max >= thresholdMax || min >= thresholdMin</p>
 *
 * @author S.Violet
 */
public class TxTimer {

    private static final Logger logger = LoggerFactory.getLogger(TxTimer.class);

    private static final TxTimerProvider2 PROVIDER;

    static {
        //统计开关, 默认关闭
        if ("true".equals(System.getProperty("slate.txtimer.enabled", "true"))) {
            TxTimerProvider2 service = Glaciion.loadSingleService(TxTimerProvider2.class).get();
            //再根据provider判断是否要启用
            if (service.enabled()) {
                PROVIDER = service;
                logger.info("TxTimer | TxTimer Enabled !!! implementation " + PROVIDER.getClass().getName());
            } else {
                PROVIDER = null;
            }
        } else {
            PROVIDER = null;
        }
    }

    /**
     * <p>交易开始时调用</p>
     *
     * <code>
     *  try (TimerContext context = TxTimer.entry("Entrance", "TestService")) {
     *      // 交易逻辑 ......
     *  }
     * </code>
     *
     * <code>
     *  TimerContext context = TxTimer.entry("Entrance", "TestService");
     *  try {
     *      // 交易逻辑 ......
     *  } finally {
     *      context.exit();
     *  }
     * </code>
     *
     * @param groupName 组别
     * @param transactionName 交易名
     */
    public static TimerContext entry(String groupName, String transactionName){
        if (PROVIDER != null) {
            return PROVIDER.entry(groupName, transactionName);
        }
        return DUMMY_CONTEXT;
    }

    /**
     * 交易结束时调用
     *
     * <code>
     *  try (TimerContext context = TxTimer.entry("Entrance", "TestService")) {
     *      // 交易逻辑 ......
     *  }
     * </code>
     *
     * <code>
     *  TimerContext context = TxTimer.entry("Entrance", "TestService");
     *  try {
     *      // 交易逻辑 ......
     *  } finally {
     *      //context.exit();
     *      TxTimer.exit(context);
     *  }
     * </code>
     *
     * @param timerContext 处理结果编码
     */
    public static void exit(TimerContext timerContext) {
        exit(timerContext, 0);
    }

    /**
     * 交易结束时调用
     *
     * <code>
     *  try (TimerContext context = TxTimer.entry("Entrance", "TestService")) {
     *      // 交易逻辑 ......
     *  }
     * </code>
     *
     * <code>
     *  TimerContext context = TxTimer.entry("Entrance", "TestService");
     *  try {
     *      // 交易逻辑 ......
     *  } finally {
     *      //context.exit(code);
     *      TxTimer.exit(context, code);
     *  }
     * </code>
     *
     * @param timerContext 处理结果编码
     */
    public static void exit(TimerContext timerContext, int resultCode) {
        if (timerContext == DUMMY_CONTEXT) {
            return;
        }
        if (PROVIDER != null) {
            PROVIDER.exit(timerContext, resultCode);
        }
    }

    public static TxTimerProvider2 getProvider(){
        if (PROVIDER != null && PROVIDER.canBeGet()) {
            return PROVIDER;
        }
        logger.error("TxTimer | Prohibit access to get TxTimerProvider2, Null or Banned by TxTimerProvider2.canBeGet()");
        return null;
    }

    private static final TimerContext DUMMY_CONTEXT = new TimerContext.Basic(){
        @Override
        public void exit(int resultCode) {
            //do nothing
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Deprecated ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //用于start和stop之间的上下文传递
    private static final ThreadLocal<LinkedList<TimerContext>> recordStack = new ThreadLocal<>();

    /**
     * <p>交易开始时调用, 弃用, 这个只能统计同步的代码块</p>
     *
     * <code>
     *  try {
     *      TxTimer.start("Entrance", "TestService");
     *      // 交易逻辑 ......
     *  } finally {
     *      TxTimer.stop();
     *  }
     * </code>
     *
     * @param groupName 组别
     * @param transactionName 交易名
     * @deprecated TxTimer#entry + TxTimer#exit instead
     */
    @Deprecated
    public static void start(String groupName, String transactionName){
        if (PROVIDER != null) {
            TimerContext timerContext = PROVIDER.entry(groupName, transactionName);
            //从ThreadLocal获取上下文
            LinkedList<TimerContext> recordStack = TxTimer.recordStack.get();
            if (recordStack == null) {
                recordStack = new LinkedList<>();
                TxTimer.recordStack.set(recordStack);
            }
            //存入堆栈
            recordStack.addLast(timerContext);
        }
    }

    /**
     * 交易结束时调用, 弃用, 这个只能统计同步的代码块
     *
     * <code>
     *  try {
     *      TxTimer.start("Entrance", "TestService");
     *      // 交易逻辑 ......
     *  } finally {
     *      TxTimer.stop();
     *  }
     * </code>
     *
     * @deprecated TxTimer#entry + TxTimer#exit instead
     */
    @Deprecated
    public static void stop(){
        stop(0);
    }

    /**
     * 交易结束时调用, 弃用, 这个只能统计同步的代码块
     *
     * <code>
     *  try {
     *      TxTimer.start("Entrance", "TestService");
     *      // 交易逻辑 ......
     *  } finally {
     *      TxTimer.stop();
     *  }
     * </code>
     *
     * @param resultCode 处理结果编码
     * @deprecated TxTimer#entry + TxTimer#exit instead
     */
    @Deprecated
    public static void stop(int resultCode){
        if (PROVIDER != null) {
            //从ThreadLocal获取上下文, 若不存在则不正常
            LinkedList<TimerContext> recordStack = TxTimer.recordStack.get();
            if (recordStack == null) {
                return;
            }
            TimerContext timerContext = recordStack.pollLast();
            //如果栈里没记录, 则删除栈
            if (recordStack.size() <= 0) {
                TxTimer.recordStack.remove();
            }
            PROVIDER.exit(timerContext, resultCode);
        }
    }

}
