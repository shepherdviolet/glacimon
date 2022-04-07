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

package sviolet.slate.common.helper.hessianlite;

import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.JavaDeserializer;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

/**
 * <p>强化版HessianLite的SerializerFactory, 依赖: com.alibaba:hessian-lite</p>
 *
 * <p>
 *     1.改用Objenesis实例化类, 提高泛用性<br>
 * </p>
 *
 * <pre>
 *     public class Foo {
 *
 *         private SerializerFactory serializerFactory;
 *
 *         public Foo() {
 *             serializerFactory = new SlateHessianLiteSerializerFactory();
 *             serializerFactory.setAllowNonSerializable(true);
 *         }
 *
 *         public
 *
 *     }
 * </pre>
 *
 * @author S.Violet
 */
public class SlateHessianLiteSerializerFactory extends SerializerFactory {

    private Objenesis objenesis = new ObjenesisStd();

    @Override
    protected Deserializer getDefaultDeserializer(Class clazz) {
        return new DefaultDeserializer(clazz, objenesis);
    }

    private static final class DefaultDeserializer extends JavaDeserializer {

        /**
         * 类型
         */
        private Class type;

        /**
         * 实例化器, 实例化器线程安全, 复用可提高性能
         */
        private ObjectInstantiator instantiator;

        private DefaultDeserializer(Class clazz, Objenesis objenesis) {
            super(clazz);
            type = clazz;
            instantiator = objenesis.getInstantiatorOf(clazz);
        }

        @Override
        protected Object instantiate() throws Exception {
            try {
                return instantiator.newInstance();
            } catch (Exception e) {
                throw new HessianProtocolException("'" + type.getName() + "' could not be instantiated", e);
            }
        }

    }

}
