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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>字段过滤器 (校验/转换): 将过滤参数从String转换成其他类型, 然后缓存起来 (用于转换开销大的场合) </p>
 *
 * @param <T> 过滤参数类型
 * @author shepherdviolet
 */
public abstract class ArgsCachedFilter<T> extends ArgsConvertedFilter<T> {

    private final Map<String, T[]> cache = new ConcurrentHashMap<>();

    /**
     * 1.从缓存获取过滤参数
     * 2.缓存内不存在则转换过滤参数类型
     */
    @Override
    public final T[] convertArgs(String[] args) throws Exception {
        String key = argsToCacheKey(args);
        T[] convertedArgs = cache.get(key);
        if (convertedArgs == null) {
            convertedArgs = preConvertArgs(args);
            cache.putIfAbsent(key, convertedArgs);
        }
        return convertedArgs;
    }

    private String argsToCacheKey(String[] args) {
        int hash = 0;
        for (String arg : args) {
            hash = hash ^ arg.hashCode();
        }
        return Arrays.toString(args) + "@" + hash;
    }

    /**
     * 转换过滤参数的类型.
     * 预检查时会调用本方法进行转换.
     * 过滤(filter)时会从缓存中读取预先转换好的过滤参数, 不会调用本方法.
     *
     * @param args 过滤参数, 非空
     * @return 类型转换后的过滤参数
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    public abstract T[] preConvertArgs(String[] args) throws Exception;

    /**
     * 测试用, 打印缓存情况
     */
    public String printCache(){
        StringBuilder stringBuilder = new StringBuilder("{");
        for (Map.Entry<String, T[]> entry : cache.entrySet()) {
            stringBuilder.append('\n').append(entry.getKey()).append(" = ").append(Arrays.toString(entry.getValue()));
        }
        return stringBuilder.append("\n}").toString();
    }

}
