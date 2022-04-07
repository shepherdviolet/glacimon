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

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * [Bouncy castle]密钥生成基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseBCKeyGenerator {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /**
     * <p>生成对称密钥, 用于服务端场合, 产生随机密钥</p>
     *
     * @param secureRandom 如果需要产生随机密钥, 建议传入null, 采用系统内核熵作为种子更安全
     * @param bits 密钥位数(128...)
     * @param keyAlgorithm 密钥算法类型
     * @return 密钥
     */
    public static byte[] generateKey(SecureRandom secureRandom, int bits, String keyAlgorithm) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(keyAlgorithm, BouncyCastleProviderUtils.getProviderName());
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (secureRandom != null) {
            keyGenerator.init(bits, secureRandom);
        } else {
            keyGenerator.init(bits, BaseKeyGenerator.getSystemSecureRandom());
        }
        return keyGenerator.generateKey().getEncoded();
    }

}
