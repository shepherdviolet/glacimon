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

package com.github.shepherdviolet.glacimon.java.crypto.base;

import com.github.shepherdviolet.glacimon.java.misc.CloseableUtils;

import java.io.*;
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
 * @author shepherdviolet
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
     * 摘要文件
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFile(File file, String type) throws IOException {
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
