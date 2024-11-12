/*
 * Copyright (C) 2022-2024 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.basic.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.*;
import org.springframework.context.support.DefaultLifecycleProcessor;

import java.lang.reflect.Field;

/**
 * <p>Springboot优雅停机工具</p>
 *
 * <p>常见的优雅停机流程:</p>
 * <p>1.停止接收新请求.</p>
 * <p>2.检查并等待在途请求处理完成.</p>
 *
 * <p>Springboot2.3以上(含)自带优雅停机, 可以通过简单的配置实现WEB服务的优雅停机(仅限HTTP请求): </p>
 *
 * <pre>
 * spring:
 *   lifecycle:
 *     timeout-per-shutdown-phase: 60s
 * server:
 *   shutdown: graceful
 * </pre>
 *
 * <p>如果你需要实现自己的优雅停机逻辑, 或者你的springboot版本低于2.3(不含), 可以使用本工具. </p>
 *
 * <pre>
 * <code></code>@Value("${graceful-shutdown.timeout:60000}")
 * private long timeout;
 *
 * // 继承GracefulShutdown实现抽象方法(详见抽象方法上的注释), 声明为SpringBean即可
 * <code></code>@Bean
 * <code></code>@ConditionalOnProperty(value = "graceful-shutdown.enabled", havingValue = "true")
 * public MyGracefulShutdown myGracefulShutdown() {
 *     return new MyGracefulShutdown(timeout);
 * }
 * </pre>
 *
 * <p>其它说明: SmartLifecycle默认由DefaultLifecycleProcessor管理, 它有一个超时参数, 每一个SmartLifecycle执行shutdown phase是有
 * 时间限制的, 超时会强制结束 (从调用SmartLifecycle#stop(Runnable callback)方法到callback被调用为止). Springboot2.3以上(含)版本中
 * 允许通过如下参数配置, 低于2.3(不含)版本中, 无法通过参数配置. 因此, 本GracefulShutdown提供了一种覆盖DefaultLifecycleProcessor超时
 * 时间的机制, 详见overwriteTimeout方法, 保证DefaultLifecycleProcessor的超时时间始终大于SmartLifecycle的超时时间. </p>
 *
 * <pre>
 * spring:
 *   lifecycle:
 *     timeout-per-shutdown-phase: 60s
 * </pre>
 *
 * @author shepherdviolet
 */
public abstract class GracefulShutdown implements SmartLifecycle, ApplicationContextAware {
    
    private static final long BASE_TIMEOUT = 3000L;
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationContext applicationContext;
    private long timeout = 30000L - BASE_TIMEOUT;
    private long checkInterval = 500L;

    private volatile boolean running;

    public GracefulShutdown() {
    }

    /**
     * @param timeout 等待在途请求完成的超时时间, GracefulShutdown会自动覆盖DefaultLifecycleProcessor的timeoutPerShutdownPhase,
     *                使得本GracefulShutdown设置的超时时间始终小于timeoutPerShutdownPhase.
     */
    public GracefulShutdown(long timeout) {
        setTimeout(timeout);
    }

    /**
     * DefaultLifecycleProcessor在获取SmartLifecycle实例后, 会先调用getPhase获取优先级, 我们利用这个改写DefaultLifecycleProcessor
     * 的timeoutPerShutdownPhase, 使得本GracefulShutdown设置的超时时间始终小于timeoutPerShutdownPhase.
     */
    protected void overwriteTimeout() {
        // 获取LifecycleProcessor
        LifecycleProcessor lifecycleProcessor = applicationContext.getBean(LifecycleProcessor.class);
        // 如果不是DefaultLifecycleProcessor, 则无法覆盖timeoutPerShutdownPhase
        if (!(lifecycleProcessor instanceof DefaultLifecycleProcessor)) {
            /*
             * 程序自定义了LifecycleProcessor, 导致本GracefulShutdown无法覆盖timeoutPerShutdownPhase.
             * 如果自定义的LifecycleProcessor的超时时间小于本GracefulShutdown的超时时间, 等待过程会被提早中止.
             */
            logger.warn("Unable to overwrite the 'timeoutPerShutdownPhase' of DefaultLifecycleProcessor, the JVM " +
                    "process may terminate earlier than you expect. Because the LifecycleProcessor in the Spring " +
                    "context is not an instance of DefaultLifecycleProcessor");
            return;
        }
        // 获取原先的timeoutPerShutdownPhase
        long timeoutPerShutdownPhase;
        try {
            Field timeoutPerShutdownPhaseField = DefaultLifecycleProcessor.class.getDeclaredField("timeoutPerShutdownPhase");
            timeoutPerShutdownPhaseField.setAccessible(true);
            timeoutPerShutdownPhase = timeoutPerShutdownPhaseField.getLong(lifecycleProcessor);
        } catch (Exception e) {
            /*
             * DefaultLifecycleProcessor原先的timeoutPerShutdownPhase无法获取.
             * 如果自定义的LifecycleProcessor的超时时间小于本GracefulShutdown的超时时间, 等待过程会被提早中止.
             */
            logger.warn("Unable to overwrite the 'timeoutPerShutdownPhase' of DefaultLifecycleProcessor, the JVM " +
                    "process may terminate earlier than you expect. Because the original 'timeoutPerShutdownPhase' " +
                    "cannot be obtained from the DefaultLifecycleProcessor");
            return;
        }
        // 如果原先的timeoutPerShutdownPhase比较大, 则无需覆盖
        if (timeoutPerShutdownPhase > timeout + BASE_TIMEOUT) {
            return;
        }
        // 覆盖timeoutPerShutdownPhase为本GracefulShutdown超时时间加3秒
        ((DefaultLifecycleProcessor)lifecycleProcessor).setTimeoutPerShutdownPhase(timeout + BASE_TIMEOUT);
        logger.info("Overwrite the 'timeoutPerShutdownPhase' of DefaultLifecycleProcessor to " + (timeout + BASE_TIMEOUT) + "ms");
    }

    /**
     * <p>1.停止接收新请求.</p>
     * <p>2.检查并等待在途请求处理完成.</p>
     */
    public void onShutdownPhase() {
        // 超时时间包括stopAcceptingRequests
        long startTime = System.currentTimeMillis();
        try {
            stopAcceptingRequests();
            while (System.currentTimeMillis() - startTime < timeout) {
                if (isAllRequestsCompleted()) {
                    logger.info("All requests completed, graceful shutdown");
                    return;
                }
                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException ignore) {
                }
            }
            logger.info("There are still unfinished requests, forced shutdown");
        } catch (Exception e) {
            logger.error("Unable to wait for all requests to complete", e);
        }
    }

    /**
     * <p>实现"停止接收新请求"逻辑, 例如:</p>
     *
     * <pre>
     *  public void stopAcceptingRequests() {
     *      // 示例参考自springboot的GracefulShutdown类
     *      // 停止接收请求
     *      List<Connector> connectors = getConnectors();
     *      connectors.forEach(this::close);
     *  }
     *
     *  private List<Connector> getConnectors() {
     * 	    List<Connector> connectors = new ArrayList<>();
     * 	    for (Service service : this.tomcat.getServer().findServices()) {
     * 		    Collections.addAll(connectors, service.findConnectors());
     * 		}
     * 		return connectors;
     *  }
     *
     *  private void close(Connector connector) {
     * 	    connector.pause();
     * 	    connector.getProtocolHandler().closeServerSocketGraceful();
     *  }
     * </pre>
     */
    public abstract void stopAcceptingRequests();

    /**
     * <p>实现"检查在途请求处理完成"逻辑, 返回true表示在途请求处理完成, 可以停机; 返回false表示在途请求未完成, 继续等待. 例如:</p>
     *
     * <pre>
     *  public boolean isAllRequestsCompleted() {
     *      // 示例参考自springboot的GracefulShutdown类
     *      // 停止接收请求
     *      for (Container host : this.tomcat.getEngine().findChildren()) {
     * 		    for (Container context : host.findChildren()) {
     * 		        if (isActive(context)) {
     * 		            // 还有在途请求
     * 		            return false;
     * 		        }
     *          }
     *      }
     *      // 无在途请求
     *      return true;
     *  }
     *
     *  private boolean isActive(Container context) {
     * 	    try {
     * 		    if (((StandardContext) context).getInProgressAsyncCount() > 0) {
     * 			    return true;
     * 			}
     * 			for (Container wrapper : context.findChildren()) {
     * 			    if (((StandardWrapper) wrapper).getCountAllocated() > 0) {
     * 				    return true;
     *              }
     *          }
     * 			return false;
     * 		} catch (Exception ex) {
     * 		    throw new RuntimeException(ex);
     *      }
     *  }
     * </pre>
     *
     * @return true: 在途请求处理完成, 可以停机; false: 在途请求未完成, 继续等待.
     */
    public abstract boolean isAllRequestsCompleted();

    /**
     * @param timeout 等待在途请求完成的超时时间, GracefulShutdown会自动覆盖DefaultLifecycleProcessor的timeoutPerShutdownPhase,
     *                使得本GracefulShutdown设置的超时时间始终小于timeoutPerShutdownPhase.
     */
    public final void setTimeout(long timeout) {
        if (timeout < 10) {
            timeout = 10L;
        }
        this.timeout = timeout;
    }

    /**
     * @param checkInterval 调用isAllRequestsCompleted检查在途请求是否完成的间隔
     */
    public final void setCheckInterval(long checkInterval) {
        if (checkInterval < 10L) {
            checkInterval = 10L;
        }
        this.checkInterval = checkInterval;
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        logger.info("GracefulShutdown component '" + getClass().getSimpleName() + "' enabled, timeout: " + timeout + ", checkInterval: " + checkInterval);
    }
    
    // Override SmartLifecycle //////////////////////////////////////////////////////////////////////////////////////

    /**
     * DefaultLifecycleProcessor在获取SmartLifecycle实例后, 会先调用getPhase获取优先级, 我们利用这个改写DefaultLifecycleProcessor
     * 的timeoutPerShutdownPhase, 使得本GracefulShutdown设置的超时时间始终小于timeoutPerShutdownPhase.
     */
    @Override
    public final int getPhase() {
        overwriteTimeout();
        return Integer.MAX_VALUE;
    }

    @Override
    public final void start() {
        this.running = true;
    }

    @Override
    public final void stop() {
        stop(() -> {});
    }

    /**
     * 处理停机阶段
     */
    @Override
    @SuppressWarnings({"NullableProblems", "ConstantValue"})
    public final void stop(Runnable callback) {
        this.running = false;
        this.logger.info("Commencing graceful shutdown. Waiting for requests to complete");
        // 启动一个线程处理拦截逻辑 (非daemon线程存在时, JVM不会退出)
        (new Thread(() -> {
            try {
                // 处理停机拦截逻辑 (停止接受新请求/等待在途请求完成)
                onShutdownPhase();
            } finally {
                // 执行回调 (否则拦截逻辑结束后, spring不会继续停机流程)
                if (callback != null) {
                    callback.run();
                }
            }
        }, "GracefulShutdown")).start();
    }

    @Override
    public final boolean isRunning() {
        return running;
    }

    @Override
    @SuppressWarnings("RedundantMethodOverride")
    public final boolean isAutoStartup() {
        return true;
    }

}