/*
 * Copyright (C) 2015-2020 S.Violet
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

import com.github.shepherdviolet.glaciion.api.annotation.PropertyInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.objenesis.SpringObjenesis;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>[Common Handler] Object instantiator</p>
 *
 * <p>Purpose: Create instance of given class</p>
 *
 * @author S.Violet
 * @see MapXBean
 */
public class MxbObjectInstantiatorImpl implements MxbObjectInstantiator {

    private static final Constructor<?> NO_PARAM_LESS_CONSTRUCTOR;

    static {
        try {
            NO_PARAM_LESS_CONSTRUCTOR = NoParamLessConstructorMark.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Impossible exception", e);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(MapXBean.class);

    private final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(128);

    private final SpringObjenesis objenesis = new SpringObjenesis();

    /**
     * Property inject: Is objenesis cache enabled
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.instantiator-cache-enabled")
    private boolean cacheEnabled = true;

    /**
     * Property inject: Is log enabled
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.log")
    private boolean logEnabled = false;

    /**
     * @inheritDoc
     */
    @Override
    public <T> T newInstance(Class<T> clazz, boolean byConstructor) throws Exception {
        if (clazz == null) {
            throw new IllegalArgumentException("Can not create instance of null class");
        }
        // Create by parameter-less constructor first
        Constructor<?> constructor = getParameterLessConstructorWithCache(clazz);
        if (constructor != NO_PARAM_LESS_CONSTRUCTOR) {
            return (T) constructor.newInstance();
        }
        // Create by objenesis
        if (byConstructor) {
            throw new NoSuchMethodException("Create instance failed! No parameter-less constructor found in class " + clazz.getName());
        }
        return objenesis.newInstance(clazz, cacheEnabled);
    }

    private <T> Constructor<?> getParameterLessConstructorWithCache(Class<T> clazz) {
        if (cacheEnabled) {
            return CONSTRUCTOR_CACHE.computeIfAbsent(clazz, this::getParameterLessConstructor);
        }
        return getParameterLessConstructor(clazz);
    }

    protected Constructor<?> getParameterLessConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return NO_PARAM_LESS_CONSTRUCTOR;
        }
    }

    private static class NoParamLessConstructorMark {
        public NoParamLessConstructorMark() {
            throw new UnsupportedOperationException("Unsupported");
        }
    }

}
