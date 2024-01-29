/*
 * Copyright (C) 2022-2023 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.helper.joda;

import org.joda.time.DateTime;
import org.joda.time.IllegalInstantException;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

/**
 * JodaTimeHelper测试
 */
public class JodaTimeHelperTest {

    @Test
    public void gapTest() {
        Assertions.assertEquals("1948-05-01T01:00:00.000+09:00", JodaTimeHelper.stringToJodaDateTime("1948-05-01", "yyyy-MM-dd").toString());
        Assertions.assertEquals("1948-05-01T01:00:00.000+09:00", JodaTimeHelper.stringToJodaDateTime("1948-05-01 00:00:00", "yyyy-MM-dd HH:mm:ss").toString());
        Assertions.assertEquals("1948-05-01T01:00:00.000+09:00", JodaTimeHelper.stringToJodaDateTime("1948-05-01 00:39:21", "yyyy-MM-dd HH:mm:ss").toString());
        Assertions.assertEquals("1948-05-01T01:00:01.000+09:00", JodaTimeHelper.stringToJodaDateTime("1948-05-01 01:00:01", "yyyy-MM-dd HH:mm:ss").toString());
        Assertions.assertEquals("1948-05-01T23:59:59.000+09:00", JodaTimeHelper.stringToJodaDateTime("1948-05-01 23:59:59", "yyyy-MM-dd HH:mm:ss").toString());
    }

    /**
     * 普通方式处理
     */
    @Test
    public void gapFailed() {
        Assertions.assertThrows(IllegalInstantException.class, () -> {
            TimeZone rawZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
            try {
                DateTime.parse("1948-05-01", DateTimeFormat.forPattern("yyyy-MM-dd"));
            } finally {
                TimeZone.setDefault(rawZone);
            }
        });
    }

}
