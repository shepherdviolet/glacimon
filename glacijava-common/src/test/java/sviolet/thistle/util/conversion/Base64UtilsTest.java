/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.thistle.util.conversion;

import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.crypto.DigestCipher;

import java.nio.charset.StandardCharsets;

public class Base64UtilsTest {

    @Test
    public void test(){

        String plain = "我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%我是123456ABCDefgh$#%@$#%";

        String s = new String(Base64Utils.encode(plain.getBytes(StandardCharsets.UTF_8)));
        Assert.assertEquals(
                "5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl",
                s);
        s = new String(Base64Utils.decode(s.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        Assert.assertEquals(
                plain,
                s);

        s = Base64Utils.encodeToString(plain.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(
                "5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl",
                s);
        s = new String(Base64Utils.decode(s));
        Assert.assertEquals(
                plain,
                s);

        s = Base64Utils.encodeToMimeString(plain.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(
                "5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gk\r\n" +
                        "IyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJD\r\n" +
                        "RGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIz\r\n" +
                        "NDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR\r\n" +
                        "5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVA\r\n" +
                        "JCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl",
                s);
        s = new String(Base64Utils.decode(s));
        Assert.assertEquals(
                plain,
                s);

        s = Base64Utils.encodeToMimeString(plain.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(
                "5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gk\r\n" +
                        "IyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJD\r\n" +
                        "RGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIz\r\n" +
                        "NDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR\r\n" +
                        "5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVA\r\n" +
                        "JCMl5oiR5pivMTIzNDU2QUJDRGVmZ2gkIyVAJCMl",
                s);
        s = new String(Base64Utils.decode(s.getBytes(StandardCharsets.UTF_8)));
        Assert.assertEquals(
                plain,
                s);

        s = Base64Utils.encodeToUrlSafeString(DigestCipher.digest(plain.getBytes(), DigestCipher.TYPE_SHA512));
        Assert.assertEquals(
                "fuHb4zQto6llioZvkrisqzWl8e9rw0_XJG4bvHYjHLSavrRNBLeWZQSVBg-YeYlhkjbc3TPIh3JZ9pm4LyjmbQ==",
                s);
        byte[] b = Base64Utils.decodeFromUrlSafeString(s);
        Assert.assertArrayEquals(
                DigestCipher.digest(plain.getBytes(), DigestCipher.TYPE_SHA512),
                b);
    }

}
