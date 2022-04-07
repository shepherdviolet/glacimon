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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.net.loadbalance.springboot.autoconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import sviolet.slate.common.x.net.loadbalance.classic.DataConverter;
import sviolet.slate.common.x.net.loadbalance.classic.SimpleOkHttpClient;
import sviolet.slate.common.x.net.loadbalance.classic.SslUtils;
import sviolet.slate.common.x.net.loadbalance.springboot.HttpClients;
import sviolet.thistle.entity.function.ThrowableBiConsumer;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.thistle.util.conversion.SimpleKeyValueEncoder;
import sviolet.thistle.util.judge.CheckUtils;

import java.io.Closeable;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 维护SpringBoot自动配置的SimpleOkHttpClient实例
 *
 * @author S.Violet
 */
class HttpClientsImpl implements HttpClients, Closeable, InitializingBean, DisposableBean {

    public static final String SETTING_PREFIX = "slate.httpclients.";
    private static final int ARRAY_MAX_SIZE = 1024;

    private static final Logger logger = LoggerFactory.getLogger(HttpClientsImpl.class);

    private final DataConverter dataConverter;
    private boolean noticeLogEnabled = true;

    private final Map<String, HttpClient> clients = new ConcurrentHashMap<>(16);

    private final Map<String, Updater> clientUpdaters = new HashMap<>(32);

    private final LinkedBlockingQueue<OverrideSettings> unsolvedSettings = new LinkedBlockingQueue<>();
    private final ExecutorService updateExecutor = ThreadPoolExecutorUtils.createLazy(60, "Slate-HttpClients-update-%d");

    HttpClientsImpl(SlatePropertiesForHttpClient slatePropertiesForHttpClient, DataConverter dataConverter) {
        this.dataConverter = dataConverter;

        if (slatePropertiesForHttpClient.getHttpclient() != null) {
            noticeLogEnabled = slatePropertiesForHttpClient.getHttpclient().isNoticeLogEnabled();
        }

        // Init client updaters
        initClientUpdaters();

        // Create clients at startup
        createClientsAtStartup(slatePropertiesForHttpClient);
    }

    /**
     * 覆盖(更新)客户端配置
     * @param overrideSettings 新配置
     */
    @Override
    public void settingsOverride(OverrideSettings overrideSettings) {
        if (overrideSettings == null) {
            if (logger.isDebugEnabled() && noticeLogEnabled) {
                logger.debug("HttpClients SettingsUpdate | overrideSettings is null, skip update");
            }
            return;
        }

        // notify async-update
        notifyUpdate(overrideSettings);
    }

    /**
     * 获取客户端
     * @param key 客户端名称
     */
    @Override
    public SimpleOkHttpClient get(String key) {
        SimpleOkHttpClient client = clients.get(key);
        if (client == null && logger.isWarnEnabled() && noticeLogEnabled) {
            logger.warn("HttpClients | No HttpClient named " + key + ", return null");
        }
        return client;
    }

    /**
     * 客户端数量
     */
    @Override
    public int size() {
        return clients.size();
    }

    /**
     * 所有客户端名称
     */
    @Override
    public Set<String> tags() {
        return clients.keySet();
    }

    /**
     * 关闭所有客户端
     */
    @Override
    public void close() throws IOException {
        Map<String, HttpClient>  clients = this.clients;
        for (Map.Entry<String, HttpClient> entry : clients.entrySet()) {
            entry.getValue().close();
        }
    }

    /**
     * 关闭所有客户端, 等同于close
     */
    @Override
    public void destroy() throws Exception {
        Map<String, HttpClient>  clients = this.clients;
        for (Map.Entry<String, HttpClient> entry : clients.entrySet()) {
            entry.getValue().destroy();
        }
    }

    /**
     * 随Spring启动
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, HttpClient>  clients = this.clients;
        for (Map.Entry<String, HttpClient> entry : clients.entrySet()) {
            entry.getValue().afterPropertiesSet();
        }
    }

    // Create /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 启动时第一次创建客户端示例
     */
    private void createClientsAtStartup(SlatePropertiesForHttpClient slatePropertiesForHttpClient) {
        if (noticeLogEnabled) {
            logger.info("HttpClients | Enabled");
        }

        if (slatePropertiesForHttpClient.getHttpclients() == null) {
            return;
        }

        //create client
        for (Map.Entry<String, HttpClientSettings> entry : slatePropertiesForHttpClient.getHttpclients().entrySet()) {

            String tag = entry.getKey();

            HttpClientSettings settings = entry.getValue();
            if (settings == null) {
                logger.warn("HttpClients | Client " + tag + "> Has no properties, skip creation");
                continue;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("HttpClients | Client " + tag + "> Creating with settings: " + settings);
            }

            HttpClient client = createClient(tag, settings);
            clients.put(tag, client);

            if (logger.isInfoEnabled()) {
                logger.info("HttpClients | Client " + tag + "> Created: " + client);
            }

        }
    }

    /**
     * 创建一个客户端
     */
    private HttpClient createClient(String tag, HttpClientSettings settings) {

        //tag
        HttpClient client = (HttpClient) new HttpClient().setTag(tag);

        //hosts
        if (!CheckUtils.isEmptyOrBlank(settings.getHosts())) {
            client.setHosts(settings.getHosts());
        } else {
            client.setHostArray(settings.getHostList());
        }

        //headers
        Map<String, String> headers = null;
        if (!CheckUtils.isEmptyOrBlank(settings.getHeaders())) {
            try {
                headers = SimpleKeyValueEncoder.decode(settings.getHeaders());
            } catch (SimpleKeyValueEncoder.DecodeException e) {
                throw new RuntimeException("HttpClients | Error while parsing headers '" + settings.getHeaders() +
                        "' to Map, illegal key-value format, see github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md", e);
            }
        }

        //custom issuer
        try {
            if (!CheckUtils.isEmptyOrBlank(settings.getCustomServerIssuerEncoded())) {
                SslUtils.setCustomServerIssuerEncoded(client, settings.getCustomServerIssuerEncoded());
            } else if (settings.getCustomServerIssuersEncoded() != null && settings.getCustomServerIssuersEncoded().length > 0){
                SslUtils.setCustomServerIssuersEncoded(client, settings.getCustomServerIssuersEncoded());
            }
        } catch (CertificateException e) {
            throw new RuntimeException("Error while setting custom server issuers", e);
        }

        //custom dn/cn verification
        if (!CheckUtils.isEmptyOrBlank(settings.getVerifyServerDnByCustomDn())) {
            SslUtils.setVerifyServerDnByCustomDn(client, settings.getVerifyServerDnByCustomDn());
        } else if (!CheckUtils.isEmptyOrBlank(settings.getVerifyServerCnByCustomHostname())){
            SslUtils.setVerifyServerCnByCustomHostname(client, settings.getVerifyServerCnByCustomHostname());
        }

        //properties
        return (HttpClient) client
                .setInitiativeInspectInterval(settings.getInitiativeInspectInterval())
                .setReturnNullIfAllBlocked(settings.isReturnNullIfAllBlocked())
                .setHttpGetInspector(settings.getHttpGetInspectorUrlSuffix())
                .setInspectorVerboseLog(settings.isInspectorVerboseLog())
                .setPassiveBlockDuration(settings.getPassiveBlockDuration())
                .setMediaType(settings.getMediaType())
                .setEncode(settings.getEncode())
                .setHeaders(headers)
                .setDataConverter(dataConverter)
                .setRecoveryCoefficient(settings.getRecoveryCoefficient())
                .setMaxIdleConnections(settings.getMaxIdleConnections())
                .setMaxThreads(settings.getMaxThreads())
                .setMaxThreadsPerHost(settings.getMaxThreadsPerHost())
                .setConnectTimeout(settings.getConnectTimeout())
                .setWriteTimeout(settings.getWriteTimeout())
                .setReadTimeout(settings.getReadTimeout())
                .setMaxReadLength(settings.getMaxReadLength())
                .setHttpCodeNeedBlock(settings.getHttpCodeNeedBlock())
                .setVerboseLog(settings.isVerboseLog())
                .setTxTimerEnabled(settings.isTxTimerEnabled())
                .setRequestTraceEnabled(settings.isRequestTraceEnabled());
    }

    private static final class HttpClient extends SimpleOkHttpClient {

        // 是否被更新的标记
        private boolean updated = false;

        private HttpClient() {
        }

        private boolean isUpdated() {
            return updated;
        }

        private void setUpdated(boolean updated) {
            this.updated = updated;
        }
    }

    // Update /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 异步触发配置更新
     */
    private void notifyUpdate(OverrideSettings overrideSettings) {
        // new settings enqueue
        unsolvedSettings.offer(overrideSettings);
        // execute update task in thread
        updateExecutor.execute(updateTask);
    }

    /**
     * 异步更新任务
     */
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            OverrideSettings settings;
            while ((settings = unsolvedSettings.poll()) != null) {
                update(settings);
            }
        }
    };

    /**
     * 更新配置
     */
    private void update(OverrideSettings settings) {
        Set<String> propertiesKeys = settings.getKeys();
        // check keys
        if (propertiesKeys == null || propertiesKeys.isEmpty()) {
            if (logger.isDebugEnabled() && noticeLogEnabled) {
                logger.debug("HttpClients SettingsUpdate | overrideSettings.getKeys() return null or empty, skip update!");
            }
            return;
        }

        for (String propertiesKey : propertiesKeys) {
            // Check if relevant
            if (propertiesKey == null || !propertiesKey.startsWith(SETTING_PREFIX) || propertiesKey.length() <= SETTING_PREFIX.length()) {
                if (logger.isTraceEnabled() && noticeLogEnabled) {
                    logger.trace("HttpClients SettingsUpdate | Skip properties key '" + propertiesKey + "', not start with " + SETTING_PREFIX);
                }
                continue;
            }

            // Get tag / key / index from 'prefix.tag.key[1]=value'
            int tagEnd = propertiesKey.indexOf('.', SETTING_PREFIX.length());
            if (tagEnd <= SETTING_PREFIX.length() || tagEnd == propertiesKey.length() - 1) {
                logger.error("HttpClients SettingsUpdate | Illegal properties key '" + propertiesKey +
                        "'. The correct format is '" + SETTING_PREFIX + "tag.property=value'. skipped!");
                continue;
            }
            String tag = propertiesKey.substring(SETTING_PREFIX.length(), tagEnd);
            String key = propertiesKey.substring(tagEnd + 1);
            int index = -1;
            if (key.endsWith("]")) {
                int indexStart = key.lastIndexOf('[');
                if (indexStart > 0 && indexStart < key.length() - 2) {
                    try {
                        index = Integer.parseInt(key.substring(indexStart + 1, key.length() - 1));
                    } catch (NumberFormatException e) {
                        logger.error("HttpClients SettingsUpdate | Illegal properties key '" + propertiesKey +
                                "', error while parsing index to number. The correct format is '" + SETTING_PREFIX +
                                "tag.property[index]=value', index should be a number. skipped!", e);
                        continue;
                    }
                    if (index < 0 || index > ARRAY_MAX_SIZE) {
                        logger.error("HttpClients SettingsUpdate | Illegal properties key '" + propertiesKey +
                                "', index < 0 or index > 1024. The correct format is '" + SETTING_PREFIX +
                                "tag.property[index]=value', index in [0, 1024]. skipped!");
                        continue;
                    }
                    key = key.substring(0, indexStart);
                }
            }

            // Get value
            String value = settings.getValue(propertiesKey);

            // Get client
            HttpClient client = clients.get(tag);

            //Check if new
            if (client == null) {
                try {
                    client = createClient(tag, new HttpClientSettings());
                    client.start();
                    clients.put(tag, client);
                    logger.info("HttpClients SettingsUpdate | " + tag +
                            "> Create new HttpClient with default properties, because no HttpClient named " + tag + " before");
                } catch (Exception e) {
                    logger.warn("HttpClients SettingsUpdate | Error while creating new HttpClient named " + tag +
                            ", skip properties key " + propertiesKey, e);
                    continue;
                }
            }

            // Find updater
            Updater updater = clientUpdaters.get(key);
            if (updater == null) {
                logger.error("HttpClients SettingsUpdate | Error while Updating setting '" + key + "' of client '" + tag +
                        "' to '" + value + "', No overridable properties key named '" + toPropertyName(tag, key, index) + "'");
                continue;
            }

            // Update setting
            updater.updateSetting(tag, key, index, value);
        }

        // Apply setting changes
        for (Updater updater : clientUpdaters.values()) {
            updater.applySettings(clients);
        }

        //Print changed
        if (logger.isInfoEnabled()) {
            for (HttpClient client : clients.values()) {
                if (client.isUpdated()) {
                    logger.info("HttpClients SettingsUpdate | Client " + client.getTag() + "> Updated: " + client);
                    client.setUpdated(false);
                }
            }
        }
    }

    // Updaters /////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initClientUpdaters() {

        installUpdater(new SingleOrArrayValueUpdater(
                Arrays.asList("hosts"),
                Arrays.asList("hostList", "host-list")) {
            @Override
            public void applySingleSetting(HttpClient client, String value) throws Exception {
                client.setHosts(value);
            }
            @Override
            public void applyArraySetting(HttpClient client, String[] value) throws Exception {
                client.setHostArray(value);
            }
            @Override
            public void applyReset(HttpClient client) {
                client.setHosts(null);
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("headers")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setHeaders(SimpleKeyValueEncoder.decode(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("initiativeInspectInterval", "initiative-inspect-interval")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setInitiativeInspectInterval(Long.parseLong(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("returnNullIfAllBlocked", "return-null-if-all-blocked")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setReturnNullIfAllBlocked(Boolean.parseBoolean(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("httpGetInspectorUrlSuffix", "http-get-inspector-url-suffix")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setHttpGetInspector(value);
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("inspectorVerboseLog", "inspector-verbose-log")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setInspectorVerboseLog(Boolean.parseBoolean(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("passiveBlockDuration", "passive-block-duration")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setPassiveBlockDuration(Long.parseLong(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("mediaType", "media-type")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setMediaType(value);
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("encode")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setEncode(value);
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("recoveryCoefficient", "recovery-coefficient")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setRecoveryCoefficient(Integer.parseInt(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("maxIdleConnections", "max-idle-connections")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setMaxIdleConnections(Integer.parseInt(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("maxThreads", "max-threads")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setMaxThreads(Integer.parseInt(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("maxThreadsPerHost", "max-threads-per-host")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setMaxThreadsPerHost(Integer.parseInt(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("connectTimeout", "connect-timeout")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setConnectTimeout(Long.parseLong(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("writeTimeout", "write-timeout")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setWriteTimeout(Long.parseLong(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("readTimeout", "read-timeout")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setReadTimeout(Long.parseLong(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("maxReadLength", "max-read-length")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setMaxReadLength(Long.parseLong(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("httpCodeNeedBlock", "http-code-need-block")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setHttpCodeNeedBlock(value);
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("verboseLog", "verbose-log")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setVerboseLog(Boolean.parseBoolean(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("txTimerEnabled", "tx-timer-enabled")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setTxTimerEnabled(Boolean.parseBoolean(value));
            }
        });

        installUpdater(new SingleValueUpdater(
                Arrays.asList("requestTraceEnabled", "request-trace-enabled")) {
            @Override
            public void applySetting(HttpClient client, String value) throws Exception {
                client.setRequestTraceEnabled(Boolean.parseBoolean(value));
            }
        });

        installUpdater(new SingleOrArrayValueUpdater(
                Arrays.asList("customServerIssuerEncoded", "custom-server-issuer-encoded"),
                Arrays.asList("customServerIssuersEncoded", "custom-server-issuers-encoded")) {
            @Override
            public void applySingleSetting(HttpClient client, String value) throws Exception {
                SslUtils.setCustomServerIssuerEncoded(client, value);
            }
            @Override
            public void applyArraySetting(HttpClient client, String[] value) throws Exception {
                SslUtils.setCustomServerIssuersEncoded(client, value);
            }
            @Override
            public void applyReset(HttpClient client) throws Exception {
                SslUtils.setCustomServerIssuerEncoded(client, null);
            }
        });

        installUpdater(new SingleOrSingleValueUpdater(
                Arrays.asList("verifyServerDnByCustomDn", "verify-server-dn-by-custom-dn"),
                Arrays.asList("verifyServerCnByCustomHostname", "verify-server-cn-by-custom-hostname")) {
            @Override
            public void applySetting1(HttpClient client, String value) throws Exception {
                SslUtils.setVerifyServerDnByCustomDn(client, value);
            }
            @Override
            public void applySetting2(HttpClient client, String value) throws Exception {
                SslUtils.setVerifyServerCnByCustomHostname(client, value);
            }
            @Override
            public void applyReset(HttpClient client) throws Exception {
                SslUtils.setVerifyServerDnByCustomDn(client, null);
            }
        });

    }

    private void installUpdater(Updater updater) {
        for (String key : updater.getKeys()) {
            clientUpdaters.put(key, updater);
        }
    }

    private interface Updater {

        void updateSetting(String tag, String key, int index, String value);

        void applySettings(Map<String, HttpClient> clients);

        Collection<String> getKeys();

    }

    private static String toPropertyName(String tag, String key, int index){
        if (index < 0) {
            return SETTING_PREFIX + "." + tag + "." + key;
        } else {
            return SETTING_PREFIX + "." + tag + "." + key + "[" + index + "]";
        }
    }

    /**
     * 单值更新
     */
    private static abstract class SingleValueUpdater implements Updater {

        private final String settingName;
        private final List<String> settingNames;
        private final Map<String, String> values = new HashMap<>();
        private final Set<String> tagToBeUpdated = new HashSet<>();

        public SingleValueUpdater(List<String> settingNames) {
            this.settingName = settingNames.get(0);
            this.settingNames = settingNames;
        }

        @Override
        public final void updateSetting(String tag, String key, int index, String value) {
            if (value == null) {
                logger.warn("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                        "' is null, setting '" + settingName + "' of client '" + tag + "' stay the same");
                return;
            }
            if (value.equals(values.get(tag))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                            "' is the same as the old value, setting '" + settingName + "' of client '" + tag + "' stay the same");
                }
                return;
            }
            values.put(tag, value);
            tagToBeUpdated.add(tag);
        }

        @Override
        public final void applySettings(Map<String, HttpClient> clients) {
            for (String tag : tagToBeUpdated) {
                HttpClient client = clients.get(tag);
                if (client == null) {
                    // impossible
                    continue;
                }
                String value = values.get(tag);
                logger.info("HttpClients SettingsUpdate | Update setting '" + settingName + "' of client '" + tag + "' to '" + value + "'");
                try {
                    client.setUpdated(true);
                    applySetting(client, value);
                } catch (NumberFormatException e) {
                    logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName +
                            "' of client '" + tag + "' to '" + value + "', number format failed", e);
                } catch (Throwable t) {
                    if (t instanceof SimpleKeyValueEncoder.DecodeException) {
                        logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName +
                                "' of client '" + tag + "' to '" + value +
                                "', illegal key-value format, see github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md", t);
                    } else {
                        logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName +
                                "' of client '" + tag + "' to '" + value + "'", t);
                    }
                }
            }
            tagToBeUpdated.clear();
        }

        @Override
        public Collection<String> getKeys() {
            return new ArrayList<>(settingNames);
        }

        public abstract void applySetting(HttpClient client, String value) throws Exception;

    }

    /**
     * 若第一个单值存在, 则第一个单值生效,
     * 若第一个单值为空, 则尝试第二个单值,
     * 若第二个单值存在, 则第二个单值生效,
     * 若第二个单值为空, 则重置参数(恢复默认设置)
     */
    private static abstract class SingleOrSingleValueUpdater implements Updater {

        private final String settingName1;
        private final String settingName2;
        private final List<String> settingNames1;
        private final List<String> settingNames2;
        private final Map<String, String> values1 = new HashMap<>();
        private final Map<String, String> values2 = new HashMap<>();
        private final Set<String> tagToBeUpdated = new HashSet<>();

        public SingleOrSingleValueUpdater(List<String> settingNames1, List<String> settingNames2) {
            this.settingName1 = settingNames1.get(0);
            this.settingName2 = settingNames2.get(0);
            this.settingNames1 = settingNames1;
            this.settingNames2 = settingNames2;
        }

        @Override
        public final void updateSetting(String tag, String key, int index, String value) {
            if (settingNames1.contains(key)) {
                update0(tag, key, index, value, settingName1, values1);
            } else if (settingNames2.contains(key)) {
                update0(tag, key, index, value, settingName2, values2);
            }
        }

        private void update0(String tag, String key, int index, String value, String settingName, Map<String, String> values) {
            if (value == null) {
                logger.warn("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                        "' is null, setting '" + settingName + "' of client '" + tag + "' stay the same");
                return;
            }
            if (value.equals(values.get(tag))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                            "' is the same as the old value, setting '" + settingName + "' of client '" + tag + "' stay the same");
                }
                return;
            }
            values.put(tag, value);
            tagToBeUpdated.add(tag);
        }

        @Override
        public final void applySettings(Map<String, HttpClient> clients) {
            for (String tag : tagToBeUpdated) {
                HttpClient client = clients.get(tag);
                if (client == null) {
                    // impossible
                    continue;
                }
                String value;
                if (!CheckUtils.isEmptyOrBlank((value = values1.get(tag)))) {
                    // Apply setting 1
                    apply0(client, tag, value, settingName1, this::applySetting1);
                } else if (!CheckUtils.isEmptyOrBlank((value = values2.get(tag)))) {
                    // Apply setting 2
                    apply0(client, tag, value, settingName2, this::applySetting2);
                } else {
                    // Reset to default
                    logger.info("HttpClients SettingsUpdate | Update setting '" + settingName1 + "' of client '" + tag + "' to default");
                    try {
                        client.setUpdated(true);
                        applyReset(client);
                    } catch (Throwable t) {
                        logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName1 +
                                "' of client '" + tag + "' to default", t);
                    }
                }
            }
            tagToBeUpdated.clear();
        }

        private void apply0(HttpClient client, String tag, String value, String settingName, ThrowableBiConsumer<HttpClient, String> applyMethod) {
            logger.info("HttpClients SettingsUpdate | Update setting '" + settingName + "' of client '" + tag + "' to '" + value + "'");
            try {
                client.setUpdated(true);
                applyMethod.accept(client, value);
            } catch (NumberFormatException e) {
                logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName +
                        "' of client '" + tag + "' to '" + value + "', number format failed", e);
            } catch (Throwable t) {
                if (t instanceof SimpleKeyValueEncoder.DecodeException) {
                    logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName +
                            "' of client '" + tag + "' to '" + value +
                            "', illegal key-value format, see github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md", t);
                } else {
                    logger.error("HttpClients SettingsUpdate | Error while updating setting '" + settingName +
                            "' of client '" + tag + "' to '" + value + "'", t);
                }
            }
        }

        @Override
        public Collection<String> getKeys() {
            List<String> keys = new ArrayList<>();
            keys.addAll(settingNames1);
            keys.addAll(settingNames2);
            return keys;
        }

        public abstract void applySetting1(HttpClient client, String value) throws Exception;

        public abstract void applySetting2(HttpClient client, String value) throws Exception;

        public abstract void applyReset(HttpClient client) throws Exception;

    }

    /**
     * 若第一个单值存在, 则第一个单值生效,
     * 若第一个单值为空, 则尝试第二个数组,
     * 若第二个数组存在, 则第二个数组生效,
     * 若第二个数组为空, 则重置参数(恢复默认设置)
     */
    private static abstract class SingleOrArrayValueUpdater implements Updater {

        private final String singleSettingName;
        private final String arraySettingName;
        private final List<String> singleSettingNames;
        private final List<String> arraySettingNames;
        private final Map<String, String> singleValues = new HashMap<>();
        private final Map<String, Map<Integer, String>> oldArrayValues = new HashMap<>();
        private final Map<String, Map<Integer, String>> newArrayValues = new HashMap<>();
        private final Set<String> tagToBeUpdated = new HashSet<>();

        public SingleOrArrayValueUpdater(List<String> singleSettingNames, List<String> arraySettingNames) {
            this.singleSettingName = singleSettingNames.get(0);
            this.arraySettingName = arraySettingNames.get(0);
            this.singleSettingNames = singleSettingNames;
            this.arraySettingNames = arraySettingNames;
        }

        @Override
        public final void updateSetting(String tag, String key, int index, String value) {
            if (singleSettingNames.contains(key)) {
                updateSingleSetting(tag, key, index, value);
            } else if (arraySettingNames.contains(key)) {
                updateArraySetting(tag, key, index, value);
            }
        }

        public final void updateSingleSetting(String tag, String key, int index, String value) {
            if (value == null) {
                logger.warn("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                        "' is null, setting '" + singleSettingName + "' of client '" + tag + "' stay the same");
                return;
            }
            if (value.equals(singleValues.get(tag))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                            "' is the same as the old value, setting '" + singleSettingName + "' of client '" + tag + "' stay the same");
                }
                return;
            }
            singleValues.put(tag, value);
            tagToBeUpdated.add(tag);
        }

        public final void updateArraySetting(String tag, String key, int index, String value) {
            if (value == null) {
                logger.warn("HttpClients SettingsUpdate | Value of properties key '" + toPropertyName(tag, key, index) +
                        "' is null, setting '" + arraySettingName + "' of client '" + tag + "' stay the same");
                return;
            }
            if (index < 0 || index > ARRAY_MAX_SIZE) {
                logger.warn("HttpClients SettingsUpdate | The array index of properties key '" + toPropertyName(tag, key, index) +
                        "' > " + ARRAY_MAX_SIZE + " or < 0. skipped!");
                return;
            }
            newArrayValues.computeIfAbsent(tag, m -> new TreeMap<>()).put(index, value);
        }

        @Override
        public final void applySettings(Map<String, HttpClient> clients) {
            // check if array value changed
            for (Map.Entry<String, Map<Integer, String>> entry : newArrayValues.entrySet()) {
                String tag = entry.getKey();
                if (!entry.getValue().equals(oldArrayValues.get(tag))) {
                    tagToBeUpdated.add(tag);
                }
            }

            for (String tag : tagToBeUpdated) {
                HttpClient client = clients.get(tag);
                if (client == null) {
                    // impossible
                    continue;
                }
                String value = singleValues.get(tag);
                Map<Integer, String> arrayValue;
                // Apply single value if it's not empty
                if (!CheckUtils.isEmptyOrBlank(value)) {
                    // Apply single value if it's not empty
                    logger.info("HttpClients SettingsUpdate | Update setting '" + singleSettingName + "' of client " + tag + " to '" + value + "'");
                    try {
                        client.setUpdated(true);
                        applySingleSetting(client, value);
                    } catch (NumberFormatException e) {
                        logger.error("HttpClients SettingsUpdate | Error while updating setting '" + singleSettingName +
                                "' of client '" + tag + "' to '" + value + "', number format failed", e);
                    } catch (Throwable t) {
                        if (t instanceof SimpleKeyValueEncoder.DecodeException) {
                            logger.error("HttpClients SettingsUpdate | Error while updating setting '" + singleSettingName +
                                    "' of client '" + tag + "' to '" + value +
                                    "', illegal key-value format, see github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md", t);
                        } else {
                            logger.error("HttpClients SettingsUpdate | Error while updating setting '" + singleSettingName +
                                    "' of client '" + tag + "' to '" + value + "'", t);
                        }
                    }
                } else if ((arrayValue = newArrayValues.get(tag)) != null) {
                    // Apply array value
                    logger.info("HttpClients SettingsUpdate | Update setting '" + arraySettingName + "' of client '" + tag + "' to '" + arrayValue + "'");
                    try {
                        client.setUpdated(true);
                        applyArraySetting(client, arrayValue.values().toArray(new String[0]));
                    } catch (NumberFormatException e) {
                        logger.error("HttpClients SettingsUpdate | Error while updating setting '" + arraySettingName +
                                "' of client '" + tag + "' to '" + value + "', number format failed", e);
                    } catch (Throwable t) {
                        if (t instanceof SimpleKeyValueEncoder.DecodeException) {
                            logger.error("HttpClients SettingsUpdate | Error while updating setting '" + arraySettingName +
                                    "' of client '" + tag + "' to '" + value +
                                    "', illegal key-value format, see github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md", t);
                        } else {
                            logger.error("HttpClients SettingsUpdate | Error while updating setting '" + arraySettingName +
                                    "' of client '" + tag + "' to '" + value + "'", t);
                        }
                    }
                } else {
                    // Reset to default
                    logger.info("HttpClients SettingsUpdate | Update setting '" + arraySettingName + "' of client '" + tag + "' to default");
                    try {
                        client.setUpdated(true);
                        applyReset(client);
                    } catch (Throwable t) {
                        logger.error("HttpClients SettingsUpdate | Error while updating setting '" + arraySettingName +
                                "' of client '" + tag + "' to default", t);
                    }
                }
            }
            oldArrayValues.putAll(newArrayValues);
            newArrayValues.clear();
            tagToBeUpdated.clear();
        }

        @Override
        public Collection<String> getKeys() {
            List<String> keys = new ArrayList<>();
            keys.addAll(singleSettingNames);
            keys.addAll(arraySettingNames);
            return keys;
        }

        public abstract void applySingleSetting(HttpClient client, String value) throws Exception;

        public abstract void applyArraySetting(HttpClient client, String[] value) throws Exception;

        public abstract void applyReset(HttpClient client) throws Exception;

    }

}
