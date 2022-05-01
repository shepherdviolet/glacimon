# MapIO

## 简介

```text
通过枚举类/POJO类字段上的注解配置规则, 将数据输入Map, 或从Map输出数据, 用于设计基于上下文的服务开发模式. 
服务请求数据(Input Map)通过MapIO过滤字段, 输入到上下文中; 上下文中的数据通过MapIO过滤字段, 输出到服务响应数据(Output Map)中.
```

<br>

## 使用说明

### 配置与扩展

* 在使用MapIO前, 请先在你的工程中配置它, 请参考[配置与扩展](https://github.com/shepherdviolet/glacimon/blob/master/docs/mapio/config.md)

<br>

### 获取MapIo实例

* MapIO有三种配置方式: Spring方式, GlacimonSpi方式, 默认方式.
* 1.获取Spring方式配置的MapIo

```text
@Autowired
private MapIo mapIo;
```

* 2.获取GlacimonSpi方式配置的MapIo

```text
MapIo mapIo = SpiMapIo.getInstance();
```

* 3.获取默认方式配置的MapIo

```text
默认方式配置的MapIo请自行持有(new出来的对象)
```

<br>

### 预加载字典类 (推荐)

* `推荐在应用程序启动时进行预加载, 这样可以在启动时就感知到非法过滤规则`

```text
// 预加载FooDict和CommonDict字典, 会检查其中的规则是否有问题, 检查过滤器是否存在
mapIo.preloadDictionaries(FooDict.class, CommonDict.class);
```

* Spring项目建议在启动后, 扫描所有字典类, 并用上面的方法进行预加载(预检查)

<br>

### 调用MapIo, 过滤Map数据

```text
// 将inputMap中的数据输入到contextMap中, 其字段根据字典FooDict.class/CommonDict.class中配置的@Input...系列规则进行过滤/转换
// FieldScreeningMode.DISCARD_BY_DEFAULT表示未在字典中配置的字段会被丢弃, 不会放入contextMap中 (如果设置为PASS_BY_DEFAULT则表示如果字段未在字典中配置, 会保持原样放入contextMap).
// 配置多个字典时, 规则执行顺序同数组顺序, 因此最后一个字典拥有最高优先级(对相同字段的处理以最后一个字典为准).
Map<String, Object> contextMap = mapIo.doMap(inputMap, IoMode.INPUT, FieldScreeningMode.DISCARD_BY_DEFAULT, FooDict.class, CommonDict.class);

// 将contextMap中的数据输出到outputMap中, 其字段根据字典BarDict.class/CommonDict.class中配置的@Output...系列规则进行过滤/转换
// FieldScreeningMode.DISCARD_BY_DEFAULT表示未在字典中配置的字段会被丢弃, 不会放入outputMap中 (如果设置为PASS_BY_DEFAULT则表示如果字段未在字典中配置, 会保持原样放入outputMap).
// 配置多个字典时, 规则执行顺序同数组顺序, 因此最后一个字典拥有最高优先级(对相同字段的处理以最后一个字典为准).
Map<String, Object> outputMap = mapIo.doMap(contextMap, IoMode.OUTPUT, FieldScreeningMode.DISCARD_BY_DEFAULT, BarDict.class, CommonDict.class);
```

<br>

### 在字典类中配置字段过滤规则

#### 方式1: 枚举中配置规则 (推荐)

* 字段过滤规则有两种: `输入规则(@Input...系列)`, `输出规则(@Output...系列)`
* 一个字段可以同时被标记为输入/输出字段, 可以同时有输入/输出过滤规则, 输入输出规则互不干扰(各自在对应的IoMode工作模式中生效)

```java
private enum SampleDict {

    /**
     * 标记为输入字段, 字段必输. (fromKey=Aaa, toKey=Aaa, required=true)
     * 等效于输入时: contextMap.put("Aaa", inputMap.get("Aaa"))
     */
    @Input
    Aaa,

    /**
     * 标记为输入字段, 字段必输, 字段名映射222->Bbb. (fromKey=222, toKey=Bbb, required=true)
     * 等效于输入时: contextMap.put("Bbb", inputMap.get("222"))
     */
    @Input(fromKey = "222")
    Bbb,

    /**
     * 标记为输入字段, 字段非必输. (fromKey=Ccc, toKey=Ccc, required=false)
     * 等效于输入时: contextMap.put("Ccc", inputMap.get("Ccc"))
     */
    @Input(required = false)
    Ccc,

    /**
     * 标记为输出字段, 字段必输. (fromKey=Ddd, toKey=Ddd, required=true)
     * 等效于输出时: outputMap.put("Ddd", contextMap.get("Ddd"))
     */
    @Output
    Ddd,

    /**
     * 标记为输出字段, 字段必输, 字段名映射555->Eee. (fromKey=555, toKey=Eee, required=true)
     * 等效于输出时: outputMap.put("Eee", contextMap.get("555"))
     */
    @Output
    Eee,

    /**
     * 标记为输出字段, 字段非必输. (fromKey=Fff, toKey=Fff, required=false)
     * 等效于输出时: outputMap.put("Fff", contextMap.get("Fff"))
     */
    @Output
    Fff,

    /**
     * 补充说明:
     * 一个字段可以同时被标记为输入/输出字段, 可以同时有输入/输出过滤规则, 输入输出规则互不干扰(各自在对应的IoMode工作模式中生效)
     */
    @Input
    //@InputFilter(type = FooFilter.class, args = {"aaa", "bbb"})
    //@InputElementFilter(type = FooFilter.class, args = {"aaa", "bbb"}, keepOrder = true)
    //@InputMapper(fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT, dictionary = SubDict.class)
    @Output
    //@OutputFilter(type = FooFilter.class, args = {"aaa", "bbb"})
    //@OutputElementFilter(type = FooFilter.class, args = {"aaa", "bbb"}, keepOrder = true)
    //@OutputMapper(fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT, dictionary = SubDict.class)
    Ggg,

    /**
     * [特殊情况][不建议使用] 覆盖字典中的字段名, 对于@Input来说相当于toKey.
     *
     * 字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
     * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名. 对外的字段名没有限制, 因为可以用@Input的fromKey指定.
     *
     * 未设置fromKey时, 等效于输入时: contextMap.put("-hhh-", inputMap.get("-hhh-"))
     * 设置了fromKey后, 等效于输入时: contextMap.put("-hhh-", inputMap.get("888"))
     */
    @Input
    //@Input(fromKey = "888")
    @FieldName("-hhh-")
    Hhh,

    /**
     * [特殊情况][不建议使用] 覆盖字典中的字段名, 对于@Output来说相当于fromKey.
     *
     * 字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
     * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名. 对外的字段名没有限制, 因为可以用@Output的toKey指定.
     *
     * 未设置toKey时, 等效于输出时: outputMap.put("-iii-", contextMap.get("-iii-"))
     * 设置了toKey后, 等效于输出时: outputMap.put("999", contextMap.get("-iii-"))
     */
    @Output
    //@Output(toKey = "999")
    @FieldName("-iii-")
    Iii,

}
```

* 下面以输入规则(@Input...系列)为例, `说明如何进行字段过滤(校验/转换)`. (输出规则同理, 替换成@Output开头的注解即可, 两种规则可以在一个字段上并存)

```text
public enum SampleDict2 {

    /**
     * [InputFilter/OutputFilter] 字段过滤器. 字段通过该过滤器过滤, 可以检查字段值, 也可以改变字段值.
     *
     * type: 指定过滤器类型(type和name至少设置一个)
     * name: 指定过滤器名称(type和name至少设置一个)
     * args: 过滤参数
     *
     * 示例: 检查输入字段(String)长度, 最小长度6, 最大长度8, 否则报错
     */
    @Input
    @InputFilter(type = StringCheckLength.class, args = {"6", "8"})
    Aaa,

    /**
     * [InputElementFilter/OutputElementFilter] 字段元素过滤器. 字段内的元素通过该过滤器过滤, 可以检查元素值, 也可以改变元素值.
     * 使用这个过滤规则时, 输入字段必须是Collection或者Map类型 (可以在前面加一个过滤器转换类型), 否则会报错.
     * 如果输入字段是Collection类型, 过滤器会过滤它的每一个元素.
     * 如果输入字段是Map类型, 过滤器会过滤它每一个元素的value. 另外, 过滤后字段默认是HashMap类型, 如果要保持顺序, 请设置keepOrder=true, 字段会变成LinkedHashMap.
     *
     * type: 指定过滤器类型(type和name至少设置一个)
     * name: 指定过滤器名称(type和name至少设置一个)
     * args: 过滤参数
     * keepOrder: 集合是Map时, 是否需要保持顺序. true:  过滤结果为LinkedHashMap. false: 过滤结果为HashMap.
     *
     * 示例: 检查输入字段(Collection或Map)的每一个元素, 检查元素(String)长度 (最小长度6, 最大长度8, 否则报错)
     */
    @Input
    @InputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
    Bbb,

    /**
     * [InputMapper/OutputMapper] 字段(Map)映射器. 按照指定字典中的规则, 对源Map中的子Map字段进行映射 (相当于对子Map做MapIo#doMap).
     * 使用这个过滤规则时, 输入字段必须是Map类型 (可以在前面加一个过滤器转换类型), 否则会报错.
     *
     * dictionary: 映射字典, 根据其中用注解定义的规则映射元素.
     * fieldScreeningMode: Map字段筛选模式
     *
     * 示例: 将Ccc这个Map类型的字段, 按照指定字典中的规则进行字段映射.
     */
    @Input
    @InputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
    Ccc,


    /**
     * 过滤规则可以配置无数个, 默认按代码书写顺序执行过滤规则. (特殊情况需要手动设置执行顺序, 见后文"关于过滤规则执行顺序"说明)
     *
     * 示例: 将输入字段类型转为String, 然后检查字段(String)长度 (最小长度6, 最大长度8, 否则报错)
     */
    @Input
    @InputFilter(type = ToString.class)
    @InputFilter(type = StringCheckLength.class, args = {"6", "8"})
    Xxx,

    /**
     * 过滤规则可以配置无数个, 默认按代码书写顺序执行过滤规则. (特殊情况需要手动设置执行顺序, 见后文"关于过滤规则执行顺序"说明)
     *
     * 示例: 将输入字段由Map转为List (取values), 然后检查每个元素的长度 (最小长度6, 最大长度8, 否则报错), 最终输出一个List
     */
    @Input
    @InputFilter(type = MapValuesToList.class)
    @InputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
    Yyy,

}
```

* 关于过滤规则执行顺序(clauseOrder). 一般情况下, MapIo会根据注解的书写顺序执行规则, 不需要手动设置执行顺序.

```text
/**
 * 关于过滤规则执行顺序(clauseOrder):
 * 1.一般情况下, MapIo会根据注解的书写顺序执行规则, 不需要手动设置执行顺序.
 * 2.如果一个字段存在多种'输入过滤规则'(@Input...), 且同一种'输入过滤规则'存在多条时, 程序将无法分辨注解的书写顺序, 这种情况,
 * 需要手动设置每个'输入过滤规则'的执行顺序.
 * 3.如果一个字段存在多种'输出过滤规则'(@Output...), 且同一种'输出过滤规则'存在多条时, 程序将无法分辨注解的书写顺序, 这种情况,
 * 需要手动设置每个'输出过滤规则'的执行顺序.
 * 4.输入规则和输出规则互不影响.
 *
 * 执行顺序(clauseOrder)设置说明:
 * 1.执行顺序的数值越小优先级越高(越先执行). 小于0表示未设置顺序(按代码书写顺序执行). 建议设置为: 0 1 2 3 ...
 * 2.一个字段的'输入过滤规则'(@Input...)执行顺序要么全都不设置, 要么全都设置, 不可以只设置部分.
 * 3.一个字段的'输出过滤规则'(@Output...)执行顺序要么全都不设置, 要么全都设置, 不可以只设置部分.
 * 4.输入规则和输出规则互不影响.
 */
public enum SampleDict4 {

    /**
     * [不需要手动设置执行顺序的情况]
     * 虽然有三种输入规则, 但是每种规则只有一条, 不需要手动设置执行顺序.
     * 虽然有三种输出规则, 但是每种规则只有一条, 不需要手动设置执行顺序.
     * 输入规则和输出规则互不影响.
     */
    @Input
    @InputFilter(type = FooFilter.class)
    @InputElementFilter(type = FooFilter.class)
    @InputMapper(dictionary = FooDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
    @Output
    @OutputFilter(type = FooFilter.class)
    @OutputElementFilter(type = FooFilter.class)
    @OutputMapper(dictionary = FooDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
    Aaa,

    /**
     * [不需要手动设置执行顺序的情况]
     * 虽然有两条InputFilter规则, 但是输入规则只有这一种, 不需要手动设置执行顺序.
     * 虽然有两条OutputFilter规则, 但是输出规则只有这一种, 不需要手动设置执行顺序.
     * 输入规则和输出规则互不影响.
     */
    @Input
    @InputFilter(type = FooFilter.class)
    @InputFilter(type = FooFilter.class)
    @Output
    @OutputFilter(type = FooFilter.class)
    @OutputFilter(type = FooFilter.class)
    Bbb,

    /**
     * [需要手动设置执行顺序的情况]
     * 有两种输入规则, 且InputElementFilter规则有多条, 需要手动设置执行顺序.
     * 虽然有两条OutputFilter规则, 但是输出规则只有这一种, 不需要手动设置执行顺序.
     * 输入规则和输出规则互不影响.
     */
    @Input
    @InputFilter(clauseOrder = 0, type = FooFilter.class)
    @InputElementFilter(clauseOrder = 1, type = FooFilter.class)
    @InputElementFilter(clauseOrder = 2, type = FooFilter.class)
    @Output
    @OutputFilter(type = FooFilter.class)
    @OutputFilter(type = FooFilter.class)
    Ccc,

    /**
     * [需要手动设置执行顺序的情况]
     * 虽然有两条InputFilter规则, 但是输入规则只有这一种, 不需要手动设置执行顺序.
     * 有两种输出规则, 且OutputElementFilter规则有多条, 需要手动设置执行顺序.
     * 输入规则和输出规则互不影响.
     */
    @Input
    @InputFilter(type = FooFilter.class)
    @InputFilter(type = FooFilter.class)
    @Output
    @OutputMapper(clauseOrder = 0, dictionary = FooDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
    @OutputElementFilter(clauseOrder = 1, type = FooFilter.class)
    @OutputElementFilter(clauseOrder = 2, type = FooFilter.class)
    Ddd,

    /**
     * [需要手动设置执行顺序的情况]
     * 有两种输入规则, 且InputElementFilter规则有多条, 需要手动设置执行顺序.
     * 有两种输出规则, 且OutputElementFilter规则有多条, 需要手动设置执行顺序.
     * 输入规则和输出规则互不影响.
     */
    @Input
    @InputFilter(clauseOrder = 0, type = FooFilter.class)
    @InputElementFilter(clauseOrder = 1, type = FooFilter.class)
    @InputElementFilter(clauseOrder = 2, type = FooFilter.class)
    @Output
    @OutputMapper(clauseOrder = 0, dictionary = FooDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
    @OutputElementFilter(clauseOrder = 1, type = FooFilter.class)
    @OutputElementFilter(clauseOrder = 2, type = FooFilter.class)
    Eee,

}
```

* 一些错误示范:

```text
public enum SampleDict4 {

    /**
     * [错误示范] 缺少@Input注解
     *
     * 必须先标记字段为输入/输出字段(@Input/@output), 才能为它设置对应的过滤规则
     */
    @InputFilter(type = FooFilter.class, args = {"aaa", "bbb"})
    //@InputElementFilter(type = FooFilter.class, args = {"aaa", "bbb"}, keepOrder = true)
    //@InputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
    Aaa,

}
```

<br>

#### 方式2: POJO中配置规则

* 支持在POJO类的Field上配置规则, 此处不作详细介绍, 请参考枚举方式

```java
public class FooRequest {
    
    @Input(fromKey = "username")
    @InputFilter(type = StringCheckLength.class, args = {"0", "10"})
    private String name;
    
}
```

<br>
