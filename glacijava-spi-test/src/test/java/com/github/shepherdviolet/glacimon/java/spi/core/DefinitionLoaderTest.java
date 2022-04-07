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

import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for class SingleServiceLoader
 *
 * @author S.Violet
 */
public class DefinitionLoaderTest extends AbstractTest {

    @Test
    public void loadSingle() {
        List<SingleDefinition> result = DefinitionLoader.loadSingleDefinitions(
                "com.github.shepherdviolet.glacimon.java.spi.test.SampleService",
                ClassUtils.getDefaultClassLoader(),
                "0");
        Collections.sort(result, new Comparator<SingleDefinition>() {
            @Override
            public int compare(SingleDefinition o1, SingleDefinition o2) {
                return o1.toString().hashCode() - o2.toString().hashCode();
            }
        });
        assertEquals("[" +
                        "SingleDefinition{interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SampleService', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SampleServiceImpl1', priority=0}, " +
                        "SingleDefinition{interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SampleService', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SampleServiceImpl2', priority=1}" +
                        "]",
                String.valueOf(result));
    }

    @Test
    public void loadMultiple() {
        List<MultipleDefinition> result = DefinitionLoader.loadMultipleDefinitions(
                "com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin",
                ClassUtils.getDefaultClassLoader(),
                "0");
        Collections.sort(result, new Comparator<MultipleDefinition>() {
            @Override
            public int compare(MultipleDefinition o1, MultipleDefinition o2) {
                return o1.toString().hashCode() - o2.toString().hashCode();
            }
        });
        assertEquals("[MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl3', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl5', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl7', rank=1}, " +
                        "MultipleDefinition{isDisable=true, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl6', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl2', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl2', rank=2}, " +
                        "MultipleDefinition{isDisable=true, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl1', rank=1}, " +
                        "MultipleDefinition{isDisable=true, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl1', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl4', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl6', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl6', rank=2}, " +
                        "MultipleDefinition{isDisable=true, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl5', rank=1}, " +
                        "MultipleDefinition{isDisable=true, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl5', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl1', rank=1}, " +
                        "MultipleDefinition{isDisable=false, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl8', rank=1}, " +
                        "MultipleDefinition{isDisable=true, interfaceType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin', implementationType='com.github.shepherdviolet.glacimon.java.spi.test.SamplePluginImpl2', rank=1}]",
                String.valueOf(result));
    }

    @Test
    public void loadProperties() {
        List<PropertiesDefinition> result = DefinitionLoader.loadPropertiesDefinitions(
                "com.github.shepherdviolet.glacimon.java.spi.test.SampleServiceImpl2",
                ClassUtils.getDefaultClassLoader(),
                "0");
        Collections.sort(result, new Comparator<PropertiesDefinition>() {
            @Override
            public int compare(PropertiesDefinition o1, PropertiesDefinition o2) {
                return o1.toString().hashCode() - o2.toString().hashCode();
            }
        });
        assertEquals("[" +

                        "PropertiesDefinition{bindType='com.github.shepherdviolet.glacimon.java.spi.test.SampleServiceImpl2', priority=2, properties={dateFormat=yyyy-MM-dd HH:mm:ss, logEnabled=false}}, " +
                        "PropertiesDefinition{bindType='com.github.shepherdviolet.glacimon.java.spi.test.SampleServiceImpl2', priority=1, properties={dateFormat=yyyy-MM-dd HH:mm:ss.SSS, logEnabled=true}}" +
                        "]",
                String.valueOf(result));
    }
}
