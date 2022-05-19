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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.RuleInfo;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.Filter;

import java.util.Arrays;

/**
 * <p>字段过滤器 (校验/转换): 将过滤参数从String转换成其他类型</p>
 *
 * @param <T> 过滤参数类型
 * @author shepherdviolet
 */
public abstract class ArgsConvertedFilter<T> implements Filter {

    /**
     * 1.转换过滤参数类型
     * 2.检查过滤参数
     */
    @Override
    public final void doPreCheckArgs(String[] args) throws Exception {
        T[] converted = convertArgs(args);
        preCheckArgs(converted);
    }

    /**
     * 1.转换过滤参数类型 (因为有预检查, 所以这里应该不会失败了)
     * 2.执行过滤
     */
    @Override
    public final Object doFilter(Object element, String[] args, RuleInfo ruleInfo) {
        T[] convertedArgs;
        try {
            convertedArgs = convertArgs(args);
        } catch (Throwable t) {
            throw new RuntimeException("MapIO | Filter | Failed to convert filter args to required type, args: " + Arrays.toString(args) +
                    ". This exception is generally not possible, because args has been checked in advance");
        }
        return filter(element, convertedArgs, ruleInfo);
    }

    /**
     * 转换过滤参数的类型 (每次预检查和过滤都会调用本方法进行转换)
     *
     * @param args 过滤参数, 非空
     * @return 类型转换后的过滤参数
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    public abstract T[] convertArgs(String[] args) throws Exception;

    /**
     * 预先检查过滤参数的格式 (注意, 这个方法可能会被调用多次, 每次的入参都有可能不同).
     *
     * 当一个字典类被加载时, MapIo会检查其中的规则是否合法, 会调用本方法检查过滤参数是否合法.
     *
     * @param args 过滤参数, 非空
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    public abstract void preCheckArgs(T[] args) throws Exception;

    /**
     * 实现字段过滤(校验/转换)
     *
     * @param element 过滤前, 非空
     * @param args 过滤参数, 非空
     * @param ruleInfo 过滤规则信息, 用于打印日志/输出错误信息
     * @return 过滤后 (不转换就返回原值)
     */
    public abstract Object filter(Object element, T[] args, RuleInfo ruleInfo);

}
