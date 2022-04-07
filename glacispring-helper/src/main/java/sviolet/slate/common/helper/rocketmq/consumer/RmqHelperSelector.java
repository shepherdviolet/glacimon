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

package sviolet.slate.common.helper.rocketmq.consumer;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>ImportSelector</p>
 *
 * @author S.Violet
 */
public class RmqHelperSelector implements ImportSelector {

    static AnnotationAttributes annotationAttributes;

    @Override
    public final String[] selectImports(AnnotationMetadata importingClassMetadata) {
        /*
         * 此处用静态变量持有注解参数, 原因同InterfaceInstConfig
         */
        annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRocketMqHelper.class.getName(), false));
        //指定配置类
        return new String[]{RmqHelperConfig.class.getName()};
    }

}
