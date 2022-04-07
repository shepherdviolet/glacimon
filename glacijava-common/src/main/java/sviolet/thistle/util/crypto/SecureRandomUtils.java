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

package sviolet.thistle.util.crypto;

import sviolet.thistle.util.crypto.base.BaseKeyGenerator;

import java.security.SecureRandom;

/**
 * 安全随机数工具
 *
 * @author S.Violet
 */
public class SecureRandomUtils {

    public static void nextBytes(byte[] bytes){
        BaseKeyGenerator.getSystemSecureRandom().nextBytes(bytes);
    }

    public static int nextInt(){
        return BaseKeyGenerator.getSystemSecureRandom().nextInt();
    }

    public static int nextInt(int bound){
        return BaseKeyGenerator.getSystemSecureRandom().nextInt(bound);
    }

    public static boolean nextBoolean(){
        return BaseKeyGenerator.getSystemSecureRandom().nextBoolean();
    }

    public static double nextDouble(){
        return BaseKeyGenerator.getSystemSecureRandom().nextDouble();
    }

    public static float nextFloat(){
        return BaseKeyGenerator.getSystemSecureRandom().nextFloat();
    }

    public static double nextGaussian(){
        return BaseKeyGenerator.getSystemSecureRandom().nextGaussian();
    }

    public static long nextLong(){
        return BaseKeyGenerator.getSystemSecureRandom().nextLong();
    }

    public static SecureRandom getSystemSecureRandom(){
        return BaseKeyGenerator.getSystemSecureRandom();
    }

}
