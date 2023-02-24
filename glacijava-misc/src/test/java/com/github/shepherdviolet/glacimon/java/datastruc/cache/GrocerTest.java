/*
 * Copyright (C) 2022-2023 S.Violet
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

package com.github.shepherdviolet.glacimon.java.datastruc.cache;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("resource")
public class GrocerTest {


    public static void main(String[] args) throws MyException, Grocer.FetchTimeoutException {

        fetch1threads_purchase500delay();
//        fetch100threads_purchase500delay();
    }

    private static void fetch1threads_purchase500delay() {
        Grocer<String, MyException> grocer = new Grocer<>(new Grocer.Purchaser<String, MyException>() {
            @Override
            public Grocer.Goods<String, MyException> purchase(String key, int quantity) {
                System.out.println("purchase " + quantity);
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ignore) {
                }
                List<String> elements = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    elements.add("FOO");
                }
                return Grocer.Goods.success(elements);
            }

            @Override
            public long defaultErrorDuration() {
                return 1000L;
            }
        }, 10, 0.5f, 16, 10000, 60);

        for (int t = 0; t < 1; t++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000; i++) {
                        try {
                            grocer.fetch("1", 10000L);
                        } catch (Throwable ignore) {
                        }
                    }
//                    try {
//                        Thread.sleep(60 * 1000);
//                    } catch (InterruptedException ignore) {
//                    }
                    for (int i = 0; i < 600; i++) {
                        try {
                            Thread.sleep(300L);
                            grocer.fetch("1", 10000L);
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }).start();
        }
        try {
            Thread.sleep(60 * 60 * 1000);
        } catch (InterruptedException ignore) {
        }
    }

    private static void fetch100threads_purchase500delay() {
        Grocer<String, MyException> grocer = new Grocer<>(new Grocer.Purchaser<String, MyException>() {
            @Override
            public Grocer.Goods<String, MyException> purchase(String key, int quantity) {
                System.out.println("purchase " + quantity);
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ignore) {
                }
                List<String> elements = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    elements.add("FOO");
                }
                return Grocer.Goods.success(elements);
            }

            @Override
            public long defaultErrorDuration() {
                return 1000L;
            }
        }, 10, 0.5f, 16, 10000, 60);

        for (int t = 0; t < 100; t++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000; i++) {
                        try {
                            grocer.fetch("1", 10000L);
                        } catch (Throwable ignore) {
                        }
                    }
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException ignore) {
                    }
                    for (int i = 0; i < 1000; i++) {
                        try {
                            grocer.fetch("1", 10000L);
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }).start();
        }
        try {
            Thread.sleep(60 * 60 * 1000);
        } catch (InterruptedException ignore) {
        }
    }

    private static class MyException extends Exception {
        public MyException(String message) {
            super(message);
        }
        public MyException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
