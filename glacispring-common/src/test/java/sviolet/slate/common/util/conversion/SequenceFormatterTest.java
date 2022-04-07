/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.slate.common.util.conversion;

import org.junit.Assert;
import org.junit.Test;
import sviolet.slate.common.util.common.LambdaBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SequenceFormatterTest {

    @Test
    public void format() throws SequenceFormatter.SequenceFormatException {

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Map<String, Object> dataMap = LambdaBuilder.hashMap(m -> {
            m.put("sequence", 10000111);
        });

        Object data = 10000111;

        Assert.assertEquals(date + "9910000111",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{}", data));
        Assert.assertEquals(date + "99000113",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{<+>2<pad-to-len>6}", data));
        Assert.assertEquals(date + "9910000113",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{<++><++>}", data));

        Assert.assertEquals(date + "9910000111",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{sequence}", dataMap));
        Assert.assertEquals(date + "99000113",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{sequence<+>2<pad-to-len>6}", dataMap));
        Assert.assertEquals(date + "990010000113",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{sequence<+>2<pad-to-len>10}", dataMap));
        Assert.assertEquals("113",
                SequenceFormatter.defaultFormatter().format("{sequence<+>2<trim-to-len>6}", dataMap));
        Assert.assertEquals("10000113",
                SequenceFormatter.defaultFormatter().format("{sequence<+>2<trim-to-len>10}", dataMap));
        Assert.assertEquals(date + "9910000113",
                SequenceFormatter.defaultFormatter().format("{_date_}{module:99}{sequence<++><++>}", dataMap));

    }

}
