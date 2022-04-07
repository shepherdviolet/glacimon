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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.model.common;

import sviolet.thistle.util.judge.CheckUtils;

import java.util.Properties;

/**
 * 先从系统参数(启动参数)中取值, 若取值失败(不存在或解析失败), 从内置的Properties中取值, 若取值失败(不存在或解析失败), 返回默认值
 *
 * @author S.Violet
 */
public class SysPropFirstProperties {

    private Properties properties;
    private LogHandler logHandler;

    /**
     * 先从系统参数(启动参数)中取值, 若取值失败(不存在或解析失败), 从内置的Properties中取值, 若取值失败(不存在或解析失败), 返回默认值
     * @param properties 先从系统参数(启动参数)中取值, 然后从这个properties中取值
     * @param logHandler 日志处理器, 用于输出: 系统参数覆盖了内置参数 / 数据解析失败 等日志
     */
    public SysPropFirstProperties(Properties properties, LogHandler logHandler) {
        if (properties == null) {
            throw new NullPointerException("properties is null");
        }
        this.properties = properties;
        this.logHandler = logHandler;
    }

    /**
     * 取值
     * @param key key, 系统参数(启动参数)和内置的Properties都用这个key取值
     * @param def 默认值
     */
    public String getString(String key, String def) {
        return getStringByDiffKey(key, key, def);
    }

    /**
     * 取值
     * @param sysPropKey 系统参数(启动参数)的取值key
     * @param propKey 内置的Properties的取值key
     * @param def 默认值
     */
    public String getStringByDiffKey(String sysPropKey, String propKey, String def) {
        String value = System.getProperty(sysPropKey, null);
        if (value != null) {
            return value;
        }
        return properties.getProperty(propKey, def);
    }

    /**
     * 取值
     * @param key key, 系统参数(启动参数)和内置的Properties都用这个key取值
     * @param def 默认值
     */
    public boolean getBoolean(String key, boolean def) {
        return getBooleanByDiffKey(key, key, def);
    }

    /**
     * 取值
     * @param sysPropKey 系统参数(启动参数)的取值key
     * @param propKey 内置的Properties的取值key
     * @param def 默认值
     */
    public boolean getBooleanByDiffKey(String sysPropKey, String propKey, boolean def) {
        String value = System.getProperty(sysPropKey, null);
        if (!CheckUtils.isEmptyOrBlank(value)) {
            return Boolean.parseBoolean(value);
        }
        value = properties.getProperty(propKey, null);
        if (!CheckUtils.isEmptyOrBlank(value)) {
            return Boolean.parseBoolean(value);
        }
        return def;
    }

    /**
     * 取值
     * @param key key, 系统参数(启动参数)和内置的Properties都用这个key取值
     * @param def 默认值
     */
    public int getInt(String key, int def) {
        return getIntByDiffKey(key, key, def);
    }

    /**
     * 取值
     * @param sysPropKey 系统参数(启动参数)的取值key
     * @param propKey 内置的Properties的取值key
     * @param def 默认值
     */
    public int getIntByDiffKey(String sysPropKey, String propKey, int def) {
        return (int) getInner(sysPropKey, propKey, def, INT_PARSER);
    }

    /**
     * 取值
     * @param key key, 系统参数(启动参数)和内置的Properties都用这个key取值
     * @param def 默认值
     */
    public long getLong(String key, long def) {
        return getLongByDiffKey(key, key, def);
    }

    /**
     * 取值
     * @param sysPropKey 系统参数(启动参数)的取值key
     * @param propKey 内置的Properties的取值key
     * @param def 默认值
     */
    public long getLongByDiffKey(String sysPropKey, String propKey, long def) {
        return (long) getInner(sysPropKey, propKey, def, LONG_PARSER);
    }

    /**
     * 取值
     * @param key key, 系统参数(启动参数)和内置的Properties都用这个key取值
     * @param def 默认值
     */
    public float getFloat(String key, float def) {
        return getFloatByDiffKey(key, key, def);
    }

    /**
     * 取值
     * @param sysPropKey 系统参数(启动参数)的取值key
     * @param propKey 内置的Properties的取值key
     * @param def 默认值
     */
    public float getFloatByDiffKey(String sysPropKey, String propKey, float def) {
        return (float) getInner(sysPropKey, propKey, def, FLOAT_PARSER);
    }

    /**
     * 取值
     * @param key key, 系统参数(启动参数)和内置的Properties都用这个key取值
     * @param def 默认值
     */
    public double getDouble(String key, double def) {
        return getDoubleByDiffKey(key, key, def);
    }

    /**
     * 取值
     * @param sysPropKey 系统参数(启动参数)的取值key
     * @param propKey 内置的Properties的取值key
     * @param def 默认值
     */
    public double getDoubleByDiffKey(String sysPropKey, String propKey, double def) {
        return (double) getInner(sysPropKey, propKey, def, DOUBLE_PARSER);
    }

    private Object getInner(String sysPropKey, String propKey, Object def, Parser parser) {
        String sysPropValue = System.getProperty(sysPropKey, null);
        String propValue;
        if (sysPropValue != null) {
            try {
                Object result = parser.parse(sysPropValue);
                if (logHandler != null) {
                    logHandler.onOverwrittenBySysProp(sysPropKey, sysPropValue, propKey, properties.getProperty(propKey, null), def, properties);
                }
                return result;
            } catch (Exception e) {
                propValue = properties.getProperty(propKey, null);
                if (logHandler != null) {
                    logHandler.onParseException(true, sysPropKey, sysPropValue, parser.toType(),
                            propValue != null ? propValue : def, properties, e);
                }
            }
        } else {
            propValue = properties.getProperty(propKey, null);
        }
        if (propValue == null) {
            return def;
        }
        try {
            return parser.parse(propValue);
        } catch (Exception e) {
            if (logHandler != null) {
                logHandler.onParseException(false, propKey, propValue, parser.toType(),
                        def, properties, e);
            }
        }
        if (logHandler != null) {
            logHandler.onUsingDefault(sysPropKey, sysPropValue, propKey, propValue, def, properties);
        }
        return def;
    }

    private static final Parser INT_PARSER = new Parser() {
        @Override
        public Object parse(String value) {
            return Integer.parseInt(value);
        }
        @Override
        public Class<?> toType() {
            return int.class;
        }
    };

    private static final Parser LONG_PARSER = new Parser() {
        @Override
        public Object parse(String value) {
            return Long.parseLong(value);
        }
        @Override
        public Class<?> toType() {
            return long.class;
        }
    };

    private static final Parser FLOAT_PARSER = new Parser() {
        @Override
        public Object parse(String value) {
            return Float.parseFloat(value);
        }
        @Override
        public Class<?> toType() {
            return float.class;
        }
    };

    private static final Parser DOUBLE_PARSER = new Parser() {
        @Override
        public Object parse(String value) {
            return Double.parseDouble(value);
        }
        @Override
        public Class<?> toType() {
            return double.class;
        }
    };

    private interface Parser {
        Object parse(String value);
        Class<?> toType();
    }

    /**
     * 日志处理器, 用于输出: 系统参数覆盖了内置参数 / 数据解析失败 等日志
     */
    public interface LogHandler {

        /**
         * 当系统参数(启动参数)存在, 并覆盖了内置参数时
         * @param sysPropKey 系统参数key
         * @param sysPropValue 系统参数的值
         * @param propKey 内置参数的key
         * @param propValue 内置参数的值
         * @param defValue 预设的默认值
         * @param properties 内置的Properties对象(不是系统参数)
         */
        void onOverwrittenBySysProp(String sysPropKey, String sysPropValue, String propKey, String propValue, Object defValue, Properties properties);

        /**
         * 当使用默认值时
         * @param sysPropKey 系统参数key
         * @param sysPropValue 系统参数的值
         * @param propKey 内置参数的key
         * @param propValue 内置参数的值
         * @param defValue 预设的默认值
         * @param properties 内置的Properties对象(不是系统参数)
         */
        void onUsingDefault(String sysPropKey, String sysPropValue, String propKey, String propValue, Object defValue, Properties properties);

        /**
         * 当value从String转为指定类型时发生异常. 你可以在这里打印日志(get方法会返回默认值)
         * @param parsingSysProp true:解析系统参数(启动参数)发生错误 false:解析内置的Properties参数发生错误
         * @param key 发生错误的key
         * @param value 发生错误的value
         * @param toType 尝试将value转换为该类型时出错
         * @param defValue 尝试使用的默认值
         * @param properties 内置的Properties对象(不是系统参数)
         * @param e 异常
         */
        void onParseException(boolean parsingSysProp, String key, String value, Class<?> toType, Object defValue, Properties properties, Exception e);

    }

}
