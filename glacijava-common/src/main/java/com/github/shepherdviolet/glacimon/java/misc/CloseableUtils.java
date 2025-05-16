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

package com.github.shepherdviolet.glacimon.java.misc;

import com.github.shepherdviolet.glacimon.java.common.interfaces.Destroyable;

import java.io.Closeable;

/**
 * Closeable工具
 *
 * @author shepherdviolet
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

}
