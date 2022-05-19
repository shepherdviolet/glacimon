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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认规则注解管理器, 使用默认的一套规则注解
 *
 * @author shepherdviolet
 */
public class DefaultRuleAnnotationManager implements RuleAnnotationManager {

    @SuppressWarnings("deprecation")
    @Override
    public InputRule getInputRule(Field field) {
        Input input = field.getAnnotation(Input.class);
        if (input == null) {
            return null;
        }
        String fieldNameStr = null;
        FieldName fieldName = field.getAnnotation(FieldName.class);
        if (fieldName != null) {
            fieldNameStr = fieldName.value();
        }
        return new InputRule(input, input.fromKey(), input.required(), fieldNameStr);
    }

    @SuppressWarnings("deprecation")
    @Override
    public OutputRule getOutputRule(Field field) {
        Output output = field.getAnnotation(Output.class);
        if (output == null) {
            return null;
        }
        String fieldNameStr = null;
        FieldName fieldName = field.getAnnotation(FieldName.class);
        if (fieldName != null) {
            fieldNameStr = fieldName.value();
        }
        return new OutputRule(output, output.toKey(), output.required(), fieldNameStr);
    }

    @Override
    public List<OrderedRule> getInputSeriesRules(Field field) {
        Annotation[] annotations = field.getAnnotations();
        List<OrderedRule> rules = new ArrayList<>(annotations.length);

        for (Annotation annotation : annotations) {
            if (annotation instanceof InputFilter) {

                InputFilter inputFilter = (InputFilter) annotation;
                rules.add(new InputFilterRule(annotation, inputFilter.clauseOrder(),
                        inputFilter.type(), inputFilter.name(), inputFilter.args()));

            } else if (annotation instanceof InputElementFilter) {

                InputElementFilter inputElementFilter = (InputElementFilter) annotation;
                rules.add(new InputElementFilterRule(annotation, inputElementFilter.clauseOrder(),
                        inputElementFilter.type(), inputElementFilter.name(), inputElementFilter.args(), inputElementFilter.keepOrder()));

            } else if (annotation instanceof InputMapper) {

                InputMapper inputMapper = (InputMapper) annotation;
                rules.add(new InputMapperRule(annotation, inputMapper.clauseOrder(),
                        inputMapper.dictionary(), inputMapper.fieldScreeningMode()));

            } else if (annotation instanceof InputFilter.InputFilters) {

                for (InputFilter inputFilter : ((InputFilter.InputFilters) annotation).value()) {
                    rules.add(new InputFilterRule(inputFilter, inputFilter.clauseOrder(),
                            inputFilter.type(), inputFilter.name(), inputFilter.args()));
                }

            } else if (annotation instanceof InputElementFilter.InputElementFilters) {

                for (InputElementFilter inputElementFilter : ((InputElementFilter.InputElementFilters) annotation).value()) {
                    rules.add(new InputElementFilterRule(inputElementFilter, inputElementFilter.clauseOrder(),
                            inputElementFilter.type(), inputElementFilter.name(), inputElementFilter.args(), inputElementFilter.keepOrder()));
                }

            } else if (annotation instanceof InputMapper.InputMappers) {

                for (InputMapper inputMapper : ((InputMapper.InputMappers) annotation).value()) {
                    rules.add(new InputMapperRule(inputMapper, inputMapper.clauseOrder(),
                            inputMapper.dictionary(), inputMapper.fieldScreeningMode()));
                }

            }
        }

        return rules;
    }

    @Override
    public List<OrderedRule> getOutputSeriesRules(Field field) {
        Annotation[] annotations = field.getAnnotations();
        List<OrderedRule> rules = new ArrayList<>(annotations.length);

        for (Annotation annotation : annotations) {
            if (annotation instanceof OutputFilter) {

                OutputFilter outputFilter = (OutputFilter) annotation;
                rules.add(new OutputFilterRule(annotation, outputFilter.clauseOrder(),
                        outputFilter.type(), outputFilter.name(), outputFilter.args()));

            } else if (annotation instanceof OutputElementFilter) {

                OutputElementFilter outputElementFilter = (OutputElementFilter) annotation;
                rules.add(new OutputElementFilterRule(annotation, outputElementFilter.clauseOrder(),
                        outputElementFilter.type(), outputElementFilter.name(), outputElementFilter.args(), outputElementFilter.keepOrder()));

            } else if (annotation instanceof OutputMapper) {

                OutputMapper outputMapper = (OutputMapper) annotation;
                rules.add(new OutputMapperRule(annotation, outputMapper.clauseOrder(),
                        outputMapper.dictionary(), outputMapper.fieldScreeningMode()));

            } else if (annotation instanceof OutputFilter.OutputFilters) {

                for (OutputFilter outputFilter : ((OutputFilter.OutputFilters) annotation).value()) {
                    rules.add(new OutputFilterRule(outputFilter, outputFilter.clauseOrder(),
                            outputFilter.type(), outputFilter.name(), outputFilter.args()));
                }

            } else if (annotation instanceof OutputElementFilter.OutputElementFilters) {

                for (OutputElementFilter outputElementFilter : ((OutputElementFilter.OutputElementFilters) annotation).value()) {
                    rules.add(new OutputElementFilterRule(outputElementFilter, outputElementFilter.clauseOrder(),
                            outputElementFilter.type(), outputElementFilter.name(), outputElementFilter.args(), outputElementFilter.keepOrder()));
                }

            } else if (annotation instanceof OutputMapper.OutputMappers) {

                for (OutputMapper outputMapper : ((OutputMapper.OutputMappers) annotation).value()) {
                    rules.add(new OutputMapperRule(outputMapper, outputMapper.clauseOrder(),
                            outputMapper.dictionary(), outputMapper.fieldScreeningMode()));
                }

            }
        }

        return rules;
    }

}
