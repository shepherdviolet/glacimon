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

package sviolet.slate.common.util.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class LambdaBuilderTest {

    @Test
    public void test(){
        Bean bean = LambdaBuilder.object(() -> {
            Bean obj = new Bean();
            obj.setName("123");
            obj.setId("456");
            return obj;
        });
        Assert.assertEquals("Bean{name='123', id='456'}", bean.toString());
        Map<String, Object> map = LambdaBuilder.hashMap(i -> {
            i.put("a", "b");
            i.put("c", "d");
        });
        Assert.assertEquals("{a=b, c=d}", map.toString());
        List<String> list = LambdaBuilder.arrayList(i -> {
           i.add("a");
           i.add("b");
        });
        Assert.assertEquals("[a, b]", list.toString());
    }

    private static final class Bean {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Bean{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

}
