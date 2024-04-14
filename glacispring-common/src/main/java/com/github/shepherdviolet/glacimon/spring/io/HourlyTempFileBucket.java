/*
 * Copyright (C) 2022-2024 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.io;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * <p>HourlyTempFileBucket</p>
 * <p>Create temporary files in the specified directory, and delete expired files regularly.</p>
 *
 * <p>Example:</p>
 *
 * <pre>
 *     // Root path: /home/yourname/temp ; File retention hours: 4
 *     HourlyTempFileBucket hourlyTempFileBucket = new HourlyTempFileBucket("/home/yourname/temp", 3);
 *     // Get temp file. File path: /home/yourname/temp/tmp-yyyy-MM-dd-HH/yourkey.txt
 *     File tempFile = hourlyTempFileBucket.get("yourkey.txt");
 * </pre>
 *
 * <p>Temp file automatic cleaning:</p>
 *
 * <p>When fileRetentionHours <= 0, HourlyTempFileBucket will not delete temporary files.
 * If fileRetentionHours > 0, it means that the temporary file is retained for $fileRetentionHours hours.
 * For example, it's 2024-04-07 08:??:?? now, if fileRetentionHours = 2 and rootDir = "/home/yourname/temp",
 * it will retain two temporary directories /home/yourname/temp/tmp-2024-04-07-08 and /home/yourname/temp/tmp-2024-04-07-07,
 * which directories are earlier than 2024-04-07-07 will be deleted (including internal files). </p>
 *
 * <p>Note that the expired file deletion program will only be triggered when the HourlyTempFileBucket#get method is called.
 * In other words, if you do not call the HourlyTempFileBucket#get method for a long time, expired temporary files will always
 * exist in filesystem.</p>
 *
 */
public class HourlyTempFileBucket extends TempFileBucket{

    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH").withZone(ZoneId.systemDefault());

    /**
     * @param rootDir Root directory
     * @param fileRetentionHours Date directories older than the specified number of hours will be deleted. Expired files
     *                          will not be deleted when set to 0.
     */
    public HourlyTempFileBucket(String rootDir, int fileRetentionHours) {
        super(rootDir, (long) fileRetentionHours * ONE_HOUR_MILLIS, DEFAULT_DATE_DIR_PREFIX);
    }

    /**
     * @param rootDir Root directory
     * @param fileRetentionHours Date directories older than the specified number of hours will be deleted. Expired files
     *                          will not be deleted when set to 0.
     * @param dateDirPrefix Date directory name prefix (in root directory)
     */
    public HourlyTempFileBucket(String rootDir, int fileRetentionHours, String dateDirPrefix) {
        super(rootDir, (long) fileRetentionHours * ONE_HOUR_MILLIS, dateDirPrefix);
    }

    @Override
    protected long parseDateStringToMilli(String dateString) throws DateTimeParseException {
        return LocalDateTime.from(dateFormatter.parse(dateString)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Override
    protected String formatMilliToDateString(long currentDateLong) {
        return dateFormatter.format(Instant.ofEpochMilli(currentDateLong));
    }

    @Override
    protected long getCurrentDateLong() {
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), 0, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

}
