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

class Constants {

    //environment

    static final boolean ENV_SPRING = ClassUtils.loadClassSafety("org.springframework.context.ApplicationContext", ClassUtils.getCurrentClassLoader()) != null;

    //log

    static final String LOG_HOME_PAGE = "https://github.com/shepherdviolet/glacimon";
    static final String LOG_GREET = "? | Java SPI Library. Home Page: " + LOG_HOME_PAGE;

    //path

    static final String PATH_ROOT = "META-INF/glacimonspi/";
    static final String PATH_INTERFACES = PATH_ROOT + "interfaces";
    static final String PATH_SINGLE_SERVICE = PATH_ROOT + "services/single/";
    static final String PATH_MULTIPLE_SERVICE = PATH_ROOT + "services/multiple/";
    static final String PATH_PROPERTIES = PATH_ROOT + "properties/";

    //vm option : config

    static final String VMOPT_DEFAULT_CLASSLOADER_FROMCONTEXT = "glacimonspi.conf.defcl.fromctx";
    static final boolean FLAG_DEFAULT_CLASSLOADER_FROMCONTEXT = "true".equals(System.getProperty(VMOPT_DEFAULT_CLASSLOADER_FROMCONTEXT, "true"));

    static final String VMOPT_SYSTEM_LOGLEVEL = "glacimonspi.conf.system.loglevel";
    static final String VMOPT_SYSTEM_LOGTIME = "glacimonspi.conf.system.logtime";
    static final String VMOPT_CUSTOM_LOGGER = "glacimonspi.conf.custom.logger";

    static final String VMOPT_PRELOAD_AUTO = "glacimonspi.conf.preload.auto";
    static final boolean FLAG_PRELOAD_AUTO = "true".equals(System.getProperty(VMOPT_PRELOAD_AUTO, ENV_SPRING ? "true" : "false"));

    static final String VMOPT_PRELOAD_CHECKSUM = "glacimonspi.conf.preload.checksum";
    static final boolean FLAG_PRELOAD_CHECKSUM = "true".equals(System.getProperty(VMOPT_PRELOAD_CHECKSUM, "true"));

    //vm option : definition

    static final String VMOPT_EXCLUDE_FILE = "glacimonspi.exclude.file";
    static final String VMOPT_SELECT = "glacimonspi.select.";
    static final String VMOPT_REMOVE = "glacimonspi.remove.";
    static final String VMOPT_PROPERTY = "glacimonspi.property.";

    //special property key

    static final String PROPERTY_PRIORITY = "@priority";

}
