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

import java.util.*;

public class ElementVisitorTest implements LambdaBuildable, ElementVisitable {

    @Test
    public void test() {

        String service = ElementVisitor.of(createRootMap())
                .child("Header")
                .child("Service")
                .getAs(String.class);
        Assertions.assertEquals("Foo", service);

        Map<String, Object> header = ElementVisitor.of(createRootMap())
                .child("Header")
                .getAsMap();
        Assertions.assertEquals("{Service=Foo}", header.toString());

        Collection<String> names = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .forEach()
                .consumeAs(String.class, names::add);
        Assertions.assertEquals("[Fish, Milk, Mince, Lemonade]", names.toString());

        names = ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .getAllAs(String.class);
        Assertions.assertEquals("[Fish, Milk, Mince, Lemonade]", names.toString());

        Collection<Map<String, Object>> ordersChildren = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .forEach()
                .consumeAsMap(ordersChildren::add);
        Assertions.assertEquals("[{OrderName=Fish, Quantity=6}, {OrderName=Milk, Quantity=3}, {OrderName=Mince, Quantity=1}, {OrderName=Lemonade, Quantity=12}]", ordersChildren.toString());

        ordersChildren = ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .getAllAsMap();
        Assertions.assertEquals("[{OrderName=Fish, Quantity=6}, {OrderName=Milk, Quantity=3}, {OrderName=Mince, Quantity=1}, {OrderName=Lemonade, Quantity=12}]", ordersChildren.toString());

        Collection<List<Object>> orders = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .forEach()
                .consumeAsList(orders::add);
        Assertions.assertEquals("[[{OrderName=Fish, Quantity=6}, {OrderName=Milk, Quantity=3}], [{OrderName=Mince, Quantity=1}, {OrderName=Lemonade, Quantity=12}]]", orders.toString());

        orders = ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .getAllAsList();
        Assertions.assertEquals("[[{OrderName=Fish, Quantity=6}, {OrderName=Milk, Quantity=3}], [{OrderName=Mince, Quantity=1}, {OrderName=Lemonade, Quantity=12}]]", orders.toString());

    }

    /**
     * {
     * "Header": {
     * "Service": "Foo"
     * },
     * "Body": {
     * "Address": "P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345",
     * "Customers": [
     * {
     * "CustomerName": "Tom",
     * "Orders": [
     * {
     * "OrderName": "Fish",
     * "Quantity": "6"
     * },
     * {
     * "OrderName": "Milk",
     * "Quantity": "3"
     * }
     * ]
     * },
     * {
     * "CustomerName": "Jerry",
     * "Orders": [
     * {
     * "OrderName": "Mince",
     * "Quantity": "1"
     * },
     * {
     * Order"Name": "Lemonade",
     * "Quantity": "12"
     * }
     * ]
     * }
     * ]
     * }
     * }
     */
    private Map<String, Object> createRootMap() {
        return buildHashMap(m -> {
            m.put("Header", buildHashMap(mm -> {
                mm.put("Service", "Foo");
            }));
            m.put("Body", buildHashMap(mm -> {
                mm.put("Address", "P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345");
                mm.put("Customers", buildArrayList(lll -> {
                    lll.add(buildHashMap(mmmm -> {
                        mmmm.put("CustomerName", "Tom");
                        mmmm.put("Orders", buildArrayList(lllll -> {
                            lllll.add(buildHashMap(mmmmmm -> {
                                mmmmmm.put("OrderName", "Fish");
                                mmmmmm.put("Quantity", "6");
                            }));
                            lllll.add(buildHashMap(mmmmmm -> {
                                mmmmmm.put("OrderName", "Milk");
                                mmmmmm.put("Quantity", "3");
                            }));
                        }));
                    }));
                    lll.add(buildHashMap(mmmm -> {
                        mmmm.put("CustomerName", "Jerry");
                        mmmm.put("Orders", buildArrayList(lllll -> {
                            lllll.add(buildHashMap(mmmmmm -> {
                                mmmmmm.put("OrderName", "Mince");
                                mmmmmm.put("Quantity", "1");
                            }));
                            lllll.add(buildHashMap(mmmmmm -> {
                                mmmmmm.put("OrderName", "Lemonade");
                                mmmmmm.put("Quantity", "12");
                            }));
                        }));
                    }));
                }));
            }));
        });
    }

}
