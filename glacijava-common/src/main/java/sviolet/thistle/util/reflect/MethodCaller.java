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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.reflect;

import java.util.Collection;
import java.util.Set;

/**
 * 方法调用者工具
 *
 * @author S.Violet
 */
public class MethodCaller {

    /**
     * 常用的startsWithSkips: Spring动态代理类
     */
    public static final String STARTS_WITH_SKIPS_SPRING_PROXY = "org.springframework.cglib.proxy.Proxy$ProxyImpl$$";

    /**
     * 忽略调用链前3个元素
     */
    private static final int SKIP_ELEMENTS = 3;

    /**
     * <p>获取方法调用者信息(类名/方法名)</p>
     *
     * <p>在A方法内调用MethodCaller.getMethodCaller(null, null)方法时, 可以得到A方法的调用者(是谁调用了A方法). </p>
     *
     * <p>使用equalSkips参数跳过一些类: 假如A方法还会被B/C两个方法调用(封装), 我们想要知道是谁调用了A/B/C方法, 可以在A方法中
     * 调用MethodCaller.getMethodCaller(equalSkips, null)获取调用者, equalSkips配置B/C方法所在类的类名. 示例:</p>
     *
     * <pre>
     * //推荐用HashSet, 性能较好
     * private final Set<String> equalSkips = new HashSet<>(Arrays.asList(
     *         AClass.class.getName(),
     *         BClass.class.getName()
     * ));
     *
     * public void method(){
     *     String callerClassName = MethodCaller.getMethodCaller(equalSkips, null).getClassName();
     * }
     * </pre>
     *
     * <p>使用startsWithSkips参数跳过一些类: 假如A方法还会被B/C两个方法调用(封装), 我们想要知道是谁调用了A/B/C方法, 可以在A方法中
     * 调用MethodCaller.getMethodCaller(null, startsWithSkips)获取调用者, startsWithSkips配置B/C方法所在类的类名的前缀. 示例:</p>
     *
     * <pre>
     * //推荐用ArrayList, 性能较好
     * private final Collection<String> startsWithSkips = Arrays.asList(
     *         "org.springframework.cglib.proxy.Proxy$ProxyImpl$$",
     *         "com.company.packagename."
     * );
     *
     * public void method(){
     *     String callerClassName = MethodCaller.getMethodCaller(null, startsWithSkips).getClassName();
     * }
     * </pre>
     *
     * <p>另外, equalSkips和startsWithSkips参数可以同时使用. </p>
     *
     * <p>关于性能, new Throwable().getStackTrace()性能较差(和抛出一个异常的开销差不多),
     * 但微微比Thread.currentThread().getStackTrace()好一点点, 实测性能如下: <br>
     * 3.31GHz CPU 单线程 <br>
     * 线程堆栈15层时 10000次 耗时112.33ms <br>
     * 线程堆栈13层时 10000次 耗时93.37ms <br>
     * 线程堆栈 7层时 10000次 耗时62.29ms <br>
     * 线程堆栈 3层时 10000次 耗时37.60ms <br>
     * </p>
     *
     * @param equalSkips      要跳过的类(equals方式匹配), 可为空, 推荐HashSet
     * @param startsWithSkips 要跳过的类(startsWith方式匹配), 可为空, 推荐ArrayList
     * @return 调用者信息, Nullable, 若返回空则表示找不到调用者
     */
    public static MethodCaller getMethodCaller(Set<String> equalSkips, Collection<String> startsWithSkips) {
        return getMethodCaller(equalSkips, startsWithSkips, SKIP_ELEMENTS);
    }

    static MethodCaller getMethodCaller(Set<String> equalSkips, Collection<String> startsWithSkips, int skipElements) {
        //获取当前堆栈
        StackTraceElement[] elements = new Throwable().getStackTrace();
        //跳过头三个元素(getMethodCaller/public getMethodCaller/调用public getMethodCaller的方法)
        if (elements != null && elements.length > skipElements) {
            //遍历堆栈
            int i = skipElements;
            for (; i < elements.length; i++) {
                //当前元素
                StackTraceElement element = elements[i];
                //如果类名与skipEquals中的一个相同, 则跳过该元素查找下一个
                if (equalSkips != null && equalSkips.contains(element.getClassName())) {
                    continue;
                }
                //如果类名是skipStarts中的任意一个作为开头的, 则跳过该元素查找下一个
                if (startsWithSkips != null && isStartsWithSkips(startsWithSkips, element)) {
                    continue;
                }
                //如果未跳过则当前元素就是调用者
                return new MethodCaller(element, i, elements);
            }
        }
        //找不到调用者
        return null;
    }

    private static boolean isStartsWithSkips(Collection<String> startsWithSkips, StackTraceElement element) {
        String className = element.getClassName();
        for (String skipStart : startsWithSkips) {
            if (skipStart != null && className.startsWith(skipStart)) {
                return true;
            }
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String callerClass;
    private String callerMethodName;
    private String callerFileName;
    private int callerLineNumber;
    private int callerIndex;
    private StackTraceElement[] stackTraces;

    private MethodCaller(StackTraceElement caller, int callerIndex, StackTraceElement[] stackTraces) {
        this.callerClass = caller.getClassName();
        this.callerMethodName = caller.getMethodName();
        this.callerFileName = caller.getFileName();
        this.callerLineNumber = caller.getLineNumber();
        this.callerIndex = callerIndex;
        this.stackTraces = stackTraces;
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
