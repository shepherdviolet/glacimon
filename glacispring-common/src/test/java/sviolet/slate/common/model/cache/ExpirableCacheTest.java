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

package sviolet.slate.common.model.cache;

import ch.qos.logback.classic.Level;
import sviolet.slate.common.helper.logback.LogbackHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ExpirableCache测试案例
 *
 * @author S.Violet
 */
public class ExpirableCacheTest {

    private static final MyExpirableCache cache = new MyExpirableCache();

    public static void main(String[] args) throws InterruptedException {

        LogbackHelper.setLevel("sviolet.slate.common.model.cache", Level.OFF);

        new Thread(new Task()).start();
        new Thread(new Task()).start();
        new Thread(new Task()).start();
        new Thread(new Task()).start();

        Thread.sleep(10000);

    }

    private static class Task implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                System.out.println(cache.get("1"));
            }
        }
    }

    private static class MyExpirableCache extends ExpirableCache<String> {

        private String[] values = {"1", "2", "3", "4", "5"};
        private AtomicInteger index = new AtomicInteger(0);

        @Override
        protected UpdateResult<String> onUpdate(String key) {
            int i = index.getAndIncrement();
            if (i >= values.length){
                i = values.length - 1;
            }
            /*
             * 第一个参数是返回值
             * 第二个参数是数据有效期, 即为2秒
             */
            return new UpdateResult<>(values[i], 2000);
        }

        @Override
        protected long onError(String key, Throwable t) {
            /*
             * 处理onUpdate方法中抛出的异常, 返回值表示没有重试间隔
             */
            return 0;
        }

    }

}
