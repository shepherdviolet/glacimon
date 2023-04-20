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

package com.github.shepherdviolet.glacimon.java.misc;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class Jdk8TimeDemo {

    /**
     * ## `Local*`和`Zoned*`的区别
     * <br><br>
     * 它们内部储存的都是指定时区的"年月日时分秒", 而不是时间戳(格林威治时间1970年01月01日00时00分00秒到现在的毫秒数). `Zoned*`在`Local*`的基础上增加了时区的属性,
     * 所以当我们试图把它们转回时间戳(toEpochMilli)的时候, `Zoned*`因为知道自己的时区, 所以可以直接转, 而`Local*`对象并不知道自己在什么时区, 所以需要额外指定时区,
     * 而且必须是它创建的时候的时区(不能随便送一个).
     * <br><br>
     * ## `Instant`是啥
     * <br><br>
     * Instant内部储存的是时间戳(格林威治时间1970年01月01日00时00分00秒到现在的毫秒数), 但是不是毫秒数, 是秒数和纳秒数.
     * <br><br>
     */
    public static void main(String[] args) {

        // 关于时区

        //系统默认时区
        ZoneId zoneId1 = ZoneId.systemDefault();
        //东八区 (固定时区无夏令时)
        ZoneId zoneId2 = ZoneId.of("UTC+8");
        //东八区 (有夏令时)
        ZoneId zoneId3 = ZoneId.of("Asia/Shanghai");
        //东八区 (固定时区无夏令时), ZoneOffset是ZoneId的子类
        ZoneId zoneId4 = ZoneOffset.ofHours(8);

        // 获取当前时间

        //Instant代表时间戳
        Instant instant1 = Instant.now();
        //*DateTime为日期时间, 系统默认时区
        LocalDateTime localDateTime1 = LocalDateTime.now();
        ZonedDateTime zonedDateTime1 = ZonedDateTime.now();
        //*DateTime为日期时间, 指定时区
        ZonedDateTime zonedDateTime2 = ZonedDateTime.now(zoneId1);

        //Instant本身就代表时间戳, toEpochMilli是获取从格林威治时间1970年01月01日00时00分00秒到现在的毫秒数
        long epochMilli1 = Instant.now().toEpochMilli();

        // 时间戳 -> *DateTime

        //*DateTime代表年月日时分秒, *Date代表年月日, *Time代表时分秒
        LocalDateTime localDateTime3 = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli1), zoneId1);
        //这个还额外包含了时区信息, 在转回时间戳的时候就不需要再指定时区了
        ZonedDateTime zonedDateTime3 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli1), zoneId1);

        // *DateTime -> 时间戳

        //Local*不知道自己是什么时区的, 所以转时间戳的时候要指定时区, 另外, 这里zoneOffset必须是ZoneOffset(它是ZoneId的子类)
        long epochMilli2 = localDateTime1.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        //也可以先用zoneId转成Zoned*, 如果你不想用ZoneOffset的话
        long epochMilli3 = localDateTime1.atZone(zoneId1).toInstant().toEpochMilli();
        //Zoned*知道自己的时区, 所以不需要指定时区了
        long epochMilli4 = zonedDateTime1.toInstant().toEpochMilli();

        // String -> *DateTime

        //这个线程安全可以单例, 要指定时区
        DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withZone(zoneId1);
        //解析成各种对象, 会抛出DateTimeParseException异常
        Instant instant4 = Instant.from(dateTimeFormatter1.parse("20200126 14:37:12"));
        LocalDateTime localDateTime4 = LocalDateTime.from(dateTimeFormatter1.parse("20200126 14:37:12"));
        ZonedDateTime zonedDateTime4 = ZonedDateTime.from(dateTimeFormatter1.parse("20200126 14:37:12"));

        // *DateTime -> String

        //这个线程安全可以单例, 要指定时区
        DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withZone(ZoneId.systemDefault());
        //格式化成String
        String string1 = dateTimeFormatter2.format(instant1);
        String string2 = dateTimeFormatter2.format(localDateTime1);
        String string3 = dateTimeFormatter2.format(zonedDateTime1);

        // `Local*` -> `Zoned*`

        ZonedDateTime zonedDateTime5 = localDateTime1.atZone(zoneId1);

        // 日期加减

        LocalDateTime localDateTime6 = localDateTime1.plusDays(3);// 加三天
        ZonedDateTime zonedDateTime6 = zonedDateTime1.minusDays(3);// 减三天

    }

}

