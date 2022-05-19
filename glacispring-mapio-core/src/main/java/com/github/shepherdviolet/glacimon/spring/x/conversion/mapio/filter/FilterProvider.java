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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;

/**
 * <p>[扩展点: Spring扩展 / GlacimonSpi扩展]</p>
 *
 * <p>过滤器提供者: 为了使MapIo可以适配普通的java程序和基于Spring的应用工程, 我们将过滤器的查找逻辑作为扩展点开放出来. </p>
 *
 * <p>默认实现:</p>
 * <p>JavaFilterProvider: 简易过滤器提供者, 只支持通过type查找, 简单的用反射实例化过滤器实例.</p>
 * <p>GlacimonSpiFilterProvider: 通过GlacimonSpi方式配置和加载过滤器.</p>
 * <p>SpringFilterProvider: 从Spring上下文中, 用类型和名称获取过滤器.</p>
 *
 * @author shepherdviolet
 */
@SingleServiceInterface
public interface FilterProvider {

    /**
     * <p>获取Filter实例. </p>
     *
     * <p>不可能出现type和name都没指定的情况 (即不可能出现filter==Filter.class, name==null的情况), 因为这种情况的规则会被忽略掉. </p>
     *
     * @param type 过滤器类型, 非空
     * @param name 过滤器名称, 如果是null表示未指定名称 (不会出现"")
     * @return 过滤器实例
     */
    Filter findFilter(Class<? extends Filter> type, String name);

}
