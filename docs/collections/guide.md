# Glacimon集合操作套件

# 目录

* [`高可读性`的集合创建工具 LambdaBuilder/LambdaBuildable 使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md)
* [`便捷安全`的跨层级集合元素访问工具ElementVisitor 使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/element-visitor.md)
* [Map键映射工具 KeyMapTranslator 使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/map-key-translator.md)

# 简介

## `高可读性`的集合创建工具 LambdaBuilder/LambdaBuildable

* [LambdaBuilder/LambdaBuildable使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md)
* 同类工具对比:

> `HuTool/Guava`也提供了类似能力, 但层级多的时候容易漏写build()方法 (会赋值一个"奇怪"的对象到Map里), 为了写build()方法, 
> 括号也容易看不清. `LambdaBuilder/LambdaBuildable`使用Lambda风格创建集合, 各有优缺点, 详细对比见: 
> [LambdaBuilder/LambdaBuildable使用文档末尾的'与同类工具对比'](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md).

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

* 另外, 如果示例中的列表'Orders'数据源自一个List, 可以简化书写:

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
                
                // [观察此处]
                // 这里使用buildArrayList(srcCollection, destElementSupplier, destElementAssembler)方法实现'源Collection转List/Set'
                mm.put("Orders", buildArrayList(sourceDataOrders, HashMap::new, (src, dest) -> {
                
                    // [观察此处]
                    // 这里会遍历源集合sourceDataOrders, 把每一个元素都转换成目标集合的元素, 不用一个一个往List add元素了
                    
                    // 方法一: 手动赋值
                    // 第一个入参src是源集合的元素, 第二个入参dest是目标集合的元素, 此处要实现从src取值, 赋值到dest中
                    dest.put("Name", src.get("category"));
                    dest.put("Quantity", src.get("num"));
                    dest.put("UnitPrise", src.get("univalent"));

                    // 方法二: 也可以配合MapKeyTranslator工具进行键映射
//                    MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
//                            "Name", "category",
//                            "Quantity", "num",
//                            "UnitPrise", "univalent"
//                    ).translate(src, dest);
                    
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

## `便捷安全`的跨层级集合元素访问工具 ElementVisitor

* [ElementVisitor使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/element-visitor.md)
* 同类工具对比:

> [Jayway JsonPath](https://github.com/json-path/JsonPath)是一个广泛使用的 JSONPath 实现，它允许你用类似 $.Body.Orders[*].Name 
> 的语法直接从 JSON 或 Map 数据结构中获取数据, 功能强大(筛选/统计等), 但表达式由String编写存在一点学习成本, 如果你熟悉Jayway JsonPath, 
> 请忽略`ElementVisitor`. `ElementVisitor`功能简单, 通过链式书写, 有多少功能靠IDE提示一目了然, 适合简单的场景, 还提供了元素替换, 路径
> 创建能力.

* ......(未完待续)......

<Br>
<Br>

## Map键映射工具 KeyMapTranslator

* [KeyMapTranslator使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/map-key-translator.md)

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