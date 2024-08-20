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

import com.github.shepherdviolet.glacimon.java.conversion.ByteUtils;
import com.github.shepherdviolet.glacimon.java.crypto.AdvancedCertificateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;

public class NonStandardDataFixerTest {

    @Test
    public void fixSignInCertTest() {
        // 正常修复
        testFixSignInsert("300c020401234567020476543210", "300c020401234567020476543210");
        testFixSignInsert("300c020401234567020476543210", "300d02040123456702050076543210");
        testFixSignInsert("300c020401234567020476543210", "300d02050001234567020476543210");
        testFixSignInsert("300c020401234567020476543210", "300e0205000123456702050076543210");
        testFixSignInsert("300d02050087654321020476543210", "300c020487654321020476543210");
        testFixSignInsert("300e0205008765432102050087654321", "300c020487654321020487654321");

        // 长度变长测试
        testFixSignInsert("300c020401234567020476543210", "30810c020401234567020476543210");
        testFixSignInsert("300c020401234567020476543210", "3082000c020401234567020476543210");
        testFixSignInsert("300c020401234567020476543210", "300e0282000401234567020476543210");
        testFixSignInsert("300c020401234567020476543210", "300f020401234567028300000476543210");

        // 异常返回原值: 开头0x30
        testFixSignInsert("9999999999999999999999999999", "9999999999999999999999999999");
        // 异常返回原值: 总长不符
        testFixSignInsert("300c02040123456702047654321090", "300c02040123456702047654321090");
        testFixSignInsert("300c0204012345670204765432", "300c0204012345670204765432");
        // 异常返回原值: rs类型不符
        testFixSignInsert("300c030401234567020476543210", "300c030401234567020476543210");
        testFixSignInsert("300c020401234567060476543210", "300c020401234567060476543210");
        // 异常返回原值: rs剩余长度不符
        testFixSignInsert("30050204012345", "30050204012345");
        testFixSignInsert("300b0204012345670204765432", "300b0204012345670204765432");
        testFixSignInsert("300d02040123456702047654321010", "300d02040123456702047654321010");
    }

    private void testFixSignInsert(String expected, String input) {
        Assertions.assertEquals(expected,
                ByteUtils.bytesToHex(NonStandardDataFixer.fixSignInCert(ByteUtils.hexToBytes(input))));
    }

    @Test
    public void fixCertSign() throws CertificateException, NoSuchProviderException {
        String ca = "30820225308201caa003020102020500a1a4cd27300a06082a811ccf55018375306a310b300906035504061302434e3111300f06035504080c085a68656a69616e67310f300d06035504070c064e696e67626f31133011060355040a0c0a4d7920436f6d70616e793110300e060355040b0c07495420446570743110300e06035504030c0754657374204341301e170d3234303832303131343835395a170d3334303831383131343835395a306a310b300906035504061302434e3111300f06035504080c085a68656a69616e67310f300d06035504070c064e696e67626f31133011060355040a0c0a4d7920436f6d70616e793110300e060355040b0c07495420446570743110300e06035504030c07546573742043413059301306072a8648ce3d020106082a811ccf5501822d03420004fce968e752ddc89cc10314e7583a2484e532fbe6410036fbd2e01c64da3b12ecd755e8da4756f7930ae1475adfa20a6050d20c23fcbf3bd98cdd72c85e6b9e9aa35d305b301d0603551d0e041604143bf81fde561956bab3ddc95ed1372dadf19d7c3e301f0603551d230418301680143bf81fde561956bab3ddc95ed1372dadf19d7c3e300c0603551d13040530030101ff300b0603551d0f040403020186300a06082a811ccf550183750349003046022100bbcbc59c4375b89088823c286c2bb0d28ce953a1beb81c582b570edb56d5e7cf022100ce389c2dec801f5efb8c9695ea6609c0e81bd3fe79de0bd44dcecd89dd7fff35";
        String good = "30820246308201eca003020102020500884aea7f300a06082a811ccf55018375306a310b300906035504061302434e3111300f06035504080c085a68656a69616e67310f300d06035504070c064e696e67626f31133011060355040a0c0a4d7920436f6d70616e793110300e060355040b0c07495420446570743110300e06035504030c0754657374204341301e170d3234303832303131343835395a170d3334303831383131343835395a306c310b300906035504061302434e3111300f06035504080c085a68656a69616e67310f300d06035504070c064e696e67626f31133011060355040a0c0a4d7920436f6d70616e793110300e060355040b0c07495420446570743112301006035504030c095465737420557365723059301306072a8648ce3d020106082a811ccf5501822d0342000488085c2c57921b631aa6b6dedc251e5757846b7b493004756cbd062122657e883eaca53f5fb25e923f73e0140b8870c6c3c75938ce46aba919a7db50f4453e26a37d307b301d0603551d0e04160414e9e829960f58bfe86e1d0b8069c53179d9c03979301f0603551d230418301680143bf81fde561956bab3ddc95ed1372dadf19d7c3e30090603551d1304023000300b0603551d0f0404030204f030210603551d11041a3018820a312e686f73742e636f6d820a322e686f73742e636f6d300a06082a811ccf55018375034800304502204899096973f27abecca96dd9614fa7de11336118a8c034ff67abe012e85012f602210088a67803c06d52a39ee0dbcd32ca47e87c5266490a2f3484b7ec841cb2ab2e38";
        String bad = "30820247308201eca003020102020500884aea7f300a06082a811ccf55018375306a310b300906035504061302434e3111300f06035504080c085a68656a69616e67310f300d06035504070c064e696e67626f31133011060355040a0c0a4d7920436f6d70616e793110300e060355040b0c07495420446570743110300e06035504030c0754657374204341301e170d3234303832303131343835395a170d3334303831383131343835395a306c310b300906035504061302434e3111300f06035504080c085a68656a69616e67310f300d06035504070c064e696e67626f31133011060355040a0c0a4d7920436f6d70616e793110300e060355040b0c07495420446570743112301006035504030c095465737420557365723059301306072a8648ce3d020106082a811ccf5501822d0342000488085c2c57921b631aa6b6dedc251e5757846b7b493004756cbd062122657e883eaca53f5fb25e923f73e0140b8870c6c3c75938ce46aba919a7db50f4453e26a37d307b301d0603551d0e04160414e9e829960f58bfe86e1d0b8069c53179d9c03979301f0603551d230418301680143bf81fde561956bab3ddc95ed1372dadf19d7c3e30090603551d1304023000300b0603551d0f0404030204f030210603551d11041a3018820a312e686f73742e636f6d820a322e686f73742e636f6d300a06082a811ccf5501837503490030460221004899096973f27abecca96dd9614fa7de11336118a8c034ff67abe012e85012f602210088a67803c06d52a39ee0dbcd32ca47e87c5266490a2f3484b7ec841cb2ab2e38";
        X509Certificate caCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(ByteUtils.hexToBytes(ca));
        X509Certificate goodCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(ByteUtils.hexToBytes(good));
        X509Certificate badCert = AdvancedCertificateUtils.parseX509ToCertificateAdvanced(ByteUtils.hexToBytes(bad));

        // 关闭自动修复 (默认是开启的)
        System.setProperty("glacijava.crypto.signautofix", "false");
        CryptoConstants.reload();

        // 本身就好的证书
        AdvancedCertificateUtils.verifyCertificateByIssuers(goodCert, new Date(), new SimpleIssuerProvider(Collections.singletonList(caCert)));

        // 坏的证书不修复会报错
        Assertions.assertThrows(CertificateException.class, () -> {
                AdvancedCertificateUtils.verifyCertificateByIssuers(badCert, new Date(), new SimpleIssuerProvider(Collections.singletonList(caCert)));
        });

        // 坏的证书修复后
        AdvancedCertificateUtils.verifyCertificateByIssuers(
                AdvancedCertificateUtils.fixCertSign(badCert),
                new Date(), new SimpleIssuerProvider(Collections.singletonList(caCert)));

        // 坏的证书数据修复后
        AdvancedCertificateUtils.verifyCertificateByIssuers(
                AdvancedCertificateUtils.parseX509ToCertificateAdvanced(AdvancedCertificateUtils.fixCertSign(ByteUtils.hexToBytes(bad))),
                new Date(), new SimpleIssuerProvider(Collections.singletonList(caCert)));

        // 重新开启自动修复
        System.setProperty("glacijava.crypto.signautofix", "true");
        CryptoConstants.reload();

        // 自动修复
        AdvancedCertificateUtils.verifyCertificateByIssuers(badCert, new Date(), new SimpleIssuerProvider(Collections.singletonList(caCert)));

    }

}
