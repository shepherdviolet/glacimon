# GlacimonSpi Single-Service 模式

```text
如果最终只需要一个实现, 请选择single-service模式. 
特点是接口类上的注解为@SingleServiceInterface. 
程序会根据定义文件中的优先度选择加载哪个实现. 
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 服务调用方

### 1.编写接口类

```text
package sample;
@SingleServiceInterface
public interface SampleSingleService {
    String method();
}
```

### 2.在定义文件中声明

* 编辑文件`META-INF/glacimonspi/interfaces`
* 添加一行:

```text
sample.SampleSingleService
```

### 3.获取服务实例并调用

* 加载服务
* 若定义文件/接口类/实现类有问题, 会抛出异常
* Spring环境中或设置-Dglacimonspi.conf.preload.auto=true时, 该操作第一次会触发[预加载](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/preload-cn.md)

```text
//使用默认类加载器(上下文类加载器)
SingleServiceLoader<SampleSingleService> loader = GlacimonSpi.loadSingleService(SampleSingleService.class);
//使用指定类加载器
//SingleServiceLoader<SampleSingleService> loader = GlacimonSpi.loadSingleService(SampleSingleService.class, classloader);
```

* 获取服务实例
* 若实例化失败, 会抛出异常

```text
SampleSingleService instance = loader.get();
```

* `注意! 不同的类加载器创建的服务加载器不是同一个, 它们获取到的服务实例也不是同一个!`
* `GlacimonSpi只保证同一个类加载器产生的服务加载器是同一个, 同一个服务加载器创建的服务实例是同一个, 如果你创建服务加载器时的类加载器不同, 服务实例也不是同一个`
* `下面这种情况获取到的服务实例有可能是不同的:`

```text
class A {
    void method1(){
        //instance1不一定与instance2是同一个实例, 因为现场的ClassLoader可能不同, 所以SingleServiceLoader也不是同一个
        SampleSingleService instance1 = GlacimonSpi.loadSingleService(SampleSingleService.class).get();
    }
}
class B {
    void method2(){
        //instance1不一定与instance2是同一个实例, 因为现场的ClassLoader可能不同, 所以SingleServiceLoader也不是同一个
        SampleSingleService instance2 = GlacimonSpi.loadSingleService(SampleSingleService.class).get();
    }
}
```

<br>

## 服务提供方

### 1.编写实现类

```text
package sample;
public class SampleSingleServiceImpl implements SampleSingleService {
    @Override
    public String method() {
        //TO DO logic
    }
}
```

* 实现类能够注入配置参数, 见[配置注入](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/property-injection-cn.md)
* 实现类能够监听自身的创建和销毁事件, 见[实现类生命周期](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/implementation-lifecycle-cn.md)

### 2.在定义文件中声明

* 编辑文件`META-INF/glacimonspi/services/single/sample.SampleSingleService`
* 内容:

```text
sample.SampleSingleServiceImpl
```

* 定义文件路径:META-INF/glacimonspi/services/single/`接口类全限定名`
* 定义文件内容(默认优先度):`实现类全限定名`
* 定义文件内容(自定优先度):`实现类全限定名` `优先度`

<br>

## 选拔机制

* single-service模式下, 最终只有一个实现类会被加载
* 实现类的定义文件中, 可以调整实现的优先度
* 优先度数值越大, 优先度越高, 优先度最高的实现会被加载, 未设置则默认为0
* 启动参数(glacimonspi.select)优先度最高
* `特殊:当最高优先度的实现不止一个时, 根据实现类全限定名的hash决定加载哪个`

### 如何设置实现的优先度

* 定义文件路径:META-INF/glacimonspi/services/single/`接口类全限定名`
* 定义文件内容:`实现类全限定名` `优先度`

* 例如, 实现类sample.SampleSingleServiceImpl的优先度为1

```text
sample.SampleSingleServiceImpl 1
```

* 可以通过声明一个更高优先级的定义来提高实现的优先级

### 通过启动参数强制指定实现类

* 添加启动参数:-Dglacimonspi.select.`接口类全限定名`=`实现类全限定名`
* 例如:

```text
-Dglacimonspi.select.sample.SampleSingleService=sample.SampleSingleServiceImpl
```
