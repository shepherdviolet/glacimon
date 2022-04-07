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

import com.github.shepherdviolet.glaciion.api.annotation.MultipleServiceInterface;

import java.util.List;

/**
 * <p>[Common Handler] MxbTypeMapper provider. </p>
 *
 * <p>Purpose: Provide type mappers to MxbTypeMapperCenter. Globally. </p>
 *
 * <p>Glaciion Extension point. Doc: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md</p>
 *
 * @author S.Violet
 * @see MapXBean
 * @see MxbTypeMapperProviderImpl
 */
@MultipleServiceInterface
public interface MxbTypeMapperProvider {

    /**
     * The type mappers returned here will be added to MxbTypeMapperCenter.
     * High priority mapper takes effect when the acceptance type is the same.
     */
    List<MxbTypeMapper> getTypeMappers();

}
