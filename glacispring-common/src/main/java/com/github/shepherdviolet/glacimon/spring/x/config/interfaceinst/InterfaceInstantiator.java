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

package com.github.shepherdviolet.glacimon.spring.x.config.interfaceinst;

/**
 * <p>接口实例化器</p>
 * @author shepherdviolet
 */
public interface InterfaceInstantiator {

    /**
     * 实现将接口类实例化成对象
     * @param interfaceType 接口类型
     * @return 实例化的对象
     */
    Object newInstance(Class<?> interfaceType) throws Exception;

    /**
     * 实现根据接口类型决定Bean名
     * @param interfaceType 接口类型
     * @return Bean名
     */
    String resolveBeanName(String interfaceType) throws Exception;

}
