/*
 * Copyright (C) 2015-2017 S.Violet
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
 * <p>根据JavaBean的参数名转化为方法名</p>
 *
 * 1、如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，就是说大小写不变。例如属性名为uName，方法是getuName/setuName。<br/>
 * 2、如果前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为URL，方法是getURL/setURL。<br/>
 * 3、如果首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，方法是getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是name。<br/>
 *
 * <p>根据JavaBean的方法名转化为参数名</p>
 *
 * @author S.Violet
 */
public class BeanMethodNameUtils {

    /**
     * 将FieldName转化为setter方法名
     * @param fieldName fieldName of java bean
     */
    public static String fieldToSetter(String fieldName) {
        return "set" + formatFieldName(fieldName);
    }

    /**
     * 将FieldName转化为getter方法名
     * @param fieldName fieldName of java bean
     */
    public static String fieldToGetter(String fieldName) {
        return "get" + formatFieldName(fieldName);
    }

    /**
     * 将FieldName转化为getter方法名(boolean属性专用的isXXX())
     * @param fieldName fieldName of java bean
     */
    public static String fieldToBoolGetter(String fieldName) {
        return "is" + formatFieldName(fieldName);
    }

    /**
     * 将is/get/set方法名转化为参数名(去掉前面的is/get/set, 将首字母转小写)
     * @param methodName 方法名
     * @return 参数名, 若返回null, 表示方法名不符合标准
     */
    public static String methodToField(String methodName) {
        if (methodName == null || methodName.length() <= 2) {
            return null;
        }
        if (methodName.startsWith("set") || methodName.startsWith("get")) {
            if (methodName.length() == 3) {
                return null;
            }
            char[] fieldNameChars = methodName.substring(3).toCharArray();
            if (fieldNameChars[0] > 64 && fieldNameChars[0] < 91) {
                fieldNameChars[0] += 32;
            }
            return String.valueOf(fieldNameChars);
        } else if (methodName.startsWith("is")) {
            char[] fieldNameChars = methodName.substring(2).toCharArray();
            if (fieldNameChars[0] > 64 && fieldNameChars[0] < 91) {
                fieldNameChars[0] += 32;
            }
            return String.valueOf(fieldNameChars);
        }
        return null;
    }

    private static String formatFieldName(String fieldName) {
        //检查输入
        if (fieldName == null || fieldName.length() <= 0) {
            return "";
        }

        //转为charArray
        char[] fieldNameChars = fieldName.toCharArray();

        //判断第一位字符
        final char firstChar = fieldNameChars[0];
        boolean isFirstCharLowerCase;
        if (firstChar > 64 && firstChar < 91) {
            isFirstCharLowerCase = false;
        } else if (firstChar > 96 && firstChar < 123){
            isFirstCharLowerCase = true;
        } else {
            //第一个字符异常的话, 直接返回
            return fieldName;
        }

        //判断第二个字符
        final char secondChar;
        boolean isSecondCharLowerCase = true;
        if (fieldNameChars.length > 1){
            secondChar = fieldNameChars[1];
            //第二个字符异常的话, 视为小写(试图把首字母变大写)
            isSecondCharLowerCase = secondChar <= 64 || secondChar >= 91;
        }

        //当前两位都是小写字母, 首字母变为大写
        if (isFirstCharLowerCase && isSecondCharLowerCase){
            fieldNameChars[0] -= 32;
        }

        return String.valueOf(fieldNameChars);
    }

}
