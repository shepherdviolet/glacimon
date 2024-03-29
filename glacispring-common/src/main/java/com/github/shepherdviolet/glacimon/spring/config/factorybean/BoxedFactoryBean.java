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

package com.github.shepherdviolet.glacimon.spring.config.factorybean;

import org.springframework.beans.factory.FactoryBean;

/**
 * [FactoryBean]用于将已经实例化好的Bean对象注册到spring context中
 *
 * @author shepherdviolet
 */
public class BoxedFactoryBean implements FactoryBean {

    private final Class<?> type;
    private final Object obj;

    public BoxedFactoryBean(Class<?> type, Object obj) {
        this.type = type;
        this.obj = obj;
    }

    @Override
    public Object getObject() throws Exception {
        return obj;
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

}
