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

package com.github.shepherdviolet.glacimon.java.spi.api.interfaces;

/**
 * Implements this interface to print logs, and add VM option -Dglacimonspi.conf.custom.logger to specify implementation class
 *
 * @author shepherdviolet
 */
public interface SpiLogger {

    /**
     * trace
     * @param msg msg
     * @param t t
     */
    void trace(String msg, Throwable t);

    /**
     * debug
     * @param msg msg
     * @param t t
     */
    void debug(String msg, Throwable t);

    /**
     * info
     * @param msg msg
     * @param t t
     */
    void info(String msg, Throwable t);

    /**
     * warn
     * @param msg msg
     * @param t t
     */
    void warn(String msg, Throwable t);

    /**
     * error
     * @param msg msg
     * @param t t
     */
    void error(String msg, Throwable t);

    /**
     * is trace enabled
     * @return is trace enabled
     */
    boolean isTraceEnabled();

    /**
     * is debug enabled
     * @return is trace enabled
     */
    boolean isDebugEnabled();

    /**
     * is info enabled
     * @return is trace enabled
     */
    boolean isInfoEnabled();

    /**
     * is warn enabled
     * @return is trace enabled
     */
    boolean isWarnEnabled();

    /**
     * is error enabled
     * @return is trace enabled
     */
    boolean isErrorEnabled();

}
