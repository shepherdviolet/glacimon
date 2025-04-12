# `便捷安全`的跨层级集合元素访问工具 ElementVisitor

* 同类工具对比:

> [Jayway JsonPath](https://github.com/json-path/JsonPath)是一个广泛使用的 JSONPath 实现，它允许你用类似 $.Body.Orders[*].Name
> 的语法直接从 JSON 或 Map 数据结构中获取数据, 功能强大(筛选/统计等), 但表达式由String编写存在一点学习成本, 如果你熟悉Jayway JsonPath,
> 请忽略`ElementVisitor`. `ElementVisitor`功能简单, 通过链式书写, 有多少功能靠IDE提示一目了然, 适合简单的场景, 还提供了元素替换, 路径
> 创建能力.

## 基本用法

## 异常处理器

### 预设`异常处理器`

### 自定义`异常处理器`

## 自动创建路径

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
