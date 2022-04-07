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

/**
 * BeanMethodNameUtils
 *
 * @author S.Violet
 */
class BeanMethodNameUtils {

    /**
     * @param fieldName fieldName of java bean
     */
    static String fieldToSetter(String fieldName) {
        return "set" + formatFieldName(fieldName);
    }

    /**
     * @param fieldName fieldName of java bean
     */
    static String fieldToGetter(String fieldName) {
        return "get" + formatFieldName(fieldName);
    }

    /**
     * @param fieldName fieldName of java bean
     */
    static String fieldToBoolGetter(String fieldName) {
        return "is" + formatFieldName(fieldName);
    }

    /**
     * @param methodName get/set method name
     * @return fieldName
     */
    static String methodToField(String methodName) {
        if (methodName == null || methodName.length() <= 2) {
            return null;
        }
        if (methodName.startsWith("set") || methodName.startsWith("get")) {
            if (methodName.length() == 3) {
                return null;
            }
            char[] fieldNameChars = methodName.substring(3, methodName.length()).toCharArray();
            if (fieldNameChars[0] > 64 && fieldNameChars[0] < 91) {
                fieldNameChars[0] += 32;
            }
            return String.valueOf(fieldNameChars);
        } else if (methodName.startsWith("is")) {
            char[] fieldNameChars = methodName.substring(2, methodName.length()).toCharArray();
            if (fieldNameChars[0] > 64 && fieldNameChars[0] < 91) {
                fieldNameChars[0] += 32;
            }
            return String.valueOf(fieldNameChars);
        }
        return null;
    }

    private static String formatFieldName(String fieldName) {
        if (fieldName == null || fieldName.length() <= 0) {
            return "";
        }

        char[] fieldNameChars = fieldName.toCharArray();

        final char firstChar = fieldNameChars[0];
        boolean isFirstCharLowerCase;
        if (firstChar > 64 && firstChar < 91) {
            isFirstCharLowerCase = false;
        } else if (firstChar > 96 && firstChar < 123){
            isFirstCharLowerCase = true;
        } else {
            return fieldName;
        }

        final char secondChar;
        boolean isSecondCharLowerCase = true;
        if (fieldNameChars.length > 1){
            secondChar = fieldNameChars[1];
            isSecondCharLowerCase = secondChar <= 64 || secondChar >= 91;
        }

        if (isFirstCharLowerCase && isSecondCharLowerCase){
            fieldNameChars[0] -= 32;
        }

        return String.valueOf(fieldNameChars);
    }

}
