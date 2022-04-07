# HttpClient配置方法(Spring 注解手动配置)

* `Maven/Gradle依赖配置`在本文最后

<br>
<br>
<br>

# 简化版配置(推荐)

* SimpleOkHttpClient继承了MultiHostOkHttpClient, 同时封装了LoadBalancedHostManager和LoadBalancedInspectManager, 简化了配置, 免去了配置三个Bean的麻烦
* 配置被简化, 如需高度定制, 请使用LoadBalancedHostManager + LoadBalancedInspectManager + MultiHostOkHttpClient
* 内置的LoadBalancedInspectManager采用TELNET方式探测后端, 可以改为HttpGet方式
* 屏蔽了setHostManager()方法, 调用会抛出异常
* 实现了DisposableBean, 在Spring容器中会自动销毁
* 若SimpleOkHttpClient在Spring中注册为Bean, 主动探测器会在Spring启动后自动开始. 否则需要手动调用SimpleOkHttpClient.start()方法开始主动探测

```text
@Configuration
public class MyConfiguration {

    @Value("${http.client.hosts}")
    private String hosts;
    
    /**
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    @Bean
    public SimpleOkHttpClient simpleOkHttpClient() {
        return (SimpleOkHttpClient) new SimpleOkHttpClient()
                .setHosts(hosts)//配置后端列表
                .setInitiativeInspectInterval(5000L)//健康主动探测间隔为5000ms
                .setPassiveBlockDuration(30000L)//健康被动探测阻断时长为30000ms, 被动阻断时间建议与所有超时时间加起来接近
                .setConnectTimeout(3000L)//连接超时时间, 单位ms
                .setWriteTimeout(10000L)//写超时时间, 单位ms
                .setReadTimeout(10000L)//读超时时间, 单位ms
                .setMaxReadLength(10L * 1024L * 1024L)//数据最大读取长度, 单位字节
                .setDataConverter(new GsonDataConverter())//设置数据转换器, 详见'关于数据转换器`dataConverter`(可选)'章节
                .setVerboseLog(true);//true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false
                //.setVerboseLogConfig(0x00000110)//微调输出的日志内容(详见源码)
                //.setHttpGetInspector("/health")//启用HTTP Get方式进行主动健康探测, URL为http://127.0.0.1:8083/health和http://127.0.0.1:8084/health, (设置+telnet+改回TELNET方式)
    }

}
```

<br>
<br>
<br>

# 标准配置(不推荐)

* `LoadBalancedInspectManager需要配置@Bean(destroyMethod = "close")`

```text
@Configuration
public class MyConfiguration {

    /**
     * 后端管理器
     * 配置管理后端地址和状态
     */
    @Bean
    public LoadBalancedHostManager loadBalancedHostManager() {
        return new LoadBalancedHostManager()
                .setHostArray(new String[]{
                    "http://127.0.0.1:8081",
                    "http://127.0.0.1:8082"
                });
    }
    
    /**
     * 主动探测管理器
     * 定时探测后端状态(默认Telnet方式)
     * 注意, 必须配置destroyMethod = "close"
     * 更多配置请看LoadBalancedInspectManager类的方法注释
     */
    @Bean(destroyMethod = "close")
    public LoadBalancedInspectManager loadBalancedInspectManager(LoadBalancedHostManager loadBalancedHostManager) {
        return new LoadBalancedInspectManager()
                //.setInspector(new HttpGetLoadBalanceInspector("/health", 1000L))//启用HTTP Get方式进行主动健康探测, URL为http://127.0.0.1:8083/health和http://127.0.0.1:8084/health, 超时时间建议为主动探测间隔的四分之一
                .setHostManager(loadBalancedHostManager)//持有LoadBalancedHostManager
                .setInspectInterval(5000L);//主动探测间隔    
    }
    
    /**
     * HTTP请求客户端
     * 调用该实例发送请求
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */ 
    @Bean
    public MultiHostOkHttpClient multiHostOkHttpClient(LoadBalancedHostManager loadBalancedHostManager) {
        return new MultiHostOkHttpClient()
                .setHostManager(loadBalancedHostManager)//持有LoadBalancedHostManager
                .setPassiveBlockDuration(30000L)//健康被动探测阻断时长为30000ms, 被动阻断时间建议与所有超时时间加起来接近
                .setConnectTimeout(3000L)//连接超时时间, 单位ms
                .setWriteTimeout(10000L)//写超时时间, 单位ms
                .setReadTimeout(10000L)//读超时时间, 单位ms
                .setMaxReadLength(10L * 1024L * 1024L)//数据最大读取长度, 单位字节
                .setDataConverter(new GsonDataConverter())//设置数据转换器, 详见'关于数据转换器`dataConverter`(可选)'章节
                .setVerboseLog(true);//为true时会输出更多日志
    }

}
```

<br>
<br>
<br>

# 关于数据转换器`dataConverter`(可选)

* 如果你希望直接发送一个Bean对象(支持Map), 或接收一个Bean对象(支持Map)作为响应, 请配置`dataConverter`
* 使用默认数据转换器`GsonDataConverter` (请添加依赖`com.google.code.gson:gson`)

```text
httpClient.setDataConverter(new GsonDataConverter())//设置数据转换器
```

* 也可以自行实现数据转换器

<br>
<br>
<br>

# 运行时调整配置

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

    private SimpleOkHttpClient simpleOkHttpClient;
    
    /**
     * 使用构造注入, 保证simpleOkHttpClient优先注入, 使用时不会为null
     */
    @Autowired
    public HttpClientConfigChangeListener(SimpleOkHttpClient simpleOkHttpClient) {
        this.simpleOkHttpClient = simpleOkHttpClient;
    }

    /**
     * 示例1:
     * 在管理平台设置新参数时, 调用SimpleOkHttpClient的set系列方法调整客户端的配置
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    public void setHosts(......) {
        simpleOkHttpClient.setHosts(......);
    }
    
    /**
     * 示例2:
     * 可以在afterPropertiesSet方法中, 给客户端添加代理/SSL连接工厂等高级配置
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        simpleOkHttpClient
                .setProxy(......)
                .setSSLSocketFactory(......);
    }

}
```

<br>
<br>
<br>

# 使用Apollo配置中心实时调整配置

```text
虽然Apollo配置中心新版本能实时更新XML属性和@Value注解中的${...}参数, 但是使用@Bean方式声明的类, 只在实例化时进行参数赋值. 
因此必须通过其他方式实现, 方法有两种. 
```

* 方法1: @Value注解在方法上, 每次参数变化都会调用该方法, 这样可以实时改变参数

```text
@Component
public class HttpClientConfigChangeListener {

    private SimpleOkHttpClient simpleOkHttpClient;
    
    /**
     * 使用构造注入, 保证在setter操作时simpleOkHttpClient已经注入
     */
    @Autowired
    public HttpClientConfigChangeListener(SimpleOkHttpClient simpleOkHttpClient) {
        this.simpleOkHttpClient = simpleOkHttpClient;
    }

    /**
     * 动态调整hosts配置
     * SimpleOkHttpClient所有的配置均可以在运行时调整, 所有Set方法均为线程安全, 
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    @Value("${http.client.hosts:}")
    public void setHosts(String hosts) {
        if (!CheckUtils.isEmptyOrBlank(hosts)) {
            simpleOkHttpClient.setHosts(hosts);
        }
    }

}
```

* 方法2: Apollo配置变化监听器

```text
@Component
public class ApolloConfigChangeService {

    @ApolloConfig
    private Config apolloConfig;

    private SimpleOkHttpClient simpleOkHttpClient;
    
    /**
     * 使用构造注入, 保证在setter操作时simpleOkHttpClient已经注入
     */
    @Autowired
    public ApolloConfigChangeService(SimpleOkHttpClient simpleOkHttpClient) {
        this.simpleOkHttpClient = simpleOkHttpClient;
    }

    /**
     * 动态调整配置
     * SimpleOkHttpClient所有的配置均可以在运行时调整, 所有Set方法均为线程安全, 
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    @ApolloConfigChangeListener
    private void onHttpClientChanged(ConfigChangeEvent configChangeEvent){
        if (configChangeEvent.isChanged("http.client.hosts")){
            simpleOkHttpClient.setHosts(apolloConfig.getProperty("http.client.hosts", ""));
        }
        if (configChangeEvent.isChanged("http.client.verboseLog")){
            simpleOkHttpClient.setVerboseLog(apolloConfig.getBooleanProperty("http.client.verboseLog", false));
        }
    }

}
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
