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
    public void common() {

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

        Map<String, Object> root = createRootMap();
        header = ElementVisitor.of(root)
                .child("Header")
                .removeAsMap();
        Assertions.assertEquals("{Service=Foo}", header.toString());
        Assertions.assertEquals("{Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[{OrderName=Fish, Quantity=6}, {OrderName=Milk, Quantity=3}], CustomerName=Tom}, {Orders=[{OrderName=Mince, Quantity=1}, {OrderName=Lemonade, Quantity=12}], CustomerName=Jerry}]}}", root.toString());

        root = createRootMap();
        Collection<String> orderNames = ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .removeAllAs(String.class);
        Assertions.assertEquals("[Fish, Milk, Mince, Lemonade]", orderNames.toString());
        Assertions.assertEquals("{Header={Service=Foo}, Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[{Quantity=6}, {Quantity=3}], CustomerName=Tom}, {Orders=[{Quantity=1}, {Quantity=12}], CustomerName=Jerry}]}}", root.toString());

        root = createRootMap();
        ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .forEach()
                .delete();
        Assertions.assertEquals("{Header={Service=Foo}, Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[{Quantity=6}, {Quantity=3}], CustomerName=Tom}, {Orders=[{Quantity=1}, {Quantity=12}], CustomerName=Jerry}]}}", root.toString());

        root = createRootMap();
        ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .forEach()
                .delete();
        Assertions.assertEquals("{Header={Service=Foo}, Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[], CustomerName=Tom}, {Orders=[], CustomerName=Jerry}]}}", root.toString());

        root = createRootMap();
        ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .forEach()
                .replaceAs(String.class, e -> "Replaced");
        Assertions.assertEquals("{Header={Service=Foo}, Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[{OrderName=Replaced, Quantity=6}, {OrderName=Replaced, Quantity=3}], CustomerName=Tom}, {Orders=[{OrderName=Replaced, Quantity=1}, {OrderName=Replaced, Quantity=12}], CustomerName=Jerry}]}}", root.toString());


        ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .forEach()
                .replaceAsMap(e -> "Replaced");
        Assertions.assertEquals("{Header={Service=Foo}, Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[Replaced, Replaced], CustomerName=Tom}, {Orders=[Replaced, Replaced], CustomerName=Jerry}]}}", root.toString());

    }

    @Test
    public void createIfAbsent() {

        Map<String, Object> root = createRootMap();
        ElementVisitor.of(root)
                .child("Comment")
                .child("ServerDesc")
                .createIfAbsent(HashMap::new)
                .forEach()
                .consumeAsMap(e -> {
                    e.put("IP", "127.0.0.1");
                    e.put("Port", "8080");
                });
        Assertions.assertEquals("{Comment={ServerDesc={IP=127.0.0.1, Port=8080}}, Header={Service=Foo}, Body={Address=P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345, Customers=[{Orders=[{OrderName=Fish, Quantity=6}, {OrderName=Milk, Quantity=3}], CustomerName=Tom}, {Orders=[{OrderName=Mince, Quantity=1}, {OrderName=Lemonade, Quantity=12}], CustomerName=Jerry}]}}", root.toString());

    }

    @Test
    public void suppressError() {

        Collection<String> names = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName2")
                .suppressErrorCategories(ElementVisitor.ErrorCategory.DATA_MISSING)
                .forEach()
                .consumeAs(String.class, names::add);
        Assertions.assertEquals("[]", names.toString());

        names = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
//                .children() expected map, found collection
                .child("Orders")
                .children()
                .child("OrderName")
                .suppressErrorCategories(ElementVisitor.ErrorCategory.DATA_INVALID)
                .forEach()
                .consumeAs(String.class, names::add);
        Assertions.assertEquals("[]", names.toString());

        List<String> names2 = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .suppressErrorCodes(ElementVisitor.ErrorCode.EXPECTED_ELEMENT_TYPE_MISMATCH)
                .forEach()
                .consumeAsMap(e -> {names2.add(e.toString());});
        Assertions.assertEquals("[]", names2.toString());

        List<String> names3 = new ArrayList<>();
        ElementVisitor.of((Map<String, Object>) null)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .suppressErrorCodes(ElementVisitor.ErrorCode.MISSING_ROOT_ELEMENT)
                .forEach()
                .consumeAsMap(e -> {names3.add(e.toString());});
        Assertions.assertEquals("[]", names3.toString());

        List<String> names4 = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders2")
                .children()
                .child("OrderName")
                .suppressErrorCodes(ElementVisitor.ErrorCode.MISSING_PARENT_ELEMENT)
                .forEach()
                .consumeAsMap(e -> {names4.add(e.toString());});
        Assertions.assertEquals("[]", names4.toString());

        List<String> names5 = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName2")
                .suppressErrorCodes(ElementVisitor.ErrorCode.MISSING_EXPECTED_ELEMENT)
                .forEach()
                .consumeAsMap(e -> {names5.add(e.toString());});
        Assertions.assertEquals("[]", names5.toString());

    }

    @Test
    public void errorHandler() {

        Collection<String> exceptions = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName2")
                .exceptionHandler(e -> {
                    exceptions.add(e.getErrorCode().toString());
                })
                .forEach()
                .consumeAs(String.class, e -> {});
        Assertions.assertEquals("[DATA_MISSING/MISSING_EXPECTED_ELEMENT, DATA_MISSING/MISSING_EXPECTED_ELEMENT, DATA_MISSING/MISSING_EXPECTED_ELEMENT, DATA_MISSING/MISSING_EXPECTED_ELEMENT]", exceptions.toString());

        Collection<String> exceptions2 = new ArrayList<>();
        ElementVisitor.of(createRootMap())
                .child("Body")
                .child("Customers")
//                .children() expected map, found collection
                .child("Orders")
                .children()
                .child("OrderName")
                .exceptionHandler(e -> {
                    exceptions2.add(e.getErrorCode().toString());
                })
                .forEach()
                .consumeAs(String.class, e -> {});
        Assertions.assertEquals("[DATA_INVALID/PARENT_ELEMENT_TYPE_MISMATCH]", exceptions2.toString());

    }

    @Test
    public void errorMessageTest() {

        try {
            ElementVisitor.of((Map<String, Object>) null)
                    .child("Header")
                    .getAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_MISSING/MISSING_ROOT_ELEMENT: The 'root' element is null. (The element you expect: root.Header)", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Body")
                    .child("Customers")
                    .child("Orders")
                    .getAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_INVALID/PARENT_ELEMENT_TYPE_MISMATCH: Parent element 'root.Body.Customers' is not an instance of Map (it's java.util.ArrayList), unable to get child 'Orders' from it. (The element you expect: root.Body.Customers.Orders)", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Comment")
                    .createIfAbsent(() -> null)
                    .getAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("PROGRAMMING_ERROR/CREATE_EXPECTED_ELEMENT_FAILED: Failed to create expected element 'root.Comment' from 'Supplier'. The 'Supplier' was set via the createIfAbsent(Supplier) method. (The element you expect: root.Comment)", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Comment")
                    .getAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_MISSING/MISSING_EXPECTED_ELEMENT: Expected element 'root.Comment' does not exist. (The element you expect: root.Comment)", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Header")
                    .getAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_INVALID/EXPECTED_ELEMENT_TYPE_MISMATCH: Expected element 'root.Header' does not match the type you expected 'java.lang.String', it's java.util.HashMap. (The element you expect: root.Header)", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Header2")
                    .child("Service")
                    .getAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_MISSING/MISSING_PARENT_ELEMENT: Parent element 'root.Header2' does not exist, can not get child or children from it. (The element you expect: root.Header2.Service)", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Header")
                    .children()
                    .child("Service")
                    .getAllAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_INVALID/PARENT_ELEMENT_TYPE_MISMATCH: Parent element 'root.Header' is not an instance of Collection (it's java.util.HashMap), unable to get children from it. (The element you expect: root.Header[*].Service)", e.getMessage());
        }

        try {
            Map<String, Object> map = createRootMap();
            ElementVisitor.of(map)
                    .child("Body")
                    .child("Customers")
                    .children()
                    .child("Orders")
                    .children()
                    .forEach()
                    .delete();

            ElementVisitor.of(map)
                    .child("Body")
                    .child("Customers")
                    .children()
                    .child("Orders")
                    .children()
                    .getAllAsMap();
        } catch (Exception e) {
            Assertions.assertEquals("DATA_MISSING/MISSING_EXPECTED_ELEMENT: Expected element 'root.Body.Customers[*].Orders[*]' does not exist. (The element you expect: root.Body.Customers[*].Orders[*])", e.getMessage());
        }

        try {
            ElementVisitor.of(createRootMap())
                    .child("Body")
                    .child("Customers")
                    .children()
                    .child("Orders")
                    .children()
                    .getAllAs(String.class);
        } catch (Exception e) {
            Assertions.assertEquals("DATA_INVALID/EXPECTED_ELEMENT_TYPE_MISMATCH: Expected element 'root.Body.Customers[0].Orders[0]' does not match the type you expected 'java.lang.String', it's class java.util.HashMap. (The element you expect: root.Body.Customers[*].Orders[*])", e.getMessage());
        }

        try {
            Map<String, Object> map = createRootMap();
            ElementVisitor.of(map)
                    .child("Body")
                    .child("Customers")
                    .children()
                    .forEach()
                    .replaceAsMap(e -> null);

            ElementVisitor.of(map)
                    .child("Body")
                    .child("Customers")
                    .children()
                    .child("Orders")
                    .children()
                    .getAllAsMap();
        } catch (Exception e) {
            Assertions.assertEquals("DATA_MISSING/MISSING_PARENT_ELEMENT: Parent element 'root.Body.Customers[0]' is null, can not get child or children from it. (The element you expect: root.Body.Customers[*].Orders[*])", e.getMessage());
        }

    }

    /**
     * {
     *   "Header": {
     *     "Service": "Foo"
     *   },
     *   "Body": {
     *     "Address": "P.O. Box 456, 789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345",
     *     "Customers": [
     *       {
     *         "CustomerName": "Tom",
     *         "Orders": [
     *           {
     *             "OrderName": "Fish",
     *             "Quantity": "6"
     *           },
     *           {
     *             "OrderName": "Milk",
     *             "Quantity": "3"
     *           }
     *         ]
     *       },
     *       {
     *         "CustomerName": "Jerry",
     *         "Orders": [
     *           {
     *             "OrderName": "Mince",
     *             "Quantity": "1"
     *           },
     *           {
     *             "OrderName": "Lemonade",
     *             "Quantity": "12"
     *           }
     *         ]
     *       }
     *     ]
     *   }
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
