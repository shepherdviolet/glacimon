# 负载均衡的HttpClient使用手册

* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-http-client/src/main/java/sviolet/slate/common/x/net/loadbalance)
* 支持配置多个后端地址, 平均分配流量(随机, 不可指定)
* 支持被动/主动方式探测后端是否可用, 自动选择可用的后端发送请求
* 支持同步/异步方式请求

> Slate 11.X 版本后该客户端的包路径从sviolet.slate.common.modelx.loadbalance调整为sviolet.slate.common.x.net.loadbalance

# 设计思路

> 在分布式架构中, 为了消除热点, 服务方会以集群方式提供服务. 
> 对外提供接口时, 通常有运营商/硬件负载(F5)/软件负载(Nginx/Apache等)提供负载均衡, 即请求方向唯一的域名发送请求, 负载均衡负责将流量分配给服务集群. 
> 对内部系统提供接口时, 通常不会使用可靠的硬件负载均衡设备, 软负载(Nginx)也存在宕机的风险, 简单的域名映射无法探知后端状态, 
> 当服务方集群中的部分机器停机或故障时, 会导致大量的请求失败. 
> 本HttpClient就是为了内部系统访问设计的, 在请求方实现负载均衡, 平均分配流量, 并提供探知后端状态的能力, 尽可能地将请求向健康的服务器发送.

# 关于阻断机制

> 当主动探测器发现某个后端不可用(telnet或http-get), 会将其标记为阻断状态, 称为主动阻断<br>
> 当网络请求发生特定的异常, 会将对应后端标记为阻断状态, 称为被动阻断<br>
> 程序尽可能不向被阻断的后端发送请求, 直到阻断时间结束<br>
> 被动阻断时, 阻断时间结束会进入恢复期, 期间只允许向该后端发起一次请求, 若请求成功, 则放开流量限制, 否则继续阻断<br>
> 当所有后端都被标记为阻断状态时, 会将请求随机发给这些后端(可配置为拒绝请求)<br>
> 主动阻断时长由主动探测间隔决定, 主动探测间隔可配置, 被动阻断时长可配置, 恢复期时长可配置<br>
> 如果服务方支持, 使用http-get方式做主动探测更可靠(需要服务方提供一个http-get接口, 返回200)<br>

# 注意 | WARNING

```text
一个客户端实例对应一个服务端集群, 若要向不同的服务端集群发送请求, 必须创建多个客户端实例. 例如, 客户端需要向 
A系统(150.1.1.1,155.1.1.2) 和 B系统(150.1.2.1,150.1.2.2) 发送请求, 则需要配置两个客户端: 
ClientA (150.1.1.1,155.1.1.2) 和 ClientB (150.1.2.1,155.1.2.2). 使用ClientA向A系统发送请求, 
使用ClientB向B系统发送请求. 严禁将一个客户端用于请求不同的服务方集群, 这会导致请求被发往错误的服务端!!!

A client corresponds to a server cluster. You should use different client instances to send requests 
to different server clusters. For example, we need to send requests to system A (150.1.1.1, 155.1.1.2) 
and system B (150.1.2.1, 150.1.2.2), we need to create two clients: client A (150.1.1.1, 155.1.1.2) 
and client B (150.1.2.1, 155.1.2.2). Use client A to send requests to system A and client B to send 
requests to system B. It is strictly forbidden to requesting different service clusters by one client 
instance, the request will be sent to the wrong host!!!
```

```text
    /**
     * 错误示范, 请勿模仿!!!
     * 一个客户端用于向不同的后端服务发送请求, 后端地址在请求时才指定. 这种方式有严重的问题, 
     * 不仅仅是发送请求时无法使用到刚设置的后端地址, 而且在多线程环境下会把请求发到错误的服务端. 
     * 
     * Error demonstration, please do not imitate !!!
     * If a client is used to send requests to different host clusters (which provide 
     * different services), and you want to specify the host address before request, 
     * This is a big mistake ! The new hosts you set cannot take effect immediately, 
     * and requests will be send to the wrong host! 
     */
    public byte[] send(String hosts, byte[] request) {
        client.setHosts(hosts);//错误 Wrong !!!
        return client.post("/path/path")
                .body(request)
                .sendForBytes();
    }
```

```text
客户端所有配置均可以在运行时调整, set系列方法均为线程安全. 但是, 配置的调整是异步生效的, 即不会在执行set
方法的同时生效. 例如, 在发送请求前修改服务端地址(hosts), 请求仍然会被发往老的服务端地址. 
正确的方式是: 开发一个控制台, 在控制台中调整参数时, 调用客户端的set系列方法调整配置; 使用Apollo配置中心, 
监听到配置发生变化时, 调用客户端的set系列方法调整配置. 
错误的方式是: 在每次发送请求前调用set系列方法调整配置. 

All configuration of the client can be adjusted at runtime, and all the setter methods are thread 
safe. However, the configuration will be applied asynchronously, that is, they do not take effect 
at the same time as the set method is invoked.For example, modify the server address (hosts) before 
sending the request, the request will still be sent to the old address.
The correct way is: develop a console, invoke the client's setter method while adjusting 
configuration in console; Use a configuration center like the Apollo, monitoring configuration 
changes, invoke the client's setter method while the configuration changed.
The wrong way is: to invoke the setter method (adjust configurations) before sending the request.
```

```text
    @Value("${hosts:}")
    private String hosts;

    /**
     * 错误示范, 请勿模仿!!!
     * 在发送请求前, 才被动地设置最新的hosts, 客户端最终会使用旧的hosts发送请求, 新的hosts不生效!!!
     * 
     * Error demonstration, please do not imitate !!!
     * If you invoke the setHosts method to set the hosts before requesting, the request will 
     * still be sent to the old hosts !
     */
    public byte[] send(byte[] request) {
        client.setHosts(hosts);//错误 Wrong !!!
        return client.post("/path/path")
                .body(request)
                .sendForBytes();
    }
```

# 配置客户端

* [SpringBoot YML自动配置(推荐)](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/config-springboot.md)
* [Spring 注解手动配置](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/config-annotation.md)
* [Spring XML手动配置](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/config-xml.md)

# 调用客户端

* [同步发送请求](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/invoke-sync.md)
* [异步发送请求](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/invoke-async.md)

# 其他

* [SSL相关配置(自定义SSL验证逻辑/访问自签名的服务端/改变域名验证)](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/ssl.md)

# 关于日志

* SLF4J日志包路径: `sviolet.slate.common.x.net.loadbalance`
* 推荐日志级别: `INFO`
* 需要打印请求报文和响应码的, 可以将`verboseLog`调为true (配合`INFO`级别)
* 需要更详细的内容, 可以将级别调为`DEBUG`
* SimpleOkHttpClient(MultiHostOkHttpClient)还支持日志微调, 详见源码中的verboseLogConfig配置
* 日志关键字: `LoadBalance` / `HttpClient` / `HttpClients`
* 包含: 请求/响应日志, Hosts变化日志, 主动探测日志, 被动阻断日志, SpringBoot自动配置日志, Apollo配置实时调整日志等
* 建议将`sviolet.slate.common.x.net.loadbalance.LoadBalancedInspectManager`日志输出到单独文件中(主动探测日志)
