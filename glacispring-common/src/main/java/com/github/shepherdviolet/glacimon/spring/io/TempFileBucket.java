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

import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutorService;

/**
 * <p>TempFileBucket</p>
 * <p>Create temporary files in the specified directory, and delete expired files regularly.</p>
 *
 * <p>Example:</p>
 *
 * <pre>
 *     // Root path: /home/yourname/temp ; File retention days: 3
 *     TempFileBucket tempFileBucket = new TempFileBucket("/home/yourname/temp", 3);
 *     // Get temp file. File path: /home/yourname/temp/tmp-yyyy-MM-dd/yourkey.txt
 *     File tempFile = tempFileBucket.get("yourkey.txt");
 * </pre>
 *
 * <p>Temp file automatic cleaning:</p>
 *
 * <p>When fileRetentionDays <= 0, TempFileBucket will not delete temporary files.
 * If fileRetentionDays > 0, it means that the temporary file is retained for $fileRetentionDays days.
 * For example, today is 2024-04-07, if fileRetentionDays = 2 and rootDir = "/home/yourname/temp",
 * it will retain two temporary directories /home/yourname/temp/tmp-2024-04-07 and /home/yourname/temp/tmp-2024-04-06,
 * which directories are earlier than 2024-04-06 will be deleted (including internal files). </p>
 *
 * <p>Note that the expired file deletion program will only be triggered when the TempFileBucket#get method is called.
 * In other words, if you do not call the TempFileBucket#get method for a long time, expired temporary files will always
 * exist in filesystem.</p>
 *
 */
public class TempFileBucket {

    private static final String DEFAULT_DATE_DIR_PREFIX = "tmp-";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private final String rootDir;
    private final int fileRetentionDays;
    private final String dateDirPrefix;

    private final ExecutorService cleanerThreadPool = ThreadPoolExecutorUtils.createLazy(3, "Glacijava-TempFileBucket-%s");// 这里不能设置成daemon线程, 过期文件清理线程要尽量完成
    private volatile long cleanedDate = 0L;

    /**
     * @param rootDir Root directory
     * @param fileRetentionDays Date directories older than the specified number of days will be deleted. Expired files
     *                          will not be deleted when set to 0.
     */
    public TempFileBucket(String rootDir, int fileRetentionDays) {
        this(rootDir, fileRetentionDays, DEFAULT_DATE_DIR_PREFIX);
    }

    /**
     * @param rootDir Root directory
     * @param fileRetentionDays Date directories older than the specified number of days will be deleted. Expired files
     *                          will not be deleted when set to 0.
     * @param dateDirPrefix Date directory name prefix (in root directory)
     */
    public TempFileBucket(String rootDir, int fileRetentionDays, String dateDirPrefix) {
        if (CheckUtils.isEmptyOrBlank(rootDir)) {
            throw new IllegalArgumentException("rootDir is null or empty");
        }
        if (CheckUtils.isEmptyOrBlank(dateDirPrefix)) {
            throw new IllegalArgumentException("dateDirPrefix is null or empty");
        }
        this.rootDir = rootDir;
        this.fileRetentionDays = fileRetentionDays;
        this.dateDirPrefix = dateDirPrefix;
    }

    public File get(String filename) throws IOException {
        if (CheckUtils.isEmptyOrBlank(filename)) {
            throw new IOException("Temp filename is null or empty");
        }
        long currentDateLong =  getCurrentDateLong();
        String currentDateString = formatMilliToDateString(currentDateLong);
        tryClean(currentDateLong);
        File rootDirFile = makeDirIfNotExists(new File(rootDir), "root");
        File dateDirFile = makeDirIfNotExists(new File(rootDirFile, dateDirPrefix + currentDateString), "date");
        return makeFileIfNotExists(new File(dateDirFile, filename));
    }

    private File makeDirIfNotExists(File dirFile, String dirDescription) throws IOException {
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (!dirFile.exists()) {
            throw new IOException("Can not make " + dirDescription + " directory: " + dirFile.getAbsolutePath());
        }
        if (!dirFile.isDirectory()) {
            throw new IOException("The " + dirDescription + " directory '" + dirFile.getAbsolutePath() + "' is not a directory");
        }
        if (!dirFile.canWrite()) {
            throw new IOException("The " + dirDescription + " directory '" + dirFile.getAbsolutePath() + "' is not writable");
        }
        return dirFile;
    }

    private File makeFileIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        if (!file.exists()) {
            throw new IOException("Can not create temp file: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("The temp file '" + file.getAbsolutePath() + "' is not a file");
        }
        if (!file.canWrite()) {
            throw new IOException("The temp file '" + file.getAbsolutePath() + "' is not writable");
        }
        return file;
    }

    private long parseDateStringToMilli(String dateString) throws DateTimeParseException {
        return LocalDate.from(dateFormatter.parse(dateString)).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String formatMilliToDateString(long currentDateLong) {
        return dateFormatter.format(Instant.ofEpochMilli(currentDateLong));
    }

    private long getCurrentDateLong() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private void tryClean(long currentDateLong) {
        if (fileRetentionDays <= 0) {
            return;
        }
        if (cleanedDate != currentDateLong) {
            cleanerThreadPool.execute(this::deleteExpiredFiles);
        }
    }

    private void deleteExpiredFiles() {
        if (fileRetentionDays <= 0) {
            return;
        }

        long currentDateLong = getCurrentDateLong();
        if (cleanedDate == currentDateLong) {
            return;
        }

        try {
            File rootDirFile = makeDirIfNotExists(new File(rootDir), "root");
            File[] dateDirFiles = rootDirFile.listFiles(file -> file.isDirectory() && file.getName().startsWith(dateDirPrefix));
            if (dateDirFiles != null) {
                for (File dateDirFile : dateDirFiles) {
                    String dateString = dateDirFile.getName().substring(dateDirPrefix.length());
                    long dateLong;
                    try {
                        dateLong = parseDateStringToMilli(dateString);
                    } catch (DateTimeParseException e) {
                        logger.warn("TempFileBucket | Skip a directory that does not comply with date directory rules: " + dateDirFile.getAbsolutePath(), e);
                        continue;
                    }
                    if (dateLong <= currentDateLong - fileRetentionDays * ONE_DAY_MILLIS) {
                        deleteDirectory(dateDirFile);
                        logger.info("TempFileBucket | Expired date directory deleted (including temp files inside): " + dateDirFile.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("TempFileBucket | Error while deleting expired files", t);
        }

        cleanedDate = currentDateLong;
    }

    private void deleteDirectory(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        if (!file.delete()) {
            logger.error("TempFileBucket | Failed to delete expired file: " + file.getAbsolutePath());
        }
    }

}
