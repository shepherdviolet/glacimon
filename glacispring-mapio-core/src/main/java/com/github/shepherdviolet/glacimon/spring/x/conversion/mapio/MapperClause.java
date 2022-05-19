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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FieldScreeningMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.RuleInfo;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ErrorCode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.IoMode;

import java.util.*;

/**
 * 规则子句, 对应字典中的@InputMapper/@OutputMapper
 *
 * @author shepherdviolet
 */
class MapperClause extends RuleClause {

    private final FieldScreeningMode fieldScreeningMode;
    private final Class<?> dictionary;
    private final MapIoImpl mapIo;
    private final IoMode ioMode;

    private final ExceptionFactory exceptionFactory;

    public MapperClause(FieldScreeningMode fieldScreeningMode, Class<?> dictionary, MapIoImpl mapIo, IoMode ioMode,
                        ExceptionFactory exceptionFactory, RuleInfo ruleInfo) {
        super(ruleInfo);

        this.fieldScreeningMode = fieldScreeningMode;
        this.dictionary = dictionary;
        this.mapIo = mapIo;
        this.ioMode = ioMode;

        this.exceptionFactory = exceptionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object doFilter(Object value) {
        try {
            if (value instanceof Map) {
                return mapIo.doMapInner((Map<String, Object>) value, ioMode, fieldScreeningMode, dictionary);
            } else {
                throw exceptionFactory.createRuntimeException(getRuleInfo(), ErrorCode.FIELD_TYPE_NOT_MATCH_IO_MAPPER_EXPECT,
                        getRuleInfo().getFromKey(), value != null ? value.getClass().getName() : "null");
            }
        } catch (RuntimeException t) {
            if (Debug.isErrorLogEnabled()) {
                Debug.addErrorTrace(", mapping 'Map' field by rules in dictionary '" + dictionary + "'(" + ioMode + ")");
            }
            throw t;
        }
    }

    @Override
    public String toString() {
        return "MapperClause{" +
                "fieldScreeningMode=" + fieldScreeningMode +
                ", dictionary=" + dictionary +
                '}';
    }
}