/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.slate.common.x.conversion.mapxbean;

import ch.qos.logback.classic.Level;
import org.junit.Assert;
import org.junit.Test;
import sviolet.slate.common.helper.logback.LogbackHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;

public class MxbTypeJudgerTest {

    @Test
    public void isIndivisible(){
        LogbackHelper.setLevel("com.github.shepherdviolet.glaciion", Level.INFO);

        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(MyEnum.A.getClass()));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(MyEnum.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(int.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(Long.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(void.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(Object.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(byte[].class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(BigDecimal.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(java.sql.Date.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(LocalDateTime.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(String.class));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isIndivisible(MyIndivisible2.class));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isIndivisible(MxbTypeJudgerTest.class));
    }

//    public static void main(String[] args) {
//        long time = System.currentTimeMillis();
//        boolean result = false;
//        for (int i = 0 ; i < 1000000 ; i++) {
//            result = MxbConstants.TYPE_JUDGER.isIndivisible(MxbTypeJudgerTest.class);
//        }
//        System.out.println(result);
//        System.out.println(System.currentTimeMillis() - time);
//    }

    @Test
    public void isBean(){
        LogbackHelper.setLevel("com.github.shepherdviolet.glaciion", Level.INFO);

        //Judged by type
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyEnum.A.getClass(), true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(int.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(java.sql.Date.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(String.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyBean1[].class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(Map.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(AbstractMap.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyIndivisible1.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyIndivisible2.class, true, true));

        //Judged by read write method
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyBean1.class, true, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyBean2.class, true, true));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isBean(MyBean3.class, true, true));

        //Judged by read method
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyBean1.class, true, false));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isBean(MyBean2.class, true, false));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isBean(MyBean3.class, true, false));

        //Judged by write method
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isBean(MyBean1.class, false, true));
        Assert.assertFalse(MxbConstants.TYPE_JUDGER.isBean(MyBean2.class, false, true));
        Assert.assertTrue(MxbConstants.TYPE_JUDGER.isBean(MyBean3.class, false, true));
    }

    public enum MyEnum {
        A,
        B
    }

    /**
     * Mark indivisible in test/resources/META-INF/glaciion/properties/sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudgerImpl
     */
    public static class MyIndivisible1 {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Mark indivisible in test/resources/META-INF/glaciion/properties/sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudgerImpl
     */
    public static class MyIndivisible2 {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class MyBean1 {

        private String name;

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class MyBean2 {

        private String name;

        public String getName() {
            return name;
        }
    }

    public static class MyBean3 {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
