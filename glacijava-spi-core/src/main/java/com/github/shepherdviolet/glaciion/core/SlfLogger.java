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

package com.github.shepherdviolet.glaciion.core;

import com.github.shepherdviolet.glaciion.Glaciion;
import com.github.shepherdviolet.glaciion.api.interfaces.SpiLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger for slf4j
 *
 * @author S.Violet
 */
class SlfLogger implements SpiLogger {

    private static final Logger logger = LoggerFactory.getLogger(Glaciion.class);

    @Override
    public void trace(String msg, Throwable t) {
        if (t == null) {
            logger.trace(msg);
        } else {
            logger.trace(msg, t);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (t == null) {
            logger.debug(msg);
        } else {
            logger.debug(msg, t);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (t == null) {
            logger.info(msg);
        } else {
            logger.info(msg, t);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (t == null) {
            logger.warn(msg);
        } else {
            logger.warn(msg, t);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (t == null) {
            logger.error(msg);
        } else {
            logger.error(msg, t);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

}
