# GlacimonSpi 升级接口

```text
扩展点接口要尽量保持不变. 如果修改了方法, 基于旧版本实现的服务会不兼容. 为了满足接口升级需求, GlacimonSpi支持在接口中新增方法, 
并提供一种向下兼容的办法. 
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 新增方法

* 在新增的方法上添加@NewMethod注解, 并配置一个`兼容办法(CompatibleApproach)`
* 若实现类未实现新增的方法, GlacimonSpi会为它生成一个代理类, 在调用未实现的方法时, 由`兼容办法(CompatibleApproach)`代为处理

```text
@SingleServiceInterface
//@MultipleServiceInterface
public interface SampleService {

    String oldMethod(String param);

    /**
     * 若实现类未实现该方法, 在调用时直接返回null, 什么都不处理
     */
    @NewMethod(compatibleApproach = DoNothing.class)
    String newMethod1(String param);

    /**
     * 若实现类未实现该方法, 在调用时会交由NewMethodCompat类处理
     */
    @NewMethod(compatibleApproach = NewMethodCompat.class)
    String newMethod2(String param);

    class NewMethodCompat implements CompatibleApproach {
        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] params) throws Throwable {
            //当调用newMethod2时, 实际上会调用oldMethod
            return ((SampleService)serviceInstance).oldMethod(params[0]);
        }
    }

}
```
