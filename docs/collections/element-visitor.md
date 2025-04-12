# 跨层级集合元素访问工具 ElementVisitor

## 基本用法

* 数据(Map)

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

* 获取所有的OrderName

```

```

### 名词解释

* **path**: 访问路径: 描述`你想获取的元素`与`根元素`之间的关系
* * 对应示例代码: `.of(root).child("Body").child("Orders").children().child("Name")`
* * 等效于JsonPath表达式: `$.Body.Orders[*].Name`
* **root_element**: 根元素: `ElementVisitor.of(...)`或`ElementVisitable.visitElement(...)`方法的入参
* **parent_element**: 路径中间的元素: 
* **expected_element**: 你想获取的元素: 

<br>

## 异常处理

| 异常类别(ErrorCategory) | 异常码(ErrorCode) | 发生原因                                                            |
| --- | --- |-----------------------------------------------------------------|
| DATA_MISSING | MISSING_ROOT_ELEMENT | ElementVisitor.of(...)或ElementVisitable.visitElement(...)方法入参为空 |
| DATA_MISSING | MISSING_PARENT_ELEMENT | 

        /**
         * 根元素(root_element)为空
         */
        MISSING_ROOT_ELEMENT(ErrorCategory.DATA_MISSING),
        /**
         * 路径中间的元素(parent_element)为空
         */
        MISSING_PARENT_ELEMENT(ErrorCategory.DATA_MISSING),
        /**
         * 你想获取的元素(expected_element)为空
         */
        MISSING_EXPECTED_ELEMENT(ErrorCategory.DATA_MISSING),

        /**
         * 路径中间的元素(parent_element)不是所需的Map或Collection类型.
         */
        PARENT_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),
        /**
         * 你想获取的元素(expected_element)不是指定的类型(你需要的类型).
         */
        EXPECTED_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),

        /**
         * [createIfAbsent] 创建你想获取的元素(expected_element)失败 (由createIfAbsent方法传入的表达式创建)
         */
        CREATE_EXPECTED_ELEMENT_FAILED(ErrorCategory.PROGRAMMING_ERROR),

        /**
         * 未定义的错误
         */
        UNDEFINED_ERROR(ErrorCategory.PROGRAMMING_ERROR),


### 压制(忽略)异常

* 注意: ExceptionHandler将无法接收到被压制(忽略)的异常


### 替换为自定义异常抛出

* 注意: ExceptionHandler无法接收到被压制(忽略)的异常
* 注意: 默认的ExceptionHandler直接抛出异常
* 注意: 设置自定义ExceptionHandler以后, 异常是否抛出由你的ExceptionHandler决定


### 仅打印错误日志



### 打印错误日志并抛出异常



### 开发框架抽象类中预设异常处理规则



<br>

## 自动创建路径



<br>

# 同类工具对比:

> [Jayway JsonPath](https://github.com/json-path/JsonPath)也提供了类似的能力, 它是一个广泛使用的 JSONPath 实现，它允许你用类似 $.Body.Orders[*].Name
> 的语法直接从 JSON 或 Map 数据结构中获取数据, 功能强大(筛选/统计等), 但表达式由String编写存在一点学习成本, 如果你熟悉Jayway JsonPath,
> 请忽略`ElementVisitor`. `ElementVisitor`功能简单, 通过链式书写, 有多少功能靠IDE提示一目了然, 适合简单的场景, 还提供了元素替换, 路径
> 创建能力.

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
