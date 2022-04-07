/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.util.file;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FileUtilsTest {

    private static final String DATA = "0123456789\n\r" +
            "0123456\n" +
            "01234567890123456\r" +
            "\n\r" +
            "01234567890123456789\n" +
            "0123456789012345678901234567890123456789\n" +
            "\n" +
            "01234567890123456789012345678901234567890123456\r" +
            "0123";

    /**
     * 读取文件每行数据的测试
     */
    @Test
    public void readLinesTest() throws IOException {
        File file = new File("./out/test-case/file-utils-read-lines.txt");
        FileUtils.writeString(file, DATA, false);

        final ArrayList<String> result = new ArrayList<>();
        FileUtils.readLines(file, 40, 10, 20, new FileUtils.LineConsumer() {
            @Override
            public boolean consume(byte[] line, boolean outOfLimit) {
                result.add(new String(line, StandardCharsets.UTF_8) + (outOfLimit ? "+" : ""));
                return true;
            }
        });

        Assert.assertEquals(
                "[0123456789, 0123456, 01234567890123456, 01234567890123456789, 0123456789012345678901234567890123456789, 0123456789012345678901234567890123456789+, 0123]",
                result.toString());

    }

}
