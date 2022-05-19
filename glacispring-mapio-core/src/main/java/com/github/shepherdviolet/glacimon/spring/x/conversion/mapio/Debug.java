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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio;

/**
 * 调试使用
 *
 * @author shepherdviolet
 */
public class Debug {

    private static boolean errorLogEnabled = true;
    private static boolean infoLogEnabled = true;
    private static boolean traceLogEnabled = false;

    private static final ThreadLocal<StringBuilder> errorTrace = new ThreadLocal<>();

    public static boolean isErrorLogEnabled() {
        return errorLogEnabled;
    }

    /**
     * 是否开启映射过程中的错误日志, 默认开启, 日志级别ERROR,
     * 日志包路径: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio
     */
    public static void setErrorLogEnabled(boolean errorLogEnabled) {
        Debug.errorLogEnabled = errorLogEnabled;
    }

    public static boolean isInfoLogEnabled() {
        return infoLogEnabled;
    }

    /**
     * 是否开启映射过程中的信息日志, 默认开启, 日志级别INFO,
     * 日志包路径: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio
     */
    public static void setInfoLogEnabled(boolean infoLogEnabled) {
        Debug.infoLogEnabled = infoLogEnabled;
    }

    public static boolean isTraceLogEnabled() {
        return traceLogEnabled;
    }

    /**
     * 是否开启映射过程中的追踪日志, 默认关闭, 日志级别TRACE
     * 日志包路径: com.github.shepherdviolet.glacimon.spring.x.conversion.mapio
     */
    public static void setTraceLogEnabled(boolean traceLogEnabled) {
        Debug.traceLogEnabled = traceLogEnabled;
    }

    static void addErrorTrace(String errorTraceMsg) {
        StringBuilder stringBuilder = errorTrace.get();
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
            errorTrace.set(stringBuilder);
        }
        stringBuilder.append(errorTraceMsg);
    }

    static String getAndRemoveErrorTrace(){
        StringBuilder stringBuilder = errorTrace.get();
        if (stringBuilder != null) {
            errorTrace.set(null);
            return stringBuilder.toString();
        }
        return "";
    }

}
