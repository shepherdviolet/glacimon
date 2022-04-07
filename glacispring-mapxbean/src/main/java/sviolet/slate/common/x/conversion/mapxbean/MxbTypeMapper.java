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

package sviolet.slate.common.x.conversion.mapxbean;

import java.lang.reflect.Type;

/**
 * <p>[Common Handler] Type mapper</p>
 *
 * <p>Purpose: Declare what types it can accept, and then implement type conversion logic</p>
 *
 * <p>MxbTypeMappers are added by {@link MxbTypeMapperProvider} extension point, and managed (invoked) by
 * {@link MxbTypeMapperCenter}. If you need to add a new mapper, implement this interface first, and then add it by
 * {@link MxbTypeMapperProvider} extension point. See: {@link MxbTypeMapperProvider} {@link MxbTypeMapperProviderImpl}.
 * Glaciion doc: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md</p>
 *
 * @author S.Violet
 * @see MapXBean
 * @see MxbTypeMapperCenter
 * @see MxbTypeMapperProvider
 */
public interface MxbTypeMapper {

    /**
     * Converts data from source type to destination type
     *
     * @param from From this object (Source type)
     * @param toType To this type (Destination type)
     * @param toGenericType To this generic type (Destination type)
     * @param cause Reason for conversion
     * @return Converted object, 1> Failed: throw Exception
     */
    Object map(Object from, Class<?> toType, Type toGenericType, Cause cause) throws Exception;

    /**
     * @return Accepted source types (Convert from those types)
     */
    Class<?>[] fromType();

    /**
     * @return Accepted destination types (Convert to those types)
     */
    Class<?>[] toType();

    /**
     * @return Mapper's priority. The smaller the number, the higher the priority.
     * High priority mapper takes effect when the acceptance type is the same.
     */
    int priority();

    /**
     * Reason for conversion
     */
    enum Cause {
        /**
         * BEAN_TO_BEAN
         */
        BEAN_TO_BEAN,

        /**
         * MAP_TO_BEAN
         */
        MAP_TO_BEAN
    }

}
