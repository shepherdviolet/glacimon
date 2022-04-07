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

package sviolet.slate.common.x.conversion.mapxbean.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.conversion.mapxbean.MapXBean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Simple exception collector, print ignored exception by slf4j.</p>
 *
 * <p>Only valid when throwExceptionIfFails = false (that is, "error skip" mode).
 * Used to collect exceptions that were ignored during the conversion process (can be used to print logs and
 * troubleshooting).</p>
 *
 * <p>1.Print ignored exceptions after conversion by slf4j, you can override "onLog" method</p>
 *
 * @author S.Violet
 * @see MapXBean
 */
public class SimpleConversionExceptionCollector extends BaseConversionExceptionCollector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, List<Throwable>> exceptionMap = new ConcurrentHashMap<>(128);

    @Override
    protected final void onStart(Integer id, Object from, Class<?> toType) {
        //do nothing
    }

    @Override
    protected final void onException(Integer id, Throwable t) {
        //put exception into map
        exceptionMap.computeIfAbsent(id, k -> new LinkedList<>()).add(t);
    }

    @Override
    protected final void onFinish(Integer id, Object from, Class<?> toType) {
        //print log
        try {
            onLog(id, from, toType, exceptionMap.remove(id));
        } catch (Throwable t) {
            logger.warn("Error when print log, error message: " + t.getMessage(), t);
        }
    }

    /**
     * Print log after conversion
     * @param id id
     * @param from nullable
     * @param toType nullable
     * @param exceptions null if no exception
     */
    protected void onLog(Integer id, Object from, Class<?> toType, List<Throwable> exceptions){
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (exceptions == null || exceptions.size() <= 0) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MapXBean | Some properties mapping failed when '")
                .append(from != null ? from.getClass() : null)
                .append("' was converted to '")
                .append(toType)
                .append("', exceptions: ");

        for (Throwable exception : exceptions) {
            stringBuilder.append("[");
            stringBuilder.append(exception.getMessage());
            Throwable cause = exception.getCause();
            while (cause != null && cause != exception) {
                exception = cause;
                stringBuilder.append(", cause by ");
                stringBuilder.append(exception.getMessage());
                cause = exception.getCause();
            }
            stringBuilder.append("]");
        }

        stringBuilder.append(", data: [")
                .append(from)
                .append("]");

        logger.warn(stringBuilder.toString());
    }

}
