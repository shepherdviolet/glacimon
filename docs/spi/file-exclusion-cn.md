# GlacimonSpi 定义文件排除

```text
当类路径下存在有问题的定义文件时, 我们能够临时排除掉它. 
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 排除定义文件

### 1.启用日志

* 基础模式不向外输出日志, 请参考[首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)启用日志 (也可以参考首页从内存中提取日志)

### 2.将日志级别调至TRACE

* 内存日志: 添加启动参数`-Dglacimonspi.conf.mem.loglevel=TRACE`
* SLF4J日志: 将`com.github.shepherdviolet.glacimon.java.spi`包的日志级别调为`TRACE`
* System.out日志: 添加启动参数`-Dglacimonspi.conf.system.loglevel=TRACE`

### 3.观察日志

* 每一个文件的加载日志后面都有一个md5值
* 记录需要排除的md5值

```text
08:44:52.753 [main] TRACE com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi - 0 | Loading file file:/D:/glacimon/glacimon-spi-test/out/test/resources/META-INF/glacimonspi/services/multiple/com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin, md5:a936a6931ba1eaa50a6a1b18cf42fc77
08:44:52.942 [main] TRACE com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi - 0 | Loading file file:/D:/glacimon/glacimon-spi-test/out/production/resources/META-INF/glacimonspi/services/multiple/com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin, md5:bb16e015cfea2dc672777ebcd8d99ba2
```

* `注意:md5值是根据文件内容计算的, 文件内容一旦变化, md5值也会变化, 因此这种排除方法只能临时解决问题`

### 4.添加启动参数

* -Dglacimonspi.exclude.file=`md5`,`md5`,`md5`
* 以上述内容为例, -Dglacimonspi.exclude.file=a936a6931ba1eaa50a6a1b18cf42fc77,bb16e015cfea2dc672777ebcd8d99ba2
