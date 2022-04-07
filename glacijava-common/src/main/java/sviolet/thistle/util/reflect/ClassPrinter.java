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

package sviolet.thistle.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

import sviolet.thistle.util.judge.CheckUtils;

/**
 * <p>将一个类或一个对象的数据/构造函数/方法打印出来</p>
 *
 * @author S.Violet
 */
public class ClassPrinter {

    public static final String CLASS_IS_NULL = "Class: null";
    public static final String OBJECT_IS_NULL = "Object: null";

    private static final int POSITION_CLASS = 0;
    private static final int POSITION_FIELD = 1;
    private static final int POSITION_CONSTRUCTOR = 2;
    private static final int POSITION_METHOD = 3;

    private static final Params DEFAULT_PARAMS = new Params();

    /**
     * 打印class
     *
     * @param clazz class
     * @param traversals true:遍历父类
     */
    public static String print(Class<?> clazz, boolean traversals) throws IllegalAccessException {
        return print(clazz, new Params().setTraversals(traversals));
    }

    /**
     * 打印class
     *
     * @param clazz class
     * @param params 打印参数
     */
    public static String print(Class<?> clazz, Params params) throws IllegalAccessException {
        if (params == null) {
            params = DEFAULT_PARAMS;
        }
        if (clazz == null) {
            return params.buildClassTitle() + '\n' + CLASS_IS_NULL;
        }
        Class<?> current = clazz;
        StringBuilder stringBuilder = new StringBuilder();
        print(current, null, stringBuilder, params);
        while (params.isTraversals() && (current = current.getSuperclass()) != null && !Object.class.equals(current)) {
            print(current, null, stringBuilder, params);
        }
        return stringBuilder.toString();
    }

    /**
     * 打印对象
     *
     * @param obj 对象
     * @param traversals true:遍历父类
     */
    public static String print(Object obj, boolean traversals) throws IllegalAccessException {
        return print(obj, new Params().setTraversals(traversals));
    }

    /**
     * 打印对象
     *
     * @param obj 对象
     * @param params 打印参数
     */
    public static String print(Object obj, Params params) throws IllegalAccessException {
        if (params == null) {
            params = DEFAULT_PARAMS;
        }
        if (obj == null) {
            return params.buildClassTitle() + '\n' + OBJECT_IS_NULL;
        }
        Class<?> current = obj.getClass();
        StringBuilder stringBuilder = new StringBuilder();
        print(current, obj, stringBuilder, params);
        while (params.isTraversals() && (current = current.getSuperclass()) != null && !Object.class.equals(current)) {
            print(current, obj, stringBuilder, params);
        }
        return stringBuilder.toString();
    }

    private static void print(Class<?> clazz, Object obj, StringBuilder stringBuilder, Params params) throws IllegalAccessException {
        printClassInfo(clazz, stringBuilder, params);
        if (params.isPrintFields()) {
            printClassFields(clazz, obj, stringBuilder, params);
        }
        if (params.isPrintConstructors()) {
            printClassConstructors(clazz, stringBuilder, params);
        }
        if (params.isPrintMethods()) {
            printClassMethods(clazz, stringBuilder, params);
        }
        stringBuilder.append("}\n");
    }

    private static void printClassMethods(Class<?> clazz, StringBuilder stringBuilder, Params params) {
        stringBuilder.append("    ").append(params.buildMethodTitle()).append('\n');
        Method[] methods = ReflectCache.getDeclaredMethods(clazz);
        if (params.isSorted()) {
            Arrays.sort(methods, METHOD_COMPARATOR);
        }
        for (Method method : methods) {
            String name = method.getName();
            if (!params.isIncludeSpecial() && name.contains("$")) {
                continue;
            }
            stringBuilder.append("    ");
            printModifiers(method.getModifiers(), stringBuilder, POSITION_METHOD);
            stringBuilder.append(method.getReturnType().getName())
                    .append(" ")
                    .append(name)
                    .append("(");
            Class<?>[] paramTypes = method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(paramTypes[i].getName());
            }
            stringBuilder.append(")");
            Class<?>[] exceptions = method.getExceptionTypes();
            if (exceptions.length > 0) {
                stringBuilder.append(" throws ");
                for (int i = 0 ; i < exceptions.length ; i++) {
                    stringBuilder.append(exceptions[i].getName());
                    if (i < exceptions.length - 1) {
                        stringBuilder.append(", ");
                    }
                }
            }
            stringBuilder.append(" {...}\n");
        }
    }

    private static void printClassConstructors(Class<?> clazz, StringBuilder stringBuilder, Params params) {
        stringBuilder.append("    ").append(params.buildConstructorTitle()).append('\n');
        Constructor<?>[] constructors = ReflectCache.getDeclaredConstructors(clazz);
        if (params.isSorted()) {
            Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        }
        for (Constructor<?> constructor : constructors) {
            stringBuilder.append("    ");
            printModifiers(constructor.getModifiers(), stringBuilder, POSITION_CONSTRUCTOR);
            stringBuilder.append(clazz.getSimpleName())
                    .append("(");
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++) {
                if (j != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(paramTypes[j].getName());
            }
            stringBuilder.append(")");
            Class<?>[] exceptions = constructor.getExceptionTypes();
            if (exceptions.length > 0) {
                stringBuilder.append(" throws ");
                for (int i = 0 ; i < exceptions.length ; i++) {
                    stringBuilder.append(exceptions[i].getName());
                    if (i < exceptions.length - 1) {
                        stringBuilder.append(", ");
                    }
                }
            }
            stringBuilder.append(" {...}\n");
        }
    }

    private static void printClassFields(Class<?> clazz, Object obj, StringBuilder stringBuilder, Params params) throws IllegalAccessException {
        stringBuilder.append("    ").append(params.buildFieldTitle()).append('\n');
        Field[] fields = ReflectCache.getDeclaredFields(clazz);
        if (params.isSorted()) {
            Arrays.sort(fields, FIELD_COMPARATOR);
        }
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            String name = field.getName();
            if (!params.isIncludeSpecial() && name.contains("$")) {
                continue;
            }
            field.setAccessible(true);
            stringBuilder.append("    ");
            printModifiers(modifiers, stringBuilder, POSITION_FIELD);
            stringBuilder.append(field.getType().getName())
                    .append(" ")
                    .append(name);
            if (obj != null || CheckUtils.isFlagMatch(modifiers, Modifier.STATIC)) {
                stringBuilder.append(" = ")
                        .append(field.get(obj));
            } else {
                stringBuilder.append(';');
            }
            stringBuilder.append('\n');
        }
    }

    private static void printClassInfo(Class<?> clazz, StringBuilder stringBuilder, Params params) {
        if (stringBuilder.length() <= 0) {
            stringBuilder.append(params.buildClassTitle()).append('\n');
        } else {
            stringBuilder.append(params.buildSuperClassTitle()).append('\n');
        }
        printModifiers(clazz.getModifiers(), stringBuilder, POSITION_CLASS);
        stringBuilder.append("class ").append(clazz.getName());
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            stringBuilder.append("\n        extends ").append(superClass.getName());
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            if (params.isSorted()) {
                Arrays.sort(interfaces, CLASS_COMPARATOR);
            }
            stringBuilder.append("\n        implements ");
            for (int i = 0 ; i < interfaces.length ; i++) {
                stringBuilder.append(interfaces[i].getName());
                if (i < interfaces.length - 1) {
                    stringBuilder.append(", ");
                }
            }
        }
        stringBuilder.append(" {\n");
    }

    private static void printModifiers(int modifiers, StringBuilder stringBuilder, int position) {
        if (CheckUtils.isFlagMatch(modifiers, Modifier.PUBLIC)) {
            stringBuilder.append("public ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.PRIVATE)) {
            stringBuilder.append("private ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.PROTECTED)) {
            stringBuilder.append("protected ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.STATIC)) {
            stringBuilder.append("static ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.FINAL)) {
            stringBuilder.append("final ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.SYNCHRONIZED)) {
            stringBuilder.append("synchronized ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.VOLATILE) && position == POSITION_FIELD) {
            stringBuilder.append("volatile ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.NATIVE)) {
            stringBuilder.append("native ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.INTERFACE)) {
            stringBuilder.append("interface ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.ABSTRACT)) {
            stringBuilder.append("abstract ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.STRICT)) {
            stringBuilder.append("strict ");
        }
    }

    private static final Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {
        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private static final Comparator<Field> FIELD_COMPARATOR = new Comparator<Field>() {
        @Override
        public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>() {
        @Override
        public int compare(Constructor<?> o1, Constructor<?> o2) {
            int result = o1.getName().compareTo(o2.getName());
            if (result != 0) {
                return result;
            }
            return o1.toString().compareTo(o2.toString());
        }
    };

    private static final Comparator<Method> METHOD_COMPARATOR = new Comparator<Method>() {
        @Override
        public int compare(Method o1, Method o2) {
            int result = o1.getName().compareTo(o2.getName());
            if (result != 0) {
                return result;
            }
            return o1.toString().compareTo(o2.toString());
        }
    };

    public static class Params {

        private boolean traversals = true;
        private boolean printFields = true;
        private boolean printFieldValues = true;
        private boolean printConstructors = true;
        private boolean printMethods = true;
        private boolean includeSpecial = false;
        private boolean sorted = false;

        private String classTitle = "#### Class #########################################################################################";
        private String superClassTitle = "++++ Super Class +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
        private String fieldTitle = "// Fields";
        private String constructorTitle = "// Constructors";
        private String methodTitle = "// Methods";

        public boolean isTraversals() {
            return traversals;
        }

        /**
         * 遍历父类, 默认true
         */
        public Params setTraversals(boolean traversals) {
            this.traversals = traversals;
            return this;
        }

        public boolean isPrintFields() {
            return printFields;
        }

        /**
         * 打印Field, 默认true
         */
        public Params setPrintFields(boolean printFields) {
            this.printFields = printFields;
            return this;
        }

        public boolean isPrintFieldValues() {
            return printFieldValues;
        }

        /**
         * 打印Field值 (打印对象时有效), 默认true
         */
        public Params setPrintFieldValues(boolean printFieldValues) {
            this.printFieldValues = printFieldValues;
            return this;
        }

        public boolean isPrintConstructors() {
            return printConstructors;
        }

        /**
         * 打印构造器, 默认true
         */
        public Params setPrintConstructors(boolean printConstructors) {
            this.printConstructors = printConstructors;
            return this;
        }

        public boolean isPrintMethods() {
            return printMethods;
        }

        /**
         * 打印方法, 默认true
         */
        public Params setPrintMethods(boolean printMethods) {
            this.printMethods = printMethods;
            return this;
        }

        public boolean isIncludeSpecial() {
            return includeSpecial;
        }

        /**
         * 包含特殊的Method和Field
         */
        public Params setIncludeSpecial(boolean includeSpecial) {
            this.includeSpecial = includeSpecial;
            return this;
        }

        public boolean isSorted() {
            return sorted;
        }

        /**
         * 内容排序, 使同一个类/对象打印出来的结果保持一致(否则每次顺序可能不同), 默认false
         */
        public Params setSorted(boolean sorted) {
            this.sorted = sorted;
            return this;
        }

        public String getClassTitle() {
            return classTitle;
        }

        private String buildClassTitle() {
            return CheckUtils.isEmpty(classTitle) ? "" : classTitle;
        }

        /**
         * 类标题, 可为空
         */
        public Params setClassTitle(String classTitle) {
            this.classTitle = classTitle;
            return this;
        }

        public String getSuperClassTitle() {
            return superClassTitle;
        }

        private String buildSuperClassTitle() {
            return CheckUtils.isEmpty(superClassTitle) ? "" : superClassTitle;
        }

        /**
         * 父类标题, 可为空
         */
        public Params setSuperClassTitle(String superClassTitle) {
            this.superClassTitle = superClassTitle;
            return this;
        }

        public String getFieldTitle() {
            return fieldTitle;
        }

        private String buildFieldTitle() {
            return CheckUtils.isEmpty(fieldTitle) ? "" : fieldTitle;
        }

        /**
         * Field标题, 可为空
         */
        public Params setFieldTitle(String fieldTitle) {
            this.fieldTitle = fieldTitle;
            return this;
        }

        public String getConstructorTitle() {
            return constructorTitle;
        }

        private String buildConstructorTitle() {
            return CheckUtils.isEmpty(constructorTitle) ? "" : constructorTitle;
        }

        /**
         * 构造器标题, 可为空
         */
        public Params setConstructorTitle(String constructorTitle) {
            this.constructorTitle = constructorTitle;
            return this;
        }

        public String getMethodTitle() {
            return methodTitle;
        }

        private String buildMethodTitle() {
            return CheckUtils.isEmpty(methodTitle) ? "" : methodTitle;
        }

        /**
         * 方法标题, 可为空
         */
        public Params setMethodTitle(String methodTitle) {
            this.methodTitle = methodTitle;
            return this;
        }
    }

}
