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

package sviolet.thistle.util.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>ClassLoader: 过滤部分类加载不走双亲委派模型</p>
 *
 * <p>可以用来实现jar包隔离, 用ClassLoader加载不同版本的类, 避免冲突. </p>
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
public abstract class FilteringDelegateClassLoader extends URLClassLoader {

    private final Set<String> filteredClasses = new HashSet<>();

    /**
     * @deprecated Use {@link FilteringDelegateClassLoader#FilteringDelegateClassLoader(URL[], ClassLoader)}
     *             or {@link FilteringDelegateClassLoader#FilteringDelegateClassLoader(URL[], ClassLoader, URLStreamHandlerFactory)}
     *             instead. Set current classloader as parent.
     */
    @Deprecated
    public FilteringDelegateClassLoader(URL[] urls) {
        super(urls);
    }

    public FilteringDelegateClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public FilteringDelegateClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 过滤类名
        if (!isClassDelegatedByParent(name)) {
            // 不走双亲委派模型, 直接由本ClassLoader加载类
            synchronized (getClassLoadingLock(name)) {
                try {
                    Class<?> c = findLoadedClass(name);
                    if (c == null) {
                        c = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    filteredClasses.add(name);
                    return c;
                } catch (ClassNotFoundException ignore) {
                    // load from parent if not found
                } catch (Throwable t) {
                    throw new RuntimeException("Error while loading class: " + name, t);
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    protected abstract boolean isClassDelegatedByParent(String name);

    @Override
    public String toString() {
        return "FilteringDelegateClassLoader{" +
                "\nurls=" + Arrays.toString(getURLs()) +
                ", \nfilteredClasses=" + filteredClasses +
                "\n}";
    }

}
