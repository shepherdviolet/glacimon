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
     * 指定哪些PropertySource不侵入: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     */
    public static final String OPTION_SKIP_PROPERTY_SOURCES = "glacispring.crypto-prop.skip-property-sources";

    /**
     * 使用代理模式侵入: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     */
    public static final String OPTION_INTERCEPT_BY_PROXY = "glacispring.crypto-prop.intercept-by-proxy";

    /**
     * 将本地属性加入Environment: 无法通过APOLLO配置中心运行时修改, 修改后必须重启应用
     * XML方式(<context:property-placeholder location="..." />)加载的配置文件, Spring不会把它们加入Environment,
     * 为了让CryptoProp能够解密XML加载的配置文件, 我们默认会将它们加入Environment, 如果不需要, 可以设置本参数为false, 默认true
     */
    public static final String OPTION_ADD_LOCAL_TO_ENV = "glacispring.crypto-prop.add-local-to-env";

    /**
     * [临时应急] 忽略CryptoProp初始化错误, 仅打印日志, 解密功能不可用: 注意这个参数只能通过启动参数设置 (-Dglacispring.crypto-prop.ignore-exception=true)
     */
    public static final String OPTION_IGNORE_EXCEPTION = "glacispring.crypto-prop.ignore-exception";

    /**
     * [临时应急|有风险] 在日志中明文打印密钥, 用于排查问题: 注意这个参数只能通过启动参数设置 (-Dglacispring.crypto-prop.print-key-unsafe=true)
     */
    public static final String OPTION_PRINT_KEY_UNSAFE = "glacispring.crypto-prop.print-key-unsafe";

}
