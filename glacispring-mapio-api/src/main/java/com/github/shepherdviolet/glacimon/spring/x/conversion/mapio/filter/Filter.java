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

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.MultipleServiceInterface;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.RuleInfo;

/**
 * <p>字段过滤器 (校验/转换)</p>
 *
 * @author shepherdviolet
 */
@MultipleServiceInterface
public interface Filter {

    /**
     * <p>预先检查过滤参数的格式 (注意, 这个方法可能会被调用多次, 每次的入参都有可能不同).</p>
     * <p>当一个字典类被加载时, MapIo会检查其中的规则是否合法, 会调用本方法检查过滤参数是否合法.</p>
     *
     * <p>这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     * 所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.</p>
     *
     * @param args 过滤参数, 非空
     * @throws Exception 这个方法里不用太关注异常, 不论抛出什么, 都会被封装成FilterRuleException, 因为过滤参数会被预检查,
     *                  所以理论上只有在预加载字典规则的时候才会报错, 映射Map的时候不会报错.
     */
    void doPreCheckArgs(String[] args) throws Exception;

    /**
     * <p>实现字段过滤(校验/转换)</p>
     *
     * @param element 过滤前, 非空
     * @param args 过滤参数, 非空
     * @param ruleInfo 过滤规则信息, 用于打印日志/输出错误信息
     * @return 过滤后 (不转换就返回原值)
     */
    Object doFilter(Object element, String[] args, RuleInfo ruleInfo);

}
