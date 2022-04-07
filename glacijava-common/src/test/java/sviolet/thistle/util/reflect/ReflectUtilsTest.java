/*
 * Copyright (C) 2015-2019 S.Violet
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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ReflectUtilsTest {

    @Test
    public void getActualClasses() throws ReflectUtils.TargetGenericClassNotFoundException {
        Assert.assertEquals("[class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericClasses(C3.class, I1.class)));
        Assert.assertEquals("[class java.lang.Object]",
                Arrays.toString(ReflectUtils.getGenericClasses(C3.class, I2.class)));
        Assert.assertEquals("[class java.lang.Double, class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericClasses(C3.class, I3.class)));
        Assert.assertEquals("[class java.lang.Long, class java.lang.Float, class java.lang.Double]",
                Arrays.toString(ReflectUtils.getGenericClasses(C3.class, C2.class)));

        Assert.assertEquals("[class java.lang.Object, class java.lang.Object]",
                Arrays.toString(ReflectUtils.getGenericClasses(C1.class, I3.class)));
        Assert.assertEquals("[class java.lang.Object, class java.lang.Object, class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericClasses(C2.class, C1.class)));
    }

    @Test
    public void getActualTypes() throws ReflectUtils.TargetGenericClassNotFoundException {
        Assert.assertEquals("[class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericTypes(C3.class, I1.class)));
        Assert.assertEquals("[class java.lang.Object]",
                Arrays.toString(ReflectUtils.getGenericTypes(C3.class, I2.class)));
        Assert.assertEquals("[class java.lang.Double, class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericTypes(C3.class, I3.class)));
        Assert.assertEquals("[class java.lang.Long, class java.lang.Float, class java.lang.Double]",
                Arrays.toString(ReflectUtils.getGenericTypes(C3.class, C2.class)));

        Assert.assertEquals("[class java.lang.Object, class java.lang.Object]",
                Arrays.toString(ReflectUtils.getGenericTypes(C1.class, I3.class)));
        Assert.assertEquals("[class java.lang.Object, class java.lang.Object, class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericTypes(C2.class, C1.class)));
    }

    @Test
    public void getMethodCaller(){
        Assert.assertEquals("MethodCaller{callerClass='sun.reflect.NativeMethodAccessorImpl', callerMethodName='invoke0'}",
                String.valueOf(ReflectUtils.getMethodCaller(null, null)));
    }

    public interface I0 {

    }

    public interface I1 <I1T1> {

    }

    public interface I2 <I2T1> {

    }

    public interface I3 <I3T1, I3T2> extends I1<I3T2> {

    }

    public interface I4 <I4T1> {

    }

    public static class C0 {

    }

    public static abstract class C1 <C1T1, C1T2, C1T3> extends C0 implements I0, I2, I3<C1T2, C1T3> {

    }

    public static class C2 <C2T1, C2T2, C2T3> extends C1<C2T2, C2T3, Integer> implements I0, I4<String> {

    }

    public static class C3 extends C2<Long, Float, Double> {

    }

}
