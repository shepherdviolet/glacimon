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

import java.util.Map;

/**
 * [Main processor] Map -> Bean, thread-safe
 *
 * @author S.Violet
 * @see MapXBean
 */
public interface MapToBeanConverter {

    /**
     * <p>Map -> Bean</p><br>
     *
     * <p>For the scene from 'Map consisting of Map and Collection nesting' to 'Java Bean'. To make the process easier
     * and clearer, 'Bean' elements in Map are considered basic data types, we does not perform Bean-to-Bean field
     * mapping. Because it is difficult to determine whether an object is a Bean or not, Bean-to-Bean can lead to
     * unexpected errors. </p><br>
     *
     * <p>Support the following situations while recursive processing: </p><br>
     *
     * <p>1) Collection to Collection : Create new collection and add values, extension point 'MxbCollectionMapper'</p>
     * <p>2) Map to Map : Create new map and put values, extension point 'MxbCollectionMapper'</p>
     * <p>3) Type mapping : Supports conversions such as 'date' / 'number' by default, extend MxbTypeMapperProvider to add type mappers</p>
     * <p>4) Map to Bean : Convert Map to 'JavaBean'</p><br>
     *
     * <p>NOT support the following situations while recursive processing: </p><br>
     *
     * <p>1) Bean -> Bean : Does not perform Bean-to-Bean field mapping if types not match, set null or throw Exception,
     * unless you provide the corresponding type mapper (MxbTypeMapper). </p>
     */
    <T> T convert(Map<String, Object> fromMap, Class<T> toType);

    /**
     * <p>Map -> Bean</p><br>
     *
     * <p>For the scene from 'Map consisting of Map and Collection nesting' to 'Java Bean'. To make the process easier
     * and clearer, 'Bean' elements in Map are considered basic data types, we does not perform Bean-to-Bean field
     * mapping. Because it is difficult to determine whether an object is a Bean or not, Bean-to-Bean can lead to
     * unexpected errors. </p><br>
     *
     * <p>Support the following situations while recursive processing: </p><br>
     *
     * <p>1) Collection to Collection : Create new collection and add values, extension point 'MxbCollectionMapper'</p>
     * <p>2) Map to Map : Create new map and put values, extension point 'MxbCollectionMapper'</p>
     * <p>3) Type mapping : Supports conversions such as 'date' / 'number' by default, extend MxbTypeMapperProvider to add type mappers</p>
     * <p>4) Map to Bean : Convert Map to 'JavaBean'</p><br>
     *
     * <p>NOT support the following situations while recursive processing: </p><br>
     *
     * <p>1) Bean -> Bean : Does not perform Bean-to-Bean field mapping if types not match, set null or throw Exception,
     * unless you provide the corresponding type mapper (MxbTypeMapper). </p>
     */
    void convert(Map<String, Object> fromMap, Object toBean);

}
