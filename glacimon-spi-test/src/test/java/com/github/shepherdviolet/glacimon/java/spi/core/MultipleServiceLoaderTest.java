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
import com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for class SingleServiceLoader
 *
 * @author shepherdviolet
 */
public class MultipleServiceLoaderTest extends AbstractTest {

    @Test
    public void getService(){
        SamplePlugin plugin = GlacimonSpi.loadMultipleService(SamplePlugin.class).get("default");
        assertEquals(
                "666",
                plugin.test());
    }

    @Test
    public void getServices(){
        List<SamplePlugin> plugins = GlacimonSpi.loadMultipleService(SamplePlugin.class).getAll();
//        StringBuilder
    }

}
