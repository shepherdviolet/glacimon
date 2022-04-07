# GlacimonSpi 配置注入

```text
向实现类实例注入配置. 
程序会根据配置文件中的优先度选择应用哪个配置. 优先度最高的会被应用, 其他配置均不生效. 
```

[返回首页](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## 向实现类注入配置

* `single-service`模式和`multiple-service`模式的实现类均支持配置注入
* 注入的数据类型支持: String/boolean/Boolean/int/Integer/long/Long/float/Float/double/Double

### 1.实现类

```text
package sample;
public class SampleServiceImpl implements SampleService {

    /**
     * 向成员变量注入, 参数名等于成员变量名(示例中为"dataFormat"). 
     */
    @PropertyInject
    private String dateFormat;

    /**
     * 向方法注入, 方法名必须为标准的Getter格式(setXxxXxx), 且入参只能有一个. 
     * 参数名由方法名决定, 驼峰格式, 首字母小写(示例中为"logEnabled"). 
     */
    @PropertyInject
    public void setLogEnabled(boolean value){
        //TO DO logic
    }
    
    /**
     * 高级用法:getVmOptionFirst
     * 优先从启动参数中获取参数, 若不存在, 则从配置文件中获取. 
     * 启动参数名由注解的getVmOptionFirst决定(示例中为"sample.service.id", 即通过"-Dsample.service.id=1"设置).
     * 配置文件中的参数名仍然由方法名决定(示例中为"serviceId", 即通过"serviceId=1"设置).
     */
    @PropertyInject(getVmOptionFirst = "sample.service.id")
    public void setServiceId(int value) {
        //TO DO logic
    }

    @Override
    public String method() {
        //TO DO logic
    }

}
```

### 2.配置文件

* 添加文件`META-INF/glacimonspi/properties/sample.SampleServiceImpl`
* 内容:

```text
dateFormat=yyyy-MM-dd HH:mm:ss
logEnabled=true
serviceId=1
```

* 配置文件路径:META-INF/glacimonspi/properties/`实现类全限定名`
* 配置文件内容为标准properties格式

<br>

## 通过启动参数调整配置

### 当@PropertyInject注解指定了`getVmOptionFirst`

* 添加启动参数:-D`getVmOptionFirst指定的参数名`=`参数值`
* 以上文为例:-Dsample.service.id=2

### 通用方式

* 添加启动参数:-Dglacimonspi.property.`实现类全限定名`.`参数名`=`参数值`
* 以上文为例:-Dglacimonspi.property.sample.SampleServiceImpl.serviceId=2

<br>

## 选拔机制

* 只有一个配置文件会被加载, 其他落选配置文件中的参数均无效
* 配置文件中, 通过添加`@priority`参数调整优先度
* 优先度数值越大, 优先度越高, 优先度最高的配置文件会被加载, 未设置则默认为0
* 启动参数(glacimonspi.property)在配置文件的基础上调整参数
* `特殊:当最高优先度的配置文件不止一个时, 根据配置文件内容的hash决定加载哪个`

### 示例

* 配置1(落选):

```text
dateFormat=yyyy-MM-dd HH:mm:ss
logEnabled=true
serviceId=1
```

* 配置2(落选):

```text
@priority=1
dateFormat=yyyy-MM-dd HH:mm:ss
logEnabled=true
serviceId=2
```

* 配置3(采纳):

```text
@priority=2
dateFormat=yyyy-MM-dd HH:mm:ss.SSS
logEnabled=true
serviceId=3
```

* 启动参数:

```text
-Dglacimonspi.property.sample.SampleServiceImpl.logEnabled=false
-Dsample.service.id=4
```

* 最终注入的参数为:
* `dateFormat=yyyy-MM-dd HH:mm:ss.SSS`
* `logEnabled=false`
* `serviceId=4`
* 其中第一个参数由最高优先级的配置文件决定
* 后两个参数由启动参数决定
