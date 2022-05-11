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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.FieldScreeningMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.IoMode;

import java.util.Map;

/**
 * MapIO API, Thread-safe
 *
 * @author shepherdviolet
 */
public interface MapIo {

    /**
     * 根据字典中'用注解定义的规则'映射Map元素
     *
     * @param rawData 原数据Map
     * @param ioMode INPUT / OUTPUT
     * @param fieldScreeningMode Map字段筛选模式
     * @param dictionaries 映射字典, 根据其中用注解定义的规则映射元素.
     *                     配置多个字典时, 规则执行顺序同数组顺序, 因此最后一个字典拥有最高优先级(对相同字段的处理以最后一个字典为准)
     * @return 映射结果Map
     */
    Map<String, Object> doMap(Map<String, Object> rawData, IoMode ioMode, FieldScreeningMode fieldScreeningMode, Class<?>... dictionaries);

    /**
     * 预加载字典类,
     * IOMapper默认采用懒加载模式, 字典类用到了才会初始化.
     * 预加载适合线上系统, 在启动时就对所有字典类配置的规则进行检查, 避免出现非法的规则.
     *
     * @param dictionaries 映射字典
     */
    void preloadDictionaries(Class<?>... dictionaries);

    /**
     * 打印映射器缓存信息
     */
    String printCachedMappers();

}
