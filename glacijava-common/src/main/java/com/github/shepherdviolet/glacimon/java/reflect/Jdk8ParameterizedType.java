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

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Get source code from sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl (JDK 8)
 */
public class Jdk8ParameterizedType implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Class<?> rawType;
    private final Type ownerType;

    private Jdk8ParameterizedType(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = (Type)(ownerType != null ? ownerType : rawType.getDeclaringClass());
        this.validateConstructorArguments();
    }

    private void validateConstructorArguments() {
        TypeVariable<?>[] typeParameters = this.rawType.getTypeParameters();
        if (typeParameters.length != this.actualTypeArguments.length) {
            throw new MalformedParameterizedTypeException();
        }
    }

    public static Jdk8ParameterizedType make(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        return new Jdk8ParameterizedType(rawType, actualTypeArguments, ownerType);
    }

    public Type[] getActualTypeArguments() {
        return (Type[])this.actualTypeArguments.clone();
    }

    public Class<?> getRawType() {
        return this.rawType;
    }

    public Type getOwnerType() {
        return this.ownerType;
    }

    public boolean equals(Object type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            if (this == parameterizedType) {
                return true;
            } else {
                Type ownerType = parameterizedType.getOwnerType();
                Type rawType = parameterizedType.getRawType();
                return Objects.equals(this.ownerType, ownerType) && Objects.equals(this.rawType, rawType) && Arrays.equals(this.actualTypeArguments, parameterizedType.getActualTypeArguments());
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.ownerType != null) {
            if (this.ownerType instanceof Class) {
                stringBuilder.append(((Class<?>)this.ownerType).getName());
            } else {
                stringBuilder.append(this.ownerType.toString());
            }

            stringBuilder.append("$");
            if (this.ownerType instanceof ParameterizedType) {
                stringBuilder.append(this.rawType.getName().replace(((Class<?>)((ParameterizedType) this.ownerType).getRawType()).getName() + "$", ""));
            } else {
                stringBuilder.append(this.rawType.getSimpleName());
            }
        } else {
            stringBuilder.append(this.rawType.getName());
        }

        if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
            stringBuilder.append("<");
            boolean firstElement = true;
            Type[] actualTypeArguments = this.actualTypeArguments;
            int var4 = actualTypeArguments.length;

            for (int i = 0; i < var4; ++i) {
                Type actualTypeArgument = actualTypeArguments[i];
                if (!firstElement) {
                    stringBuilder.append(", ");
                }
                /*
                    兼容JDK 1.7, 因为Type.getTypeName方法是1.8新增的
                 */
                String typeName = null;
                if (actualTypeArgument instanceof Class && TYPE_NAME_GETTER != null) {
                    try {
                        typeName = (String) TYPE_NAME_GETTER.invoke(actualTypeArgument);
                    } catch (Exception ignore) {
                    }
                }

                stringBuilder.append(typeName != null ? typeName : actualTypeArgument);
                firstElement = false;
            }

            stringBuilder.append(">");
        }

        return stringBuilder.toString();
    }

    private static final Method TYPE_NAME_GETTER;

    static {
        Method typeNameGetter;
        try {
            typeNameGetter = Class.class.getMethod("getTypeName");
        } catch (NoSuchMethodException e) {
            typeNameGetter = null;
        }
        TYPE_NAME_GETTER = typeNameGetter;
    }

}