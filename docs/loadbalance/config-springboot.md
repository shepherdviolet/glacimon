# HttpClient配置方法(SpringBoot YML自动配置)

* `Maven/Gradle依赖配置`在本文最后

<br>
<br>
<br>

# YML配置(推荐)

* 在application.yml/application-profile.yml中增加配置

```yaml
slate:
  httpclient:
    enabled: true
  httpclients:
    client1:
      hosts: http://127.0.0.1:8081,http://127.0.0.1:8082
      initiative-inspect-interval: 5000
      passive-block-duration: 30000
      connect-timeout: 3000
      write-timeout: 10000
      read-timeout: 10000
      max-read-length: 10485760
      verbose-log: true
    client2:
      # 方式二, 优先级低. 在properties中: slate.httpclients.client2.host-list[0]=http://127.0.0.1:8083
      host-list:
        - http://127.0.0.1:8083
        - http://127.0.0.1:8084
      initiative-inspect-interval: 5000
      passive-block-duration: 30000
      connect-timeout: 3000
      write-timeout: 10000
      read-timeout: 10000
      max-read-length: 10485760
      verbose-log: true
```

* 以上文为例, 启用并配置了client1和client2两个HTTP请求客户端
* client1有两个主机http://127.0.0.1:8081和http://127.0.0.1:8082
* client2有两个主机http://127.0.0.1:8083和http://127.0.0.1:8084
* hosts配置的优先级比hostList高, 同时配置时只有hosts生效
* 健康主动探测间隔(initiativeInspectInterval)为5000ms
* 健康被动探测阻断时长(passiveBlockDuration)为30000ms
* connectTimeout/writeTimeout/readTimeout分别为连接/写/读超时时间, 单位ms
* maxReadLength数据最大读取长度, 单位字节
* verbose-log为true: INFO级别可打印更多的日志(请求报文/响应码等)

### YML中所提供的全部配置说明

```yaml
slate:
  httpclient:
    # 启用HttpClients (必须, 修改该配置需重启, 源码见HttpClientsConfig)
    enabled: true
    # 使用Apollo配置中心动态调整配置 (可选, 修改该配置需重启, 源码见HttpClientsApolloConfig)
    apollo-support: true
    # 设置Apollo配置的Namespace, 多个用逗号分隔, 默认为空(默认监听应用默认私有配置application). 如非必要, 请勿配置该参数.
    apollo-namespace: application
  httpclients:
    client1:
      # 后端列表(方式一, 优先级高)
      hosts: http://127.0.0.1:8083,http://127.0.0.1:8084
      # 后端列表(方式二, 优先级低), 在properties中: slate.httpclients.client1.host-list[0]=http://127.0.0.1:8083
      host-list:
        - http://127.0.0.1:8083
        - http://127.0.0.1:8084
      # 健康主动探测间隔, 单位ms
      initiative-inspect-interval: 5000
      # true: 当所有后端都被阻断时不发送请求(抛异常), false: 当所有后端都被阻断时随机发送请求
      return-null-if-all-blocked: false
      # 启用HTTP Get方式进行主动健康探测, URL为http://127.0.0.1:8083/health和http://127.0.0.1:8084/health, (设置+telnet+改回TELNET方式)
      http-get-inspector-url-suffix: /health
      # 健康被动探测阻断时长, 单位ms
      passive-block-duration: 30000
      # mediaType
      media-type: application/json;charset=utf-8
      # 编码
      encode: utf-8
      # Http请求头, 键值对格式参考: https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md
      headers: User-Agent=SlateHttpClient,Referer=http://github.com
      # 阻断后的恢复期系数, 恢复期时长 = blockDuration * recoveryCoefficient, 设置1则无恢复期
      recovery-coefficient: 10
      # 最大闲置连接数. 客户端会保持与服务端的连接, 保持数量由此设置决定, 直到闲置超过5分钟. 默认16
      max-idle-connections: 16
      # 异步方式最大线程数, 配置仅在异步方式有效, 同步无限制
      max-threads: 256
      # 异步方式每个后端最大线程数, 配置仅在异步方式有效, 同步无限制
      max-threads-per-host: 256
      # 连接超时时间, 单位ms
      connect-timeout: 3000
      # 写超时时间, 单位ms
      write-timeout: 10000
      # 读超时时间, 单位ms
      read-timeout: 10000
      # 数据最大读取长度, 单位字节
      max-read-length: 10485760
      # 当后端HTTP返回码为400或500时阻断后端
      http-code-need-block: 400,500
      # true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false
      verbose-log: false
      # true启用TxTimer对请求耗时的统计(目前只支持同步方式), 详见https://github.com/shepherdviolet/slate/blob/master/docs/txtimer/guide.md
      tx-timer-enabled: false
      # true: 开启简易的请求日志追踪(请求日志追加4位数追踪号), 默认false
      request-trace-enabled: false
      # 添加自定义的根证书, 用于验证自签名的服务器(设置一个, 优先级高). 如果设置为"UNSAFE-TRUST-ALL-ISSUERS"则不校验服务端证书链, 信任一切服务端证书, 不安全!!!
      custom-server-issuer-encoded: '自签名的服务端根证书X509-Base64字符串'
      # 添加自定义的根证书, 用于验证自签名的服务器(设置多个, 优先级低). 在properties中: slate.httpclients.custom-server-issuers-encoded[0]=...
      custom-server-issuers-encoded: 
        - '自签名的服务端根证书X509-Base64字符串(1)'
        - '自签名的服务端根证书X509-Base64字符串(2)'
      # 使用指定的域名验证服务端证书的DN(方式一, 优先级高). 如果设置为"UNSAFE-TRUST-ALL-DN"则不校验DN, 所有合法证书都通过, 不安全!!!
      verify-server-dn-by-customdn: 'CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\, Ltd,OU=service operation department,L=beijing,ST=beijing,C=CN'
      # 使用指定的域名验证服务端证书的CN(方式二, 优先级低). 如果设置为"UNSAFE-TRUST-ALL-CN"则不校验CN, 所有合法证书都通过, 不安全!!!
      verify-server-cn-by-custom-hostname: 'www.baidu.com'
```

<br>
<br>
<br>

# 配置数据转换器`dataConverter`(可选)

* 如果你希望直接发送一个Bean对象(支持Map), 或接收一个Bean对象(支持Map)作为响应, 请配置`dataConverter`
* SpringBoot自动配置模式下, 如果你的工程依赖了`com.google.code.gson:gson`会自动配置一个`GsonDataConverter`
* 如果你的工程未依赖`com.google.code.gson:gson`, 或希望自行实现转换逻辑, 请自行配置一个:

```text
    //必须叫这个名字
    @Bean(HttpClients.DATA_CONVERTER_NAME)
    public DataConverter httpClientsDataConverter(){
        return new YourDataConverter();
    }
```

<br>
<br>
<br>

# 手动配置

* 除了用YML配置, 也可以进行手动配置, 参考https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/config-annotation.md

<br>
<br>
<br>

# 获得客户端实例

* 获得所有客户端(包括运行时动态添加的)

```text
    private SimpleOkHttpClient client1;
    
    /**
     * 使用构造注入, 保证在操作时HttpClients已经注入
     */
    @Autowired
    public Constructor(HttpClients httpClients) {
        this.client1 = httpClients.get("cliente1");
    }
}
```

* 注解注入(无法获得运行时动态添加的)
* 注意, 无法用@Autowired注解获得客户端

```text
    @HttpClient("client1")
    private SimpleOkHttpClient client1;
    
    @HttpClient("client2")
    public void setClient2(SimpleOkHttpClient client2) {
        // ......
    }
```

<br>
<br>
<br>

# 运行时调整配置

* 部分配置未支持用YML配置, 必须通过手动配置(例如: 设置CookieJar, 设置Proxy等)

```text
客户端所有配置均可以在运行时调整, set系列方法均为线程安全. 但是, 配置的调整是异步生效的, 即不会在执行set方法的同时生效. 
例如, 在发送请求前修改服务端地址(hosts), 请求仍然会被发往老的服务端地址. 
正确的方式是: 开发一个控制台, 在控制台中调整参数时, 调用客户端的set系列方法调整配置; 使用Apollo配置中心, 监听到配置发生变化时, 
调用客户端的set系列方法调整配置. 
错误的方式是: 在每次发送请求前调用set系列方法调整配置. 
```

```text
@Component
public class MyHttpTransport implements InitializingBean {

    private SimpleOkHttpClient client1;
    
    /**
     * 使用构造注入, 保证simpleOkHttpClient优先注入, 使用时不会为null
     */
    @Autowired
    public HttpClientConfigChangeListener(HttpClients httpClients) {
        this.client1 = httpClients.get("client1");
    }

    /**
     * 示例1:
     * 在管理平台设置新参数时, 调用SimpleOkHttpClient的set系列方法调整客户端的配置
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    public void setHosts(......) {
        client1.setHosts(......);
    }
    
    /**
     * 示例2:
     * 可以在afterPropertiesSet方法中, 给客户端添加代理/SSL连接工厂等高级配置
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        client1
                .setProxy(......)
                .setSSLSocketFactory(......);
    }

}
```

<br>
<br>
<br>

# 使用启动参数调整配置

* HttpClients支持用启动参数调整配置, 但必须重启应用才会生效
* 启动参数格式: -Dslate.httpclients.`客户端标识`.`配置名`=`配置值`

* 例如: 修改client2的hosts为http://127.0.0.1:8083,http://127.0.0.1:8084

> -Dslate.httpclients.client2.hosts=http://127.0.0.1:8083,http://127.0.0.1:8084<br>

* 例如: 给client1添加两个Http请求头 [键值对格式](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)

> -Dslate.httpclients.client1.headers=User-Agent=SlateHttpClient,Referer=http://github.com<br>

* 其他说明

> 如果`客户端标识`是新增的, 应用会创建一个新的HttpClient实例<br>

<br>
<br>
<br>

# 使用Apollo配置中心实时调整配置

* 启用Apollo

```text
@EnableApolloConfig
@SpringBootApplication
public class BootApplication {
}
```

* 在application.yml/application-profile.yml中增加配置

```yaml
slate:
  httpclient:
    enabled: true
    apollo-support: true
```

> 该配置为true时, 程序会监听Apollo中应用的`私有配置(namespace=application)`, 根据其中的配置调整客户端参数(详见源码`HttpClientsApolloConfig`)

* 进入Apollo配置中心控制台, 新增或修改应用的`私有配置(namespace=application)`, 应用端的HttpClient配置就会实时调整
* 注意: 只能配置在应用的`私有配置(namespace=application)`中, 配在`公共配置或非默认配置(namespace!=application)`中无效

> Key格式: slate.httpclients.`客户端标识`.`配置名`

* 例如: 修改client2的hosts为http://127.0.0.1:8083,http://127.0.0.1:8084

> Key: `slate.httpclients.client2.hosts`<br>
> Value: `http://127.0.0.1:8083,http://127.0.0.1:8084`<br>

* 例如: 给client1添加两个Http请求头 [键值对格式](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)

> Key: `slate.httpclients.client1.headers`<br>
> Value: `User-Agent=SlateHttpClient,Referer=http://github.com`<br>

* 其他说明

> 如果`客户端标识`是新增的, 应用端会实时创建一个新的HttpClient实例<br>
> 在日志中搜索`HttpClients`关键字可以观察到配置实时调整的情况<br>

* (可选) 如果想要配置在`公共配置或非默认配置(namespace!=application)`中, 请在application.yml/application-profile.yml中增加配置
* 多个namespace用逗号分隔, 默认为application

```yaml
slate:
  httpclient:
    enabled: true
    apollo-support: true
    apollo-namespace: application,yournamespace
```

<br>
<br>
<br>

# 依赖

* gradle

```text
//version替换为具体版本, 另外需要依赖spring库
dependencies {
    compile 'com.github.shepherdviolet:slate-http-client:?'
}
```

* maven

```maven
    <!--version替换为具体版本, 另外需要依赖spring库-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-http-client</artifactId>
        <version>?</version>
    </dependency>
```
