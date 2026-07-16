/*
 * Copyright (C) 2022-2026 S.Violet
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

package com.github.shepherdviolet.glacimon.java.crypto.experiment;

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.io.FileUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * [CTF用] 用BigInteger实现的RSA加解密工具
 *
 * m:      明文(plain)
 * c:      密文(cipher)
 * n:      模数
 * φ(n):   n的欧拉函数 (可以写为phi), phi = (p-1) * (q-1)
 * e:      公钥指数.
 * d:      私钥指数, d是e模φ(n)的模逆元, 即: ed ≡ 1 (mod φ(n))
 * (n, e): RSA公钥
 * (n, d): RSA私钥
 *
 * 公钥加密 (m是明文, 必须整数且小于n) (c是密文):
 *     m^e ≡ c (mod n)
 * 私钥解密 (m是明文, 必须整数且小于n) (c是密文):
 *     c^d ≡ m (mod n)
 * 私钥签名 (m是待签名数据, 必须整数且小于n) (c是签名值):
 *     m^d ≡ c (mod n)
 * 公钥验签 (m是待签名数据, 必须整数且小于n) (c是签名值):
 *     c^e ≡ m (mod n)
 *
 * @author shepherdviolet
 */
public class CtfRsaUtils {

    /**
     * 用e和n对m进行加密
     * m^e ≡ c (mod n)
     */
    public static BigInteger encrypt(BigInteger m, BigInteger e, BigInteger n) {
        return m.modPow(e, n);
    }

    /**
     * 用d和n对c进行解密
     * c^d ≡ m (mod n)
     */
    public static BigInteger decrypt(BigInteger c, BigInteger d, BigInteger n) {
        return c.modPow(d, n);
    }

    /**
     * 用pqe计算d
     * phi = (p-1) * (q-1)
     * ed ≡ 1 (mod φ(n))
     */
    public static BigInteger pqeToD(BigInteger p, BigInteger q, BigInteger e) {
        BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        BigInteger phi = pMinus1.multiply(qMinus1);
        return e.modInverse(phi);
    }

    /**
     * 用pq求n
     * n = p * q
     */
    public static BigInteger pqToN(BigInteger p, BigInteger q) {
        return p.multiply(q);
    }

    /**
     * 用pqe对m进行加密
     */
    public static BigInteger encryptWithPQE(BigInteger m, BigInteger p, BigInteger q, BigInteger e) {
        return encrypt(m, e, pqToN(p, q));
    }

    /**
     * 用pqe对c进行解密
     */
    public static BigInteger decryptWithPQE(BigInteger c, BigInteger p, BigInteger q, BigInteger e) {
        return decrypt(c, pqeToD(p, q, e), pqToN(p, q));
    }

    // 转换工具 ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 从文件中读取BigInteger (支持纯二进制文件，或者存了10进制/16进制/Base64文本的文件)
     */
    public static BigInteger readBigIntegerFromFile(java.io.File file, String charset) throws IOException, FileUtils.LengthOutOfLimitException {
        byte[] bytes = FileUtils.readBytes(file, 10 * 1024 * 1024);
        if (bytes.length == 0) {
            return BigInteger.ZERO;
        }
        if (charset == null) {
            charset = Charset.defaultCharset().name();
        }
        try {
            String txt = new String(bytes, charset).trim();
            if (!txt.matches("^[A-Za-z0-9\\s+/=_Xx-]+$")) {
                return new BigInteger(1, bytes);
            }
            return stringToBigInteger(txt);
        } catch (Exception e) {
            return new BigInteger(1, bytes);
        }
    }

    /**
     * 文本转BigInteger (支持10进制、0x开头的16进制、纯16进制、Base64)
     */
    public static BigInteger stringToBigInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigInteger.ZERO;
        }
        String clean = text.trim();
        if (clean.startsWith("0x") || clean.startsWith("0X")) {
            return new BigInteger(clean.substring(2), 16);
        }
        if (clean.matches("^[0-9]+$")) {
            return new BigInteger(clean, 10);
        }
        if (clean.matches("^[0-9a-fA-F]+$") && clean.length() >= 8) {
            // 为了减少误判(abc之类的), 十六进制要8位以上才算
            return new BigInteger(clean, 16);
        }
        if (clean.matches("^[A-Za-z0-9+/=\\s\\r\\n]+$")) {
            try {
                byte[] decoded = Base64Utils.decode(clean);
                if (decoded != null && decoded.length > 0) {
                    return new BigInteger(1, decoded);
                }
            } catch (Exception ignored) {
            }
        }
        return new BigInteger(1, text.getBytes());
    }

    /**
     * BigInteger转原始文本
     */
    public static String bigIntegerToString(BigInteger biginteger, String charset) {
        if (biginteger == null) {
            return "";
        }
        if (charset == null) {
            charset = Charset.defaultCharset().name();
        }
        try {
            return new String(biginteger.toByteArray(), charset);
        } catch (Exception e) {
            return new String(biginteger.toByteArray());
        }
    }

}
