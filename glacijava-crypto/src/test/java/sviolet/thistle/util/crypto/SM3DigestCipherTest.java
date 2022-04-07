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

package sviolet.thistle.util.crypto;

import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.conversion.ByteUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SM3DigestCipherTest {

    private static final String HELLO_RESULT = "becbbfaae6548b8bf0cfcad5a27183cd1be6093b1cceccc303d9c61d0a645268";

    @Test
    public void common() throws IOException {

        //摘要字节
        byte[] hash = SM3DigestCipher.digest("hello".getBytes("utf-8"), SM3DigestCipher.TYPE_SM3);

        Assert.assertEquals(HELLO_RESULT, ByteUtils.bytesToHex(hash));

        //摘要字符串
        hash = SM3DigestCipher.digestStr("hello", SM3DigestCipher.TYPE_SM3, "utf-8");

        Assert.assertEquals(HELLO_RESULT, ByteUtils.bytesToHex(hash));

        //摘要十六进制字符串
        hash = SM3DigestCipher.digestHexStr(ByteUtils.bytesToHex("hello".getBytes("utf-8")), SM3DigestCipher.TYPE_SM3);

        Assert.assertEquals(HELLO_RESULT, ByteUtils.bytesToHex(hash));

        //摘要输入流
        hash = SM3DigestCipher.digestInputStream(new ByteArrayInputStream("hello".getBytes("utf-8")), SM3DigestCipher.TYPE_SM3);

        Assert.assertEquals(HELLO_RESULT, ByteUtils.bytesToHex(hash));
    }

}
