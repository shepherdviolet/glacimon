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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.conversion.mapxbean.mapper.date;

import sviolet.slate.common.x.conversion.mapxbean.MxbTypeMapper;
import sviolet.thistle.util.conversion.DateTimeUtils;
import sviolet.thistle.util.judge.CheckUtils;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.TimeZone;

public class MxbMapperAllDate2String implements MxbTypeMapper {

    private static final Class[] FROM = new Class[]{
            java.util.Date.class,
            java.sql.Date.class,
            java.sql.Timestamp.class
    };

    private static final Class[] TO = new Class[]{
            String.class,
    };

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private String dateFormat = DEFAULT_DATE_FORMAT;
    private TimeZone timeZone;

    public MxbMapperAllDate2String(String dateFormat, String timeZone) {
        if (!CheckUtils.isEmptyOrBlank(dateFormat)) {
            this.dateFormat = dateFormat;
        }
        if (!CheckUtils.isEmptyOrBlank(timeZone)) {
            this.timeZone = TimeZone.getTimeZone(timeZone);
        }

        // format check
        DateTimeUtils.dateToDateTimeString(new Date(), dateFormat, null, this.timeZone);
    }

    @Override
    public Object map(Object from, Class<?> toType, Type toGenericType, Cause cause) {
        return DateTimeUtils.dateToDateTimeString((java.util.Date)from, dateFormat, null, timeZone);
    }

    @Override
    public Class<?>[] fromType() {
        return FROM;
    }

    @Override
    public Class<?>[] toType() {
        return TO;
    }

    @Override
    public int priority() {
        //Lowest priority
        return Integer.MAX_VALUE;
    }

}
