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

package com.github.shepherdviolet.glacimon.java.x.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * 临时使用的测试案例
 *
 * @author shepherdviolet
 */
public class TraceTest {

    @Test
    public void test() throws InvalidBatonException {
        System.setProperty("glacijava.trace.trace-id-key", "_traceId");
        Trace.start();
        Trace.setData("hello", "hello");
        TraceBaton baton = Trace.getBaton();
        String batonStr = baton.toString();
//        System.out.println(batonStr);
        Trace.handoff(TraceBaton.fromString(batonStr));
        Assertions.assertEquals("hello", Trace.getData("hello"));
        Assertions.assertEquals(Trace.getTraceId(), MDC.get("_traceId"));
    }

}
