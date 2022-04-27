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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.rule;

import java.lang.annotation.Annotation;

public class OrderedRule extends BaseRule {

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
     */
    private final int order;

    public OrderedRule(Annotation annotation, int order) {
        super(annotation);
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
