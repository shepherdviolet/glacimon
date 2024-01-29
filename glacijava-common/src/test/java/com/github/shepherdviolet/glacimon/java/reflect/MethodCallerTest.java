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

package com.github.shepherdviolet.glacimon.java.reflect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MethodCallerTest {

    @Test
    public void getMethodCaller(){
        Assertions.assertEquals("MethodCaller{callerClass='sun.reflect.NativeMethodAccessorImpl', callerMethodName='invoke0'}",
                String.valueOf(MethodCaller.getMethodCaller(null, null)));
    }

}
