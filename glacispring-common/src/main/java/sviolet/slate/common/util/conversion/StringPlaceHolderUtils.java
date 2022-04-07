/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.slate.common.util.conversion;

import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Map;

/**
 * 字符串占位符处理工具, 依赖spring-core
 *
 * @author S.Violet
 */
public class StringPlaceHolderUtils {

    private static final PropertyPlaceholderHelper STANDARD_HELPER = new PropertyPlaceholderHelper("${", "}", ":", true);

    /**
     * 利用Spring的PropertyPlaceholderHelper处理字符串中的标准占位符, 例如: no-${id:0} -> no-1
     * @param string 待处理的字符串
     * @param properties 参数, 用参数中的值替换字符串中的占位符
     * @return 处理后的字符串, 遇到无法处理的占位符时, 会保留原样返回
     */
    public static String replaceStandardly(String string, Map<String, String> properties){
        return STANDARD_HELPER.replacePlaceholders(string, new PropertyPlaceholderHelper.PlaceholderResolver() {
            @Override
            public String resolvePlaceholder(String placeholderName) {
                return properties.get(placeholderName);
            }
        });
    }

}
