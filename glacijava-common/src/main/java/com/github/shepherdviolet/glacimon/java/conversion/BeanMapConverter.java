/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.java.conversion;

import com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean - Map 转换器 (浅克隆)
 *
 * @author shepherdviolet
 */
public class BeanMapConverter {

    public static Map<String, Object> beanToMap(Object bean) {
        Map<String, Object> map = new HashMap<>();
        beanToMap(bean, map);
        return map;
    }

    public static void beanToMap(Object bean, Map<String, Object> map) {
        if (bean == null || map == null) {
            return;
        }
        try {
            Map<String, BeanInfoUtils.PropertyInfo> infos = BeanInfoUtils.getPropertyInfos(bean.getClass());
            for (Map.Entry<String, BeanInfoUtils.PropertyInfo> entry : infos.entrySet()) {
                map.put(entry.getKey(), entry.getValue().get(bean, false));
            }
        } catch (Throwable ignore) {
        }
    }

    public static void mapToBean(Map<String, Object> map, Object bean) {
        if (bean == null || map == null) {
            return;
        }
        try {
            Map<String, BeanInfoUtils.PropertyInfo> infos = BeanInfoUtils.getPropertyInfos(bean.getClass());
            for (Map.Entry<String, BeanInfoUtils.PropertyInfo> entry : infos.entrySet()) {
                if (!map.containsKey(entry.getKey())) {
                    continue;
                }
                entry.getValue().set(bean, map.get(entry.getKey()), false);
            }
        } catch (Throwable ignore) {
        }
    }

}
