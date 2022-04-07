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

package com.github.shepherdviolet.glacimon.java.spi.test;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.MultipleServiceInterface;
import com.github.shepherdviolet.glacimon.java.spi.api.annotation.NewMethod;
import com.github.shepherdviolet.glacimon.java.spi.api.compat.DoNothing;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.CompatibleApproach;

import java.lang.reflect.Method;

@MultipleServiceInterface
public interface CompatService {

    String oldMethod(String param);

    @NewMethod(compatibleApproach = DoNothing.class)
    String newMethod(String param);

    @NewMethod(compatibleApproach = NewMethodCompat.class)
    String newMethod(String param1, String param2);

    class NewMethodCompat implements CompatibleApproach {
        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] params) throws Throwable {
            return ((CompatService)serviceInstance).oldMethod(params[0] + "" + params[1]);
        }
    }

}
