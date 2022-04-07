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

import java.io.UnsupportedEncodingException;

public class StringUtilsTest {

    @Test
    public void truncateByUtf8ByteLength() {

        String s = "11喵aa汪";
//        System.out.println(ByteUtils.bytesToHex(s.getBytes(StandardCharsets.UTF_8)));

        truncateByUtf8ByteLength0(s, 11, "11喵aa汪");
        truncateByUtf8ByteLength0(s, 10, "11喵aa汪");
        truncateByUtf8ByteLength0(s, 9, "11喵aa");
        truncateByUtf8ByteLength0(s, 8, "11喵aa");
        truncateByUtf8ByteLength0(s, 7, "11喵aa");
        truncateByUtf8ByteLength0(s, 6, "11喵a");
        truncateByUtf8ByteLength0(s, 5, "11喵");
        truncateByUtf8ByteLength0(s, 4, "11");
        truncateByUtf8ByteLength0(s, 3, "11");
        truncateByUtf8ByteLength0(s, 2, "11");
        truncateByUtf8ByteLength0(s, 1, "1");
        truncateByUtf8ByteLength0(s, 0, "");
        truncateByUtf8ByteLength0(s, -1, "");
        truncateByUtf8ByteLength0(null, 1, null);

    }

    private void truncateByUtf8ByteLength0(String string, int toLength, String expected) {
        Assert.assertEquals(expected, StringUtils.truncateByUtf8ByteLength(string, toLength));
    }

    @Test
    public void truncateByGbkByteLength() throws UnsupportedEncodingException {

        String s = "11喵吱aa汪吱";
//        System.out.println(ByteUtils.bytesToHex(s.getBytes("GBK")));

        truncateByGbkByteLength0(s, 13, "11喵吱aa汪吱");
        truncateByGbkByteLength0(s, 12, "11喵吱aa汪吱");
        truncateByGbkByteLength0(s, 11, "11喵吱aa汪");
        truncateByGbkByteLength0(s, 10, "11喵吱aa汪");
        truncateByGbkByteLength0(s, 9, "11喵吱aa");
        truncateByGbkByteLength0(s, 8, "11喵吱aa");
        truncateByGbkByteLength0(s, 7, "11喵吱a");
        truncateByGbkByteLength0(s, 6, "11喵吱");
        truncateByGbkByteLength0(s, 5, "11喵");
        truncateByGbkByteLength0(s, 4, "11喵");
        truncateByGbkByteLength0(s, 3, "11");
        truncateByGbkByteLength0(s, 2, "11");
        truncateByGbkByteLength0(s, 1, "1");
        truncateByGbkByteLength0(s, 0, "");
        truncateByGbkByteLength0(s, -1, "");
        truncateByGbkByteLength0(null, 1, null);

    }

    private void truncateByGbkByteLength0(String string, int toLength, String expected) {
        Assert.assertEquals(expected, StringUtils.truncateByGbkByteLength(string, toLength));
    }

    @Test
    public void leftPaddingToLength(){
        Assert.assertEquals("", StringUtils.leftPaddingToLength("12345678", 0, 0, '0'));
        Assert.assertEquals("345678", StringUtils.leftPaddingToLength("12345678", 6, 6, '0'));
        Assert.assertEquals("0012345678", StringUtils.leftPaddingToLength("12345678", 10, 10, '0'));
        Assert.assertEquals("12345678", StringUtils.leftPaddingToLength("12345678", 6, 10, '0'));
        Assert.assertEquals("345678", StringUtils.leftPaddingToLength("12345678", 4, 6, '0'));
        Assert.assertEquals("012345678", StringUtils.leftPaddingToLength("12345678", 9, 12, '0'));
    }

    @Test
    public void rightPaddingToLength(){
        Assert.assertEquals("", StringUtils.rightPaddingToLength("12345678", 0, 0, '0'));
        Assert.assertEquals("123456", StringUtils.rightPaddingToLength("12345678", 6, 6, '0'));
        Assert.assertEquals("1234567800", StringUtils.rightPaddingToLength("12345678", 10, 10, '0'));
        Assert.assertEquals("12345678", StringUtils.rightPaddingToLength("12345678", 6, 10, '0'));
        Assert.assertEquals("123456", StringUtils.rightPaddingToLength("12345678", 4, 6, '0'));
        Assert.assertEquals("123456780", StringUtils.rightPaddingToLength("12345678", 9, 12, '0'));
    }

    @Test
    public void leftTrimToLength(){
        Assert.assertEquals("12345678", StringUtils.leftTrimToLength("12345678", 0, '0'));
        Assert.assertEquals("12345678", StringUtils.leftTrimToLength("0012345678", 0, '0'));
        Assert.assertEquals("", StringUtils.leftTrimToLength("0000", 0, '0'));
        Assert.assertEquals("0", StringUtils.leftTrimToLength("0000", 1, '0'));
        Assert.assertEquals("00", StringUtils.leftTrimToLength("0000", 2, '0'));
        Assert.assertEquals("0000", StringUtils.leftTrimToLength("0000", 5, '0'));
    }

}
