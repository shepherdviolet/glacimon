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

package com.github.shepherdviolet.glacimon.java.crypto.base;

import org.bouncycastle.asn1.*;

import java.math.BigInteger;

/**
 * 非标准数据修复器
 *
 * @author shepherdviolet
 */
public class NonStandardDataFixer {

    /**
     * 修复非标准的DER编码的签名数据.
     *
     * 20230320发现某签名服务器签名出来的DER格式签名不标准, 当R值偏大时, r[0] >= 0x80, 使得整个大数字变成了负数, 而R和S值是不能
     * 小于1的(见org.bouncycastle.crypto.signers.SM2Signer#verifySignature方法). 在标准的DER格式中, 遇到这种情况, 应该在前面
     * 增加一个字节0x00, 以保证数值为正整数. 顺带一提, RS格式的签名数据, 不会在前面追加0x00, 因为它要严格保证32+32字节.
     *
     * @param der DER编码的签名数据
     * @return 修复后的DER编码的签名数据, 修复失败返回原值
     */
    public static byte[] fixDerSign(byte[] der) {
        if (der == null) {
            return null;
        }
        // 只针对DER长度为70bytes的情况, 减少不必要的开销. 因为不标准的DER格式签名, RS值都只有32字节, 整个DER长度为70字节.
        if (der.length != 70) {
            return der;
        }
        try {
            ASN1Sequence asn1Sequence = DERSequence.getInstance(der);
            byte[] r = ((ASN1Integer) asn1Sequence.getObjectAt(0)).getValue().toByteArray();
            byte[] s = ((ASN1Integer) asn1Sequence.getObjectAt(1)).getValue().toByteArray();
            // 检查RS值是否为负数
            if (r[0] >= 0 && s[0] >= 0) {
                return der;
            }
            // 如果为负数, 则在前面追加0x00, 只需要在创建BigInteger时指定为正数就行
            BigInteger rInteger = new BigInteger(1, r);
            BigInteger sInteger = new BigInteger(1, s);
            ASN1EncodableVector encodableVector = new ASN1EncodableVector();
            encodableVector.add(new ASN1Integer(rInteger));
            encodableVector.add(new ASN1Integer(sInteger));
            return new DERSequence(encodableVector).getEncoded(ASN1Encoding.DER);
        } catch (Exception e) {
            return der;
        }
    }

}
