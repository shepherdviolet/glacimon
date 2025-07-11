/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.inspector;

import com.github.shepherdviolet.glacimon.java.net.NetworkUtils;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.LoadBalanceInspector;
import com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.GlaciHttpClient;
import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 负载均衡--模拟TELNET方式探测网络状况
 *
 * @author shepherdviolet
 */
public class TelnetLoadBalanceInspector implements LoadBalanceInspector {

    private static final String HTTPS_SCHEME = "https";
    private static final long DEFAULT_TIMEOUT = 2500L;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final GlaciHttpClient.Settings settings;
    private long timeout = DEFAULT_TIMEOUT;

    public TelnetLoadBalanceInspector(GlaciHttpClient.Settings settings) {
        this.settings = settings;
    }

    @Override
    public boolean inspect(String url) {
        try {
            //解析url
            URI uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("scheme or host is missing");
            }
            //ip/域名
            String host = uri.getHost();
            //端口
            int port = uri.getPort();
            if (port < 0) {
                port = HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }
            // 判断是否是域名，如果是则用 Dns 解析
            Dns dns = getDns();
            boolean isTraceEnabled = logger.isTraceEnabled();
            if (dns != null && !isIpAddress(host)) {
                try {
                    List<InetAddress> addresses = dns.lookup(host);
                    if (addresses.isEmpty()) {
                        if (isTraceEnabled) {
                            logger.trace("Inspect: DNS resolved no IPs for host " + host);
                        }
                        return false;
                    }
                    int to = (int) timeout / addresses.size();
                    int count = 0;
                    for (InetAddress address : addresses) {
                        count++;
                        if (NetworkUtils.telnet(address, port, to)) {
                            if (isTraceEnabled) {
                                logger.trace("Inspect: Address " + address.getHostAddress() + " is available, after " + count + " attempts.");
                            }
                            return true;
                        }
                    }
                    if (isTraceEnabled) {
                        logger.trace("Inspect: Host " + host + " is not available, after " + count + " attempts.");
                    }
                    return false;
                } catch (UnknownHostException e) {
                    if (isTraceEnabled) {
                        logger.trace("Inspect: DNS resolve failed for host " + host, e);
                    }
                    return false;
                }
            }
            //telnet
            return NetworkUtils.telnet(host, port, (int) timeout);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Inspect: Invalid url " + url, e);
            }
        }
        return false;
    }

    protected Dns getDns() {
        if (settings == null) {
            return null;
        }
        return settings.getDns();
    }

    private boolean isIpAddress(String host) {
        return host.matches("^(\\d{1,3}\\.){3}\\d{1,3}$") || // IPv4
                host.matches("^\\[[0-9a-fA-F:]+\\]$") ||      // Ipv6 (RFC 2732)
                host.matches("^[0-9a-fA-F:]+$");              // Ipv6 (raw, no brackets)
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void refreshSettings() {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public String toString() {
        return "TelnetLoadBalanceInspector{}";
    }
}
