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

package sviolet.slate.common.helper.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import sviolet.thistle.util.judge.CheckUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>[Spring Bean] 根据Apollo配置中心动态更新内容的参数集合, 必须声明为Spring Bean才有效</p>
 * <p>依赖com.ctrip.framework.apollo:apollo-client</p>
 *
 * @author S.Violet
 */
public class ApolloRefreshableProperties implements InitializingBean {

    private static final Map<String, String> EMPTY_MAP = new HashMap<>(0);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String namespace = ConfigConsts.NAMESPACE_APPLICATION;
    private String prefix = "";
    private boolean isSubPrefix = true;

    private Config config;
    private volatile Map<String, String> properties = EMPTY_MAP;

    /**
     * 获取参数
     * @param key 参数名
     * @return 参数值
     */
    public String get(String key) {
        return properties.get(key);
    }

    /**
     * namespace下所有符合prefix的参数名
     * @return 参数名集合
     */
    public Set<String> keySet() {
        return properties.keySet();
    }

    /**
     * 设置Apollo配置的namespace, 参数会从该namespace中获取
     * @param namespace Apollo配置的namespace
     */
    public ApolloRefreshableProperties setNamespace(String namespace) {
        if (namespace == null) {
            namespace = ConfigConsts.NAMESPACE_APPLICATION;
        }
        this.namespace = namespace;
        return this;
    }

    /**
     * 设置参数名的前缀, 只有以该前缀开头的参数才会被加载到这个集合中
     * @param prefix 参数名的前缀
     */
    public ApolloRefreshableProperties setPrefix(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        this.prefix = prefix;
        return this;
    }

    /**
     * 是否将参数名前缀裁掉, 如果设置true, 集合中的key值将不会存在前缀的字符, 如果设置false, 集合中的key值将保留前缀字符(不裁剪)
     * @param subPrefix 默认true
     */
    public ApolloRefreshableProperties setSubPrefix(boolean subPrefix) {
        isSubPrefix = subPrefix;
        return this;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CheckUtils.isEmptyOrBlank(namespace)) {
            logger.warn("ApolloRefreshableProperties | Unable to load properties from apollo, because namespace is null or empty");
            return;
        }
        //获取Apollo配置
        config = ConfigService.getConfig(namespace);
        //检查配置是否获取到
        checkConfig();
        //监听配置变化
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                //Apollo配置变更时
                refresh();
            }
        });
        //启动时先加载一次配置
        refresh();
        logger.info("ApolloRefreshableProperties | Listening property changes from apollo, namespace: " + namespace);
    }

    private void refresh(){
        if (config == null) {
            logger.debug("ApolloRefreshableProperties | Refresh | No config instance");
            return;
        }
        Set<String> names = config.getPropertyNames();
        if (names == null || names.size() <= 0) {
            properties = EMPTY_MAP;
            logger.debug("ApolloRefreshableProperties | Refresh | No property in namespace '" + namespace + "'");
            return;
        }

        Map<String, String> newProperties = new HashMap<>();
        logger.debug("ApolloRefreshableProperties | Refresh | Refreshing namespace '" + namespace + "'");
        for (String name : names) {
            if (name.startsWith(prefix)) {
                String key = (!isSubPrefix || prefix.length() <= 0) ? name : name.substring(prefix.length());
                String value = config.getProperty(name, null);
                newProperties.put(key, value);
                logger.debug("ApolloRefreshableProperties | Refresh | " + name + " -> " + key + "=" + value);
            }
        }
        properties = newProperties;
    }

    private void checkConfig() {
        //兼容老版本没有getSourceType方法
        try {
            Config.class.getMethod("getSourceType");
        } catch (Exception e) {
            return;
        }
        if (config == null || config.getSourceType() == ConfigSourceType.NONE) {
            logger.error("ApolloRefreshableProperties | Unable to load properties from apollo, namespace '" + namespace + "' not found");
            throw new IllegalStateException("ApolloRefreshableProperties | Unable to load properties from apollo, namespace '" + namespace + "' not found");
        } else if (config.getSourceType() == ConfigSourceType.LOCAL) {
            logger.warn("ApolloRefreshableProperties | Namespace '" + namespace + "' in local cache only, can not refresh from apollo server");
        }
    }

}
