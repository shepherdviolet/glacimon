/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.slate.common.util.proxy;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * <p>Spring AOP 工具</p>
 *
 * <p>依赖: org.springframework:spring-aop</p>
 *
 * <p>Spring AOP有两种代理方式, 默认是JDK Dynamic, 可以选用CGLIB, 设置方法为:</p>
 * <p><code>@EnableAspectJAutoProxy(proxyTargetClass = true)</code></p>
 * <p><code>@EnableTransactionManagement(proxyTargetClass = true)</code></p>
 *
 * @author S.Violet
 */
public class SpringAopUtils {

    /**
     * 判断一个对象是否是Spring AOP代理对象, 其他方式产生的代理无法用这个判断
     *
     * 依赖: org.springframework:spring-aop
     */
    public static boolean isAopProxy(Object proxy){
        return AopUtils.isAopProxy(proxy);
    }

    /**
     * 判断一个对象是否是基于JDK动态代理的Spring AOP代理对象, 其他方式产生的代理无法用这个判断
     *
     * 依赖: org.springframework:spring-aop
     */
    public static boolean isJdkProxy(Object proxy){
        return AopUtils.isJdkDynamicProxy(proxy);
    }

    /**
     * 判断一个对象是否是基于CGLIB代理的Spring AOP代理对象, 其他方式产生的代理无法用这个判断
     *
     * 依赖: org.springframework:spring-aop
     */
    public static boolean isCglibProxy(Object proxy){
        return AopUtils.isCglibProxy(proxy);
    }

    /**
     * 在被AOP代理的方法中, 获取代理实例. 用于代理类内部方法间调用.
     *
     * 注意: 必须设置AOP代理方式为CGLIB: @EnableAspectJAutoProxy(proxyTargetClass = true) 和 @EnableTransactionManagement(proxyTargetClass = true)
     * 依赖: org.springframework:spring-aop
     */
    public static <T> T getCurrentProxy(){
        return (T) AopContext.currentProxy();
    }

    /**
     * 从Spring AOP代理对象中获取被代理的对象
     *
     * 依赖: org.springframework:spring-aop
     *
     * @param proxy Spring AOP代理对象(未指定哪种方式)
     * @throws ProxyTypeNotMatchException 不是Spring AOP代理对象
     * @throws ProxyTargetGetException 被代理对象获取失败
     */
    public static <T> T getProxyTarget(Object proxy) throws ProxyTypeNotMatchException, ProxyTargetGetException {
        if (proxy == null) {
            throw new ProxyTypeNotMatchException("Can not get proxy target from null");
        }
        if (!isAopProxy(proxy)) {
            throw new ProxyTypeNotMatchException("Object " + proxy.getClass().getName() + " is not an AOP proxy instance");
        }
        if (isCglibProxy(proxy)) {
            return getCglibProxyTarget0(proxy);
        }
        if (isJdkProxy(proxy)) {
            return getJdkProxyTarget0(proxy);
        }
        throw new ProxyTypeNotMatchException("Object " + proxy.getClass().getName() + " is not a JDK Dynamic or CGLIB proxy instance");
    }

    /**
     * 从Spring AOP CGLIB代理对象中获取被代理的对象
     *
     * 前提: AOP代理方式为CGLIB: 设置了@EnableAspectJAutoProxy(proxyTargetClass = true) @EnableTransactionManagement(proxyTargetClass = true)
     * 依赖: org.springframework:spring-aop
     *
     * @param proxy Spring AOP CGLIB代理对象
     * @throws ProxyTypeNotMatchException 不是Spring AOP CGLIB代理对象
     * @throws ProxyTargetGetException 被代理对象获取失败
     */
    public static <T> T getCglibProxyTarget(Object proxy) throws ProxyTypeNotMatchException, ProxyTargetGetException {
        if (proxy == null) {
            throw new ProxyTypeNotMatchException("Can not get proxy target from null");
        }
        if (!isAopProxy(proxy)) {
            throw new ProxyTypeNotMatchException("Object " + proxy.getClass().getName() + " is not an AOP proxy instance");
        }
        if (!isCglibProxy(proxy)) {
            throw new ProxyTypeNotMatchException("Object " + proxy.getClass().getName() + " is not a CGLIB proxy instance");
        }
        return getCglibProxyTarget0(proxy);
    }

    private static <T> T getCglibProxyTarget0(Object proxy) throws ProxyTargetGetException {
        try {
            Field cglibCallbackField = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            cglibCallbackField.setAccessible(true);
            Object cglibCallback = cglibCallbackField.get(proxy);
            Field advisedField = cglibCallback.getClass().getDeclaredField("advised");
            advisedField.setAccessible(true);
            return (T) ((AdvisedSupport)advisedField.get(cglibCallback)).getTargetSource().getTarget();
        } catch (Throwable t) {
            throw new ProxyTargetGetException("Failed to get target instance of JDK dynamic proxy " + proxy.getClass().getName(), t);
        }
    }

    /**
     * 从Spring AOP JDK Dynamic代理对象中获取被代理的对象
     *
     * 前提: AOP代理方式为默认: 未设置或设置了@EnableAspectJAutoProxy(proxyTargetClass = false) @EnableTransactionManagement(proxyTargetClass = false)
     * 依赖: org.springframework:spring-aop
     *
     * @param proxy Spring AOP JDK Dynamic代理对象
     * @throws ProxyTypeNotMatchException 不是Spring AOP JDK Dynamic代理对象
     * @throws ProxyTargetGetException 被代理对象获取失败
     */
    public static <T> T getJdkProxyTarget(Object proxy) throws ProxyTypeNotMatchException, ProxyTargetGetException {
        if (proxy == null) {
            throw new ProxyTypeNotMatchException("Can not get proxy target from null");
        }
        if (!isAopProxy(proxy)) {
            throw new ProxyTypeNotMatchException("Object " + proxy.getClass().getName() + " is not an AOP proxy instance");
        }
        if (!isJdkProxy(proxy)) {
            throw new ProxyTypeNotMatchException("Object " + proxy.getClass().getName() + " is not a JDK dynamic proxy instance");
        }
        return getJdkProxyTarget0(proxy);
    }

    private static <T> T getJdkProxyTarget0(Object proxy) throws ProxyTargetGetException {
        try {
            Field hField = proxy.getClass().getSuperclass().getDeclaredField("h");
            hField.setAccessible(true);
            Object h = hField.get(proxy);
            Field advisedField = h.getClass().getDeclaredField("advised");
            advisedField.setAccessible(true);
            return (T) ((AdvisedSupport) advisedField.get(h)).getTargetSource().getTarget();
        } catch (Throwable t) {
            throw new ProxyTargetGetException("Failed to get target instance of JDK dynamic proxy " + proxy.getClass().getName(), t);
        }
    }

    public static class ProxyTypeNotMatchException extends Exception {

        private static final long serialVersionUID = -3447138652839061521L;

        public ProxyTypeNotMatchException(String message) {
            super(message);
        }

    }

    public static class ProxyTargetGetException extends Exception {

        private static final long serialVersionUID = -3447138652839061521L;

        public ProxyTargetGetException(String message, Throwable t) {
            super(message, t);
        }

    }

}
