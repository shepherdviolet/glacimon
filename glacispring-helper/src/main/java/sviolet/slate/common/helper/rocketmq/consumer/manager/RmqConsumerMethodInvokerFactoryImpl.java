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

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费方法调用器工厂:
 * 1.根据方法参数选择如何调用该方法, 如何预处理消息(类型转换)
 * 2.可以自定义实现该工厂, 扩展类型支持, 或者实现事件拦截
 *
 * @author S.Violet
 */
public class RmqConsumerMethodInvokerFactoryImpl implements RmqConsumerMethodInvokerFactory {

    private static final Logger logger = LoggerFactory.getLogger(RmqConsumerMethodInvokerFactoryImpl.class);

    /**
     * 当JSON解析失败时, 打回消息重新消费(RECONSUME), 默认false(因为JSON解析失败一般也没可能再消费成功了)
     */
    @Value("${slate.common.rocketmq.reconsume-when-json-parse-failed:false}")
    private boolean reconsumeWhenJsonParseFailed;

    @Override
    public RmqConsumerMethodInvoker newInvoker(Class<?> acceptType, Object bean, Method method, String charset) {
        if (Message.class.isAssignableFrom(acceptType)) {
            return new MessageInvoker(bean, method);
        }
        if (byte[].class.isAssignableFrom(acceptType)) {
            return new BytesInvoker(bean, method);
        }
        if (String.class.isAssignableFrom(acceptType)) {
            return new StringInvoker(bean, method, charset);
        }
        if (Map.class.isAssignableFrom(acceptType)) {
            return new MapInvoker(bean, method, charset, reconsumeWhenJsonParseFailed);
        }
        return newInvokerExt(acceptType, bean, method, charset);
    }

    protected RmqConsumerMethodInvoker newInvokerExt(Class<?> acceptType, Object bean, Method method, String charset){
        return null;
    }

    private static class MessageInvoker implements RmqConsumerMethodInvoker {

        private Object bean;
        private Method method;

        private MessageInvoker(Object bean, Method method) {
            this.bean = bean;
            this.method = method;

        }

        @Override
        public Object invoke(MessageExt messageExt) throws Exception {
            return method.invoke(bean, messageExt);
        }

    }

    private static class BytesInvoker implements RmqConsumerMethodInvoker {

        private Object bean;
        private Method method;

        private BytesInvoker(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
            method.setAccessible(true);
        }

        @Override
        public Object invoke(MessageExt messageExt) throws Exception {
            return method.invoke(bean, new Object[]{messageExt.getBody()});
        }

    }

    private static class StringInvoker implements RmqConsumerMethodInvoker {

        private Object bean;
        private Method method;
        private Charset charset;

        private StringInvoker(Object bean, Method method, String charset) {
            this.bean = bean;
            this.method = method;
            this.charset = Charset.forName(charset);
            method.setAccessible(true);
        }

        @Override
        public Object invoke(MessageExt messageExt) throws Exception {
            return method.invoke(bean, new String(messageExt.getBody(), charset));
        }

    }

    private static class MapInvoker implements RmqConsumerMethodInvoker {

        private Object bean;
        private Method method;
        private Charset charset;
        private boolean reconsumeWhenJsonParseFailed;

        private MapInvoker(Object bean, Method method, String charset, boolean reconsumeWhenJsonParseFailed) {
            this.bean = bean;
            this.method = method;
            this.charset = Charset.forName(charset);
            this.reconsumeWhenJsonParseFailed = reconsumeWhenJsonParseFailed;
            method.setAccessible(true);
        }

        @Override
        public Object invoke(MessageExt messageExt) throws Exception {
            Map map;
            byte[] body = messageExt.getBody();
            if (body != null && body.length > 0) {
                try {
                    map = JSON.parseObject(new String(body, charset), Map.class);
                } catch (Exception e) {
                    logger.error("Error while parsing RocketMQ message to Map, reconsume:" + reconsumeWhenJsonParseFailed + ", message:" + messageExt, e);
                    return !reconsumeWhenJsonParseFailed;
                }
            } else {
                map = new HashMap(0);
            }
            return method.invoke(bean, map);
        }

    }

}
