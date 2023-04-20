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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

public class JodaTimeDemo {

    public static void main(String[] args) {

        // String -> LocalDateTime(不带时区)
        LocalDateTime localDateTime = LocalDateTime.parse("2023-04-20 19:37:58", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(localDateTime);

        // String -> LocalDate(不带时区)
        LocalDate localDate = LocalDate.parse("2023-04-20 19:37:58", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(localDate);

        // String -> DateTime(带时区)
        // 这个工具类解决了DateTime#parse方法可能会抛出IllegalInstantException异常的"问题", 如果用DateTime.parse转, 有可能会抛出那个异常.
        DateTime dateTime = JodaTimeHelper.stringToJodaDateTime("2023-04-20 19:37:58", "yyyy-MM-dd HH:mm:ss");
        System.out.println(dateTime);

        // [LocalDateTime]判断localDateTime是否在当前时间之前/之后/相等
        localDateTime.isBefore(LocalDateTime.now());
        localDateTime.isAfter(LocalDateTime.now());
        localDateTime.isEqual(LocalDateTime.now());

        // [LocalDate]判断localDate是否在当前时间之前/之后/相等
        localDate.isBefore(LocalDate.now());
        localDate.isAfter(LocalDate.now());
        localDate.isEqual(LocalDate.now());

        // [DateTime]判断dateTime是否在当前时间之前/之后/相等
        dateTime.isBefore(DateTime.now());
        dateTime.isAfter(DateTime.now());
        dateTime.isEqual(DateTime.now());

        // JODA -> java.util.Date
        localDateTime.toDate();
        localDate.toDateTimeAtStartOfDay(); // 这个是获取当天开始的时间, 碰到夏令时起始日, 可能不是00:00:00哦
        dateTime.toDateTime();

        // 获取当日起始时间/结束时间 (因为有夏令时的关系, 起始时间不一定是00:00:00)
        dateTime.withTimeAtStartOfDay();
        dateTime.millisOfDay().withMaximumValue();// 当日结束时间, 例如23:59:59

        // 时间加减 (此处以LocalDateTime为例, 其他类型也可以这么操作)
        localDateTime.minusDays(3);//减三天
        localDateTime.plusDays(3);//加三天

        // JODA -> String
        localDateTime.toString("yyyy-MM-dd HH:mm:ss");
        localDate.toString("yyyy-MM-dd");
        dateTime.toString("yyyy-MM-dd HH:mm:ss");

    }

}
