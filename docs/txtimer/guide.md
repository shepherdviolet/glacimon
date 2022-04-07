# TxTimer 简单的交易耗时统计

* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-txtimer/src/main/java/sviolet/slate/common/x/monitor/txtimer)
* TxTimer支持Glaciion SPI扩展, 并提供一个缺省的实现
* `缺省实现`实现了交易耗时的统计, 并通过日志定时输出报告
* 本文档讲述`缺省实现`的用法, 除了`全局禁用`方法以外, 其他的配置均只对`缺省实现`有效
* `Maven/Gradle依赖配置`在本文最后

## 全局禁用

* TxTimer默认启用, 如需禁用, 添加启动参数
* 任何实现都能用这个方法禁用

```text
-Dslate.txtimer.enabled=false
```

<br>
<br>

# 缺省实现

* TxTimer的定位: 比Micrometer+Prometheus+Grafana这种完善/实时/图形化的方案更加精简, 用于事后离线分析
* 日志体积: `1个交易持续24小时约4.2MB`, 日志压缩比: `约6%(4.2MB->252KB)`, 内存消耗: `每个交易约2.2KB`
* 缺省实现中, 数据记录的时间是交易结束时间, 也就是说报告中每个时间段的交易是在这个时间范围内结束的交易

## 日志

* SLF4J日志包路径: `sviolet.slate.common.x.monitor.txtimer.def`
* 推荐日志级别: `INFO`, 建议将日志输出到一个独立的文件中便于查看, 推荐单文件200MB, 最大历史50-100个(根据实际需求)
* 日志关键字: `TxTimer`
* [日志样例](https://github.com/shepherdviolet/slate/blob/master/docs/txtimer/log-sample.md)

## 记录耗时

* `注意!!! 必须使用try-with-resource或try-finally包裹, 确保stop被执行`
* `GroupName`是组别名, 统计报告按组别分类输出
* `TransactionName`是交易名

```text
    try (TimerContext timerContext = TxTimer.entry("GroupName", "TransactionName")) {
        // 交易逻辑 ......
    }
```

## 记录耗时(类库框架层用法)

* 在开发类库或框架时, 如果想要让用户自己决定是否启用TxTimer
* 可以使用`NoRefTxTimer`, 它对TxTimer没有直接类引用, 不会触发TxTimer类加载, 可以减少不必要的对象加载
* 当用户选择开启TxTimer时, 使用`NoRefTxTimerFactory.newInstance()`获得代理实例

```text
    private NoRefTxTimer txTimer;
    
    public void setTxTimerEnabled(){
        txTimer = NoRefTxTimerFactory.newInstance();
    }
```

* 仅当代理实例存在时, 记录耗时
* `注意!!! 必须使用try-with-resource或try-finally包裹, 确保stop被执行`

```text
    NoRefTxTimer txTimer = this.txTimer;
    if (txTimer != null) {
        try (TimerContext timerContext = txTimer.entry("GroupName", "TransactionName")) {
            // 交易逻辑 ......
        }
    } else {
        // 交易逻辑 ......
    }
```

## 统计报告配置

* 统计报告使用SLF4J输出, 你可以将该日志输出到一个独立的日志文件中便于查看
* `缺省实现`提供一些参数配置

### 不可动态修改的配置

* 由于内部实现原因, 这些配置不允许在运行时修改
* 目前只能通过启动参数调整

> -Dslate.txtimer.report.interval=5 日志报告输出间隔, 单位分钟, 2-60, 默认5 <br>
> -Dslate.txtimer.pagelines=20 日志每次输出的最大行数, 大于该行数会分页, 默认20 <br>

* 也可以在main函数中, 用如下方法调整默认值

```text
    public static void main(String[] args) {
        //若启动参数里设置了, 则用启动参数的设置, 否则默认为10
        System.setProperty("slate.txtimer.report.interval", 
                System.getProperty("slate.txtimer.report.interval", "10"));
        // 启动程序 ......
    }
```

* 默认情况下, 统计报告每隔五分钟输出一次, 时间间隔通过`slate.txtimer.report.interval`修改

### 可动态修改的配置

* 如下配置可以在启动参数里设置, 也可以在运行时动态修改
* 注意: 启动参数优先级高于运行时修改, 运行时无法修改在启动参数里指定的值

> -Dslate.txtimer.threshold.avg=2000 打印周期内平均耗时超过该值的交易, 单位:毫秒<br>
> -Dslate.txtimer.threshold.max=10000 打印周期内最大耗时超过该值的交易, 单位:毫秒<br>
> -Dslate.txtimer.threshold.min=1000 打印周期内最小耗时超过该值的交易, 单位:毫秒<br>
> -Dslate.txtimer.reportall.interval=60 全量日志报告输出间隔(周期), 单位:分钟, 2-∞, 默认∞(不输出全量日志)<br>
> -Dslate.txtimer.report.printpermin=true false时, 报告的周期为N分钟(默认5分钟, N为日志打印间隔), true时, 报告的周期为1分钟(日志量变大, 数据变精细), 默认false<br>

* 运行时修改

```text
    DefaultTxTimerConfig.setThresholdAvg(2000);
    DefaultTxTimerConfig.setThresholdMax(10000);
    DefaultTxTimerConfig.setThresholdMin(1000);
    DefaultTxTimerConfig.setReportAllInterval(60);
    DefaultTxTimerConfig.setReportPrintsPerMinute(true);
```

* 默认情况下, 统计报告会输出所有交易的信息, 可以通过`slate.txtimer.threshold`系列参数配置输出的条件
* 以上面的配置为例, 当一个交易的平均耗时超过2000ms, 或最大耗时超过10000ms, 或最小耗时超过1000ms, 才会输出信息, 否则不输出
* 默认情况下, 全量统计报告关闭, 可以通过`slate.txtimer.reportall.interval`参数指定一个输出间隔来开启
* 以上面的配置为例, 每隔5分钟, 日志中会打印满足`slate.txtimer.threshold`系列参数条件的交易信息, 每隔60分钟, 日志中会打印所有交易的信息
* 通常这些参数会配合使用, 设置较高的阈值, 避免频繁输出大量的信息, 开启全量统计日志, 在每隔较长的一段时间后, 输出一次全部信息

## 配合Spring容器/Apollo配置中心动态修改配置

* 在工程的配置类中添加注释@EnableTxTimerSpringConfig

```text
    @Configuration
    @EnableTxTimerSpringConfig
    public class AppConfiguration {
    }
```

* @EnableTxTimerSpringConfig注解实际上是引入了`DefaultTxTimerSpringConfig`配置类
* 因此只需要在yml/properties中, 或apollo配置中心中, 配置对应参数, 即可完成阈值的修改

* 支持修改的配置如下:

> slate.txtimer.threshold.avg=2000 打印周期内平均耗时超过该值的交易, 单位:毫秒<br>
> slate.txtimer.threshold.max=10000 打印周期内最大耗时超过该值的交易, 单位:毫秒<br>
> slate.txtimer.threshold.min=1000 打印周期内最小耗时超过该值的交易, 单位:毫秒<br>
> slate.txtimer.reportall.interval=60 全量日志报告输出间隔(周期), 单位:分钟, 2-∞, 默认∞(不输出全量日志)<br>
> slate.txtimer.report.printpermin=true false时, 报告的周期为N分钟(默认5分钟, N为日志打印间隔), true时, 报告的周期为1分钟(日志量变大, 数据变精细), 默认false<br>

<br>
<br>

# Glaciion SPI扩展点

## 自定义实现统计和报告逻辑

* 扩展点接口:sviolet.slate.common.x.monitor.txtimer.TxTimerProvider2
* 使用扩展点之前, 请先仔细阅读文档: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md

## 修改缺省实现的配置

* 缺省实现:sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider2
* 可以重新定义缺省实现的配置, 具体请阅读文档: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md

<br>
<br>

# 依赖

* gradle

```text
//version替换为具体版本
dependencies {
    compile 'com.github.shepherdviolet:slate-txtimer:?'
}
```

* maven

```maven
    <!--version替换为具体版本-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-txtimer</artifactId>
        <version>?</version>
    </dependency>
```
