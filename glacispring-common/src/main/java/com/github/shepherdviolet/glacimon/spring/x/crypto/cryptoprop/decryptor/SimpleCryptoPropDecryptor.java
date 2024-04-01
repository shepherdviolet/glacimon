/*
 * Copyright (C) 2022-2024 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropConstants;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropEnv;
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.entity.CryptoPropDecryptException;

import static com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor.SimpleCryptoPropUtils.DECRYPT_KEY_NULL;
import static com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor.SimpleCryptoPropUtils.isKeyNull;

/**
 * [Spring属性解密] 简易版属性解密器 (只支持aes和rsa算法, 不支持协议扩展)
 *
 * @author shepherdviolet
 */
public class SimpleCryptoPropDecryptor extends AbstractCryptoPropDecryptor {

    private SimpleCryptoPropUtils.DecryptKey key = DECRYPT_KEY_NULL;

    public SimpleCryptoPropDecryptor() {
        logger.info("CryptoProp | SimpleCryptoPropDecryptor Enabled");
    }

    public SimpleCryptoPropDecryptor(String key) {
        logger.info("CryptoProp | SimpleCryptoPropDecryptor Enabled");
        setKey(key);
    }

    /**
     * 解密
     */
    @Override
    protected String doDecrypt(String cipher) {
        return SimpleCryptoPropUtils.decrypt(cipher, key);
    }

    /**
     * <pre>
     * 对称加密:
     * 密钥字符串: aes:IPRGkutx3FfsCYty (BASE64)
     * 密钥文件路径: aes:file:/home/yourname/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 密钥类路径: aes:classpath:config/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
     * 非对称加密:
     * 私钥字符串: rsa:MIICdwIB......KM2wnjk1ZY= (DER格式)
     * 私钥文件路径: rsa:file:/home/yourname/crypto_prop_private.pem (PEM格式, 文件里不要有多余的换行)
     * 私钥类路径: rsa:classpath:config/crypto_prop_private.pem (PEM格式, 文件里不要有多余的换行)
     * </pre>
     * @param rawKey 密钥
     * @throws CryptoPropDecryptException 异常
     */
    public synchronized void setKey(String rawKey) {
        if (isKeyNull(rawKey)) {
            rawKey = null;
        }
        // 如果key没有变化就不加载key
        if (rawKey == null && this.key.getRawKey() == null) {
            return;
        }
        if (rawKey != null && rawKey.equals(this.key.getRawKey())) {
            return;
        }
        // 解析密钥
        key = SimpleCryptoPropUtils.parseDecryptKey(rawKey);
        // 清空缓存
        wipeCache();
        // 日志
        logger.info("CryptoProp | Key set to: " + SimpleCryptoPropUtils.hidePartially(rawKey) + " (hide partially)");
        if (key.getAesKey() != null) {
            logger.info("CryptoProp | AES key sha256: " + SimpleCryptoPropUtils.sha256(key.getAesKeyString()));
        }
        if (key.getRsaKey() != null) {
            logger.info("CryptoProp | RSA decrypt key sha256: " + SimpleCryptoPropUtils.sha256(key.getRsaKeyString()));
        }
    }

    @Override
    public void setEnv(CryptoPropEnv env) {
        // 由于BeanFactoryPostProcessor早于Bean实例化, CryptoPropBeanFactoryPostProcessor自身和它依赖的
        // Bean无法通过@Value注入需要的参数, 我们只能从Environment和PropertySourcesPlaceholderConfigurer获取Spring启动早期的参数(属性).
        // CryptoPropBeanFactoryPostProcessor会创建一个CryptoPropEnv, 传递给它依赖的Bean, 供它们获取需要的参数.
        setKey(env.getProperty(CryptoPropConstants.OPTION_DECRYPT_KEY));
    }

    @Override
    protected void printLogWhenKeyNull(String name, String value) {
        logger.warn("CryptoProp | Can not decrypt cipher '" + value + "', because the decrypt key '" + CryptoPropConstants.OPTION_DECRYPT_KEY + "' is null");
    }

}
