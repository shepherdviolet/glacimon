/*
 * Copyright (C) 2015-2018 S.Violet
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

import sviolet.thistle.entity.common.Destroyable;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Closeable工具
 *
 * @author S.Violet
 */
public class CloseableUtils {

    public static void closeQuiet(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
        }
    }

    public static void closeQuiet(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
        }
    }

    public static void closeQuiet(Destroyable destroyable) {
        if (destroyable == null) {
            return;
        }
        try {
            destroyable.onDestroy();
        } catch (Exception ignore) {
        }
    }

    public static void closeIfCloseable(Object obj){
        if (obj == null) {
            return;
        }
        try {
            if (obj instanceof Closeable){
                ((Closeable) obj).close();
            } else if (obj instanceof AutoCloseable) {
                ((AutoCloseable) obj).close();
            } else if (obj instanceof Destroyable) {
                ((Destroyable) obj).onDestroy();
            }
        } catch (Exception ignore){
        }
    }

    /* **********************************************************************************************************
     * MappedByteBuffer:注意事项
     * MappedByteBuffer在一些运行时环境中(例如HOTSPOT), 会占用内存并占用文件句柄, 导致文件无法读写删除, 直到对象被GC.
     * 且没有常规办法可以回收资源. ANDROID中不存在此问题.
     * isMappedByteBufferCanClean方法可以判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时环境建
     * 议不要随便使用MappedByteBuffer, 改用传统的IO.
     * cleanMappedByteBuffer方法可以手动回收MappedByteBuffer.
     *
     * MappedByteBuffer是通过下列方法获得的:
     *      FileInputStream inputStream = new FileInputStream(file);
     *      FileChannel channel = inputStream.getChannel();
     *      MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
     */

    /**
     * 判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时建议不要随便使用MappedByteBuffer, 建议用传统的IO.
     * 注意, 安卓平台中, MappedByteBuffer不支持手动回收, 但是不回收也不存在文件句柄占用问题, 可以用MappedByteBuffer.
     *
     * @return true:支持手动回收MappedByteBuffer
     */
    public static boolean isMappedByteBufferCanClean(){
        return MAPPED_BYTE_BUFFER_UNMAP.isMappedByteBufferCanClean();
    }

    /**
     * 手动回收MappedByteBuffer.
     *
     * @return true:回收成功
     */
    public static boolean cleanMappedByteBuffer(ByteBuffer byteBuffer) {
        return MAPPED_BYTE_BUFFER_UNMAP.cleanMappedByteBuffer(byteBuffer);
    }

    private static final MappedByteBufferUnmap MAPPED_BYTE_BUFFER_UNMAP;

    static {
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK ||
                PlatformUtils.RUNTIME_VERSION == null){
            //1. Android : 安卓平台中, MappedByteBuffer不支持手动回收, 但是不回收也不存在文件句柄占用问题, 可以用MappedByteBuffer
            //2. 未知版本 : 不支持手动回收, 不建议使用MappedByteBuffer
            MAPPED_BYTE_BUFFER_UNMAP = new UnsupportedMappedByteBufferUnmap();
        } else if (PlatformUtils.RUNTIME_VERSION.startsWith("1.")) {
            //JDK8- : MappedByteBuffer需要被手动回收, 会有句柄未释放问题
            MAPPED_BYTE_BUFFER_UNMAP = new Jdk8MappedByteBufferUnmap();
        } else {
            //JDK9+ : MappedByteBuffer需要被手动回收, 会有句柄未释放问题
            MAPPED_BYTE_BUFFER_UNMAP = new Jdk9MappedByteBufferUnmap();
        }
    }

    private interface MappedByteBufferUnmap {

        boolean isMappedByteBufferCanClean();

        boolean cleanMappedByteBuffer(ByteBuffer byteBuffer);

    }

    private static class UnsupportedMappedByteBufferUnmap implements MappedByteBufferUnmap {

        @Override
        public boolean isMappedByteBufferCanClean() {
            return false;
        }

        @Override
        public boolean cleanMappedByteBuffer(ByteBuffer byteBuffer) {
            return false;
        }

    }

    private static class Jdk8MappedByteBufferUnmap implements MappedByteBufferUnmap {

        private volatile int isMappedByteBufferCanClean = -1;
        private Class<?> directByteBufferClass;
        private Method directByteBufferCleanerMethod;
        private Method cleanerCleanMethod;

        @Override
        public boolean isMappedByteBufferCanClean() {
            if (isMappedByteBufferCanClean >= 0){
                return isMappedByteBufferCanClean == 0;
            }
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    try {
                        //cleaner method
                        directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                        directByteBufferCleanerMethod = directByteBufferClass.getDeclaredMethod("cleaner");
                        if (!"sun.misc.Cleaner".equals(directByteBufferCleanerMethod.getReturnType().getName())){
                            throw new Exception("Return type of method cleaner is not sun.misc.Cleaner");
                        }
                        directByteBufferCleanerMethod.setAccessible(true);

                        //clean method
                        Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
                        cleanerCleanMethod = cleanerClass.getDeclaredMethod("clean");
                        cleanerCleanMethod.setAccessible(true);

                        isMappedByteBufferCanClean = 0;
                        return true;
                    } catch (Throwable t) {
                        System.out.println("WARNING: This jre environment can not clean MappedByteBuffer manually (jdk8- 1), isMappedByteBufferCanClean = false, error: " + t.getMessage());
                        t.printStackTrace();
                        isMappedByteBufferCanClean = 1;
                        return false;
                    }
                }
            });
        }

        @Override
        public boolean cleanMappedByteBuffer(final ByteBuffer byteBuffer) {
            if (byteBuffer instanceof MappedByteBuffer && isMappedByteBufferCanClean()){
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    @Override
                    public Boolean run() {
                        try {
                            if (!directByteBufferClass.isAssignableFrom(byteBuffer.getClass())){
                                return false;
                            }
                            cleanerCleanMethod.invoke(directByteBufferCleanerMethod.invoke(byteBuffer));
                            return true;
                        } catch (Throwable t) {
                            System.out.println("WARNING:This jre environment can not clean MappedByteBuffer manually (jdk8- 2), isMappedByteBufferCanClean = false, error: " + t.getMessage());
                            isMappedByteBufferCanClean = 1;
                            return false;
                        }
                    }
                });
            }
            return false;
        }

    }

    private static class Jdk9MappedByteBufferUnmap implements MappedByteBufferUnmap {

        private volatile int isMappedByteBufferCanClean = -1;
        private Object unsafeInstance;
        private Method unsafeInvokeCleanerMethod;

        @Override
        public boolean isMappedByteBufferCanClean() {
            if (isMappedByteBufferCanClean >= 0){
                return isMappedByteBufferCanClean == 0;
            }
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    try {
                        Class<?> unsafeClass;
                        try {
                            unsafeClass = Class.forName("sun.misc.Unsafe");
                        } catch(Throwable t) {
                            // 现在(JDK11) jdk.internal.misc.Unsafe里没有invokeCleaner方法, 但是哪天sun.misc.Unsafe被删除的时候,
                            // jdk.internal.misc.Unsafe里会不会就有invokeCleaner了呢?
                            unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                        }

                        //unsafe instance
                        Field unsafeTheUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                        unsafeTheUnsafeField.setAccessible(true);
                        unsafeInstance = unsafeTheUnsafeField.get(null);

                        //invokeCleaner method
                        unsafeInvokeCleanerMethod = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
                        unsafeInvokeCleanerMethod.setAccessible(true);

                        isMappedByteBufferCanClean = 0;
                        return true;
                    } catch (Throwable t) {
                        System.out.println("WARNING: This jre environment can not clean MappedByteBuffer manually (jdk9+ 1), isMappedByteBufferCanClean = false, error: " + t.getMessage());
                        t.printStackTrace();
                        isMappedByteBufferCanClean = 1;
                        return false;
                    }
                }
            });
        }

        @Override
        public boolean cleanMappedByteBuffer(final ByteBuffer byteBuffer) {
            if (byteBuffer instanceof MappedByteBuffer && byteBuffer.isDirect() && isMappedByteBufferCanClean()){
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    @Override
                    public Boolean run() {
                        try {
                            unsafeInvokeCleanerMethod.invoke(unsafeInstance, byteBuffer);
                            return true;
                        } catch (IllegalArgumentException e) {
                            //byteBuffer本身无法被清理, 不是环境不支持
                            return false;
                        } catch (Throwable t) {
                            System.out.println("WARNING:This jre environment can not clean MappedByteBuffer manually (jdk8- 2), isMappedByteBufferCanClean = false, error: " + t.getMessage());
                            isMappedByteBufferCanClean = 1;
                            return false;
                        }
                    }
                });
            }
            return false;
        }

    }

}
