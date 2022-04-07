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

package sviolet.thistle.util.crypto;

import sviolet.thistle.util.crypto.base.BaseKeyGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AES秘钥生成工具
 *
 * @author S.Violet
 */
public class AESKeyGenerator {

    public static final String KEY_ALGORITHM = "AES";

    /**
     * <p>生成128位AES对称密钥, 用于服务端场合, 产生随机密钥</p>
     *
     * @return 秘钥
     */
    public static byte[] generateAes128() {
        return BaseKeyGenerator.generateKey((SecureRandom) null, 128, KEY_ALGORITHM);
    }

    /**
     * <p>生成128位AES对称密钥, 用于服务端场合, 产生随机密钥.
     * 推荐使用AESKeyGenerator.generateAes128()代替, 使用自定义的SecureRandom可能会导致安全问题</p>
     *
     * @param secureRandom 随机数, 推荐使用AESKeyGenerator.generateAes128()代替, 使用自定义的SecureRandom可能会导致安全问题
     * @return 秘钥
     */
    @Deprecated
    public static byte[] generateAes128(SecureRandom secureRandom) {
        return BaseKeyGenerator.generateKey(secureRandom, 128, KEY_ALGORITHM);
    }

    /**
     * <p>生成对称密钥, 用于固定密钥的场合.
     * 不同系统平台相同seed生成结果可能不同, Android使用该方法, 相同seed仍会产生随机密钥.</p>
     *
     * @param seed 秘钥种子
     * @return 秘钥
     */
    @Deprecated
    public static byte[] generateAes128(byte[] seed) {
        return BaseKeyGenerator.generateKey(seed, 128, KEY_ALGORITHM);
    }

    /**
     * <p>生成256位AES对称密钥, 用于服务端场合, 产生随机密钥</p>
     *
     * <p>要求JDK8_162以上</p>
     *
     * @return 秘钥
     */
    public static byte[] generateAes256() {
        return BaseKeyGenerator.generateKey((SecureRandom) null, 256, KEY_ALGORITHM);
    }

    /**
     * <p>生成256位AES对称密钥, 用于服务端场合, 产生随机密钥.
     * 推荐使用AESKeyGenerator.generateAes128()代替, 使用自定义的SecureRandom可能会导致安全问题</p>
     *
     * <p>要求JDK8_162以上</p>
     *
     * @param secureRandom 随机数, 推荐使用AESKeyGenerator.generateAes128()代替, 使用自定义的SecureRandom可能会导致安全问题
     * @return 秘钥
     */
    @Deprecated
    public static byte[] generateAes256(SecureRandom secureRandom) {
        return BaseKeyGenerator.generateKey(secureRandom, 256, KEY_ALGORITHM);
    }

    /**
     * <p>生成对称密钥, 用于固定密钥的场合.
     * 不同系统平台相同seed生成结果可能不同, Android使用该方法, 相同seed仍会产生随机密钥.</p>
     *
     * <p>要求JDK8_162以上</p>
     *
     * @param seed 秘钥种子
     * @return 秘钥
     */
    @Deprecated
    public static byte[] generateAes256(byte[] seed) {
        return BaseKeyGenerator.generateKey(seed, 256, KEY_ALGORITHM);
    }

    /**
     * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey128(byte[] seed){
        return BaseKeyGenerator.generateShaKey128(seed);
    }

    /**
     * 利用SHA256摘要算法计算256位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey256(byte[] seed){
        return BaseKeyGenerator.generateShaKey256(seed);
    }

}
