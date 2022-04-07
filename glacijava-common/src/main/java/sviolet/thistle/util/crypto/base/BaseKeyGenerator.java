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

package sviolet.thistle.util.crypto.base;

import sviolet.thistle.util.crypto.DigestCipher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密钥生成基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
// About suppressed warnings: It's a util, the algorithm type should not be restricted
@SuppressWarnings({"lgtm[java/weak-cryptographic-algorithm]"})
public class BaseKeyGenerator {

    /**
     * <p>
     * 说明:<br>
     * <br>
     * 1.如果SecureRandom不设置种子, 在Linux中会从/dev/./urandom中获取内核熵, 作为种子生成器的种子, 再由种子生成器产生
     * SecureRandom的种子, 因此不设置种子产生的随机密钥相对比设置了自定义种子的安全.<br>
     * <br>
     * 2.SecureRandom.nextBytes方法是同步方法, 如果多线程用一个实例, 会造成一定的性能损失, 因此采用ThreadLocal, 每个线程
     * 一个SecureRandom实例. 每个SecureRandom实例化的时候(不设置种子), 会从种子生成器产生一个不同的种子, 因此产生的密钥
     * 也不会重复.<br>
     * </p>
     *
     * <p>
     * 单例/四线程/每线程1000000次:1000ms
     * ThreadLocal/四线程/每线程1000000次:400ms
     * </p>
     */
    private static ThreadLocal<SecureRandom> secureRandoms = new ThreadLocal<>();

    public static SecureRandom getSystemSecureRandom(){
        SecureRandom systemSecureRandom = secureRandoms.get();
        if (systemSecureRandom == null) {
            systemSecureRandom = new SecureRandom();
            secureRandoms.set(systemSecureRandom);
        }
        return systemSecureRandom;
    }

    /**
     * <p>生成对称密钥, 用于服务端场合, 产生随机密钥</p>
     *
     * @param secureRandom 如果需要产生随机密钥, 建议传入null, 采用系统内核熵作为种子更安全
     * @param bits 密钥位数(64/128/192/256...)
     * @param keyAlgorithm 密钥算法类型
     * @return 密钥
     */
    public static byte[] generateKey(SecureRandom secureRandom, int bits, String keyAlgorithm) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (secureRandom != null) {
            keyGenerator.init(bits, secureRandom);
        } else {
            keyGenerator.init(bits, getSystemSecureRandom());
        }
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * <p>生成对称密钥, 用于固定密钥的场合.
     * 不同系统平台相同seed生成结果可能不同, Android使用该方法, 相同seed仍会产生随机密钥.</p>
     *
     * @param seed 密钥种子
     * @param bits 密钥位数(64/128/192/256...)
     * @param keyAlgorithm 密钥算法类型
     * @return 密钥
     */
    public static byte[] generateKey(byte[] seed, int bits, String keyAlgorithm) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        SecureRandom secureRandom = new SecureRandom(seed);
        keyGenerator.init(bits, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey64(byte[] seed){
        byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
        byte[] password = new byte[8];
        System.arraycopy(sha, 0, password, 0, password.length);
        return password;
    }

    /**
     * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey128(byte[] seed){
        byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
        byte[] password = new byte[16];
        System.arraycopy(sha, 0, password, 0, password.length);
        return password;
    }

    /**
     * 利用SHA256摘要算法计算192位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey192(byte[] seed){
        byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
        byte[] password = new byte[24];
        System.arraycopy(sha, 0, password, 0, password.length);
        return password;
    }

    /**
     * 利用SHA256摘要算法计算256位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey256(byte[] seed){
        return DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
    }

}
