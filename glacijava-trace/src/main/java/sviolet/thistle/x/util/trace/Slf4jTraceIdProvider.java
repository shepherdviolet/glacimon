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

package sviolet.thistle.x.util.trace;

import org.slf4j.MDC;

/**
 * MDC追踪号提供器
 *
 * @author S.Violet
 */
class Slf4jTraceIdProvider extends LocalTraceIdProvider {

    @Override
    void set(String traceId) {
        super.set(traceId);
        MDC.put(Trace.TRACE_ID_KEY, traceId);
    }

}
