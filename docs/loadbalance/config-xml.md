# HttpClient配置方法(Spring XML手动配置)

* `Maven/Gradle依赖配置`在本文最后

<br>
<br>
<br>

# 配置客户端

* 若HttpClient在Spring中注册为Bean, 服务停止时会自动销毁客户端, 否则需要手动调用GlaciHttpClient.close()方法销毁实例.
* 若HttpClient在Spring中注册为Bean, 主动探测器会在Spring启动后自动开始. 否则需要手动调用GlaciHttpClient.start()方法开始主动探测.
* 默认采用TELNET方式探测后端, 可以改为HttpGet方式

```text
    <bean id="glaciHttpClient" class="com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient">
        <property name="hosts" value="http://127.0.0.1:8081,http://127.0.0.1:8082"/>
        <property name="connectTimeout" value="3000"/><!-- 连接超时时间, 单位ms -->
        <property name="writeTimeout" value="10000"/><!-- 写超时时间, 单位ms -->
        <property name="readTimeout" value="10000"/><!-- 读超时时间, 单位ms -->
        <property name="maxReadLength" value="10485760"/><!-- 数据最大读取长度, 单位字节 -->
        <property name="maxIdleConnections" value="0"/><!-- 最大闲置连接数. 若设置为0(默认), 每次重新解析域名+重新建立连接, 性能差, 但支持动态域名解析. 若设置为正整数(例如16), 会复用连接池中的连接, 性能强, 但若DNS域名解析记录更新, 可能会向原IP发送请求. -->
        <property name="initiativeInspectInterval" value="5000"/><!-- 健康主动探测间隔为5000ms; 若设置成<=0, 则暂停主动探测(暂停特性:2025.0.1+) -->
        <property name="passiveBlockDuration" value="30000"/><!-- 健康被动探测阻断时长为30000ms, 被动阻断时间建议与所有超时时间加起来接近 -->
        <property name="dataConverter" ref="dataConverter"/><!-- 设置数据转换器, 详见'关于数据转换器`dataConverter`(可选)'章节 -->
        <!--<property name="logPrintPayload" value="true"/> 日志打印请求/响应报文体: 支持"byte[]/Bean/表单"请求, 支持"byte[]/Bean"响应-->
        <!--<property name="httpGetInspector" ref="/health"/> 启用HTTP Get方式进行主动健康探测, URL为http://127.0.0.1:8083/health和http://127.0.0.1:8084/health, (设置+telnet+改回TELNET方式, 设置+disable+禁用主动探测)-->
    </bean>
```

<br>
<br>
<br>

# 关于数据转换器`dataConverter`(可选)

* 如果你希望直接发送一个Bean对象(支持Map), 或接收一个Bean对象(支持Map)作为响应, 请配置`dataConverter`
* 使用默认数据转换器`GsonDataConverter` (请添加依赖`com.google.code.gson:gson`)

```text
    <!-- HTTP请求客户端 -->
    <!-- 调用该实例发送请求 -->
    <bean id="glaciHttpClient" class="com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient">
        ......
        <property name="dataConverter" ref="dataConverter"/><!-- 设置数据转换器 -->
    </bean>
    
    <!-- 数据转换器 -->
    <bean id="dataConverter" class="com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GsonDataConverter">
    </bean>
```

* 也可以自行实现数据转换器

<br>
<br>
<br>

# 运行时调整配置

```text
客户端所有配置均可以在运行时调整, set系列方法均为线程安全. 但是, 配置的调整是异步生效的, 即不会在执行set方法后立即生效. 
例如, 在发送请求前修改服务端地址(hosts), 请求仍然会被发往老的服务端地址. 
正确的方式是: 开发一个控制台, 在控制台中调整参数时, 调用客户端的set系列方法调整配置; 使用Apollo配置中心, 监听到配置发生变化时, 
调用客户端的set系列方法调整配置. 
错误的方式是: 在每次发送请求前调用set系列方法调整配置. 
```

```text
public class MyHttpTransport implements InitializingBean {

    private GlaciHttpClient glaciHttpClient;
    
    public void setGlaciHttpClient(GlaciHttpClient glaciHttpClient) {
        this.glaciHttpClient = glaciHttpClient;
    }

    /**
     * 示例1:
     * 在管理平台设置新参数时, 调用GlaciHttpClient的set系列方法调整客户端的配置
     * 更多配置请看GlaciHttpClient类的方法注释
     */
    public void setHosts(......) {
        if (glaciHttpClient != null) {
            glaciHttpClient.setHosts(......);
        }
    }
    
    /**
     * 示例2:
     * 可以在afterPropertiesSet方法中, 给客户端添加代理/SSL连接工厂等高级配置
     * 更多配置请看GlaciHttpClient类的方法注释
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        glaciHttpClient
                .setProxy(......)
                .setSSLSocketFactory(......);
    }

}
```

<br>
<br>
<br>

# 使用Apollo配置中心实时调整配置

* Apollo配置中心新版本能实时更新XML属性和@Value注解中的${...}参数
* 因此只需要按如下方式使用, 即可根据Apollo实时调整配置

```text

    <!-- 启用apollo(任意一个XML中声明过一次即可) -->
    <apollo:config/>

    <!-- 使用${...}应用apollo参数 -->
    <bean id="glaciHttpClient" class="com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient">
        <property name="hosts" value="${http.client.hosts}"/>
        ......
    </bean>
```

<br>
<br>
<br>

# 依赖

* gradle

```text
//version替换为具体版本, 另外需要依赖spring库
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacispring-httpclient:?'
    
    // [可选] 启用GSON数据转换器(支持sendForBean)需要添加此依赖
    //compile 'com.google.code.gson:gson:2.10'
    // [可选] 自定义DNS解析(dns-description)需要添加此依赖
    //compile 'dnsjava:dnsjava:3.6.3'
    // [可选] 启用TxTimer统计请求耗时(tx-timer-enabled)需要添加此依赖
    compile 'com.github.shepherdviolet.glacimon:glacispring-txtimer:?'
}
```

* maven

```maven
    <!--version替换为具体版本, 另外需要依赖spring库-->
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-httpclient</artifactId>
        <version>?</version>
    </dependency>
```
