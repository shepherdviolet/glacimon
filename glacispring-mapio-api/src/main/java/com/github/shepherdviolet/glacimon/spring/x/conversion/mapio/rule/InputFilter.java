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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule;

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.Filter;

import java.lang.annotation.*;

/**
 * <p>字段输入规则: 使用过滤器对字段进行过滤.</p>
 *
 * @author shepherdviolet
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(InputFilter.InputFilters.class)
public @interface InputFilter {

    /**
     * <p>关于过滤规则执行顺序(clauseOrder):</p>
     * <p>1.一般情况下, MapIo会根据注解的书写顺序执行规则, 不需要手动设置执行顺序.</p>
     * <p>2.如果一个字段存在多种'输入过滤规则'(@Input...), 且同一种'输入过滤规则'存在多条时, 程序将无法分辨注解的书写顺序, 这种情况,
     * 需要手动设置每个'输入过滤规则'的执行顺序.</p>
     * <p>3.如果一个字段存在多种'输出过滤规则'(@Output...), 且同一种'输出过滤规则'存在多条时, 程序将无法分辨注解的书写顺序, 这种情况,
     * 需要手动设置每个'输出过滤规则'的执行顺序.</p>
     * <p>4.输入规则和输出规则互不影响.</p>
     * <p></p>
     * <p>执行顺序(clauseOrder)设置说明:</p>
     * <p>1.执行顺序的数值越小优先级越高(越先执行). 小于0表示未设置顺序(按代码书写顺序执行). 建议设置为: 0 1 2 3 ...</p>
     * <p>2.一个字段的'输入过滤规则'(@Input...)执行顺序要么全都不设置, 要么全都设置, 不可以只设置部分.</p>
     * <p>3.一个字段的'输出过滤规则'(@Output...)执行顺序要么全都不设置, 要么全都设置, 不可以只设置部分.</p>
     * <p>4.输入规则和输出规则互不影响.</p>
     * <p></p>
     * <p>例如:</p>
     * <pre>
     * -    // 有两种规则"InputFilter""InputElementFilter", 且"InputElementFilter"存在对条
     * -    // 需要手动设置顺序
     * -    @Input
     * -    @InputFilter(clauseOrder = 0, ...)
     * -    @InputElementFilter(clauseOrder = 1, ...)
     * -    @InputElementFilter(clauseOrder = 2, ...)
     * -    Name,
     * -
     * -    // 有两种规则"InputFilter""InputMapper", 且"InputMapper"存在多条
     * -    // 需要手动设置顺序
     * -    @InputMapper(clauseOrder = 0, ...)
     * -    @InputFilter(clauseOrder = 1, ...)
     * -    @InputMapper(clauseOrder = 2, ...)
     * -    Addr
     * </pre>
     * <p>例如:</p>
     * <pre>
     * -    // 有三种规则"InputFilter""InputElementFilter""InputMapper", 但是每种只有一条
     * -    // 不需要设置顺序
     * -    @Input
     * -    @InputFilter
     * -    @InputMapper
     * -    @InputElementFilter
     * -    Name,
     * -    // 有一种规则"InputFilter", 它存在多条
     * -    // 不需要设置顺序
     * -    @Input
     * -    @InputFilter
     * -    @InputFilter
     * -    @InputFilter
     * -    Name,
     * </pre>
     *
     */
    int clauseOrder() default -1;

    /**
     * 过滤器类型, type和name至少设置一个, 否则无效
     */
    Class<? extends Filter> type() default Filter.class;

    /**
     * 过滤器名称, type和name至少设置一个, 否则无效
     */
    String name() default "";

    /**
     * 过滤参数, 可选
     */
    String[] args() default {};

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface InputFilters {

        InputFilter[] value();

    }

}
