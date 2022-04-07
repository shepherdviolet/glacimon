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

package sviolet.thistle.util.common;

import java.lang.reflect.Field;

/**
 * JAVA运行环境信息
 *
 * @author S.Violet
 */
public class PlatformUtils {

    public enum Platform{

        /**
         * Java HotSpot
         */
        HOTSPOT("Java HotSpot"),

        /**
         * OpenJDK
         */
        OPENJDK("OpenJDK"),

        /**
         * Dalvik
         */
        DALVIK("Dalvik"),

        /**
         * EBA
         */
        JROCKIT("BEA"),

        /**
         * GNU libgcj
         */
        GNU("GNU libgcj"),

        /**
         * PERC
         */
        PERC("PERC"),

        /**
         * UNKNOWN
         */
        UNKNOWN("UNKNOWN");

        private String prefix;

        Platform(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static final String VM_NAME = System.getProperty("java.vm.name");
    public static final String VM_VERSION = System.getProperty("java.vm.version");
    public static final String VM_VENDOR = System.getProperty("java.vm.vendor");
    public static final String RUNTIME_VERSION = System.getProperty("java.runtime.version");
    public static final String VM_INFO = System.getProperty("java.vm.info");
    public static final String SPECIFICATION_VERSION = System.getProperty("java.specification.version");

    /**
     * platform
     */
    public static final Platform PLATFORM = getPlatform();

    /**
     * Android version. Will be -1 for none android platform, and -2 for exception
     */
    public static final int ANDROID_VERSION = getAndroidVersion();

    /**
     * Flag telling if this version of Android is based on the OpenJDK
     */
    public static final boolean ANDROID_IS_OPEN_JDK = getAndroidIsOpenJDK();

    public static final String GAE_VERSION = getGaeRuntimeVersion();
    public static final boolean GAE_IS_GOOGLE_APP_ENGINE = getGaeIsGoogleAppEngine();

    private static Platform getPlatform(){
        if (VM_NAME.startsWith(Platform.HOTSPOT.getPrefix())){
            return Platform.HOTSPOT;
        }
        if (VM_NAME.startsWith(Platform.OPENJDK.getPrefix())){
            return Platform.OPENJDK;
        }
        if (VM_NAME.startsWith(Platform.DALVIK.getPrefix())){
            return Platform.DALVIK;
        }
        if (VM_NAME.startsWith(Platform.JROCKIT.getPrefix())){
            return Platform.JROCKIT;
        }
        if (VM_NAME.startsWith(Platform.GNU.getPrefix())){
            return Platform.GNU;
        }
        if (VM_NAME.startsWith(Platform.PERC.getPrefix())){
            return Platform.PERC;
        }
        return Platform.UNKNOWN;
    }

    private static int getAndroidVersion() {
        if (PLATFORM != Platform.DALVIK) {
            return -1;
        }
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.Build$VERSION");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -2;
        }
        Field field;
        try {
            field = clazz.getField("SDK_INT");
        } catch (NoSuchFieldException e) {
            return getOldAndroidVersion(clazz);
        }
        int version;
        try {
            version = (Integer) field.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -2;
        }
        return version;
    }

    private static int getOldAndroidVersion(Class<?> versionClass) {
        Field field;
        try {
            field = versionClass.getField("SDK");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return -2;
        }
        String version;
        try {
            version = (String) field.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -2;
        }
        return Integer.parseInt(version);
    }

    private static boolean getAndroidIsOpenJDK() {
        if(ANDROID_VERSION < 0) {
            return false;
        }
        String bootClasspath = System.getProperty("java.boot.class.path");
        return bootClasspath != null && bootClasspath.toLowerCase().contains("core-oj.jar");
    }

    private static boolean getGaeIsGoogleAppEngine() {
        return GAE_VERSION != null;
    }

    private static String getGaeRuntimeVersion() {
        return System.getProperty("com.google.appengine.runtime.version");
    }

}
