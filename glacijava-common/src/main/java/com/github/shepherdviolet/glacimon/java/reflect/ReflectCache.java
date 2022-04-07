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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.shepherdviolet.glacimon.java.cache.LruCache;

/**
 * <p>反射缓存, 用于一些反复使用反射的Class</p>
 *
 * <p>其实JDK已经实现了反射对象的缓存, 正常使用反射也没问题</p>
 *
 * @author S.Violet
 */

public class ReflectCache {

    private static final int DEFAULT_MAX_SIZE = 100;

    private static LruCache<Class, Field[]> declaredFields = new LruCache<>(DEFAULT_MAX_SIZE);
    private static LruCache<Class, Method[]> declaredMethods = new LruCache<>(DEFAULT_MAX_SIZE);
    private static LruCache<Class, Constructor[]> declaredConstructors = new LruCache<>(DEFAULT_MAX_SIZE);

    /**
     * 设置缓存大小
     * @param size 若<=0, 则关闭缓存
     */
    public static void setCacheSize(int size){
        if (size <= 0){
            declaredFields = null;
            declaredMethods = null;
            declaredConstructors = null;
        }else {
            declaredFields = new LruCache<>(size);
            declaredMethods = new LruCache<>(size);
            declaredConstructors = new LruCache<>(size);
        }
    }

    public static Field[] getDeclaredFields(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final LruCache<Class, Field[]> cache = declaredFields;
        if (cache == null){
            return clazz.getDeclaredFields();
        }

        Field[] fields = cache.get(clazz);
        if (fields == null){
            fields = clazz.getDeclaredFields();
            for (Field field : fields){
                field.setAccessible(true);
            }
            cache.put(clazz, fields);
        }
        return fields;
    }

    public static Method[] getDeclaredMethods(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final LruCache<Class, Method[]> cache = declaredMethods;
        if (cache == null){
            return clazz.getDeclaredMethods();
        }

        Method[] methods = cache.get(clazz);
        if (methods == null){
            methods = clazz.getDeclaredMethods();
            for (Method method : methods){
                method.setAccessible(true);
            }
            cache.put(clazz, methods);
        }
        return methods;
    }

    public static Constructor[] getDeclaredConstructors(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final LruCache<Class, Constructor[]> cache = declaredConstructors;
        if (cache == null){
            return clazz.getDeclaredConstructors();
        }

        Constructor[] constructors = cache.get(clazz);
        if (constructors == null){
            constructors = clazz.getDeclaredConstructors();
            for (Constructor constructor : constructors){
                constructor.setAccessible(true);
            }
            cache.put(clazz, constructors);
        }
        return constructors;
    }

}
