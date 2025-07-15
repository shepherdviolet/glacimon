/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.net.loadbalance.classic.dns;

import com.github.shepherdviolet.glacimon.java.concurrent.GuavaThreadFactoryBuilder;
import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;
import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DNS更新器, 在TTL到期前尝试更新, 减少解析阻塞
 */
public class DnsBackgroundUpdater implements Closeable {

    private static final String LOG_PREFIX = "LoadBalance | ";
    private static final long DEFAULT_INTERVAL_SEC = 3600L;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String tag = LOG_PREFIX;

    private long lastDnsReportTime = System.currentTimeMillis();

    private volatile Dns dns;

    private final ExecutorService dispatchThreadPool = ThreadPoolExecutorUtils.createFixed(1,
            new GuavaThreadFactoryBuilder().setNameFormat("Glacispring-LBDNS-Dispatch-%s").setDaemon(true).build());
    private final ExecutorService updateThreadPool = ThreadPoolExecutorUtils.createCached(0,
            Integer.MAX_VALUE, 60, "Glacispring-LBDNS-Update-%s");
    private final Object updateWaitLock = new Object();

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public void start() {
        if (started.compareAndSet(false, true)) {
            //开始探测
            dispatchStart();
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        started.set(true);
        synchronized (updateWaitLock) {
            updateWaitLock.notifyAll();
        }
        try {
            dispatchThreadPool.shutdownNow();
        } catch (Throwable ignore){
        }
        try {
            updateThreadPool.shutdownNow();
        } catch (Throwable ignore){
        }
    }

    public synchronized void setDns(Dns dns) {
        // 清理旧实例
        if (this.dns instanceof BackgroundUpdatingDns) {
            ((BackgroundUpdatingDns) this.dns).setBackgroundUpdateWaitLock(null);
        }
        // 设置新实例
        this.dns = dns;
        if (dns instanceof BackgroundUpdatingDns) {
            ((BackgroundUpdatingDns) dns).setBackgroundUpdateWaitLock(updateWaitLock);
        }
        synchronized (updateWaitLock) {
            updateWaitLock.notifyAll();
        }
    }

    /**
     * 设置客户端的标识
     * @param tag 标识
     */
    public DnsBackgroundUpdater setTag(String tag) {
        this.tag = tag != null ? LOG_PREFIX + tag + "> " : LOG_PREFIX;
        return this;
    }

    private void dispatchStart() {
        dispatchThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (logger.isInfoEnabled()) {
                    logger.info(tag + "CustomDNS: Start");
                }
                while (!closed.get()){
                    try {
                        //间隔
                        long waitDuration = Math.max(getNextUpdateTime() - System.currentTimeMillis(), 1);
                        if (logger.isTraceEnabled()) {
                            logger.trace(tag + "CustomDNS: The next update will be in " + waitDuration + " milliseconds");
                        }
                        synchronized (updateWaitLock) {
                            try {
                                updateWaitLock.wait(waitDuration);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        if (closed.get()) {
                            break;
                        }
                        BackgroundUpdatingDns dns = getBackgroundUpdatingDns();
                        if (dns == null) {
                            if (logger.isTraceEnabled()) {
                                logger.trace(tag + "CustomDNS: No BackgroundUpdatingDns instance to be updated");
                            }
                            continue;
                        }
                        if (!dns.isBackgroundUpdate()) {
                            continue;
                        }
                        if (logger.isTraceEnabled()) {
                            logger.trace(tag + "CustomDNS: Update start");
                        }

                        // 执行
                        CountDownLatch latch = dns.doBackgroundUpdate(updateThreadPool);

                        // 等待更新完毕
                        try {
                            latch.await(DEFAULT_INTERVAL_SEC, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException ignore) {
                            if (logger.isTraceEnabled()) {
                                logger.warn(tag + "CustomDNS: Latch await interrupted");
                            }
                        }
                        // 打印dns报告
                        dns = getBackgroundUpdatingDns();
                        if (dns != null && logger.isInfoEnabled() && System.currentTimeMillis() - lastDnsReportTime > dns.getReportIntervalSec() * 1000L) {
                            logger.info(tag + "CustomDNS: \n==== HttpClient DNS Report ==================================\n" + dns.printCacheRecords());
                            lastDnsReportTime = System.currentTimeMillis();
                        }
                    } catch (Throwable t) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("CustomDNS: unexpected error", t);
                        }
                    }
                }
                if (logger.isInfoEnabled()) {
                    logger.info(tag + "CustomDNS: Closed");
                }
            }
        });
    }

    private BackgroundUpdatingDns getBackgroundUpdatingDns() {
        if (dns instanceof BackgroundUpdatingDns) {
            return (BackgroundUpdatingDns) dns;
        }
        return null;
    }

    private long getNextUpdateTime() {
        long now = System.currentTimeMillis();
        BackgroundUpdatingDns dns = getBackgroundUpdatingDns();
        if (dns == null || !dns.isBackgroundUpdate()) {
            return now + DEFAULT_INTERVAL_SEC * 1000L;
        }
        long maxUpdateTime = now + dns.getUpdMaxIntervalSec() * 1000L;
        long minUpdateTime = now + dns.getUpdMinIntervalSec() * 1000L;
        long nextUpdateTime = dns.getNextUpdateTime();
        if (nextUpdateTime < minUpdateTime) {
            return minUpdateTime;
        }
        if (nextUpdateTime > maxUpdateTime) {
            return maxUpdateTime;
        }
        return nextUpdateTime;
    }

}
