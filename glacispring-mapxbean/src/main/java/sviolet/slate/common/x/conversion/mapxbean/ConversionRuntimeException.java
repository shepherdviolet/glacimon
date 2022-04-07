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

/**
 * MapXBean conversion exception
 *
 * @author S.Violet
 * @see MapXBean
 */
public class ConversionRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1014117760954793998L;

    private ConversionPath conversionPath;

    /**
     * @param message Error message
     * @param cause Error cause, Nullable
     * @param conversionPath It represents the conversion path when the exception is thrown
     */
    public ConversionRuntimeException(String message, Throwable cause, ConversionPath conversionPath) {
        super(message + ", conversion-path: " + conversionPath, cause);
    }

    /**
     * It represents the conversion path when the exception is thrown, Nullable
     */
    public ConversionPath getConversionPath() {
        return conversionPath;
    }
}