# HttpClient配置方法(Spring XML手动配置)

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
    <bean id="simpleOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.SimpleOkHttpClient">
        <property name="hosts" value="http://127.0.0.1:8081,http://127.0.0.1:8082"/>
        <property name="initiativeInspectInterval" value="5000"/><!-- 健康主动探测间隔为5000ms -->
        <property name="passiveBlockDuration" value="30000"/><!-- 健康被动探测阻断时长为30000ms, 被动阻断时间建议与所有超时时间加起来接近 -->
        <property name="connectTimeout" value="3000"/><!-- 连接超时时间, 单位ms -->
        <property name="writeTimeout" value="10000"/><!-- 写超时时间, 单位ms -->
        <property name="readTimeout" value="10000"/><!-- 读超时时间, 单位ms -->
        <property name="maxReadLength" value="10485760"/><!-- 数据最大读取长度, 单位字节 -->
        <property name="dataConverter" ref="dataConverter"/><!-- 设置数据转换器, 详见'关于数据转换器`dataConverter`(可选)'章节 -->
        <property name="verboseLog" value="true"/><!-- 为true时会输出更多日志 -->
        <!--<property name="verboseLogConfig" value="0x00000110"/> 微调输出的日志内容(详见源码)-->
        <!--<property name="httpGetInspector" ref="/health"/> 启用HTTP Get方式进行主动健康探测, URL为http://127.0.0.1:8083/health和http://127.0.0.1:8084/health, (设置+telnet+改回TELNET方式)-->
    </bean>
```

<br>
<br>
<br>

# 标准配置(不推荐)

* `LoadBalancedInspectManager需要配置destroy-method="close"`

```text

    <!-- 后端管理器 -->
    <!-- 配置管理后端地址和状态 -->
    <bean id="loadBalancedHostManager" class="sviolet.slate.common.x.net.loadbalance.LoadBalancedHostManager">
        <property name="hosts" value="http://127.0.0.1:8081,http://127.0.0.1:8082"/>
    </bean>
    
    <!-- 主动探测管理器 -->
    <!-- 定时探测后端状态(默认Telnet方式) -->
    <bean id="loadBalancedInspector" class="sviolet.slate.common.x.net.loadbalance.LoadBalancedInspectManager"
        destroy-method="close">
        <property name="hostManager" ref="loadBalancedHostManager"/><!-- 持有LoadBalancedHostManager -->
        <property name="inspectInterval" value="5000"/><!-- 健康主动探测间隔为5000ms -->
    </bean>
    
    <!-- HTTP请求客户端 -->
    <!-- 调用该实例发送请求 -->
    <bean id="multiHostOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.MultiHostOkHttpClient">
        <property name="hostManager" ref="loadBalancedHostManager"/><!-- 持有LoadBalancedHostManager -->
        <property name="passiveBlockDuration" value="30000"/><!-- 健康被动探测阻断时长为30000ms, 被动阻断时间建议与所有超时时间加起来接近 -->
        <property name="connectTimeout" value="3000"/><!-- 连接超时时间, 单位ms -->
        <property name="writeTimeout" value="10000"/><!-- 写超时时间, 单位ms -->
        <property name="readTimeout" value="10000"/><!-- 读超时时间, 单位ms -->
        <property name="maxReadLength" value="10485760"/><!-- 数据最大读取长度, 单位字节 -->
        <property name="dataConverter" ref="dataConverter"/><!-- 设置数据转换器, 详见'关于数据转换器`dataConverter`(可选)'章节 -->
        <property name="verboseLog" value="true"/><!-- true: INFO级别可打印更多的日志(请求报文/响应码等), 默认false -->
        <!--<property name="verboseLogConfig" value="0x00000110"/> 微调输出的日志内容(详见源码)-->
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
    <bean id="multiHostOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.MultiHostOkHttpClient">
        ......
        <property name="dataConverter" ref="dataConverter"/><!-- 设置数据转换器 -->
    </bean>
    
    <!-- 数据转换器 -->
    <bean id="dataConverter" class="sviolet.slate.common.x.net.loadbalance.classic.GsonDataConverter">
    </bean>
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
public class MyHttpTransport implements InitializingBean {

    private SimpleOkHttpClient simpleOkHttpClient;
    
    public void setSimpleOkHttpClient(SimpleOkHttpClient simpleOkHttpClient) {
        this.simpleOkHttpClient = simpleOkHttpClient;
    }

    /**
     * 示例1:
     * 在管理平台设置新参数时, 调用SimpleOkHttpClient的set系列方法调整客户端的配置
     * 更多配置请看SimpleOkHttpClient和MultiHostOkHttpClient类的方法注释
     */
    public void setHosts(......) {
        if (simpleOkHttpClient != null) {
            simpleOkHttpClient.setHosts(......);
        }
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

* Apollo配置中心新版本能实时更新XML属性和@Value注解中的${...}参数
* 因此只需要按如下方式使用, 即可根据Apollo实时调整配置

```text

    <!-- 启用apollo(任意一个XML中声明过一次即可) -->
    <apollo:config/>

    <!-- 使用${...}应用apollo参数 -->
    <bean id="simpleOkHttpClient" class="sviolet.slate.common.x.net.loadbalance.classic.SimpleOkHttpClient">
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
