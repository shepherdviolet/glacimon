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

package com.github.shepherdviolet.glacimon.spring.x.beanfactory.mbrproc;

import java.lang.annotation.Annotation;

/**
 * <p>默认的ImportSelector, 配合EnableMemberProcessor注解开启功能</p>
 * @author shepherdviolet
 */
public class DefaultMemberProcessorSelector extends MemberProcessorSelector {

    @Override
    protected Class<? extends Annotation> getEnableAnnotationType() {
        return EnableMemberProcessor.class;
    }

}
