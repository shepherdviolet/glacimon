# HttpClient调用方法(同步GET/POST)

* 关于URL地址

> 以本文档中的代码为例 <br>
> http://127.0.0.1:8081 和 http://127.0.0.1:8082 是某后台系统的两个节点, 我们要向它们请求数据, 请求的URL为 /user/update.json <br>
> 配置HttpClient的hosts参数为 http://127.0.0.1:8081,http://127.0.0.1:8082 <br>
> 调用客户端发送请求 client.get("/user/update.json").send() <br>
> 程序会自动选择一个应用服务器, 最终的请求地址为 http://127.0.0.1:8081/user/update.json 或 http://127.0.0.1:8082/user/update.json <br>

* 其他说明

> 同步方式中`maxThreads` / `maxThreadsPerHost`配置无效, 使用调用线程发起网络请求 <br>

## `注意!!!`

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
              //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
         //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
 try (GlaciHttpClient.ResponsePackage responsePackage = client.post("/path/path")
         .body("hello world".getBytes())
         //.formBody(formBody)//表单提交
         //.beanBody(bean)//发送JavaBean, 需要配置dataConverter, 见配置文档
         //.httpHeader("Accept", "application/json;charset=utf-8")
         //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
                //.httpHeader("Accept", "application/json;charset=utf-8")
                //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
                //.encode("utf-8")
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
              //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
              //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
         //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
 try (GlaciHttpClient.ResponsePackage responsePackage = client.get("/path/path")
         .urlParam("name", "000000001")
         .urlParam("key", "000000001")
         //.httpHeader("Accept", "application/json;charset=utf-8")
         //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
                //.httpHeader("Accept", "application/json;charset=utf-8")
                //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
                //.encode("utf-8")
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
              //.mediaType("application/json;charset=utf-8") //不要用httpHeader方法设置Content-Type, 请用mediaType方法设置
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
