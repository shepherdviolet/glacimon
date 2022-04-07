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

package sviolet.slate.common.util.proxy;

import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;

/**
 * <p>Spring CGLIB 代理工具</p>
 *
 * @author S.Violet
 */
public class SpringCglibUtils {

    /**
     * 给定类/接口创建一个代理对象, 所有的方法实现为空, 通常用于将接口实例化
     *
     * 依赖: org.springframework:spring-core
     */
    public static <T> T newEmptyInstance(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {clazz}, emptyInvocationHandler);
    }

    private static InvocationHandler emptyInvocationHandler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // do something
            // 注意如果接口方法返回类型是基本类型的话, 这里返回null会抛出空指针
            return null;
        }
    };

}
