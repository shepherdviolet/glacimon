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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.asm;

import javassist.CannotCompileException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * 类的字节码替换测试
 *
 * @author S.Violet
 */
public class ClassReplacerTest {

    @Test
    public void test() throws IOException, CannotCompileException {
        ClassReplacer.replace("META-INF/classfiles/TargetClass.classfile");
        TargetClass targetClass = new TargetClass();
//        System.out.println(targetClass.getData());
        Assert.assertEquals(
                "new",
                targetClass.getData());
    }

}
