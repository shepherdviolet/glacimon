# 跨层级集合元素访问工具 ElementVisitor

# ElementVisitor有什么用?

* **假如我们要从下面的集合中获取所有的`OrderName`字段** (ElementVisitor支持Map/List/Set/Collection)

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

* 你可以配置错误信息前缀, ElementVisitException错误信息前面会添加你指定的内容, 便于排查问题

```
        List<String> orderNames = ElementVisitor.of(root)
                ......
                .exceptionMessagePrefix("Response message format is incorrect.\n") // 添加错误信息前缀
                .getAllAs(String.class);
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
                ......
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
                .consumeAsMap(map -> {
                    map.put("Comment", "This is a comment");
                });
        
        // 遍历, 替换为新的元素
        ElementVisitor.of(root)
                ......
                .forEach()
                .replaceAsMap(map -> {
                    Map<String, Object> newMap = new HashMap<>();
                    newMap.put("Name", map.get("OrderName"));
                    newMap.put("Num", map.get("Quantity"));
                    return newElement;
                });
        
```

# 名词解释

> 如果觉得生涩, 可以跳过名词解释, 直接看使用示例

* **path: 访问路径:** 描述`你想访问的元素`与`根元素`之间的关系
* * 对应示例代码: `.child("Body").child("Customers").children().child("Orders").children().child("OrderName")`
* * 等效于JsonPath表达式: `$.Body.Customers[*].Orders[*].OrderName`
* **root_element: 根元素:** `ElementVisitor.of(...)`方法的入参, 即为源数据集合
* * 对应示例中: `$`.Body.Customers[*].Orders[*].OrderName
* **parent_element: 路径中间元素:** 除了`根元素`和`你想访问的元素`以外的中间环节
* * 对应示例中: $.`Body.Customers[*].Orders[*]`.OrderName
* **expected_element: 你想访问的元素:** 你想获取/移除/消费/替换的元素
* * 对应示例中: $.Body.Customers[*].Orders[*].`OrderName`
* **oneway: 单路访问:** `根元素`和`路径中间元素`都是Map类型, 即`访问路径`中只用child(String)方法, 不用children()方法
* * 单路访问提供: getAsMap getAsList getAsSet getAs removeAsMap removeAsList removeAsSet removeAs 方法, 返回单个元素
* * 单路访问提供: 缺失自动创建(createIfAbsent)方法, `路径中间元素`和`你想访问的元素`不存在时自动创建
* **multiway: 多路访问:** `根元素`和`路径中间元素`存在List/Set/Collection类型, 即`访问路径`中用了children()方法
* * 多路访问提供: getAsMap getAllAsList getAllAsSet getAllAs removeAllAsMap removeAllAsList removeAllAsSet removeAllAs 方法, 返回多个元素(List)

<br>
<br>

# 访问元素

* 默认情况下: get.../remove.../consume.../replace...方法一定会返回元素, 不为null, 不为空List. 以下情况会抛出ElementVisitException, 请参考`异常处理`章节妥善处理异常, 本章节不做示范
* * `访问路径`中的元素(根元素/路径中间元素/你想访问的元素)不存在时
* * `根元素`/`路径中间元素`的类型与`访问路径`期望的不符 (不为Map或不为Collection)
* * `你想访问的元素`的类型与期望的不符 (由getAs/removeAs/consumeAs/replaceAs访问方法指定)
* * 其他原因 (详见`异常处理`章节)
* 压制异常(suppressError)后: 指定错误不再抛出异常, 但get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0)
* 设置异常处理器(exceptionHandler)后: 异常是否抛出, 抛出什么异常, 由exceptionHandler决定. get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0) 

<br>

## 访问路径

* 源数据集合 (示例)

```
{
  "Header": {
    "Service": "Foo"
  },
  "Body": {
    "Address": "789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345",
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

* 访问`Body`集合本身(是个Map) `(单路访问)`

```
        ElementVisitor.of(root)
                .child("Body") // 相当于root.get("Body"), 但要注意, 这个方法并不是真的当场在做get
                ......
        // 等效于JSONPath: $.Body
```

* 访问`Address`字段(是个String) `(单路访问)`

```
        ElementVisitor.of(root)
                .child("Body") // 相当于root.get("Body"), 但要注意, 这个方法并不是真的当场在做get
                .child("Address") // 相当于body.get("Address"), 但要注意, 这个方法并不是真的当场在做get
                ......
        // 等效于JSONPath: $.Body.Address
```

* 访问`Customers`集合本身(是个List) `(单路访问)`

```
        ElementVisitor.of(root)
                .child("Body") // 相当于root.get("Body"), 但要注意, 这个方法并不是真的当场在做get
                .child("Customers") // 相当于body.get("Customers"), 但要注意, 这个方法并不是真的当场在做get
                ......
        // 等效于JSONPath: $.Body.Customers
```

* 访问`Customers`集合所有的子元素(是两个Map) `(多路访问)`

```
        ElementVisitor.of(root)
                .child("Body") // 相当于root.get("Body"), 但要注意, 这个方法并不是真的当场在做get
                .child("Customers") // 相当于body.get("Customers"), 但要注意, 这个方法并不是真的当场在做get
                .children() // 访问(遍历)List/Set/Collection的子元素, 上面的Customers是个List
                ......
        // 等效于JSONPath: $.Body.Customers[*]
```

* 访问所有的`Orders`集合的所有子元素(是四个Map) `(多路访问)`

```
        ElementVisitor.of(root)
                .child("Body") // 相当于root.get("Body"), 但要注意, 这个方法并不是真的当场在做get
                .child("Customers") // 相当于body.get("Customers"), 但要注意, 这个方法并不是真的当场在做get
                .children() // 访问(遍历)List/Set/Collection的子元素, 上面的Customers是个List
                .child("Orders") // 相当于customer.get("Orders"), 但要注意, 这个方法并不是真的当场在做get
                .children() // 访问(遍历)List/Set/Collection的子元素, 上面的Orders是个List
                ......
        // 等效于JSONPath: $.Body.Customers[*].Orders[*]
```

* 访问所有的`OrderName`字段 `(多路访问)`

```
        ElementVisitor.of(root)
                .child("Body") // 相当于root.get("Body"), 但要注意, 这个方法并不是真的当场在做get
                .child("Customers") // 相当于body.get("Customers"), 但要注意, 这个方法并不是真的当场在做get
                .children() // 访问(遍历)List/Set/Collection的子元素, 上面的Customers是个List
                .child("Orders") // 相当于customer.get("Orders"), 但要注意, 这个方法并不是真的当场在做get
                .children() // 访问(遍历)List/Set/Collection的子元素, 上面的Orders是个List
                .child("OrderName") // 相当于order.get("OrderName"), 但要注意, 这个方法并不是真的当场在做get
                ......
        // 等效于JSONPath: $.Body.Customers[*].Orders[*].OrderName
```


<br>

## 获取元素

* 单路访问 (`访问路径`中不存在List/Set/Collection, 即从未调用过children()方法)

```
        Map<String, Object> result = ElementVisitor.of(root)
                ......
                .getAsMap(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<String> result = ElementVisitor.of(root)
                ......
                .getAsList(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        Set<String> result = ElementVisitor.of(root)
                ......
                .getAsSet(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        String result = ElementVisitor.of(root)
                ......
                .getAs(String.class); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

* 多路访问 (`访问路径`中存在List/Set/Collection, 即调用过children()方法)

```
        List<Map<String, Object>> result = ElementVisitor.of(root)
                ......
                .getAllAsMap(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<List<String>> result = ElementVisitor.of(root)
                ......
                .getAllAsList(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<Set<String>> result = ElementVisitor.of(root)
                ......
                .getAllAsSet(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<String> result = ElementVisitor.of(root)
                ......
                .getAllAs(String.class); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

<br>

## 获取并移除元素

* 单路访问 (`访问路径`中不存在List/Set/Collection, 即从未调用过children()方法)

```
        Map<String, Object> result = ElementVisitor.of(root)
                ......
                .removeAsMap(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<String> result = ElementVisitor.of(root)
                ......
                .removeAsList(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        Set<String> result = ElementVisitor.of(root)
                ......
                .removeAsSet(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        String result = ElementVisitor.of(root)
                ......
                .removeAs(String.class); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

* 多路访问 (`访问路径`中存在List/Set/Collection, 即调用过children()方法)

```
        List<Map<String, Object>> result = ElementVisitor.of(root)
                ......
                .removeAllAsMap(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<List<String>> result = ElementVisitor.of(root)
                ......
                .removeAllAsList(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<Set<String>> result = ElementVisitor.of(root)
                ......
                .removeAllAsSet(); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        List<String> result = ElementVisitor.of(root)
                ......
                .removeAllAs(String.class); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

<br>

## 遍历:消费元素

* 在Lambda表达式中消费(处理)你想访问的元素
* 注意: 如果出现异常, 但异常被压制或被exceptionHandler忽略, Lambda表达式将不会被执行 (对于不存在的元素)

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .consumeAsMap(map -> {
                    // 在此处对你想访问的元素进行处理
                    //map.put("your key", "your value");
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .consumeAsList(list -> {
                    // 在此处对你想访问的元素进行处理
                    //list.add("your value");
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .consumeAsSet(set -> {
                    // 在此处对你想访问的元素进行处理
                    //set.add("your value");
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .consumeAs(String.class, str -> {
                    // 在此处对你想访问的元素进行处理
                    //logger.info(str);
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

<br>

## 遍历:替换元素

* 在Lambda表达式中接收你想访问的元素, 然后返回新的元素 (替换旧元素)
* 注意: 如果出现异常, 但异常被压制或被exceptionHandler忽略, Lambda表达式将不会被执行 (对于不存在的元素)

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .replaceAsMap(map -> {
                    // 在此处接收你想访问的元素, 然后返回新的元素 (替换旧元素), 新元素类型可以不是原类型 (可以是任意你想要的类型)
                    //Map<String, Object> newMap = new HashMap<>();
                    //newMap.put("Name", map.get("OrderName"));
                    //newMap.put("Num", map.get("Quantity"));
                    //return newElement;
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .replaceAsList(list -> {
                    // 在此处接收你想访问的元素, 然后返回新的元素 (替换旧元素), 新元素类型可以不是原类型 (可以是任意你想要的类型)
                    //return list.toString();
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .replaceAsSet(set -> {
                    // 在此处接收你想访问的元素, 然后返回新的元素 (替换旧元素), 新元素类型可以不是原类型 (可以是任意你想要的类型)
                    //return set.toString();
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .replaceAs(String.class, str -> {
                    // 在此处接收你想访问的元素, 然后返回新的元素 (替换旧元素), 新元素类型可以不是原类型 (可以是任意你想要的类型)
                    return str.split(",");
                }); // 如果获取失败可能会抛出ElementVisitException (详见'异常处理'章节)
```

<br>

## 遍历:删除元素

```
        ElementVisitor.of(root)
                ......
                .forEach()
                .delete(); // 这个方法不会抛出异常(即使你想访问的元素不存在或者类型不匹配)
```

<br>

## 缺失自动创建(createIfAbsent) (只支持`单路访问`模式)

* 假如我们想从下面的集合中获取`$.Header.Customer`元素, 并在里面添加一个字段`Address`

```
{
  "Header": {
    "Customer": {  <-- 想获取这个元素,并在里面添加一个字段`Address`
      "CustomerName": "Tom"
    }
  },
  "Body": {
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
  }
}
```

* 但是, 实际的源数据集合中`$.Header`和`$.Header.Customer`元素不存在, 如下所示:

```
{  <-- 没有Header, 没有Customer
  "Body": {
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
  }
}
```

* 我们可以使用`缺失自动创建(createIfAbsent)`功能, 自动创建缺失的`路径中间元素`和`你想访问的元素`

```
        ElementVisitor.of(root) // 注意: `根元素`缺失无法自动创建
                .child("Header") // `路径中间元素`缺失能够自动创建, 实例类型为: LinkedHashMap
                .child("Customer") // `你想访问的元素`缺失能够自动创建, 实例由createIfAbsent入参Supplier创建, 不限制元素类型(可以不是Map)
                .createIfAbsent(HashMap::new) // 配置'缺失自动创建', 设置一个Supplier用于创建`你想访问的元素` (如果它不存在的话, 可以不是Map)
                .forEach()
                .consumeAsMap(e -> {
                    e.put("Address", "789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345");
                });
```

* 执行上述代码后, 源数据集合变成:

```
{
  "Header": {  <-- 自动创建
    "Customer": {  <-- 自动创建
      "Address": "789 Elmwood Plaza, Village Heights, Region Delta, Continentia 12-345"
    }
  },
  "Body": {
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
  }
}
```

<br>
<br>

# 异常处理

| 异常类别(ErrorCategory) | 异常码(ErrorCode)                 | 发生原因                                                                       |
|---------------------|--------------------------------|----------------------------------------------------------------------------|
| DATA_MISSING        | MISSING_ROOT_ELEMENT           | `访问路径`中`根元素`为空, 即ElementVisitor.of(...)方法入参为空 (源数据集合为空)                    |
| DATA_MISSING        | MISSING_PARENT_ELEMENT         | `访问路径`中`路径中间元素`不存在                                                         |
| DATA_MISSING        | MISSING_EXPECTED_ELEMENT       | `访问路径`中`你想访问的元素`不存在                                                        |
| DATA_INVALID        | ROOT_ELEMENT_TYPE_MISMATCH     | `根元素`的类型与`访问路径`期望的不符 (不为Map或不为Collection)                                  |
| DATA_INVALID        | PARENT_ELEMENT_TYPE_MISMATCH   | `路径中间元素`的类型与`访问路径`期望的不符 (不为Map或不为Collection)                               |
| DATA_INVALID        | EXPECTED_ELEMENT_TYPE_MISMATCH | `你想访问的元素`的类型与期望的不符 (由getAs.../removeAs.../consumeAs.../replaceAs...访问方法指定) |
| PROGRAMMING_ERROR   | CREATE_EXPECTED_ELEMENT_FAILED | 创建你`想访问的元素`失败 (由createIfAbsent方法传入的表达式创建)                                  |
| PROGRAMMING_ERROR   | UNDEFINED_ERROR                | 未定义的错误, 实际访问中不会抛出此异常                                                       |

* 默认情况下: get.../remove.../consume.../replace...方法一定会返回元素, 不为null, 不为空List. 以下情况会抛出`ElementVisitException`, 请参考`异常处理`章节妥善处理异常, 本章节不做示范
* * `访问路径`中的元素(根元素/路径中间元素/你想访问的元素)不存在时
* * `根元素`/`路径中间元素`的类型与`访问路径`期望的不符 (不为Map或不为Collection)
* * `你想访问的元素`的类型与期望的不符 (由getAs/removeAs/consumeAs/replaceAs访问方法指定)
* * 创建你`想访问的元素`失败 (由createIfAbsent方法传入的表达式创建)
* 压制异常(suppressError)后: 指定错误不再抛出异常, 但get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0)
* 设置异常处理器(exceptionHandler)后: 异常是否抛出, 抛出什么异常, 由exceptionHandler决定. get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0)

<br>

* 关于ElementVisitException

```
// 获取错误信息 (已包含元素访问路径, 便于故障定位)
e.getMessage()
// 获取错误类别, 可针对不同的类别进行不同的处理
e.getErrorCategory()
// 获取错误码, 可针对不同的错误码进行不同的处理
e.geterrorCode()
// 发生错误时, 正在访问的元素的`访问路径`, JSONPath格式, 例如: $.Body.Orders[*]
e.getPathErrorOccurred()
// `你想访问的元素`的完整`访问路径`, JSONPath格式, 例如: $.Body.Orders[*].OrderName
e.getPathYouExpected()
// 发生错误时, 正在访问的元素的名称, 例如: Orders[*]
e.getElementNameErrorOccurred()
// `你想访问的元素`的名称, 例如: OrderName
e.getElementNameYouExpected()
```

<br>

## 设置错误信息前缀

```
        List<String> orderNames = ElementVisitor.of(root)
                ......
                .exceptionMessagePrefix("Response message format is incorrect.\n") // 设置错误信息前缀
                .getAllAs(String.class);
```

* 添加前缀前, `ElementVisitException`错误信息格式如下:

```
PARENT_ELEMENT_TYPE_MISMATCH: Parent element '$.Body.Customers' is not an instance of Map (it's java.util.ArrayList), unable to get child 'Orders' from it
{
  Body: {
    Customers: {    <-- Not Map
      Orders: <Expected>
    }
  }
}"
```

* 添加前缀后:

```
Response message format is incorrect.
PARENT_ELEMENT_TYPE_MISMATCH: Parent element '$.Body.Customers' is not an instance of Map (it's java.util.ArrayList), unable to get child 'Orders' from it
{
  Body: {
    Customers: {    <-- Not Map
      Orders: <Expected>
    }
  }
}"
```

* 添加前缀有助于问题排查

<br>

## try-catch方式处理

```
    try {
        ElementVisitor.of(root)
                ......
    } catch (ElementVisitException e) {
        ......
    }
```

* `不压制异常`也`不设置exceptionHandler`的情况下, get.../remove.../consume.../replace...方法的返回值不可能为null, 也不可能为空List, 因为不存在会直接抛出异常

<br>

## 压制(忽略)异常

* 根据异常类别压制

```
        ElementVisitor.of(root)
                ......
                .suppressErrorCategories(ElementVisitor.ErrorCategory.DATA_MISSING, ElementVisitor.ErrorCategory.DATA_INVALID) // 压制元素不存在/元素类型不符的错误
                ......
```

* 根据异常码压制

```
        ElementVisitor.of(root)
                ......
                .suppressErrorCodes(ElementVisitor.ErrorCode.MISSING_PARENT_ELEMENT, ElementVisitor.ErrorCode.MISSING_EXPECTED_ELEMENT) // 压制`路径中间元素不存在`和`你想访问的元素不存在`的错误
                ......
```

* 注意: exceptionHandler将无法接收到被压制的异常
* 注意: 设置压制异常后: get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0)
* suppressErrorCategories和suppressErrorCodes可以同时设置
* 调用suppressErrorCategories方法会覆盖之前用suppressErrorCategories方法设置的异常类别
* 调用suppressErrorCodes方法会覆盖之前用suppressErrorCodes方法设置的异常码

<br>

## 替换为其他异常抛出

```
        ElementVisitor.of(root)
                ......
                .exceptionHandler(e -> {throw new ServiceException(e.getMessage(), e);}) // 转为ServiceException抛出
                ......
```

* 注意: ExceptionHandler无法接收到被压制的异常
* 注意: 设置异常处理器(exceptionHandler)后: 异常是否抛出, 抛出什么异常, 由exceptionHandler决定. get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0)
* Lambda表达式入参e是一个ElementVisitException, 你可以通过它获取你想要的信息

<br>

## 仅打印错误日志

```
        ElementVisitor.of(root)
                ......
                .exceptionHandler(e -> logger.error(e.getMessage(), e)) // 只打印日志, 不抛出异常
                ......
```

* 注意: ExceptionHandler无法接收到被压制的异常
* 注意: 设置异常处理器(exceptionHandler)后: 异常是否抛出, 抛出什么异常, 由exceptionHandler决定. get.../remove.../consume.../replace...方法可能会返回null或者空List (size为0)
* Lambda表达式入参e是一个ElementVisitException, 你可以通过它获取你想要的信息

<br>

## 部分错误仅打印日志, 部分错误抛出异常

```
        ElementVisitor.of(root)
                ......
                .exceptionHandler(e -> {
                    switch (e.getErrorCategory()){
                        case DATA_MISSING:
                            // 元素不存在只打印日志, 不抛出异常
                            logger.error(e.getMessage(), e);
                        case DATA_INVALID:
                        case PROGRAMMING_ERROR:
                        default:
                            // 其他错误抛出异常
                            throw e;
                    }
                })
                ......
```

<br>

## 开发框架抽象类中预设异常处理规则

```
    public static abstract class AbsService {
        
        private final Logger logger = LoggerFactory.getLogger(getClass());
        
        // 在服务抽象类中预设异常处理规则
        protected ElementVisitor visitElement(Map<String, Object> root) {
            return ElementVisitor.of(root).exceptionHandler(e -> {
                switch (e.getErrorCategory()){
                    case DATA_MISSING:
                        logger.error(e.getMessage(), e);
                    case DATA_INVALID:
                    case PROGRAMMING_ERROR:
                    default:
                        throw e;
                }
            });
        }
        
        public abstract void process(Map<String, Object> context);
        
    }
    
    public static class FooService extends AbsService {
        
        @Override
        public void process(Map<String, Object> context) {
            
            // 使用父类方法访问元素, 即可使用统一的异常处理规则
            visitElement(context)
                    ......
                    .forEach()
                    .replaceAsMap(e -> {
                        ......
                    });
            
        }
        
    }
```

<br>
<br>

# 同类工具对比:

> [Jayway JsonPath](https://github.com/json-path/JsonPath)也提供了类似的能力, 它是一个广泛使用的 JSONPath 实现，它允许你用类似 $.Body.Orders[*].Name
> 的语法直接从 JSON 或 Map 数据结构中获取数据, 功能强大(筛选/统计等), 但表达式由String编写存在一点学习成本, 如果你熟悉Jayway JsonPath,
> 请忽略`ElementVisitor`. `ElementVisitor`功能简单, 通过链式书写, 有多少功能靠IDE提示一目了然, 适合简单的场景, 还提供了元素替换, 路径
> 创建能力.

<br>
<br>

# 依赖

* gradle

```text
//version替换为具体版本(2025.1.0以上)
dependencies {
    implementation 'com.github.shepherdviolet.glacimon:glacijava-common:?'
}
```

* maven

```maven
    <!--version替换为具体版本(2025.1.0以上)-->
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-common</artifactId>
        <version>?</version>
    </dependency>
```


