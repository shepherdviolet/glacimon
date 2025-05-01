# GlaciHttpClient (glacispring-httpclient) 使用手册

* [Source Code](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-httpclient/src/main/java/com/github/shepherdviolet/glacimon/spring/x/net/loadbalance)
* 支持客户端负载均衡, 可配置多个后端地址, 平均分配流量 (Round-Robin)
* 支持被动/主动方式探测后端是否可用, 优先请求可用的后端
* 支持Apollo配置动态调整 (无需重启应用即可生效)

# 设计思路

> 每个HttpClient内部维护一组后端地址, 通过被动/主动探测的方式维护这些地址的健康状态, 采用轮询策略(Round-Robin)选择健康的地址发送请求.

# 关于阻断机制

> 当主动探测器发现某个后端不可用(telnet或http-get), 会将其标记为阻断状态, 称为主动阻断. <br>
> 当网络请求发生特定的异常(例如超时)或返回特定的HTTP响应码, 会将对应后端标记为阻断状态, 称为被动阻断. <br>
> 程序尽可能不向被阻断的后端发送请求, 直到阻断时间结束. <br>
> 被动阻断时, 阻断时间结束会进入恢复期, 期间只允许向该后端发起一次请求, 若请求成功, 则放开流量限制, 否则继续阻断. <br>
> 当所有后端都被标记为阻断状态时, 会将请求随机发给这些后端(可配置为拒绝请求). <br>
> 主动阻断时长由主动探测间隔决定, 主动探测间隔可配置, 被动阻断时长可配置, 恢复期时长可配置<br>
> 如果服务方支持, 使用http-get方式做主动探测, 主动探测更可靠(需要服务方提供一个http-get接口, 返回200)<br>

# `注意!!!`

* 每个HttpClient实例对应一个后端集群, 多个后端集群应创建多个HttpClient实例. 

```text
    GlaciHttpClient clientFoo;
    GlaciHttpClient clientBar;
    
    public byte[] sendToFoo(byte[] request) {
        return client.post("/path/path")
                .body(request)
                .sendForBytes();
    }
    
    public byte[] sendToBar(byte[] request) {
        return client.post("/path/path")
                .body(request)
                .sendForBytes();
    }
```

* `严禁用一个HttpClient请求不同的后端集群, 这会导致请求被发往错误的后端!!!`

```text
    GlaciHttpClient client;

    /**
     * 严禁用一个HttpClient请求不同的后端集群. 
     * setHosts允许运行时调整, 但是配置是异步生效的! 这么干会把请求发给错误的后端!
     */
    public byte[] send(String hosts, byte[] request) {
        client.setHosts(hosts); // 错误示范!!! 严禁用一个HttpClient请求不同的后端集群
        return client.post("/path/path")
                .body(request)
                .sendForBytes();
    }
```

* HttpClient所有的配置都允许运行时调整, `但是新配置是异步生效的!`

```text
    /**
     * HttpClient所有的配置都允许运行时调整, set系列方法是线程安全的. 但是, 新配置是异步生效的, 即不会在执行set方法后立即生效. 
     * 仅限于配合Apollo等配置中心, 对HttpClient进行长效性配置调整. 禁止在请求调用前临时调整配置. 
     */
    public byte[] send(String hosts, byte[] request) {
        client.setConnectTimeout(newTimeout); // 错误示范!!! 禁止在请求调用前临时调整配置, 新配置不会立即生效!
        return client.post("/path/path")
                .body(request)
                .sendForBytes();
    }
```

# 配置客户端

* [SpringBoot YML自动配置(推荐)](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/config-springboot.md)
* [Spring 注解手动配置](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/config-annotation.md)
* [Spring XML手动配置](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/config-xml.md)

# 调用客户端

* [同步发送请求](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/invoke-sync.md)
* [异步发送请求](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/invoke-async.md)

# 其他

* [SSL相关配置(自定义SSL验证逻辑/访问自签名的服务端/改变域名验证)](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/ssl.md)

# 关于日志

* SLF4J日志包路径: `com.github.shepherdviolet.glacimon.spring.x.net.loadbalance`
* 日志关键字: `LoadBalance` / `HttpClient` / `HttpClients`
* 日志内容: 请求/响应日志, Hosts变化日志, 主动探测日志, 被动阻断日志, SpringBoot自动配置日志, Apollo配置实时调整日志等
* ----推荐日志级别: `INFO`
* ----需要更详细的内容, 可以将级别调为`DEBUG`/`TRACE`
* ----GlaciHttpClient还支持日志微调, 详见GlaciHttpClient源码中的setLogPrint...系列方法
* ----`建议`将主动探测日志`com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalancedInspectManager`输出到单独文件中 (设为`DEBUG`级别可以持续观察后端状态)
