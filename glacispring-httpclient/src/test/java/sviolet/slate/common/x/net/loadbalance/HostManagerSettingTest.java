/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.slate.common.x.net.loadbalance;

import ch.qos.logback.classic.Level;
import sviolet.slate.common.helper.logback.LogbackHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadBalancedHostManager测试案例
 * 1.配置变化
 *
 * 测试要点:
 * 1.无需后端
 * 2.在配置变化完毕, 出现最终配置后(有四个), 观察实际发送情况是否和这四个中的一个相符
 */
public class HostManagerSettingTest {

    private static final int MAX_HOST_NUM = 8;
    private static final Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) {

        LogbackHelper.setLevel("sviolet.slate.common.x.net.loadbalance", Level.ERROR);
        System.setProperty("slate.loadbalance.warndisabled", "true");

        final LoadBalancedHostManager manager = new LoadBalancedHostManager();

        final AtomicInteger noHostCounter = new AtomicInteger(0);
        final AtomicInteger[] counters = new AtomicInteger[MAX_HOST_NUM];
        for (int i = 0 ; i < MAX_HOST_NUM ; i++){
            counters[i] = new AtomicInteger(0);
        }

        setting(manager, 0x00000000, true);

        print(noHostCounter, counters);

//        changeSettingInterval(manager);//低频度变配置
        changeSettingConcurrent(manager);//高频率(并发)变配置

        newTask(noHostCounter, counters, manager, 256);

    }

    private static void changeSettingInterval(final LoadBalancedHostManager manager) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x10000000, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x01000000, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x00100000, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x00010000, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x11110000, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x00001111, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x11111111, true);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {
                }

                setting(manager, 0x10101010, true);

            }
        }).start();
    }

    /**
     * 执行一段时间后, 看最后那些通道的计数在增加, 是否与打印出来的四种开关状态中的一种一致
     */
    private static void changeSettingConcurrent(final LoadBalancedHostManager manager) {
        for (int i = 0 ; i < 4 ; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ignored) {
                    }
                    int switcher = 0x00000000;
                    for (int j = 0 ; j < 1000000 ; j++) {
                        switcher = random.nextInt(0x1FFFFFFF);
                        setting(manager, switcher, false);
                    }
                    String hexString = Integer.toHexString(switcher & 0x11111111);
                    StringBuilder stringBuilder = new StringBuilder(hexString);
                    while (stringBuilder.length() < 8){
                        stringBuilder.insert(0, "0");
                    }
                    System.out.println("state (1 of 4):" + stringBuilder.reverse().toString());
                }
            }).start();
        }
    }

    private static void newTask(final AtomicInteger noHostCounter, final AtomicInteger[] counters, final LoadBalancedHostManager manager, int num) {
        for (int i = 0 ; i < num ; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 6000; i++) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException ignored) {
                        }
                        LoadBalancedHostManager.Host host = manager.nextHost();
                        if (host == null){
                            noHostCounter.incrementAndGet();
                        } else {
                            AtomicInteger counter = counters[Integer.parseInt(host.getUrl())];
                            counter.incrementAndGet();
                        }
                    }
                }
            }).start();
        }
    }

    private static void setting(LoadBalancedHostManager manager, int hostSwitchers, boolean printEnabled) {
        StringBuilder stringBuilder = null;
        if (printEnabled) {
            stringBuilder = new StringBuilder("switchers ");
        }
        List<String> hosts = new ArrayList<>(0);
        for (int i = 0 ; i < MAX_HOST_NUM ; i++){
            boolean enable = (hostSwitchers & (0x1 << i * 4)) > 0;
            if (enable){
                hosts.add(String.valueOf(i));
            }
            if (printEnabled) {
                stringBuilder.append(enable);
                stringBuilder.append(" ");
            }
        }
        manager.setHostList(hosts);
        if (printEnabled) {
            System.out.println(stringBuilder.toString());
        }
    }

    private static void print(final AtomicInteger noHostCounter, final AtomicInteger[] counters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i < 60 ; i++) {
                    for (int j = 0; j < 20; j++) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException ignored) {
                        }

                        StringBuilder stringBuilder = new StringBuilder();
                        for (AtomicInteger counter : counters) {
                            stringBuilder.append(counter.get());
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append("X");
                        stringBuilder.append(noHostCounter.get());
                        System.out.println("counters " + stringBuilder.toString());
                    }
                }
            }
        }).start();
    }

}
