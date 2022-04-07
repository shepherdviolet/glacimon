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

package sviolet.slate.common.spring.bean;

import org.springframework.beans.factory.FactoryBean;

/**
 * [FactoryBean]用于将已经实例化好的Bean对象注册到spring context中
 *
 * @author S.Violet
 */
public class BoxedFactoryBean implements FactoryBean {

    private Class<?> type;
    private Object obj;

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
