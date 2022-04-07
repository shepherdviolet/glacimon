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

import sviolet.slate.common.x.conversion.mapxbean.ConversionExceptionCollector;
import sviolet.slate.common.x.conversion.mapxbean.MapXBean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Base exception collector, abstract class.</p>
 *
 * <p>Only valid when throwExceptionIfFails = false (that is, "error skip" mode).
 * Used to collect exceptions that were ignored during the conversion process (can be used to print logs and
 * troubleshooting).</p>
 *
 * <p>1.Linking the three steps (onStart / onException / onFinish) with id</p>
 *
 * @author S.Violet
 * @see MapXBean
 */
public abstract class BaseConversionExceptionCollector implements ConversionExceptionCollector {

    private static final AtomicInteger ID_FACTORY = new AtomicInteger(0);

    private final ThreadLocal<Integer> idHolder = new ThreadLocal<>();

    @Override
    public final void onStart(Object from, Class<?> toType) {
        //Generate id
        Integer id = ID_FACTORY.getAndIncrement();
        //Set to ThreadLocal
        idHolder.set(id);
        //Call onStart
        onStart(id, from, toType);
    }

    @Override
    public final void onException(Throwable t) {
        //Call onException with id getting from ThreadLocal
        onException(idHolder.get(), t);
    }

    @Override
    public final void onFinish(Object from, Class<?> toType) {
        //Get id from ThreadLocal
        Integer id = idHolder.get();
        //Remove from ThreadLocal
        idHolder.remove();
        //Call onFinish
        onFinish(id, from, toType);
    }

    /**
     * Before conversion,
     * Be careful, don't throw exceptions here.
     */
    protected abstract void onStart(Integer id, Object from, Class<?> toType);

    /**
     * Catch the exceptions,
     * Be careful, don't throw exceptions here.
     */
    protected abstract void onException(Integer id, Throwable t);

    /**
     * After conversion,
     * Be careful, don't throw exceptions here.
     */
    protected abstract void onFinish(Integer id, Object from, Class<?> toType);

}
