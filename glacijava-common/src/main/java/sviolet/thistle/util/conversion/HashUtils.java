/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.util.conversion;

/**
 * Hash算法工具
 *
 * @author S.Violet
 */
public class HashUtils {

    public static int fnv1(byte[] data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (byte b : data) {
            hash = (hash ^ b) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return (hash & 0x7FFFFFFF);
    }

    public static int sdbm(byte[] data) {
        int hash = 0;
        for (byte b : data) {
            hash = b + (hash << 6) + (hash << 16) - hash;
        }
        return (hash & 0x7FFFFFFF);
    }

    public static int djb2(byte[] data) {
        int hash = 5381;
        for (byte b : data) {
            hash = ((hash << 5) + hash) + b;
        }
        return (hash & 0x7FFFFFFF);
    }

}
