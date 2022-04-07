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

package sviolet.slate.common.helper.sentinel;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * <p>JSON格式的Sentinel规则设置器. 说明文档见: https://github.com/shepherdviolet/slate/blob/master/docs/ezsentinel/guide.md</p>
 *
 * <p>依赖: compile "com.google.code.gson:gson:$version_gson"</p>
 *
 * @author S.Violet
 */
public class JsonEzSentinelRuleConfigurer extends AbstractEzSentinelRuleConfigurer<String> {

    private final Gson gson = new Gson();

    /**
     * 同update方法
     * @param ruleData 规则数据
     */
    public void setRuleData(String ruleData) {
        update(ruleData);
    }

    @Override
    protected Rules convertRuleData(String ruleData) {
        String data = ruleData;
        if (ruleData != null){
            if (ruleData.startsWith("classpath:")){
                data = loadFromClasspath(ruleData);
            }
        }
        return gson.fromJson(data, Rules.class);
    }

    private String loadFromClasspath(String path) {
        String data;
        try {
            URL url = getClass().getClassLoader().getResource(path.substring(10));
            if (url == null) {
                throw new RuntimeException("Can not find the rule data file from " + path);
            }
            data = readFromInputStream(url.openStream(), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error while loading rule data from " + path, e);
        }
        return data;
    }

    protected String readFromInputStream(InputStream inputStream, String charset) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buff = new char[1024];
            int length;
            while ((length = bufferedReader.read(buff)) >= 0) {
                stringBuilder.append(buff, 0, length);
            }
            return stringBuilder.toString();
        }
    }

}
