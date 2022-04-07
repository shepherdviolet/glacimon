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

/**
 * Exception collector.
 * Only valid when throwExceptionIfFails = false (that is, "error skip" mode).
 * Used to collect exceptions that were ignored during the conversion process (can be used to print logs and troubleshooting).
 *
 * @author S.Violet
 * @see MapXBean
 */
public interface ConversionExceptionCollector {

    /**
     * Before conversion,
     * Be careful, don't throw exceptions here.
     */
    void onStart(Object from, Class<?> toType);

    /**
     * Catch the exceptions,
     * Be careful, don't throw exceptions here.
     */
    void onException(Throwable t);

    /**
     * After conversion,
     * Be careful, don't throw exceptions here.
     */
    void onFinish(Object from, Class<?> toType);

}
