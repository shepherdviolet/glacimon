# GlacimonSpi 实现类声明周期

```text
实现类能够监听自身的创建和销毁事件(服务加载器被卸载). 
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 监听服务实例创建完成的事件

* 实现InitializableImplementation接口

```text
public class SampleServiceImpl implements SampleService, InitializableImplementation {

    @Override
    public void onServiceCreated() {
        //当服务实例被创建完成后, 会调用该方法, 此时所有的参数都已注入完毕, 通常用于进行初始化操作
    }
    
    @Override
    public String method() {
        //TO DO logic
    }
}
```

<br>

## 监听服务实例关闭的事件

* 实现CloseableImplementation接口

```text
public class SampleServiceImpl implements SampleService, CloseableImplementation {

    //持有类型为AtomicBoolean的closed对象, 当服务被关闭后, 该值会变为true
    //如果服务中有异步线程, 可以将closed作为循环条件, 当close为true时退出循环
    private AtomicBoolean closed;
    
    @Override
    public void setCloseFlag(AtomicBoolean closed) {
        //持有closed对象
        this.closed = closed;
    }
    
    @Override
    public String method() {
        //通过判断closed对象值是否为true, 来感知服务是否被关闭
        if (closed != null && closed.get()) {
            //closed
        } else {
            //not closed
        }
    }
}
```

* 当实例对应的服务加载器被卸载时, closed值会变为true, 见[关于缓存](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/about-cache-cn.md)
* 当服务加载器(SingleServiceLoader/MultipleServiceLoader)被调用closed方法后, closed值会变为true
