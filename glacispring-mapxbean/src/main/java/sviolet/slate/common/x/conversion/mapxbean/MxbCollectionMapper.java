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

import com.github.shepherdviolet.glaciion.api.annotation.SingleServiceInterface;

import java.lang.reflect.Type;

/**
 * <p>[Sub Processor] Collection converter (including Map)</p>
 *
 * <p>Purpose: Determine whether it is a collection to collection scenes, and then implement collection conversion logic.
 * Notice that Map is collection here</p>
 *
 * <p>Glaciion Extension point. Doc: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md</p>
 *
 * @author S.Violet
 * @see MapXBean
 * @see MxbCollectionMapperImpl
 */
@SingleServiceInterface
public interface MxbCollectionMapper {

    /**
     * Return this means it is not Collection to Collection (including Map) scenes, need to be handled in other ways
     */
    public static final Object RESULT_NOT_COLLECTION_TO_COLLECTION = new Object();

    /**
     * <p>Determine whether it is a collection to collection scenes, and then implement collection conversion logic.
     * Notice that Map is collection here</p>
     *
     * @param from From this object (Source type)
     * @param fromClass From this type (Source type)
     * @param toClass To this type (Destination type)
     * @param toType To this generic type (Destination type)
     * @param toUniversalCollection true: to ArrayList / linkedHashSet / LinkedHashMap... , false: to Collection which is specified
     * @param elementConverter Use it to handle element conversion (Convert element of collection), see {@link MxbCollectionMapperImpl}
     * @param objectInstantiator Use it to create instance of any class
     * @param exceptionThrower Use it to throw Exception
     * @param conversionPath It represents the conversion path when the exception is thrown, Nullable
     * @return Converted object, 1> Failed: throw Exception by 'exceptionThrower', 2> NOT Collection to Collection scenes: return MxbCollectionMapper.RESULT_NOT_COLLECTION_TO_COLLECTION
     */
    Object onConvert(Object from,
                     Class<?> fromClass,
                     Class<?> toClass,
                     Type toType,
                     boolean toUniversalCollection,
                     PropertyOrElementConverter elementConverter,
                     MxbObjectInstantiator objectInstantiator,
                     ConversionExceptionThrower exceptionThrower,
                     ConversionPath conversionPath) throws Exception;

}
