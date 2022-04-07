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

package com.github.shepherdviolet.glaciion.api.exceptions;

/**
 * Glaciion: Illegal implementation class
 *
 * @author S.Violet
 */
public class IllegalImplementationException extends RuntimeException {

    private static final long serialVersionUID = 1011600639314300582L;

    public IllegalImplementationException(String message) {
        super(message);
    }

    public IllegalImplementationException(String message, Throwable cause) {
        super(message, cause);
    }

}
