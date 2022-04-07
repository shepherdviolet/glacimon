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

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class ClassPrinterTest {

    @Test
    public void test() throws IllegalAccessException {
        Assert.assertEquals("#### Class #########################################################################################\n" +
                        "public class sviolet.thistle.util.reflect.ClassPrinterTest$SuperElectronicCar\n" +
                        "        extends sviolet.thistle.util.reflect.ClassPrinterTest$ElectronicCar\n" +
                        "        implements java.util.concurrent.Callable {\n" +
                        "    // Fields\n" +
                        "    private java.lang.String name = nameValue1\n" +
                        "    private static final java.lang.String sfName = sfNameValue1\n" +
                        "    // Constructors\n" +
                        "    public SuperElectronicCar(sviolet.thistle.util.reflect.ClassPrinterTest) {...}\n" +
                        "    // Methods\n" +
                        "    public java.lang.Object call() throws java.lang.Exception {...}\n" +
                        "    public void close() throws java.io.IOException {...}\n" +
                        "    public void execute(java.lang.Runnable) throws java.lang.RuntimeException {...}\n" +
                        "    public java.math.BigDecimal getBigDecimal() {...}\n" +
                        "    protected java.lang.Integer getInt() throws java.io.IOException {...}\n" +
                        "    public void run() {...}\n" +
                        "}\n" +
                        "++++ Super Class +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" +
                        "protected static abstract class sviolet.thistle.util.reflect.ClassPrinterTest$ElectronicCar\n" +
                        "        extends sviolet.thistle.util.reflect.ClassPrinterTest$Car\n" +
                        "        implements java.io.Closeable, java.lang.Runnable {\n" +
                        "    // Fields\n" +
                        "    private java.lang.String name = nameValue2\n" +
                        "    private static java.lang.String sName = sNameValue2\n" +
                        "    private static final java.lang.String sfName = sfNameValue2\n" +
                        "    // Constructors\n" +
                        "    public ElectronicCar() {...}\n" +
                        "    public ElectronicCar(java.lang.String) {...}\n" +
                        "    // Methods\n" +
                        "    public void close() throws java.io.IOException {...}\n" +
                        "    public abstract java.math.BigDecimal getBigDecimal() {...}\n" +
                        "    public void run() {...}\n" +
                        "}\n" +
                        "++++ Super Class +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" +
                        "private static class sviolet.thistle.util.reflect.ClassPrinterTest$Car\n" +
                        "        extends java.lang.Object\n" +
                        "        implements java.util.concurrent.Executor {\n" +
                        "    // Fields\n" +
                        "    private java.lang.String name = nameValue3\n" +
                        "    private static java.lang.String sName = sNameValue3\n" +
                        "    private static final java.lang.String sfName = sfNameValue3\n" +
                        "    // Constructors\n" +
                        "    public Car() {...}\n" +
                        "    public Car(java.lang.String) {...}\n" +
                        "    // Methods\n" +
                        "    public void execute(java.lang.Runnable) throws java.lang.RuntimeException {...}\n" +
                        "}\n",
                ClassPrinter.print(new SuperElectronicCar(), new ClassPrinter.Params().setSorted(true)));
    }

    public class SuperElectronicCar extends ElectronicCar implements Callable {
        private static final String sfName = "sfNameValue1";
        private String name = "nameValue1";

        public SuperElectronicCar() {
        }

        protected Integer getInt() throws IOException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal() {
            return null;
        }

        @Override
        public Object call() throws Exception {
            return null;
        }
    }

    protected static abstract class ElectronicCar extends Car implements Closeable, Runnable {
        private static final String sfName = "sfNameValue2";
        private static String sName = "sNameValue2";
        private String name = "nameValue2";

        public ElectronicCar() {
        }

        public ElectronicCar(String name) {
            this.name = name;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void run() {
        }

        public abstract BigDecimal getBigDecimal();
    }

    private static class Car implements Executor {
        private static final String sfName = "sfNameValue3";
        private static String sName = "sNameValue3";
        private String name = "nameValue3";

        public Car() {
        }

        public Car(String name) {
            this.name = name;
        }

        @Override
        public void execute(Runnable command) throws RuntimeException {
        }
    }

}
