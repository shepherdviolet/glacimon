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

package sviolet.thistle.util.conversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date time utils (JDK7)
 * 
 * @author S.Violet
 */
public class DateTimeUtils {

    // Current ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Current date string, yyyy-MM-dd
     */
    public static String currentDateString(){
        return currentDateTimeString("yyyy-MM-dd", null, null);
    }

    /**
     * Current time string, HH:mm:ss.SSS
     */
    public static String currentTimeString(){
        return currentDateTimeString("HH:mm:ss.SSS", null, null);
    }

    /**
     * Current date time string, yyyy-MM-dd HH:mm:ss.SSS
     */
    public static String currentDateTimeString(){
        return currentDateTimeString("yyyy-MM-dd HH:mm:ss.SSS", null, null);
    }

    /**
     * Current date time string, custom format
     *
     * @param template date string format
     */
    public static String currentDateTimeString(String template){
        return currentDateTimeString(template, null, null);
    }

    /**
     * Current date time string, custom format
     *
     * @param template date string format
     * @param locale Locale, nullable, e.g. Locale.SIMPLIFIED_CHINESE
     * @param timeZone TimeZone, nullable, e.g. TimeZone.getTimeZone("GMT+08:00")
     */
    public static String currentDateTimeString(String template, Locale locale, TimeZone timeZone){
        SimpleDateFormat formatter = new SimpleDateFormat(template, locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT));
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return formatter.format(new Date());
    }

    /**
     * Current time millis
     */
    public static long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }

    /**
     * Current nano time, can only be used to measure elapsed time and is not related to any other notion of
     * system or wall-clock time
     */
    public static long getNanoTime(){
        return System.nanoTime();
    }

    // Millis / Date to date string //////////////////////////////////////////////////////////////////////////////////

    /**
     * Millis to date string, yyyy-MM-dd
     */
    public static String millisToDateString(long timeMillis){
        return millisToDateTimeString(timeMillis, "yyyy-MM-dd", null, null);
    }

    /**
     * Millis to time string, HH:mm:ss.SSS
     */
    public static String millisToTimeString(long timeMillis){
        return millisToDateTimeString(timeMillis, "HH:mm:ss.SSS", null, null);
    }

    /**
     * Millis to date time string, yyyy-MM-dd HH:mm:ss.SSS
     */
    public static String millisToDateTimeString(long timeMillis){
        return millisToDateTimeString(timeMillis, "yyyy-MM-dd HH:mm:ss.SSS", null, null);
    }

    /**
     * Millis to date time string, custom format
     *
     * @param template date string format
     */
    public static String millisToDateTimeString(long timeMillis, String template){
        return millisToDateTimeString(timeMillis, template, null, null);
    }

    /**
     * Millis to date time string, custom format
     *
     * @param template date string format
     * @param locale Locale, nullable, e.g. Locale.SIMPLIFIED_CHINESE
     * @param timeZone TimeZone, nullable, e.g. TimeZone.getTimeZone("GMT+08:00")
     */
    public static String millisToDateTimeString(long timeMillis, String template, Locale locale, TimeZone timeZone){
        SimpleDateFormat formatter = new SimpleDateFormat(template, locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT));
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return formatter.format(new Date(timeMillis));
    }

    /**
     * Date to date string, yyyy-MM-dd
     */
    public static String dateToDateString(Date date){
        return dateToDateTimeString(date, "yyyy-MM-dd", null, null);
    }

    /**
     * Date to time string, HH:mm:ss.SSS
     */
    public static String dateToTimeString(Date date){
        return dateToDateTimeString(date, "HH:mm:ss.SSS", null, null);
    }

    /**
     * Date to date time string, yyyy-MM-dd HH:mm:ss.SSS
     */
    public static String dateToDateTimeString(Date date){
        return dateToDateTimeString(date, "yyyy-MM-dd HH:mm:ss.SSS", null, null);
    }

    /**
     * Date to date time string, custom format
     *
     * @param template date string format
     */
    public static String dateToDateTimeString(Date date, String template){
        return dateToDateTimeString(date, template, null, null);
    }

    /**
     * Date to date time string, custom format
     *
     * @param template date string format
     * @param locale Locale, nullable, e.g. Locale.SIMPLIFIED_CHINESE
     * @param timeZone TimeZone, nullable, e.g. TimeZone.getTimeZone("GMT+08:00")
     */
    public static String dateToDateTimeString(Date date, String template, Locale locale, TimeZone timeZone){
        SimpleDateFormat formatter = new SimpleDateFormat(template, locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT));
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return formatter.format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Date string to Date
     * @param dateStr Date string
     * @param pattern Date format, e.g. yyyy-MM-dd HH-mm-ss
     */
    public static Date stringToDate(String dateStr, String pattern) throws ParseException {
        return stringToDate(dateStr, pattern, null, null);
    }

    /**
     * Date string to Date
     * @param dateStr Date string
     * @param pattern Date format, e.g. yyyy-MM-dd HH-mm-ss
     * @param locale Locale, e.g. Locale.SIMPLIFIED_CHINESE
     * @param timeZone TimeZone, nullable, e.g. TimeZone.getTimeZone("GMT+08:00")
     */
    public static Date stringToDate(String dateStr, String pattern, Locale locale, TimeZone timeZone) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT));
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return formatter.parse(dateStr);
    }

    /**
     * Date string to Date, return fallback if fails
     * @param dateStr Date string
     * @param pattern Date format, e.g. yyyy-MM-dd HH-mm-ss
     * @param fallback return this if fails
     */
    public static Date stringToDate(String dateStr, String pattern, Date fallback) {
        return stringToDate(dateStr, pattern, fallback, null, null);
    }

    /**
     * Date string to Date, return fallback if fails
     * @param dateStr Date string
     * @param pattern Date format, e.g. yyyy-MM-dd HH-mm-ss
     * @param fallback return this if fails
     * @param locale Locale, e.g. Locale.SIMPLIFIED_CHINESE
     * @param timeZone TimeZone, nullable, e.g. TimeZone.getTimeZone("GMT+08:00")
     */
    public static Date stringToDate(String dateStr, String pattern, Date fallback, Locale locale, TimeZone timeZone) {
        try {
            return stringToDate(dateStr, pattern, locale, timeZone);
        } catch (ParseException e) {
            return fallback;
        }
    }

}
