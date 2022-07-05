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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio;

import com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FieldScreeningMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.IoMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.FilterProvider;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule.RuleAnnotationManager;

import java.util.Map;

/**
 * MapIo configured by GlacimonSpi
 *
 * @author shepherdviolet
 */
@SuppressWarnings("resource")
public class SpiMapIo {

    private static final MapIo INSTANCE;

    static {
        MapIoImpl mapDocker = new MapIoImpl();
        mapDocker.setRuleAnnotationParser(GlacimonSpi.loadSingleService(RuleAnnotationManager.class).get());
        mapDocker.setFilterProvider(GlacimonSpi.loadSingleService(FilterProvider.class).get());
        mapDocker.setExceptionFactory(GlacimonSpi.loadSingleService(ExceptionFactory.class).get());
        INSTANCE = new MapIoWrapper(mapDocker);
    }

    /**
     * @return The MapIo configured by GlacimonSpi
     */
    public static MapIo getInstance() {
        return INSTANCE;
    }

    private static class MapIoWrapper implements MapIo {

        private final MapIo provider;

        public MapIoWrapper(MapIo provider) {
            this.provider = provider;
        }

        @Override
        public Map<String, Object> doMap(Map<String, Object> rawData, IoMode ioMode, FieldScreeningMode fieldScreeningMode, Class<?>... dictionaries) {
            return provider.doMap(rawData, ioMode, fieldScreeningMode, dictionaries);
        }

        @Override
        public Map<String, Object> doMap(Map<String, Object> rawData, IoMode ioMode, FieldScreeningMode fieldScreeningMode, Class<?>[]... dictionaries) {
            return provider.doMap(rawData, ioMode, fieldScreeningMode, dictionaries);
        }

        @Override
        public void preloadDictionaries(Class<?>... dictionaries) {
            provider.preloadDictionaries(dictionaries);
        }

        @Override
        public String printCachedMappers() {
            return provider.printCachedMappers();
        }
    }

}
