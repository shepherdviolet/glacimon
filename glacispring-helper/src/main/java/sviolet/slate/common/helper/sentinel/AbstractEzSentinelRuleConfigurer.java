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

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.*;

/**
 * <p>说明文档见: https://github.com/shepherdviolet/slate/blob/master/docs/ezsentinel/guide.md</p>
 *
 * <p>Sentinel官方最佳实践的方案中, 应该是改造Dashboard, 由Dashboard向一个数据源推送规则, 而客户端则连接数据源获取更新.
 * 而EzSentinel是一个简易的变通方案, 不改造Dashboard, 也不配置客户端数据源. 通过一个大JSON来维护规则(人工), 然后手动
 * 将这个JSON配置到Apollo配置中心, 借助Apollo准实时将配置用setter方法注入的特性, 在setter方法中显式地调用Sentinel的
 * API来实现规则配置. 而Dashboard仅用于观察(不做配置).</p>
 *
 * <p>通过EzSentinel配置规则后, 可以在Dashboard的流控规则中看到_ez_开头的资源, 这个是用来观察某个应用规则是否生效/何时生效/
 * 失败原因的.</p>
 *
 * @param <T> 规则数据类型
 * @author S.Violet
 */
public abstract class AbstractEzSentinelRuleConfigurer<T> implements EzSentinelRuleConfigurer<T> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static final String RULE_VERSION = "_ez_rule_version=";
    private static final String UPDATE_TIME = "_ez_rule_update_time=";
    private static final String ERROR_MESSAGE = "_ez_error_msg=";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 在Apollo配置变化时, setter方法中调用该方法更新规则, 参考{@link EnableEzSentinel}
     *
     * @param ruleData 规则数据
     */
    public void update(T ruleData) {
        logger.info("EzSentinel | Update sentinel rule start");

        //convert
        Rules rules = null;
        if (ruleData != null) {
            try {
                rules = convertRuleData(ruleData);
            } catch (Exception e) {
                handleException("illegal_rule_data", e, ruleData);
                return;
            }
        }

        //update
        updateRules(rules);
        logger.info("EzSentinel | Update sentinel rule finish");
    }

    protected abstract Rules convertRuleData(T ruleData);

    protected void updateRules(Rules rules) {

        if (rules == null) {
            SystemRuleManager.loadRules(Collections.emptyList());
            DegradeRuleManager.loadRules(Collections.emptyList());
            FlowRuleManager.loadRules(Collections.emptyList());
            logger.info("EzSentinel | No sentinel rules (It will clear all previous rules!)");
            return;
        }

        //System rules
        if (rules.systemRule != null) {
            SystemRuleManager.loadRules(Collections.singletonList(rules.systemRule.build()));
            logger.info("EzSentinel | System rule updated, 1 rules");
        } else {
            SystemRuleManager.loadRules(Collections.emptyList());
            logger.info("EzSentinel | System rule updated, 0 rules");
        }

        //Degrade / Flow rules
        List<DegradeRule> degradeRules = new LinkedList<>();
        List<FlowRule> flowRules = new LinkedList<>();

        if (rules.getResourceRules() != null && rules.getResourceRules().size() > 0) {
            Map<String, RuleGroup> ruleGroups = rules.getRuleGroups();
            if (ruleGroups == null) {
                handleException("missing_rule_groups", new Exception("Missing 'ruleGroups' in the rule data"), null);
                return;
            }

            for (Map.Entry<String, String> entry : rules.getResourceRules().entrySet()) {
                //Entry -> key: resourceName value: ruleGroupId
                String resource = entry.getKey();
                String ruleGroupId = entry.getValue();

                RuleGroup ruleGroup = ruleGroups.get(ruleGroupId);
                if (ruleGroup == null) {
                    handleException("undefined_rule_group", new Exception("Undefined rule group '" + ruleGroupId +
                            "' in ruleGroups, required by resource '" + resource + "'"), null);
                    return;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("EzSentinel | Resource '" + resource + "' -> ruleGroup '" + ruleGroupId + "'");
                }

                //Degrade rules
                if (ruleGroup.getDegradeRules() != null) {
                    for (DegradeRuleBuilder degradeRuleBuilder : ruleGroup.getDegradeRules()) {
                        degradeRules.add(degradeRuleBuilder.build(resource));
                        if (logger.isDebugEnabled()) {
                            logger.debug("EzSentinel | Resource '" + resource + "' -> " + degradeRuleBuilder);
                        }
                    }
                }

                //Flow rules
                if (ruleGroup.getFlowRules() != null) {
                    for (FlowRuleBuilder flowRuleBuilder : ruleGroup.getFlowRules()) {
                        flowRules.add(flowRuleBuilder.build(resource));
                        if (logger.isDebugEnabled()) {
                            logger.debug("EzSentinel | Resource '" + resource + "' -> " + flowRuleBuilder);
                        }
                    }
                }
            }
        }

        //comment
        flowRules.add(commentRule(UPDATE_TIME + TIME_FORMATTER.format(LocalDateTime.now())));
        flowRules.add(commentRule(RULE_VERSION + rules.getRuleVersion()));

        //load
        DegradeRuleManager.loadRules(degradeRules);
        FlowRuleManager.loadRules(flowRules);

        logger.info("EzSentinel | Degrade rule updated, " + degradeRules.size() + " rules");
        logger.info("EzSentinel | Flow rule updated, " + flowRules.size() + " rules");
    }

    protected void handleException(String errorCode, Exception e, T ruleData){
        if (e != null) {
            logger.error("EzSentinel | Error while updating sentinel rules, " + errorCode + ", " + TIME_FORMATTER.format(LocalDateTime.now()), e);
        } else {
            logger.error("EzSentinel | Error while updating sentinel rules, " + errorCode + ", " + TIME_FORMATTER.format(LocalDateTime.now()));
        }
        if (ruleData != null) {
            logger.error("EzSentinel | Rule data:" + ruleData);
        }
        List<FlowRule> flowRules = FlowRuleManager.getRules();
        flowRules.add(commentRule(ERROR_MESSAGE + errorCode + "_" + TIME_FORMATTER.format(LocalDateTime.now())));
        FlowRuleManager.loadRules(flowRules);
    }

    // Utils /////////////////////////////////////////////////////////////////////////////////////////////

    protected static int nullable(Integer value, int fallback){
        if (value == null) {
            return fallback;
        }
        return value;
    }

    protected static FlowRule commentRule(String comment){
        FlowRule flowRule = new FlowRule(comment);
        flowRule.setCount(0);
        return flowRule;
    }

    // Classes /////////////////////////////////////////////////////////////////////////////////////////////

    public static class Rules {

        private Map<String, RuleGroup> ruleGroups;
        private SystemRuleBuilder systemRule;
        private Map<String, String> resourceRules;
        private String ruleVersion;

        public Map<String, RuleGroup> getRuleGroups() {
            return ruleGroups;
        }

        public void setRuleGroups(Map<String, RuleGroup> ruleGroups) {
            this.ruleGroups = ruleGroups;
        }

        public SystemRuleBuilder getSystemRule() {
            return systemRule;
        }

        public void setSystemRule(SystemRuleBuilder systemRule) {
            this.systemRule = systemRule;
        }

        public Map<String, String> getResourceRules() {
            return resourceRules;
        }

        public void setResourceRules(Map<String, String> resourceRules) {
            this.resourceRules = resourceRules;
        }

        public String getRuleVersion() {
            return ruleVersion;
        }

        public void setRuleVersion(String ruleVersion) {
            this.ruleVersion = ruleVersion;
        }
    }

    public static class RuleGroup {

        private List<FlowRuleBuilder> flowRules;
        private List<DegradeRuleBuilder> degradeRules;

        public List<FlowRuleBuilder> getFlowRules() {
            return flowRules;
        }

        public void setFlowRules(List<FlowRuleBuilder> flowRules) {
            this.flowRules = flowRules;
        }

        public List<DegradeRuleBuilder> getDegradeRules() {
            return degradeRules;
        }

        public void setDegradeRules(List<DegradeRuleBuilder> degradeRules) {
            this.degradeRules = degradeRules;
        }
    }

    public static class FlowRuleBuilder {

        private double count;
        private String grade = "THREAD";
        private String limitApp = "default";
        private String strategy = "DIRECT";
        private String controlBehavior = "DEFAULT";

        public double getCount() {
            return count;
        }

        public void setCount(double count) {
            this.count = count;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getLimitApp() {
            return limitApp;
        }

        public void setLimitApp(String limitApp) {
            this.limitApp = limitApp;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getControlBehavior() {
            return controlBehavior;
        }

        public void setControlBehavior(String controlBehavior) {
            this.controlBehavior = controlBehavior;
        }

        private static final Map<String, Integer> FLOW_GRADE_MAP = new HashMap<String, Integer>(){{
            put("THREAD", FLOW_GRADE_THREAD);
            put("QPS", FLOW_GRADE_QPS);
        }};

        private static final Map<String, Integer> STRATEGY_MAP = new HashMap<String, Integer>(){{
            put("DIRECT", STRATEGY_DIRECT);
            put("RELATE", STRATEGY_RELATE);
            put("CHAIN", STRATEGY_CHAIN);
        }};

        private static final Map<String, Integer> CONTROL_BEHAVIOR_MAP = new HashMap<String, Integer>(){{
            put("DEFAULT", CONTROL_BEHAVIOR_DEFAULT);
            put("WARM_UP", CONTROL_BEHAVIOR_WARM_UP);
            put("RATE_LIMITER", CONTROL_BEHAVIOR_RATE_LIMITER);
            put("WARM_UP_RATE_LIMITER", CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER);
        }};

        protected FlowRule build(String resource){
            FlowRule flowRule = new FlowRule(resource);
            flowRule.setCount(getCount());
            flowRule.setGrade(nullable(FLOW_GRADE_MAP.get(getGrade()), FLOW_GRADE_THREAD));
            flowRule.setLimitApp(getLimitApp());
            flowRule.setStrategy(nullable(STRATEGY_MAP.get(getStrategy()), STRATEGY_DIRECT));
            flowRule.setControlBehavior(nullable(CONTROL_BEHAVIOR_MAP.get(getControlBehavior()), CONTROL_BEHAVIOR_DEFAULT));
            return flowRule;
        }

        @Override
        public String toString() {
            return "FlowRule{" +
                    "count=" + count +
                    ", grade='" + grade + '\'' +
                    ", limitApp='" + limitApp + '\'' +
                    ", strategy='" + strategy + '\'' +
                    ", controlBehavior='" + controlBehavior + '\'' +
                    '}';
        }
    }

    public static class DegradeRuleBuilder {

        private double count;
        private int timeWindow;//s
        private String grade = "RT";
        private String limitApp = "default";

        public double getCount() {
            return count;
        }

        public void setCount(double count) {
            this.count = count;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public int getTimeWindow() {
            return timeWindow;
        }

        public void setTimeWindow(int timeWindow) {
            this.timeWindow = timeWindow;
        }

        public String getLimitApp() {
            return limitApp;
        }

        public void setLimitApp(String limitApp) {
            this.limitApp = limitApp;
        }

        private static final Map<String, Integer> DEGRADE_GRADE_MAP = new HashMap<String, Integer>(){{
            put("RT", DEGRADE_GRADE_RT);
            put("EXCEPTION_RATIO", DEGRADE_GRADE_EXCEPTION_RATIO);
            put("EXCEPTION_COUNT", DEGRADE_GRADE_EXCEPTION_COUNT);
        }};

        protected DegradeRule build(String resource){
            DegradeRule degradeRule = new DegradeRule(resource);
            degradeRule.setCount(getCount());
            degradeRule.setGrade(nullable(DEGRADE_GRADE_MAP.get(getGrade()), DEGRADE_GRADE_RT));
            degradeRule.setTimeWindow(getTimeWindow());
            degradeRule.setLimitApp(getLimitApp());
            return degradeRule;
        }

        @Override
        public String toString() {
            return "DegradeRule{" +
                    "count=" + count +
                    ", timeWindow=" + timeWindow +
                    ", grade='" + grade + '\'' +
                    ", limitApp='" + limitApp + '\'' +
                    '}';
        }
    }

    public static class SystemRuleBuilder {

        private int highestSystemLoad = -1;//-1 disable
        private int avgRt = -1;//-1 disable
        private int maxThread = -1;//-1 disable
        private int qps = -1;//-1 disable

        public int getHighestSystemLoad() {
            return highestSystemLoad;
        }

        public void setHighestSystemLoad(int highestSystemLoad) {
            this.highestSystemLoad = highestSystemLoad;
        }

        public int getAvgRt() {
            return avgRt;
        }

        public void setAvgRt(int avgRt) {
            this.avgRt = avgRt;
        }

        public int getMaxThread() {
            return maxThread;
        }

        public void setMaxThread(int maxThread) {
            this.maxThread = maxThread;
        }

        public int getQps() {
            return qps;
        }

        public void setQps(int qps) {
            this.qps = qps;
        }

        protected SystemRule build(){
            SystemRule systemRule = new SystemRule();
            systemRule.setHighestSystemLoad(getHighestSystemLoad());
            systemRule.setAvgRt(getAvgRt());
            systemRule.setMaxThread(getMaxThread());
            systemRule.setQps(getQps());
            return systemRule;
        }
    }

}
