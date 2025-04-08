/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.java.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaBuilderTest implements LambdaBuildable {

    @Test
    public void test(){
        Bean bean = LambdaBuilder.object(() -> {
            Bean obj = new Bean();
            obj.setName("123");
            obj.setId("456");
            return obj;
        });
        Assertions.assertEquals("Bean{name='123', id='456'}", bean.toString());
        Map<String, Object> map = LambdaBuilder.hashMap(i -> {
            i.put("a", "b");
            i.put("c", "d");
        });
        Assertions.assertEquals("{a=b, c=d}", map.toString());
        List<String> list = LambdaBuilder.arrayList(i -> {
           i.add("a");
           i.add("b");
        });
        Assertions.assertEquals("[a, b]", list.toString());
    }

    /**
     *  {
     *    "Header": {
     *      "Service": "Foo",
     *      "Version": "1.0.0",
     *      "Time": "20250408",
     *      "Sequence": "202504080000357652"
     *    },
     *    "Body": {
     *      "Username": "test@test.com",
     *      "Password": "nDwkR+v1SUod6dkd9n4jxw==",
     *      "Orders": [
     *        {
     *          "Id": "0001",
     *          "Name": "Fish",
     *          "Quantity": "6",
     *          "UnitPrise": "68.8"
     *        },
     *        {
     *          "Id": "0002",
     *          "Name": "Milk",
     *          "Quantity": "3",
     *          "UnitPrise": "28.9"
     *        }
     *      ]
     *    }
     *  }
     */
    @Test
    public void test2() {

        // P.S. Lambda表达式的入参根据层级命名为m(第一层) mm(第二层) mmm(第三层), 可读性好, 不容易弄错
        Map<String, Object> map = buildHashMap(m -> {
            m.put("Header", buildHashMap(mm -> {
                mm.put("Service", "Foo");
                mm.put("Version", "1.0.0");
                mm.put("Time", "20250408");
                mm.put("Sequence", "202504080000357652");
            }));
            m.put("Body", buildHashMap(mm -> {
                mm.put("Username", "test@test.com");
                mm.put("Password", "nDwkR+v1SUod6dkd9n4jxw==");
                mm.put("Orders", buildArrayList(lll -> {
                    lll.add(buildHashMap(mmmm -> {
                        mmmm.put("Id", "0001");
                        mmmm.put("Name", "Fish");
                        mmmm.put("Quantity", "6");
                        mmmm.put("UnitPrise", "68.8");
                    }));
                    lll.add(buildHashMap(mmmm -> {
                        mmmm.put("Id", "0002");
                        mmmm.put("Name", "Milk");
                        mmmm.put("Quantity", "3");
                        mmmm.put("UnitPrise", "28.9");
                    }));
                }));
            }));
        });

        Map<String, Object> root = new HashMap<>();

        Map<String, Object> header = new HashMap<>();
        header.put("Service", "Foo");
        header.put("Version", "1.0.0");
        header.put("Time", "20250408");
        header.put("Sequence", "202504080000357652");

        Map<String, Object> body = new HashMap<>();
        body.put("Username", "test@test.com");
        body.put("Password", "nDwkR+v1SUod6dkd9n4jxw==");

        List<Map<String, Object>> orders = new ArrayList<>();

        Map<String, Object> order1 = new HashMap<>();
        order1.put("Id", "0001");
        order1.put("Name", "Fish");
        order1.put("Quantity", "6");
        order1.put("UnitPrise", "68.8");
        orders.add(order1);

        Map<String, Object> order2 = new HashMap<>();
        order2.put("Id", "0002");
        order2.put("Name", "Milk");
        order2.put("Quantity", "3");
        order2.put("UnitPrise", "28.9");
        orders.add(order2);

        body.put("Orders", orders);

        root.put("Header", header);
        root.put("Body", body);

        Assertions.assertEquals(root.toString(), map.toString());

    }

    private static final class Bean {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Bean{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

}
