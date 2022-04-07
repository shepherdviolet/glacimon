# GlacimonSpi Multiple-Service 模式

```text
如果最终需要多个实现, 请选择multiple-service模式. 
特点是接口类上的注解为@MultipleServiceInterface. 
程序会根据定义文件中的启用(+)与禁用(-)标记选择加载哪些实现. 
加载器能够根据名称获取实例, 也可以获取全部实例列表(根据优先度排序). 
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 服务调用方

### 1.编写一个接口类

```text
package sample;
@MultipleServiceInterface
public interface SampleMultipleService {

    String method();
    
}
```

### 2.在定义文件中声明

* 编辑文件`META-INF/glacimonspi/interfaces`
* 添加一行:

```text
sample.SampleMultipleService
```

### 3.获取服务实例并调用

* 加载服务
* 若定义文件/接口类/实现类有问题, 会抛出异常
* Spring环境中或设置-Dglacimonspi.conf.preload.auto=true时, 该操作第一次会触发[预加载](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/preload-cn.md)

```text
//使用默认类加载器(上下文类加载器)
MultipleServiceLoader<SampleMultipleService> loader = GlacimonSpi.loadMultipleService(SampleMultipleService.class);

//使用指定类加载器
//MultipleServiceLoader<SampleMultipleService> loader = GlacimonSpi.loadMultipleService(SampleMultipleService.class, classloader);
```

* 获取服务实例
* 若实例化失败, 会抛出异常

```text
//指定名称获取
SampleMultipleService instance = loader.get("name");

//获取全部(按实现优先度排序, 优先度数值越大, 优先度越高, 第0个优先度最高)
List<SampleMultipleService> instances = loader.getAll();
```

* `注意! 不同的类加载器创建的服务加载器不是同一个, 它们获取到的服务实例也不是同一个!`
* `GlacimonSpi只保证同一个类加载器产生的服务加载器是同一个, 同一个服务加载器创建的服务实例是同一个, 如果你创建服务加载器时的类加载器不同, 服务实例也不是同一个`
* `下面这种情况获取到的服务实例有可能是不同的:`

```text
class A {
    void method1(){
        //instance1不一定与instance2是同一个实例, 因为现场的ClassLoader可能不同, 所以MultipleServiceLoader也可能不同
        SampleMultipleService instance1 = GlacimonSpi.loadMultipleService(SampleMultipleService.class).get("name");
    }
}
class B {
    void method2(){
        //instance1不一定与instance2是同一个实例, 因为现场的ClassLoader可能不同, 所以MultipleServiceLoader也可能不同
        SampleMultipleService instance2 = GlacimonSpi.loadMultipleService(SampleMultipleService.class).get("name");
    }
}
```

<br>

## 服务提供方

### 1.编写实现类

```text
package sample;

//实现名称, 可选, 不设置则无法通过名称获取
@ImplementationName("name")
//实现优先度, 可选, 数值越大, 优先度越高, 第0个优先度最高, 不设置则优先度为0
@ImplementationPriority(1)
public class SampleMultipleServiceImpl1 implements SampleMultipleService {

    @Override
    public String method() {
        //TO DO logic
    }
    
}
```

* 实现类能够注入配置参数, 见[配置注入](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/property-injection-cn.md)
* 实现类能够监听自身的创建和销毁事件, 见[实现类生命周期](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/implementation-lifecycle-cn.md)
* `特殊:当实现名称重复时, 只有一个能用名称获取, 其他的只能通过getAll获取到`
* `特殊:当实现优先度相同时, 根据实现类全限定名的hash排序`

### 2.在定义文件中声明

* 编辑文件`META-INF/glacimonspi/services/multiple/sample.SampleMultipleService`
* 内容:

```text
+sample.SampleMultipleServiceImpl1
+sample.SampleMultipleServiceImpl2
+sample.SampleMultipleServiceImpl3
```

* 定义文件路径:META-INF/glacimonspi/services/multiple/`接口类全限定名`
* 定义文件内容:+`实现类全限定名`

<br>

## 插拔机制

* multiple-service模式下, 能够加载多个实现类
* 实现类的定义文件中, 有启用和禁用两种指令, +代表启用, -代表禁用
* 启用(+)和禁用(-)指令有级别之分, +和-为一级, ++和--为二级, +++和---为三级, 以此类推
* 同级别的指令禁用(-)比启用(+)优先度更高
* 启动参数(glacimonspi.delete)优先度最高

```text
判断一个实现是否被启用的逻辑是, 看最高级别的指令, 存在-则禁用, 只存在+则启用. 
实际使用时, 想要启用一个实现, 需要添加一个更高级别的启用(+)指令. 想要禁用一个
实现, 只需要添加一个与最高级别同等级的禁用(-)指令即可.
```

### 示例

* 定义文件路径:META-INF/glacimonspi/services/multiple/`接口类全限定名`

#### 下面几种情况服务最终被启用了

* 最高级别为一级, 只有+, 最终启用

```text
+sample.SampleMultipleServiceImpl1
```

* 最高级别是二级, 只有++, 最终启用

```text
+sample.SampleMultipleServiceImpl1
-sample.SampleMultipleServiceImpl1
++sample.SampleMultipleServiceImpl1
```

#### 下面几种情况服务最终被禁用了

* 最高级别是一级, 有+有-, 最终禁用

```text
+sample.SampleMultipleServiceImpl1
-sample.SampleMultipleServiceImpl1
```

* 最高级别是二级, 有++有--, 最终禁用

```text
+sample.SampleMultipleServiceImpl1
-sample.SampleMultipleServiceImpl1
++sample.SampleMultipleServiceImpl1
--sample.SampleMultipleServiceImpl1
```

### 通过启动参数强制删除实现

* 添加启动参数-Dglacimonspi.remove.`接口类全限定名`=`实现类全限定名1`,`实现类全限定名2`
* 例如:

```text
-Dglacimonspi.remove.sample.SampleMultipleService=sample.SampleMultipleServiceImpl1,sample.SampleMultipleServiceImpl2
```
