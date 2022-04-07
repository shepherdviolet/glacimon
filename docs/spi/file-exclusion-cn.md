# Glaciion 定义文件排除

```text
当类路径下存在有问题的定义文件时, 我们能够临时排除掉它. 
```

[返回首页](https://github.com/shepherdviolet/glaciion/blob/master/docs/index-cn.md)

<br>

## 排除定义文件

### 1.将日志级别调至TRACE

* SLF4J: 将`com.github.shepherdviolet.glaciion`包的日志级别调为`TRACE`
* System.out: 添加启动参数`-Dglaciion.conf.system.loglevel=TRACE`

### 2.观察日志

* 每一个文件的加载日志后面都有一个md5值
* 记录需要排除的md5值

```text
08:44:52.753 [main] TRACE com.github.shepherdviolet.glaciion.Glaciion - 0 | Loading file file:/D:/glaciion/tests/out/test/resources/META-INF/glaciion/services/multiple/com.github.shepherdviolet.glaciion.test.SamplePlugin, md5:a936a6931ba1eaa50a6a1b18cf42fc77
08:44:52.942 [main] TRACE com.github.shepherdviolet.glaciion.Glaciion - 0 | Loading file file:/D:/glaciion/tests/out/production/resources/META-INF/glaciion/services/multiple/com.github.shepherdviolet.glaciion.test.SamplePlugin, md5:bb16e015cfea2dc672777ebcd8d99ba2
```

* `注意:md5值是根据文件内容计算的, 文件内容一旦变化, md5值也会变化, 因此这种排除方法只能临时解决问题`

### 3.添加启动参数

* -Dglaciion.exclude.file=`md5`,`md5`,`md5`
* 以上述内容为例, -Dglaciion.exclude.file=a936a6931ba1eaa50a6a1b18cf42fc77,bb16e015cfea2dc672777ebcd8d99ba2
