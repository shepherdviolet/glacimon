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

package com.github.shepherdviolet.glacimon.java.spi.api.interfaces;

import java.lang.reflect.Method;

/**
 * <p>When the old version implementation class does not implements the new interface method (@NewMethod annotation marked),
 * we will build a proxy for the service instance. When the unimplemented method is invoked, the invocation will be handled
 * by {@link CompatibleApproach}.</p>
 *
 * @author S.Violet
 */
public interface CompatibleApproach {

    /**
     * <p>When the unimplemented method is invoked, the invocation will be handled by this method.</p>
     *
     * @param serviceInterface Service interface (Extension point)
     * @param serviceInstance Raw service instance (of the implementation class)
     * @param method Method which invoked
     * @param params Parameters of invocation
     * @return Return value of the method
     * @throws Throwable Any throwable
     */
    Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] params) throws Throwable;

}
