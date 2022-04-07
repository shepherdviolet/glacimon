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

package sviolet.slate.common.x.proxy.interfaceinst;

import org.springframework.beans.factory.FactoryBean;

/**
 * <p>[JDK8- Spring 5-]</p>
 *
 * <p>FactoryBean, 适配低版本</p>
 *
 * @author S.Violet
 */
public class InterfaceInstFactoryBean4<T> implements FactoryBean<T> {

    private Class<?> clazz;
    private InterfaceInstantiator interfaceInstantiator;

    public InterfaceInstFactoryBean4(Class<?> clazz, InterfaceInstantiator interfaceInstantiator) {
        this.clazz = clazz;
        this.interfaceInstantiator = interfaceInstantiator;
    }

    @Override
    public T getObject() throws Exception {
        return (T) interfaceInstantiator.newInstance(clazz);
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
