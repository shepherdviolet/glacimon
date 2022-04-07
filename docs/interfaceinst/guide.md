# InterfaceInstantiation 接口实例化工具 (For Spring)

* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/x/proxy/interfaceinst)

## 说明

* 将指定路径下的接口类实例化并作为Bean注册到ApplicationContext
* 可以对这些接口实例做AOP切面实现功能, 也可以自定义`接口实例化器`将接口实例化成想要的代理类
* `Maven/Gradle依赖配置`在本文最后

## 日志

* SLF4J日志包路径: `sviolet.slate.common.x.proxy.interfaceinst`
* 推荐日志级别: `INFO`
* 日志关键字: `InterfaceInst`
* [日志样例](https://github.com/shepherdviolet/slate/blob/master/docs/interfaceinst/log-sample.md)

## InterfaceInstantiation做了些什么?

* 扫描指定路径下的所有接口, 检查是否申明了@InterfaceInstance注解(可以配置为不需要注解, 也可以自定义注解)
* 对接口做代理, 每个方法均为空实现(可以自定义接口实例化器, 创建自定义代理类)
* 将接口实例作为Bean注册到Spring ApplicationContext, 默认BeanName为接口全限定名

# 基础用法

* 启用InterfaceInstantiation, 指定扫描路径
* @EnableInterfaceInstantiation注解可以多次声明, 配置不同的包路径和实例化器
* `basePackages`指定需要扫描的路径

```text
     @Configuration
     @EnableInterfaceInstantiation(
        basePackages = {
            "sample.facade", 
            "template.facade"
        }
     )
     public class AppConfiguration {
         ......
     }
```

* 在指定路径下定义接口, 加上@InterfaceInstance`标记注解`

```text
     package sample.facade;
     
     @InterfaceInstance
     public interface MyInterface {
         String method(String request);
     }
```

* 本工具会在Spring启动时, 将MyInterface接口实例化成一个名为sample.facade.MyInterface的Bean
* 我们可以对接口实例做AOP切面, 实现需要的逻辑(AOP过程省略...)
* 作为一个普通的Bean注入和使用

```text
     @Autowired
     private MyInterface myInterface;
     
     @RequestMapping("/test")
     public @ResponseBody String test(){
         return myInterface.method("hello");
     }
```

<br>
<br>
<br>

# 进阶用法

## 即使接口类没有@InterfaceInstance`标记注解`, 也进行实例化

```text
     @Configuration
     @EnableInterfaceInstantiation(
        annotationRequired = false, 
        basePackages = {
            "sample.facade"
        }
     )
     public class AppConfiguration {
         ......
     }
```

## 用自定义的`标记注解`代替@InterfaceInstance

* 定义一个`标记注解`

```text
package sample.anno;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {

}
```

* 指定自定义的`标记注解`

```text
     @Configuration
     @EnableInterfaceInstantiation(
        annotationClass = MyAnnotation.class, 
        basePackages = {
            "sample.facade"
        }
     )
     public class AppConfiguration {
         ......
     }
```

## 启用自定义的`接口实例化器`实例化指定包路径下的接口

```text
     @Configuration
     @EnableInterfaceInstantiation(
        interfaceInstantiator = MyInterfaceInstantiator.class, 
        basePackages = {
            "sample.facade"
        }
     )
     public class AppConfiguration {
         ......
     }
```

## 自定义`接口实例化器`

* 实现`接口实例化器`(可以参考DefaultInterfaceInstantiator)

```text
public class MyInterfaceInstantiator {

    public Object newInstance(Class<?> clazz) throws Exception {
        //根据接口类型clazz创建实例对象
    }

    String resolveBeanName(String className) throws Exception {
        //根据接口类名决定BeanName
    }
    
}
```

## 自定义能实现代理逻辑, 能获得ApplicationContext的`接口实例化器`

```text
public class MyInterfaceInstantiator extends ContextAwaredInterfaceInstantiator {

    private PortalService portalService;

    @Override
    public String resolveBeanName(String interfaceType) throws Exception {
        //根据接口类名决定BeanName
        //示例: 根据接口上的注解获得名称
        Class<?> interfaceClass = Class.forName(interfaceType);
        if (interfaceClass.isAnnotationPresent(BeanName.class)) {
            BeanName annotation = interfaceClass.getAnnotation(BeanName.class);
            if (!CheckUtils.isEmptyOrBlank(annotation.value())){
                return annotation.value();
            }
        }
        return interfaceType;
    }

    @Override
    protected void onInitialized(Class<?> interfaceType, Object proxy) {
        //当Spring初始化完成, 每个代理类都会触发该方法
    }

    @Override
    protected Object onMethodInvoke(Class<?> interfaceType, Object proxy, Method method, Object[] objects) throws Throwable {
        //实现代理逻辑
        //示例: 将所有接口调用都转向调用PortalService
        if (portalService == null) {
            throw new Exception("Application is not started yet, portalService is null");
        }
        if (objects.length == 1 && objects[0] instanceof String && "handle".equals(method.getName())){
            return portalService.handle((String) objects[0]);
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //从ApplicationContext中获取Bean, 注意该方法只会触发一次
        portalService = applicationContext.getBean(PortalService.class);
    }

}
```

<br>
<br>
<br>

# 高级用法

## 自定义`开关注解`(@Enable...)

* 定义`Selector`

```text
public class CustomInterfaceInstSelector extends InterfaceInstSelector {
    @Override
    protected Class<? extends Annotation> getEnableAnnotationType() {
        //对应自定义的开关注解
        return EnableMyFunction.class;
    }
}
```

* 定义`开关注解`
* 必须包含 basePackages / interfaceInstantiator / annotationRequired / annotationClass 参数
* 只需要修改: 注解名称, Import的`Selector`, 默认的接口实例化器, 默认是否判断`标记注解`, 默认的`标记注解`类型

```text
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CustomInterfaceInstSelector.class})//对应自定义的Selector
public @interface EnableMyFunction {

    /**
     * 配置需要实例化的接口类包路径(可指定多个)
     */
    String[] basePackages();

    /**
     * 配置接口类实例化器(可自定义实现)
     * 示例: 将默认的接口实例化器改为自己的MyInterfaceInstantiator
     */
    Class<? extends InterfaceInstantiator> interfaceInstantiator() default MyInterfaceInstantiator.class;

    /**
     * true: 指定包路径下的接口类, 必须申明指定注解才进行实例化(注解类型由annotationClass指定, 默认@InterfaceInstance).
     * false: 指定包路径下的接口类, 不声明指定注解也进行实例化.
     * 示例: 默认需要注解
     */
    boolean annotationRequired() default true;

    /**
     * 当annotationRequired为true时, 指定包路径下的接口类必须声明指定的注解才能实例化, 注解类型可以在这里定义.
     * 示例: 将默认注解改为MyAnnotation
     */
    Class<? extends Annotation> annotationClass() default MyAnnotation.class;

}

```

* `接口实例化器` 和 `标记注解` 请参考 `进阶用法`

<br>
<br>
<br>

# 依赖

* gradle

```text
//version替换为具体版本
dependencies {
    compile 'com.github.shepherdviolet:slate-common:?'
}
```

* maven

```maven
    <!--version替换为具体版本-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-common</artifactId>
        <version>?</version>
    </dependency>
```
