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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced;

import org.springframework.core.env.MutablePropertySources;

/**
 * <p>[Spring属性解密] PropertySource转换器(切入解密逻辑), 加强模式(或CUT_IN_ENVIRONMENT模式)专用</p>
 *
 * <p>加强模式(或CUT_IN_ENVIRONMENT模式)用这个转换器, 对Environment中的PropertySource进行转换, 切入解密逻辑.</p>
 *
 * @author shepherdviolet
 */
public interface ICryptoPropertySourceConverter {

    void convertPropertySources(MutablePropertySources propSources);

}