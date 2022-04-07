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

package sviolet.slate.common.helper.mybatis.monitor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.monitor.txtimer.TimerContext;
import sviolet.slate.common.x.monitor.txtimer.TxTimer;

import java.util.Properties;

/**
 * <p>使用TxTimer统计sql执行时间, 依赖: org.mybatis:mybatis</p>
 *
 * <pre>{@code
 * <?xml version="1.0" encoding="utf-8"?>
 * <!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
 * <configuration>
 *  <plugins>
 *      <plugin interceptor="sviolet.slate.common.helper.mybatis.monitor.MybatisTxTimerPlugin">
 *          <property name="groupName" value="MyBatis"/>
 *      </plugin>
 *  </plugins>
 * </configuration>
 * }</pre>
 *
 * @author S.Violet
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class}),
})
public class MybatisTxTimerPlugin implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MybatisTxTimerPlugin.class);

    private String groupName;

    public MybatisTxTimerPlugin() {
        this("Mybatis");
    }

    public MybatisTxTimerPlugin(String groupName) {
        this.groupName = groupName;
        logger.info("TxTimer | MyBatis monitor enabled");
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String id;
        try {
            id = ((MappedStatement)invocation.getArgs()[0]).getId();
        } catch (Exception ignore) {
            return invocation.proceed();
        }
        try (TimerContext timerContext = TxTimer.entry(groupName, id)) {
            return invocation.proceed();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        groupName = properties.getProperty("groupName", "MyBatis");
    }

}
