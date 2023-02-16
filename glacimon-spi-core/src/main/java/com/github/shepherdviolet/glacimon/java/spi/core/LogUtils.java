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

package com.github.shepherdviolet.glacimon.java.spi.core;

import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.SpiLogger;

class LogUtils {

    //system logger

    private static final String SYSTEM_LOGGER = "com.github.shepherdviolet.glacimon.java.spi.core.SystemLogger";

    //slf4j logger

    private static final String SLF4J_LOGGER = "com.github.shepherdviolet.glacimon.java.spi.core.SlfLogger";
    private static final String SLF4J_FLAG = "org.slf4j.Logger";

    private static final SpiLogger LOGGER;

    static {
        String loggerClassName = SYSTEM_LOGGER;
        //check slf4j
        if (ClassUtils.loadClassSafety(SLF4J_FLAG, ClassUtils.getCurrentClassLoader()) != null) {
            loggerClassName = SLF4J_LOGGER;
        }
        //load logger
        LOGGER = ClassUtils.loadInternalComponent(Constants.VMOPT_CUSTOM_LOGGER, SpiLogger.class, loggerClassName);
    }

    static SpiLogger getLogger(){
        return LOGGER;
    }

}
