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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logger for System.out
 *
 * @author S.Violet
 */
class SystemLogger implements SpiLogger {

    private static final String GLACIMON_SPI = " com.github.shepherdviolet.glacimon.java.spi ";

    private static final int TRACE = 5;
    private static final int DEBUG = 4;
    private static final int INFO = 3;
    private static final int WARN = 2;
    private static final int ERROR = 1;
    private static final int OFF = 0;

    private final String timeFormat;
    private final int logLevel;
    private final ThreadLocal<SimpleDateFormat> formats = new ThreadLocal<>();

    SystemLogger() {
        String logLevelStr = System.getProperty(Constants.VMOPT_SYSTEM_LOGLEVEL, "OFF").toUpperCase();
        switch (logLevelStr) {
            case "TRACE":
                logLevel = TRACE;
                break;
            case "DEBUG":
                logLevel = DEBUG;
                break;
            case "INFO":
                logLevel = INFO;
                break;
            case "WARN":
                logLevel = WARN;
                break;
            case "ERROR":
                logLevel = ERROR;
                break;
            case "OFF":
            default:
                logLevel = OFF;
                break;
        }

        timeFormat = System.getProperty(Constants.VMOPT_SYSTEM_LOGTIME, "yyyy-MM-dd HH:mm:ss.SSS");
        String time = new SimpleDateFormat(timeFormat, Locale.getDefault()).format(new Date());
        debug("SystemLogger enabled, level:" + logLevelStr + ", " + time, null);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (logLevel >= TRACE) {
            System.out.println(getTimeString() + GLACIMON_SPI + msg);
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (logLevel >= DEBUG) {
            System.out.println(getTimeString() + GLACIMON_SPI + msg);
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (logLevel >= INFO) {
            System.out.println(getTimeString() + GLACIMON_SPI + msg);
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (logLevel >= WARN) {
            System.out.println(getTimeString() + GLACIMON_SPI + msg);
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (logLevel >= ERROR) {
            System.out.println(getTimeString() + GLACIMON_SPI + msg);
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return logLevel >= TRACE;
    }

    @Override
    public boolean isDebugEnabled() {
        return logLevel >= DEBUG;
    }

    @Override
    public boolean isInfoEnabled() {
        return logLevel >= INFO;
    }

    @Override
    public boolean isWarnEnabled() {
        return logLevel >= WARN;
    }

    @Override
    public boolean isErrorEnabled() {
        return logLevel >= ERROR;
    }

    private String getTimeString(){
        SimpleDateFormat format = formats.get();
        if (format == null) {
            format = new SimpleDateFormat(timeFormat, Locale.getDefault());
            formats.set(format);
        }
        return format.format(new Date());
    }

}
