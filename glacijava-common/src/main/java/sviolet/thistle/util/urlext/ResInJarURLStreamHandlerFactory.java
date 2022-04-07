/*
 * Copyright (C) 2015-2022 S.Violet
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

package sviolet.thistle.util.urlext;

import sviolet.thistle.util.urlext.installer.URLStreamHandlerFactoryInstaller;
import sviolet.thistle.util.urlext.installer.URLStreamHandlerFactoryWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * <p>URL协议: 访问jar包中的资源(jar包中的jar包等)</p>
 *
 * <p>resinjar:META-INF/libs/name.jar  -->  访问classpath下的META-INF/libs/name.jar</p>
 *
 * <p> ============================================================================================================ </p>
 * <p>示例: Classloader加载jar包中的jar包</p>
 * <p></p>
 *
 * <p>Q: 为什么不直接从jar包加载类呢?</p>
 * <p>A: 如果jar包是独立的文件, 确实可以直接加载. 但如果想要加载jar包中的jar包, 就只能用这个办法了, 因为ClassLoader不支持加载jar包中的jar包.
 * (直接用getClassLoader().getResource(path)获取的URL, 交给ClassLoader加载会报错)</p>
 * <p></p>
 *
 * <p>安装URL协议</p>
 *
 * <pre>
 *      static {
 *          // 安装URL协议: resinjar
 *          ResInJarURLStreamHandlerFactory.install();
 *      }
 * </pre>
 *
 * <p>使用URL协议</p>
 *
 * <pre>
 *      List<URL> jarUrls = new ArrayList<>();
 *
 *      // 加载classpath下的Jar包: META-INF/libs/name.jar
 *      jarUrls.add(new URL("resinjar:META-INF/libs/name.jar"));
 *
 *      URL[] jarUrlArray = new URL[jarUrls.size()];
 *      jarUrls.toArray(jarUrlArray);
 *
 *      // 创建普通ClassLoader
 *      ClassLoader classLoader = new URLClassLoader(jarUrlArray, getClass().getClassLoader());
 *
 *      // 创建特殊的ClassLoader (过滤部分类加载不走双亲委派模型)
 *      ClassLoader classLoader = new FilteringDelegateClassLoader(jarUrlArray, getClass().getClassLoader()) {
 *          protected boolean isClassDelegatedByParent(String name) {
 *              return false;
 *          }
 *      };
 * </pre>
 *
 * @author shepherdviolet
 */
public class ResInJarURLStreamHandlerFactory implements URLStreamHandlerFactory {

    // 协议名称
    public static final String PROTOCOL = "resinjar";

    /**
     * 安装URL协议: resinjar
     */
    public static void install() {
        if (!URLStreamHandlerFactoryInstaller.isProtocolInstalled(PROTOCOL)) {
            URLStreamHandlerFactoryInstaller.setURLStreamHandlerFactory(new URLStreamHandlerFactoryWrapper(new ResInJarURLStreamHandlerFactory()));
        }
    }

    private ResInJarURLStreamHandlerFactory() {
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (PROTOCOL.equals(protocol)) {
            return new ResInJarURLStreamHandler();
        }
        return null;
    }

    public static class ResInJarURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return new ResInJarURLConnection(url);
        }

        private static class ResInJarURLConnection extends URLConnection {

            // 目标URL
            private URL targetUrl;

            public ResInJarURLConnection(URL url) {
                super(url);
            }

            /**
             * 连接的时候从classpath下找资源, 不存在就报错
             */
            public void connect() throws IOException {
                if (!this.connected) {
                    try {
                        targetUrl = getClassLoader().getResource(getURL().getPath());
                    } catch (Throwable t){
                        throw new ResourceNotFoundException("ResInJarURLStreamHandlerFactory | Resource '" + getURL() +
                                "' not found in classpath, classloader: " + getClassLoader(), t);
                    }
                    if (targetUrl == null) {
                        throw new ResourceNotFoundException("ResInJarURLStreamHandlerFactory | Resource '" + getURL() +
                                "' not found in classpath, classloader: " + getClassLoader());
                    }
                    this.connected = true;
                }
            }

            /**
             * 目标资源的InputStream转交给resinjar协议
             */
            @Override
            public InputStream getInputStream() throws IOException {
                this.connect();
                return targetUrl.openStream();
            }

            /**
             * 不支持输出
             */
            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new UnknownServiceException("ResInJarURLStreamHandlerFactory | protocol doesn't support output");
            }

            /**
             * 重要!!!
             *
             * 在TOMCAT等WEB容器中, 一个JVM内含多个WEB应用, 这些WEB应用会共享URL.class, 所以本协议实际上是给所有WEB应用安装了.
             * 因此, 在获取资源的时候, 要用contextClassLoader, 这样才能正确加载到WEB应用自己的类和资源.
             */
            private ClassLoader getClassLoader() {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader == null) {
                    classLoader = ClassLoader.getSystemClassLoader();
                }
                return classLoader;
            }

        }

    }

    /**
     * 在classpath下找不到指定的资源
     */
    public static class ResourceNotFoundException extends IOException {

        private static final long serialVersionUID = 7084349948577031022L;

        public ResourceNotFoundException(String message) {
            super(message);
        }

        public ResourceNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
