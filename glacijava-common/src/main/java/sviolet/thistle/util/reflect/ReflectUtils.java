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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

/**
 * 反射工具
 *
 * @author S.Violet
 */
@Deprecated
public class ReflectUtils {

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型.</p>
     *
     * @param clazz 类
     * @param targetGenericClass 泛型定义类, 指定要获取的泛型定义在哪个类/接口
     * @return 不为空, NonNull, 类型:Class
     * @throws TargetGenericClassNotFoundException 异常情况: 给定类的父类或接口中找不到泛型定义类
     * @deprecated Use GenericClassUtils#getActualClasses instead
     */
    @Deprecated
    public static Class<?>[] getGenericClasses(Class<?> clazz, Class<?> targetGenericClass) throws TargetGenericClassNotFoundException {
        try {
            Collection<Class<?>> actualClasses = GenericClassUtils.getActualClasses(clazz, targetGenericClass).values();
            Class<?>[] result = new Class<?>[actualClasses.size()];
            actualClasses.toArray(result);
            return result;
        } catch (GenericClassUtils.TargetGenericClassNotFoundException e) {
            throw new TargetGenericClassNotFoundException(e);
        }
    }

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型.</p>
     *
     * @param clazz 类
     * @param targetGenericClass 泛型定义类, 指定要获取的泛型定义在哪个类/接口
     * @return 不为空, NonNull, 类型:Class/ParameterizedType, 返回null表示未找到指定的声明泛型的类
     * @throws TargetGenericClassNotFoundException 异常情况: 给定类的父类或接口中找不到泛型定义类
     * @deprecated Use GenericClassUtils#getActualTypes instead
     */
    @Deprecated
    public static Type[] getGenericTypes(Class clazz, Class targetGenericClass) throws TargetGenericClassNotFoundException {
        try {
            Collection<Type> actualTypes = GenericClassUtils.getActualTypes(clazz, targetGenericClass).values();
            Type[] result = new Type[actualTypes.size()];
            actualTypes.toArray(result);
            return result;
        } catch (GenericClassUtils.TargetGenericClassNotFoundException e) {
            throw new TargetGenericClassNotFoundException(e);
        }
    }

    /**
     * 指定的泛型定义类没有在父类和接口中找到. 通俗地讲, 就是遍历了类的所有父类和接口, 都没有找到匹配第二个参数targetGenericClass
     * 的, 所以无法获取到泛型具体类型
     *
     * @deprecated Use GenericClassUtils instead
     */
    public static class TargetGenericClassNotFoundException extends Exception {

        private static final long serialVersionUID = 8742474476663138141L;

        public TargetGenericClassNotFoundException(Exception cause) {
            super(cause.getMessage(), cause);
        }

    }

    private static final int DEFAULT_SKIP_ELEMENTS = 3;

    /**
     * <p>获取方法调用者信息(类名/方法名)</p>
     *
     * @param equalSkips      要跳过的类(equals方式匹配), 可为空, 推荐HashSet
     * @param startsWithSkips 要跳过的类(startsWith方式匹配), 可为空, 推荐ArrayList
     * @return 调用者信息, Nullable, 若返回空则表示找不到调用者
     * @deprecated Use MethodCaller#getMethodCaller instead
     */
    @Deprecated
    public static MethodCaller getMethodCaller(Set<String> equalSkips, Collection<String> startsWithSkips) {
        sviolet.thistle.util.reflect.MethodCaller info = sviolet.thistle.util.reflect.MethodCaller.getMethodCaller(equalSkips, startsWithSkips, DEFAULT_SKIP_ELEMENTS);
        return info != null ? new MethodCaller(info) : null;
    }

    /**
     * @deprecated Use MethodCaller#getMethodCaller instead
     */
    @Deprecated
    public static class MethodCaller {

        private String callerClass;
        private String callerMethodName;
        private String callerFileName;
        private int callerLineNumber;
        private int callerIndex;
        private StackTraceElement[] stackTraces;

        private MethodCaller(sviolet.thistle.util.reflect.MethodCaller info){
            this.callerClass = info.getCallerClass();
            this.callerMethodName = info.getCallerMethodName();
            this.callerFileName = info.getCallerFileName();
            this.callerLineNumber = info.getCallerLineNumber();
            this.callerIndex = info.getCallerIndex();
            this.stackTraces = info.getStackTraces();
        }

        /**
         * 调用点所在的类, 不为空
         */
        public String getCallerClass() {
            return callerClass;
        }

        /**
         * 调用点的方法名, 不为空
         */
        public String getCallerMethodName() {
            return callerMethodName;
        }

        /**
         * 调用点所在类对应的文件, 可为空
         */
        public String getCallerFileName() {
            return callerFileName;
        }

        /**
         * 调用点所在行数
         */
        public int getCallerLineNumber() {
            return callerLineNumber;
        }

        /**
         * 调用点在线程堆栈中的位置
         */
        public int getCallerIndex() {
            return callerIndex;
        }

        /**
         * 调用线程堆栈
         */
        public StackTraceElement[] getStackTraces() {
            return stackTraces;
        }

        @Override
        public String toString() {
            return "MethodCaller{" +
                    "callerClass='" + callerClass + '\'' +
                    ", callerMethodName='" + callerMethodName + '\'' +
                    '}';
        }
    }

}
