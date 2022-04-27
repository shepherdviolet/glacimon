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

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>[扩展点: Spring扩展 / GlacimonSpi扩展]</p>
 *
 * <p>规则注解管理器, 实现规则注解与核心逻辑解耦. 可以通过自定义RuleAnnotationParser改变规则注解的类型.</p>
 *
 * @author shepherdviolet
 */
@SingleServiceInterface
public interface RuleAnnotationManager {

    /**
     * 从Field中获取Input注解规则, 默认为@Input
     */
    InputRule getInputRule(Field field);

    /**
     * 从Field中获取Output注解规则, 默认为@Output
     */
    OutputRule getOutputRule(Field field);

    /**
     * 从Field中获取Input...系列注解规则(不含Input注解), 默认为@InputFilter/@InputElementFilter/@InputMapper
     */
    List<OrderedRule> getInputSeriesRules(Field field);

    /**
     * 从Field中获取Input...系列注解规则(不含Input注解), 默认为@InputFilter/@InputElementFilter/@InputMapper
     */
    List<OrderedRule> getOutputSeriesRules(Field field);

}
