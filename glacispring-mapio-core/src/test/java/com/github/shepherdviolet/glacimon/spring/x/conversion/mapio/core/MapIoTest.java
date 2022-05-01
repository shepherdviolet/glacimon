/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core;

import com.github.shepherdviolet.glacimon.java.common.function.ThrowableRunnable;
import com.github.shepherdviolet.glacimon.java.misc.LambdaBuilder;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.basic.FieldScreeningMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.api.rule.*;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.DefaultExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.basic.IoMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.core.filters.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MapIoTest {

    @Test
    public void commonTest() {
        doTest();
    }

    protected MapIo getMapIo() {
        return new MapIoImpl().setExceptionFactory(new DefaultExceptionFactory() {
            @Override
            protected RuntimeException newException(String errorMsg) {
                return new IllegalStateException(errorMsg);
            }
        });
    }

    protected final void doTest() {
        MapIoImpl mapIo = (MapIoImpl) getMapIo();
        preloadTest(mapIo);
        mapTest(mapIo);
    }

    private void preloadTest(MapIoImpl mapIo) {
        mapIo.preloadDictionaries(FooDict.class, CommonDict.class, ComplexDict.class);
        System.out.println(mapIo.printCachedMappers());

        assertThrowable("MapIO | Because the field has multiple rule categories, and one rule category has multiple rule clauses, in this case, the program cannot determine the code writing order. Please set 'clauseOrder' for each rule (@Input... or @Output... series annotation) for this field, to clarify the order of rules' execution. See https://github.com/shepherdviolet/glacimon/blob/master/docs/mapio/guide.md",
                () -> mapIo.preloadDictionaries(BadComplexDict1.class));
        assertThrowable("MapIO | Because the field has multiple rule categories, and one rule category has multiple rule clauses, in this case, the program cannot determine the code writing order. Please set 'clauseOrder' for each rule (@Input... or @Output... series annotation) for this field, to clarify the order of rules' execution. See https://github.com/shepherdviolet/glacimon/blob/master/docs/mapio/guide.md",
                () -> mapIo.preloadDictionaries(BadComplexDict2.class));
        assertThrowable("MapIO | If you want to set the 'clauseOrder' of @Input... (or @Output...) series annotation for a field, you must set all of them, not only part of them!",
                () -> mapIo.preloadDictionaries(BadComplexDict3.class));
        assertThrowable("MapIO | If you want to set the 'clauseOrder' of @Input... (or @Output...) series annotation for a field, you must set all of them, not only part of them!",
                () -> mapIo.preloadDictionaries(BadComplexDict4.class));
        assertThrowable("MapIO | The 'clauseOrder = 1' is duplicated!",
                () -> mapIo.preloadDictionaries(BadComplexDict5.class));
        assertThrowable("MapIO | The 'clauseOrder = 1' is duplicated!",
                () -> mapIo.preloadDictionaries(BadComplexDict6.class));

    }

    private void mapTest(MapIoImpl mapIo) {
        Map<String, Object> map = LambdaBuilder.hashMap(m -> {
            m.put("Id", "2022042101");
            m.put("Comment", "hello");
            m.put("Req-Date", "20220421");
            m.put("--trace-id--", "a5d765df65340ce686546546835410");
            m.put("ScoreMap", LambdaBuilder.linkedHashMap(mm -> {
                mm.put("Tom", "36");
                mm.put("Arch", "100");
            }));
            m.put("Others", "Field that should not be present");
        });

        map = mapIo.doMap(map, IoMode.INPUT, FieldScreeningMode.DISCARD_BY_DEFAULT, FooDict.class, CommonDict.class);
        System.out.println(map);

        EnumKeyMapWrapper<Object> wrappedMap = EnumKeyMapWrapper.wrap(map);
        wrappedMap.put(CommonDict.RetCode, "00");
        wrappedMap.put(CommonDict.RetMsg, "success");
        wrappedMap.put(CommonDict.ResDate, "20220421");
        wrappedMap.put(FooDict.TelephoneMap, LambdaBuilder.hashMap(m -> {
            m.put("1", 123);
            m.put("2", 456);
            m.put("3", 789);
        }));

        map = mapIo.doMap(map, IoMode.OUTPUT, FieldScreeningMode.DISCARD_BY_DEFAULT, FooDict.class, CommonDict.class);
        System.out.println(map);
    }

    private void assertThrowable(String expectedErrorMsg, ThrowableRunnable process) {
        try {
            process.run();
            throw new Error("No expected throwable");
        } catch (Throwable t) {
            Assert.assertEquals(expectedErrorMsg, getCauseFromThrowable(t).getMessage());
        }
    }

    private Throwable getCauseFromThrowable(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null && cause != throwable) {
            throwable = cause;
            cause = throwable.getCause();
        }
        return throwable;
    }

    private enum CommonDict {

        @Input
        @InputFilter(type = StringCheckLength.class, args = {"0", "10"})
        @InputFilter(type = StringToInteger.class)
        Id,

        @Input(required = false)
        Comment,

        @Input(fromKey = "Req-Date")
        ReqDate,

        @Input
        @Output
        @FieldName("--trace-id--")
        TraceId,

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        @Output
        RetCode,

        @Output(required = false)
        RetMsg,

        @Output(toKey = "Res-Date")
        ResDate,

    }

    private enum FooDict {

        @Input
        @InputElementFilter(type = StringCheckLength.class, args = {"1", "8"})
        @InputElementFilter(type = StringToInteger.class)
        ScoreMap,

        @Input(fromKey = "ScoreMap")
        @InputElementFilter(type = StringToInteger.class, keepOrder = true)
        ScoreMapOrdered,

        @Input(fromKey = "ScoreMap")
        @InputFilter(type = MapValuesToList.class)
        @InputElementFilter(type = StringToInteger.class)
        ScoreList,

        @Output(toKey = "TelephoneList")
        @OutputFilter(type = MapValuesToList.class)
        @OutputElementFilter(type = ToString.class)
        TelephoneMap,

    }

    private enum SubDict {

    }

    private enum ComplexDict {

        @Input
        @InputFilter(clauseOrder = 0, type = MapValuesToList.class)
        @InputElementFilter(clauseOrder = 1, type = StringCheckLength.class, args = {"6", "8"})
        @InputElementFilter(clauseOrder = 2, type = StringToInteger.class)
        @InputFilter(clauseOrder = 3, type = DummyFilter.class)
        FieldApple,

        @Output
        @OutputFilter(clauseOrder = 0, type = MapValuesToList.class)
        @OutputElementFilter(clauseOrder = 1, type = StringCheckLength.class, args = {"6", "8"})
        @OutputElementFilter(clauseOrder = 2, type = StringToInteger.class)
        @OutputFilter(clauseOrder = 3, type = DummyFilter.class)
        FieldPie,

    }

    private enum BadComplexDict1 {

        @Input
        @InputFilter(type = MapValuesToList.class)
        @InputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
        @InputElementFilter(type = StringToInteger.class)
        @InputFilter(type = DummyFilter.class)
        FieldApple,

    }

    private enum BadComplexDict2 {

        @Output
        @OutputFilter(type = MapValuesToList.class)
        @OutputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
        @OutputElementFilter(type = StringToInteger.class)
        @OutputFilter(type = DummyFilter.class)
        FieldPie,

    }

    private enum BadComplexDict3 {

        @Input
        @InputFilter(clauseOrder = 0, type = MapValuesToList.class)
        @InputElementFilter(clauseOrder = 1, type = StringCheckLength.class, args = {"6", "8"})
        @InputElementFilter(type = StringToInteger.class)
        @InputFilter(type = DummyFilter.class)
        FieldApple,

    }

    private enum BadComplexDict4 {

        @Output
        @OutputFilter(type = MapValuesToList.class)
        @OutputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
        @OutputElementFilter(clauseOrder = 0, type = StringToInteger.class)
        @OutputFilter(clauseOrder = 1, type = DummyFilter.class)
        FieldPie,

    }

    private enum BadComplexDict5 {

        @Input
        @InputFilter(clauseOrder = 1, type = MapValuesToList.class)
        @InputElementFilter(clauseOrder = 1, type = StringCheckLength.class, args = {"6", "8"})
        @InputElementFilter(type = StringToInteger.class)
        @InputFilter(type = DummyFilter.class)
        FieldApple,

    }

    private enum BadComplexDict6 {

        @Output
        @OutputFilter(type = MapValuesToList.class)
        @OutputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
        @OutputElementFilter(clauseOrder = 1, type = StringToInteger.class)
        @OutputFilter(clauseOrder = 1, type = DummyFilter.class)
        FieldPie,

    }


}
