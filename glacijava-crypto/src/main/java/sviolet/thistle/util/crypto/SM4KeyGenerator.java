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

import sviolet.thistle.util.crypto.base.BaseBCKeyGenerator;

import java.security.SecureRandom;

/**
 * SM4秘钥生成工具
 *
 * @author S.Violet
 */
public class SM4KeyGenerator {

    public static final String KEY_ALGORITHM = "SM4";

    /**
     * <p>生成128位SM4对称密钥, 用于服务端场合, 产生随机密钥</p>
     *
     * @return 秘钥
     */
    public static byte[] generate128() {
        return BaseBCKeyGenerator.generateKey((SecureRandom) null, 128, KEY_ALGORITHM);
    }

}
