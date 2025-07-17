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
    private final long stopUpdAftFails;
    private final long stopUpdAftIdleSec;

    private final List<String> ips;
    private final List<Resolver> resolvers;
    private final boolean isIpv6Available;
    private final ConcurrentHashMap<String, CacheRecord> cache = new ConcurrentHashMap<>();

    private volatile BackgroundUpdateWaitLock backgroundUpdateWaitLock = BackgroundUpdateWaitLock.DUMMY;

    // Constructor ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * [可运行时修改]
     * </p>配置自定义Dns</p>
     * <p>参数采用SimpleKeyValueEncoder格式, 详见: https://github.com/shepherdviolet/glacimon/blob/master/docs/kvencoder/guide.md</p>
     * <p></p>
     * <p>参数说明:</p>
     * <p>ip: DNS服务地址, 必输; 多个地址使用'|'分割, 例如: ip=8.8.8.8|114.114.114.114</p>
     * <p>resolveTimeoutSeconds: 域名解析超时时间(秒), 可选, 默认5s</p>
     * <p>preferIpv6: true:Ipv6优先, false:Ipv4优先, 可选, 默认false</p>
     * <p>minTtlSeconds: 最小TTL(秒), 可选, 默认20; 实际TTL为max(服务器返回TTL, 该参数值)</p>
     * <p>maxTtlSeconds: 最大TTL(秒), 可选, 默认300; 实际TTL为min(服务器返回TTL, 该参数值)</p>
     * <p>errorTtlSeconds: 域名解析错误时的TTL(秒), 可选, 默认0</p>
     * <p>updMinIntervalSec: 后台自动更新最小间隔(秒), 可选, 默认5; 程序会在TTL到期前自动更新域名解析记录, 这是更新线程最小间隔.</p>
     * <p>updMaxIntervalSec: 后台自动更新最大间隔(秒), 可选, 默认3600; 程序会在TTL到期前自动更新域名解析记录, 这是更新线程最大间隔.</p>
     * <p>isBackgroundUpdate: true:启用后台自动更新, false:关闭后台自动更新, 可选, 默认true</p>
     * <p>reportIntervalSec: DNS解析报告打印间隔(秒), 可选, 默认3600; 程序会在日志中打印DNS解析相关统计信息</p>
     * <p>stopUpdAftFails: 域名解析失败指定次数后, 停止自动更新, 可选, 默认5; 仅影响自动更新, 不影响同步解析</p>
     * <p>stopUpdAftIdleSec: 域名未使用指定时间(秒)后, 停止自动更新, 可选, 默认1200; 仅影响自动更新, 不影响同步解析</p>
     * <p></p>
     * <p>参数格式: key1=value1,key2=value2</p>
     * <p></p>
     * <p>示例(设置一个DNS): ip=8.8.8.8</p>
     * <p>示例(设置多个DNS): ip=8.8.8.8|114.114.114.114</p>
     * <p>示例(设置解析超时时间): ip=8.8.8.8,resolveTimeoutSeconds=5</p>
     * <p>示例(设置ipv6是否优先): ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false</p>
     * <p>示例(设置最大最小TTL): ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false,minTtlSeconds=30,maxTtlSeconds=300</p>
     * <p>示例(设置是否后台自动更新): ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false,isBackgroundUpdate=true</p>
     */
    public BackgroundUpdatingDns(String dnsDescription) {
        try {
            Map<String, String> params = SimpleKeyValueEncoder.decode(dnsDescription);
            String ip = parseStringOrThrow(params, "ip");
            this.resolveTimeoutSeconds = parseLongOrDefault(params, "resolveTimeoutSeconds", 5, 1);
            this.preferIpv6 = parseBooleanOrDefault(params, "preferIpv6", false);
            this.minTtlSeconds = parseLongOrDefault(params, "minTtlSeconds", 20, 0);
            this.maxTtlSeconds = parseLongOrDefault(params, "maxTtlSeconds", 300, minTtlSeconds);
            this.errorTtlSeconds = parseLongOrDefault(params, "errorTtlSeconds", 0, 0);
            this.updMinIntervalSec = parseLongOrDefault(params, "updMinIntervalSec", 5, 0);
            this.updMaxIntervalSec = parseLongOrDefault(params, "updMaxIntervalSec", 3600, updMinIntervalSec);
            this.isBackgroundUpdate = parseBooleanOrDefault(params, "isBackgroundUpdate", true);
            this.reportIntervalSec = parseLongOrDefault(params, "reportIntervalSec", 3600, 60);
            this.stopUpdAftFails = parseLongOrDefault(params, "stopUpdAftFails", 5, 1);
            this.stopUpdAftIdleSec = parseLongOrDefault(params, "stopUpdAftIdleSec", 1200, 60);
            if (!params.isEmpty()) {
                logger.warn("LoadBalance | DNS: Invalid dnsDescription parameters: " + params + ", check your config glacispring.httpclients.*.dns-description");
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

    public void setBackgroundUpdateWaitLock(BackgroundUpdateWaitLock waitLock) {
        if (waitLock == null) {
            waitLock = BackgroundUpdateWaitLock.DUMMY;
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
        cacheRecord.updateLastLookupTime(now);
        if (now < cacheRecord.getExpireAt()) {
            /*
             * 成功从缓存拿到记录, 通知更新器退出IDLE状态
             * 适用于所有记录都长时间没用, 导致更新器进入IDLE状态, 哪怕从缓存成功得到记录, 也退出IDLE
             */
            backgroundUpdateWaitLock.exitIdle();
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
            CacheRecord cacheRecord = entry.getValue();
            long updateAt = cacheRecord.getUpdateAt();
            if (updateAt == Long.MAX_VALUE) {
                continue;
            }
            if (cacheRecord.isUpdateStopped(now, stopUpdAftFails, stopUpdAftIdleSec)) {
                continue;
            }
            if (updateAt <= now) {
                recordsToUpdate.put(entry.getKey(), cacheRecord);
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
                                logger.trace("LoadBalance | DNS: doBackgroundUpdate: Hostname " + hostname + " starts to update");
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
                                logger.trace("LoadBalance | DNS: doBackgroundUpdate: Lookup failed", t);
                            }
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            } catch (Throwable t) {
                countDownLatch.countDown();
                if (logger.isTraceEnabled()) {
                    logger.trace("LoadBalance | DNS: doBackgroundUpdate: Unexpected error", t);
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
        long now = System.currentTimeMillis();
        long nextUpdateTime = Long.MAX_VALUE;
        for (CacheRecord record : cache.values()) {
            long updateAt = record.getUpdateAt();
            if (updateAt == Long.MAX_VALUE) {
                continue;
            }
            if (record.isUpdateStopped(now, stopUpdAftFails, stopUpdAftIdleSec)) {
                continue;
            }
            nextUpdateTime = Math.min(updateAt, nextUpdateTime);
        }
        return nextUpdateTime;
    }

    private void updateCacheRecord(String hostname, List<InetAddress> addresses, long ttl) {
        CacheRecord record = getCacheRecord(hostname);

        String recordString = null;
        if (logger.isTraceEnabled()) {
            recordString = record.toString();
        }

        ttl = Math.min(maxTtlSeconds, ttl);
        ttl = Math.max(minTtlSeconds, ttl);
        record.setAddresses(addresses, ttl);
        record.countSuccessfulLookups();
        record.resetConsecFails();

        if (logger.isTraceEnabled()) {
            logger.trace("LoadBalance | DNS: Record Updated: " + recordString + " ---> " + record);
        }

        if (record.isFirstLookup()) {
            backgroundUpdateWaitLock.signalAll();
        } else {
            /*
             * 成功lookup到地址, 通知更新器退出IDLE状态
             * 适用于所有记录都解析失败超过限值, 导致更新器进入IDLE状态, 当一条记录手动解析成功后, 退出IDLE
             */
            backgroundUpdateWaitLock.exitIdle();
        }
    }

    private void updateCacheRecord(String hostname, UnknownHostException exception) {
        CacheRecord record = getCacheRecord(hostname);

        String recordString = null;
        if (logger.isTraceEnabled()) {
            recordString = record.toString();
        }

        // 之前就失败了 或者 地址已过期了 才去记录错误, 否则有地址用就先用
        if (record.getException() != null || System.currentTimeMillis() >= record.getExpireAt()) {
            record.setException(exception, errorTtlSeconds);
        }

        record.countFailedLookups();
        record.countConsecFails();

        if (logger.isTraceEnabled()) {
            logger.trace("LoadBalance | DNS: Record Updated: " + recordString + " ---> " + record);
        }

        if (record.isFirstLookup()) {
            backgroundUpdateWaitLock.signalAll();
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
                ", stopUpdAftFails=" + stopUpdAftFails +
                ", stopUpdAftIdleSec=" + stopUpdAftIdleSec +
                ", isIpv6Available=" + isIpv6Available +
                '}';
    }

    public String printCacheRecords() {
        StringBuilder stringBuilder = new StringBuilder("---- Custom DNS Resolver Info ------------------------------------\n")
                .append(this).append('\n').append("---- Hostname Record Info ----------------------------------------\n");
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
        private final AtomicLong consecFails = new AtomicLong(0);
        private final AtomicLong lastLookupTime = new AtomicLong(System.currentTimeMillis());

        private boolean isFirstLookup() {
            return firstLookup.compareAndSet(true, false);
        }

        private boolean isUpdateStopped(long now, long stopUpdAftFails, long stopUpdAftIdleSec) {
            if (consecFails.get() >= stopUpdAftFails) {
                return true;
            }
            return now - lastLookupTime.get() > stopUpdAftIdleSec * 1000L;
        }

        private void countConsecFails() {
            consecFails.getAndIncrement();
        }

        private void resetConsecFails() {
            consecFails.set(0);
        }

        private void updateLastLookupTime(long now) {
            lastLookupTime.set(now);
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
            cacheHits.getAndIncrement();
        }

        private void countCacheMisses() {
            cacheMisses.getAndIncrement();
        }

        private void countSuccessfulLookups() {
            successfulLookups.getAndIncrement();
        }

        private void countFailedLookups() {
            failedLookups.getAndIncrement();
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
                    "cacheHit=" + cacheHits +
                    ", cacheMiss=" + cacheMisses +
                    ", succLookup=" + successfulLookups +
                    ", failLookup=" + failedLookups +
                    ", consecFail=" + consecFails +
                    ", lastLookup=" + lastLookupTime +
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
