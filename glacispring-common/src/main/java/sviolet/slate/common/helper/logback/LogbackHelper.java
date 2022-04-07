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

package sviolet.slate.common.helper.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logback帮助类, 依赖: ch.qos.logback:logback-classic
 *
 * @author S.Violet
 */
public class LogbackHelper {

    private static Logger logger = LoggerFactory.getLogger(LogbackHelper.class);

    /**
     * 全局修改某个包名的日志级别
     * @param packagePath 包名
     * @param level 日志级别
     * @return true:修改成功
     */
    public static boolean setLevel(String packagePath, Level level){
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getLogger(packagePath).setLevel(level);
            return true;
        } catch (Throwable t) {
            logger.error("Error while setting Logback's level, package:" + packagePath + ", level:" + level);
            return false;
        }
    }

}
