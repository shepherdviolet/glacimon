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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FieldScreeningMode;

import java.lang.annotation.Annotation;

public class OutputMapperRule extends OrderedRule {

    /**
     * 映射字典, 根据其中用注解定义的规则映射元素.
     */
    private final Class<?> dictionary;

    /**
     * Map字段筛选模式
     */
    private final FieldScreeningMode fieldScreeningMode;

    public OutputMapperRule(Annotation annotation, int order,
                            Class<?> dictionary, FieldScreeningMode fieldScreeningMode) {
        super(annotation, order);

        if (fieldScreeningMode == null) {
            throw new IllegalArgumentException("fieldScreeningMode is null");
        }
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }
        this.fieldScreeningMode = fieldScreeningMode;
        this.dictionary = dictionary;
    }

    public Class<?> dictionary() {
        return dictionary;
    }

    public FieldScreeningMode fieldScreeningMode() {
        return fieldScreeningMode;
    }

}
