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

package sviolet.slate.common.x.net.loadbalance.classic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.conversion.ByteUtils;

/**
 * GSON数据转换器
 *
 * @author S.Violet
 */
public class GsonDataConverter implements DataConverter {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Gson gson;

    private String encode = "UTF-8";

    public GsonDataConverter() {
        this(DEFAULT_DATE_FORMAT);
    }

    public GsonDataConverter(String dateFormat) {
        gson = new GsonBuilder()
                .setDateFormat(dateFormat)
                .create();
    }

    @Override
    public byte[] convert(Object bean) throws Exception{
        if (bean == null) {
            return new byte[0];
        }
        try {
            return gson.toJson(bean).getBytes(encode);
        } catch (Exception e) {
            logger.warn("Gson convert Error, type:" + bean.getClass() + ", bean:" + bean);
            throw e;
        }
    }

    @Override
    public <T> T convert(byte[] data, Class<T> type) throws Exception{
        if (data == null || data.length <= 0) {
            return null;
        }
        try {
            return gson.fromJson(new String(data, encode), type);
        } catch (Exception e) {
            logger.warn("Gson convert Error, type:" + type + ", hex:" + ByteUtils.bytesToHex(data));
            logger.warn("Gson convert Error, string:" + new String(data, encode));
            throw e;
        }
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

}
