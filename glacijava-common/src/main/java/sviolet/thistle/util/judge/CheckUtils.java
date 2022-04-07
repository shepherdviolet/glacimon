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

package sviolet.thistle.util.judge;

/**
 * 用于检查数据的工具
 * @author S.Violet
 */
public class CheckUtils {

    /**
     * 检查String为空<br/>
     * null || "" <br/>
     * @param input 检查数据
     * @return true 空 false 非空
     */
    public static boolean isEmpty(String input){
        return input == null || input.length() <= 0;
    }

    /**
     * 检查String不为空<br/>
     * !(null || "") <br/>
     * @param input 检查数据
     * @return true 非空 false 空
     */
    public static boolean notEmpty(String input){
        return input != null && input.length() > 0;
    }

    /**
     * 检查String为空或空白<br/>
     * null || "" || "  " <br/>
     * @param input 检查数据
     * @return true 空 false 非空
     */
    public static boolean isEmptyOrBlank(String input){
        return input == null || input.length() <= 0 || input.trim().length() <= 0;
    }

    /**
     * 检查String为空或空白<br/>
     * !(null || "" || "  ") <br/>
     * @param input 检查数据
     * @return true 非空 false 空
     */
    public static boolean notEmptyNotBlank(String input){
        return input != null && input.length() > 0 && input.trim().length() > 0;
    }

    /**
     * 检查位标记是否符合<p/>
     *
     * <pre>
     * 例如:
     * input    =   0x00001100;
     * flag     =   0x00000001;
     * return   =   false;
     *
     * input    =   0x00001100;
     * flag     =   0x00001000;
     * return   =   true;
     * </pre>
     *
     * @param input 检查数据
     * @param flag 检查标记
     * @return true 符合标记 false 不符合标记
     */
    public static boolean isFlagMatch(int input, int flag){
        return (input & flag) > 0;
    }

}
