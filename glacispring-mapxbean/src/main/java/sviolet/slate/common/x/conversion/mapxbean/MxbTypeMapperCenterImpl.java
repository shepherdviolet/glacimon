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

package sviolet.slate.common.x.conversion.mapxbean;

import com.github.shepherdviolet.glaciion.Glaciion;
import com.github.shepherdviolet.glaciion.api.annotation.PropertyInject;
import com.github.shepherdviolet.glaciion.api.interfaces.InitializableImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

/**
 * <p>[Common Handler] Type mapping center</p>
 *
 * <p>Purpose: Mapping type to type with MxbTypeMapper (provided from MxbTypeMapperProvider)</p>
 *
 * @author S.Violet
 * @see MapXBean
 */
public class MxbTypeMapperCenterImpl implements MxbTypeMapperCenter, InitializableImplementation {

    private static final String LOG_PREFIX = "MapXBean | ";

    private final Logger logger = LoggerFactory.getLogger(MapXBean.class);

    private Map<Class<?>, Map<Class<?>, MxbTypeMapper>> typeMappers = new HashMap<>();

    /**
     * Property inject: Is log enabled
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.log")
    private boolean logEnabled = false;

    /**
     * Get and init mappers from MxbTypeMapperProviders, after service created
     */
    @Override
    public void onServiceCreated() {
        List<MxbTypeMapper> allMappers = new ArrayList<>(32);
        //Get mapper from MxbTypeMapperProviders
        List<MxbTypeMapperProvider> providers = Glaciion.loadMultipleService(MxbTypeMapperProvider.class).getAll();
        //Multiple providers support
        for (MxbTypeMapperProvider provider : providers) {
            List<MxbTypeMapper> mappers = provider.getTypeMappers();
            if (mappers != null) {
                allMappers.addAll(mappers);
            }
        }
        //Sort by priority, the smaller the number, the higher the priority
        allMappers.sort(Comparator.comparingInt(MxbTypeMapper::priority).thenComparingInt(o -> o.getClass().hashCode()));
        //Store mappers in 'typeMappers', fromType->toType->MxbTypeMapper
        for (MxbTypeMapper mapper : allMappers) {
            if (mapper.fromType() == null || mapper.fromType().length <= 0) {
                throw new RuntimeException("Invalid MxbTypeMapper " + mapper.getClass().getName() + ", fromType method return null or empty, you can ignore this plugin by Glaciion SPI");
            }
            if (mapper.toType() == null || mapper.toType().length <= 0) {
                throw new RuntimeException("Invalid MxbTypeMapper " + mapper.getClass().getName() + ", toType method return null or empty, you can ignore this plugin by Glaciion SPI");
            }
            for (Class<?> fromType : mapper.fromType()) {
                for (Class<?> toType : mapper.toType()) {
                    initMapper(mapper, fromType, toType);
                }
            }
        }
    }

    /**
     * Store mappers
     */
    private void initMapper(MxbTypeMapper mapper, Class<?> fromType, Class<?> toType) {
        Map<Class<?>, MxbTypeMapper> mappers = typeMappers.computeIfAbsent(fromType, k -> new HashMap<>());
        //High priority mapper takes effect when the acceptance type is the same.
        MxbTypeMapper previous = mappers.get(toType);
        if (previous == null) {
            mappers.put(toType, mapper);
            if (logEnabled) {
                logger.info(LOG_PREFIX + "MxbTypeMapper Enabled from <" + fromType.getName() + "> to <" + toType.getName() + "> mapper <" + mapper.getClass().getName() + ">");
            }
        } else {
            if (logEnabled) {
                logger.info(LOG_PREFIX + "MxbTypeMapper Disabled from <" + fromType.getName() + "> to <" + toType.getName() + "> mapper <" + mapper.getClass().getName() + "> Overridden by a higher priority plugin " + previous.getClass().getName());
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object onConvert(Object from, Class<?> toType, Type toGenericType, MxbTypeMapper.Cause cause) throws Exception {
        if (from == null || toType == null) {
            return null;
        }

        Class<?> fromType = from.getClass();

        //fromType match toType, just return
        if (toType.isAssignableFrom(fromType)) {
            return from;
        }

        //Get mappers
        Map<Class<?>, MxbTypeMapper> mappers = typeMappers.get(fromType);
        if (mappers == null) {
            return onConvertInner(from, toType, toGenericType, cause);
        }
        MxbTypeMapper mapper = mappers.get(toType);
        if (mapper == null) {
            return onConvertInner(from, toType, toGenericType, cause);
        }

        //Convert by mapper
        return mapper.map(from, toType, toGenericType, cause);
    }

    protected Object onConvertInner(Object from, Class<?> toType, Type toGenericType, MxbTypeMapper.Cause cause) throws Exception {
        // Any to String
        if (String.class.equals(toType)) {
            return String.valueOf(from);
        }

        // String to Enum
        if (from instanceof String && toType.isEnum()) {
            return Enum.valueOf((Class)toType, (String) from);
        }

        return RESULT_NO_PROPER_MAPPER;
    }

}
