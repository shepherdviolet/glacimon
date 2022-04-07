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

package sviolet.slate.common.x.proxy.interfaceinst;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <p>接口自动实例化工具</p>
 *
 * <p>
 * 用于将指定路径下的接口类实例化, 用于对接口做AOP切面. <br>
 *     1)扫描指定路径下的所有接口, 检查是否申明了@InterfaceInstance注解<br>
 *     2)对接口做代理, 每个方法均为空实现<br>
 *     3)该注解可以多次配置, 配置不同的包路径和实例化器<br>
 * </p>
 *
 * <p>1.启用配置, 指定路径(可指定多个)</p>
 * <pre>
 *      <code>@Configuration</code>
 *      <code>@EnableInterfaceInstantiation(basePackages = {"template.interfaces", "template.interfaces2"})</code>
 *      public class AdvisorConfiguration {
 *          ......
 *      }
 * </pre>
 *
 * <p>2.在指定路径下定义接口, 加上@InterfaceInstance注解</p>
 * <pre>
 *      package template.interfaces;
 *      <code>@InterfaceInstance</code>
 *      public interface MyInterface {
 *          String method(String request);
 *      }
 * </pre>
 *
 * <p>3.对接口类做AOP切面, 实现需要的逻辑(省略过程...)</p>
 *
 * <p>4.注入, 使用</p>
 * <pre>
 *      <code>@Autowired</code>
 *      private MyInterface myInterface;
 *      <code>@RequestMapping("/test")</code>
 *      public @ResponseBody String test(){
 *          return myInterface.method("hello");
 *      }
 * </pre>
 *
 * @author S.Violet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DefaultInterfaceInstSelector.class})
public @interface EnableInterfaceInstantiation {

    /**
     * 自定义参数:
     * 配置需要实例化的接口类包路径(可指定多个)
     */
    String[] basePackages();

    /**
     * 自定义参数:
     * 配置接口类实例化器(可自定义实现)
     * 默认DefaultInterfaceInstantiator.class
     */
    Class<? extends InterfaceInstantiator> interfaceInstantiator() default DefaultInterfaceInstantiator.class;

    /**
     * 自定义参数:
     * true: 指定包路径下的接口类, 必须申明指定注解才进行实例化(注解类型由annotationClass指定, 默认@InterfaceInstance).
     * false: 指定包路径下的接口类, 不声明指定注解也进行实例化.
     * 默认true
     */
    boolean annotationRequired() default true;

    /**
     * 自定义参数:
     * 当annotationRequired为true时, 指定包路径下的接口类必须声明指定的注解才能实例化, 注解类型可以在这里定义.
     * 默认@InterfaceInstance
     */
    Class<? extends Annotation> annotationClass() default InterfaceInstance.class;

}
