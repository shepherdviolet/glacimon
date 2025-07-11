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

import com.github.shepherdviolet.glacimon.java.net.NetworkUtils;
import okhttp3.Dns;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DNS实现, 需要依赖: dnsjava:dnsjava:3.6.3
 *
 * @author shepherdviolet
 */
public class DnsImpl implements Dns {

    private final String ip;
    private final long resolveTimeoutSeconds;
    private final long maxTtlSeconds;
    private final boolean ipv6Enabled;
    private final boolean isIPv6Available;

    private final Resolver resolver;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public DnsImpl(String ip, long resolveTimeoutSeconds, boolean ipv6Enabled) throws Exception {
        this(ip, resolveTimeoutSeconds, ipv6Enabled, 300);
    }

    public DnsImpl(String ip, long resolveTimeoutSeconds, boolean ipv6Enabled, long maxTtlSeconds) throws Exception {
        this.resolver = new SimpleResolver(ip);
        this.resolver.setTimeout(Duration.ofSeconds(resolveTimeoutSeconds));
        this.ip = ip;
        this.resolveTimeoutSeconds = resolveTimeoutSeconds;
        this.ipv6Enabled = ipv6Enabled;
        this.maxTtlSeconds = maxTtlSeconds;
        this.isIPv6Available = NetworkUtils.isIPv6Available();
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (hostname == null) {
            throw new UnknownHostException("hostname is null");
        }
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(hostname);
        if (entry != null && now < entry.expireAt) {
            return entry.addresses;
        }
        return resolve(hostname, now);
    }

    private List<InetAddress> resolve(String hostname, long now) throws UnknownHostException {
        try {
            List<InetAddress> addresses = new ArrayList<>();
            long minTtl = Long.MAX_VALUE;

            if (ipv6Enabled && isIPv6Available) {
                Lookup lookupAAAA = new Lookup(hostname, Type.AAAA);
                lookupAAAA.setResolver(resolver);
                lookupAAAA.setCache(null);

                org.xbill.DNS.Record[] recordsAAAA = lookupAAAA.run();
                if (recordsAAAA != null && lookupAAAA.getResult() == Lookup.SUCCESSFUL) {
                    for (org.xbill.DNS.Record record : recordsAAAA) {
                        if (record instanceof AAAARecord) {
                            AAAARecord aaaa = (AAAARecord) record;
                            addresses.add(aaaa.getAddress());
                            minTtl = Math.min(minTtl, aaaa.getTTL());
                        }
                    }
                }
            }

            // 始终查询 IPv4
            Lookup lookupA = new Lookup(hostname, Type.A);
            lookupA.setResolver(resolver);
            lookupA.setCache(null);

            org.xbill.DNS.Record[] recordsA = lookupA.run();
            if (recordsA != null && lookupA.getResult() == Lookup.SUCCESSFUL) {
                for (org.xbill.DNS.Record record : recordsA) {
                    if (record instanceof ARecord) {
                        ARecord a = (ARecord) record;
                        addresses.add(a.getAddress());
                        minTtl = Math.min(minTtl, a.getTTL());
                    }
                }
            }

            if (addresses.isEmpty()) {
                throw new UnknownHostException("No A or AAAA records found for " + hostname);
            }

            long ttl = Math.min(minTtl, maxTtlSeconds);
            long expireAt = now + ttl * 1000L;
            cache.put(hostname, new CacheEntry(addresses, expireAt));

            return addresses;

        } catch (TextParseException e) {
            UnknownHostException exception = new UnknownHostException("Invalid hostname: " + hostname);
            exception.initCause(e);
            throw exception;
        } catch (Exception e) {
            UnknownHostException exception = new UnknownHostException("DNS resolve error for " + hostname + ": " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    private static class CacheEntry {
        final List<InetAddress> addresses;
        final long expireAt;

        CacheEntry(List<InetAddress> addresses, long expireAt) {
            this.addresses = addresses;
            this.expireAt = expireAt;
        }
    }

    @Override
    public String toString() {
        return "DnsImpl{" +
                "ip='" + ip + '\'' +
                ", resolveTimeoutSeconds=" + resolveTimeoutSeconds +
                ", ipv6Enabled=" + ipv6Enabled +
                ", isIPv6Available=" + isIPv6Available +
                ", maxTtlSeconds=" + maxTtlSeconds +
                '}';
    }
}
