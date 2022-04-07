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
import sviolet.thistle.util.conversion.PrimitiveUtils;
import sviolet.thistle.util.conversion.StringUtils;
import sviolet.thistle.util.reflect.BeanInfoUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>[Common Handler] Type judger</p>
 *
 * <p>Purpose: Determine what type a class is</p>
 *
 * @author S.Violet
 * @see MapXBean
 */
public class MxbTypeJudgerImpl implements MxbTypeJudger {

    /**
     * Property inject: Is log enabled
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.log")
    private boolean logEnabled = false;

    // Is Indivisible ////////////////////////////////////////////////////////////////////////////////////////////////

    private final Set<String> INDIVISIBLE_TYPES = new HashSet<>();

    private final Map<Class<?>, Boolean> IS_INDIVISIBLE_CACHE = new ConcurrentHashMap<>(128);

    /**
     * @inheritDoc
     */
    @Override
    public final boolean isIndivisible(Class<?> type) {
        if (type == null) {
            return false;
        }
        return IS_INDIVISIBLE_CACHE.computeIfAbsent(type, this::isIndivisible0);
    }

    protected boolean isIndivisible0(Class<?> type) {
        return type.isEnum() ||
                PrimitiveUtils.isPrimitiveOrWrapper(type) ||
                Object.class.equals(type) ||
                byte[].class.equals(type) ||
                char[].class.equals(type) ||
                CharSequence.class.isAssignableFrom(type) ||
                BigDecimal.class.isAssignableFrom(type) ||
                BigInteger.class.isAssignableFrom(type) ||
                Date.class.isAssignableFrom(type) ||
                Temporal.class.isAssignableFrom(type) ||
                judgeIndivisibleByProperties(type) ||
                judgeIndivisibleByCustom(type);
    }

    protected boolean judgeIndivisibleByProperties(Class<?> type) {
        return INDIVISIBLE_TYPES.contains(type.getName());
    }

    protected boolean judgeIndivisibleByCustom(Class<?> type) {
        return false;
    }

    /**
     * Property inject: Indivisible types
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.indivisible-types")
    public void setIndivisibleTypes(String types) {
        INDIVISIBLE_TYPES.addAll(StringUtils.splitAndTrim(types, ","));
    }

    // Is Bean ////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final Boolean[] A_BEAN_READABLE_WRITABLE = new Boolean[]{true, true};
    protected static final Boolean[] A_BEAN_READABLE = new Boolean[]{true, false};
    protected static final Boolean[] A_BEAN_WRITABLE = new Boolean[]{false, true};
    protected static final Boolean[] NOT_A_BEAN = new Boolean[]{false, false};

    private final Map<Class<?>, Boolean[]> IS_BEAN_CACHE = new ConcurrentHashMap<>(128);

    /**
     * @inheritDoc
     */
    @Override
    public final boolean isBean(Class<?> type, boolean readable, boolean writable) {
        if (type == null) {
            return false;
        }
        Boolean[] beanState = IS_BEAN_CACHE.computeIfAbsent(type, this::isBean0);
        return (!readable || beanState[0]) && (!writable || beanState[1]);
    }

    /**
     * @param type type
     * @return A_BEAN_READABLE_WRITABLE / A_BEAN_READABLE / A_BEAN_WRITABLE / NOT_A_BEAN
     */
    protected Boolean[] isBean0(Class<?> type){
        if (isIndivisible(type) ||
                type.isArray() ||
                type.isInterface() ||
                Modifier.isAbstract(type.getModifiers())) {
            return NOT_A_BEAN;
        }
        return judgeBean(type);
    }

    /**
     * @param type type
     * @return A_BEAN_READABLE_WRITABLE / A_BEAN_READABLE / A_BEAN_WRITABLE / NOT_A_BEAN
     */
    private Boolean[] judgeBean(Class<?> type) {
        try {
            Map<String, BeanInfoUtils.PropertyInfo> propertyInfos = BeanInfoUtils.getPropertyInfos(type);
            if (propertyInfos.size() <= 0) {
                return NOT_A_BEAN;
            }
            int reader = 0;
            int writer = 0;
            for (BeanInfoUtils.PropertyInfo propertyInfo : propertyInfos.values()) {
                if (propertyInfo.getReadMethod() != null) {
                    reader++;
                }
                if (propertyInfo.getWriteMethod() != null) {
                    writer++;
                }
            }
            if (reader > 0) {
                if (writer > 0) {
                    return A_BEAN_READABLE_WRITABLE;
                } else {
                    return A_BEAN_READABLE;
                }
            } else {
                if (writer > 0) {
                    return A_BEAN_WRITABLE;
                } else {
                    return NOT_A_BEAN;
                }
            }
        } catch (IntrospectionException e) {
            return NOT_A_BEAN;
        }
    }

}
