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
import com.github.shepherdviolet.glaciion.test.CompatService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for interface compat
 *
 * @author S.Violet
 */
public class CompatTest {

    @Test
    public void compat(){
        CompatService oldService = Glaciion.loadMultipleService(CompatService.class).get("old");
        CompatService newService = Glaciion.loadMultipleService(CompatService.class).get("new");
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
