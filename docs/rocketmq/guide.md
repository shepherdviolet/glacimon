# RocketMQ助手

* [Source Code](https://github.com/shepherdviolet/slate/tree/master/slate-helper/src/main/java/sviolet/slate/common/helper/rocketmq)

# 注解方式声明消费者(Consumer)

```text
注意!!! 作为消费者(Consumer), 必须保证事务的幂等性, 因为任何一条消息都有可能发生重复消费(即消费端收到多次). 另外, 在顺序消费模式中
退回消息(RECONSUME)也要小心, 消息可能会无限重复. 顺序消费模式性能差, 慎用. 
```

<br>

## 配置

### Spring Boot (推荐)

* 添加注解`@EnableRocketMqHelper`

```text
@Configuration
@EnableRocketMqHelper
public class AppConfiguration {
}
```

* 配置NameServer地址(yaml/properties/启动参数)

```text
slate.common.rocketmq.namesrv=host1:9876;host2:9876
```

<br>

### Spring

* 添加配置类

```text
@Configuration
@EnableMemberProcessor({
        RmqConcurrentConsumerMemProc.class,
        RmqOrderedConsumerMemProc.class,
        RmqCustomConsumerMemProc.class,
})
public class RocketMqHelperConfiguration {
    @Bean(RmqConsumerManager.BEAN_NAME)
    public RmqConsumerManager consumerManager(){
        return new RmqConsumerManagerImpl(null);
    }

    @Bean(RmqConsumerMethodInvokerFactory.BEAN_NAME)
    public RmqConsumerMethodInvokerFactory consumerMethodInvokerFactory(){
        return new RmqConsumerMethodInvokerFactoryImpl();
    }
}
```

* 配置NameServer地址(properties/启动参数)

```text
slate.common.rocketmq.namesrv=host1:9876;host2:9876
```

<br>

## 使用

### 普通并发消费

* 简单示例

```text
    //consumerGroup是消费组名, 不要重复
    //subExpression是订阅的TAG表达式, 默认全订阅
    @RocketMqConcurrentConsumer(
            consumerGroup = "consumer-group-name",
            topic = "topic-name",
            subExpression = "tag-a || tag-b || tag-c"
    )
    public boolean test(MessageExt message){
        //入参默认支持MessageExt/Message/byte[]/String/Map, 需要支持更多类型请自定义RmqConsumerMethodInvokerFactory, 见RmqHelperConfig
        logger.info("Received: {}", message);
        //返回类型不限定, 可以为void, 但是返回false会退回消息(RECONSUME, 该消息会重新被消费)
        //该方法抛出异常时, 默认不会退回消息(RECONSUME), 设置reconsumeWhenException=true后, 当抛出异常时会退回消息
        return true;
    }
```

* 参数全解

```text
    //consumerGroup是消费组名, 不要重复
    //subExpression是订阅的TAG表达式, 默认全订阅
    @RocketMqConcurrentConsumer(
            //消费组名, 不要重复
            consumerGroup = "consumer-group-name",
            //Topic
            topic = "topic-name",
            //subExpression/sqlExpression二选一, 订阅的TAG表达式, 默认全订阅
            subExpression = "tag-a || tag-b || tag-c",
            //subExpression/sqlExpression二选一, SQL92过滤条件, 需Broker配置
            sqlExpression = "a between 0 and 3",
            //选择消息从队列头还是队列尾开始消费
            consumeFromWhere = ConsumeWay.CONSUME_FROM_FIRST_OFFSET,
            //是否以广播方式订阅, 默认false
            isBroadcast = true,
            //最小线程数
            threadMin = 4,
            //最大线程数
            threadMax = 8,
            //方法抛出异常时退回消息(RECONSUME, 该消息会重新被消费), 默认false
            reconsumeWhenException = true,
            //字符集
            charset = "UTF-8",
            //指定nameServer地址, 不设置使用参数slate.common.rocketmq.namesrv的值, 若参数不存在则用EnableRocketMqHelper的defaultNameServer
            nameServer = "192.168.1.1:9876"
    )
```

<br>

### 顺序消费

* 顺序消费慎用, 全局单线程, 退回消息处理不当可能会无限循环
* RocketMQ只保证同一个队列中的消息有序(一个Topic会有多个队列), 生产端需要将消息放进同一个队列, 消费端也要从同一个队列取
* 例如生产端将所有tag=0的消息放入0号队列, 消费端订阅tag=0的消息, 这样获取到的消息保证有序
* 简单示例

```text
    @RocketMqOrderedConsumer(
            consumerGroup = "consumer-group-name",
            topic = "topic-name",
            subExpression = "0"
    )
    public void test(String message){
        //入参默认支持MessageExt/Message/byte[]/String/Map, 需要支持更多类型请自定义RmqConsumerMethodInvokerFactory, 见RmqHelperConfig
        logger.info("Received: {}", message);
        //返回类型不限定, 可以为void, 但是返回false会退回消息(RECONSUME, 该消息会重新被消费)
        //该方法抛出异常时, 默认不会退回消息(RECONSUME), 设置reconsumeWhenException=true后, 当抛出异常时会退回消息
    }
```

<br>

### 自定义消费者

* 如果注解的配置无法满足需求, 但又想将消费者绑定到一个方法上
* 声明自定义的消费者

```text
    @Bean(name = "customConsumer", destroyMethod = "shutdown")
    public DefaultMQPushConsumer customConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer-group-name");
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe("topic-name", "*");
        //注意!!! consumer不要启动, 也不要绑定listener, 因为这些操作会在绑定方法时自动进行
        return consumer;
    }
```

* 绑定到方法上

```text
    @RocketMqCustomConsumer(
            consumerBeanName = "customConsumer",
            isOrdered = false
    )
    public void test(Map map) {
        //入参默认支持MessageExt/Message/byte[]/String/Map, 需要支持更多类型请自定义RmqConsumerMethodInvokerFactory, 见RmqHelperConfig
        logger.info("Received: {}", map);
        //返回类型不限定, 可以为void, 但是返回false会退回消息(RECONSUME, 该消息会重新被消费)
        //该方法抛出异常时, 默认不会退回消息(RECONSUME), 设置reconsumeWhenException=true后, 当抛出异常时会退回消息
    }
```

<br>

### 扩展方法参数类型/方法调用拦截

* 支持自定义RmqConsumerMethodInvokerFactory, 支持更多的绑定方法参数类型, 或者实现方法调用拦截
* 详见`RmqHelperConfig`和`RmqConsumerMethodInvokerFactoryImpl`

<br>

## 依赖

```gradle
dependencies {
    compile 'com.github.shepherdviolet:slate-helper:?'
    compile 'org.apache.rocketmq:rocketmq-client:4.4.0'
}

```

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-helper</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>org.apache.rocketmq</groupId>
        <artifactId>rocketmq-client</artifactId>
        <version>4.4.0</version>
    </dependency>
```
