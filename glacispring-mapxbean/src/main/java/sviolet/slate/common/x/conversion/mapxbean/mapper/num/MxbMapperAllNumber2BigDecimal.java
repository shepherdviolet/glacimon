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

package sviolet.slate.common.x.conversion.mapxbean.mapper.num;

import sviolet.slate.common.x.conversion.mapxbean.MxbTypeMapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class MxbMapperAllNumber2BigDecimal implements MxbTypeMapper {

    private static final Class[] FROM = new Class[]{
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
    };

    private static final Class[] TO = new Class[]{
            BigDecimal.class,
    };

    @Override
    public Object map(Object from, Class<?> toType, Type toGenericType, Cause cause) {
        return new BigDecimal(String.valueOf(from));
    }

    @Override
    public Class<?>[] fromType() {
        return FROM;
    }

    @Override
    public Class<?>[] toType() {
        return TO;
    }

    @Override
    public int priority() {
        //Lowest priority
        return Integer.MAX_VALUE;
    }

}
