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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.monitor.txtimer;

import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;
import sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerReportRepository;
import sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerReportScanner;
import sviolet.thistle.util.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class TxTimerReportReaderTest {

    private static final String DATA1 = "" +
            "2019-09-16 16:09:09,933 INFO Slate-TxTimer-Report-1 s.s.c.x.monitor.txtimer.def.Reporter flush 283: Page 1\n" +
            "   Ver Rand StartTime Duration Group Name RunCnt     TotAvg TotCnt     CurrMin CurrMax CurrAvg CurrCnt (TimeUnit:ms)\n" +
            "TxT|1|58r7a54o|20190916 16:04:00|300000|TestGroup|0001|0||1466|37||1188|1342|1265|2|\n" +
            "TxT|1|58r7a54o|20190916 16:04:00|300000|TestGroup|0002|0||1014|45||944|1424|1062|8|\n" +
            "TxT|1|58r7a54o|20190916 16:04:00|300000|TestGroup|0003|0||2549|2||0|0|0|0|\n" +
            "TxT|1|58r7a54o|20190916 16:04:00|300000|TestGroup|0004|0||1466|37||0|0|0|0|\n" +
            "2019-09-16 16:09:09,933 INFO Slate-TxTimer-Report-1 s.s.c.x.monitor.txtimer.def.Reporter flush 283: Page 1\n" +
            "[TxT] Ver From To Group Name RunCnt [Blank] TotAvg(ms) TotCnt [Blank] CurrMin(ms) CurrMax(ms) CurrAvg(ms) CurrCnt\n" +
            "TxT|1|58r7a54o|20190916 16:09:00|300000|TestGroup|0001|0||1466|37||1088|1242|1165|10|";

    private static final String DATA2 = "" +
            "2019-09-16 16:09:09,833 INFO Slate-TxTimer-Report-1 s.s.c.x.monitor.txtimer.def.Reporter flush 283: Page 1\n" +
            "   Ver Rand StartTime Duration Group Name RunCnt     TotAvg TotCnt     CurrMin CurrMax CurrAvg CurrCnt (TimeUnit:ms)\n" +
            "TxT|1|DriYUYUu|20190917 14:57:00|60000|HttpTransport|Service1|200||95|242996||4|1313|95|97770|\n" +
            "TxT|1|DriYUYUu|20190917 14:57:00|60000|HttpTransport|Service2|200||94|246025||3|1308|94|98903|\n" +
            "TxT|1|DriYUYUu|20190917 14:57:00|60000|HttpTransport|Service3|200||92|249177||2|1311|92|100119|\n" +
            "TxT|1|DriYUYUu|20190917 14:57:00|60000|HttpTransport|Service4|200||91|251712||1|1295|91|101202|\n" +
            "TxT|1|DriYUYUu|20190917 14:57:00|60000|HttpTransport|Service5|200||90|255383||0|1307|90|102742|\n" +
            "2019-09-16 16:09:09,933 INFO Slate-TxTimer-Report-1 s.s.c.x.monitor.txtimer.def.Reporter flush 283: Page 1\n" +
            "   Ver Rand StartTime Duration Group Name RunCnt     TotAvg TotCnt     CurrMin CurrMax CurrAvg CurrCnt (TimeUnit:ms)\n" +
            "TxT|1|DriYUYUu|20190917 14:58:00|60000|HttpTransport|Service1|200||95|242996||4|904|99|120964|\n" +
            "TxT|1|DriYUYUu|20190917 14:58:00|60000|HttpTransport|Service2|200||94|246025||3|910|97|122697|\n" +
            "TxT|1|DriYUYUu|20190917 14:58:00|60000|HttpTransport|Service3|200||92|249177||2|904|96|124243|\n" +
            "TxT|1|DriYUYUu|20190917 14:58:00|60000|HttpTransport|Service4|200||91|251712||1|899|95|125431|\n" +
            "TxT|1|DriYUYUu|20190917 14:58:00|60000|HttpTransport|Service5|200||90|255383||0|900|94|127251|";

    private static final String EXPECTED = "{HttpTransport={Service4={1568703420000={DriYUYUu=DefaultData{group='HttpTransport', name='Service4', time=1568703420000, count=101202.0, avgElapse=91, maxElapse=1295, minElapse=1}}, 1568703480000={DriYUYUu=DefaultData{group='HttpTransport', name='Service4', time=1568703480000, count=125431.0, avgElapse=95, maxElapse=899, minElapse=1}}}, Service3={1568703420000={DriYUYUu=DefaultData{group='HttpTransport', name='Service3', time=1568703420000, count=100119.0, avgElapse=92, maxElapse=1311, minElapse=2}}, 1568703480000={DriYUYUu=DefaultData{group='HttpTransport', name='Service3', time=1568703480000, count=124243.0, avgElapse=96, maxElapse=904, minElapse=2}}}, Service2={1568703420000={DriYUYUu=DefaultData{group='HttpTransport', name='Service2', time=1568703420000, count=98903.0, avgElapse=94, maxElapse=1308, minElapse=3}}, 1568703480000={DriYUYUu=DefaultData{group='HttpTransport', name='Service2', time=1568703480000, count=122697.0, avgElapse=97, maxElapse=910, minElapse=3}}}, Service1={1568703420000={DriYUYUu=DefaultData{group='HttpTransport', name='Service1', time=1568703420000, count=97770.0, avgElapse=95, maxElapse=1313, minElapse=4}}, 1568703480000={DriYUYUu=DefaultData{group='HttpTransport', name='Service1', time=1568703480000, count=120964.0, avgElapse=99, maxElapse=904, minElapse=4}}}, Service5={1568703420000={DriYUYUu=DefaultData{group='HttpTransport', name='Service5', time=1568703420000, count=102742.0, avgElapse=90, maxElapse=1307, minElapse=0}}, 1568703480000={DriYUYUu=DefaultData{group='HttpTransport', name='Service5', time=1568703480000, count=127251.0, avgElapse=94, maxElapse=900, minElapse=0}}}}, TestGroup={0002={1568621040000={58r7a54o=DefaultData{group='TestGroup', name='0002', time=1568621040000, count=1.6, avgElapse=1062, maxElapse=1424, minElapse=944}}, 1568621100000={58r7a54o=DefaultData{group='TestGroup', name='0002', time=1568621100000, count=1.6, avgElapse=1062, maxElapse=1424, minElapse=944}}, 1568621160000={58r7a54o=DefaultData{group='TestGroup', name='0002', time=1568621160000, count=1.6, avgElapse=1062, maxElapse=1424, minElapse=944}}, 1568621220000={58r7a54o=DefaultData{group='TestGroup', name='0002', time=1568621220000, count=1.6, avgElapse=1062, maxElapse=1424, minElapse=944}}, 1568621280000={58r7a54o=DefaultData{group='TestGroup', name='0002', time=1568621280000, count=1.6, avgElapse=1062, maxElapse=1424, minElapse=944}}}, 0001={1568621040000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621040000, count=0.4, avgElapse=1265, maxElapse=1342, minElapse=1188}}, 1568621100000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621100000, count=0.4, avgElapse=1265, maxElapse=1342, minElapse=1188}}, 1568621160000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621160000, count=0.4, avgElapse=1265, maxElapse=1342, minElapse=1188}}, 1568621220000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621220000, count=0.4, avgElapse=1265, maxElapse=1342, minElapse=1188}}, 1568621280000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621280000, count=0.4, avgElapse=1265, maxElapse=1342, minElapse=1188}}, 1568621340000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621340000, count=2.0, avgElapse=1165, maxElapse=1242, minElapse=1088}}, 1568621400000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621400000, count=2.0, avgElapse=1165, maxElapse=1242, minElapse=1088}}, 1568621460000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621460000, count=2.0, avgElapse=1165, maxElapse=1242, minElapse=1088}}, 1568621520000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621520000, count=2.0, avgElapse=1165, maxElapse=1242, minElapse=1088}}, 1568621580000={58r7a54o=DefaultData{group='TestGroup', name='0001', time=1568621580000, count=2.0, avgElapse=1165, maxElapse=1242, minElapse=1088}}}}}";

    @Test
    public void testReportReader() throws IOException {

        FileUtils.writeString(new File("./out/test-case/txtimer/data1.stat"), DATA1, false);
        FileUtils.writeString(new File("./out/test-case/txtimer/unexpected.txt"), "unexpected data", false);
        FileUtils.writeString(new File("./out/test-case/txtimer/dir2/data2.stat"), DATA2, false);

        Map<String, Map<String, TreeMap<Long, Map<String, DefaultTxTimerReportRepository.DefaultData>>>> data = new DefaultTxTimerReportScanner()
                .read("./out/test-case/txtimer/", ".*\\.stat$", true, StandardCharsets.UTF_8)
                .getResultData();

//        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(data));

        Assert.assertEquals(
                EXPECTED,
                data.toString());

    }

}
