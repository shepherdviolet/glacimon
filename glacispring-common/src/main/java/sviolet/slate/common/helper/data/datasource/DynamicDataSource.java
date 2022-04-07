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

package sviolet.slate.common.helper.data.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>动态数据源, 依赖org.springframework:spring-jdbc</p>
 *
 * <p>配置动态数据源, 并用@Primary指定为首选(防止因为MybatisAutoConfiguration不生效报sqlSessionFactory不存在的错).
 * SpringBoot中, 不要在YAML中配置spring.datasource配置了. </p>
 *
 * <pre>
 *    <code>@Bean</code>
 *    <code>@Primary</code>
 *    public DataSource dataSource(){
 *        return new DynamicDataSource("defaultDataSource");
 *    }
 * </pre>
 *
 * <p>然后配置其他数据源</p>
 *
 * <pre>
 *     <code>@Bean("dataSource1")</code>
 *     public DataSource dataSource1(){
 *         //H2数据源
 *         return new EmbeddedDatabaseBuilder()
 *                 .setType(EmbeddedDatabaseType.H2)
 *                 .setName("dataSource2")
 *                 .addScripts(
 *                         "classpath:config/demo/mybatis/h2init/schema.sql",
 *                         "classpath:config/demo/mybatis/h2init/data.sql"
 *                 )
 *                 .build();
 *     }
 * </pre>
 *
 * <pre>
 *     <code>@Bean("dataSource2")</code>
 *     public DataSource dataSource2(){
 *         //TOMCAT数据源
 *         PoolProperties configuration = new PoolProperties();
 *         configuration.setDriverClassName("oracle.jdbc.driver.OracleDriver");
 *         configuration.setUrl("jdbc:oracle:thin:@127.0.0.1:1521:ORCL");
 *         configuration.setUsername("sviolet");
 *         configuration.setPassword("sviolet");
 *         configuration.setInitialSize(5);
 *         configuration.setMinIdle(5);
 *         configuration.setMaxIdle(50);
 *         configuration.setMaxActive(50);
 *         configuration.setMaxWait(10000);
 *         configuration.setTimeBetweenEvictionRunsMillis(30000);
 *         configuration.setMinEvictableIdleTimeMillis(1800000);
 *         configuration.setValidationQuery("select 1 from dual");
 *         configuration.setTestWhileIdle(true);
 *         configuration.setTestOnBorrow(true);
 *
 *         org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
 *         dataSource.setPoolProperties(configuration);
 *         return dataSource;
 *     }
 * </pre>
 *
 * <p>最后, 只要在每次数据库调用前, 设置数据源名称即可, 设置以后当前线程就会用指定数据源操作数据库(无数次)</p>
 *
 * <pre>
 *     DynamicDataSource.selectDataSource("dataSource2");
 * </pre>
 *
 * @author S.Violet
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> dataSourceName = new ThreadLocal<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String defaultDataSourceName;

    /**
     * 在操作数据库前调用该方法选择数据源, 设置以后当前线程就会用指定数据源操作数据库(无数次)
     * @param dataSourceName 数据源名称(BeanName)
     */
    public static void selectDataSource(String dataSourceName){
        DynamicDataSource.dataSourceName.set(dataSourceName);
    }

    /**
     * 动态数据源
     * @param defaultDataSourceName 默认数据源名称
     */
    public DynamicDataSource(String defaultDataSourceName) {
        this.defaultDataSourceName = defaultDataSourceName;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String name = dataSourceName.get();
        if (name == null) {
            return defaultDataSourceName;
        }
        if (logger.isInfoEnabled()) {
            logger.info("DynamicDataSource select datasource:" + name);
        }
        return name;
    }

    /**
     * 注入全部数据源
     */
    @Autowired
    public void setDataSources(ObjectProvider<Map<String, DataSource>> dataSourceProvider){
        Map<String, DataSource> dataSources = dataSourceProvider.getIfAvailable();
        if (dataSources == null || dataSources.size() <= 0) {
            return;
        }
        //筛选数据源, 不要AbstractRoutingDataSource
        Map<Object, Object> otherDataSources = new HashMap<>(16);
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            if (entry.getValue() instanceof AbstractRoutingDataSource) {
                continue;
            }
            otherDataSources.put(entry.getKey(), entry.getValue());
        }
        //设置数据源
        setTargetDataSources(otherDataSources);
    }

}
