package com.github.shepherdviolet.glacimon.spring.helper.mina;

import com.github.shepherdviolet.glacimon.java.concurrent.AsyncWaiter;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

/**
 * 基于 Apache MINA 实现的 TCP 短连接客户端。
 */
public class MinaShortTcpClient implements InitializingBean, DisposableBean, AutoCloseable {

    private final String host;
    private final int port;
    private final int connectTimeout;
    private final int writeTimeout;
    private final int readTimeout;

    private volatile NioSocketConnector connector;

    /**
     * @param host                 服务器主机名或 IP
     * @param port                 服务器端口
     * @param connectTimeoutMillis 连接超时时间（毫秒）
     * @param writeTimeoutMillis   写请求超时时间（毫秒）
     * @param readTimeoutMillis    读取响应超时时间（毫秒）
     */
    public MinaShortTcpClient(String host, int port, int connectTimeoutMillis, int writeTimeoutMillis, int readTimeoutMillis) {
        this.host = host;
        this.port = port;
        this.connectTimeout = connectTimeoutMillis;
        this.writeTimeout = writeTimeoutMillis;
        this.readTimeout = readTimeoutMillis;

        if (CheckUtils.isEmptyOrBlank(host)) {
            throw new IllegalArgumentException("host is empty");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port is out of range");
        }
        if (connectTimeoutMillis <= 0) {
            throw new IllegalArgumentException("connectTimeout <= 0");
        }
        if (writeTimeoutMillis <= 0) {
            throw new IllegalArgumentException("writeTimeout <= 0");
        }
        if (readTimeoutMillis <= 0) {
            throw new IllegalArgumentException("readTimeout <= 0");
        }
    }

    /**
     * 发送请求并接收响应（同步阻塞模式，双工短连接）
     *
     * @param data 要发送的数据字节数组
     * @return 服务端返回的字节数组
     * @throws ConnectException     连接失败(未开始发送数据)
     * @throws IOException          发生其他网络或IO错误
     */
    public byte[] sendDuplex(byte[] data) throws ConnectException, IOException {
        return send(data, true);
    }

    /**
     * 发送请求（同步阻塞模式，单工短连接）
     *
     * @param data 要发送的数据字节数组
     * @throws ConnectException     连接失败(未开始发送数据)
     * @throws IOException          发生其他网络或IO错误
     */
    public void sendSimplex(byte[] data) throws ConnectException, IOException {
        send(data, false);
    }

    private byte[] send(byte[] data, boolean isDuplex) throws ConnectException, IOException {
        try {
            // 启动客户端 (第一次)
            start();

            // 响应等待器
            final AsyncWaiter<byte[]> responseWaiter;
            if (isDuplex) {
                // 双工
                responseWaiter = new AsyncWaiter<>(readTimeout);
            } else {
                responseWaiter = null;
            }

            // 连接
            ConnectFuture connectFuture;
            try {
                connectFuture = connector.connect(new InetSocketAddress(host, port), null, (session, f) -> {
                    if (isDuplex) {
                        // 双工
                        session.setAttribute("ResponseWaiter", responseWaiter);
                    }
                });
                connectFuture.awaitUninterruptibly();
                // 检查连接是否成功
                if (!connectFuture.isConnected()) {
                    Throwable exception = connectFuture.getException();
                    if (exception != null) {
                        throw exception;
                    }
                    throw new ConnectException("Unable to connect to server: " + host + ":" + port);
                }
            } catch (Throwable exception) {
                if (exception instanceof ConnectException) {
                    throw (ConnectException) exception;
                } else {
                    ConnectException connectException = new ConnectException("Unable to connect to server: " + host + ":" + port);
                    connectException.initCause(exception);
                    throw connectException;
                }
            }

            // 写 & 读
            IoSession session = connectFuture.getSession();
            try {
                // 写入
                WriteFuture writeFuture = session.write(data);
                writeFuture.awaitUninterruptibly(writeTimeout);
                // 等待读取
                if (isDuplex) {
                    switch (responseWaiter.waitForResult()) {
                        case SUCCESS:
                            return responseWaiter.getValue();
                        case TIMEOUT:
                            throw new SocketTimeoutException("Read response timeout");
                        case ERROR:
                        default:
                            throw responseWaiter.getException();
                    }
                } else {
                    return null;
                }
            } finally {
                session.closeNow(); // 关闭会话
            }
        } catch (Throwable t) {
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            throw new IOException(t);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() {
        if (connector == null) {
            synchronized (this) {
                if (connector == null) {
                    connector = new NioSocketConnector();
                    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
                    connector.setConnectTimeoutMillis(connectTimeout);
                    connector.setHandler(new ResponseHandler());
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        close();
    }

    @Override
    public void close() throws Exception {
        try {
            connector.dispose();
        } catch (Throwable ignore) {
        }
    }

    /**
     * 内部静态类：处理服务端响应
     * 使用 CountDownLatch 实现阻塞等待，并在异常时传递到主线程
     */
    @SuppressWarnings("unchecked")
    private static class ResponseHandler extends IoHandlerAdapter {

        /**
         * 接收到消息时回调，仅会收到 byte[] 类型
         */
        @Override
        public void messageReceived(IoSession session, Object message) {
            Object asyncWaiter = session.getAttribute("ResponseWaiter");
            if (asyncWaiter == null) {
                // 单工: 若收到回包, 直接抛弃, 这里不用关闭会话
                return;
            }
            if (asyncWaiter instanceof AsyncWaiter) {
                // 双工回调
                ((AsyncWaiter<byte[]>) asyncWaiter).callback((byte[]) message);
                return;
            }
            // imposable
            session.closeNow();
        }

        /**
         * 捕获异常时回调，保存异常并唤醒等待线程
         */
        @Override
        public void exceptionCaught(IoSession session, Throwable cause) {
            Object asyncWaiter = session.getAttribute("ResponseWaiter");
            if (asyncWaiter instanceof AsyncWaiter) {
                // 双工回调
                ((AsyncWaiter<byte[]>) asyncWaiter).callback((Exception) cause);
            }
            // 都要关闭会话
            session.closeNow();
        }

    }

    /**
     * 只转发 byte[] 的 CodecFactory
     */
    private static class ByteArrayCodecFactory implements ProtocolCodecFactory {
        private final ByteArrayEncoder encoder = new ByteArrayEncoder();
        private final ByteArrayDecoder decoder = new ByteArrayDecoder();

        @Override
        public ProtocolEncoder getEncoder(IoSession session) {
            return encoder;
        }

        @Override
        public ProtocolDecoder getDecoder(IoSession session) {
            return decoder;
        }
    }

    /**
     * 将 byte[] 原封不动写入 IoBuffer
     */
    private static class ByteArrayEncoder implements ProtocolEncoder {
        @Override
        public void encode(IoSession session, Object message, ProtocolEncoderOutput out) {
            byte[] data = (byte[]) message;
            IoBuffer buffer = IoBuffer.allocate(data.length, false);
            buffer.put(data);
            buffer.flip();
            out.write(buffer);
        }

        @Override
        public void dispose(IoSession session) {}
    }

    /**
     * 将 IoBuffer 中剩余内容一次性解析为 byte[]
     */
    private static class ByteArrayDecoder extends CumulativeProtocolDecoder {
        @Override
        protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
            if (in.remaining() > 0) {
                byte[] data = new byte[in.remaining()];
                in.get(data);
                out.write(data);
                return true;
            }
            return false;
        }
    }
}
