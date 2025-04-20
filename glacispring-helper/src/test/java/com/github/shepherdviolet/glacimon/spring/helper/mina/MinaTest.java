package com.github.shepherdviolet.glacimon.spring.helper.mina;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class MinaTest {

    public static void main(String[] args) throws Exception {

        MinaShortTcpServer server = new MinaShortTcpServer(1990, 0, 10, new MinaShortTcpServer.Processor() {
            @Override
            public byte[] process(byte[] request, SocketAddress remoteAddr) throws Exception {
                System.out.println("Downstream addr: " + ((InetSocketAddress) remoteAddr).getHostString());
                System.out.println("Received: " + new String(request));
                return "WORLD".getBytes();
            }
        });
        server.start();

        MinaShortTcpClient client = new MinaShortTcpClient("127.0.0.1", 1990, 3000, 10000, 10000);
        byte[] res = client.send("hello".getBytes());
        System.out.println("Response: " + new String(res));

        server.close();
        client.close();
    }

}
