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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.common;

import java.lang.management.ManagementFactory;

/**
 * 环境工具
 */
public class EnvironmentUtils {

    /**
     * 当前进程PID
     */
    public static final String PID;

    static {
        String pid;
        try {
            String mxBeanName = ManagementFactory.getRuntimeMXBean().getName();
            pid = mxBeanName.split("@")[0];
        } catch (Exception ignored) {
            pid = "";
        }
        PID = pid;
    }

}
