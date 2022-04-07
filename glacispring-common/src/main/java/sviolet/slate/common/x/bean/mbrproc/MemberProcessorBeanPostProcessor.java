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

package sviolet.slate.common.x.bean.mbrproc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import sviolet.slate.common.spring.processor.MemberVisitBeanPostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>成员处理器核心逻辑</p>
 *
 * <p>
 * 说明:<br>
 * 1.用于对SpringContext中所有的Bean成员(Field/Method)进行处理, 处理时机为Bean装配阶段(BeanPostProcessor)<br>
 * 2.可以实现Bean成员的自定义注入/变换/代理替换等<br>
 * 3.该注解允许多次声明, 声明不同的处理器处理不同的注解
 * </p>
 *
 * @author S.Violet
 */
class MemberProcessorBeanPostProcessor extends MemberVisitBeanPostProcessor implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Class<? extends Annotation>, MemberProcessor> processors;
    private ApplicationContext applicationContext;

    MemberProcessorBeanPostProcessor(List<AnnotationAttributes> annotationAttributesList) {
        //config
        processors = new HashMap<>(annotationAttributesList.size());
        //handle enable annotations
        for (AnnotationAttributes annotationAttributes : annotationAttributesList) {
            //get processor class
            Class<? extends MemberProcessor>[] processorClasses = (Class<? extends MemberProcessor>[]) annotationAttributes.getClassArray("value");
            for (Class<? extends MemberProcessor> processorClass : processorClasses) {
                addProcessor(processorClass);
            }
        }
    }

    private void addProcessor(Class<? extends MemberProcessor> processorClass) {
        //create processor instance
        MemberProcessor processor;
        try {
            processor = processorClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Illegal MemberProcessor " + processorClass.getName(), e);
        }
        //get accept type
        Class<?> clazz = processor.acceptAnnotationType();
        if (!Annotation.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("Illegal MemberProcessor " + processorClass.getName() + ", method acceptAnnotationType returns null " +
                    "or not an Annotation class, MemberProcessor#acceptAnnotationType must return a class which extends Annotation");
        }
        Class<? extends Annotation> annotationClass = (Class<? extends Annotation>)clazz;
        //check previous
        MemberProcessor previous = processors.get(annotationClass);
        if (previous != null) {
            if (!processorClass.equals(previous.getClass())) {
                throw new RuntimeException("Duplicate MemberProcessor declared that they accept annotation " + annotationClass.getName() +
                        ", " + previous.getClass().getName() + " and " + processor.getClass().getName());
            } else {
                return;
            }
        }
        processors.put(annotationClass, processor);
        if (logger.isInfoEnabled()) {
            logger.info("MemberProcessor | Add MemberProcessor " + processorClass.getName() + " for annotation " + annotationClass.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void visitField(Object bean, String beanName, Field field) {
        Annotation[] annotations = AnnotationUtils.getAnnotations(field);
        for (Annotation annotation : annotations) {
            MemberProcessor processor = processors.get(annotation.annotationType());
            if (processor != null) {
                processor.visitField(bean, beanName, field, annotation, applicationContext);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void visitMethod(Object bean, String beanName, Method method) {
        Annotation[] annotations = AnnotationUtils.getAnnotations(method);
        for (Annotation annotation : annotations) {
            MemberProcessor processor = processors.get(annotation.annotationType());
            if (processor != null) {
                processor.visitMethod(bean, beanName, method, annotation, applicationContext);
            }
        }
    }

    @Override
    public int getOrder() {
        //lowest order
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
