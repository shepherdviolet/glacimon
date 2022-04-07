# HttpClient调用方法(同步GET/POST)

* 关于URL地址

> 以本文档中的代码为例 <br>
> http://127.0.0.1:8081 和 http://127.0.0.1:8082 是两台应用服务器, 我们要向他们请求数据, 请求的URL后缀为 /user/update.json <br>
> 配置SimpleOkHttpClient或LoadBalancedHostManager的hosts参数为 http://127.0.0.1:8081,http://127.0.0.1:8082 <br>
> 调用客户端发送请求 multiHostOkHttpClient.get("/user/update.json").send() <br>
> 程序会自动选择一个应用服务器, 然后将应用服务器的地址与URL后缀拼接 <br>
> 最终的请求地址为 http://127.0.0.1:8081/user/update.json 或 http://127.0.0.1:8082/user/update.json <br>

* 建议

> 同步方式通常在服务端使用时, 通常需要自行实现线程隔离/线程数限制/等待队列限制等 <br>
> 同步方式中`maxThreads` / `maxThreadsPerHost`配置无效, 使用调用线程发起网络请求 <br>

* `注意 | WARNING`

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

### POST

* 同步POST:返回byte[]类型的响应
 
 ```text
  try {
      byte[] response = client.post("/path/path")
              .urlParam("traceId", "000000001")
              .body("hello world".getBytes())
              //.formBody(formBody)//表单提交
              //.beanBody(bean)//发送JavaBean, 需要配置dataConverter, 见配置文档
              //.httpHeader("Accept", "application/json;charset=utf-8")
              //.mediaType("application/json;charset=utf-8")
              //.encode("utf-8")
              .sendForBytes();
  } catch (NoHostException e) {
      //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
  } catch (RequestBuildException e) {
      //在网络请求未发送前抛出的异常
  } catch (IOException e) {
      //网络异常
  } catch (HttpRejectException e) {
      //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
      //获得拒绝码 e.getResponseCode()
      //获得拒绝信息 e.getResponseMessage()
  }
 ```

* 同步POST:返回InputStream类型的响应
* 注意:InputStream需要手动关闭(close), 示例中是使用try-with-resource语法糖写法关闭的

 ```text
 try (InputStream inputStream = client.post("/path/path")
         .body("hello world".getBytes())
         //.formBody(formBody)//表单提交
         //.beanBody(bean)//发送JavaBean, 需要配置dataConverter, 见配置文档
         //.httpHeader("Accept", "application/json;charset=utf-8")
         //.mediaType("application/json;charset=utf-8")
         //.encode("utf-8")
         .sendForInputStream()) {

     inputStream......

 } catch (NoHostException e) {
     //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
 } catch (RequestBuildException e) {
     //在网络请求未发送前抛出的异常
 } catch (IOException e) {
     //网络异常
 } catch (HttpRejectException e) {
     //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
     //获得拒绝码 e.getResponseCode()
     //获得拒绝信息 e.getResponseMessage()
 }
```

* 同步POST:返回ResponsePackage类型的响应
* 注意:ResponsePackage需要手动关闭(close), 示例中是使用try-with-resource语法糖写法关闭的

 ```text
 try (MultiHostOkHttpClient.ResponsePackage responsePackage = client.post("/path/path")
         .body("hello world".getBytes())
         //.formBody(formBody)//表单提交
         //.beanBody(bean)//发送JavaBean, 需要配置dataConverter, 见配置文档
         //.httpHeader("Accept", "application/json;charset=utf-8")
         //.mediaType("application/json;charset=utf-8")
         //.encode("utf-8")
         .send()) {

     String response = responsePackage.body().string();

 } catch (NoHostException e) {
     //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
 } catch (RequestBuildException e) {
     //在网络请求未发送前抛出的异常
 } catch (IOException e) {
     //网络异常
 } catch (HttpRejectException e) {
     //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
     //获得拒绝码 e.getResponseCode()
     //获得拒绝信息 e.getResponseMessage()
 }
```

* 同步POST:请求报文体Map, 返回报文体Map
* 注意:必须要配置dataConverter, 见配置文档

```text
 try {
        Map<String, Object> requestMap = new HashMap<>(2);
        requestMap.put("name", "wang wang");
        requestMap.put("key", "963");
        Map<String, Object> responseMap = client.post("/path/path")
                .beanBody(requestMap)
                .sendForBean(Map.class);
 } catch (NoHostException e) {
     //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
 } catch (RequestBuildException e) {
     //在网络请求未发送前抛出的异常
 } catch (IOException e) {
     //网络异常
 } catch (HttpRejectException e) {
     //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
     //获得拒绝码 e.getResponseCode()
     //获得拒绝信息 e.getResponseMessage()
 }
```

* 同步POST:返回JavaBean类型的响应
* 注意:必须要配置dataConverter, 见配置文档

 ```text
  try {
      ResponseBean response = client.post("/path/path")
              .urlParam("traceId", "000000001")
              .body("hello world".getBytes())
              //.formBody(formBody)//表单提交
              //.beanBody(bean)//发送JavaBean, 需要配置dataConverter, 见配置文档
              //.httpHeader("Accept", "application/json;charset=utf-8")
              //.mediaType("application/json;charset=utf-8")
              //.encode("utf-8")
              .sendForBean(ResponseBean.class);
  } catch (NoHostException e) {
      //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
  } catch (RequestBuildException e) {
      //在网络请求未发送前抛出的异常
  } catch (IOException e) {
      //网络异常
  } catch (HttpRejectException e) {
      //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
      //获得拒绝码 e.getResponseCode()
      //获得拒绝信息 e.getResponseMessage()
  }
 ```

### GET

* 同步GET:返回byte[]类型的响应
 
 ```text
  try {
      byte[] response = client.get("/path/path")
              .urlParam("name", "000000001")
              .urlParam("key", "000000001")
              //.httpHeader("Accept", "application/json;charset=utf-8")
              //.mediaType("application/json;charset=utf-8")
              //.encode("utf-8")
              .sendForBytes();
  } catch (NoHostException e) {
      //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
  } catch (RequestBuildException e) {
      //在网络请求未发送前抛出的异常
  } catch (IOException e) {
      //网络异常
  } catch (HttpRejectException e) {
      //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
      //获得拒绝码 e.getResponseCode()
      //获得拒绝信息 e.getResponseMessage()
  }
 ```

* 同步GET:返回InputStream类型的响应
* 注意:InputStream需要手动关闭(close), 示例中是使用try-with-resource语法糖写法关闭的

 ```text
 try (InputStream inputStream = client.get("/path/path")
         .urlParam("name", "000000001")
         .urlParam("key", "000000001")
         //.httpHeader("Accept", "application/json;charset=utf-8")
         //.mediaType("application/json;charset=utf-8")
         //.encode("utf-8")
         .sendForInputStream()) {

     inputStream......

 } catch (NoHostException e) {
     //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
 } catch (RequestBuildException e) {
     //在网络请求未发送前抛出的异常
 } catch (IOException e) {
     //网络异常
 } catch (HttpRejectException e) {
     //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
     //获得拒绝码 e.getResponseCode()
     //获得拒绝信息 e.getResponseMessage()
 }
```

* 同步GET:返回ResponsePackage类型的响应
* 注意:ResponsePackage需要手动关闭(close), 示例中是使用try-with-resource语法糖写法关闭的

 ```text
 try (MultiHostOkHttpClient.ResponsePackage responsePackage = client.get("/path/path")
         .urlParam("name", "000000001")
         .urlParam("key", "000000001")
         //.httpHeader("Accept", "application/json;charset=utf-8")
         //.mediaType("application/json;charset=utf-8")
         //.encode("utf-8")
         .send()) {

     String response = responsePackage.body().string();

 } catch (NoHostException e) {
     //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
 } catch (RequestBuildException e) {
     //在网络请求未发送前抛出的异常
 } catch (IOException e) {
     //网络异常
 } catch (HttpRejectException e) {
     //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
     //获得拒绝码 e.getResponseCode()
     //获得拒绝信息 e.getResponseMessage()
 }
```

* 同步GET:返回报文体Map
* 注意:必须要配置dataConverter, 见配置文档

```text
 try {
        Map<String, Object> responseMap = client.get("/path/path")
                .urlParam("name", "000000001")
                .urlParam("key", "000000001")
                .sendForBean(Map.class);
 } catch (NoHostException e) {
     //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
 } catch (RequestBuildException e) {
     //在网络请求未发送前抛出的异常
 } catch (IOException e) {
     //网络异常
 } catch (HttpRejectException e) {
     //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
     //获得拒绝码 e.getResponseCode()
     //获得拒绝信息 e.getResponseMessage()
 }
```

* 同步GET:返回JavaBean类型的响应
* 注意:必须要配置dataConverter, 见配置文档

 ```text
  try {
      ResponseBean response = client.get("/path/path")
              .urlParam("name", "000000001")
              .urlParam("key", "000000001")
              //.httpHeader("Accept", "application/json;charset=utf-8")
              //.mediaType("application/json;charset=utf-8")
              //.encode("utf-8")
              .sendForBean(ResponseBean.class);
  } catch (NoHostException e) {
      //当hosts没有配置任何后端地址, 或配置returnNullIfAllBlocked=true时所有后端都处于异常状态, 则抛出该异常
  } catch (RequestBuildException e) {
      //在网络请求未发送前抛出的异常
  } catch (IOException e) {
      //网络异常
  } catch (HttpRejectException e) {
      //HTTP拒绝, 即HTTP返回码不为200(2??)时, 抛出该异常
      //获得拒绝码 e.getResponseCode()
      //获得拒绝信息 e.getResponseMessage()
  }
 ```
