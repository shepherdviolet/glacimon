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

package sviolet.slate.common.x.net.loadbalance.springboot;

import sviolet.slate.common.x.net.loadbalance.classic.SimpleOkHttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>HttpClients配置: 自动配置SimpleOkHttpClient</p>
 * <p>配置前缀: slate.httpclients</p>
 */
public interface HttpClients {

    String HTTP_CLIENTS_NAME = "slate.httpclient.httpClients";
    String DATA_CONVERTER_NAME = "slate.httpclient.dataConverter";

    /**
     * 获取Http请求客户端
     * @param tag tag(标识)
     * @return SimpleOkHttpClient, 若不存在则返回空
     */
    SimpleOkHttpClient get(String tag);

    /**
     * @return 返回集合中的客户端数量
     */
    int size();

    /**
     * @return 返回集合中所有客户端的tag
     */
    Set<String> tags();

    /**
     * <p>[高级] 运行时修改客户端配置, 动态添加客户端</p>
     *
     * <p>Spring启动后, 会先根据YML配置和启动参数创建HttpClient. 若需要在运行时, 调整这些HttpClient的配置, 甚至新增HttpClient,
     * 就需要用到这个方法. 该方法接收一个覆盖配置(OverrideSettings), 覆盖配置指定了哪些配置需要修改成什么值, 程序会根据覆盖配置
     * 对HttpClient进行调整, 甚至新增.</p>
     */
    void settingsOverride(OverrideSettings overrideSettings);

    /**
     * <p>覆盖配置(接口)</p>
     *
     * <p>示例: 使用Apollo配置中心动态调整配置</p>
     * <pre>
     *  <code>@Configuration</code>
     *  public class HttpClientsApolloConfig {
     *
     *      private HttpClients httpClients;
     *
     *      //构造注入HttpClients
     *      <code>@Autowired</code>
     *      public HttpClientsApolloConfig(HttpClients httpClients) {
     *          this.httpClients = httpClients;
     *      }
     *
     *      //获取Apollo配置, 注意要指定正确的namespace
     *      <code>@ApolloConfig("application")</code>
     *      private Config config;
     *
     *      //监听Apollo配置变化, 注意要指定正确的namespace
     *      <code>@ApolloConfigChangeListener("application")</code>
     *      private void onApolloConfigChanged(ConfigChangeEvent configChangeEvent){
     *          httpClients.settingsOverride(new ApolloOverrideSettings(config));
     *      }
     *
     *      //将Apollo的配置包装为OverrideSettings
     *      private static class ApolloOverrideSettings implements HttpClients.OverrideSettings {
     *
     *          private Config config;
     *
     *          private ApolloOverrideSettings(Config config) {
     *              this.config = config;
     *          }
     *
     *          <code>@Override</code>
     *          public Set<String> getKeys() {
     *              return config.getPropertyNames();
     *          }
     *
     *          <code>@Override</code>
     *          public String getValue(String key) {
     *              return config.getProperty(key, null);
     *          }
     *
     *      }
     *
     *  }
     * </pre>
     */
    interface OverrideSettings {

        /**
         * <p>返回需要调整的配置清单, 格式如下:</p>
         * <p>slate.httpclients.tag.propname</p>
         * <p>其中, tag为HttpClient的标识, propname为配置名称.</p>
         * <p>例如: slate.httpclients.default.hosts 表示调整default客户端的后端列表, 若不存在default客户端, 则会创建一个新的</p>
         */
        Set<String> getKeys();

        /**
         * <p>根据key返回配置的新值</p>
         * @param key 对应getKeys方法返回的值
         * @return key对应的新值
         */
        String getValue(String key);

    }

    /**
     * 基于Map实现的覆盖配置
     */
    class MapBasedOverrideSettings implements OverrideSettings {

        private Map<String, String> map;

        /**
         * 覆盖配置会从指定的map中获取并返回
         */
        public MapBasedOverrideSettings(Map<String, String> map) {
            if (map == null) {
                map = new HashMap<>(0);
            }
            this.map = map;
        }

        @Override
        public Set<String> getKeys() {
            return map.keySet();
        }

        @Override
        public String getValue(String key) {
            return map.get(key);
        }

    }

}
