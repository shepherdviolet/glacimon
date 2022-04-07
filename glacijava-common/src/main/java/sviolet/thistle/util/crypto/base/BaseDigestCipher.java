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

import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.common.PlatformUtils;
import sviolet.thistle.util.file.FileUtils;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseDigestCipher {

    /**
     * 摘要byte[]
     *
     * @param bytes bytes
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digest(byte[] bytes, String type) {
        if (bytes == null){
            throw new NullPointerException("[DigestCipher]digest: bytes is null");
        }
        try {
            MessageDigest cipher = MessageDigest.getInstance(type);
            return cipher.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
        }
    }

    /**
     * 摘要输入流(处理完毕会关闭输入流)
     *
     * @param inputStream 输入流
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestInputStream(InputStream inputStream, String type) throws IOException {
        if (inputStream == null){
            throw new NullPointerException("[DigestCipher]digestInputStream: inputStream is null");
        }
        try {
            MessageDigest cipher = MessageDigest.getInstance(type);
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                cipher.update(buff, 0, size);
            }
            return cipher.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
        } catch (IOException e) {
            throw e;
        }finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    /**
     * 摘要文件, 根据运行时环境选择使用NIO或IO方式
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFile(File file, String type) throws IOException {
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK){
            //安卓API11以上使用NIO, API10以下会很慢
            if (PlatformUtils.ANDROID_VERSION < CryptoConstants.ANDROID_API11){
                return digestFileIo(file, type);
            } else {
                return digestFileNio(file, type);
            }
        }
        //能手动回收MappedByteBuffer则使用NIO
        if (FileUtils.isMappedByteBufferCanClean()){
            return digestFileNio(file, type);
        } else {
            return digestFileIo(file, type);
        }
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
        FileInputStream inputStream = null;
        FileChannel channel = null;
        MappedByteBuffer byteBuffer = null;
        long position = 0;

        try {
            inputStream = new FileInputStream(file);
            channel = inputStream.getChannel();
            MessageDigest cipher = MessageDigest.getInstance(type);
            //handle length > Integer.MAX_VALUE
            while (position < file.length()) {
                long size = file.length() - position;
                if (size > Integer.MAX_VALUE) {
                    size = Integer.MAX_VALUE;
                }
                try {
                    byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, position, size);
                    cipher.update(byteBuffer);
                } finally {
                    //尝试将MappedByteBuffer回收, 解决后续文件无法被读写删除的问题
                    FileUtils.cleanMappedByteBuffer(byteBuffer);
                }
                position += Integer.MAX_VALUE;
            }
            return cipher.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
        } catch (IOException e) {
            throw e;
        }finally {
            CloseableUtils.closeQuiet(inputStream);
            CloseableUtils.closeQuiet(channel);
        }
    }

    /**
     * 摘要文件普通方式, 阻塞较慢
     *
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFileIo(File file, String type) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file), CryptoConstants.BUFFER_SIZE << 2);
            MessageDigest cipher = MessageDigest.getInstance(type);
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                cipher.update(buff, 0, size);
            }
            return cipher.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
        } catch (IOException e) {
            throw e;
        }finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

}
