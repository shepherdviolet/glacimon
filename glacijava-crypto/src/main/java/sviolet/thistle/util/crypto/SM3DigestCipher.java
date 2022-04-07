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

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.base.BaseBCDigestCipher;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * [国密算法]SM3摘要工具
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * @author S.Violet
 */
public class SM3DigestCipher {

    /**
     * SM3摘要长度32bytes
     */
    public static final int SM3_HASH_LENGTH = 32;

    /**
     * 摘要类型:SM3
     */
    public static final String TYPE_SM3 = "SM3";

    private static final String DEFAULT_ENCODING = "utf-8";

    /**
     * 摘要byte[]
     *
     * @param bytes bytes
     * @param type 摘要算法, SM3DigestCipher.TYPE_SM3
     * @return 摘要bytes
     */
    public static byte[] digest(byte[] bytes,String type) {
        return BaseBCDigestCipher.digest(bytes, getDigest(type));
    }

    /**
     * 摘要字符串(.getBytes("UTF-8")), 注意抛出异常
     *
     * @param str 字符串
     * @param type 摘要算法, SM3DigestCipher.TYPE_SM3
     * @return 摘要bytes
     */
    public static byte[] digestStr(String str, String type){
        return digestStr(str, type, DEFAULT_ENCODING);
    }

    /**
     * 摘要字符串(.getBytes(encoding))
     *
     * @param str bytes
     * @param type 摘要算法, SM3DigestCipher.TYPE_SM3
     * @param encoding 编码方式
     * @return 摘要bytes
     */
    public static byte[] digestStr(String str, String type, String encoding){
        if (str == null){
            throw new NullPointerException("[DigestCipher]digestStr: str is null");
        }
        try {
            return BaseBCDigestCipher.digest(str.getBytes(encoding), getDigest(type));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("[DigestCipher]Unsupported Encoding:" + encoding, e);
        }
    }

    /**
     * 摘要十六进制字符串(ByteUtils.hexToBytes(hexStr))
     *
     * @param hexStr 十六进制字符串
     * @param type 摘要算法, SM3DigestCipher.TYPE_SM3
     * @return 摘要bytes
     */
    public static byte[] digestHexStr(String hexStr, String type){
        if (hexStr == null){
            throw new NullPointerException("[DigestCipher]digestHexStr: hexStr is null");
        }
        return BaseBCDigestCipher.digest(ByteUtils.hexToBytes(hexStr), getDigest(type));
    }

    /**
     * 摘要输入流, 处理完毕会关闭流
     * @param inputStream 输入流(处理完毕会关闭流)
     * @param type 摘要算法, SM3DigestCipher.TYPE_SM3
     * @return 摘要bytes
     */
    public static byte[] digestInputStream(InputStream inputStream, String type) throws IOException {
        return BaseBCDigestCipher.digestInputStream(inputStream, getDigest(type));
    }

    private static Digest getDigest(String type){
        switch (type) {
            case TYPE_SM3:
                return new SM3Digest();
            default:
                throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type);
        }
    }

}
