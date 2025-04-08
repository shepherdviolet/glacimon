# Glacimon集合操作套件

# 目录

* [LambdaBuilder/LambdaBuildable使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/lambda-builder.md)
* [ElementVisitor使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/element-visitor.md)
* [KeyMapTranslator使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/map-key-translator.md)

# 简介

## `高可读性`的集合创建工具 LambdaBuilder/LambdaBuildable

* [LambdaBuilder/LambdaBuildable使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/lambda-builder.md)

* 假如要组装如下结构的Map

```
{
  "Header": {
    "Service": "Foo",
    "Time": "20250408",
    "Sequence": "202504080000357652"
  },
  "Body": {
    "Username": "test@test.com",
    "Orders": [
      {
        "Name": "Fish",
        "Quantity": "6",
        "UnitPrise": "68.8"
      },
      {
        "Name": "Milk",
        "Quantity": "3",
        "UnitPrise": "28.9"
      }
    ]
  }
}
```

* 使用传统方式组装

```
public class Test {
    public void process() {
        Map<String, Object> root = new HashMap<>();

        Map<String, Object> header = new HashMap<>();
        header.put("Service", "Foo");
        header.put("Time", "20250408");
        header.put("Sequence", "202504080000357652");

        Map<String, Object> body = new HashMap<>();
        body.put("Username", "test@test.com");

        List<Map<String, Object>> orders = new ArrayList<>();

        Map<String, Object> order1 = new HashMap<>();
        order1.put("Name", "Fish");
        order1.put("Quantity", "6");
        order1.put("UnitPrise", "68.8");
        orders.add(order1);

        Map<String, Object> order2 = new HashMap<>();
        order2.put("Name", "Milk");
        order2.put("Quantity", "3");
        order2.put("UnitPrise", "28.9");
        orders.add(order2);

        body.put("Orders", orders);

        root.put("Header", header);
        root.put("Body", body);
    }
}
```

* 使用LambdaBuildable组装

```
public class Test implements LambdaBuildable {
    public void process() {
        // P.S. Lambda表达式的入参根据层级命名为m(第一层) mm(第二层) mmm(第三层), 可读性好, 不容易弄错
        Map<String, Object> map = buildHashMap(m -> {
            m.put("Header", buildHashMap(mm -> {
                mm.put("Service", "Foo");
                mm.put("Time", "20250408");
                mm.put("Sequence", "202504080000357652");
            }));
            m.put("Body", buildHashMap(mm -> {
                mm.put("Username", "test@test.com");
                mm.put("Orders", buildArrayList(lll -> {
                    lll.add(buildHashMap(mmmm -> {
                        mmmm.put("Name", "Fish");
                        mmmm.put("Quantity", "6");
                        mmmm.put("UnitPrise", "68.8");
                    }));
                    lll.add(buildHashMap(mmmm -> {
                        mmmm.put("Name", "Milk");
                        mmmm.put("Quantity", "3");
                        mmmm.put("UnitPrise", "28.9");
                    }));
                }));
            }));
        });
    }
}
```

* 虽然使用LambdaBuildable并没有减少代码量, 但代码阅读起来有层级, 可读性高

* 另外, 如果示例中的List 'Orders' 数据源于另一个Map, 可以简化为:

```
public class Test implements LambdaBuildable {
    public void process() {
        // sourceDataOrders为源数据
        List<Map<String, Object>> sourceDataOrders = ...源数据...
        
        // P.S. Lambda表达式的入参根据层级命名为m(第一层) mm(第二层) mmm(第三层), 可读性好, 不容易弄错
        Map<String, Object> map = buildHashMap(m -> {
            m.put("Header", buildHashMap(mm -> {
                mm.put("Service", "Foo");
                mm.put("Time", "20250408");
                mm.put("Sequence", "202504080000357652");
            }));
            m.put("Body", buildHashMap(mm -> {
                mm.put("Username", "test@test.com");
                mm.put("Orders", buildArrayList(sourceDataOrders, HashMap::new, (src, dest) -> {
                
                    // [观察此处] 这里会遍历源集合sourceDataOrders, 把每一个元素都转换成目标集合的元素, 不用一个一个add了
                    
                    // 方法一: 手动赋值
                    // 第一个入参src是源集合的元素, 第二个入参dest是目标集合的元素, 此处要实现从src取值, 赋值到dest中
//                    dest.put("Name", src.get("category"));
//                    dest.put("Quantity", src.get("num"));
//                    dest.put("UnitPrise", src.get("univalent"));

                    // 方法二: 也可以配合MapKeyTranslator工具进行键映射
                    MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
                            "Name", "category",
                            "Quantity", "num",
                            "UnitPrise", "univalent"
                    ).translate(src, dest);
                    
                }));
            }));
        });
    }
}
```

* 在一些特殊场合下, LambdaBuildable可以让你直接在某个方法入参中创建一个简单的map, 免去了new一个的麻烦

```
    bean.setMap(buildHashMap(m -> {
        m.put("name", "value");
    }));
```

<Br>
<Br>

## `极简`的跨层级集合元素访问工具 ElementVisitor

* [ElementVisitor使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/element-visitor.md)

* ......(未完待续)......

<Br>
<Br>

## Map键映射工具 KeyMapTranslator

* [KeyMapTranslator使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/map-key-translator.md)

* 在业务逻辑开发过程中, 经常会遇到一个Map向另一个Map赋值的情况, 两边的key不相同, 如下所示:

```
    Map<String, Object> src = ...源数据...
    Map<String, Object> dest = new HashMap<>();
    
    dest.put("AAA", src.get("aaa"));
    dest.put("BBB", src.get("bbb"));
    dest.put("CCC", src.get("ccc"));
    dest.put("DDD", src.get("ddd"));
    dest.put("EEE", src.get("eee"));
    dest.put("FFF", src.get("fff"));
```

* KeyMapTranslator可以简化书写 (比如从Excel中复制出来, 把分隔符替换为",")

```
    Map<String, Object> src = ...源数据...
    Map<String, Object> dest = new HashMap<>();

    MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
            "AAA",  "aaa",
            "BBB",  "bbb",
            "CCC",  "ccc",
            "DDD",  "ddd",
            "EEE",  "eee",
            "FFF",  "fff"
    ).translate(src, dest);
```

```
    Map<String, Object> src = ...源数据...

    Map<String, Object> dest = MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
            "AAA",  "aaa",
            "BBB",  "bbb",
            "CCC",  "ccc",
            "DDD",  "ddd",
            "EEE",  "eee",
            "FFF",  "fff"
    ).translate(src);
```

<Br>
<Br>