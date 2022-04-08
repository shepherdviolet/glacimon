/*
 * Copyright (C) 2022-2022 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapxbean;

import com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi;

/**
 * Internal constants and components
 *
 * @author shepherdviolet
 */
class MxbConstants {

    // Common
    static final MxbObjectInstantiator OBJECT_INSTANTIATOR = GlacimonSpi.loadSingleService(MxbObjectInstantiator.class).get();
    static final MxbTypeMapperCenter TYPE_MAPPER_CENTER = GlacimonSpi.loadSingleService(MxbTypeMapperCenter.class).get();
    static final MxbTypeJudger TYPE_JUDGER = GlacimonSpi.loadSingleService(MxbTypeJudger.class).get();

    // Map -> Bean only
    static final MxbCollectionMapper MTB_COLLECTION_MAPPER = GlacimonSpi.loadSingleService(MxbCollectionMapper.class).get();

}
