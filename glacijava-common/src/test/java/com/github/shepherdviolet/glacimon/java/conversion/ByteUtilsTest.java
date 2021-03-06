/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.java.conversion;

import org.junit.Assert;
import org.junit.Test;

public class ByteUtilsTest {

    @Test
    public void leftTrim() {
        Assert.assertArrayEquals(ByteUtils.hexToBytes("1a1a1a"),
                ByteUtils.leftTrim(ByteUtils.hexToBytes("1a1a1a")));
        Assert.assertArrayEquals(ByteUtils.hexToBytes("1a1a1a"),
                ByteUtils.leftTrim(ByteUtils.hexToBytes("001a1a1a")));
        Assert.assertArrayEquals(ByteUtils.hexToBytes("1a1a1a"),
                ByteUtils.leftTrim(ByteUtils.hexToBytes("00001a1a1a")));
        Assert.assertArrayEquals(ByteUtils.hexToBytes(""),
                ByteUtils.leftTrim(ByteUtils.hexToBytes("00")));
        Assert.assertArrayEquals(ByteUtils.hexToBytes(""),
                ByteUtils.leftTrim(ByteUtils.hexToBytes("0000")));
    }

}
