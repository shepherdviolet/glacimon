# `高可读性`的集合创建工具 LambdaBuilder/LambdaBuildable

* **LambdaBuilder**: LambdaBuilder是一个工具类, 提供hashMap/arrayList等集合创建的静态方法
* **LambdaBuildable**: LambdaBuildable是一个接口类, 提供了buildHashMap/buildArrayList等default方法, 实现此接口的类可以直接调用方法, 省掉了`LambdaBuilder.`的书写.

> 文中LambdaBuilder只示范一次, 其他示例均使用LambdaBuildable. 这两种方式本质上一样, 只是一个像工具类一样调用LambdaBuilder静态方法, 一个直接调用接口的方法(不用写LambdaBuilder.).

### 假如要组装如下结构的Map

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

### 使用LambdaBuilder组装

```
public class Test {
    public void process() {
        // P.S. Lambda表达式的入参根据层级命名为m(第一层) mm(第二层) mmm(第三层), 可读性好, 不容易弄错
        Map<String, Object> map = LambdaBuilder.HashMap(m -> {
            m.put("Header", LambdaBuilder.HashMap(mm -> {
                mm.put("Service", "Foo");
                mm.put("Time", "20250408");
                mm.put("Sequence", "202504080000357652");
            }));
            m.put("Body", LambdaBuilder.HashMap(mm -> {
                mm.put("Username", "test@test.com");
                mm.put("Orders", LambdaBuilder.ArrayList(lll -> {
                    lll.add(LambdaBuilder.HashMap(mmmm -> {
                        mmmm.put("Name", "Fish");
                        mmmm.put("Quantity", "6");
                        mmmm.put("UnitPrise", "68.8");
                    }));
                    lll.add(LambdaBuilder.HashMap(mmmm -> {
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

### 使用LambdaBuildable组装

> LambdaBuildable是一个接口类, 提供了buildHashMap/buildArrayList等default方法, 实现此接口的类可以直接调用方法, 省掉了`LambdaBuilder.`的书写.

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

### 源Collection转List/Set

* 如果示例中的列表'Orders'数据源自一个List, 可以简化书写:

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

<br>

# 依赖

* gradle

```text
//version替换为具体版本(2025.1.0以上)
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacijava-common:?'
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

