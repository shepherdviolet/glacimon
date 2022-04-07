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

import org.junit.Assert;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.*;

public class BeanInfoUtilsTest {

    @Test
    public void getPropertyInfos() throws IntrospectionException {
        Map<String, BeanInfoUtils.PropertyInfo> propertyInfos = new TreeMap<>(BeanInfoUtils.getPropertyInfos(Bean2.class));
        StringBuilder stringBuilder = new StringBuilder();
        for (BeanInfoUtils.PropertyInfo propertyInfo : propertyInfos.values()) {
            stringBuilder.append(propertyInfo).append("\n");
        }
        Assert.assertEquals("PropertyInfo{propertyName='a', propertyClass=class [Ljava.util.Map;, propertyType=java.util.Map<java.lang.String, java.util.Set<java.lang.Runnable>[]>[], readMethod=public java.util.Map[] sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean1.getA(), writeMethod=public void sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.setA(java.lang.Object)}\n" +
                        "PropertyInfo{propertyName='b', propertyClass=interface java.util.Collection, propertyType=java.util.Collection<java.lang.Long>, readMethod=public java.lang.Object sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.getB(), writeMethod=public void sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean1.setB(java.util.Collection)}\n" +
                        "PropertyInfo{propertyName='c', propertyClass=interface java.util.Map, propertyType=java.util.Map<java.lang.String, java.util.List<java.lang.String>>, readMethod=null, writeMethod=public void sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.setC(java.lang.Object)}\n" +
                        "PropertyInfo{propertyName='d', propertyClass=interface java.util.Set, propertyType=java.util.Set<java.lang.Runnable>, readMethod=null, writeMethod=public void sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.setD(java.lang.Object)}\n" +
                        "PropertyInfo{propertyName='e', propertyClass=class java.lang.Object, propertyType=class java.lang.Object, readMethod=public java.lang.Object sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.getE(), writeMethod=null}\n" +
                        "PropertyInfo{propertyName='int', propertyClass=int, propertyType=int, readMethod=public int sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean1.getInt(), writeMethod=null}\n" +
                        "PropertyInfo{propertyName='list', propertyClass=interface java.util.List, propertyType=java.util.List<java.lang.String>, readMethod=public java.util.List sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.getList(), writeMethod=public void sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.setList(java.util.List)}\n" +
                        "PropertyInfo{propertyName='string', propertyClass=class java.lang.String, propertyType=class java.lang.String, readMethod=public java.lang.String sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.getString(), writeMethod=public void sviolet.thistle.util.reflect.BeanInfoUtilsTest$Bean0.setString(java.lang.String)}\n",
                stringBuilder.toString());
//        System.out.println(stringBuilder.toString());
    }

//    public static void main(String[] args) throws IntrospectionException {
//        Map<String, BeanInfoUtils.PropertyInfo> propertyInfos = null;
//        long time = System.currentTimeMillis();
//        for (int i = 0 ; i < 10000 ; i++) {
//            propertyInfos = BeanInfoUtils.getPropertyInfos(Bean2.class, true);
//        }
//        System.out.println(System.currentTimeMillis() - time);
//        System.out.println(propertyInfos);
//    }

    public static class Bean0 <A, B, C, D, E> {

        public A getA(){
            return null;
        }

        public void setA(A i) {
        }

        public B getB(){
            return null;
        }

        public void setC(C i) {
        }

        public void setD(D i) {
        }

        public E getE(){
            return null;
        }

        public String getString(){
            return null;
        }

        public void setString(String i) {
        }

        public List<String> getList(){
            return null;
        }

        public void setList(List<String> i) {
        }

        public void setInt(long i) {
        }

    }

    public static class Bean1 <AA, BB> extends Bean0<Map<String, AA[]>[], Collection<Long>, Map<String, List<String>>, AA, BB> {

        @Override
        public Map<String, AA[]>[] getA() {
            return null;
        }

        public void setB(Collection<Long> i){
        }

        public void setString(byte[] i) {
        }

        public int getInt(){
            return 0;
        }

    }

    public static class Bean2 <AAA> extends Bean1<Set<Runnable>, AAA> {

    }

}
