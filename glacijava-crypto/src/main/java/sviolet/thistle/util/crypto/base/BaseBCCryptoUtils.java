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

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;

/**
 * [Bouncy castle]基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseBCCryptoUtils {

    /**
     * 根据密钥实例(公钥或私钥)计算SM2用于加密时, 密文C1区域的长度, 密文为C1C3C2或C1C2C3, C1区域为随机公钥点数据(ASN.1格式),
     * C2为密文数据, C3为摘要数据(SM3).
     *
     * @param keyParams 密钥实例(公钥或私钥)
     * @return 密文C1区域长度
     */
    public static int calculateSM2C1Length(ECKeyParameters keyParams) {
        return calculateSM2C1Length(keyParams.getParameters());
    }

    /**
     * 根据密钥实例(公钥或私钥)计算SM2用于加密时, 密文C1区域的长度, 密文为C1C3C2或C1C2C3, C1区域为随机公钥点数据(ASN.1格式),
     * C2为密文数据, C3为摘要数据(SM3).
     * domainParameters是椭圆曲线参数, 需要:椭圆曲线/G点/N(order)/H(cofactor)
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @return 密文C1区域长度
     */
    public static int calculateSM2C1Length(ECDomainParameters domainParameters) {
        return calculateSM2CurveLength(domainParameters) * 2 + 1;
    }

    /**
     * 根据椭圆曲线参数计算curveLength.
     * domainParameters是椭圆曲线参数, 需要:椭圆曲线/G点/N(order)/H(cofactor)
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H) 或 ECKeyParameters.getParameters()
     * @return curveLength
     */
    public static int calculateSM2CurveLength(ECDomainParameters domainParameters) {
        return (domainParameters.getCurve().getFieldSize() + 7) / 8;
    }

}
