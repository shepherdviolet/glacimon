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

import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.base.BaseDigestCipher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * [国际算法]摘要工具
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * @author S.Violet
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
     * 摘要文件, 根据运行时环境选择使用NIO或IO方式
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFile(File file, String type) throws IOException {
        return BaseDigestCipher.digestFile(file, type);
    }

    /**
     * 摘要文件NIO方式, 较快<br/>
     *
     * 注意:非安卓平台使用该方法前, 请使用FileUtils.isMappedByteBufferCanClean()判断MappedByteBuffer是否能被手动回收,
     * 如果isMappedByteBufferCanClean返回false, 建议使用digestFileIo, 否则操作后, 文件将在一段时间内无法被读写删除<br/>
     *
     * 注意:安卓平台API11以上使用, API10以下会很慢<br/>
     *
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFileNio(File file, String type) throws IOException {
        return BaseDigestCipher.digestFileNio(file, type);
    }

    /**
     * 摘要文件普通方式, 阻塞较慢
     *
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFileIo(File file, String type) throws IOException {
        return BaseDigestCipher.digestFileIo(file, type);
    }

}
