package com.github.shepherdviolet.glacimon.spring.helper.mina;

import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 Apache MINA 实现的 TCP 短连接服务端
 */
public class MinaShortTcpServer implements InitializingBean, DisposableBean, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean isDuplex;
    private final int port;
    private final int corePoolSize;
    private final int maxPoolSize;
    private final Processor processor;
    private long gracefulShutdownTimeout = 60000L;

    private volatile IoAcceptor acceptor;
    private ExecutorService executor;

    private final AtomicInteger inFlightRequests = new AtomicInteger(0);
    private final Object shutdownLock = new Object();

    /**
     * @param isDuplex true: 双工 false: 单工
     * @param port 端口
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param processor 处理器
     */
    public MinaShortTcpServer(boolean isDuplex, int port, int corePoolSize, int maxPoolSize, Processor processor) {
        this.isDuplex = isDuplex;
        this.port = port;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.processor = processor;

        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1024 and 65535");
        }
        if (corePoolSize < 0 || corePoolSize > maxPoolSize) {
            throw new IllegalArgumentException("corePoolSize must be >= 0 and <= maxPoolSize");
        }
        if (maxPoolSize < 1 || maxPoolSize > 4000) {
            throw new IllegalArgumentException("maxPoolSize must be between 1 and 4000");
        }
        if (processor == null) {
            throw new IllegalArgumentException("processor must not be null");
        }
    }

    public void setGracefulShutdownTimeout(long gracefulShutdownTimeout) {
        if (gracefulShutdownTimeout < 0) {
            throw new IllegalArgumentException("gracefulShutdownTimeout must be >= 0");
        }
        this.gracefulShutdownTimeout = gracefulShutdownTimeout;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws Exception {
        if (acceptor == null) {
            synchronized (this) {
                if (acceptor == null) {
                    acceptor = new NioSocketAcceptor();
                    executor = ThreadPoolExecutorUtils.createCached(corePoolSize, maxPoolSize, 60, "MinaShortTcpServer-%s");
                    acceptor.getFilterChain().addLast("executor", new ExecutorFilter(executor));
                    acceptor.setHandler(new ByteArrayAdapter());
                    // 绑定端口开始监听
                    acceptor.bind(new InetSocketAddress(port));
                    logger.info("MinaShortTcpServer started on port '" + port + "', duplex '" + isDuplex +
                            "', corePoolSize '" + corePoolSize + "', maxPoolSize '" + maxPoolSize +
                            "', gracefulShutdownTimeout '" + gracefulShutdownTimeout + "'");
                }
            }
        }
    }

    private class ByteArrayAdapter extends IoHandlerAdapter {

        @Override
        public void messageReceived(IoSession session, Object message) {
            inFlightRequests.incrementAndGet();
            try {
                executor.execute(() -> {
                    try {
                        byte[] request = new byte[((IoBuffer) message).remaining()];
                        ((IoBuffer) message).get(request);
                        // 极端情况下(server关闭后立即创建一个同端口的server), remoteAddr为null, 创建一个0.0.0.0:0给processor
                        SocketAddress remoteAddr = session.getRemoteAddress();
                        byte[] response = processor.process(request, remoteAddr != null ? remoteAddr : new InetSocketAddress("0.0.0.0", 0));
                        if (isDuplex) {
                            WriteFuture writeFuture = session.write(IoBuffer.wrap(response));
                            writeFuture.addListener(f -> {
                                session.closeOnFlush();
                                closeSession(session, null);
                            });
                        } else {
                            closeSession(session, null);
                        }
                    } catch (Throwable t) {
                        closeSession(session, t);
                    }
                });
            } catch (Throwable t) {
                closeSession(session, t);
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            logger.error("MinaShortTcpServer(" + port + "): Error while receiving request", cause);
            session.closeNow();
        }

        private void closeSession(IoSession session, Throwable t) {
            if (t != null) {
                logger.error("MinaShortTcpServer(" + port + "): Error while processing request", t);
            }
            try {
                session.closeNow();
            } finally {
                int left = inFlightRequests.decrementAndGet();
                if (left <= 0) {
                    synchronized (shutdownLock) {
                        shutdownLock.notifyAll();
                    }
                }
            }
        }
    }

    /**
     * 优雅停止：停止接收新连接，等待在途请求完成后销毁。
     */
    @Override
    public void destroy() throws Exception {
        gracefulStop();
    }

    @Override
    public void close() throws Exception {
        gracefulStop();
    }

    public void gracefulStop() {
        if (acceptor == null) {
            logger.warn("MinaShortTcpServer(" + port + "): Server not started yet, skip shutdown");
            return;
        }
        // 解除端口绑定，停止接收新连接
        try {
            acceptor.unbind();
        } catch (Throwable ignore) {
        }
        logger.info("MinaShortTcpServer(" + port + ") unbound, waiting in-flight requests...");
        // 等待所有在途请求完成
        try {
            long waitUntil = System.currentTimeMillis() + gracefulShutdownTimeout;
            synchronized (shutdownLock) {
                while (inFlightRequests.get() > 0 && System.currentTimeMillis() < waitUntil) {
                    shutdownLock.wait(5000L);
                }
            }
        } catch (InterruptedException ignore) {
        }
        if (inFlightRequests.get() == 0) {
            logger.info("MinaShortTcpServer(" + port + "): All in-flight requests completed. Shutting down...");
        } else {
            logger.info("MinaShortTcpServer(" + port + "): Force shutting down ...");
        }
        // 释放 MINA 资源
        try {
            acceptor.dispose();
        } catch (Throwable ignore) {
        }
        // 关闭线程池
        try {
            executor.shutdownNow();
        } catch (Throwable ignore) {
        }
        logger.info("MinaShortTcpServer(" + port + "): Shutdown complete.");
    }

    public int inflightRequests() {
        return inFlightRequests.get();
    }

    /**
     * 业务处理器接口：实现自己的业务流程
     */
    public interface Processor {

        byte[] process(byte[] request, SocketAddress remoteAddr) throws Exception;

    }

}
