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

import sviolet.thistle.util.conversion.ByteUtils;

/**
 * 基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseCryptoUtils {

    public static final byte SM2_CIPHER_TEXT_PREFIX_UNCOMPRESSED = 0x04;

    /**
     * 将坐标(X/Y)转为ASN.1编码的坐标数据(非压缩)
     * @param xBytes 坐标X, 字节形式(bigInteger.toByteArray()获得)
     * @param yBytes 坐标Y, 字节形式(bigInteger.toByteArray()获得)
     * @return ASN.1编码的坐标数据(非压缩)
     */
    public static byte[] pointToASN1Encoding(byte[] xBytes, byte[] yBytes) {
        if (xBytes == null) {
            throw new NullPointerException("xBytes == null");
        }
        if (yBytes == null) {
            throw new NullPointerException("yBytes == null");
        }

        // 去掉前面的0x00
        xBytes = ByteUtils.trimHeader(xBytes);
        yBytes = ByteUtils.trimHeader(yBytes);

        byte[] asn1Encoding = new byte[1 + xBytes.length + yBytes.length];
        asn1Encoding[0] = SM2_CIPHER_TEXT_PREFIX_UNCOMPRESSED;
        System.arraycopy(xBytes, 0, asn1Encoding, 1, xBytes.length);
        System.arraycopy(yBytes, 0, asn1Encoding, 1 + xBytes.length, yBytes.length);
        return asn1Encoding;
    }

}
