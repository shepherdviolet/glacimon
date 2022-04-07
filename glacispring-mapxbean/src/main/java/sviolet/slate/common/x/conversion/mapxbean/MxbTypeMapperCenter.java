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

import com.github.shepherdviolet.glaciion.api.annotation.SingleServiceInterface;

import java.lang.reflect.Type;

/**
 * <p>[Common Handler] Type mapping center</p>
 *
 * <p>Purpose: Mapping type to type with MxbTypeMapper (provided from MxbTypeMapperProvider)</p>
 *
 * <p>Glaciion Extension point. Doc: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md</p>
 *
 * @author S.Violet
 * @see MapXBean
 * @see MxbTypeMapperCenterImpl
 */
@SingleServiceInterface
public interface MxbTypeMapperCenter {

    /**
     * Return this means it is no proper mapper for given types, need to be handled in other ways
     */
    public static final Object RESULT_NO_PROPER_MAPPER = new Object();

    /**
     * Mapping type to type with MxbTypeMapper (provided from MxbTypeMapperProvider)
     *
     * @param from From this object (Source type)
     * @param toType To this type (Destination type)
     * @param toGenericType To this generic type (Destination type)
     * @param cause Reason for conversion
     * @return Converted object, 1> Failed: throw ConversionRuntimeException, 2> No proper mapper for given types: return MxbTypeMapperCenter.RESULT_NO_PROPER_MAPPER
     */
    Object onConvert(Object from, Class<?> toType, Type toGenericType, MxbTypeMapper.Cause cause) throws Exception;

}
