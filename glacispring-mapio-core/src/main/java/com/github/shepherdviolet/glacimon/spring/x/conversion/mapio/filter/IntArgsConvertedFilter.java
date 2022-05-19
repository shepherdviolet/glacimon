/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter;

/**
 * <p>字段过滤器 (校验/转换): 过滤参数类型为Integer</p>
 *
 * @author shepherdviolet
 */
public abstract class IntArgsConvertedFilter extends ArgsConvertedFilter<Integer> {

    /**
     * 过滤参数: String -> Integer
     */
    @Override
    public Integer[] convertArgs(String[] args) throws Exception {
        Integer[] intArgs = new Integer[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            intArgs[i] = Integer.parseInt(args[i]);
        }
        return intArgs;
    }

}
