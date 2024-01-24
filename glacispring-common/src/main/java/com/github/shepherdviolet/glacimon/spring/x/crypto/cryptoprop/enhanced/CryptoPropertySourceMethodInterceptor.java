/*
 * Copyright (C) 2022-2024 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropDecryptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.PropertySource;

public class CryptoPropertySourceMethodInterceptor<T> implements MethodInterceptor {

    private final PropertySource<T> delegate;
    private final CryptoPropDecryptor decryptor;

    public CryptoPropertySourceMethodInterceptor(PropertySource<T> delegate, CryptoPropDecryptor decryptor) {
        this.delegate = delegate;
        this.decryptor = decryptor;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (isGetDelegateCall(invocation)) {
            return delegate;
        }
        if (isGetPropertyCall(invocation)) {
            Object value = invocation.proceed();
            // 如果属性值是String则尝试解密
            if (value instanceof String) {
                return decryptor.decrypt(getNameArgument(invocation), (String) value);
            }
            return value;
        }
        return invocation.proceed();
    }

    private String getNameArgument(MethodInvocation invocation) {
        return (String) invocation.getArguments()[0];
    }

    private boolean isGetDelegateCall(MethodInvocation invocation) {
        return invocation.getMethod().getName().equals("getDelegate");
    }

    private boolean isGetPropertyCall(MethodInvocation invocation) {
        return invocation.getMethod().getName().equals("getProperty")
                && invocation.getMethod().getParameters().length == 1
                && invocation.getMethod().getParameters()[0].getType() == String.class;
    }
}