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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logger is written to memory
 *
 * @author shepherdviolet
 */
class MemLogger implements SpiLogger {

    private static final int LOG_MAX_LENGTH = 4 * 1024 * 1024;

    private static final int TRACE = 5;
    private static final int DEBUG = 4;
    private static final int INFO = 3;
    private static final int WARN = 2;
    private static final int ERROR = 1;
    private static final int OFF = 0;

    // 用List而不用StringBuffer的原因是, 便于MAT等Heap查看工具观察日志 (StringBuffer必须导出value到文件才能看)
    private final List<String> MEM_LOGS = new ArrayList<>();
    private final AtomicInteger logLength = new AtomicInteger(0);

    private final int logLevel;
    private final String timeFormat = "yyyy-MM-dd HH:mm:ss.SSS ";
    private final ThreadLocal<SimpleDateFormat> formats = new ThreadLocal<>();

    MemLogger() {
        String logLevelStr = System.getProperty(Constants.VMOPT_MEM_LOGLEVEL, "DEBUG").toUpperCase();
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

        String time = new SimpleDateFormat(timeFormat, Locale.getDefault()).format(new Date());
        info("MemLogger enabled, level:" + logLevelStr + ", " + time, null);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (logLevel >= TRACE) {
            printLog(msg);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (logLevel >= DEBUG) {
            printLog(msg);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (logLevel >= INFO) {
            printLog(msg);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (logLevel >= WARN) {
            printLog(msg);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (logLevel >= ERROR) {
            printLog(msg);
        }
    }

    private void printLog(String msg) {
        if (msg == null) {
            return;
        }
        int len = timeFormat.length() + msg.length();
        // 限制最大长度
        if (logLength.addAndGet(len) > LOG_MAX_LENGTH) {
            logLength.getAndAdd(-len);
            return;
        }
        MEM_LOGS.add(getTimeString() + msg);
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
