# MapIO配置与扩展

* [返回上级](https://github.com/shepherdviolet/glacimon/blob/master/docs/mapio/guide.md)
* MapIO有三种配置/扩展方式: Spring方式(推荐), GlacimonSpi方式(推荐), 默认方式

# Spring方式配置(推荐)

* MapIo组件通过Spring Bean方式配置, 需要手动配置.
* 字段过滤器通过Spring Bean方式扩展, 按需添加过滤器.

## 配置组件

```text
@Configuration
public class TestConfig {

    /**
     * MapIO
     */
    @Bean("mapIo")
    public MapIo mapIo(@Qualifier("mapIoRuleAnnotationManager") RuleAnnotationManager ruleAnnotationManager,
                       @Qualifier("mapIoFilterProvider") FilterProvider filterProvider,
                       @Qualifier("mapIoExceptionFactory") ExceptionFactory exceptionFactory){
        return new MapIoImpl()
                .setRuleAnnotationParser(ruleAnnotationManager)
                .setFilterProvider(filterProvider)
                .setExceptionFactory(exceptionFactory);
    }

    /**
     * 规则注解管理器, 将枚举类/POJO类字段上的注解解析为过滤规则.
     * 如果想自定义注解类型, 可以扩展它.
     */
    @Bean("mapIoRuleAnnotationManager")
    public RuleAnnotationManager mapIoRuleAnnotationManager() {
        // 默认规则注解管理器, 使用默认的一套规则注解
        return new DefaultRuleAnnotationManager();
    }

    /**
     * 过滤器提供者: MapIo通过它查找字段过滤器. SpringFilterProvider会从Spring上下文中获取过滤器实例.
     */
    @Bean("mapIoFilterProvider")
    public FilterProvider mapIoFilterProvider() {
        return new SpringFilterProvider();
    }

    /**
     * 异常工厂: MapIo的公共逻辑和自带的过滤器在映射Map的过程中, 如果遇到错误, 会调用这个工厂创建异常, 默认异常类型是RuntimeException.
     * 如果想自定义异常类型, 可以扩展它.
     */
    @Bean("mapIoExceptionFactory")
    public ExceptionFactory mapIoExceptionFactory() {
        // 异常工厂默认实现: MapIo的公共逻辑和自带的过滤器在映射Map的过程中, 会抛出一些异常, 默认类型是RuntimeException.
        return new DefaultExceptionFactory();
    }

}
```

## 添加字段过滤器

* 1.实现自己的字段过滤器:

```text
/**
 * 过滤器需要实现com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter接口. 
 *
 * 如果过滤参数需要转为其他类型, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.ArgsConvertedFilter
 * 如果过滤参数需要转为Integer类型, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.IntArgsConvertedFilter
 * 如果过滤参数需要缓存, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.ArgsCachedFilter
 * 
 * 可以参考默认提供的过滤器:
 * com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter包
 * com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filters包
 *
 * ImplementationName注解指定过滤器名称, 这样规则注解里可以通过名称查找这个过滤器.
 */
public final class FooFilter implements Filter {

    /**
     * <p>预先检查过滤参数的格式 (注意, 这个方法可能会被调用多次, 每次的入参都有可能不同).</p>
     * <p>当一个字典类被加载时, MapIo会检查其中的规则是否合法, 会调用本方法检查过滤参数是否合法.</p>
     *
     * <p>这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     * 所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.</p>
     *
     * @param args 过滤参数, 非空
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    @Override
    public void doPreCheckArgs(String[] args) throws Exception {
        // 在这里预检查过滤参数, 有问题就抛出异常
    }

    /**
     * <p>实现字段过滤(校验/转换)</p>
     *
     * @param element 过滤前字段, 非空
     * @param args 过滤参数, 非空. 它会经过doPreCheckArgs方法预检查, 所以到了这个方法, 过滤参数一般不会有问题, 不用再检查了. 
     * @param ruleInfo 过滤规则信息, 用于打印日志/输出错误信息
     * @return 过滤后字段 (不转换就返回原值!)
     */
    @Override
    public Object doFilter(Object element, String[] args, RuleInfo ruleInfo) {
        // 在这里对字段进行校验/转换
        // 此处以转String为例
        return String.valueOf(element);
    }

}
```

* 2.添加过滤器到Spring上下文:

```text
@Configuration
public class TestConfig {

    // 指定过滤器名称, 这样规则注解里可以通过名称查找这个过滤器.
    @Bean("FooFilter")
    public Filter fooFilter() {
        return new FooFilter();
    }

    // 指定过滤器名称, 这样规则注解里可以通过名称查找这个过滤器.
    @Bean("BarFilter")
    public Filter barFilter() {
        return new BarFilter();
    }

}

```

<br>
<br>

# GlacimonSpi方式配置(推荐)

* `这种方式开箱即用, 默认无需配置.`
* MapIo组件通过GlacimonSpi方式配置, 默认无需手动配置, 有特殊需求时可以通过扩展点更改实现.
* 字段过滤器通过GlacimonSpi方式扩展, 按需添加过滤器.

## 扩展点

* 使用扩展点之前, 请先仔细阅读文档: https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md

### 规则注解管理器

* 扩展点: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.rule.RuleAnnotationManager
* 默认实现: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.rule.DefaultRuleAnnotationManager
* 说明: 将枚举类/POJO类字段上的注解解析为过滤规则. 如果想自定义注解类型, 可以扩展它.

### 过滤器提供者

* 扩展点: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.FilterProvider
* 默认实现: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.GlacimonSpiFilterProvider (GlacimonSpi专用)
* 说明: MapIo通过它查找字段过滤器. GlacimonSpiFilterProvider会通过SPI服务加载机制加载字段过滤器(Filter).

### 异常工厂

* 扩展点: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.ExceptionFactory
* 默认实现: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.DefaultExceptionFactory
* 说明: MapIo的公共逻辑和自带的过滤器在映射Map的过程中, 如果遇到错误, 会调用这个工厂创建异常, 默认异常类型是RuntimeException. 如果想自定义异常类型, 可以扩展它.

### 字段过滤器

* 扩展点(multi-service): com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter
* 默认实现: ToString / StringToInteger / StringCheckLength / StringCheckRegex / MapKeysToList / MapValuesToList ...
* 说明: 通过此扩展点添加字段过滤器

#### 添加字段过滤器示例:

* 1.实现自己的字段过滤器:

```text
/**
 * 过滤器需要实现com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter接口. 
 *
 * 如果过滤参数需要转为其他类型, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.ArgsConvertedFilter
 * 如果过滤参数需要转为Integer类型, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.IntArgsConvertedFilter
 * 如果过滤参数需要缓存, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.ArgsCachedFilter
 * 
 * 可以参考默认提供的过滤器:
 * com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter包
 * com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filters包
 *
 * ImplementationName注解指定过滤器名称, 这样规则注解里可以通过名称查找这个过滤器.
 */
@ImplementationName("FooFilter")
public final class FooFilter implements Filter {

    /**
     * <p>预先检查过滤参数的格式 (注意, 这个方法可能会被调用多次, 每次的入参都有可能不同).</p>
     * <p>当一个字典类被加载时, MapIo会检查其中的规则是否合法, 会调用本方法检查过滤参数是否合法.</p>
     *
     * <p>这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     * 所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.</p>
     *
     * @param args 过滤参数, 非空
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    @Override
    public void doPreCheckArgs(String[] args) throws Exception {
        // 在这里预检查过滤参数, 有问题就抛出异常
    }

    /**
     * <p>实现字段过滤(校验/转换)</p>
     *
     * @param element 过滤前字段, 非空
     * @param args 过滤参数, 非空. 它会经过doPreCheckArgs方法预检查, 所以到了这个方法, 过滤参数一般不会有问题, 不用再检查了. 
     * @param ruleInfo 过滤规则信息, 用于打印日志/输出错误信息
     * @return 过滤后字段 (不转换就返回原值!)
     */
    @Override
    public Object doFilter(Object element, String[] args, RuleInfo ruleInfo) {
        // 在这里对字段进行校验/转换
        // 此处以转String为例
        return String.valueOf(element);
    }

}
```

* 2.在类路径下创建配置文件: /META-INF/glacimonspi/services/multiple/com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter
* 3.在配置文件中添加过滤器:

```text
+com.package.package.FooFilter
+com.package.package.BarFilter
```

* 4.如果需要剔除默认的过滤器, 请参考[Glacimon SPI 文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md)

<br>
<br>

# 默认方式配置

* 手动创建MapIo实例, 需要手动配置.
* 字段过滤器通过反射机制, 根据类名创建实例, 按需添加过滤器.

## 配置组件

```text
public class Test {

    public void test(){
        MapIo mapIo = new MapIoImpl();
                //.setRuleAnnotationParser(ruleAnnotationManager);// DefaultRuleAnnotationManager
                //.setFilterProvider(filterProvider); // JavaFilterProvider, 它只支持通过反射机制, 根据类名创建实例
                //.setExceptionFactory(exceptionFactory); // DefaultExceptionFactory
        
        mapIo.doMap(...);
    }

}
```

## 添加字段过滤器

* 这种方式只支持根据类型(type)查找过滤器, 所以过滤器只需要实现好, 在规则注解里就可以用了(不能用名称查找)
* 实现自己的字段过滤器:

```text
/**
 * 过滤器需要实现com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.filter.Filter接口. 
 *
 * 如果过滤参数需要转为其他类型, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.ArgsConvertedFilter
 * 如果过滤参数需要转为Integer类型, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.IntArgsConvertedFilter
 * 如果过滤参数需要缓存, 可以继承com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter.ArgsCachedFilter
 * 
 * 可以参考默认提供的过滤器:
 * com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter包
 * com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filters包
 *
 * ImplementationName注解指定过滤器名称, 这样规则注解里可以通过名称查找这个过滤器.
 */
public final class FooFilter implements Filter {

    /**
     * <p>预先检查过滤参数的格式 (注意, 这个方法可能会被调用多次, 每次的入参都有可能不同).</p>
     * <p>当一个字典类被加载时, MapIo会检查其中的规则是否合法, 会调用本方法检查过滤参数是否合法.</p>
     *
     * <p>这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     * 所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.</p>
     *
     * @param args 过滤参数, 非空
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    @Override
    public void doPreCheckArgs(String[] args) throws Exception {
        // 在这里预检查过滤参数, 有问题就抛出异常
    }

    /**
     * <p>实现字段过滤(校验/转换)</p>
     *
     * @param element 过滤前字段, 非空
     * @param args 过滤参数, 非空. 它会经过doPreCheckArgs方法预检查, 所以到了这个方法, 过滤参数一般不会有问题, 不用再检查了. 
     * @param ruleInfo 过滤规则信息, 用于打印日志/输出错误信息
     * @return 过滤后字段 (不转换就返回原值!)
     */
    @Override
    public Object doFilter(Object element, String[] args, RuleInfo ruleInfo) {
        // 在这里对字段进行校验/转换
        // 此处以转String为例
        return String.valueOf(element);
    }

}
```