package org.jstart.carrot.dubbo.registry;

import io.grpc.stub.StreamObserver;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.rpc.RpcException;
import org.jstart.carrot.discovery.grpc.Discovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Carrot Discovery 的 Dubbo Registry SPI 实现。
 */
public class CarrotDubboRegistry extends FailbackRegistry {
    private static final Logger logger = LoggerFactory.getLogger(CarrotDubboRegistry.class);
    private static final String DEFAULT_NAMESPACE = "public";
    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    private static final String METADATA_DUBBO_URL = "dubbo.url";

    private final URL registryUrl;
    private final CarrotDubboDiscoveryClient discoveryClient;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<URL, RegistrationState> registrations = new ConcurrentHashMap<>();
    private final ConcurrentMap<SubscriptionKey, SubscriptionState> subscriptions = new ConcurrentHashMap<>();

    public CarrotDubboRegistry(URL url) {
        super(url);
        this.registryUrl = Objects.requireNonNull(url, "url");
        this.discoveryClient = new CarrotDubboDiscoveryClient(url);
        this.scheduler = Executors.newScheduledThreadPool(2, task -> {
            Thread thread = new Thread(task, "carrot-dubbo-registry");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
        for (SubscriptionState state : subscriptions.values()) {
            state.stop();
        }
        subscriptions.clear();
        for (RegistrationState state : registrations.values()) {
            state.stop(false);
        }
        registrations.clear();
        scheduler.shutdownNow();
        discoveryClient.destroy();
    }

    @Override
    public List<URL> lookup(URL url) {
        if (!isProviderCategory(url)) {
            return Collections.emptyList();
        }
        try {
            Discovery.ListInstancesResponse response = discoveryClient.listInstances(buildServiceKey(url));
            if (response.getCode() != EResultCode.SUCCESS.getCode()) {
                return Collections.emptyList();
            }
            return toProviderUrls(url, response.getInstancesList());
        } catch (Exception e) {
            logger.warn("carrot dubbo lookup failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void doRegister(URL url) {
        if (!isProviderCategory(url)) {
            logger.debug("carrot dubbo ignore register url: {}", url);
            return;
        }
        RegistrationState previous = registrations.remove(url);
        if (previous != null) {
            previous.stop(true);
        }
        RegistrationState state = new RegistrationState(url);
        try {
            state.registerAndOpenHeartbeat(null);
            state.start();
            registrations.put(url, state);
        } catch (Exception e) {
            state.stop(true);
            throw new RpcException("Failed to register service to carrot registry, url=" + url, e);
        }
    }

    @Override
    public void doUnregister(URL url) {
        RegistrationState state = registrations.remove(url);
        if (state != null) {
            state.stop(true);
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        if (!isProviderCategory(url)) {
            listener.notify(List.of(emptyUrl(url)));
            return;
        }
        SubscriptionKey key = new SubscriptionKey(url, listener);
        SubscriptionState state = new SubscriptionState(url, listener);
        SubscriptionState previous = subscriptions.put(key, state);
        if (previous != null) {
            previous.stop();
        }
        try {
            state.pollAndNotify();
            state.start();
        } catch (Exception e) {
            subscriptions.remove(key);
            state.stop();
            throw new RpcException("Failed to subscribe carrot registry, url=" + url, e);
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        SubscriptionState state = subscriptions.remove(new SubscriptionKey(url, listener));
        if (state != null) {
            state.stop();
        }
    }

    private boolean isProviderCategory(URL url) {
        String category = url.getParameter("category", "providers");
        if (category == null || category.isBlank()) {
            return true;
        }
        if ("*".equals(category)) {
            return true;
        }
        for (String item : category.split(",")) {
            if ("providers".equals(item == null ? "" : item.trim())) {
                return true;
            }
        }
        return false;
    }

    private Discovery.ServiceKey buildServiceKey(URL url) {
        String serviceName = url.getServiceInterface();
        if (serviceName == null || serviceName.isBlank()) {
            serviceName = url.getPath();
        }
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Dubbo service interface is empty: " + url);
        }
        return Discovery.ServiceKey.newBuilder()
                .setNamespace(resolveNamespace())
                .setGroup(resolveGroup(url))
                .setServiceName(serviceName)
                .build();
    }

    private String resolveNamespace() {
        String namespace = registryUrl.getParameter("namespace", "");
        if (!namespace.isBlank()) {
            return namespace;
        }
        String encoded = registryUrl.getParameter("carrotNamespaceB64", "");
        if (!encoded.isBlank()) {
            try {
                return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.warn("carrot dubbo decode namespace failed: {}", e.getMessage(), e);
            }
        }
        return DEFAULT_NAMESPACE;
    }

    private String resolveGroup(URL serviceUrl) {
        String group = serviceUrl.getParameter("group", "");
        if (group != null && !group.isBlank()) {
            return group;
        }
        group = registryUrl.getParameter("group", "");
        if (group != null && !group.isBlank()) {
            return group;
        }
        return DEFAULT_GROUP;
    }

    private List<URL> toProviderUrls(URL consumerUrl, List<Discovery.Instance> instances) {
        if (instances == null || instances.isEmpty()) {
            return List.of(emptyUrl(consumerUrl));
        }
        Map<String, URL> urls = new LinkedHashMap<>();
        for (Discovery.Instance instance : instances) {
            String rawUrl = instance.getMetadataOrDefault(METADATA_DUBBO_URL, "");
            if (rawUrl.isBlank()) {
                logger.warn("carrot dubbo provider metadata missing dubbo.url, instanceId={}, service={}",
                        instance.getInstanceId(), buildServiceKey(consumerUrl).getServiceName());
                continue;
            }
            try {
                URL providerUrl = URL.valueOf(rawUrl);
                boolean strictMatch = UrlUtils.isMatch(consumerUrl, providerUrl);
                if (!strictMatch && !relaxedMatch(consumerUrl, providerUrl)) {
                    logger.warn("carrot dubbo provider filtered, consumerUrl={}, providerUrl={}",
                            consumerUrl, providerUrl);
                    continue;
                }
                if (!strictMatch) {
                    logger.info("carrot dubbo provider accepted by relaxed match, providerUrl={}", providerUrl);
                }
                urls.put(providerUrl.toFullString(), providerUrl);
            } catch (Exception e) {
                logger.warn("carrot dubbo parse provider url failed, rawUrl={}, message={}", rawUrl, e.getMessage(), e);
            }
        }
        if (urls.isEmpty()) {
            return List.of(emptyUrl(consumerUrl));
        }
        return new ArrayList<>(urls.values());
    }

    private boolean relaxedMatch(URL consumerUrl, URL providerUrl) {
        if (providerUrl == null) {
            return false;
        }
        if (providerUrl.getParameter("disabled", false) || !providerUrl.getParameter("enabled", true)) {
            return false;
        }
        String consumerInterface = resolveServiceInterface(consumerUrl);
        String providerInterface = resolveServiceInterface(providerUrl);
        if (!consumerInterface.equals(providerInterface)) {
            return false;
        }
        if (!parameterMatches(consumerUrl, providerUrl, "group")) {
            return false;
        }
        if (!parameterMatches(consumerUrl, providerUrl, "version")) {
            return false;
        }
        return parameterMatches(consumerUrl, providerUrl, "classifier");
    }

    private boolean parameterMatches(URL consumerUrl, URL providerUrl, String key) {
        String consumerValue = consumerUrl.getParameter(key, "");
        if (consumerValue == null || consumerValue.isBlank()) {
            return true;
        }
        String providerValue = providerUrl.getParameter(key, "");
        return consumerValue.equals(providerValue);
    }

    private String resolveServiceInterface(URL url) {
        String serviceInterface = url.getServiceInterface();
        if (serviceInterface == null || serviceInterface.isBlank()) {
            serviceInterface = url.getPath();
        }
        return serviceInterface == null ? "" : serviceInterface;
    }

    private URL emptyUrl(URL consumerUrl) {
        String interfaceName = consumerUrl.getServiceInterface();
        if (interfaceName == null || interfaceName.isBlank()) {
            interfaceName = consumerUrl.getPath();
        }
        StringBuilder builder = new StringBuilder("empty://0.0.0.0/");
        builder.append(interfaceName == null ? "" : interfaceName);
        builder.append("?category=providers");
        String group = consumerUrl.getParameter("group", "");
        if (!group.isBlank()) {
            builder.append("&group=").append(group);
        }
        String version = consumerUrl.getParameter("version", "");
        if (!version.isBlank()) {
            builder.append("&version=").append(version);
        }
        return URL.valueOf(builder.toString());
    }

    private final class RegistrationState {
        private final URL serviceUrl;
        private final Discovery.ServiceKey serviceKey;
        private final String instanceId;
        private final String ip;
        private final int port;
        private final Discovery.Instance instance;
        private final long heartbeatIntervalMs;
        private volatile StreamObserver<Discovery.HeartbeatRequest> heartbeatObserver;
        private volatile CarrotDubboDiscoveryClient.HostPort currentNode;
        private volatile ScheduledFuture<?> heartbeatFuture;

        private RegistrationState(URL serviceUrl) {
            this.serviceUrl = serviceUrl;
            this.serviceKey = buildServiceKey(serviceUrl);
            this.instanceId = UUID.nameUUIDFromBytes(serviceUrl.toFullString().getBytes(StandardCharsets.UTF_8))
                    .toString();
            this.ip = serviceUrl.getHost();
            this.port = serviceUrl.getPort();
            this.heartbeatIntervalMs = serviceUrl.getParameter("refreshIntervalMs",
                    registryUrl.getParameter("refreshIntervalMs", 5000));

            Map<String, String> metadata = new LinkedHashMap<>(serviceUrl.getParameters());
            metadata.put(METADATA_DUBBO_URL, serviceUrl.toFullString());
            this.instance = Discovery.Instance.newBuilder()
                    .setInstanceId(instanceId)
                    .setIp(ip == null ? "" : ip)
                    .setPort(port)
                    .setWeight(serviceUrl.getParameter("weight", 1.0))
                    .putAllMetadata(metadata)
                    .build();
        }

        private void start() {
            long interval = Math.max(heartbeatIntervalMs, 1000L);
            heartbeatFuture = scheduler.scheduleAtFixedRate(this::heartbeat, interval, interval, TimeUnit.MILLISECONDS);
        }

        private void heartbeat() {
            StreamObserver<Discovery.HeartbeatRequest> observer = heartbeatObserver;
            if (observer == null) {
                try {
                    registerAndOpenHeartbeat(currentNode);
                    observer = heartbeatObserver;
                } catch (Exception e) {
                    logger.warn("carrot dubbo reconnect heartbeat failed: {}", e.getMessage(), e);
                    return;
                }
            }
            if (observer == null) {
                return;
            }
            try {
                observer.onNext(Discovery.HeartbeatRequest.newBuilder()
                        .setAccessToken(registryUrl.getParameter("accessToken", ""))
                        .setService(serviceKey)
                        .setInstanceId(instanceId)
                        .setIp(ip == null ? "" : ip)
                        .setPort(port)
                        .build());
            } catch (Exception e) {
                markDisconnected();
            }
        }

        private synchronized void registerAndOpenHeartbeat(CarrotDubboDiscoveryClient.HostPort excluded) {
            if (heartbeatObserver != null) {
                return;
            }

            // 初始注册时先尝试当前共享 channel，避免切换节点干扰其他 RegistrationState
            if (excluded == null) {
                try {
                    Discovery.Reply reply = discoveryClient.register(serviceKey, instance);
                    if (reply.getCode() == EResultCode.SUCCESS.getCode()) {
                        currentNode = discoveryClient.getCurrentNode();
                        heartbeatObserver = discoveryClient.openHeartbeatStream(new StreamObserver<>() {
                            @Override
                            public void onNext(Discovery.Reply value) {
                                if (value == null) {
                                    return;
                                }
                                if (value.getCode() == EResultCode.SUCCESS.getCode()) {
                                    return;
                                }
                                logger.warn("carrot dubbo heartbeat rejected: code={}, message={}",
                                        value.getCode(), value.getMessage());
                                markDisconnected();
                            }

                            @Override
                            public void onError(Throwable t) {
                                logger.warn("carrot dubbo heartbeat stream error: {}", t.getMessage());
                                markDisconnected();
                            }

                            @Override
                            public void onCompleted() {
                                logger.warn("carrot dubbo heartbeat stream completed");
                                markDisconnected();
                            }
                        });
                        return;
                    }
                } catch (Exception e) {
                    logger.debug("carrot dubbo register failed on current node, will retry on another: {}",
                            e.getMessage());
                }
            }

            // 当前 channel 不可用（或重连时），切换到其他节点再试
            try {
                discoveryClient.refreshServerNodes();
            } catch (Exception e) {
                logger.debug("carrot dubbo refresh server nodes failed: {}", e.getMessage(), e);
            }
            currentNode = excluded == null
                    ? discoveryClient.switchToRandomServerNode()
                    : discoveryClient.switchToNextRandomServerNode(excluded);

            Discovery.Reply reply = discoveryClient.register(serviceKey, instance);
            if (reply.getCode() != EResultCode.SUCCESS.getCode()) {
                throw new RpcException("Carrot register failed: " + reply.getMessage());
            }
            heartbeatObserver = discoveryClient.openHeartbeatStream(new StreamObserver<>() {
                @Override
                public void onNext(Discovery.Reply value) {
                    if (value == null) {
                        return;
                    }
                    if (value.getCode() == EResultCode.SUCCESS.getCode()) {
                        return;
                    }
                    logger.warn("carrot dubbo heartbeat rejected: code={}, message={}",
                            value.getCode(), value.getMessage());
                    markDisconnected();
                }

                @Override
                public void onError(Throwable t) {
                    logger.warn("carrot dubbo heartbeat stream error: {}", t.getMessage());
                    markDisconnected();
                }

                @Override
                public void onCompleted() {
                    logger.warn("carrot dubbo heartbeat stream completed");
                    markDisconnected();
                }
            });
        }

        private synchronized void markDisconnected() {
            if (heartbeatObserver != null) {
                try {
                    heartbeatObserver.onCompleted();
                } catch (Exception e) {
                    logger.debug("carrot dubbo close heartbeat stream failed: {}", e.getMessage(), e);
                }
            }
            heartbeatObserver = null;
        }

        private void stop(boolean deregister) {
            ScheduledFuture<?> future = heartbeatFuture;
            if (future != null) {
                future.cancel(true);
            }
            heartbeatFuture = null;
            markDisconnected();
            if (deregister) {
                try {
                    discoveryClient.deregister(serviceKey, instanceId, ip, port);
                } catch (Exception e) {
                    logger.warn("carrot dubbo deregister failed: {}", e.getMessage(), e);
                }
            }
        }
    }

    private final class SubscriptionState {
        private final URL consumerUrl;
        private final NotifyListener listener;
        private final Discovery.ServiceKey serviceKey;
        private final long pollIntervalMs;
        private volatile long lastVersion = Long.MIN_VALUE;
        private volatile String lastSignature = "";
        private volatile ScheduledFuture<?> pollFuture;

        private SubscriptionState(URL consumerUrl, NotifyListener listener) {
            this.consumerUrl = consumerUrl;
            this.listener = listener;
            this.serviceKey = buildServiceKey(consumerUrl);
            this.pollIntervalMs = consumerUrl.getParameter("pollIntervalMs",
                    registryUrl.getParameter("pollIntervalMs", 5000));
        }

        private void start() {
            long interval = Math.max(pollIntervalMs, 1000L);
            pollFuture = scheduler.scheduleAtFixedRate(this::safePoll, interval, interval, TimeUnit.MILLISECONDS);
        }

        private void safePoll() {
            try {
                pollAndNotify();
            } catch (Exception e) {
                logger.warn("carrot dubbo poll instances failed: {}", e.getMessage());
            }
        }

        private synchronized void pollAndNotify() {
            Discovery.ListInstancesResponse response = discoveryClient.listInstances(serviceKey);
            if (response.getCode() != EResultCode.SUCCESS.getCode()) {
                throw new RpcException("Carrot listInstances failed: " + response.getMessage());
            }
            List<URL> urls = toProviderUrls(consumerUrl, response.getInstancesList());
            String signature = buildSignature(urls);
            if (response.getVersion() == lastVersion && signature.equals(lastSignature)) {
                return;
            }
            lastVersion = response.getVersion();
            lastSignature = signature;
            listener.notify(urls);
        }

        private void stop() {
            ScheduledFuture<?> future = pollFuture;
            if (future != null) {
                future.cancel(true);
            }
            pollFuture = null;
        }
    }

    private record SubscriptionKey(URL url, NotifyListener listener) {
    }

    private String buildSignature(List<URL> urls) {
        StringBuilder builder = new StringBuilder();
        for (URL url : urls) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(url.toFullString());
        }
        return builder.toString();
    }
}
