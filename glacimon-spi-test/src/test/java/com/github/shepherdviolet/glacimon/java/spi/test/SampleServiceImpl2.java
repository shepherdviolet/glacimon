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

package com.github.shepherdviolet.glacimon.java.spi.test;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.PropertyInject;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.CloseableImplementation;
import com.github.shepherdviolet.glacimon.java.spi.api.interfaces.InitializableImplementation;

import java.util.concurrent.atomic.AtomicBoolean;

public class SampleServiceImpl2 implements SampleService, InitializableImplementation, CloseableImplementation {

    @PropertyInject
    private String dateFormat;

    private boolean logEnabled;

    private AtomicBoolean closed;

    @PropertyInject
    public void setLogEnabled(boolean logEnabled){
        this.logEnabled = logEnabled;
    }

    @Override
    public String test() {
        if (closed != null && closed.get()) {
            //closed
            return "closed";
        } else {
            return dateFormat + logEnabled;
        }
    }

    @Override
    public void setCloseFlag(AtomicBoolean closed) {
        this.closed = closed;
    }

    @Override
    public void onServiceCreated() {

    }
}
