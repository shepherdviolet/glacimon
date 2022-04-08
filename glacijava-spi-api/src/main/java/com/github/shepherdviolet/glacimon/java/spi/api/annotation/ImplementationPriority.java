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

package com.github.shepherdviolet.glacimon.java.spi.api.annotation;

import java.lang.annotation.*;

/**
 * Marked on the implementation class, specifying the priority. Only for multiple-service mode.
 *
 * @author shepherdviolet
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ImplementationPriority {

    /**
     * [Only for multiple-service mode]
     * Specify the priority of implementation. The larger the number, the higher the priority.
     * The highest priority implementation will be placed at the top of the instance list.
     * When the two implementations have the same priority, they are sorted according to the hash value of the class name.
     * If not set, the priority defaults to 0.
     */
    int value();

}