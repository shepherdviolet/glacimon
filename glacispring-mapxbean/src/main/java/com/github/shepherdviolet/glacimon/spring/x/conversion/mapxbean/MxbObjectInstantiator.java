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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapxbean;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;

/**
 * <p>[Common Handler] Object instantiator</p>
 *
 * <p>Purpose: Create instance of given class</p>
 *
 * <p>GlacimonSpi Extension point. Doc: https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md</p>
 *
 * @author shepherdviolet
 * @see MapXBean
 * @see MxbObjectInstantiatorImpl
 */
@SingleServiceInterface
public interface MxbObjectInstantiator {

    /**
     * Create instance of given class
     *
     * @param clazz class
     * @param byConstructor true: Must be created using the parameter-less constructor, false: Can be created by
     *                      objenesis without constructor
     * @param <T> class type
     * @return The instance of given class
     */
    <T> T newInstance(Class<T> clazz, boolean byConstructor) throws Exception;

}
