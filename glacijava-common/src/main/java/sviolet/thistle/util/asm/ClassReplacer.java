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
import javassist.ClassPool;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>[谨慎使用] 依赖: org.javassist:javassist </p>
 * <p>类替换器: 在一个类被加载前, 可以将它的字节码替换为指定的class(文件) </p>
 *
 * <p>用途: 如果你想修改一个JAR包中的一个类的实现, 但是又不想破坏JAR包, 可以用这个办法, 将自己的实现编译好, 在程序启动时,
 * 用这个工具加载一下class, JVM中的这个类就变成你自己的实现了. </p>
 *
 * <p>示例:</p>
 * <p>1.将一个类编译成class文件</p>
 * <pre>
 * public class TargetClass {
 *     public String getData(){
 *         return "new";
 *     }
 * }
 * </pre>
 * <p>2.将文件重命名: 类名.class -> 类名.classfile</p>
 * <p>3.将文件放进Classpath中, 例如: /META-INF/classfiles/类名.classfile</p>
 * <p>4.在应用启动后第一时间调用: ClassReplacer.replace("META-INF/classfiles/类名.classfile");</p>
 *
 * @author S.Violet
 */
public class ClassReplacer {

    /**
     * <p>在一个类被加载前, 可以将它的字节码替换为指定的class(文件)</p>
     *
     * <pre>
     *     ClassReplacer.replace("META-INF/classfiles/TargetClass.classfile");
     * </pre>
     *
     * @param classDataClasspath 文件在classpath下的路径
     */
    public static void replace(String classDataClasspath) throws IOException, CannotCompileException {
        replace(ClassReplacer.class.getClassLoader().getResourceAsStream(classDataClasspath));
    }

    /**
     * <p>在一个类被加载前, 可以将它的字节码替换为指定的class(文件)</p>
     *
     * <pre>
     *     ClassReplacer.replace(new FileInputStream("E:\\_Temp\\TargetClass.classfile"));
     * </pre>
     *
     * @param classDataInputStream 指定的class文件的输入流
     */
    public static void replace(InputStream classDataInputStream) throws IOException, CannotCompileException {
        ClassPool.getDefault().makeClass(classDataInputStream).toClass();
    }

}
