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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Get source code from sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl (JDK 8)
 */
public class Jdk8GenericArrayType implements GenericArrayType {

    private final Type genericComponentType;

    private Jdk8GenericArrayType(Type componentType) {
        this.genericComponentType = componentType;
    }

    public static Jdk8GenericArrayType make(Type componentType) {
        return new Jdk8GenericArrayType(componentType);
    }

    public Type getGenericComponentType() {
        return this.genericComponentType;
    }

    public String toString() {
        Type componentType = this.getGenericComponentType();
        StringBuilder stringBuilder = new StringBuilder();
        if (componentType instanceof Class) {
            stringBuilder.append(((Class<?>)componentType).getName());
        } else {
            stringBuilder.append(componentType.toString());
        }

        stringBuilder.append("[]");
        return stringBuilder.toString();
    }

    public boolean equals(Object type) {
        if (type instanceof GenericArrayType) {
            GenericArrayType var2 = (GenericArrayType)type;
            return Objects.equals(this.genericComponentType, var2.getGenericComponentType());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(this.genericComponentType);
    }
}
