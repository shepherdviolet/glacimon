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
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class MxbMapperString2UtilDate implements MxbTypeMapper {

    private static final Class[] FROM = new Class[]{
            String.class
    };

    private static final Class[] TO = new Class[]{
            java.util.Date.class,
    };

    private TimeZone timeZone;
    private Map<Integer, List<PatternAndFormat>> patternAndFormatMap = new HashMap<>();

    /**
     * @param timeZone Time zone, Nullable, e.g. GMT+08:00
     */
    public MxbMapperString2UtilDate(String timeZone) {
        if (!CheckUtils.isEmptyOrBlank(timeZone)) {
            this.timeZone = TimeZone.getTimeZone(timeZone);
        }
        createFormat("yyyy-MM-dd HH:mm:ss.SSS", "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$");
        createFormat("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
        createFormat("yyyy-MM-dd", "^\\d{4}-\\d{2}-\\d{2}$");
        createFormat("yyyyMMddHHmmss", "^\\d{14}$");
        createFormat("yyyyMMdd", "^\\d{8}$");
        createFormat("yyyy-MM-dd HH:mm:ss,SSS", "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}$");
    }

    protected void createFormat(String dateFormat, String pattern){
        List<PatternAndFormat> list = patternAndFormatMap.computeIfAbsent(dateFormat.length(), k -> new ArrayList<>(2));
        list.add(new PatternAndFormat(pattern, dateFormat, timeZone));
    }

    @Override
    public Object map(Object from, Class<?> toType, Type toGenericType, Cause cause) throws Exception {
        String fromStr = (String) from;
        int fromLength = fromStr.length();
        if (fromLength <= 0) {
            //Treat as null
            return null;
        }
        List<PatternAndFormat> list = patternAndFormatMap.get(fromLength);
        if (list == null) {
            throw new Exception("Cannot find date formatter for '" + fromStr + "'");
        }
        for (PatternAndFormat patternAndFormat : list) {
            if (patternAndFormat.check(fromStr)) {
                try {
                    return patternAndFormat.parse(fromStr);
                } catch (Exception e) {
                    throw new Exception("Parse date string '" + fromStr + "' to 'Date' object by format " + patternAndFormat.dateFormat + " failed", e);
                }
            }
        }
        throw new Exception("Cannot find date formatter for '" + fromStr + "'");
    }

    @Override
    public Class<?>[] fromType() {
        return FROM;
    }

    @Override
    public Class<?>[] toType() {
        return TO;
    }

    private static class PatternAndFormat {
        private Pattern pattern;
        private String dateFormat;
        private TimeZone timeZone;

        private PatternAndFormat(String pattern, String dateFormat, TimeZone timeZone) {
            this.pattern = Pattern.compile(pattern);
            this.dateFormat = dateFormat;
            this.timeZone = timeZone;
        }

        private boolean check(String value) {
            return pattern.matcher(value).matches();
        }

        private java.util.Date parse(String value) throws ParseException {
            return DateTimeUtils.stringToDate(value, dateFormat, null, timeZone);
        }
    }

    @Override
    public int priority() {
        //Lowest priority
        return Integer.MAX_VALUE;
    }

}
