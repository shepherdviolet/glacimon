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
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;

import java.math.BigInteger;
import java.security.spec.ECFieldFp;

/**
 * SM2官方推荐椭圆曲线参数sm2p256v1,
 * 通讯双方必须使用相同的椭圆曲线
 *
 * @author S.Violet
 */
public class SM2DefaultCurve {

    //SM2默认椭圆曲线
    public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
    private static final BigInteger P = CURVE.getQ();
    private static final BigInteger A = CURVE.getA().toBigInteger();
    private static final BigInteger B = CURVE.getB().toBigInteger();
    public static final BigInteger N = CURVE.getOrder();
    public static final BigInteger H = CURVE.getCofactor();

    //SM2默认G点
    public static final ECPoint G_POINT = CURVE.createPoint(
            new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16),//x
            new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16)//y
    );

    //SM2默认参数集合(含:椭圆曲线/G点/N(order)/H(cofactor))
    public static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT, N, H);

    //SM2默认curveLength
    public static final int CURVE_LENGTH = BaseBCCryptoUtils.calculateSM2CurveLength(DOMAIN_PARAMS); // 32

    //SM2默认C1区域长度(SM2用于加密时密文C1区长度)
    public static final int C1_LENGTH = BaseBCCryptoUtils.calculateSM2C1Length(DOMAIN_PARAMS); // 65

    //SM2默认参数集合: 使用JDK实例表示椭圆参数, 用于将SEC1标准的私钥数据转为PKCS8标准的私钥数据
    public static final java.security.spec.ECParameterSpec EC_PARAM_SPEC_FOR_SEC1 = new java.security.spec.ECParameterSpec(
            new java.security.spec.EllipticCurve(new ECFieldFp(P), A, B),
            new java.security.spec.ECPoint(G_POINT.getAffineXCoord().toBigInteger(), G_POINT.getAffineYCoord().toBigInteger()),
            N,
            H.intValue()
    );

}
