/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.slate.common.x.conversion.mapxbean;

import com.github.shepherdviolet.glaciion.api.annotation.PropertyInject;
import com.github.shepherdviolet.glaciion.api.interfaces.InitializableImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.conversion.mapxbean.mapper.date.*;
import sviolet.slate.common.x.conversion.mapxbean.mapper.num.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>[Common Handler] MxbTypeMapper provider. </p>
 *
 * <p>Purpose: Provide type mappers to MxbTypeMapperCenter. Globally. </p>
 *
 * @author S.Violet
 * @see MapXBean
 */
public class MxbTypeMapperProviderImpl implements MxbTypeMapperProvider, InitializableImplementation {

    private final Logger logger = LoggerFactory.getLogger(MapXBean.class);

    private List<MxbTypeMapper> typeMappers = new ArrayList<>(32);

    /**
     * Property inject: Date format, Nullable, e.g. yyyy-MM-dd HH:mm:ss.SSS
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.date-format")
    private String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * Property inject: Time zone, Nullable, e.g. GMT+08:00
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.time-zone")
    private String timeZone;

    /**
     * Property inject: Is log enabled
     */
    @PropertyInject(getVmOptionFirst = "slate.mapxbean.log")
    private boolean logEnabled = false;

    /**
     * Add type mappers after service created
     */
    @Override
    public void onServiceCreated() {
        //Date
        typeMappers.add(new MxbMapperAllDate2SqlDate());
        typeMappers.add(new MxbMapperAllDate2SqlTimestamp());
        typeMappers.add(new MxbMapperAllDate2String(dateFormat, timeZone));
        typeMappers.add(new MxbMapperAllDate2UtilDate());
        typeMappers.add(new MxbMapperString2SqlDate(timeZone));
        typeMappers.add(new MxbMapperString2SqlTimestamp(timeZone));
        typeMappers.add(new MxbMapperString2UtilDate(timeZone));
        //Number
        typeMappers.add(new MxbMapperAllInteger2BigInteger());
        typeMappers.add(new MxbMapperAllNumber2BigDecimal());
        typeMappers.add(new MxbMapperAllNumber2String());
        typeMappers.add(new MxbMapperLowlevelNum2Double());
        typeMappers.add(new MxbMapperLowlevelNum2Float());
        typeMappers.add(new MxbMapperLowlevelNum2Integer());
        typeMappers.add(new MxbMapperLowlevelNum2Long());
    }

    /**
     * The type mappers returned here will be added to MxbTypeMapperCenter.
     * High priority mapper takes effect when the acceptance type is the same.
     */
    @Override
    public List<MxbTypeMapper> getTypeMappers() {
        return typeMappers;
    }

}
