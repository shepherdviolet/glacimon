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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.net;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 网络工具
 *
 * @author S.Violet
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

    private static final LocalIpFilter DEFAULT_LOCAL_IP_FILTER = new LocalIpFilter() {
        @Override
        public boolean filter(NetworkInterface networkInterface, InetAddress inetAddress) throws SocketException {
            return inetAddress != null && !inetAddress.isLoopbackAddress() && (networkInterface.isPointToPoint() || !inetAddress.isLinkLocalAddress());
        }
    };

    /**
     * 本地IP过滤规则
     */
    public interface LocalIpFilter {
        boolean filter(NetworkInterface networkInterface, InetAddress inetAddress) throws SocketException;
    }

    //获取本机IP精简版
//    private String getFirstLocalIp() throws SocketException {
//        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//        if (networkInterfaces == null) {
//            return null;
//        }
//        while (networkInterfaces.hasMoreElements()) {
//            NetworkInterface networkInterface = networkInterfaces.nextElement();
//            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
//            while (addresses.hasMoreElements()) {
//                InetAddress address = addresses.nextElement();
//                if (address != null && !address.isLoopbackAddress() && (networkInterface.isPointToPoint() || !address.isLinkLocalAddress())) {
//                    return address.getHostAddress();
//                }
//            }
//        }
//        return null;
//    }

}
