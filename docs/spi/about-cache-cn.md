# Glaciion 关于缓存

```text
服务加载完成后, 服务加载器(SingleServiceLoader/MultipleServiceLoader)会被缓存起来, 重复加载时会从缓存中获取. 
服务实例化后, 服务实例会被缓存在服务加载器中, 重复获取时会从缓存中获取. 
因此, 一般情况下, 服务实例会被永久持有(类似于Spring Context中的单例), 若服务实现类设计不当可能会造成内存泄露. 
为满足特殊使用场景, 允许卸载服务加载器(从缓存中删除).
```

[返回首页](https://github.com/shepherdviolet/glaciion/blob/master/docs/index-cn.md)

<br>

## 引用关系

* SingleServiceLoader和MultipleServiceLoader类的静态变量引用了服务加载器实例
* 服务加载器引用了服务实例
* 因此服务实例被静态变量间接引用

<br>

## 卸载服务加载器(特殊)

* 如果你的实现类需要被妥善地关闭, 即收到关闭的信号, 你可以在需要时调用卸载API来通知所有实现类关闭, 
见[实现类生命周期](https://github.com/shepherdviolet/glaciion/blob/master/docs/implementation-lifecycle-cn.md)
* 如果你的程序需要动态加载/卸载类, 即需要在运行时创建和销毁类加载器, 你需要在销毁类加载器时, 卸载这个类加载器加载的所有服务, 
否则会造成内存泄露
* 卸载操作本质上是将服务加载器从缓存中删除, 然后设置服务加载器的closed状态为true, 
而实现了CloseableImplementation接口的实现类可以根据持有的closed标记判断自己是否被关闭, 
见[实现类生命周期](https://github.com/shepherdviolet/glaciion/blob/master/docs/implementation-lifecycle-cn.md)

```text
//卸载默认类加载器加载的所有服务, 常用于在系统停止时通知实现类关闭
Glaciion.uninstallDefaultClassloader();
//卸载指定类加载器加载的所有服务, 常用于运行时需要销毁类加载器的场景
//Glaciion.uninstall(classloader);
```
