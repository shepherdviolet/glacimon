/*
 * Copyright (C) 2022-2022 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.java.spi.core;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.NewMethod;
import com.github.shepherdviolet.glacimon.java.spi.api.exceptions.IllegalInterfaceException;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.CompatibleApproach;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.ServiceProxy;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ProxyUtils {

    private static final SpiLogger LOGGER = LogUtils.getLogger();

    /**
     * build proxy instance if some method is abstract, for old version implementation
     */
    @SuppressWarnings("unchecked")
    static <T> T buildProxyIfNeeded(ClassLoader classLoader, Class<T> interfaceClass, T instance, String loaderId) {
        //check @NewMethod annotation on methods
        List<MethodProxy> methodsWithAnnotation = getMethodsWithAnnotation(interfaceClass);
        //return raw instance if no @NewMethod
        if (methodsWithAnnotation.size() <= 0) {
            return instance;
        }
        //check if there is abstract method
        List<MethodProxy> methodProxies = new LinkedList<>();
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            //abstract method
            if (Modifier.isAbstract(method.getModifiers())) {
                //if marked by @NewMethod
                for (MethodProxy methodProxy : methodsWithAnnotation) {
                    if (isMethodMatch(method, methodProxy.method)) {
                        methodProxies.add(methodProxy);
                    }
                }
            }
        }
        //return raw instance if no matched abstract method
        if (methodProxies.size() <= 0) {
            return instance;
        }
        //initCompatibleApproach method proxies
        StringBuilder proxiedMethods = new StringBuilder();
        for (MethodProxy methodProxy : methodProxies) {
            if (LOGGER.isDebugEnabled()) {
                proxiedMethods.append(methodToString(methodProxy.method));
                proxiedMethods.append(" ");
            }
            try {
                methodProxy.initCompatibleApproach();
            } catch (Exception e) {
                throw new IllegalInterfaceException("Illegal compatibleApproach " + methodProxy.annotation.compatibleApproach().getName() +
                        " of interface " + interfaceClass.getName(), e);
            }
        }
        //build proxy
        T proxy = (T) Proxy.newProxyInstance(
                classLoader,
                new Class[]{ServiceProxy.class, interfaceClass},
                new ProxyInvocationHandler(interfaceClass, instance, methodProxies));
        //log
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(loaderId + "|OldImpl-Compat| Create a compatible proxy for old implementation " +
                    instance.getClass().getName() + " (which is implements the old version interface), proxied methods: " +
                    proxiedMethods, null);
        }
        return proxy;
    }

    private static <T> List<MethodProxy> getMethodsWithAnnotation(Class<T> interfaceClass) {
        //get methods with @NewMethod annotation
        List<MethodProxy> methodProxies = new LinkedList<>();
        Method[] interfaceMethods = interfaceClass.getMethods();
        for (Method method : interfaceMethods) {
            NewMethod annotation = method.getAnnotation(NewMethod.class);
            if (annotation != null) {
                methodProxies.add(new MethodProxy(method, annotation));
            }
        }
        return methodProxies;
    }

    private static boolean isMethodMatch(Method method1, Method method2) {
        //check method name
        if (!method1.getName().equals(method2.getName())) {
            return false;
        }
        //check method params
        Class<?>[] params1 = method1.getParameterTypes();
        Class<?>[] params2 = method2.getParameterTypes();
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static String methodToString(Method method) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(method.getName());
        stringBuilder.append('(');
        Class<?>[] params = method.getParameterTypes();
        for (int j = 0; j < params.length; j++) {
            stringBuilder.append(params[j].getName());
            if (j < (params.length - 1)) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(')');
        return stringBuilder.toString();
    }

    private static class MethodProxy {

        private final Method method;
        private final NewMethod annotation;
        private CompatibleApproach compatibleApproach;

        private MethodProxy(Method method, NewMethod annotation) {
            this.method = method;
            this.annotation = annotation;
        }

        private void initCompatibleApproach() throws Exception {
            //new compatible approach
            compatibleApproach = annotation.compatibleApproach().newInstance();
        }

    }

    private static class ProxyInvocationHandler implements InvocationHandler {

        private final Class<?> serviceInterface;
        private final Object serviceInstance;
        private final List<MethodProxy> methodProxies;

        private final Map<Method, CompatibleApproach> compatibleApproachCache = new ConcurrentHashMap<>();

        private ProxyInvocationHandler(Class<?> serviceInterface, Object serviceInstance, List<MethodProxy> methodProxies) {
            this.serviceInterface = serviceInterface;
            this.serviceInstance = serviceInstance;
            this.methodProxies = methodProxies;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            CompatibleApproach compatibleApproach = compatibleApproachCache.get(method);
            if (compatibleApproach == null) {
                //compat
                for (MethodProxy item : methodProxies) {
                    if (isMethodMatch(method, item.method)) {
                        compatibleApproach = item.compatibleApproach;
                    }
                }
                //get raw service instance
                if (compatibleApproach == null) {
                    if (isMethodMatch(method, GET_RAW_SERVICE_INSTANCE_METHOD)) {
                        compatibleApproach = GET_RAW_SERVICE_INSTANCE_APPROACH;
                    }
                }
                //just invoke
                if (compatibleApproach == null) {
                    compatibleApproach = INVOKE_DIRECTLY;
                }
                compatibleApproachCache.put(method, compatibleApproach);
            }
            //invoke compatibleApproach
            return compatibleApproach.onInvoke(serviceInterface, serviceInstance, method, args);
        }

    }

    // CompatibleApproach: InvokeDirectly ////////////////////////////////////////////////////////////////////////////////////

    private static final CompatibleApproach INVOKE_DIRECTLY = new InvokeDirectly();

    private static class InvokeDirectly implements CompatibleApproach {

        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] args) throws Throwable {
            // just invoke method of service instance
            return method.invoke(serviceInstance, args);
        }

    }

    // CompatibleApproach: InvokeGetRawServiceInstance ////////////////////////////////////////////////////////////////////////////////////

    private static final Method GET_RAW_SERVICE_INSTANCE_METHOD;
    private static final CompatibleApproach GET_RAW_SERVICE_INSTANCE_APPROACH = new InvokeGetRawServiceInstance();

    static {
        try {
            GET_RAW_SERVICE_INSTANCE_METHOD = ServiceProxy.class.getMethod("getRawServiceInstance");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class InvokeGetRawServiceInstance implements CompatibleApproach {

        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] args) throws Throwable {
            //get raw service instance
            return serviceInstance;
        }

    }

}
