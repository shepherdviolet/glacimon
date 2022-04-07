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

import org.bouncycastle.crypto.Digest;
import sviolet.thistle.util.common.CloseableUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * [Bouncy castle]摘要基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseBCDigestCipher {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /**
     * 摘要byte[]
     *
     * @param bytes bytes
     * @param digest 摘要器实例
     * @return 摘要bytes
     */
    public static byte[] digest(byte[] bytes, Digest digest) {
        if (bytes == null){
            throw new NullPointerException("[DigestCipher]digest: bytes is null");
        }
        if (digest == null) {
            throw new NullPointerException("[DigestCipher]digest: digest instance is null");
        }
        digest.update(bytes, 0, bytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }

    /**
     * 摘要输入流(处理完毕会关闭输入流)
     *
     * @param inputStream 输入流
     * @param digest 摘要器实例
     * @return 摘要bytes
     */
    public static byte[] digestInputStream(InputStream inputStream, Digest digest) throws IOException {
        if (inputStream == null){
            throw new NullPointerException("[DigestCipher]digestInputStream: inputStream is null");
        }
        if (digest == null) {
            throw new NullPointerException("[DigestCipher]digestInputStream: digest instance is null");
        }
        try {
            byte[] buff = new byte[CryptoConstants.BUFFER_SIZE];
            int size;
            while((size = inputStream.read(buff)) != -1){
                digest.update(buff, 0, size);
            }
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            return result;
        }finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

}
