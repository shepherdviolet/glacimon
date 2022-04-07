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

import sviolet.slate.common.x.conversion.mapxbean.strategy.InflateUntilIndivisible;

/**
 * MapXBean
 *
 * @author S.Violet
 */
public class MapXBean {

    /**
     * <p>Bean -> Map, The BeanToMapConverter is thread-safe</p><br>
     *
     * <p>In the scene of Bean -> Map. While a Bean is converting to a Map, all the properties of Bean will keep the
     * original type by default, unless {@link BeanToMapInflateStrategy} tells the program that it needs to be inflated
     * (this method returns true).
     * 'Inflate' means that in the scene of Bean -> Map, if a property (of Java Bean) or an element (of Collection) is
     * a Java Bean (judged by {@link MxbTypeJudger#isBean}), the property (or element) can be converted to a Map as long
     * as the method {@link BeanToMapInflateStrategy#needToBeInflated} returns true. The process of converting property
     * (or element) to Map is called 'Inflate'.</p><br>
     *
     * <p>Usages:</p><br>
     *
     * <p>1) If there is no {@link BeanToMapInflateStrategy}, Bean's properties will be put directly to a Map, which is
     * equivalent to shallow cloning.</p><br>
     *
     * <p>2) {@link InflateUntilIndivisible} can help you converting a 'Java Bean' to 'Map consisting of Map and
     * Collection nesting', all the properties or elements will be inflate until indivisible
     * (Decide by {@link InflateUntilIndivisible}).</p><br>
     *
     * <p>3) You can customize a {@link BeanToMapInflateStrategy}, to decide whether to continue inflating property to Map.
     * Notice that the property must be a Java Bean with read methods (Judged by {@link MxbTypeJudger#isBean}).</p><br>
     *
     * <p>Support the following situations while recursive processing: </p><br>
     *
     * <p>1) Collection to Collection : Create new collection and add values, extension point 'MxbCollectionMapper'</p>
     * <p>2) Map to Map : Create new map and put values, extension point 'MxbCollectionMapper'</p>
     * <p>3) Bean to Map : While the Bean is converting to Map, the properties will keep the original type (by default),
     * or to be inflated to Map (Decide by {@link BeanToMapInflateStrategy}).</p><br>
     *
     * <p>NOT support the following situations while recursive processing: </p><br>
     *
     * <p>1) Bean -> Bean : While the Bean is converting to Map, NO Bean to Bean case.</p>
     * <p>2) Type mapping : While the Bean is converting to Map, NO type mapping case.</p><br>
     */
    public static BeanToMapConverterImpl.Builder beanToMap(){
        return new BeanToMapConverterImpl.Builder();
    }

    /**
     * <p>Map -> Bean, The MapToBeanConverter is thread-safe</p><br>
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
    public static MapToBeanConverterImpl.Builder mapToBean(){
        return new MapToBeanConverterImpl.Builder();
    }

}
