# GlacimonSpi 预加载

```text
预先检查并加载SPI定义, 若定义文件/接口类/实现类中有错误, 能够提前抛出错误. 
预加载能输出CheckSum值, 用于判断实现类/配置是否被意外改动(被依赖工程污染/手误修改等).
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 什么是预加载

```text
后端系统通常由多人开发. 如果一个开发人员调整了定义文件, 或者引入了一个包含GlacimonSpi定义文件的类库, 就有可能导致实现被意外替换, 配置
被意外调整, 最终导致生产事故. 另外, 如果不在启动阶段加载全部服务, 就无法在系统上线的第一时间发现问题. 预加载就是被设计用来解决此类
问题的.提前扫描和加载类路径下的所有定义文件和配置文件, 如果存在问题, 则抛出异常. 最终可以输出一个CheckSum值, 用于判断定义和配置是
否被修改. 预加载不会创建服务实例. 
```

<br>

## 自动预加载

* 自动预加载是指:在第一次加载服务时, 自动触发预加载
* Spring环境下默认开启自动预加载(判断类路径下是否存在ApplicationContext类)
* 非Spring环境下默认关闭自动预加载
* 可以添加启动参数开启或关闭自动预加载

```text
-Dglacimonspi.conf.preload.auto=true
-Dglacimonspi.conf.preload.auto=false
```

<br>

## 手动预加载

* 可以在系统启动阶段手动调用API开始预加载

```text
//使用默认类加载器预加载
GlacimonSpi.preload();
//使用指定类加载器预加载
//GlacimonSpi.preload(classloader);
```

<br>

## CheckSum

* 预加载完成后, 可以获得一个CheckSum值
* 添加启动参数'-Dglacimonspi.conf.preload.checksum=false`可以关闭CheckSum的计算

### 什么情况会导致CheckSum值变化

* 类路径中扩展点(GlacimonSpi接口)的增加和减少
* 实现类的变更和增减(包括顺序和名称)
* 实现类注入参数的变化(包括用启动参数调整)

### 如何使用CheckSum值

* 在日志中可以看到CheckSum值

```text
11:40:57.470 [main] INFO com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi - ? | Preload | CheckSum 1851083996, classloader:sun.misc.Launcher$AppClassLoader@18b4aac2
```

* 通过API获取CheckSum值
* `注意:若预加载未完成或CheckSum被关闭, 会返回null`

```text
//获得使用默认类加载器预加载的CheckSum
Integer checkSum = GlacimonSpi.getPreloadCheckSum();

//获得使用指定类加载器预加载的CheckSum
//Integer checkSum = GlacimonSpi.getPreloadCheckSum(classloader);

//获得所有CheckSum值, Map的Key是类加载器的ID
//Map<String, Integer> checkSums = GlacimonSpi.getPreloadCheckSums()
```

* 极端严格的情况下, 可以在更新包或配置中心配置本次程序包的CheckSum值, 在系统启动阶段自动检查
