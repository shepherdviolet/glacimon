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

import com.github.shepherdviolet.glacimon.java.conversion.SimpleKeyValueEncoder;
import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import com.github.shepherdviolet.glacimon.java.net.NetworkUtils;
import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TTL到期前自动更新的DNS
 */
public class BackgroundUpdatingDns implements Dns {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long resolveTimeoutSeconds;
    private final boolean preferIpv6;
    private final long minTtlSeconds;
    private final long maxTtlSeconds;
    private final long errorTtlSeconds;
    private final long updMinIntervalSec;
    private final long updMaxIntervalSec;
    private final boolean isBackgroundUpdate;
    private final long reportIntervalSec;

    private final List<String> ips;
    private final List<Resolver> resolvers;
    private final boolean isIpv6Available;
    private final ConcurrentHashMap<String, CacheRecord> cache = new ConcurrentHashMap<>();

    private volatile Object backgroundUpdateWaitLock = new Object();

    // Constructor ///////////////////////////////////////////////////////////////////////////////////////////////////

    public BackgroundUpdatingDns(String dnsDescription) {
        try {
            Map<String, String> params = SimpleKeyValueEncoder.decode(dnsDescription);
            String ip = parseStringOrThrow(params, "key");
            this.resolveTimeoutSeconds = parseLongOrDefault(params, "resolveTimeoutSeconds", 5, 1);
            this.preferIpv6 = parseBooleanOrDefault(params, "preferIpv6", false);
            this.minTtlSeconds = parseLongOrDefault(params, "minTtlSeconds", 30, 0);
            this.maxTtlSeconds = parseLongOrDefault(params, "maxTtlSeconds", 300, minTtlSeconds);
            this.errorTtlSeconds = parseLongOrDefault(params, "errorTtlSeconds", 0, 0);
            this.updMinIntervalSec = parseLongOrDefault(params, "updMinIntervalSec", 5, 0);
            this.updMaxIntervalSec = parseLongOrDefault(params, "updMaxIntervalSec", 3600, updMinIntervalSec);
            this.isBackgroundUpdate = parseBooleanOrDefault(params, "isBackgroundUpdate", true);
            this.reportIntervalSec = parseLongOrDefault(params, "reportIntervalSec", 3600, 60);
            if (!params.isEmpty()) {
                logger.warn("LoadBalance | CustomDNS: Invalid dnsDescription parameters: " + params);
            }

            String[] ipArray = ip.split("\\|");
            this.isIpv6Available = NetworkUtils.isIpv6Available();
            this.ips = Stream.of(ipArray).map(String::trim).filter(CheckUtils::notEmpty).collect(Collectors.toList());
            this.resolvers = new ArrayList<>(this.ips.size());
            try {
                for (String i : this.ips) {
                    SimpleResolver resolver = new SimpleResolver(i);
                    resolver.setTimeout(Duration.ofSeconds(resolveTimeoutSeconds));
                    this.resolvers.add(resolver);
                }
            } catch (Throwable t) {
                throw new IllegalArgumentException("Invalid DNS resolver IPs: " + ips, t);
            }
            if (this.resolvers.isEmpty()) {
                throw new IllegalArgumentException("No invalid DNS resolver, IPs: " + ips);
            }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Invalid dnsDescription '" + dnsDescription + "'", t);
        }
    }

    private String parseStringOrThrow(Map<String, String> params, String key) {
        String value = params.remove(key);
        if (CheckUtils.isEmptyOrBlank(value)) {
            throw new IllegalArgumentException("Missing required parameter '" + key + "'");
        }
        return value;
    }

    private long parseLongOrDefault(Map<String, String> params, String key, long defaultValue, long minValue) {
        String value = params.remove(key);
        if (CheckUtils.isEmptyOrBlank(value)) {
            return Math.max(defaultValue, minValue);
        }
        try {
            return Math.max(Long.parseLong(value), minValue);
        } catch (Throwable t) {
            throw new IllegalArgumentException("Illegal parameter '" + key + "'", t);
        }
    }

    private boolean parseBooleanOrDefault(Map<String, String> params, String key, boolean defaultValue) {
        String value = params.remove(key);
        if (CheckUtils.isEmptyOrBlank(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public void setBackgroundUpdateWaitLock(Object waitLock) {
        if (waitLock == null) {
            waitLock = new Object();
        }
        this.backgroundUpdateWaitLock = waitLock;
    }

    // Lookup /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 域名解析 (同步等待)
     */
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (hostname == null) {
            throw new UnknownHostException("hostname is null");
        }
        CacheRecord cacheRecord = getCacheRecord(hostname);
        long now = System.currentTimeMillis();
        if (now < cacheRecord.getExpireAt()) {
            cacheRecord.countCacheHits();
            UnknownHostException e = cacheRecord.getException();
            List<InetAddress> addresses = cacheRecord.getAddresses();
            if (e != null) {
                throw e;
            } else if (addresses == null || addresses.isEmpty()) {
                throw new UnknownHostException("No A or AAAA records found for " + hostname);
            } else {
                return addresses;
            }
        }
        cacheRecord.countCacheMisses();
        LookupResult result = null;
        UnknownHostException exception = null;
        for (Resolver resolver : resolvers) {
            try {
                result = doLookup(resolver, hostname);
                if (!result.getAddresses().isEmpty()) {
                    break;
                }
            } catch (Throwable t) {
                // 记录主DNS的错误
                if (exception == null) {
                    if (t instanceof UnknownHostException) {
                        exception = (UnknownHostException) t;
                    } else {
                        exception = new UnknownHostException(t.getMessage());
                        exception.initCause(t);
                    }
                }
            }
        }
        if (result != null && !result.getAddresses().isEmpty()) {
            updateCacheRecord(hostname, result.getAddresses(), result.getTtl());
            return result.getAddresses();
        }
        if (exception == null) {
            exception = new UnknownHostException("No A or AAAA records found for " + hostname);
        }
        updateCacheRecord(hostname, exception);
        throw exception;
    }

    /**
     * 子类实现域名解析
     */
    private LookupResult doLookup(Resolver resolver, String hostname) throws UnknownHostException {
        try {
            List<InetAddress> addresses = new ArrayList<>();
            long minTtl = Long.MAX_VALUE;

            if (isIpv6Available && preferIpv6) {
                Lookup lookupAAAA = new Lookup(hostname, Type.AAAA);
                lookupAAAA.setResolver(resolver);
                lookupAAAA.setCache(null);
                org.xbill.DNS.Record[] recordsAAAA = lookupAAAA.run();
                switch (lookupAAAA.getResult()) {
                    case Lookup.SUCCESSFUL:
                        if (recordsAAAA != null) {
                            for (org.xbill.DNS.Record record : recordsAAAA) {
                                if (record instanceof AAAARecord) {
                                    AAAARecord aaaa = (AAAARecord) record;
                                    addresses.add(aaaa.getAddress());
                                    minTtl = Math.min(minTtl, aaaa.getTTL());
                                }
                            }
                        }
                        break;
                    case Lookup.UNRECOVERABLE:
                        throw new UnknownHostException("DNS resolver " + resolver.toString() + " is unrecoverable");
                    case Lookup.TRY_AGAIN:
                        throw new UnknownHostException("DNS resolver " + resolver.toString() + " does not respond");
                    default:
                        break;
                }
            }

            // 始终查询 IPv4
            Lookup lookupA = new Lookup(hostname, Type.A);
            lookupA.setResolver(resolver);
            lookupA.setCache(null);
            org.xbill.DNS.Record[] recordsA = lookupA.run();
            switch (lookupA.getResult()) {
                case Lookup.SUCCESSFUL:
                    if (recordsA != null) {
                        for (org.xbill.DNS.Record record : recordsA) {
                            if (record instanceof ARecord) {
                                ARecord a = (ARecord) record;
                                addresses.add(a.getAddress());
                                minTtl = Math.min(minTtl, a.getTTL());
                            }
                        }
                    }
                    break;
                case Lookup.UNRECOVERABLE:
                    throw new UnknownHostException("DNS resolver " + resolver.toString() + " is unrecoverable");
                case Lookup.TRY_AGAIN:
                    throw new UnknownHostException("DNS resolver " + resolver.toString() + " does not respond");
                default:
                    break;
            }

            if (isIpv6Available && !preferIpv6) {
                Lookup lookupAAAA = new Lookup(hostname, Type.AAAA);
                lookupAAAA.setResolver(resolver);
                lookupAAAA.setCache(null);
                org.xbill.DNS.Record[] recordsAAAA = lookupAAAA.run();
                switch (lookupAAAA.getResult()) {
                    case Lookup.SUCCESSFUL:
                        if (recordsAAAA != null) {
                            for (org.xbill.DNS.Record record : recordsAAAA) {
                                if (record instanceof AAAARecord) {
                                    AAAARecord aaaa = (AAAARecord) record;
                                    addresses.add(aaaa.getAddress());
                                    minTtl = Math.min(minTtl, aaaa.getTTL());
                                }
                            }
                        }
                        break;
                    case Lookup.UNRECOVERABLE:
                        throw new UnknownHostException("DNS resolver " + resolver.toString() + " is unrecoverable");
                    case Lookup.TRY_AGAIN:
                        throw new UnknownHostException("DNS resolver " + resolver.toString() + " does not respond");
                    default:
                        break;
                }
            }

            return new LookupResult(addresses, minTtl);
        } catch (TextParseException e) {
            UnknownHostException exception = new UnknownHostException("Invalid hostname " + hostname);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * 后台自动更新 (供DnsBackground
     */
    public CountDownLatch doBackgroundUpdate(ExecutorService updateThreadPool) {
        Map<String, CacheRecord> recordsToUpdate = new HashMap<>(cache.size() * 2);
        long now = System.currentTimeMillis();

        // 筛选需要更新的记录
        for (Map.Entry<String, CacheRecord> entry : cache.entrySet()) {
            long updateAt = entry.getValue().getUpdateAt();
            if (updateAt == Long.MAX_VALUE) {
                continue;
            }
            if (updateAt <= now) {
                recordsToUpdate.put(entry.getKey(), entry.getValue());
            }
        }

        // 执行更新
        CountDownLatch countDownLatch = new CountDownLatch(recordsToUpdate.size());
        for (Map.Entry<String, CacheRecord> entry : recordsToUpdate.entrySet()) {
            String hostname = entry.getKey();
            CacheRecord record = entry.getValue();
            try {
                updateThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (logger.isTraceEnabled()) {
                                logger.trace("LoadBalance | CustomDNS: doBackgroundUpdate: hostname " + hostname + " starts to update");
                            }
                            LookupResult result = null;
                            UnknownHostException exception = null;
                            for (Resolver resolver : resolvers) {
                                try {
                                    result = doLookup(resolver, hostname);
                                    if (!result.getAddresses().isEmpty()) {
                                        break;
                                    }
                                } catch (Throwable t) {
                                    // 记录主DNS的错误
                                    if (exception == null) {
                                        if (t instanceof UnknownHostException) {
                                            exception = (UnknownHostException) t;
                                        } else {
                                            exception = new UnknownHostException(t.getMessage());
                                            exception.initCause(t);
                                        }
                                    }
                                }
                            }
                            if (result != null && !result.getAddresses().isEmpty()) {
                                updateCacheRecord(hostname, result.getAddresses(), result.getTtl());
                                return;
                            }
                            if (exception == null) {
                                exception = new UnknownHostException("No A or AAAA records found for " + hostname);
                            }
                            updateCacheRecord(hostname, exception);
                        } catch (Throwable t) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("LoadBalance | CustomDNS: doBackgroundUpdate: lookup failed", t);
                            }
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            } catch (Throwable t) {
                countDownLatch.countDown();
                if (logger.isTraceEnabled()) {
                    logger.trace("LoadBalance | CustomDNS: doBackgroundUpdate: unexpected error", t);
                }
            }
        }

        return countDownLatch;
    }

    /**
     * 计算下次自动更新时间: 找到最近到期时间, 然后提早一半
     */
    public long getNextUpdateTime() {
        // 先找到最近到期时间
        long nextUpdateTime = Long.MAX_VALUE;
        for (CacheRecord record : cache.values()) {
            long updateAt = record.getUpdateAt();
            if (updateAt == Long.MAX_VALUE) {
                continue;
            }
            nextUpdateTime = Math.min(updateAt, nextUpdateTime);
        }
        return nextUpdateTime;
    }

    private void updateCacheRecord(String hostname, List<InetAddress> addresses, long ttl) {
        CacheRecord record = getCacheRecord(hostname);
        record.countSuccessfulLookups();

        String recordString = null;
        if (logger.isTraceEnabled()) {
            recordString = record.toString();
        }
        ttl = Math.min(maxTtlSeconds, ttl);
        ttl = Math.max(minTtlSeconds, ttl);
        record.setAddresses(addresses, ttl);
        if (logger.isTraceEnabled()) {
            logger.trace("LoadBalance | CustomDNS: updateCacheRecord " + recordString + " ---> " + record);
        }

        if (record.isFirstLookup()) {
            synchronized (backgroundUpdateWaitLock) {
                backgroundUpdateWaitLock.notifyAll();
            }
        }
    }

    private void updateCacheRecord(String hostname, UnknownHostException exception) {
        CacheRecord record = getCacheRecord(hostname);
        record.countFailedLookups();

        // 之前就失败了 或者 地址已过期了 才去记录错误, 否则有地址用就先用
        if (record.getException() != null || System.currentTimeMillis() >= record.getExpireAt()) {
            record.setException(exception, errorTtlSeconds);
        }

        if (record.isFirstLookup()) {
            synchronized (backgroundUpdateWaitLock) {
                backgroundUpdateWaitLock.notifyAll();
            }
        }
    }

    private CacheRecord getCacheRecord(String hostname) {
        return cache.computeIfAbsent(hostname, k -> new CacheRecord());
    }

    public long getUpdMinIntervalSec() {
        return updMinIntervalSec;
    }

    public long getUpdMaxIntervalSec() {
        return updMaxIntervalSec;
    }

    public boolean isBackgroundUpdate() {
        return isBackgroundUpdate;
    }

    public long getReportIntervalSec() {
        return reportIntervalSec;
    }

    @Override
    public String toString() {
        return "Dns{" +
                "ip=" + ips +
                ", resolveTimeoutSeconds=" + resolveTimeoutSeconds +
                ", preferIpv6=" + preferIpv6 +
                ", minTtlSeconds=" + minTtlSeconds +
                ", maxTtlSeconds=" + maxTtlSeconds +
                ", errorTtlSeconds=" + errorTtlSeconds +
                ", updMinIntervalSec=" + updMinIntervalSec +
                ", updMaxIntervalSec=" + updMaxIntervalSec +
                ", isBackgroundUpdate=" + isBackgroundUpdate +
                ", reportIntervalSec=" + reportIntervalSec +
                ", isIpv6Available=" + isIpv6Available +
                '}';
    }

    public String printCacheRecords() {
        StringBuilder stringBuilder = new StringBuilder("---- Resolver Info ------------------\n")
                .append(this).append('\n').append("---- Cache Info ---------------------\n");
        for (Map.Entry<String, CacheRecord> record : cache.entrySet()) {
            stringBuilder.append(record.getKey()).append(record.getValue().toString()).append('\n');
        }
        return stringBuilder.toString();
    }

    private static class CacheRecord {

        private volatile List<InetAddress> addresses;
        private volatile long ttl = -1; // 秒
        private volatile long expireAt = -1; // 毫秒(时间戳)
        private volatile UnknownHostException exception;
        private final AtomicLong cacheHits = new AtomicLong(0);
        private final AtomicLong cacheMisses = new AtomicLong(0);
        private final AtomicLong successfulLookups = new AtomicLong(0);
        private final AtomicLong failedLookups = new AtomicLong(0);
        private final AtomicBoolean firstLookup = new AtomicBoolean(true);

        private boolean isFirstLookup() {
            return firstLookup.compareAndSet(true, false);
        }

        private void setAddresses(List<InetAddress> addresses, long ttl) {
            this.addresses = addresses;
            this.ttl = ttl;
            this.expireAt = System.currentTimeMillis() + ttl * 1000L;
            this.exception = null;
        }

        private void setException(UnknownHostException exception, long ttl) {
            this.addresses = null;
            this.ttl = ttl;
            this.expireAt = System.currentTimeMillis() + ttl * 1000L;
            this.exception = exception;
        }

        private void countCacheHits() {
            cacheHits.incrementAndGet();
        }

        private void countCacheMisses() {
            cacheMisses.incrementAndGet();
        }

        private void countSuccessfulLookups() {
            successfulLookups.incrementAndGet();
        }

        private void countFailedLookups() {
            failedLookups.incrementAndGet();
        }

        private List<InetAddress> getAddresses() {
            return addresses;
        }

        private long getTtl() {
            return ttl;
        }

        private long getExpireAt() {
            return expireAt;
        }

        private long getUpdateAt() {
            if (expireAt < 0 || ttl < 0) {
                return Long.MAX_VALUE; // 还没解析成功过, 不知道什么时候更新
            }
            return expireAt - ttl * 1000L / 2L;
        }

        private UnknownHostException getException() {
            return exception;
        }

        @Override
        public String toString() {
            Exception e =  exception;
            return "{" +
                    "cacheHits=" + cacheHits +
                    ", cacheMisses=" + cacheMisses +
                    ", succLookups=" + successfulLookups +
                    ", failLookups=" + failedLookups +
                    ", ttl=" + ttl +
                    ", addr=" + addresses +
                    ", err=" + (e == null ? "" : e.getMessage()) +
                    '}';
        }
    }

    private static class LookupResult {

        private final List<InetAddress> addresses;
        private final long ttl;

        private LookupResult(List<InetAddress> addresses, long ttl) {
            this.addresses = addresses;
            this.ttl = ttl;
        }

        private List<InetAddress> getAddresses() {
            return addresses;
        }

        private long getTtl() {
            return ttl;
        }
    }

}
