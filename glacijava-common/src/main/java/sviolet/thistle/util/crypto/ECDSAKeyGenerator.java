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

import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.crypto.base.BaseAsymKeyGenerator;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * ECDSA秘钥生成工具
 * @author S.Violet
 */
public class ECDSAKeyGenerator {

    /**
     * 密钥类型:EC
     */
    public static final String ECDSA_KEY_ALGORITHM = "EC";

    /**
     * 随机生成ECDSA密钥对(secp256r1:256位)
     *
     * @return 密钥对
     */
    public static ECKeyPair generateKeyPair() {
        KeyPair keyPair;
        try {
            keyPair = BaseAsymKeyGenerator.generateEcKeyPair("secp256r1", ECDSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        return new ECKeyPair((ECPublicKey) keyPair.getPublic(), (ECPrivateKey) keyPair.getPrivate());
    }

    /**
     * <p>根据X509格式的公钥数据生成公钥</p>
     *
     * @param x509EncodedPublicKey X509格式公钥数据
     * @return 公钥
     */
    public static ECPublicKey generatePublicKeyByX509(byte[] x509EncodedPublicKey) throws InvalidKeySpecException {
        return (ECPublicKey) BaseAsymKeyGenerator.parsePublicKeyByX509(x509EncodedPublicKey, ECDSA_KEY_ALGORITHM);
    }

    /**
     * <p>根据PKCS8格式的私钥数据生成私钥</p>
     *
     * @param pkcs8EncodedPrivateKey PKCS8格式私钥数据
     * @return 私钥
     */
    public static ECPrivateKey generatePrivateKeyByPKCS8(byte[] pkcs8EncodedPrivateKey) throws InvalidKeySpecException {
        return (ECPrivateKey) BaseAsymKeyGenerator.parsePrivateKeyByPKCS8(pkcs8EncodedPrivateKey, ECDSA_KEY_ALGORITHM);
    }

    /**
     * 将私钥转为PKCS8格式的二进制数据
     * @param privateKey 私钥
     * @return PKCS8格式的私钥数据
     */
    public static byte[] encodePrivateKeyToPKCS8(ECPrivateKey privateKey) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.encodePrivateKeyToPKCS8(privateKey, ECDSA_KEY_ALGORITHM);
    }

    /**
     * 将公钥转为X509格式的二进制数据
     * @param publicKey 公钥
     * @return X509格式的公钥数据
     */
    public static byte[] encodePublicKeyToX509(ECPublicKey publicKey) throws InvalidKeySpecException {
        return BaseAsymKeyGenerator.encodePublicKeyToX509(publicKey, ECDSA_KEY_ALGORITHM);
    }

    public static class ECKeyPair {

        private ECPublicKey publicKey = null;
        private ECPrivateKey privateKey = null;

        public ECKeyPair(ECPublicKey publicKey, ECPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * 获取公钥
         */
        public ECPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * 获取私钥
         */
        public ECPrivateKey getPrivateKey() {
            return privateKey;
        }

        public byte[] getX509EncodedPublicKey() throws InvalidKeySpecException {
            return encodePublicKeyToX509(publicKey);
        }

        public byte[] getPKCS8EncodedPrivateKey() throws InvalidKeySpecException {
            return encodePrivateKeyToPKCS8(privateKey);
        }

        @Override
        public String toString() {
            try {
                return "ECKeyPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) + "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
            } catch (InvalidKeySpecException e) {
                return "ECKeyPair\n<exception>" + e.getMessage();
            }
        }
    }
}
