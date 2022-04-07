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

package sviolet.slate.common.x.net.loadbalance.inspector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.net.loadbalance.LoadBalanceInspector;
import sviolet.thistle.util.net.NetworkUtils;

import java.net.URI;

/**
 * 负载均衡--模拟TELNET方式探测网络状况
 *
 * @author S.Violet
 */
public class TelnetLoadBalanceInspector implements LoadBalanceInspector {

    private static final String HTTPS_SCHEME = "https";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean inspect(String url, long timeout, boolean verboseLog) {
        try {
            //解析url
            URI uri = URI.create(url);
            //处理端口
            int port = uri.getPort();
            if (port < 0){
                if (HTTPS_SCHEME.equals(uri.getScheme())){
                    port = 443;
                } else {
                    port = 80;
                }
            }
            //telnet
            return inspectByTelnet(uri.getHost(), port, timeout);
        } catch (Exception e) {
            if (logger.isErrorEnabled()){
                logger.error("Inspect: invalid url " + url, e);
            }
        }
        return false;
    }

    protected boolean inspectByTelnet(String hostname, int ip, long timeout){
        return NetworkUtils.telnet(hostname, ip, timeout > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) timeout);
    }

    @Override
    public String toString() {
        return "TelnetLoadBalanceInspector{}";
    }
}
