# 全局追踪工具

* [Source Code](https://github.com/shepherdviolet/thistle/tree/master/thistle-trace/src/main/java/sviolet/thistle/x/util/trace)

# API

## 开始追踪

```text
Trace.start();
```

## 存取其他信息

```text
Trace.setData("key", "value");
String value = Trace.getData("key");
```

## 继续追踪

* 原线程(原进程)中获取接力信息

```text
//接力信息
TraceBaton traceBaton = Trace.getBaton();
//接力信息还可以转为String
//String stringData = traceBaton.toString();
```

* 新线程(新进程)开始时继续追踪

```text
//将String格式的接力信息解析为Bean(这个可能会抛出异常)
//TraceBaton traceBaton = TraceBaton.fromString(stringData);
Trace.handoff(traceBaton);
```

## 异步追踪帮助

```text
//开始追踪
Trace.start();
//存信息
Trace.setData("key", "value");

......

//异步调用时可以用Trace.traceable方法制造Runnable/Callable
threadPool.execute(Trace.traceable(()-> {
    //这个Runnable在实例化时会获取接力信息(TraceBaton), 在run方法前会完成接力(Trace.handoff(traceBaton))
    //String traceId = Trace.getTraceId();
    //String value = Trace.getData("key");
    ......
}));
```

<br>
<br>

# 默认特性

* 默认实现为: DefaultTraceProvider
* `追踪号`和其他追踪信息保存在ThreadLocal中
* 如果应用依赖SLF4J, `追踪号`还会存入MDC, KEY为`_trace_id_`, 可以打印在日志中
* 以logback打印追踪号为例:

```text
    <property name="logging.pattern" value="${log.pattern:-%d %X{_trace_id_} %-5p %t %c{40} %L: %m%n}"/>

    <appender name="ConsoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${logging.pattern}</pattern>
        </encoder>
        ......
    </appender>
```

* `追踪号`默认为无符号的UUID (例如:3e164de2db664773bdfe9427dd05e3f8)
* `追踪号`可以设置为较短的URL-Safe-Base64格式 (例如:PhZN4ttmR3O9_pQn3QXj-A, `RFC4648_URLSAFE编码, 并去掉了末尾的==`)

```text
// 方式一: (高优先级) 通过Java启动参数将追踪号设置为URL-Safe-Base64格式
-Dthistle.trace.trace-id-compressed=true

// 方式二: 通过Glaciion SPI扩展点 的特性设置
1.在Classpath下创建文件: META-INF/glaciion/properties/sviolet.thistle.x.util.trace.DefaultTraceProvider
2.编写内容:

traceIdCompressed=true

```

<br>
<br>

# Glaciion SPI扩展点

* 扩展点: sviolet.thistle.x.util.trace.TraceProvider
* 详见文档: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md
