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

package sviolet.slate.common.x.conversion.mapxbean.strategy;

import sviolet.slate.common.x.conversion.mapxbean.BeanToMapInflateStrategy;
import sviolet.slate.common.x.conversion.mapxbean.ConversionPath;
import sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudger;

import java.util.Collection;
import java.util.Map;

/**
 * <p>[Inflate Strategy] [For Bean -> Map] Inflate the elements of Root node's Collection property (including Map).
 * root->**(It's Collection or Map)->**(Inflate these)</p><br>
 *
 * <p>NOTE that the property must be a Java Bean, because the program has already determined whether it is a JavaBean
 * before calling this interface. (Judged by built-in component {@link MxbTypeJudger#isBean})</p><br>
 *
 * <p>References:</p><br>
 *
 * <p>In the scene of Bean -> Map. While a Bean is converting to a Map, all the properties of Bean will keep the
 * original type by default, unless {@link BeanToMapInflateStrategy} tells the program that it needs to be inflated
 * (this method returns true).
 * 'Inflate' means that in the scene of Bean -> Map, if a property (of Java Bean) or an element (of Collection) is
 * a Java Bean (judged by {@link MxbTypeJudger#isBean}), the property (or element) can be converted to a Map as long
 * as the method {@link BeanToMapInflateStrategy#needToBeInflated} returns true. The process of converting property
 * (or element) to Map is called 'Inflate'.</p><br>
 *
 * @author S.Violet
 * @see sviolet.slate.common.x.conversion.mapxbean.MapXBean
 * @see BeanToMapInflateStrategy
 */
public class InflateCollectionElements implements BeanToMapInflateStrategy {

    /**
     * @inheritDoc
     */
    @Override
    public boolean needToBeInflated(Object value, Class<?> valueClass, MxbTypeJudger typeJudger, ConversionPath conversionPath) {
        // root->**(It's Collection or Map)->**(Inflate these)
        if (conversionPath.depth() != 2) {
            return false;
        }
        // Parent node is Collection or Map
        ConversionPath parentNode = conversionPath.parentNode();
        if (parentNode != null) {
            if (Collection.class.isAssignableFrom(parentNode.fromClass()) ||
                    Map.class.isAssignableFrom(parentNode.fromClass())){
                return true;
            }
        }
        return false;
    }

}
