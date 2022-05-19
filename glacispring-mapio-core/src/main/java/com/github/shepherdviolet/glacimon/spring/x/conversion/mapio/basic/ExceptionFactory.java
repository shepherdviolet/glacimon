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

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic;

import com.github.shepherdviolet.glacimon.java.spi.api.annotation.SingleServiceInterface;

/**
 * <p>[扩展点: Spring扩展 / GlacimonSpi扩展]</p>
 *
 * <p>异常工厂: MapIo的公共逻辑和自带的过滤器在映射Map的过程中, 会抛出一些异常, 默认类型是RuntimeException. (默认实现: DefaultExceptionFactory)</p>
 * <p>应用工程可以扩展实现本工厂, 将那些异常修改为自己需要的类型, 适配自己的工程. 有两种扩展方式: Spring方式和GlacimonSpi方式,
 * 分别适用于Spring项目和Java普通程序. </p>
 *
 * @author shepherdviolet
 */
@SingleServiceInterface
public interface ExceptionFactory {

    /**
     * 创建异常
     * @param ruleInfo 过滤规则信息, 用于打印日志/输出错误信息
     * @param errorCode 错误码
     * @param errorArgs 错误参数
     */
    RuntimeException createRuntimeException(RuleInfo ruleInfo, ErrorCode errorCode, String... errorArgs);

}
