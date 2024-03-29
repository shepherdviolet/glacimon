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

package com.github.shepherdviolet.glacimon.java.clazz;

import javassist.CannotCompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * 类的字节码替换测试
 *
 * @author shepherdviolet
 */
public class ClassReplacerTest {

    /**
     * JUnit4的时候本测试可以运行
     * JUnit5的时候, 只能在IDEA中单个执行, gradlew test批量执行的时候会报错, 因此改成main函数
     */
    public static void main(String[] args) throws IOException, CannotCompileException {
        ClassReplacer.replace("META-INF/classfiles/TargetClass.classfile");
        TargetClass targetClass = new TargetClass();
        System.out.println(targetClass.getData());
        Assertions.assertEquals(
                "new",
                targetClass.getData());
    }

}
