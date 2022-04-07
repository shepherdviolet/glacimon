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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.bean.mbrproc;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <p>在配置类中声明该注解, 添加SpringBean成员处理器</p>
 *
 * <p>
 * 说明:<br>
 * 1.用于对SpringContext中所有的Bean成员(Field/Method)进行处理, 处理时机为Bean装配阶段(BeanPostProcessor)<br>
 * 2.可以实现Bean成员的自定义注入/变换/代理替换等<br>
 * 3.该注解允许多次声明, 声明不同的处理器处理不同的注解
 * </p>
 *
 * <p>用法 ===========================================================================================</p>
 *
 * <p>定义一个注解:</p>
 *
 * <pre>
 *  <code>@Target({ElementType.FIELD, ElementType.METHOD})</code>
 *  <code>@Retention(RetentionPolicy.RUNTIME)</code>
 *  <code>@Documented</code>
 *  public @interface HttpClient {
 *      // ...
 *  }
 * </pre>
 *
 * <p>编写一个处理器:</p>
 *
 * <pre>
 *  public class HttpClientMemberProcessor implements MemberProcessor<HttpClient> {
 *
 *      public Class<HttpClient> acceptAnnotationType() {
 *          //声明本处理器接收的注解类型. 注意: 不允许存在两个以上的处理器处理同一个注解!
 *          return HttpClient.class;
 *      }
 *
 *      public void visitField(Object bean, String beanName, Field field, HttpClient annotation, ApplicationContext applicationContext) {
 *          //处理每个Bean的Field (前提是Field上申明了指定的注解)
 *      }
 *
 *      public void visitMethod(Object bean, String beanName, Method method, HttpClient annotation, ApplicationContext applicationContext) {
 *          //处理每个Bean的Method (前提是方法上申明了指定的注解)
 *      }
 *
 *  }
 * </pre>
 *
 * <p>启用:</p>
 *
 * <pre>
 *  <code>@Configuration</code>
 *  <code>@EnableMemberProcessor(HttpClientMemberProcessor.class)</code>
 *  public class MyConfiguration {
 *      // ...
 *  }
 * </pre>
 *
 * @author S.Violet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DefaultMemberProcessorSelector.class})
public @interface EnableMemberProcessor {

    Class<? extends MemberProcessor>[] value();

}
