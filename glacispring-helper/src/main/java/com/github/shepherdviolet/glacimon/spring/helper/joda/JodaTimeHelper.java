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
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalInstantException;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * <p>JodaTime助手, 依赖: joda-time:joda-time.</p>
 * <p></p>
 * <p>=============================================================================================</p>
 * <p>stringToJodaDateTime: 解决了DateTime#parse方法可能会抛出IllegalInstantException异常的"问题".</p>
 * <p></p>
 * <p>问题复现:</p>
 * <p>DateTime.parse("1948-05-01", DateTimeFormat.forPattern("yyyy-MM-dd"))</p>
 * <p>当默认时区为'Asia/Shanghai'时, 调用上述方法会抛出IllegalInstantException异常:</p>
 * <p>Cannot parse "1948-05-01": Illegal instant due to time zone offset transition (Asia/Shanghai)</p>
 * <p></p>
 * <p>触发原因:</p>
 * <p>在Asia/Shanghai时区中, 1948-05-01这一天进入夏令时, 这一天从01:00:00开始, 也就是说, 00:00:00-00:59:59这段
 * 时间是不存在的, Joda在解析字符串'1948-05-01'或'1948-05-01 00:??:??'时, 发现这个时间无法映射, 就报错了. (经试验,
 * jdk8的time包不会出现这个"问题", 它会自动解析为'1948-05-01 01:00:00')</p>
 * <p></p>
 * <p>解决办法1 (推荐) -------------------------------------------</p>
 * <p></p>
 * <p>使用本助手类解析String日期: <br>
 * DateTime dateTime = JodaTimeHelper.stringToJodaDateTime("1948-05-01", "yyyy-MM-dd");</p>
 * <p></p>
 * <p>解决办法2 (推荐,官方) ---------------------------------------</p>
 * <p></p>
 * <p>如果不需要时区的概念, 使用LocalDateTime/LocalDate来解析:</p>
 * <p>// LocalDateTime不存在时区的概念, 所以任何时间都能解析成功</p>
 * <p>LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormat.forPattern(pattern));</p>
 * <p>// 如果只有年月日, 解析成LocalDate也是一个选择</p>
 * <p>LocalDate date = LocalDate.parse(dateTimeStr, DateTimeFormat.forPattern(pattern));</p>
 * <p>// 如果LocalDate又想转为DateTime使用, 可以获取当天的起始时间, 例如'1948-05-01'在时区Asia/Shanghai中的起始时间为'1948-05-01 01:00:00'</p>
 * <p>LocalDate date = LocalDate.parse(dateTimeStr, DateTimeFormat.forPattern(pattern));</p>
 * <p></p>
 * <p>解决办法3 (不推荐) -----------------------------------------</p>
 * <p></p>
 * <p>有个临时解决办法, 把默认时区设置为固定时区, 例如: GMT+8:</p>
 * <pre>
 *     static {
 *         if ("Asia/Shanghai".equals(TimeZone.getDefault().getID())) {
 *             TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
 *         }
 *     }
 * </pre>>
 * <p>或者每次解析时指定为固定时区:</p>
 * <p>DateTime.parse("1948-05-01", DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.forOffsetHours(8)))</p>
 * <p></p>
 * <p>=============================================================================================</p>
 *
 * @author shepherdviolet
 */
public class JodaTimeHelper {

    /**
     * String转Joda的DateTime, 解决了一个"问题".
     * 建议用本方法代替: DateTime dateTime = DateTime.parse(dateTimeStr, DateTimeFormat.forPattern(pattern)).
     * 详细说明见本类的JavaDoc.
     *
     * @param dateTimeStr 日期的String
     * @param pattern 日期格式
     * @return DateTime
     */
    public static DateTime stringToJodaDateTime(String dateTimeStr, String pattern) {
        return stringToJodaDateTime(dateTimeStr, pattern, DateTimeZone.getDefault());
    }

    /**
     * String转Joda的DateTime, 解决了一个"问题".
     * 建议用本方法代替: DateTime dateTime = DateTime.parse(dateTimeStr, DateTimeFormat.forPattern(pattern)).
     * 详细说明见本类的JavaDoc.
     *
     * @param dateTimeStr 日期的String
     * @param pattern 日期格式
     * @param dateTimeZone 时区
     * @return DateTime
     */
    public static DateTime stringToJodaDateTime(String dateTimeStr, String pattern, DateTimeZone dateTimeZone) {
        try {
            return DateTime.parse(dateTimeStr, DateTimeFormat.forPattern(pattern).withZone(dateTimeZone));
        } catch (RuntimeException ex) {
            if (IllegalInstantException.isIllegalInstant(ex)) {
                return LocalDate.parse(dateTimeStr, DateTimeFormat.forPattern(pattern)).toDateTimeAtStartOfDay(dateTimeZone);
            }
            throw ex;
        }
    }

}
