package sviolet.slate.common.x.monitor.txtimer;

import sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerConfig;
import sviolet.thistle.util.crypto.SecureRandomUtils;

public class TxTimerCommonTest {

    public static void main(String[] args) throws InterruptedException {
        //ARGS
        System.setProperty("slate.txtimer.enabled", "true");
        System.setProperty("slate.txtimer.report.interval", "2");
        System.setProperty("slate.txtimer.threshold.avg", "110");
//        System.setProperty("slate.txtimer.threshold.max", "700");
//        System.setProperty("slate.txtimer.threshold.min", "30");
//        System.setProperty("slate.txtimer.report.printpermin", "true");

        //Set
        DefaultTxTimerConfig.setReportAllInterval(60);
        DefaultTxTimerConfig.setThresholdAvg(120);
        DefaultTxTimerConfig.setThresholdMax(600);
        DefaultTxTimerConfig.setThresholdMin(30);

        //Set2
        DefaultTxTimerConfig.setReportAllInterval(5);
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
