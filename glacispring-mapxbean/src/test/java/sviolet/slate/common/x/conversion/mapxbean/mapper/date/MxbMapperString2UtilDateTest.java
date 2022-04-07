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

package sviolet.slate.common.x.conversion.mapxbean.mapper.date;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MxbMapperString2UtilDateTest {

    private MxbMapperString2UtilDate mapper = new MxbMapperString2UtilDate(null);

    @Test
    public void testSucceed() throws Exception {
        test("2018-10-15 09:26:21.333", "2018-10-15 09:26:21.333");
        test("2018-10-15 09:26:21.000", "2018-10-15 09:26:21");
        test("2018-10-15 00:00:00.000", "2018-10-15");
        test("2018-10-15 09:26:21.333", "2018-10-15 09:26:21,333");
        test("2018-10-15 09:26:21.000", "20181015092621");
        test("2018-10-15 00:00:00.000", "20181015");
    }

    @Test(expected = Exception.class)
    public void testFailed1() throws Exception {
        test("2018-10-15 09:26:21.333", "2018-10-15 09:26:21?333");
    }

    @Test(expected = Exception.class)
    public void testFailed2() throws Exception {
        test("2018-10-15 09:26:21.333", "2018-10-15 09:26:21.33");
    }

    @Test(expected = Exception.class)
    public void testFailed3() throws Exception {
        test("2018-10-15 09:26:21.333", "2018-10-1509:26:21");
    }

    private void test(String expected, String actual) throws Exception {
        Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(expected),
                map(actual));
    }

    private Date map(String dateString) throws Exception {
        return (Date) mapper.map(dateString, Date.class, Date.class, null);
    }

}
