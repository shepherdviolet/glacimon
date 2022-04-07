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

package sviolet.slate.common.helper.sentinel;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <p>用于Spring容器中修改EzSentinel配置. 说明文档见: https://github.com/shepherdviolet/slate/blob/master/docs/ezsentinel/guide.md</p>
 *
 * <p>依赖: compile "com.google.code.gson:gson:$version_gson"</p>
 *
 * <p>
 *     1.添加gson依赖: compile "com.google.code.gson:gson:$version_gson" <br>
 *     2.添加本注解来启用: EnableEzSentinel <br>
 *     3.增加参数: slate.common.ez-sentinel.rule-data, 设置限流熔断规则. <br>
 * </p>
 *
 * @author S.Violet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({EzSentinelConfiguration.class})
public @interface EnableEzSentinel {
}
