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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [非SpringBoot专用]
 * 1.动态配置规则
 * 2.如果没有设置四个必要的启动参数, 则禁用
 *
 * @author S.Violet
 */
public class JsonEzSentinelRuleConfigurerForSpring4 implements EzSentinelRuleConfigurer<String> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EzSentinelRuleConfigurer<String> configurer = createConfigurer();

    @Override
    public void update(String ruleData) {
        configurer.update(ruleData);
    }

    /**
     * 同update方法
     * @param ruleData 规则数据
     */
    public void setRuleData(String ruleData) {
        update(ruleData);
    }

    protected EzSentinelRuleConfigurer<String> createConfigurer(){
        if (isEnabled()) {
            try {
                EzSentinelRuleConfigurer<String> configurer = (EzSentinelRuleConfigurer<String>) Class.forName("sviolet.slate.common.helper.sentinel.JsonEzSentinelRuleConfigurer").newInstance();
                logger.info("JsonEzSentinelRuleConfigurerForSpring4 | Sentinel Enabled");
                return configurer;
            } catch (Exception e) {
                logger.error("JsonEzSentinelRuleConfigurerForSpring4 | Sentinel Disabled, Error while create JsonEzSentinelRuleConfigurer", e);
            }
        }
        //这个只对核心有效, 即对本地统计限流熔断有效
        com.alibaba.csp.sentinel.Constants.ON = false;
        return new EzSentinelRuleConfigurer<String>() {
            @Override
            public void update(String ruleData) {
                //do nothing
            }
        };
    }

    protected boolean isEnabled(){
        if (System.getProperty("project.name") == null) {
            logger.error("JsonEzSentinelRuleConfigurerForSpring4 | Sentinel Disabled, Because '-Dproject.name' is not set");
            return false;
        }
        if (System.getProperty("csp.sentinel.statistic.max.rt") == null) {
            logger.error("JsonEzSentinelRuleConfigurerForSpring4 | Sentinel Disabled, Because '-Dcsp.sentinel.statistic.max.rt=120000' is not set");
            return false;
        }
        if (System.getProperty("csp.sentinel.dashboard.server") == null) {
            logger.error("JsonEzSentinelRuleConfigurerForSpring4 | Sentinel Disabled, Because '-Dcsp.sentinel.dashboard.server' is not set");
            return false;
        }
        if (System.getProperty("csp.sentinel.api.port") == null) {
            logger.error("JsonEzSentinelRuleConfigurerForSpring4 | Sentinel Disabled, Because '-Dcsp.sentinel.api.port' is not set");
            return false;
        }
        return true;
    }

}
