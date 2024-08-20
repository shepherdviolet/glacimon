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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;

/**
 * 非标准数据修复器
 *
 * @author shepherdviolet
 */
public class NonStandardDataFixer {

    /**
     * (尝试)修复非标准的DER编码的签名数据.
     *
     * 20230320:
     * 发现某签名服务器签名出来的DER格式签名不标准, 当R值偏大时, r[0] >= 0x80, 使得整个大数字变成了负数, 而R和S值是不能
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

    /**
     * (尝试)修复非标准的X509证书中的签名数据.
     *
     * 20240820:
     * 发现某RA签发出来的SM2证书本身签名格式不标准(CA私钥对用户证书签名), 截取证书末尾的签名数据(ASN.1), 如下所示:
     * 30 45 02 21 008265......27 02 20 0039e7......43
     * 30表示type=SEQUENCE, 45表示SEQUENCE长度69 (0x45首位为0, 本身即为长度, 若首位为1, 表示长度区域的长度)
     * 02表示type=INTEGER, 21表示INTEGER长度为33, 008265......27为INTEGER的值
     * 02表示type=INTEGER, 20表示INTEGER长度为32, 0039e7......43为INTEGER的值
     * 问题出在0039e7......43, ASN.1标准要求正整数首位为1时, 前面补零(0x00, 防止变成负数),
     * 而0039e7......43的39首位不为1, 不应该补零, 但是这个RA签发的证书却补零了, 所以BouncyCastle解析签名数据失败, 导致证书验签失败.
     *
     * ASN.1格式参考可参考: https://blog.csdn.net/new9232/article/details/139205158
     *
     * @param certX509 X509格式的证书数据(需要修复其中的签名数据)
     * @return 尝试修复后的X509证书数据, 修复失败返回原证书
     */
    public static byte[] fixCertSign(byte[] certX509) {
        // 用ASN.1解析整个证书
        try (ASN1InputStream certAsn1InputStream = new ASN1InputStream(certX509)) {
            DLSequence certDlSequence = (DLSequence)certAsn1InputStream.readObject();
            // 最后一个是签名
            ASN1Encodable signObject = certDlSequence.getObjectAt(certDlSequence.size() - 1);
            byte[] signOctets = ((DERBitString) signObject).getOctets();

            try (ASN1InputStream signAsn1InputStream = new ASN1InputStream(signOctets)){
                // 尝试用ASN.1格式解析签名, 如果签名数据没问题则返回原证书
                signAsn1InputStream.readObject();
                return certX509;
            } catch (Exception e) {
                // 解析失败: 签名格式错误
                // 判断是否是"malformed integer"错误
                if (e.getCause() == null || !"malformed integer".equals(e.getCause().getMessage())) {
                    return certX509;
                }
                // 尝试修复签名
                signOctets = fixSignInCert(signOctets);
                // 重新组装证书
                ASN1EncodableVector certVector = new ASN1EncodableVector();
                for (int i = 0 ; i < certDlSequence.size() - 1 ; i++) {
                    certVector.add(certDlSequence.getObjectAt(i));
                }
                certVector.add(new DERBitString(signOctets));
                byte[] certDerEncoded = new DERSequence(certVector).getEncoded(ASN1Encoding.DER);
                return BaseBCCertificateUtils.parseCertificateByBouncyCastle(
                        new ByteArrayInputStream(certDerEncoded), BaseCertificateUtils.TYPE_X509).getEncoded();
            }
        } catch (Exception ex) {
            return certX509;
        }
    }

    /**
     * (尝试)修复非标准的X509证书中的签名数据.
     *
     * 20240820:
     * 发现某RA签发出来的SM2证书本身签名格式不标准(CA私钥对用户证书签名), 截取证书末尾的签名数据(ASN.1), 如下所示:
     * 30 45 02 21 008265......27 02 20 0039e7......43
     * 30表示type=SEQUENCE, 45表示SEQUENCE长度69 (0x45首位为0, 本身即为长度, 若首位为1, 表示长度区域的长度)
     * 02表示type=INTEGER, 21表示INTEGER长度为33, 008265......27为INTEGER的值
     * 02表示type=INTEGER, 20表示INTEGER长度为32, 0039e7......43为INTEGER的值
     * 问题出在0039e7......43, ASN.1标准要求正整数首位为1时, 前面补零(0x00, 防止变成负数),
     * 而0039e7......43的39首位不为1, 不应该补零, 但是这个RA签发的证书却补零了, 所以BouncyCastle解析签名数据失败, 导致证书验签失败.
     *
     * ASN.1格式参考可参考: https://blog.csdn.net/new9232/article/details/139205158
     *
     * @param cert X509证书(需要修复其中的签名数据)
     * @return 尝试修复后的证书, 修复失败返回原证书
     */
    public static X509Certificate fixCertSign(X509Certificate cert) {
        try (ASN1InputStream signAsn1InputStream = new ASN1InputStream(cert.getSignature())){
            // 尝试用ASN.1格式解析签名, 如果签名数据没问题则返回原证书
            signAsn1InputStream.readObject();
            return cert;
        } catch (Exception e) {
            // 解析失败: 签名格式错误
            // 判断是否是"malformed integer"错误
            if (e.getCause() == null || !"malformed integer".equals(e.getCause().getMessage())) {
                return cert;
            }
            // 用ASN.1解析整个证书
            try (ASN1InputStream certAsn1InputStream = new ASN1InputStream(BaseCertificateUtils.encodeCertificate(cert))) {
                DLSequence certDlSequence = (DLSequence)certAsn1InputStream.readObject();
                // 最后一个是签名
                ASN1Encodable signObject = certDlSequence.getObjectAt(certDlSequence.size() - 1);
                byte[] signOctets = ((DERBitString) signObject).getOctets();
                // 尝试修复签名
                signOctets = fixSignInCert(signOctets);
                // 重新组装证书
                ASN1EncodableVector certVector = new ASN1EncodableVector();
                for (int i = 0 ; i < certDlSequence.size() - 1 ; i++) {
                    certVector.add(certDlSequence.getObjectAt(i));
                }
                certVector.add(new DERBitString(signOctets));
                byte[] certDerEncoded = new DERSequence(certVector).getEncoded(ASN1Encoding.DER);
                return (X509Certificate) BaseBCCertificateUtils.parseCertificateByBouncyCastle(
                        new ByteArrayInputStream(certDerEncoded), BaseCertificateUtils.TYPE_X509);
            } catch (Exception ex) {
                return cert;
            }
        }
    }

    /**
     * 修复证书中的签名数据, 其他环节可以用BC的ASN.1组件解析/组包, 异常数据的读取只能手工进行
     */
    static byte[] fixSignInCert(byte[] signOctets) {
        int index = 0;
        // type=SEQUENCE
        if (signOctets[index++] != 0x30) {
            return signOctets;
        }
        // sign length
        int signLength = signOctets[index++];
        if (signLength < 0) {
            int lengthLen = signLength & 0x7F;
            byte[] lengthBuf = new byte[lengthLen];
            for (int i = 0; i < lengthLen; i++) {
                lengthBuf[i] = signOctets[index++];
            }
            signLength = new BigInteger(1, lengthBuf).intValue();
        }
        // 检查剩余长度与length是否相符
        if (signLength != signOctets.length - index) {
            return signOctets;
        }
        // r: type=INTEGER
        if (signOctets[index++] != 0x02) {
            return signOctets;
        }
        // r length
        int rLength = signOctets[index++];
        if (rLength < 0) {
            int lengthLen = rLength & 0x7F;
            byte[] lengthBuf = new byte[lengthLen];
            for (int i = 0; i < lengthLen; i++) {
                lengthBuf[i] = signOctets[index++];
            }
            rLength = new BigInteger(1, lengthBuf).intValue();
        }
        // 检查剩余长度与length是否相符
        if (rLength >= signOctets.length - index) {
            return signOctets;
        }
        // r value
        byte[] rValue = new byte[rLength];
        for (int i = 0; i < rLength; i++) {
            rValue[i] = signOctets[index++];
        }
        // s: type=INTEGER
        if (signOctets[index++] != 0x02) {
            return signOctets;
        }
        // s length
        int sLength = signOctets[index++];
        if (sLength < 0) {
            int lengthLen = sLength & 0x7F;
            byte[] lengthBuf = new byte[lengthLen];
            for (int i = 0; i < lengthLen; i++) {
                lengthBuf[i] = signOctets[index++];
            }
            sLength = new BigInteger(1, lengthBuf).intValue();
        }
        // 检查剩余长度与length是否相符
        if (sLength != signOctets.length - index) {
            return signOctets;
        }
        // s value
        byte[] sValue = new byte[sLength];
        for (int i = 0; i < sLength; i++) {
            sValue[i] = signOctets[index++];
        }
        // 去掉rs前面不正确的补零(最高位不是1是不用补0的), BigInteger指定1正整数, 前面的填充会标准化
        try {
            BigInteger rInteger = new BigInteger(1, rValue);
            BigInteger sInteger = new BigInteger(1, sValue);
            ASN1EncodableVector signVector = new ASN1EncodableVector();
            signVector.add(new ASN1Integer(rInteger));
            signVector.add(new ASN1Integer(sInteger));
            return new DERSequence(signVector).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            return signOctets;
        }
    }

}
