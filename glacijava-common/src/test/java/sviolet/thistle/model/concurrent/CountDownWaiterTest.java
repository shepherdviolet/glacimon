/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.thistle.model.concurrent;

import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.compat.concurrent.CompatThreadFactoryBuilder;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class CountDownWaiterTest {

    private final ExecutorService threadPool = ThreadPoolExecutorUtils.createFixed(4,
            new CompatThreadFactoryBuilder()
                    .setNameFormat("test-%d")
                    .setDaemon(true)
                    .build());

    @Test
    public void countDownWaiter() throws InterruptedException {
        final Map<String, Integer> map = new ConcurrentHashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);

        final AtomicInteger result = new AtomicInteger(0);

        final CountDownWaiter waiter = new CountDownWaiter(0);
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            waiter.countUp();
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException ignore) {
                        }
                        result.getAndAdd(entry.getValue());
//                        System.out.println("Task finished, key: " + entry.getKey());
                    } finally {
                        waiter.countDown();
                    }
                }
            });
        }

        if (!waiter.await()) {
            throw new RuntimeException("Timeout");
        }

        Assert.assertEquals(6, result.get());
//        System.out.println("All finished, result: " + result.get());
    }

}
