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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TempFileBucketTest {

    private static final String ROOT_DIR = "./glacispring-common/build/temp-bucket-test";
    private static final String DATE_DIR_PREFIX = "tmp-";

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    public static void main(String[] args) throws IOException, ParseException {

        TempFileBucket tempFileBucket = new TempFileBucket(ROOT_DIR, 3, DATE_DIR_PREFIX);

        createTempFile(getDateString(0), "1.txt");
        createTempFile(getDateString(-1), "2.txt");
        createTempFile(getDateString(-2), "3.txt");
        createTempFile(getDateString(-3), "4.txt");
        createTempFile(getDateString(-4), "5.txt");

        File tempFile = tempFileBucket.get("new.txt");

    }

    private static String getDateString(int offset) throws ParseException {
        return dateFormatter.format(LocalDate.now().plusDays(offset).atStartOfDay(ZoneId.systemDefault()));
    }

    private static void createTempFile(String dateString, String fileName) throws IOException {
        File dateDir = new File(ROOT_DIR, DATE_DIR_PREFIX + dateString);
        dateDir.mkdirs();
        File tempFile = new File(dateDir, fileName);
        tempFile.createNewFile();
    }

}
