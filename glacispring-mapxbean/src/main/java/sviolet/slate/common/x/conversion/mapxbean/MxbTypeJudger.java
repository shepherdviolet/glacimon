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

/**
 * <p>[Common Handler] Type judger</p>
 *
 * <p>Purpose: Determine what type a class is</p>
 *
 * <p>Glaciion Extension point. Doc: https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md</p>
 *
 * @author S.Violet
 * @see MapXBean
 * @see MxbTypeJudgerImpl
 */
@SingleServiceInterface
public interface MxbTypeJudger {

    /**
     * Determine if a class is indivisible type
     *
     * @param clazz class
     * @return true: Is indivisible
     */
    boolean isIndivisible(Class<?> clazz);

    /**
     * Determine if a class is a 'Bean (Java Bean)'.
     *
     * WARNING! if readable == false and writable == false, the result is always true! So at least one of the two flags
     * is true.
     *
     * @param clazz class
     * @param readable true: At least one read method (getter)
     * @param writable true: At least one write method (setter)
     * @return true: Is Bean
     */
    boolean isBean(Class<?> clazz, boolean readable, boolean writable);

}
