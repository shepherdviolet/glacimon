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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

/**
 * Abstract test
 *
 * @author shepherdviolet
 */
public abstract class AbstractTest {

    /**
     * init before test
     */
    @BeforeAll // 在所有Test方法前运行一次, BeforeEach在每个Test方法前运行
    public void init(){
        //Enable mockito annotations
        MockitoAnnotations.initMocks(this);
        //Set log level
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.github.shepherdviolet.glacimon.java.spi").setLevel(Level.TRACE);
        //Invoke prepare
        prepare();
    }

    /**
     * prepare
     */
    protected void prepare() {

    }

}
