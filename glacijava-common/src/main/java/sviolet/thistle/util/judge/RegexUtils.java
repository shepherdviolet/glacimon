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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具
 *
 * @author S.Violet
 */
public class RegexUtils {


    private static final String REGEX_CELLPHONE = "^1(3[0-9]|4[57]|5[0-35-9]|7[6-8]|8[0-9]|70)\\d{8}$";

    /**
     * 验证手机号码
     *
     * 13[0-9], 14[5,7], 15[0, 1, 2, 3, 5, 6, 7, 8, 9], 17[6, 7, 8], 18[0-9], 170[0-9]
     * 移动号段: 134,135,136,137,138,139,150,151,152,157,158,159,182,183,184,187,188,147,178,1705
     * 联通号段: 130,131,132,155,156,185,186,145,176,1709
     * 电信号段: 133,153,180,181,189,177,1700
     */
    public static boolean checkCellphone(String cellphone) {
        return match(REGEX_CELLPHONE, cellphone);
    }

    private static final String REGEX_TELEPHONE = "^(0\\d{2}-\\d{8}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)$";

    /**
     * 验证固话号码
     */
    public static boolean checkTelephone(String telephone) {
        return match(REGEX_TELEPHONE, telephone);
    }

    /**
     * 判断字符串是否符合正则表达式
     * @param regex 正则表达式
     * @param string 字符串
     * @return true:符合
     */
    public static boolean match(String regex, String string){
        if (string == null){
            return false;
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string);
        return m.matches();
    }

}
