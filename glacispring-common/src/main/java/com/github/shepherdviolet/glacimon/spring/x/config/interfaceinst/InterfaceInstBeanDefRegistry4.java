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

package com.github.shepherdviolet.glacimon.spring.x.config.interfaceinst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>[JDK8- Spring 5-]</p>
 *
 * <p>????????????, ???????????????</p>
 *
 * @author shepherdviolet
 */
class InterfaceInstBeanDefRegistry4 implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<AnnotationAttributes> annotationAttributesList;

    InterfaceInstBeanDefRegistry4(List<AnnotationAttributes> annotationAttributesList) {
        this.annotationAttributesList = annotationAttributesList;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        logger.info("InterfaceInst | Interface Instantiation Start (spring 5- or jdk 8-) Doc: https://github.com/shepherdviolet/glacimon");

        Set<String> processedClasses = new HashSet<>(128);

        for (AnnotationAttributes annotationAttributes : annotationAttributesList) {

            //?????????????????????
            InterfaceInstantiator interfaceInstantiator;
            try {
                Class<? extends InterfaceInstantiator> interfaceInstantiatorClass = annotationAttributes.getClass("interfaceInstantiator");
                interfaceInstantiator = interfaceInstantiatorClass.newInstance();
                logger.info("InterfaceInst | InterfaceInstantiator:" + interfaceInstantiatorClass.getName());
            } catch (Exception e) {
                throw new FatalBeanException("InterfaceInst | InterfaceInstantiator create failed", e);
            }
            final InterfaceInstantiator interfaceInstantiatorFinal = interfaceInstantiator;

            //?????????????????????
            Class<? extends Annotation> annotationClass = annotationAttributes.getClass("annotationClass");

            //???????????????
            ClassPathScanningCandidateComponentProvider beanScanner = new ClassPathScanningCandidateComponentProvider(false) {
                @Override
                protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                    // access interface
                    return beanDefinition.getMetadata().isInterface();
                }
            };

            //??????????????????
            if (annotationAttributes.getBoolean("annotationRequired")) {
                beanScanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
            } else {
                beanScanner.addIncludeFilter(new TypeFilter() {
                    @Override
                    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
                        return true;
                    }
                });
            }

            //?????????
            String[] basePackages = annotationAttributes.getStringArray("basePackages");

            if (basePackages == null || basePackages.length <= 0) {
                logger.info("InterfaceInst | Skip, no basePackages");
                return;
            }

            //???????????????
            for (String basePackage : basePackages) {

                //????????????????????????????????????
                logger.info("InterfaceInst | Scan package:" + basePackage);
                Set<BeanDefinition> beanDefinitions = beanScanner.findCandidateComponents(basePackage);

                if (beanDefinitions == null || beanDefinitions.size() <= 0) {
                    continue;
                }

                //?????????????????????
                for (BeanDefinition beanDefinition : beanDefinitions) {

                    //??????
                    String className = beanDefinition.getBeanClassName();
                    //Bean???
                    String beanName;
                    try {
                        beanName = interfaceInstantiator.resolveBeanName(className);
                    } catch (Exception e) {
                        throw new FatalBeanException("InterfaceInst | Resolve bean name failed:" + className, e);
                    }

                    //???????????????????????????
                    if (processedClasses.contains(className)) {
                        logger.warn("InterfaceInst | Duplicate class(skipped):" + className);
                        continue;
                    }

                    try {

                        //???
                        final Class clazz = Class.forName(className);

                        //Bean??????
                        BeanDefinition newBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InterfaceInstFactoryBean4.class)
                                .addConstructorArgValue(clazz)
                                .addConstructorArgValue(interfaceInstantiator)
                                .getBeanDefinition();

                        //??????Bean??????
                        registry.registerBeanDefinition(beanName, newBeanDefinition);

                        //????????????
                        processedClasses.add(className);

                        logger.info("InterfaceInst | Bean created:" + className + ", name:" + beanName);

                    } catch (ClassNotFoundException e) {
                        throw new FatalBeanException("InterfaceInst | Interface class not found:" + className, e);
                    }

                }

            }

        }

        logger.info("InterfaceInst | Interface Instantiation Finished");

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //do nothing
    }

    @Override
    public int getOrder() {
        //register interface beans before others
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
