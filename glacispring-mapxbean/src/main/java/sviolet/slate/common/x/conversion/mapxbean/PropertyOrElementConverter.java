/*
 * Copyright (C) 2015-2020 S.Violet
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
 * <p>[Sub Processor] Property or element converter</p>
 *
 * <p>Convert property to expected type (properties of Bean / elements of Collection).
 * Implemented by anonymous in *ConverterImpl. For property (element) conversions in sub processor</p>
 *
 * @author S.Violet
 * @see MapXBean
 * @see MapToBeanConverterImpl
 * @see BeanToMapConverterImpl
 */
public interface PropertyOrElementConverter {

    /**
     * <p>Convert property to expected type (properties of Bean / elements of Collection).
     * Implemented by anonymous in *ConverterImpl. For property (element) conversions in sub processor</p>
     *
     * @param value From this object (Source type)
     * @param valueClass From this type (Source type)
     * @param expectClass To this type (Destination type)
     * @param expectType To this generic type (Destination type)
     * @param conversionPath It represents the conversion path when the exception is thrown, Nullable
     * @return Converted object, 1> Failed: throw Exception
     */
    Object convert(Object value, Class<?> valueClass, Class<?> expectClass, Type expectType, ConversionPath conversionPath) throws Exception;

}
