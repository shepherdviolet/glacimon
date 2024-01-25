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

import org.slf4j.LoggerFactory;

/**
 * [Spring属性解密] 属性解密模式
 *
 * @author shepherdviolet
 */
public enum CryptoPropMode {

    /**
     * 普通模式 = CutInConfigurer
     */
    NORMAL(true, false),

    /**
     * 加强模式 = CutInConfigurer + CutInEnvironment
     */
    ENHANCED(true, true),

    /**
     * 在PropertySourcesPlaceholderConfigurer切入解密逻辑
     */
    CUT_IN_CONFIGURER(true, false),

    /**
     * 在Environment切入解密逻辑
     */
    CUT_IN_ENVIRONMENT(false, true),

    /**
     * 在所有地方切入解密逻辑
     */
    CUT_IN_ALL(true, true);



    private final boolean cutInConfigurer;
    private final boolean cutInEnvironment;

    CryptoPropMode(boolean cutInConfigurer, boolean cutInEnvironment) {
        this.cutInConfigurer = cutInConfigurer;
        this.cutInEnvironment = cutInEnvironment;
    }

    public boolean isCutInConfigurer() {
        return cutInConfigurer;
    }

    public boolean isCutInEnvironment() {
        return cutInEnvironment;
    }

    public static CryptoPropMode parseMode(String modeString){
        try {
            return CryptoPropMode.valueOf(modeString.toUpperCase());
        } catch (Throwable t) {
            LoggerFactory.getLogger(CryptoPropMode.class).warn("CryptoProp | Illegal CryptoPropMode '" + modeString + "', fail back to 'NORMAL'");
        }
        return CryptoPropMode.NORMAL;
    }

}
