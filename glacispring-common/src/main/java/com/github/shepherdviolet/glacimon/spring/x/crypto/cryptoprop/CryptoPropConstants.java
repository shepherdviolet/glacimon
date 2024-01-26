/*
 * Copyright (C) 2022-2024 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop;

/**
 * <p>[Spring属性解密] 常量</p>
 *
 * @author shepherdviolet
 */
public class CryptoPropConstants {

    /**
     * 解密密钥: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     */
    public static final String OPTION_DECRYPT_KEY = "glacispring.crypto-prop.key";

    /**
     * 模式: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     */
    public static final String OPTION_MODE = "glacispring.crypto-prop.mode";

    /**
     * [增强模式] 指定哪些PropertySource不侵入: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     */
    public static final String OPTION_SKIP_PROPERTY_SOURCES = "glacispring.crypto-prop.enhanced.skip-property-sources";

    /**
     * [增强模式] 使用代理模式侵入: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     */
    public static final String OPTION_INTERCEPT_BY_PROXY = "glacispring.crypto-prop.enhanced.intercept-by-proxy";

    /**
     * [临时应急] 忽略CryptoProp初始化错误, 仅打印日志, 解密功能不可用: 注意这个参数只能通过启动参数设置 (-Dglacispring.crypto-prop.ignore-exception=true)
     */
    public static final String OPTION_IGNORE_EXCEPTION = "glacispring.crypto-prop.ignore-exception";

    /**
     * [临时应急|有风险] 在日志中明文打印密钥, 用于排查问题: 注意这个参数只能通过启动参数设置 (-Dglacispring.crypto-prop.print-key-unsafe=true)
     */
    public static final String OPTION_PRINT_KEY_UNSAFE = "glacispring.crypto-prop.print-key-unsafe";

}
