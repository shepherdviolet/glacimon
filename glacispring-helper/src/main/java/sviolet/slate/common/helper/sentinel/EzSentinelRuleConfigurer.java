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

/**
 * @see AbstractEzSentinelRuleConfigurer
 * @param <T> 规则数据类型
 * @author S.Violet
 */
public interface EzSentinelRuleConfigurer <T> {

    /**
     * 在Apollo配置变化时, setter方法中调用该方法更新规则, 参考{@link EnableEzSentinel}
     *
     * @param ruleData 规则数据
     */
    void update(T ruleData);

}
