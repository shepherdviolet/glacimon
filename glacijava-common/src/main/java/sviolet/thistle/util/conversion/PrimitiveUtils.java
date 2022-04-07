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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.conversion;

/**
 * 基本类型转换工具
 *
 * @author S.Violet
 */
public class PrimitiveUtils {

    /**
     * 判断一个类是否是基本类型或基本类型的包装类型
     * @param clazz 类
     * @return true: 是基本类型或基本类型的包装类型
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive() ||
                Boolean.class.isAssignableFrom(clazz) ||
                Integer.class.isAssignableFrom(clazz) ||
                Long.class.isAssignableFrom(clazz) ||
                Float.class.isAssignableFrom(clazz) ||
                Double.class.isAssignableFrom(clazz) ||
                Byte.class.isAssignableFrom(clazz) ||
                Character.class.isAssignableFrom(clazz) ||
                Short.class.isAssignableFrom(clazz) ||
                Void.class.isAssignableFrom(clazz);
    }

    /**
     * 获得基本类型的包装类型
     * @param clazz 类
     * @return 若入参是基本类型, 则返回对应的包装类型
     */
    public static Class<?> toWrapperType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (!clazz.isPrimitive()){
            return clazz;
        }
        if (boolean.class.isAssignableFrom(clazz)) {
            return Boolean.class;
        } else if (int.class.isAssignableFrom(clazz)) {
            return Integer.class;
        } else if (long.class.isAssignableFrom(clazz)) {
            return Long.class;
        } else if (float.class.isAssignableFrom(clazz)) {
            return Float.class;
        } else if (double.class.isAssignableFrom(clazz)) {
            return Double.class;
        } else if (byte.class.isAssignableFrom(clazz)) {
            return Byte.class;
        } else if (char.class.isAssignableFrom(clazz)) {
            return Character.class;
        } else if (short.class.isAssignableFrom(clazz)) {
            return Short.class;
        } else if (void.class.isAssignableFrom(clazz)) {
            return Void.class;
        }
        return clazz;
    }

}
