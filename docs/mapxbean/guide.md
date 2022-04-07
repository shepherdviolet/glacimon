# MapXBean | Map - Bean 转换工具

* [English](https://github.com/shepherdviolet/slate/blob/master/docs/mapxbean/guide-en.md)
* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-mapxbean/src/main/java/sviolet/slate/common/x/conversion/mapxbean)
* [支持 Glaciion SPI 扩展点](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)
<br>

# 简单示例

### Map转Bean

```text
// 可单例, 线程安全
private final MapToBeanConverter mapToBeanConverter = MapXBean.mapToBean().build();

public void foo() {
    Bean resultBean = mapToBeanConverter.convert(map, Bean.class);
}
```

* 转换前

```text
Map<String, Object>{
    name="Foo"
    locations=List<Map<String, Object>>[
        Map<String, Object>{
                address="Foo Street"
                time="2020-03-01 14:23:58"
        }
        Map<String, Object>{
                address="Foo Street"
                time="2020-03-01 16:26:17"
        }
    ]
}
```

* 转换后

```text
## Map->Bean
Person{
    name="Foo"
    ## 元素类型由List的泛型<Location>推断
    locations=List<Location>[
        ## Map->Bean
        Location{
                address="Foo Street"
                ## String->Date **类型映射**
                time=Date(2020-03-01 14:23:58)
        }
        ## Map->Bean
        Location{
                address="Foo Street"
                ## String->Date **类型映射**
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

<br>

### Bean转Map, Bean属性保持原类型(浅复制)

> 在Bean转Map的场景中. 当一个Bean被转为Map时, 它的所有的属性默认都会保持原始类型 <br>

```text
// 可单例, 线程安全
private final BeanToMapConverter beanToMapConverter = MapXBean.beanToMap().build()

public void foo() {
    Map<String, Object> resultMap = beanToMapConverter.convert(bean);
}
```

* 转换前

```text
Person{
    name="Foo"
    locations=List<Location>[
        Location{
                address="Foo Street"
                time=Date(2020-03-01 14:23:58)
        }
        Location{
                address="Foo Street"
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

* 转换后

```text
## Bean->Map
Map<String, Object>{
    ## Bean属性保持原类型
    name="Foo"
    ## 注意这个集合是重新创建的
    locations=List<Location>[
        ## 集合元素也保持原类型
        Location{
                address="Foo Street"
                time=Date(2020-03-01 14:23:58)
        }
        ## 集合元素也保持原类型
        Location{
                address="Foo Street"
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

<br>

### Bean转Map, Bean属性继续拆解成Map, 直到不可分割

> 设置inflateStrategy为InflateUntilIndivisible后, 我们会将Bean转为一个由Map和Collection嵌套构成的Map, 所有的属性或元素都会被 <br>
> 继续拆解成Map, 直到不可分割. 你也可以指定哪些特殊的类型不可分割, 或者指定拆解深度. <br>

```text
// 可单例, 线程安全, InflateUntilIndivisible可以指定哪些特殊的类型不可分割
private final BeanToMapConverter beanToMapConverter = MapXBean.beanToMap().inflateStrategy(new InflateUntilIndivisible()).build()

public void foo() {
    Map<String, Object> resultMap = beanToMapConverter.convert(bean);
}
```

* 转换前

```text
Person{
    name="Foo"
    locations=List<Location>[
        Location{
                address="Foo Street"
                time=Date(2020-03-01 14:23:58)
        }
        Location{
                address="Foo Street"
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

* 转换后

```text
## Bean->Map
Map<String, Object>{
    ## String在默认的不可分割类型中
    name="Foo"
    ## 注意这个集合是重新创建的
    locations=List<Map<String, Object>>[
        ## Bean->Map **继续拆解**
        Map<String, Object>{
                ## String在默认的不可分割类型中
                address="Foo Street"
                ## Date在默认的不可分割类型中
                time=Date(2020-03-01 14:23:58)
        }
        ## Bean->Map **继续拆解**
        Map<String, Object>{
                ## String在默认的不可分割类型中
                address="Foo Street"
                ## Date在默认的不可分割类型中
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

<br>

# 高级用法

### 当一个属性或元素处理失败时抛出异常

> 当一个属性或元素处理失败时, 默认不会抛出异常, 失败的字段会留空(null). 如果设置throwExceptionIfFails为true, 那么就会抛出异常了. <br>

```text
MapXBean.mapToBean()
    .throwExceptionIfFails(true)
    .build();
MapXBean.beanToMap()
    .throwExceptionIfFails(true)
    .build()
```

<br>

### 当一个属性或元素处理失败时打印日志

> 当一个属性或元素处理失败时, 默认不会抛出异常, 失败的字段会留空(null). 如果设置了exceptionCollector, 就能够将这些异常信息打印 <br>
> 到日志中, 失败的字段仍然会留空(null). 注意exceptionCollector只在throwExceptionIfFails为false时有效. <br>

```text
MapXBean.mapToBean()
    .exceptionCollector(new SimpleConversionExceptionCollector())
    .build();
MapXBean.beanToMap()
    .exceptionCollector(new SimpleConversionExceptionCollector())
    .build()
```

<br>

### 关于 `类型映射` (仅Map转Bean时出现)

> 在Map转Bean的场景中. 如果程序发现Map中的数据类型与Bean需要的类型不匹配, 程序会试着用MxbTypeMapper进行类型转换. <br>

| 默认的类型映射器(MxbTypeMapper) | 源类型 | 目标类型 |
| --------------------- | ---- | ---- |
| MxbMapperAllDate2SqlDate | All date | sql.Date |
| MxbMapperAllDate2SqlTimestamp | All date | sql.Timestamp |
| MxbMapperAllDate2String | All date | String |
| MxbMapperAllDate2UtilDate | All date | util.Date |
| MxbMapperString2SqlDate | String | sql.Date |
| MxbMapperString2SqlTimestamp | String | sql.Timestamp |
| MxbMapperString2UtilDate | String | util.Date |
| MxbMapperAllInteger2BigInteger | All integer | BigInteger |
| MxbMapperAllNumber2BigDecimal | All number | BigDecimal |
| MxbMapperAllNumber2String | All number | String |
| MxbMapperLowlevelNum2Double | Low level number | Double |
| MxbMapperLowlevelNum2Float | Low level number | Float |
| MxbMapperLowlevelNum2Integer | Low level number | Integer |
| MxbMapperLowlevelNum2Long | Low level number | Long |

* 你可以添加自己的MxbTypeMapper, 利用[Glaciion SPI 扩展点](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

| 扩展点 | 默认实现 |
| --------------- | ---------------------- |
| MxbTypeMapperProvider | MxbTypeMapperProviderImpl |

<br>

### 关于 `继续拆解` (InflateStrategy, 仅Bean转Map出现)

> 在Bean转Map的场景中. 当一个Bean被转为Map时, 它的所有的属性默认都会保持原始类型, 除非你设置了BeanToMapInflateStrategy, 来告诉 <br>
> 程序哪些Bean可以被继续拆解成Map. <br>

> 继续拆解(Inflate)的意思是, 在在Bean转Map的场景中, 如果一个Bean的属性或者一个集合的元素是一个Java Bean, 它可以继续被转换为Map, <br>
> 只要你设置的BeanToMapInflateStrategy的方法返回true. 这个过程就称之为"继续拆解(Inflate)". <br>

* 默认的继续拆解规则(BeanToMapInflateStrategy)

| 默认的继续拆解规则(BeanToMapInflateStrategy) | 说明 |
| -------------------------------- | ------- |
| null | 所有的属性都会保持原始类型 |
| InflateUntilIndivisible | 所有的属性或元素都会被继续拆解成Map, 直到不可分割 |
| InflateCollectionElements | 如果根节点的属性是集合(包括Map), 则拆解它的元素 |

* `InflateUntilIndivisible` 可以指定最大拆解深度, 也可以指定哪些类型是不可分割的

```text
/*
    1.在文件中指定哪些类型不可分割: /META-INF/keep-types.properties
        示例: 
            sviolet.slate.common.x.conversion.mapxbean.MapToBeanTest$Person1
            sviolet.slate.common.x.conversion.mapxbean.MapToBeanTest$Person2
    2.然后将这个properties文件加载为Properties实例
    3.设置继续拆解规则为: new InflateUntilIndivisible(properties)
    4.那么在示例中 'MapToBeanTest$Person1' 和 'MapToBeanTest$Person2' 将不会被继续分割
*/
Properties keepTypesProperties = new Properties();
keepTypesProperties.load(getClass().getResourceAsStream("/META-INF/keep-types.properties"));

MapXBean.beanToMap()
    .inflateStrategy(new InflateUntilIndivisible(keepTypesProperties))
    .build();
```

* 你可以实现自定义的继续拆解规则, 实现接口`BeanToMapInflateStrategy`即可

<br>

### 自定义 "是否不可分割 / 是否Bean" 的判断逻辑

* 默认的不可分割类型

| 默认的不可分割类型 |
| ------------------------- |
| Class.isEnum |
| PrimitiveUtils.isPrimitiveOrWrapper |
| Object |
| byte[] |
| char[] |
| instanceof CharSequence (String ...) |
| instanceof BigDecimal |
| instanceof BigInteger |
| instanceof Date (util.Date / sql.Date ...) |
| instanceof Temporal (Instant / LocalDateTime ...) |

* 默认的 "是否Bean" 判断逻辑

```text
!MxbTypeJudger.isIndivisible(type) &&
!Class.isArray() &&
!Class.isInterface() &&
!Modifier.isAbstract(Class.getModifiers()) &&
Has read methods (getter) or write methods (setter)
```

* 你可以自定义 "是否不可分割 / 是否Bean" 的判断逻辑, 利用[Glaciion SPI 扩展点](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

| 扩展点 | 默认实现 |
| --------------- | ---------------------- |
| MxbTypeJudger | MxbTypeJudgerImpl |

<br>

### 所有 Glaciion 扩展点

* [Glaciion SPI 文档](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

| 扩展点 | 默认实现 |
| --------------- | ---------------------- |
| MxbCollectionMapper | MxbCollectionMapperImpl |
| MxbObjectInstantiator | MxbObjectInstantiatorImpl |
| MxbTypeJudger | MxbTypeJudgerImpl |
| MxbTypeMapperCenter | MxbTypeMapperCenterImpl |
| MxbTypeMapperProvider | MxbTypeMapperProviderImpl |

<br>

# 添加依赖

```gradle

repositories {
    //Slate in mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet:slate-mapxbean:?'
}

```

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-mapxbean</artifactId>
        <version>?</version>
    </dependency>
```
