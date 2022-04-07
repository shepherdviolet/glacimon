/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.slate.common.x.conversion.mapxbean.strategy;

import sviolet.slate.common.x.conversion.mapxbean.BeanToMapInflateStrategy;
import sviolet.slate.common.x.conversion.mapxbean.ConversionPath;
import sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudger;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>[Inflate Strategy] [For Bean -> Map] Converting a 'Java Bean' to 'Map consisting of Map and Collection nesting',
 * all the properties or elements will be inflate until indivisible, or reaches the specified depth, or meets the specified
 * classes. </p><br>
 *
 * <p>NOTE that the property must be a Java Bean, because the program has already determined whether it is a JavaBean
 * before calling this interface. (Judged by built-in component {@link MxbTypeJudger#isBean})</p><br>
 *
 * <p>References:</p><br>
 *
 * <p>In the scene of Bean -> Map. While a Bean is converting to a Map, all the properties of Bean will keep the
 * original type by default, unless {@link BeanToMapInflateStrategy} tells the program that it needs to be inflated
 * (this method returns true).
 * 'Inflate' means that in the scene of Bean -> Map, if a property (of Java Bean) or an element (of Collection) is
 * a Java Bean (judged by {@link MxbTypeJudger#isBean}), the property (or element) can be converted to a Map as long
 * as the method {@link BeanToMapInflateStrategy#needToBeInflated} returns true. The process of converting property
 * (or element) to Map is called 'Inflate'.</p><br>
 *
 * @author S.Violet
 * @see sviolet.slate.common.x.conversion.mapxbean.MapXBean
 * @see BeanToMapInflateStrategy
 */
public class InflateUntilIndivisible implements BeanToMapInflateStrategy {

    private final Set<String> keepTypes;
    private final int maxDepth;

    public InflateUntilIndivisible() {
        this((Set<String>)null, Integer.MAX_VALUE);
    }

    /**
     * @param keepTypes Specify which types (class names) will not be inflated
     */
    public InflateUntilIndivisible(Set<String> keepTypes) {
        this(keepTypes, Integer.MAX_VALUE);
    }

    /**
     * @param maxDepth Continue to inflate until specified depth, Integer.MAX_VALUE by default. Example: maxDepth = 1,
     *                 the properties of root node will be inflated, but the properties of sub nodes will not be
     *                 inflated (NOTE that the property must be a Java Bean). root->**(Inflate these)
     */
    public InflateUntilIndivisible(int maxDepth) {
        this((Set<String>)null, maxDepth);
    }

    /**
     * @param keepTypes Specify which types (class names) will not be inflated
     * @param maxDepth Continue to inflate until specified depth, Integer.MAX_VALUE by default. Example: maxDepth = 1,
     *                 the properties of root node will be inflated, but the properties of sub nodes will not be
     *                 inflated (NOTE that the property must be a Java Bean). root->**(Inflate these)
     */
    public InflateUntilIndivisible(Set<String> keepTypes, int maxDepth) {
        this.keepTypes = keepTypes != null ? keepTypes : new HashSet<>(0);
        this.maxDepth = maxDepth;
    }

    /**
     * <p>Example: example.properties</p>
     * <pre>
     * sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudgerTest$MyIndivisible1
     * sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudgerTest$MyIndivisible2
     * </pre>
     *
     * <p>Example: Load properties file to Properties instance in Spring: </p>
     *
     * <pre>
     *  <code>@Configuration</code>
     *  public class PropertiesConfiguration {
     *      <code>@Bean(name = "myProperties")</code>
     *      public static PropertiesFactoryBean artifactMapping() {
     *          PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
     *          propertiesFactoryBean.setLocations(new ClassPathResource("config/demo/properties/general.properties"));
     *          propertiesFactoryBean.setIgnoreResourceNotFound(true);
     *          propertiesFactoryBean.setFileEncoding("UTF-8");
     *          return propertiesFactoryBean;
     *      }
     *
     *  }
     * </pre>
     *
     * <pre>
     *  <bean id="propertiesReader" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
     *      <property name="locations">
     *          <list>
     *              <value>classpath*:/config/demo/properties/general.properties</value>
     *          </list>
     *      </property>
     *  </bean>
     * </pre>
     *
     * @param keepTypesProperties Specify which types (class names) will not be inflated, set by properties
     */
    public InflateUntilIndivisible(Properties keepTypesProperties) {
        this(keepTypesProperties, Integer.MAX_VALUE);
    }

    /**
     * <p>Example: example.properties</p>
     * <pre>
     * sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudgerTest$MyIndivisible1
     * sviolet.slate.common.x.conversion.mapxbean.MxbTypeJudgerTest$MyIndivisible2
     * </pre>
     *
     * <p>Example: Load properties file to Properties instance in Spring: </p>
     *
     * <pre>
     *  <code>@Configuration</code>
     *  public class PropertiesConfiguration {
     *      <code>@Bean(name = "myProperties")</code>
     *      public static PropertiesFactoryBean artifactMapping() {
     *          PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
     *          propertiesFactoryBean.setLocations(new ClassPathResource("config/demo/properties/general.properties"));
     *          propertiesFactoryBean.setIgnoreResourceNotFound(true);
     *          propertiesFactoryBean.setFileEncoding("UTF-8");
     *          return propertiesFactoryBean;
     *      }
     *
     *  }
     * </pre>
     *
     * <pre>
     *  <bean id="propertiesReader" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
     *      <property name="locations">
     *          <list>
     *              <value>classpath*:/config/demo/properties/general.properties</value>
     *          </list>
     *      </property>
     *  </bean>
     * </pre>
     *
     * @param keepTypesProperties Specify which types (class names) will not be inflated, set by properties
     * @param maxDepth Continue to inflate until specified depth, Integer.MAX_VALUE by default. Example: maxDepth = 1,
     *                 the properties of root node will be inflated, but the properties of sub nodes will not be
     *                 inflated (NOTE that the property must be a Java Bean). root->**(Inflate these)
     */
    public InflateUntilIndivisible(Properties keepTypesProperties, int maxDepth) {
        this(keepTypesProperties != null ?
                        keepTypesProperties.keySet().stream().map(String::valueOf).collect(Collectors.toSet()) :
                        new HashSet<>(0),
                maxDepth);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean needToBeInflated(Object value, Class<?> valueClass, MxbTypeJudger typeJudger, ConversionPath conversionPath) {
        return conversionPath.depth() <= maxDepth && !keepTypes.contains(valueClass.getName());
    }

}
