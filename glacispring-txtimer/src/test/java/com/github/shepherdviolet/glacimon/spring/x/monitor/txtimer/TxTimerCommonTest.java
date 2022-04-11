/*
 * Copyright (C) 2022-2022 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer;

import com.github.shepherdviolet.glacimon.java.crypto.SecureRandomUtils;
import com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.def.DefaultTxTimerConfig;

public class TxTimerCommonTest {

    public static void main(String[] args) throws InterruptedException {
        //ARGS
        System.setProperty("glacispring.txtimer.enabled", "true");
        System.setProperty("glacispring.txtimer.report.interval", "2");
        System.setProperty("glacispring.txtimer.threshold.avg", "110");
//        System.setProperty("glacispring.txtimer.threshold.max", "700");
//        System.setProperty("glacispring.txtimer.threshold.min", "30");
//        System.setProperty("glacispring.txtimer.report.printpermin", "true");

        //Set
        DefaultTxTimerConfig.setReportAllInterval(60);
        DefaultTxTimerConfig.setThresholdAvg(120);
        DefaultTxTimerConfig.setThresholdMax(600);
        DefaultTxTimerConfig.setThresholdMin(30);

        //Set2
        DefaultTxTimerConfig.setReportAllInterval(2);
        DefaultTxTimerConfig.setThresholdMax(700);

        for (int i = 0 ; i < 1000 ; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ignored) {
                    }

                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < 10 * 60 * 1000L) {

                        try (TimerContext timerContext = TxTimer.entry("HttpTransport", "Service" + String.valueOf(finalI % 50))) {
                            Thread.sleep(SecureRandomUtils.nextInt(100) + finalI % 100);
//                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }

                    }

                }
            }).start();
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 12 * 60 * 1000L) {
            Thread.sleep(10000L);
        }
    }

}
