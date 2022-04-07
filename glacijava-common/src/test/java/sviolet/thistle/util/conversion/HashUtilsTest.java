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

public class HashUtilsTest {

    public static void main(String[] args) {
        String s = "1111";
        System.out.println(HashUtils.fnv1(s.getBytes()) % 1000000000);
        System.out.println(HashUtils.sdbm(s.getBytes()) % 1000000000);
        System.out.println(HashUtils.djb2(s.getBytes()) % 1000000000);
    }

}
