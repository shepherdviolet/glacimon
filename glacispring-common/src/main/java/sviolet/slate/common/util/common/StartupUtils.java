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

package sviolet.slate.common.util.common;

import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Java启动工具
 *
 * @author S.Violet
 */
public class StartupUtils {

    /**
     * 启动非WEB容器的SpringBoot应用
     *
     * <pre>
     *     StartupUtils.startNonWebSpringBootApp(Main.class, args);
     * </pre>
     */
    public static void startNonWebSpringBootApp(Class<?> source, String[] args){
        if (source == null) {
            throw new RuntimeException("source class is null");
        }
        final ShutdownState shutdownState = new ShutdownState();
        final Thread thread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdownState.shutdown = true;
                thread.interrupt();
            }
        }));
        try {
            new SpringApplicationBuilder(source)
                    .web(WebApplicationType.NONE)
                    .run(args);
        } catch (Exception e) {
            LoggerFactory.getLogger(source).error("Error while application starting", e);
            return;
        }
        while (!shutdownState.shutdown) {
            try {
                Thread.sleep(60000L);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class ShutdownState{
        private volatile boolean shutdown = false;
    }

}
