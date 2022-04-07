/*
 * Copyright (C) 2015-2021 S.Violet
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

package sviolet.thistle.util.conversion;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class UuidUtilsTest {

    @Test
    public void test() {
        UUID uuid = UUID.fromString("3e164de2-db66-4773-bdfe-9427dd05e3f8");
        Assert.assertEquals("3e164de2-db66-4773-bdfe-9427dd05e3f8",
                UuidUtils.toStringUuid(uuid));
        Assert.assertEquals("3e164de2db664773bdfe9427dd05e3f8",
                UuidUtils.toStringUuidWithoutDash(uuid));
        Assert.assertEquals("PhZN4ttmR3O9_pQn3QXj-A",
                UuidUtils.toStringUuidCompressed(uuid));
    }

//    public static void main(String[] args) {
//        String uuid = null;
//        long time = System.currentTimeMillis();
//        for (int i = 0 ; i < 10000000 ; i++) {
////            uuid = UUID.randomUUID().toString().replaceAll("-", "");//12072ms
////            uuid = UuidUtils.newStringUuid();//7499ms
//            uuid = UuidUtils.newStringUuidWithoutDash();//4532ms
////            uuid = UuidUtils.newStringUuidCompressed();//4746ms
//        }
//        System.out.println(System.currentTimeMillis() - time);
//        System.out.println(uuid);
//    }

}
