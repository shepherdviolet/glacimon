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

package com.github.shepherdviolet.glacimon.java.net;

import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * 网络工具
 *
 * @author shepherdviolet
 */
public class NetworkUtils {

    /**
     * 模拟TELNET
     * @param hostname IP或域名, 例如: 61.135.169.125或www.baidu.com
     * @param port 端口
     * @param timeout 探测超时ms
     * @return true:成功 false:失败
     */
    public static boolean telnet(String hostname, int port, int timeout) {
        Socket socket = null;
        try {
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            socket.connect(address, timeout);
            return true;
        } catch (Throwable ignore) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 模拟TELNET
     * @param host InetAddress
     * @param port 端口
     * @param timeout 探测超时ms
     * @return true:成功 false:失败
     */
    public static boolean telnet(InetAddress host, int port, int timeout) {
        Socket socket = null;
        try {
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, timeout);
            return true;
        } catch (Throwable ignore) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 判断当前环境是否支持ipv6
     * @return true: 支持
     */
    public static boolean isIpv6Available() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (!nif.isUp() || nif.isLoopback() || nif.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address && !addr.isLoopbackAddress()) {
                        return true;
                    }
                }
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    /**
     * 获取第一个本地IP
     *
     * @return 第一个本地IP
     * @throws SocketException 异常
     */
    public static InetAddress getFirstLocalIp() throws SocketException {
        return getFirstLocalIp(null);
    }

    /**
     * 获取第一个本地IP
     *
     * @param localIpFilter IP过滤规则
     * @return 第一个本地IP
     * @throws SocketException 异常
     */
    public static InetAddress getFirstLocalIp(LocalIpFilter localIpFilter) throws SocketException {
        if (localIpFilter == null) {
            localIpFilter = DEFAULT_LOCAL_IP_FILTER;
        }
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces == null) {
            return null;
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (localIpFilter.filter(networkInterface, address)) {
                    return address;
                }
            }
        }
        return null;
    }

    /**
     * 获取本地设备IP
     *
     * @return 本地IP清单
     * @throws SocketException 异常
     */
    public static List<InetAddress> getLocalIps() throws SocketException {
        return getLocalIps(null);
    }

    /**
     * 获取本地设备IP
     *
     * @param localIpFilter IP过滤规则
     * @return 本地IP清单
     * @throws SocketException 异常
     */
    public static List<InetAddress> getLocalIps(LocalIpFilter localIpFilter) throws SocketException {
        if (localIpFilter == null) {
            localIpFilter = DEFAULT_LOCAL_IP_FILTER;
        }
        List<InetAddress> list = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces == null) {
            return list;
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (localIpFilter.filter(networkInterface, address)) {
                    list.add(address);
                }
            }
        }
        return list;
    }

    private static final LocalIpFilter DEFAULT_LOCAL_IP_FILTER = new DefaultLocalIpFilter();

    /**
     * 默认本地IP过滤规则
     */
    private static class DefaultLocalIpFilter implements LocalIpFilter {

        private static final String IGNORED_INTERFACE = "glacimon.networkutils.ignored.interface";
        private static final String IGNORED_ADDRESS = "glacimon.networkutils.ignored.address";
        private static final String IGNORED_ADDRESS_PREFIX = "glacimon.networkutils.ignored.address.prefix";

        private static final Set<String> IGNORED_INTERFACE_SET = getPropertySet(IGNORED_INTERFACE);
        private static final Set<String> IGNORED_ADDRESS_SET = getPropertySet(IGNORED_ADDRESS);
        private static final Set<String> IGNORED_ADDRESS_PREFIX_SET = getPropertySet(IGNORED_ADDRESS_PREFIX);

        @Override
        public boolean filter(NetworkInterface networkInterface, InetAddress inetAddress) throws SocketException {
            if (!networkInterface.isUp() || // 网卡禁用
                    networkInterface.isLoopback() || // 环回地址
                    networkInterface.isVirtual() || // 虚拟网卡
                    inetAddress == null || // 地址为空
                    inetAddress.isLoopbackAddress()){ // 环回地址
                return false;
            }

            // 局域网地址(169.254.0.0/16), 且不是点到点网络(例如拨号器)
            if (inetAddress.isLinkLocalAddress() && !networkInterface.isPointToPoint()) {
                return false;
            }

            // 根据网卡名忽略(lo eth0 wlan0 ...)
            if (IGNORED_INTERFACE_SET != null && IGNORED_INTERFACE_SET.contains(networkInterface.getName())) {
                return false;
            }

            String hostAddress = inetAddress.getHostAddress();

            // 根据IP地址忽略
            if (IGNORED_ADDRESS_SET != null && IGNORED_ADDRESS_SET.contains(hostAddress)) {
                return false;
            }

            // 根据IP地址前缀忽略
            if (IGNORED_ADDRESS_PREFIX_SET != null) {
                for (String ignoredPrefix : IGNORED_ADDRESS_PREFIX_SET) {
                    if (hostAddress.startsWith(ignoredPrefix)) {
                        return false;
                    }
                }
            }

            return true;
        }

        private static Set<String> getPropertySet(String propertyKey) {
            Set<String> set = null;
            String propertyValue = System.getProperty(propertyKey);
            if (CheckUtils.notEmptyNotBlank(propertyValue)) {
                for (String subValue : propertyValue.split(",")) {
                    if (set == null) {
                        set = new HashSet<>();
                    }
                    set.add(subValue.trim());
                }
            }
            return set;
        }

    };

    /**
     * 本地IP过滤规则
     */
    public interface LocalIpFilter {
        boolean filter(NetworkInterface networkInterface, InetAddress inetAddress) throws SocketException;
    }

}
