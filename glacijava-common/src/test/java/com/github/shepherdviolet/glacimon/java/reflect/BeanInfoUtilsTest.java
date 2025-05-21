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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("PropertyInfo{propertyName='a', propertyClass=class [Ljava.util.Map;, propertyType=java.util.Map<java.lang.String, java.util.Set<java.lang.Runnable>[]>[], readMethod=public java.util.Map[] com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean1.getA(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setA(java.lang.Object)}\n" +
                        "PropertyInfo{propertyName='b', propertyClass=interface java.util.Collection, propertyType=java.util.Collection<java.lang.Long>, readMethod=public java.lang.Object com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getB(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean1.setB(java.util.Collection)}\n" +
                        "PropertyInfo{propertyName='c', propertyClass=interface java.util.Map, propertyType=java.util.Map<java.lang.String, java.util.List<java.lang.String>>, readMethod=null, writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setC(java.lang.Object)}\n" +
                        "PropertyInfo{propertyName='d', propertyClass=interface java.util.Set, propertyType=java.util.Set<java.lang.Runnable>, readMethod=null, writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setD(java.lang.Object)}\n" +
                        "PropertyInfo{propertyName='e', propertyClass=class java.lang.Object, propertyType=class java.lang.Object, readMethod=public java.lang.Object com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getE(), writeMethod=null}\n" +
                        "PropertyInfo{propertyName='f', propertyClass=class java.lang.Integer, propertyType=class java.lang.Integer, readMethod=public java.lang.Integer com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean2.getF(), writeMethod=null}\n" +
                        "PropertyInfo{propertyName='int', propertyClass=int, propertyType=int, readMethod=public int com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean1.getInt(), writeMethod=null}\n" +
                        "PropertyInfo{propertyName='int2', propertyClass=int, propertyType=int, readMethod=public int com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getInt2(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setInt2(int)}\n" +
                        "PropertyInfo{propertyName='int3', propertyClass=class java.lang.Integer, propertyType=class java.lang.Integer, readMethod=public java.lang.Integer com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getInt3(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setInt3(java.lang.Integer)}\n" +
                        "PropertyInfo{propertyName='list', propertyClass=interface java.util.List, propertyType=java.util.List<java.lang.String>, readMethod=public java.util.List com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getList(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setList(java.util.List)}\n" +
                        "PropertyInfo{propertyName='string', propertyClass=class java.lang.String, propertyType=class java.lang.String, readMethod=public java.lang.String com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getString(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setString(java.lang.String)}\n" +
                        "PropertyInfo{propertyName='string2', propertyClass=class java.lang.String, propertyType=class java.lang.String, readMethod=public java.lang.String com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getString2(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setString2(java.lang.String)}\n" +
                        "PropertyInfo{propertyName='string3', propertyClass=class java.lang.String, propertyType=class java.lang.String, readMethod=public java.lang.String com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getString3(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setString3(java.lang.String)}\n" +
                        "PropertyInfo{propertyName='string4', propertyClass=class java.lang.String, propertyType=class java.lang.String, readMethod=public java.lang.String com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.getString4(), writeMethod=public void com.github.shepherdviolet.glacimon.java.reflect.BeanInfoUtilsTest$Bean0.setString4(java.lang.String)}\n",
                stringBuilder.toString());
//        System.out.println(stringBuilder.toString());
    }

    @Test
    public void setAndGet() throws IntrospectionException {
        Bean2<String> bean2 = new Bean2<>();
        Assertions.assertEquals("Bean2{a=null, " +
                        "b=null, " +
                        "c=null, " +
                        "d=null, " +
                        "e=null, " +
                        "f=0, " +
                        "string='null', " +
                        "string2='null', " +
                        "string3='null', " +
                        "string4='null', " +
                        "list=null, " +
                        "int=0, " +
                        "int2=0, " +
                        "int3=null}",
                bean2.toString());

        Map<String, BeanInfoUtils.PropertyInfo> propertyInfos = new TreeMap<>(BeanInfoUtils.getPropertyInfos(Bean2.class));
        propertyInfos.get("string").set(bean2, "i am string", false);
        propertyInfos.get("string2").set(null, "bean is null", false);
        propertyInfos.get("string3").set(bean2, "type not match".getBytes(), false);
        propertyInfos.get("e").set(bean2, "no write method", false);
        propertyInfos.get("int2").set(bean2, 2222, false);
        propertyInfos.get("int3").set(bean2, 3333, false);
        Assertions.assertEquals("Bean2{a=null, " +
                        "b=null, " +
                        "c=null, " +
                        "d=null, " +
                        "e=null, " +
                        "f=0, " +
                        "string='i am string', " +
                        "string2='null', " +
                        "string3='null', " +
                        "string4='null', " +
                        "list=null, " +
                        "int=0, " +
                        "int2=2222, " +
                        "int3=3333}",
                bean2.toString());

        Assertions.assertEquals("i am string", propertyInfos.get("string").get(bean2, false));
        Assertions.assertEquals(Integer.valueOf(2222), propertyInfos.get("int2").get(bean2, false));
        Assertions.assertEquals(Integer.valueOf(3333), propertyInfos.get("int3").get(bean2, false));

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

        protected A a;
        protected B b;
        protected C c;
        protected D d;
        protected E e;
        protected String string;
        protected String string2;
        protected String string3;
        protected String string4;
        protected List<String> list;
        protected int i;
        protected int i2;
        protected Integer i3;

        public A getA(){
            return null;
        }

        public void setA(A i) {
            this.a = i;
        }

        public B getB(){
            return this.b;
        }

        public void setC(C i) {
            this.c = i;
        }

        public void setD(D i) {
            this.d = i;
        }

        public E getE(){
            return this.e;
        }

        public String getString(){
            return this.string;
        }

        public void setString(String i) {
            this.string = i;
        }

        public String getString2(){
            return this.string2;
        }

        public void setString2(String i) {
            this.string2 = i;
        }

        public String getString3(){
            return this.string3;
        }

        public void setString3(String i) {
            this.string3= i;
        }

        public String getString4(){
            return this.string4;
        }

        public void setString4(String i) {
            this.string4 = i;
        }

        public List<String> getList(){
            return this.list;
        }

        public void setList(List<String> i) {
            this.list = i;
        }

        public void setInt(long i) {
            this.i = (int) i;
        }

        public int getInt2() {
            return i2;
        }

        public void setInt2(int i2) {
            this.i2 = i2;
        }

        public Integer getInt3() {
            return i3;
        }

        public void setInt3(Integer i3) {
            this.i3 = i3;
        }
    }

    public static class Bean1 <AA, BB> extends Bean0<Map<String, AA[]>[], Collection<Long>, Map<String, List<String>>, AA, BB> {

        @Override
        public Map<String, AA[]>[] getA() {
            return a;
        }

        public void setB(Collection<Long> i){
            b = i;
        }

        public void setString(byte[] i) {
            string = new String(i);
        }

        public int getInt() {
            return i;
        }

    }

    public static class Bean2 <AAA> extends Bean1<Set<Runnable>, AAA> {

        public String noMethodProp;

        private String protectedProp;
        private int f;

        protected String getProtectedProp() {
            return protectedProp;
        }

        public Integer getF() {
            return f;
        }

        public void setF(String f) {
            this.f = Integer.parseInt(f);
        }

        @Override
        public String toString() {
            return "Bean2{" +
                    "a=" + a +
                    ", b=" + b +
                    ", c=" + c +
                    ", d=" + d +
                    ", e=" + e +
                    ", f=" + f +
                    ", string='" + string + '\'' +
                    ", string2='" + string2 + '\'' +
                    ", string3='" + string3 + '\'' +
                    ", string4='" + string4 + '\'' +
                    ", list=" + list +
                    ", int=" + i +
                    ", int2=" + i2 +
                    ", int3=" + i3 +
                    '}';
        }
    }

}
