/*
 * Copyright (C) 2015-2018 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.spring.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * [BeanPostProcessor]对每个Bean的成员进行访问
 *
 * @author S.Violet
 */
public abstract class MemberVisitBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    @Override
    public final Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class clazz = bean.getClass();
        ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                visitField(bean, beanName, field);
            }
        });
        ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                visitMethod(bean, beanName, method);
            }
        });
        return bean;
    }

    @Override
    public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * visit field of bean
     * @param bean bean
     * @param beanName bean name
     * @param field field
     */
    protected abstract void visitField(Object bean, String beanName, Field field);

    /**
     * visit method of bean
     * @param bean bean
     * @param beanName bean name
     * @param method method
     */
    protected abstract void visitMethod(Object bean, String beanName, Method method);

}

