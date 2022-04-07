# EzSentinel | 一种Sentinel动态规则的变通方案

* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-helper/src/main/java/sviolet/slate/common/helper/sentinel)
* [Sentinel是啥?](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8)

## Sentinel动态规则的现状

```text
Sentinel目前的开源版中, Dashboard向客户端推送限流熔断规则是不完美的, 默认是直接推送到客户端内存中, 无法保证可靠性, 
且客户端重启即失效, 而使用API方式硬编码也难以维护. 官方给出了最佳实践的方案, PUSH模式, 目前需要改造Dashboard, 
由Dashboard向一个数据源推送规则, 而客户端则连接数据源获取更新. 这个方案确实是最佳方案, 网上也有人给出了基于APOLLO/ZK
等数据源的改造示例, 但是它们都绕不开一个中间件, 而且开源版的Dashboard功能实际上是较弱的, 在服务数量很多的场景中, 
一个一个在Dashboard中配置也很繁琐, 如果需要改进这些, 需要大量地改造Dashboard, 这样在后续官方迭代时, 获取更新会很痛苦. 
```

## EzSentinel可以帮助我们动态调整规则

```text
EzSentinel是一个简易的动态规则变通方案, 不改造Dashboard, 也不配置客户端数据源, 在Sentinel的使用上等同于官方说的"原始模式", 
官方的Dashboard仅用于观察, 查看规则是否生效. 
EzSentinel的本质是通过一个大JSON来维护规则(人工), 然后手动将这个JSON配置到Apollo配置中心, 借助Apollo实时将配置用setter方法
注入的特性, 在setter方法中调用Sentinel的规则配置API来实现规则调整. 如果不用Apollo, 也可以将大JSON放在classpath中读取(无法
动态调整).
```

## EzSentinel可以帮助我们关闭Sentinel

```text
Sentinel的SpringBoot版提供一个参数spring.cloud.sentinel.enabled, 用来开关Sentinel. 但实际上这个参数对核心包的功能无效, 即对本
地监控限流熔断无效, 核心包的功能必须用com.alibaba.csp.sentinel.Constants.ON=false关闭. 
EzSentinel的EzSentinelConfiguration中监听了spring.cloud.sentinel.enabled参数, 讲它作用于Constants.ON, 这样就可以用一个参数完
全关闭Sentinel了(调整开关状态后需要重启应用).
另外, 非SpringBoot版本不太一样, 见源码JsonEzSentinelRuleConfigurerForSpring4. 
```

* 添加启动参数/YAML参数/APOLLO参数即可: spring.cloud.sentinel.enabled=false

# 如何使用

## 控制台

* 控制台直接使用官方开源版, 不做任何改造, 不需要数据源

## 客户端(SpringBoot)

* 添加依赖

```text
//Sentinel官方依赖
compile "com.alibaba.cloud:spring-cloud-starter-alibaba-sentinel:$version_sentinel"
//EzSentinel依赖 (slate-helper 14.5+)
compile "com.github.shepherdviolet:slate-helper:$version_slate"
//默认的JsonEzSentinelRuleConfigurer使用GSON解析JSON, 你也可以不要GSON, 自行实现AbstractEzSentinelRuleConfigurer
compile "com.google.code.gson:gson:$version_gson"
```

* <重要>修改RT(耗时)最大值, 默认为4900ms, 根据实际情况来

```text
  添加启动参数: -Dcsp.sentinel.statistic.max.rt=120000
  或
  在main函数中: System.setProperty("csp.sentinel.statistic.max.rt", "120000");
```

* application.yaml

```text
## Sentinel官方配置
spring:
  cloud:
    sentinel:
      ## 总开关, 默认开, 修改后需重启
      enabled: true
      transport:
        ## <重要>dashboard地址
        dashboard: localhost:60000
        ## 本地监听端口, 用于接收指令, 这个端口冲突了也没关系, 会自动向后找
        port: 60001
        ## <重要>本机IP, 让Dashboard发现自己, Windows环境一般可以不设置(自动获取), Linux环境有两种方案
        ## 方案1.不配置这个参数, 让程序自动获取, 但是必须正确配置Linux主机名和IP (/etc/sysconfig/network和/etc/hosts), 否则会取不到
        ## 方案2.写死这个参数, 或在启动脚本中设置这个参数, 可以在启动脚本中利用linux命令获取本机IP
        client-ip: ?.?.?.?
      metric:
        ## 设置日志文件大小和数量
        file-single-size: 52428800
        file-total-count: 6
      log:
        ## 指定日志路径(另外还有两个日志要启动参数设置-DEAGLEEYE.LOG.PATH和-DJM.LOG.PATH)
        dir: logs/

## EzSentinel配置
slate:
  common:
    ez-sentinel:
      ## <重要>规则: 可以指向classpath中的一个文件
      rule-data: classpath:config/demo/sentinel/rules.json
      ## <重要>规则: 也可以直接是JSON内容(这种情况通常是配置在Apollo的)
      #rule-data: {...json...}
```

* (备忘)Linux脚本如何获取本机IP?

```text
    HOSTIP=$(/sbin/ifconfig -a | sed -n '2p' |awk '{print $2}')
    HOSTIP=`echo $HOSTIP|cut -b6-20`

    -Dspring.cloud.sentinel.transport.client-ip=$HOSTIP
```

* EzSentinel规则格式(支持注释), 具体参数可以参考[官方文档](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8)

```text
EzSentinel的规则数据与官方的持久化数据格式不同, 官方的格式是一个资源对应多个规则详细参数. 而EzSentinel的格式是先配置几个规则组(ruleGroup), 
然后指定每个资源用哪个规则组. 这样的方式对手工编辑文本比较友好, 规则内容也更加简洁. 
```

```text
{
  //https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8
  //Rule version, optional
  "ruleVersion":"1.0.0",
  //System rule for all resources, optional
  "systemRule":{"highestSystemLoad":"-1", "avgRt":"-1", "maxThread":"-1", "qps":"-1"},
  //Define rule groups
  "ruleGroups":{
    //Rule for slow transactions
    "slow":{
      //Required: count=int, grade=THREAD/QPS
      //Optional: limitApp=String, strategy=DIRECT/RELATE/CHAIN, controlBehavior=DEFAULT/WARM_UP/RATE_LIMITER/WARM_UP_RATE_LIMITER
      "flowRules":[
        //Max 10 threads
        {"count":"10", "grade":"THREAD", "limitApp":"default", "strategy":"DIRECT", "controlBehavior":"DEFAULT"},
        //Max 20 QPS
        {"count":"20", "grade":"QPS", "limitApp":"default", "strategy":"DIRECT", "controlBehavior":"DEFAULT"}
      ],
      //Required: count=RT:ms|EXCEPTION_RATIO:0.0-0.1|EXCEPTION_COUNT:int, timeWindow=seconds, grade=RT/EXCEPTION_RATIO/EXCEPTION_COUNT
      //Optional: limitApp=String
      "degradeRules": [
        //Degrade if exception ratio > 50% for 60s
        {"count":"0.5", "timeWindow":"60", "grade":"EXCEPTION_RATIO", "limitApp":"default"}
      ]
    },
    //Rule for fast transactions
    "fast":{
      //Required: count=int, grade=THREAD/QPS
      //Optional: limitApp=String, strategy=DIRECT/RELATE/CHAIN, controlBehavior=DEFAULT/WARM_UP/RATE_LIMITER/WARM_UP_RATE_LIMITER
      "flowRules":[
        //Max 100 threads
        {"count":"100", "grade":"THREAD", "limitApp":"default", "strategy":"DIRECT", "controlBehavior":"DEFAULT"},
        //Max 200 QPS
        {"count":"200", "grade":"QPS", "limitApp":"default", "strategy":"DIRECT", "controlBehavior":"DEFAULT"}
      ],
      //Required: count=RT:ms|EXCEPTION_RATIO:0.0-0.1|EXCEPTION_COUNT:int, timeWindow=seconds, grade=RT/EXCEPTION_RATIO/EXCEPTION_COUNT
      //Optional: limitApp=String
      "degradeRules": [
        //Degrade if RT > 4000ms for 60s (count must be less than 4900 unless you set parameter csp.sentinel.statistic.max.rt)
        {"count":"4000", "timeWindow":"60", "grade":"RT", "limitApp":"default"},
        //Degrade if exception ratio > 50% for 60s
        {"count":"0.5", "timeWindow":"60", "grade":"EXCEPTION_RATIO", "limitApp":"default"}
      ]
    }
  },
  //ResourceName -> ruleGroup
  "resourceRules":{
    "test-annotation":"fast",
    "test-api":"slow"
  }
}
```

* <重要>启用EzSentinel

```text
@EnableEzSentinel
public class YourConfiguration {
    ...
}
```

* 定义资源, 更多内容请参考[官方文档](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8)

```text
注解方式

    @SentinelResource("ResourceName")
    @RequestMapping("")
    public String index() {
        ...
    }

API方式

    try (Entry entry = SphU.entry("HelloWorld")) {
        ...
    } catch (BlockException ex) {
        ...
    }
```

* 启动应用, 可以观察相关日志, 关键字`EzSentinel`. Sentinel本身的日志在application.yaml中指定, 默认在${user.home}/logs下

* 访问Dashboard, 找到应用(需发生过请求才会有显示), 可以观察`流控规则``降级规则`是否生效
* 另外, Dashboard`流控规则`中会有`_ez_`开头的资源, 它们用于展示规则的`更新时间`/`版本`/`更新失败原因`, 便于管理员判断下发的规则是否生效

* `动态下发规则: `如果应用接入了Apollo配置中心, 只需要将规则数据(JSON), 配置到Apollo, 参数名为`slate.common.ez-sentinel.rule-data`, 点击发布即可将规则下发给对应的应用(准实时生效). 发布后请观察Dashboard, 确保规则下发成功. 

## 客户端(非SpringBoot)

* 添加依赖

```text
//Sentinel官方依赖
compile "com.alibaba.csp:sentinel-core:$version_sentinel"
compile "com.alibaba.csp:sentinel-annotation-aspectj:$version_sentinel"
compile "com.alibaba.csp:sentinel-transport-simple-http:$version_sentinel"
//EzSentinel依赖 (slate-common 14.5+)
compile "com.github.shepherdviolet:slate-common:$version_slate"
//默认的JsonEzSentinelRuleConfigurer使用GSON解析JSON, 你也可以不要GSON, 自行实现AbstractEzSentinelRuleConfigurer
compile "com.google.code.gson:gson:$version_gson"
```

* 添加启动参数(后续某版本可以配置在classpath:sentinel.properties中)

```text
## Sentinel官方配置

# <重要>修改RT(耗时)最大值, 默认为4900ms, 根据实际情况来
csp.sentinel.statistic.max.rt=120000

# <重要>application name
project.name=spring-boot-template
# log configs
#csp.sentinel.log.dir=logs/
#csp.sentinel.metric.file.single.size=52428800
#csp.sentinel.metric.file.total.count=6
#EAGLEEYE.LOG.PATH=logs/
#JM.LOG.PATH=logs/

## <重要>dashboard address
csp.sentinel.dashboard.server=localhost:60000
## 本地监听端口, 用于接收指令, 这个端口冲突了也没关系, 会自动向后找
csp.sentinel.api.port=60001
## <重要>本机IP, 让Dashboard发现自己, Windows环境一般可以不设置(自动获取), Linux环境有两种方案
## 方案1.不配置这个参数, 让程序自动获取, 但是必须正确配置Linux主机名和IP (/etc/sysconfig/network和/etc/hosts), 否则会取不到
## 方案2.写死这个参数, 或在启动脚本中设置这个参数, 可以在启动脚本中利用linux命令获取本机IP
#csp.sentinel.heartbeat.client.ip=?.?.?.?
#csp.sentinel.heartbeat.interval.ms=

## EzSentinel配置
## <重要>规则: 可以指向classpath中的一个文件
slate.common.ez-sentinel.rule-data: classpath:config/demo/sentinel/rules.json
## <重要>规则: 也可以直接是JSON内容(这种情况通常是配置在Apollo的)
#slate.common.ez-sentinel.rule-data: {...json...}
```

* (备忘)Linux脚本如何获取本机IP?

```text
    HOSTIP=$(/sbin/ifconfig -a | sed -n '2p' |awk '{print $2}')
    HOSTIP=`echo $HOSTIP|cut -b6-20`

    -Dcsp.sentinel.heartbeat.client.ip=$HOSTIP
```

* EzSentinel规则格式, 此处省略, 请参考上文`客户端(SpringBoot)`章节内容

* 启用EzSentinel, 请参考`EzSentinelConfiguration`配置类, 将其中的Bean用XML形式声明

```text
    <!-- 使用非SpringBoot专用的版本, 这个版本不是根据spring.cloud.sentinel.enabled开关的 -->
    <!-- 是根据四个必要的启动参数是否设置来决定是否启用Sentinel的, 详见源码 -->
    <bean id="ezSentinelRuleConfigurer" class="sviolet.slate.common.helper.sentinel.JsonEzSentinelRuleConfigurerForSpring4">
        <property name="ruleData" value="${slate.common.ez-sentinel.rule-data:}"/>
    </bean>
```

* 定义资源, 此处省略, 请参考上文`客户端(SpringBoot)`章节内容

* 启动应用, 可以观察相关日志, 关键字`EzSentinel`. Sentinel本身的日志在Spring Environment/Java VM options中指定, 默认在${user.home}/logs下

* 访问Dashboard, 找到应用(需发生过请求才会有显示), 可以观察`流控规则``降级规则`是否生效
* 另外, Dashboard`流控规则`中会有`_ez_`开头的资源, 它们用于展示规则的`更新时间`/`版本`/`更新失败原因`, 便于管理员判断下发的规则是否生效

* `动态下发规则: `如果应用接入了Apollo配置中心, 只需要将规则数据(JSON), 配置到Apollo, 参数名为`slate.common.ez-sentinel.rule-data`, 点击发布即可将规则下发给对应的应用(准实时生效). 发布后请观察Dashboard, 确保规则下发成功. 

## 调整Apollo字段长度

* Apollo配置中心默认的字段长度限制是20000, 建议根据实际需求适当调大
* 登录Apollo数据库`apolloconfigdb`用户
* 执行SQL, 等一会生效无需重启Apollo

```text
UPDATE `ServerConfig` SET `Value` = '200000' WHERE `Key` = 'item.value.length.limit';
```

* 另外, Apollo参数值太长的话, 在Apollo控制台发布时会遇到困难, 弹出框需要下拉很久才能看到发布按钮, 有两个办法快速滚动:

```text
方法1:鼠标点击弹出框, 按TAB键可以直接跳到最下面的输入框, 就能看见发布按钮. 
方法2:在页面上端点击鼠标滚轮, 将鼠标拉到页面下端, 拖动距离越远速度越快. 
```

## 推荐配套TxTimer使用

* [TxTimer](https://github.com/shepherdviolet/slate/blob/master/docs/txtimer/guide.md)

```text
开源版的SentinelDashboard没有数据源, 没办法看五分钟前的统计数据, 这使得生产出问题的时候, 难以判断哪些服务(资源)拖垮了系统, 
配套TxTimer(或Prometheus等更好的方案), 可以从更加宏观的角度判断问题, 配套Skywalking等全链路监控, 还可以针对个别请求判断问题. 
```
