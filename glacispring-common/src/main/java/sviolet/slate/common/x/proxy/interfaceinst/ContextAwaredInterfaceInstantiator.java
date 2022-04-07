/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.slate.common.x.proxy.interfaceinst;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>接口实例化器:可实现代理逻辑, 可获取ApplicationContext, 可获取接口类型</p>
 *
 * @author S.Violet
 */
public abstract class ContextAwaredInterfaceInstantiator implements InterfaceInstantiator, ApplicationContextAware {

    private AtomicBoolean applicationContextAwared = new AtomicBoolean(false);

    @Override
    public final Object newInstance(Class<?> interfaceType) throws Exception {
        //创建一个实现指定接口和ApplicationContextAware接口的代理类
        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{interfaceType, ApplicationContextAware.class, InitializingBean.class},
                new InvokeHandler(interfaceType));
    }

    /**
     * 实现根据类名决定Bean名
     */
    @Override
    public String resolveBeanName(String interfaceType) throws Exception {
        return interfaceType;
    }

    /**
     * 当接口实例初始化完成时(Spring InitializingBean.afterPropertiesSet), 触发该方法, 每个代理对象都会触发一次
     * @param interfaceType 接口类型
     * @param proxy 代理类实例
     */
    protected abstract void onInitialized(Class<?> interfaceType, Object proxy);

    /**
     * 当接口方法被调用时, 触发该方法
     * @param interfaceType 接口类型
     * @param proxy 代理类实例
     * @param method 被调用方法
     * @param args 方法参数
     */
    protected abstract Object onMethodInvoke(Class<?> interfaceType, Object proxy, Method method, Object[] args) throws Throwable;

    private class InvokeHandler implements InvocationHandler {

        private Class<?> interfaceType;

        /**
         * @param interfaceType 保存接口类型
         */
        private InvokeHandler(Class<?> interfaceType) {
            this.interfaceType = interfaceType;
        }

        @Override
        public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args.length == 1 && args[0] instanceof ApplicationContext && "setApplicationContext".equals(method.getName())) {
                //第一个代理类的setApplicationContext被调用时, 将ApplicationContext对象传给ContextAwaredInterfaceInstantiator的实现类
                //用于ContextAwaredInterfaceInstantiator的实现类从上下文中获取Bean, 便于实现更复杂的逻辑(转给其他Bean处理等)
                if (applicationContextAwared.compareAndSet(false, true)) {
                    setApplicationContext((ApplicationContext) args[0]);
                }
                return null;
            } else if (args.length == 0 && "afterPropertiesSet".equals(method.getName())) {
                //当代理类的afterPropertiesSet被调用时, 触发onInitialized
                onInitialized(interfaceType, proxy);
                return null;
            }
            //当代理类的其他方法被调用
            return onMethodInvoke(interfaceType, proxy, method, args);
        }

    }

}
