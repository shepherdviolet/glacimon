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

package com.github.shepherdviolet.glacimon.java.io;

import java.io.InputStream;

/**
 * Java路径工具
 *
 * @author shepherdviolet
 */
public class JavaDirectoryUtils {

    /**
     * 获得项目工程路径
     */
    public static String getProjectDir(){
        return System.getProperty("user.dir");
    }

    /**
     * 获得操作系统用户路径
     */
    public static String getUserDir(){
        return System.getProperty("user.home");
    }

    /**
     * 获得资源路径
     */
    public static String getResourceDir(){
        return Class.class.getResource("/").getPath();
    }

    /**
     * 获得资源的输入流
     * @param resourcePath 资源路径
     * @return 如果返回null表示资源不存在
     */
    public static InputStream getResourceInputStream(String resourcePath){
        return Class.class.getResourceAsStream(resourcePath);
    }

}
