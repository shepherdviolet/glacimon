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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GenericClassUtilsTest {

    @Test
    public void getActualClasses() throws GenericClassUtils.TargetGenericClassNotFoundException, NoSuchMethodException {
        Assert.assertEquals("{I1T1=class java.lang.Integer}",
                String.valueOf(GenericClassUtils.getActualClasses(C3.class, I1.class)));
        Assert.assertEquals("{I2T1=class java.lang.Object}",
                String.valueOf(GenericClassUtils.getActualClasses(C3[].class, I2.class)));
        Assert.assertEquals("{I3T1=class java.lang.Double, I3T2=class java.lang.Integer}",
                String.valueOf(GenericClassUtils.getActualClasses(C3[][].class, I3.class)));
        Assert.assertEquals("{C2T1=class java.lang.Long, C2T2=class java.lang.Float, C2T3=class java.lang.Double}",
                String.valueOf(GenericClassUtils.getActualClasses(C3[][][].class, C2.class)));

        Assert.assertEquals("{I3T1=class java.lang.Object, I3T2=class java.lang.Object}",
                String.valueOf(GenericClassUtils.getActualClasses(C1[][][][].class, I3.class)));
        Assert.assertEquals("{C1T1=class java.lang.Object, C1T2=class java.lang.Object, C1T3=class java.lang.Integer}",
                String.valueOf(GenericClassUtils.getActualClasses(C2[][][][][].class, C1.class)));

        Assert.assertEquals("{E=class java.lang.Object}",
                String.valueOf(GenericClassUtils.getActualClasses(SpecialTypes.class.getMethod("genericArrayType4").getGenericReturnType(), List.class)));
        Assert.assertEquals("{E=interface java.util.Set}",
                String.valueOf(GenericClassUtils.getActualClasses(SpecialTypes.class.getMethod("genericArrayType5").getGenericReturnType(), List.class)));
    }

    @Test
    public void getActualTypes() throws GenericClassUtils.TargetGenericClassNotFoundException, NoSuchMethodException {
        Assert.assertEquals("{I1T1=class java.lang.Integer}",
                String.valueOf(GenericClassUtils.getActualTypes(C3.class, I1.class)));
        Assert.assertEquals("{I2T1=class java.lang.Object}",
                String.valueOf(GenericClassUtils.getActualTypes(C3[].class, I2.class)));
        Assert.assertEquals("{I3T1=class java.lang.Double, I3T2=class java.lang.Integer}",
                String.valueOf(GenericClassUtils.getActualTypes(C3[][].class, I3.class)));
        Assert.assertEquals("{C2T1=class java.lang.Long, C2T2=class java.lang.Float, C2T3=class java.lang.Double}",
                String.valueOf(GenericClassUtils.getActualTypes(C3[][][].class, C2.class)));

        Assert.assertEquals("{I3T1=class java.lang.Object, I3T2=class java.lang.Object}",
                String.valueOf(GenericClassUtils.getActualTypes(C1[][][][].class, I3.class)));
        Assert.assertEquals("{C1T1=class java.lang.Object, C1T2=class java.lang.Object, C1T3=class java.lang.Integer}",
                String.valueOf(GenericClassUtils.getActualTypes(C2[][][][][].class, C1.class)));

        Assert.assertEquals("{E=class java.lang.Object}",
                String.valueOf(GenericClassUtils.getActualTypes(SpecialTypes.class.getMethod("genericArrayType4").getGenericReturnType(), List.class)));
        Assert.assertEquals("{E=java.util.Set<java.lang.String>}",
                String.valueOf(GenericClassUtils.getActualTypes(SpecialTypes.class.getMethod("genericArrayType5").getGenericReturnType(), List.class)));
    }

//    public static void main(String[] args) throws GenericClassUtils.TargetGenericClassNotFoundException {
//        // -Djava.compiler=NONE
//        long time = System.currentTimeMillis();
//        Map<String, Type> result = null;
//        for (int i = 0 ; i < 1000000 ; i++) {
//            result = GenericClassUtils.getActualTypes(C3.class, I3.class);
//        }
//        System.out.println(System.currentTimeMillis() - time);
//        System.out.println(result);
//    }

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

    @Test
    public void typeToRawClass() throws NoSuchMethodException {
//        System.out.println(SpecialTypes.class.getMethod("genericArrayType1").getGenericReturnType().getClass());
        Assert.assertEquals(String.class, GenericClassUtils.typeToRawClass(String.class));
        Assert.assertEquals(Object.class, GenericClassUtils.typeToRawClass(SpecialTypes.class.getMethod("typeVariable").getGenericReturnType()));
        Assert.assertEquals(List.class, GenericClassUtils.typeToRawClass(SpecialTypes.class.getMethod("parameterizedType").getGenericReturnType()));
        Assert.assertEquals(List[].class, GenericClassUtils.typeToRawClass(SpecialTypes.class.getMethod("genericArrayType1").getGenericReturnType()));
        Assert.assertEquals(List[][].class, GenericClassUtils.typeToRawClass(SpecialTypes.class.getMethod("genericArrayType2").getGenericReturnType()));
        Assert.assertEquals(ArrayList[][][][].class, GenericClassUtils.typeToRawClass(SpecialTypes.class.getMethod("genericArrayType4").getGenericReturnType()));
    }

    public static class SpecialTypes <A> {

        public A typeVariable(){
            return null;
        }

        public List<A> parameterizedType(){
            return null;
        }

        public List<A>[] genericArrayType1(){
            return null;
        }

        public List<A>[][] genericArrayType2(){
            return null;
        }

        public ArrayList<A>[][][][] genericArrayType4(){
            return null;
        }

        public ArrayList<Set<String>>[][][][][] genericArrayType5(){
            return null;
        }

    }

}
