# Glacimon集合操作套件

# 目录

* [`高可读性`的集合创建工具 LambdaBuilder/LambdaBuildable 详细文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md)
* [跨层级集合元素访问工具 ElementVisitor 详细文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/element-visitor.md)
* [Map键映射工具 KeyMapTranslator 详细文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/map-key-translator.md)

# 简介

## `高可读性`的集合创建工具 LambdaBuilder/LambdaBuildable

* [LambdaBuilder/LambdaBuildable 详细文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md)
* 同类工具对比:

> `HuTool/Guava`也提供了类似能力, 但层级多的时候容易漏写build()方法 (会赋值一个"奇怪"的对象到Map里), 为了写build()方法, 
> 括号也容易看不清. `LambdaBuilder/LambdaBuildable`使用Lambda风格创建集合, 各有优缺点, 详细对比见: 
> [LambdaBuilder/LambdaBuildable详细文档末尾的'与同类工具对比'](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md).

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

* 虽然使用LambdaBuilder/LambdaBuildable并没有减少代码量, 但代码阅读起来有层级, 可读性高
* 在一些特殊场合下, LambdaBuilder/LambdaBuildable可以让你直接在某个方法入参中创建一个简单的map, 免去了new一个的麻烦

```
    bean.setMap(buildHashMap(m -> {
        m.put("name", "value");
    }));
```

* 另外, LambdaBuilder/LambdaBuildable还提供从Collection转List/Set的能力, 详见[LambdaBuilder/LambdaBuildable详细文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/lambda-builder.md)

<Br>
<Br>

## 跨层级集合元素访问工具 ElementVisitor

* [ElementVisitor 使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/element-visitor.md)
* 同类工具对比:

> [Jayway JsonPath](https://github.com/json-path/JsonPath)也提供了类似的能力, 它是一个广泛使用的 JSONPath 实现，它允许你用类似 $.Body.Orders[*].Name 
> 的语法直接从 JSON 或 Map 数据结构中获取数据, 功能强大(筛选/统计等), 但表达式由String编写存在一点学习成本, 如果你熟悉Jayway JsonPath, 
> 请忽略`ElementVisitor`. `ElementVisitor`功能简单, 通过链式书写, 有多少功能靠IDE提示一目了然, 适合简单的场景, 还提供了元素替换, 路径
> 创建能力.

* **假如我们要从如下集合中获取所有的`OrderName`字段** (ElementVisitor支持Map/List/Set/Collection)

```
{
  "Header": {
    "Service": "Foo"
  },
  "Body": {
    "Customers": [
      {
        "CustomerName": "Tom",
        "Orders": [
          {
            "OrderName": "Fish",
            "Quantity": "6"
          },
          {
            "OrderName": "Milk",
            "Quantity": "3"
          }
        ]
      },
      {
        "CustomerName": "Jerry",
        "Orders": [
          {
            "OrderName": "Mince",
            "Quantity": "1"
          },
          {
            Order"Name": "Lemonade",
            "Quantity": "12"
          }
        ]
      }
    ]
  }
}
```

### 使用传统方式 (不借助任何工具)

* 观察以下代码
* * 你需要一层一层地获取, 从Map里get, 用for循环遍历List.
* * 每一次获取/遍历, 你都需要进行空值检查, 类型检查. 一不小心就会出现NullPointException或者ClassCastException.
* * 每一次检查, 你都需要编写有用的错误信息来帮助故障定位.
* * 最关键的问题是, 结构越复杂, 可读性越差.

```
        List<String> orderNames = new ArrayList<>();

        Map<String, Object> body = (Map<String, Object>) root.get("Body");
        if (body == null) {
            throw new ServiceException("root.Body is null");
        }
        List<Map<String, Object>> customers = (List<Map<String, Object>>) body.get("Customers");
        if (customers == null) {
            throw new ServiceException("root.Customers is null");
        }
        for (Map<String, Object> customer : customers) {
            List<Map<String, Object>> orders = (List<Map<String, Object>>) customer.get("Orders");
            if (orders == null) {
                throw new ServiceException("root.Customers.Orders is null");
            }
            for (Map<String, Object> order : orders) {
                String orderName = (String) order.get("OrderName");
                if (orderName == null) {
                    throw new ServiceException("root.Customers.Orders.OrderName is null");
                }
                orderNames.add(orderName);
            }
        }

        System.out.printf("Order names: %s\n", orderNames);
```

### 使用ElementVisitor

* 观察以下代码
* * 使用child和children方法配置元素访问路径, 等效于JsonPath表达式: $.Body.Customers[*].Orders[*].OrderName
* * getAllAs(String.class)方法返回满足条件的所有元素 (如未设置exceptionHandler, 则可能抛出ElementVisitException)

```
        List<String> orderNames = ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .child("OrderName")
                .getAllAs(String.class); // 如果获取失败会抛出ElementVisitException (详见'异常处理'章节)

        System.out.printf("Order names: %s\n", orderNames);
```

* 你可以配置异常处理器(exceptionHandler)处理异常. (ElementVisitException错误信息中已包含元素访问路径, 便于故障定位)

```
        List<String> orderNames = ElementVisitor.of(root)
                ......
                .exceptionHandler(e -> {throw new ServiceException(e.getMessage(), e);}) // 示例一: 转为ServiceException抛出
                //.exceptionHandler(e -> logger.error(e.getMessage(), e)) // 示例二: 只打印日志, 不抛出异常
                .getAllAs(String.class); // 注意, 此时返回值可能为空List (即size为0的List)
```

* 你可以使用try-catch处理异常

```
    try {
        List<String> orderNames = ElementVisitor.of(root)
                ......
                .getAllAs(String.class);
    } catch (ElementVisitException e) {
        ......
    }
```

* 你还可以直接压制(忽略)指定异常

```
        List<String> orderNames = ElementVisitor.of(root)
                .suppressErrorCategories(ElementVisitor.ErrorCategory.DATA_MISSING, ElementVisitor.ErrorCategory.DATA_INVALID) // 压制元素不存在/元素类型不匹配的错误
                .getAllAs(String.class); // 注意, 此时返回值可能为空List (即size为0的List)
```

* ElementVisitor提供了多种元素访问方式

```
        // 此处仅展示几种用法

        // 获取并从集合中移除
        List<String> orderNames = ElementVisitor.of(root)
                ......
                .removeAllAs(String.class);
        
        // 遍历, 并往$.Body.Customers[*].Orders[*]中添加字段Comment
        ElementVisitor.of(root)
                .child("Body")
                .child("Customers")
                .children()
                .child("Orders")
                .children()
                .forEach()
                .consumeAsMap(e -> {
                    e.put("Comment", "This is a comment");
                });
        
        // 遍历, 替换为新的元素
        ElementVisitor.of(root)
                ......
                .forEach()
                .replaceAsMap(e -> {
                    Map<String, Object> newElement = new HashMap<>();
                    newElement.put("Name", e.get("OrderName"));
                    newElement.put("Num", e.get("Quantity"));
                    return newElement;
                });
        
```

<Br>
<Br>

## Map键映射工具 KeyMapTranslator

* [KeyMapTranslator 使用文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/collections/map-key-translator.md)

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