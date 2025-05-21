/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.java.conversion;

import com.github.shepherdviolet.glacimon.java.collections.LambdaBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class BeanMapConverterTest {

    @Test
    public void test() {

        Bean bean = new Bean();
        Map<String, Object> map = LambdaBuilder.hashMap(m -> {
            m.put("str", "str value");
            m.put("i", 10086);
            m.put("list", LambdaBuilder.arrayList(ll -> {
                ll.add("list value 1");
                ll.add("list value 2");
            }));
            m.put("map", LambdaBuilder.hashMap(mm -> {
                mm.put("map key 1", "map value 1");
                mm.put("map key 2", "map value 2");
            }));
            m.put("noWrite", "noWrite new value");
            m.put("noRead", "noRead new value");
            m.put("typeMisMath", "typeMisMath new value");
        });

        BeanMapConverter.mapToBean(map, bean);
        Assertions.assertEquals("Bean{str='str value', i=10086, list=[list value 1, list value 2], map={map key 2=map value 2, map key 1=map value 1}, noWrite='noWrite old value', noRead='noRead new value', typeMisMatch=1024}", bean.toString());

        Map<String, Object> map2 = BeanMapConverter.beanToMap(bean);
        Assertions.assertEquals("{str=str value, typeMisMatch=1024, noWrite=noWrite old value, i=10086, list=[list value 1, list value 2], map={map key 2=map value 2, map key 1=map value 1}, noRead=null}", map2.toString());

    }

    private static class Bean {

        String str;
        int i;
        List<String> list;
        Map<String, Integer> map;

        String noWrite = "noWrite old value";
        String noRead;
        int typeMisMatch = 1024;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        public Map<String, Integer> getMap() {
            return map;
        }

        public void setMap(Map<String, Integer> map) {
            this.map = map;
        }

        public String getNoWrite() {
            return noWrite;
        }

        public void setNoRead(String noRead) {
            this.noRead = noRead;
        }

        public int getTypeMisMatch() {
            return typeMisMatch;
        }

        public void setTypeMisMatch(int typeMisMatch) {
            this.typeMisMatch = typeMisMatch;
        }

        @Override
        public String toString() {
            return "Bean{" +
                    "str='" + str + '\'' +
                    ", i=" + i +
                    ", list=" + list +
                    ", map=" + map +
                    ", noWrite='" + noWrite + '\'' +
                    ", noRead='" + noRead + '\'' +
                    ", typeMisMatch=" + typeMisMatch +
                    '}';
        }

    }

}
