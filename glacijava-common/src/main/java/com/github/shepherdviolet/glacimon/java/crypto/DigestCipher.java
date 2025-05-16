/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.java.crypto;

import com.github.shepherdviolet.glacimon.java.conversion.ByteUtils;
import com.github.shepherdviolet.glacimon.java.crypto.base.BaseDigestCipher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * [国际算法]摘要工具
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * @author shepherdviolet
 */
public class DigestCipher {

    /**
     * 摘要类型:MD5
     */
    public static final String TYPE_MD5 = "MD5";

    /**
     * 摘要类型:SHA1
     */
    public static final String TYPE_SHA1 = "SHA1";

    /**
     * 摘要类型:SHA256
     */
    public static final String TYPE_SHA256 = "SHA-256";

    /**
     * 摘要类型:SHA-512
     */
    public static final String TYPE_SHA512 = "SHA-512";

    private static final String DEFAULT_ENCODING = "utf-8";

    /**
     * 摘要byte[]
     *
     * @param bytes bytes
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digest(byte[] bytes,String type) {
        return BaseDigestCipher.digest(bytes, type);
    }

    /**
     * 摘要字符串(.getBytes("UTF-8")), 注意抛出异常
     *
     * @param str 字符串
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestStr(String str, String type){
        return digestStr(str, type, DEFAULT_ENCODING);
    }

    /**
     * 摘要字符串(.getBytes(encoding))
     *
     * @param str bytes
     * @param type 摘要算法
     * @param encoding 编码方式
     * @return 摘要bytes
     */
    public static byte[] digestStr(String str, String type, String encoding){
        if (str == null){
            throw new NullPointerException("[DigestCipher]digestStr: str is null");
        }
        try {
            return BaseDigestCipher.digest(str.getBytes(encoding), type);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("[DigestCipher]Unsupported Encoding:" + encoding, e);
        }
    }

    /**
     * 摘要十六进制字符串(ByteUtils.hexToBytes(hexStr))
     *
     * @param hexStr 十六进制字符串
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestHexStr(String hexStr, String type){
        if (hexStr == null){
            throw new NullPointerException("[DigestCipher]digestHexStr: hexStr is null");
        }
        return BaseDigestCipher.digest(ByteUtils.hexToBytes(hexStr), type);
    }

    /**
     * 摘要输入流, 处理完毕会关闭流
     * @param inputStream 输入流(处理完毕会关闭流)
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestInputStream(InputStream inputStream, String type) throws IOException {
        return BaseDigestCipher.digestInputStream(inputStream, type);
    }

    /**
     * 摘要文件
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFile(File file, String type) throws IOException {
        return BaseDigestCipher.digestFile(file, type);
    }

}
