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

import com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi;
import com.github.shepherdviolet.glacimon.java.spi.test.CompatService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for interface compat
 *
 * @author shepherdviolet
 */
public class CompatTest {

    @Test
    public void compat(){
        CompatService oldService = GlacimonSpi.loadMultipleService(CompatService.class).get("old");
        CompatService newService = GlacimonSpi.loadMultipleService(CompatService.class).get("new");
        assertEquals(
                "value1 handled by old version",
                oldService.oldMethod("value1"));
        assertNull(
                oldService.newMethod("value2"));
        assertEquals(
                "value3value4 handled by old version",
                oldService.newMethod("value3", "value4"));
        assertEquals(
                "value5 handle by new version",
                newService.oldMethod("value5"));
        assertEquals(
                "value6 handle by newMethod1",
                newService.newMethod("value6"));
        assertEquals(
                "value7value8 handle by newMethod2",
                newService.newMethod("value7", "value8"));
    }

}
