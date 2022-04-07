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

package sviolet.slate.common.helper.jsch;

import com.jcraft.jsch.JSch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSch助手, 进行了一些公共配置, 依赖: com.jcraft:jsch
 *
 * @author S.Violet
 */
public class JschHelper extends JSch {

    private static Logger logger = LoggerFactory.getLogger(JschHelper.class);

    static {

        //参数配置
        JSch.setConfig("StrictHostKeyChecking", "no");

        //日志配置
        JSch.setLogger(new com.jcraft.jsch.Logger() {
            @Override
            public boolean isEnabled(int level) {
                return true;
            }
            @Override
            public void log(int level, String message) {
                switch (level) {
                    case INFO:
                        logger.info(message);
                        break;
                    case WARN:
                        logger.warn(message);
                        break;
                    case ERROR:
                        logger.error(message);
                        break;
                    case FATAL:
                        logger.error(message);
                        break;
                    case DEBUG:
                    default:
                        logger.debug(message);
                        break;
                }
            }
        });

    }

}
