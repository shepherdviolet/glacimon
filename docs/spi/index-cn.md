# GlacimonSpi

* A library of Java Service Provider Interface
* Git: https://github.com/shepherdviolet/glacimon
* [English Documents](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md)

<br>

## 什么是SPI?

```text
SPI是一种根据给定接口, 查找并加载对应实现的机制. 
GlacimonSpi是一种SPI机制的实现. 能为你的类库提供扩展能力. 
```

<br>

## 特性

* 能够通过选拔机制加载唯一的一个实现: single-service模式
* 能够通过插拔机制加载多个实现: multiple-service模式
* 能够通过选拔机制加载一份配置, 注入到实现中
* 能够在系统启动阶段预先检查和加载类路径下的所有服务: preload
* 能够通过定义文件/启动参数调整实现和配置

<br>

## Maven依赖

```gradle
repositories {
    //In mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacijava-spi-core:?'
}
```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-spi-core</artifactId>
        <version>?</version> 
    </dependency>
```

<br>

## 文档

* [Single-Service 模式](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/single-service-mode-cn.md)

```text
如果最终只需要一个实现, 请选择single-service模式. 
特点是接口类上的注解为@SingleServiceInterface. 
程序会根据定义文件中的优先度选择加载哪个实现. 
```

* [Multiple-Service 模式](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/multiple-service-mode-cn.md)

```text
如果最终需要多个实现, 请选择multiple-service模式. 
特点是接口类上的注解为@MultipleServiceInterface. 
程序会根据定义文件中的启用(+)与禁用(-)标记选择加载哪些实现. 
加载器能够根据名称获取实例, 也可以获取全部实例列表(根据优先度排序). 
```

* [配置注入](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/property-injection-cn.md)

```text
向实现类实例注入配置. 
程序会根据配置文件中的优先度选择应用哪个配置. 优先度最高的会被应用, 其他配置均不生效. 
```

* [预加载(preload)](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/preload-cn.md)

```text
预先检查并加载SPI定义, 若定义文件/接口类/实现类中有错误, 能够提前抛出错误. 
预加载能输出CheckSum值, 用于判断实现类/配置是否被意外改动(被依赖工程污染/手误修改等).
```

* [实现类生命周期](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/implementation-lifecycle-cn.md)

```text
实现类能够监听自身的创建和销毁事件(服务加载器被卸载). 
```

* [定义文件排除](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/file-exclusion-cn.md)

```text
当类路径下存在有问题的定义文件时, 我们能够临时排除掉它. 
```

* [关于缓存](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/about-cache-cn.md)

```text
服务加载完成后, 服务加载器(SingleServiceLoader/MultipleServiceLoader)会被缓存起来, 重复加载时会从缓存中获取. 
服务实例化后, 服务实例会被缓存在服务加载器中, 重复获取时会从缓存中获取. 
因此, 一般情况下, 服务实例会被永久持有(类似于Spring Context中的单例), 若服务实现类设计不当可能会造成内存泄露. 
为满足特殊使用场景, 允许卸载服务加载器(从缓存中删除).
```

* [升级接口](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/upgrade-interface-cn.md)

```text
扩展点接口要尽量保持不变. 如果修改了方法, 基于旧版本实现的服务会不兼容. 为了满足接口升级需求, GlacimonSpi支持在接口中新增方法, 
并提供一种向下兼容的办法. 
```

<br>

## 日志相关

* 当类路径中存在SLF4J时, 默认用SLF4J输出日志
* 当类路径中不存在SLF4J时, 默认用System.out输出日志

### SLF4J日志

* 日志包路径`com.github.shepherdviolet.glacimon.java.spi`
* 推荐级别`INFO`
* 如果遇到问题, 请将日志级别调至`DEBUG`或`TRACE`

### System.out日志

* 默认日志级别为`OFF`
* 如果遇到问题, 请将日志级别调至`DEBUG`或`TRACE`
* 通过启动参数调整日志级别

```text
-Dglacimonspi.conf.system.loglevel=ERROR
-Dglacimonspi.conf.system.loglevel=WARN
-Dglacimonspi.conf.system.loglevel=INFO
-Dglacimonspi.conf.system.loglevel=DEBUG
-Dglacimonspi.conf.system.loglevel=TRACE
```

* 通过启动参数调整日期格式

```text
-Dglacimonspi.conf.system.logtime=yyyyMMdd-HHmmss
```

### 自定义日志打印器

* 实现接口:com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger
* 添加启动参数:-Dglacimonspi.conf.custom.logger=`自定义日志打印器的类名`
