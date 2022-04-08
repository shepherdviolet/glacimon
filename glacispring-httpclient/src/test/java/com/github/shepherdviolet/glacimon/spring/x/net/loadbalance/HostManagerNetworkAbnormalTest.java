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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadBalancedHostManager测试案例
 * 1.配置固定不变
 * 2.存在网路故障
 *
 * 测试要点:
 * 1.无需后端
 * 2.观察网络故障情况变化时, 故障后端是否被有效阻断
 */
public class HostManagerNetworkAbnormalTest {

    static final int HOST_NUM = 4;

    public static void main(String[] args) {

        final Random random = new Random(System.currentTimeMillis());

        final Map<String, AtomicInteger> counters = new HashMap<>(HOST_NUM);
        final Map<String, AtomicBoolean> switchers = new HashMap<>(HOST_NUM);
        final List<String> hosts = new ArrayList<>(HOST_NUM);
        for (int i = 0 ; i < HOST_NUM ; i++) {
            counters.put(String.valueOf(i), new AtomicInteger(0));
            switchers.put(String.valueOf(i), new AtomicBoolean(true));
            hosts.add(String.valueOf(i));
        }

        final LoadBalancedHostManager manager = new LoadBalancedHostManager();
        manager.setHostList(hosts);

        randomAbnormal(random, counters, switchers, hosts);//随机网络波动
//        staticAbnormal(random, counters, switchers, hosts);//固定故障情况

        newTask(counters, manager, switchers, 256);//带间隔的任务(配合多任务数)
//        newFastTask(counters, manager, switchers, 4);//不带间隔的任务(配合少量任务数)

    }

    static void randomAbnormal(final Random random, final Map<String, AtomicInteger> counters, final Map<String, AtomicBoolean> switchers, final List<String> hosts) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i < 60 ; i++){

                    delayAndPrint(counters);

                    int index = random.nextInt(HOST_NUM);
                    AtomicBoolean switcher = switchers.get(hosts.get(index));
                    switcher.set(!switcher.get());

                    StringBuilder stringBuilder = new StringBuilder();
                    for (AtomicBoolean swi : switchers.values()){
                        stringBuilder.append(swi.get());
                        stringBuilder.append(" ");
                    }
                    System.out.println("switchers " + stringBuilder.toString() + " --------------------------");
                }
            }
        }).start();
    }

    static void staticAbnormal(final Random random, final Map<String, AtomicInteger> counters, final Map<String, AtomicBoolean> switchers, final List<String> hosts) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //网路故障的Host编号
                int[] badIndex = {0, 1, 2, 3};
//                int[] badIndex = {1, 3};
//                int[] badIndex = {0, 1};
//                int[] badIndex = {1};
//                int[] badIndex = {};

                for (int i = 0 ; i < badIndex.length ; i++){
                    AtomicBoolean switcher = switchers.get(hosts.get(badIndex[i]));
                    switcher.set(false);
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (AtomicBoolean swi : switchers.values()){
                    stringBuilder.append(swi.get());
                    stringBuilder.append(" ");
                }
                System.out.println("switchers " + stringBuilder.toString() + " --------------------------");

                for (int i = 0 ; i < 60 ; i++){
                    delayAndPrint(counters);
                }
            }
        }).start();
    }

    static void delayAndPrint(Map<String, AtomicInteger> counters) {
        for (int j = 0 ; j < 20 ; j++) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ignored) {
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (AtomicInteger counter : counters.values()){
                stringBuilder.append(counter.get());
                stringBuilder.append(" ");
            }
            System.out.println("counters " + stringBuilder.toString());
        }
    }

    static void newTask(final Map<String, AtomicInteger> counters, final LoadBalancedHostManager manager, final Map<String, AtomicBoolean> switchers, int num) {
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
                        AtomicInteger counter = counters.get(host.getUrl());
                        counter.incrementAndGet();
                        AtomicBoolean switcher = switchers.get(host.getUrl());
                        host.feedback(switcher.get(), 3000L, 4);
                    }
                }
            }).start();
        }
    }

    static void newFastTask(final Map<String, AtomicInteger> counters, final LoadBalancedHostManager manager, final Map<String, AtomicBoolean> switchers, int num) {
        for (int i = 0 ; i < num ; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000000000; i++) {
                        LoadBalancedHostManager.Host host = manager.nextHost();
                        AtomicInteger counter = counters.get(host.getUrl());
                        counter.incrementAndGet();
                        AtomicBoolean switcher = switchers.get(host.getUrl());
                        host.feedback(switcher.get(), 3000L, 4);
                    }
                }
            }).start();
        }
    }

}
