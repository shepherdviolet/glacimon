# MapXBean | Map - Bean Converter

* [中文](https://github.com/shepherdviolet/slate/blob/master/docs/mapxbean/guide.md)
* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-mapxbean/src/main/java/sviolet/slate/common/x/conversion/mapxbean)
* [Support Glaciion SPI extension point](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

<br>

# Quick start

### Convert from Map to Bean

```text
// Can be a singleton
private final MapToBeanConverter mapToBeanConverter = MapXBean.mapToBean().build();

public void foo() {
    Bean resultBean = mapToBeanConverter.convert(map, Bean.class);
}
```

* From

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

* To

```text
## Map->Bean
Person{
    name="Foo"
    ## Detect the element type from <Location>
    locations=List<Location>[
        ## Map->Bean
        Location{
                address="Foo Street"
                ## String->Date **Type Mapping**
                time=Date(2020-03-01 14:23:58)
        }
        ## Map->Bean
        Location{
                address="Foo Street"
                ## String->Date **Type Mapping**
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

<br>

### Convert from Bean to Map, and all properties keep original types

> In the scene of Bean -> Map. While a Bean is converting to a Map, all the properties of Bean will keep the original <br>
> type by default. <br>

```text
// Can be a singleton
private final BeanToMapConverter beanToMapConverter = MapXBean.beanToMap().build()

public void foo() {
    Map<String, Object> resultMap = beanToMapConverter.convert(bean);
}
```

* From

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

* To

```text
## Bean->Map
Map<String, Object>{
    ## Keep original type by default
    name="Foo"
    ## Recreate the List
    locations=List<Location>[
        ## Keep original type by default
        Location{
                address="Foo Street"
                time=Date(2020-03-01 14:23:58)
        }
        ## Keep original type by default
        Location{
                address="Foo Street"
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

<br>

### Convert from Bean to Map, and all properties inflate to Map until indivisible

> Set inflateStrategy to InflateUntilIndivisible. Converting a 'Java Bean' to 'Map consisting of Map and Collection <br>
> nesting', all the properties or elements will be inflate until indivisible, or reaches the specified depth, or <br>
> meets the specified classes. <br>

```text
// Can be a singleton, InflateUntilIndivisible can specify which classes are indivisible
private final BeanToMapConverter beanToMapConverter = MapXBean.beanToMap().inflateStrategy(new InflateUntilIndivisible()).build()

public void foo() {
    Map<String, Object> resultMap = beanToMapConverter.convert(bean);
}
```

* From

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

* To

```text
## Bean->Map
Map<String, Object>{
    ## Indivisible type String
    name="Foo"
    ## Recreate the List
    locations=List<Map<String, Object>>[
        ## Bean->Map **Inflate**
        Map<String, Object>{
                ## Indivisible type String
                address="Foo Street"
                ## Indivisible type Date
                time=Date(2020-03-01 14:23:58)
        }
        ## Bean->Map **Inflate**
        Map<String, Object>{
                ## Indivisible type String
                address="Foo Street"
                ## Indivisible type Date
                time=Date(2020-03-01 16:26:17)
        }
    ]
}
```

<br>

# Advanced usage

### Throw exception if property (element) mapping failed

> When property or element mapping fails, no exception will be thrown by default, the failed property will left null. <br>
> After setting throwExceptionIfFails to true, an exception will be thrown when the property mapping fails. <br>

```text
MapXBean.mapToBean()
    .throwExceptionIfFails(true)
    .build();
MapXBean.beanToMap()
    .throwExceptionIfFails(true)
    .build()
```

<br>

### Print log if property (element) mapping failed

> When property or element mapping fails, no exception will be thrown by default, the failed property will left null. <br>
> After setting a exceptionCollector, warning log will be printed when the property mapping fails (the failed property <br>
> will left null). Note that the exceptionCollector is invalid when the throwExceptionIfFails is true. <br>

```text
MapXBean.mapToBean()
    .exceptionCollector(new SimpleConversionExceptionCollector())
    .build();
MapXBean.beanToMap()
    .exceptionCollector(new SimpleConversionExceptionCollector())
    .build()
```

<br>

### About `Type Mapping` (For Map -> Bean Only)

> In the scene of Map -> Bean, if we find that the data type in the Map does not match the type required by the bean, <br>
> we will try to convert the data to the required type by MxbTypeMappers. <br>

| Default MxbTypeMapper | From | To |
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

* You can add your own MxbTypeMapper by [Glaciion SPI extension point](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

| Extension Point | Default Implementation |
| --------------- | ---------------------- |
| MxbTypeMapperProvider | MxbTypeMapperProviderImpl |

<br>

### About `Inflate` (Inflate strategy, For Bean -> Map Only)

> In the scene of Bean -> Map. While a Bean is converting to a Map, all the properties of Bean will keep the original <br>
> type by default, unless the BeanToMapInflateStrategy tells the program that it needs to be inflated. <br>

> 'Inflate' means that in the scene of Bean -> Map, if a property (of Java Bean) or an element (of Collection) is a <br>
> Java Bean, the property (or element) can be converted to a Map as long as the method <br>
> BeanToMapInflateStrategy#needToBeInflated returns true. The process of converting property (or element) to Map is <br>
> called 'Inflate'. <br>

* Default BeanToMapInflateStrategy

| Default BeanToMapInflateStrategy | Comment |
| -------------------------------- | ------- |
| null | All the properties of Bean will keep the original |
| InflateUntilIndivisible | All properties inflate to Map until indivisible |
| InflateCollectionElements | Inflate the elements of Root node's Collection property (including Map) |

* `InflateUntilIndivisible` can specify the max depth, can specify which types keep the original type (do not inflate it)

```text
/*
    1.Declare which classes are indivisible in /META-INF/keep-types.properties file
        Example: 
            sviolet.slate.common.x.conversion.mapxbean.MapToBeanTest$Person1
            sviolet.slate.common.x.conversion.mapxbean.MapToBeanTest$Person2
    2.Then load the properties file as Properties instance
    3.Set new InflateUntilIndivisible(...)
    4.Class 'MapToBeanTest$Person1' and 'MapToBeanTest$Person2' will not be inflated
*/
Properties keepTypesProperties = new Properties();
keepTypesProperties.load(getClass().getResourceAsStream("/META-INF/keep-types.properties"));

MapXBean.beanToMap()
    .inflateStrategy(new InflateUntilIndivisible(keepTypesProperties))
    .build();
```

* You can customize your own strategy by implements interface `BeanToMapInflateStrategy`

<br>

### Customize isIndivisible / isBean judgment logic

* Default Indivisible Types

| Default Indivisible Types |
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

* Default isBean judgment logic

```text
!MxbTypeJudger.isIndivisible(type) &&
!Class.isArray() &&
!Class.isInterface() &&
!Modifier.isAbstract(Class.getModifiers()) &&
Has read methods (getter) or write methods (setter)
```

* You can customize isIndivisible / isBean judgment logic by [Glaciion SPI extension point](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

| Extension Point | Default Implementation |
| --------------- | ---------------------- |
| MxbTypeJudger | MxbTypeJudgerImpl |

<br>

### All Glaciion Extension Points

* [Glaciion SPI Document](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

| Extension Point | Default Implementation |
| --------------- | ---------------------- |
| MxbCollectionMapper | MxbCollectionMapperImpl |
| MxbObjectInstantiator | MxbObjectInstantiatorImpl |
| MxbTypeJudger | MxbTypeJudgerImpl |
| MxbTypeMapperCenter | MxbTypeMapperCenterImpl |
| MxbTypeMapperProvider | MxbTypeMapperProviderImpl |

<br>

# Import dependencies from maven repository

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
