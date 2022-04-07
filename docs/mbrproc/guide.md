# Bean Member Processor | Bean成员处理器 (For Spring)

* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/x/bean/mbrproc)

## 说明

* 用于对Spring上下文中所有的Bean的成员(Field/Method)进行处理, 处理时机为Bean装配阶段(BeanPostProcessor), 成员需标注指定注解
* 可以实现Bean成员的自定义注入/变换/代理替换等
* `@EnableMemberProcessor`注解允许多次声明, 声明不同的处理器处理不同的注解
* `Maven/Gradle依赖配置`在本文最后

## 日志

* SLF4J日志包路径: `sviolet.slate.common.x.bean.mbrproc`
* 推荐日志级别: `INFO`
* 日志关键字: `MemberProcessor`

<br>
<br>
<br>

## 用法

* 定义一个注解, 用于标注在成员变量或方法上

```text
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAnnotation {
    // ...
}
```

* 编写一个成员处理器, 用于处理标注了指定注解的成员变量或方法

```text
public class MyMemberProcessor implements MemberProcessor<MyAnnotation> {

    public Class<MyAnnotation> acceptAnnotationType() {
        //声明本处理器接收的注解类型. 注意: 不允许存在两个以上的处理器处理同一个注解!
        return MyAnnotation.class;
    }

    public void visitField(Object bean, String beanName, Field field, HttpClient annotation, ApplicationContext applicationContext) {
        //处理每个Bean的Field (前提是Field上申明了指定的注解)
    }

    public void visitMethod(Object bean, String beanName, Method method, HttpClient annotation, ApplicationContext applicationContext) {
        //处理每个Bean的Method (前提是方法上申明了指定的注解)
    }

}
```

* 启用成员处理器

```text
@Configuration
@EnableMemberProcessor(MyMemberProcessor.class)
public class MyConfiguration {
    // ...
}
```

* 当Spring启动时, 对于上下文中所有的Bean, 标注了指定注解(MyAnnotation)的成员(Field/Method)都会通过成员处理器(MyMemberProcessor)处理

<br>
<br>
<br>

## 示例

* 以slate-http-client的客户端注入为例, 我们要实现将HTTP客户端注入到标注了`@HttpClient`注解的成员变量/方法上

* 定义注解, 此处省略部分代码, [详见源码](https://github.com/shepherdviolet/slate/blob/master/slate-http-client/src/main/java/sviolet/slate/common/x/net/loadbalance/springboot/autowired/HttpClient.java)

```text
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpClient {
    String value();
    boolean required() default true;
}
```

* 编写成员处理器, 此处省略部分代码, [详见源码](https://github.com/shepherdviolet/slate/blob/master/slate-http-client/src/main/java/sviolet/slate/common/x/net/loadbalance/springboot/autowired/HttpClientMemberProcessor.java)

```text
public class HttpClientMemberProcessor implements MemberProcessor<HttpClient> {

    @Override
    public Class<HttpClient> acceptAnnotationType() {
        // 处理HttpClient注解
        return HttpClient.class;
    }

    @Override
    public void visitField(Object bean, String beanName, Field field, HttpClient annotation, ApplicationContext applicationContext) {
        if (!SimpleOkHttpClient.class.isAssignableFrom(field.getType())) {
            // (此处省略代码) 类型不匹配, 报错
        }
        SimpleOkHttpClient client = getHttpClient(applicationContext, annotation, bean);
        if (client != null) {
            // 注入
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, client);
        }
    }

    @Override
    public void visitMethod(Object bean, String beanName, Method method, HttpClient annotation, ApplicationContext applicationContext) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            // (此处省略代码) 方法入参超过一个, 报错
        }
        if (!SimpleOkHttpClient.class.isAssignableFrom(parameterTypes[0])) {
            // (此处省略代码) 入参类型不匹配, 报错
        }
        SimpleOkHttpClient client = getHttpClient(applicationContext, annotation, bean);
        if (client != null) {
            // 注入
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, bean, client);
        }
    }

    private SimpleOkHttpClient getHttpClient(ApplicationContext applicationContext, HttpClient annotation, Object bean) {
        // (此处省略代码) 从Spring上下文中获取HTTP客户端实例
    }

}
```

* 启用成员处理器, 此处省略部分代码, [详见源码](https://github.com/shepherdviolet/slate/blob/master/slate-http-client/src/main/java/sviolet/slate/common/x/net/loadbalance/springboot/autoconfig/HttpClientsConfig.java)

```text
@Configuration
@ConditionalOnExpression("${slate.httpclient.enabled:false}")
@EnableMemberProcessor(HttpClientMemberProcessor.class)//开启@HttpClient注解注入
public class HttpClientsConfig {
    // (此处省略代码)
}
```

* 这个示例实现了, 在Spring启动时, 将HTTP客户端注入到标注了`@HttpClient`注解的地方

<br>
<br>
<br>

# 高级用法

## 自定义`开关注解`(@Enable...)

* 定义`Selector`

```text
public class CustomMemberProcessorSelector extends MemberProcessorSelector {
    @Override
    protected Class<? extends Annotation> getEnableAnnotationType() {
        //对应自定义的开关注解
        return EnableMyFunction.class;
    }
}
```

* 定义`开关注解`
* 必须包含 value 参数
* 只需要修改: 注解名称, Import的`Selector`

```text
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CustomMemberProcessorSelector.class})//对应自定义的Selector
public @interface EnableMyFunction {

    Class<? extends MemberProcessor>[] value();

}

```

* 这样就可以用自定义注解`@EnableMyFunction`开启成员处理器了

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
