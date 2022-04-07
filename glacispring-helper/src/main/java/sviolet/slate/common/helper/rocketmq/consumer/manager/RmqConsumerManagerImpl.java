/*
 * Copyright (C) 2015-2019 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.helper.rocketmq.consumer.manager;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import sviolet.slate.common.helper.rocketmq.consumer.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RocketMQ Consumer管理器:
 * 创建Consumer, 与指定方法绑定, 实现消息处理和方法调用逻辑
 *
 * @author S.Violet
 */
public class RmqConsumerManagerImpl implements RmqConsumerManager, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RmqConsumerManagerImpl.class);

    /**
     * NameServer地址. 优先级中等.
     * 优先级:
     * 1.@RocketMqConcurrentConsumer(nameServer = "...") / @RocketMqOrderedConsumer(nameServer = "...")
     * 2.启动参数slate.common.rocketmq.namesrv
     * 3.配置文件slate.common.rocketmq.namesrv
     * 4.@EnableRocketMq(defaultNameServer = "...")
     */
    @Value("${slate.common.rocketmq.namesrv:}")
    private String nameServerFromProperties;

    /**
     * true: 绑定的方法抛出异常时在日志中打印消息信息
     */
    @Value("${slate.common.rocketmq.print-message-when-exception:true}")
    private boolean printMessageWhenException;

    /**
     * true: 消息被打回重新消费(RECONSUME)时在日志中打印消息信息
     */
    @Value("${slate.common.rocketmq.print-message-when-reconsume:true}")
    private boolean printMessageWhenReconsume;

    /**
     * 默认字符集
     */
    @Value("${slate.common.rocketmq.default-charset:UTF-8}")
    private String defaultCharset;

    /**
     * 方法调用者工厂
     */
    @Autowired
    private RmqConsumerMethodInvokerFactory invokerFactory;

    private ApplicationContext applicationContext;

    private final ConcurrentHashMap<String, MQPushConsumer> consumers = new ConcurrentHashMap<>();
    private final Map<String, Object> enableAnnotationAttributes;

    public RmqConsumerManagerImpl(Map<String, Object> enableAnnotationAttributes) {
        if (enableAnnotationAttributes == null) {
            enableAnnotationAttributes = new HashMap<>();
        }
        this.enableAnnotationAttributes = enableAnnotationAttributes;
    }

    /**
     * 与RocketMqCustomConsumer注解的方法绑定(自定义消费者)
     */
    @Override
    public void registerMethod(Object bean, String beanName, Method method, RocketMqCustomConsumer annotation) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Register RocketMQ custom consumer to " + bean.getClass().getName() + "#" + method.getName() +
                    ", annotation:" + annotation);
        }

        //Invoker
        RmqConsumerMethodInvoker invoker = createInvoker(bean, method, getCharset(annotation.charset()));

        //获取Consumer
        DefaultMQPushConsumer consumer = applicationContext.getBean(annotation.consumerBeanName(), DefaultMQPushConsumer.class);
        //consumer必须是未启动的
        if (consumer.getDefaultMQPushConsumerImpl().getServiceState() != ServiceState.CREATE_JUST) {
            throw new RocketMqHelperException("DO NOT start the DefaultMQPushConsumer manually if you want to register it on the method annotated by @RocketMqCustomConsumer");
        }
        //是否顺序消费
        if (annotation.isOrdered()) {
            consumer.registerMessageListener(new OrderedListener(invoker, annotation.reconsumeWhenException()));
        } else {
            consumer.registerMessageListener(new ConcurrentListener(invoker, annotation.reconsumeWhenException()));
        }
        consumer.start();

        //缓存
        cacheConsumer(consumer.getConsumerGroup(), consumer);

        if (logger.isInfoEnabled()) {
            logger.info("New RocketMQ Consumer: " + consumer);
        }
    }

    /**
     * 与RocketMqConcurrentConsumer注解的方法绑定(普通并发消费者)
     */
    @Override
    public void registerMethod(Object bean, String beanName, Method method, RocketMqConcurrentConsumer annotation) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Register RocketMQ concurrent consumer to " + bean.getClass().getName() + "#" + method.getName() +
                    ", annotation:" + annotation);
        }

        //Invoker
        RmqConsumerMethodInvoker invoker = createInvoker(bean, method, getCharset(annotation.charset()));

        //创建Consumer
        String consumerGroup = getConsumerGroup(bean, method, annotation.consumerGroup());
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(getNameServer(annotation.nameServer()));
        //sqlExpression优先
        if (!"".equals(annotation.sqlExpression())) {
            consumer.subscribe(annotation.topic(), MessageSelector.bySql(annotation.sqlExpression()));
        } else {
            consumer.subscribe(annotation.topic(), annotation.subExpression());
        }
        if (annotation.isBroadcast()) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        }
        consumer.setConsumeFromWhere(parseConsumeFromWhere(annotation.consumeFromWhere()));
        consumer.setConsumeThreadMin(annotation.threadMin());
        consumer.setConsumeThreadMax(annotation.threadMax());
        consumer.registerMessageListener(new ConcurrentListener(invoker, annotation.reconsumeWhenException()));
        consumer.start();

        //缓存
        cacheConsumer(consumerGroup, consumer);

        if (logger.isInfoEnabled()) {
            logger.info("New RocketMQ Consumer: " + consumer);
        }
    }

    /**
     * 与RocketMqOrderedConsumer注解的方法绑定(顺序消费者)
     */
    @Override
    public void registerMethod(Object bean, String beanName, Method method, RocketMqOrderedConsumer annotation) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Register RocketMQ ordered consumer to " + bean.getClass().getName() + "#" + method.getName() +
                    ", annotation:" + annotation);
        }

        //Invoker
        RmqConsumerMethodInvoker invoker = createInvoker(bean, method, getCharset(annotation.charset()));

        //创建Consumer
        String consumerGroup = getConsumerGroup(bean, method, annotation.consumerGroup());
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(getNameServer(annotation.nameServer()));
        consumer.subscribe(annotation.topic(), annotation.subExpression());
        consumer.setConsumeFromWhere(parseConsumeFromWhere(annotation.consumeFromWhere()));
        consumer.registerMessageListener(new OrderedListener(invoker, annotation.reconsumeWhenException()));
        consumer.start();

        //缓存
        cacheConsumer(consumerGroup, consumer);

        if (logger.isInfoEnabled()) {
            logger.info("New RocketMQ Consumer: " + consumer);
        }
    }

    /**
     * 停止所有的消费者
     */
    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, MQPushConsumer> entry : consumers.entrySet()) {
            try {
                entry.getValue().shutdown();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 处理一条消息.
     * APM埋点时, 可以拦截这个方法, 捕获消息消费前后的信息.
     */
    protected boolean consumeOneMessage(RmqConsumerMethodInvoker invoker, MessageExt message, boolean reconsumeWhenException){
        try {
            Object result = invoker.invoke(message);
            //返回值为false时, 打回消息重新消费(RECONSUME)
            if (Boolean.FALSE.equals(result)) {
                return false;
            }
        } catch (Throwable t) {
            if (printMessageWhenException) {
                logger.error("Uncaught exception while consuming message:" + message, t);
            } else {
                logger.error("Uncaught exception while consuming message", t);
            }
            //抛出异常时, 打回消息重新消费(RECONSUME), 默认reconsumeWhenException=false
            if (reconsumeWhenException) {
                return false;
            }
        }
        return true;
    }

    /**
     * 并发消费
     */
    private class ConcurrentListener implements MessageListenerConcurrently {

        private RmqConsumerMethodInvoker invoker;
        private boolean reconsumeWhenException;

        private ConcurrentListener(RmqConsumerMethodInvoker invoker, boolean reconsumeWhenException) {
            this.invoker = invoker;
            this.reconsumeWhenException = reconsumeWhenException;
        }

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
            if (logger.isDebugEnabled()) {
                logger.debug("Consume Concurrent Messages: " + messages);
            }
            context.setAckIndex(-1);
            for (MessageExt message : messages) {
                if (consumeOneMessage(invoker, message, reconsumeWhenException)){
                    context.setAckIndex(context.getAckIndex() + 1);
                } else {
                    break;
                }
            }
            if (logger.isInfoEnabled() && printMessageWhenReconsume) {
                for (int i = context.getAckIndex() + 1 ; i < messages.size() ; i++) {
                    logger.info("RECONSUME_LATER: " + messages.get(i));
                }
            }
            //使用ackIndex机制退回部分失败消息
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

    }

    /**
     * 顺序消费
     */
    private class OrderedListener implements MessageListenerOrderly {

        private RmqConsumerMethodInvoker invoker;
        private boolean reconsumeWhenException;

        private OrderedListener(RmqConsumerMethodInvoker invoker, boolean reconsumeWhenException) {
            this.invoker = invoker;
            this.reconsumeWhenException = reconsumeWhenException;
        }

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messages, ConsumeOrderlyContext context) {
            if (logger.isDebugEnabled()) {
                logger.debug("Consume Ordered Messages: " + messages);
            }
            for (MessageExt message : messages) {
                if (!consumeOneMessage(invoker, message, reconsumeWhenException)){
                    //有一条消息处理失败时, 会退回全部消息
                    if (logger.isInfoEnabled() && printMessageWhenReconsume) {
                        logger.info("SUSPEND_CURRENT_QUEUE_A_MOMENT: " + messages);
                    }
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

    private void cacheConsumer(String consumerGroup, DefaultMQPushConsumer consumer) throws Exception {
        MQPushConsumer previous = consumers.putIfAbsent(consumerGroup, consumer);
        if (previous != null) {
            throw new RocketMqHelperException("The consumer group[" + consumerGroup + "] has been created before, specify another name please.\n" +
                    "See http://rocketmq.apache.org/docs/faq/ for further details.");
        }
    }

    protected String getCharset(String charsetFromAnnotation) {
        if ("".equals(charsetFromAnnotation)) {
            return defaultCharset;
        }
        return charsetFromAnnotation;
    }

    protected String getConsumerGroup(Object bean, Method method, String consumerGroupFromAnnotation) {
        return consumerGroupFromAnnotation;
    }

    protected RmqConsumerMethodInvoker createInvoker(Object bean, Method method, String charset) throws Exception {
        //检查方法入参
        Class[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            throw new RocketMqHelperException("The method " + bean.getClass().getName() + "#" + method.getName() +
                    " can only have one parameter, because it's a RocketMQ consumer method");
        }
        //创建Invoker
        RmqConsumerMethodInvoker invoker = invokerFactory.newInvoker(paramTypes[0], bean, method, charset);
        if (invoker == null) {
            throw new RocketMqHelperException("Unable to find an invoker suitable for the RocketMQ consumer method " + bean.getClass().getName() +
                    "#" + method.getName() + " (Invoker for parameter type " + paramTypes[0].getName() + ")");
        }
        return invoker;
    }

    protected String getNameServer(String nameServerFromAnnotation){
        //注解中的优先
        if (!"".equals(nameServerFromAnnotation)) {
            return nameServerFromAnnotation;
        }
        //配置其次
        if (!"".equals(nameServerFromProperties)) {
            return nameServerFromProperties;
        }
        //默认值最后
        Object defaultNameServer = enableAnnotationAttributes.get("defaultNameServer");
        if ((defaultNameServer instanceof String) && !"".equals(defaultNameServer)) {
            return (String) defaultNameServer;
        }
        return "localhost:9876";
    }

    protected ConsumeFromWhere parseConsumeFromWhere(ConsumeWay consumeWay) throws Exception {
        switch (consumeWay) {
            case CONSUME_FROM_LAST_OFFSET : return ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
            case CONSUME_FROM_FIRST_OFFSET : return ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET;
            case CONSUME_FROM_TIMESTAMP : return ConsumeFromWhere.CONSUME_FROM_TIMESTAMP;
            default: throw new RocketMqHelperException("Illegal ConsumeWay " + consumeWay);
        }
    }

}
