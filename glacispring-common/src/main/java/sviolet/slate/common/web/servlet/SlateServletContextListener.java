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

package sviolet.slate.common.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.thistle.util.lifecycle.CloseableManageUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <p>[ServletContextListener]</p>
 *
 * <p>
 *     Slate库统一Servlet监听器<br>
 *     1.销毁所有在CloseableManageUtils注册的Closeable实例<br>
 *     2.销毁所有由ThreadPoolExecutorUtils创建的Executor<br>
 * </p>
 *
 * <p>建议使用本库的Servlet工程, 注册本监听器.</p>
 *
 * <p>Spring MVC: 在web.xml中注册此监听器(listener可以设置多个):</p>
 *
 * <pre>{@code
 *  <web-app ......>
 *      <listener>
 *          <listener-class>sviolet.slate.common.web.servlet.SlateServletContextListener</listener-class>
 *      </listener>
 *      <listener>
 *          ......
 *      </listener>
 *      ......
 *  </web-app>
 * }</pre>
 *
 * <p>Spring Boot: </p>
 *
 * <pre>
 *  <code>@Configuration</code>
 *  public class AppConf {
 *      <code>@Bean</code>
 *      public ServletContextListener slateServletContextListener() {
 *          return new SlateServletContextListener();
 *      }
 *      ......
 *  }
 * }</pre>
 *
 * @author S.Violet
 *
 */
public class SlateServletContextListener implements ServletContextListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("SlateServletContextListener init");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("SlateServletContextListener destroy");
        CloseableManageUtils.closeAll();
        ThreadPoolExecutorUtils.shutdownNowAll();
    }

}