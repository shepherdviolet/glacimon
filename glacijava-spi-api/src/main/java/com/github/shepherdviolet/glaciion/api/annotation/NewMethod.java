/*
 * Copyright (C) 2019-2019 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glaciion
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glaciion.api.annotation;

import com.github.shepherdviolet.glaciion.api.interfaces.CompatibleApproach;

import java.lang.annotation.*;

/**
 * Marked on the method of interface, indicates that this method is new (Added in newer version).
 * And you should provide a compatible approach for old version.
 *
 * @author S.Violet
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface NewMethod {

    /**
     * The old version implementation class does not implements the method. So you need to provide a compatible approach.
     * The class should implements {@link com.github.shepherdviolet.glaciion.api.interfaces.CompatibleApproach}.
     * Set to {@link com.github.shepherdviolet.glaciion.api.compat.DoNothing} if you simply let it do nothing and return null.
     */
    Class<? extends CompatibleApproach> compatibleApproach();

}
