package org.jstart.carrot.config.support;

import io.grpc.stub.StreamObserver;
import org.jstart.carrot.config.EResultCode;
import org.jstart.carrot.config.comm.ConfigListener;
import org.jstart.carrot.config.comm.ServerKey;
import org.jstart.carrot.config.comm.Snapshot;
import org.jstart.carrot.config.comm.Subscription;
import org.jstart.carrot.config.config.CarrotConfigProperties;
import org.jstart.carrot.config.grpc.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CarrotConfigService implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CarrotConfigService.class);



    private final CarrotConfigClient client;
    private final CarrotConfigProperties properties;
    private final CarrotConfigRepository repository;

    private final Set<ServerKey> watchedKeys;
    private final ConcurrentHashMap<ServerKey, CopyOnWriteArrayList<ConfigListener>> listenersByKey;

    private final Object watchLock = new Object();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private volatile ScheduledExecutorService watchScheduler;
    private volatile ScheduledFuture<?> watchTask;
    private volatile boolean running = true;
    private volatile StreamObserver<Config.WatchRequest> watchRequestObserver;
    private volatile CarrotConfigClient.HostPort currentAdmin;

    public CarrotConfigService(CarrotConfigClient client, CarrotConfigProperties properties) {
        this.client = Objects.requireNonNull(client, "client");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.repository = new CarrotConfigRepository();
        this.watchedKeys = ConcurrentHashMap.newKeySet();
        this.listenersByKey = new ConcurrentHashMap<>();
    }

    public CarrotConfigRepository getRepository() {
        return repository;
    }

    /**
     * 获取配置
     * @param namespace
     * @param group
     * @param dataId
     * @return
     */
    public Snapshot get(String namespace, String group, String dataId) {
        ServerKey key = normalizeKey(namespace, group, dataId);
        Snapshot snapshot = repository.get(key);
        if(snapshot != null){
            return snapshot;
        }
        return fetchAndCache(key);
    }

    /**
     * 订阅
     * @param namespace
     * @param group
     * @param dataId
     * @param listener
     * @return
     */
    public Subscription subscribe(String namespace, String group, String dataId, ConfigListener listener) {
        ServerKey key = normalizeKey(namespace, group, dataId);
        if (listener != null) {
            listenersByKey.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
        } else {
            listenersByKey.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        }
        watchedKeys.add(key);
        ensureStarted();
        fetchAndNotify(key);
        return new SubscriptionImpl(key, listener);
    }

    private void ensureStarted() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        synchronized (watchLock) {
            if (!running || watchedKeys.isEmpty()) {
                started.set(false);
                return;
            }
            ensureWatchSchedulerLocked();
            ensureWatchStream();
        }
    }

    private void safeSendWatchRequest() {
        if (!running) {
            return;
        }
        try {
            sendWatchRequest();
        } catch (Exception e) {
            logger.warn("carrot config api watch tick error: {}", e.getMessage());
        }
    }

    private void sendWatchRequest() {
        StreamObserver<Config.WatchRequest> observer = watchRequestObserver;
        if (observer == null) {
            ensureWatchStream();
            observer = watchRequestObserver;
        }
        if (observer == null) {
            return;
        }

        List<ServerKey> keys = List.copyOf(watchedKeys);
        if (keys.isEmpty()) {
            return;
        }
        List<Config.ConfigMeta> metas = new ArrayList<>(keys.size());
        for (ServerKey key : keys) {
            Snapshot snapshot = repository.get(key);
            long lastModified = snapshot == null ? 0L : snapshot.lastModified();
            metas.add(Config.ConfigMeta.newBuilder()
                    .setKey(toProtoKey(key))
                    .setLastModified(lastModified)
                    .build());
        }

        Config.WatchRequest request = Config.WatchRequest.newBuilder()
                .setAccessToken(nullToEmpty(properties.getAccessToken()))
                .addAllConfigs(metas)
                .build();

        try {
            observer.onNext(request);
        } catch (Exception e) {
            resetWatchStream(e.getMessage());
        }
    }

    private void ensureWatchStream() {
        synchronized (watchLock) {
            if (!running || watchedKeys.isEmpty()) {
                return;
            }
            if (watchRequestObserver != null) {
                return;
            }
            this.watchRequestObserver = client.openWatchStream(new StreamObserver<>() {
                @Override
                public void onNext(Config.WatchResponse value) {
                    if (value.getCode() == EResultCode.SUCCESS.getCode()) {
                        if (value.getChangedCount() > 0) {
                            handleChanged(value.getChangedList());
                        }
                        return;
                    }
                    if (value.getCode() == EResultCode.UNAUTHORIZED.getCode()) {
                        logger.warn("carrot config api watch unauthorized");
                        resetWatchStream("unauthorized");
                        return;
                    }
                    logger.warn("carrot config api watch response error: {} {}", value.getCode(), value.getMessage());
                }

                @Override
                public void onError(Throwable t) {
                    resetWatchStream(t == null ? "unknown" : t.getMessage());
                }

                @Override
                public void onCompleted() {
                    resetWatchStream("completed");
                }
            });
            this.currentAdmin = client.getCurrentNode();
        }
    }

    private void resetWatchStream(String reason) {
        synchronized (watchLock) {
            if (watchRequestObserver == null) {
                return;
            }
            closeWatchStreamLocked();
        }

        if (!running || watchedKeys.isEmpty()) {
            return;
        }
        CarrotConfigClient.HostPort excluded = currentAdmin;
        try {
            client.switchToNextRandomServerNode(excluded);
        } catch (Exception e) {
            logger.warn("carrot config api watch switch node error: {}", e.getMessage());
        }
        ensureWatchStream();
        logger.warn("carrot config api watch reset: {}", reason);
    }

    private void handleChanged(List<Config.ConfigMeta> changedList) {
        if (changedList == null || changedList.isEmpty()) {
            return;
        }
        for (Config.ConfigMeta meta : changedList) {
            if (meta == null || meta.getKey() == null) {
                continue;
            }
            ServerKey key = new ServerKey(
                    normalizeNamespace(meta.getKey().getNamespace()),
                    normalizeGroup(meta.getKey().getGroup()),
                    meta.getKey().getDataId() == null ? "" : meta.getKey().getDataId().trim());
            if (key.dataId().isEmpty()) {
                continue;
            }
            if (!watchedKeys.contains(key)) {
                continue;
            }
            fetchAndNotify(key);
        }
    }

    private Snapshot fetchAndCache(ServerKey key) {
        Config.ConfigResponse response = client.getConfig(new ServerKey(key.namespace(), key.group(), key.dataId()));
        if (response.getCode() != EResultCode.SUCCESS.getCode()) {
            logger.warn("carrot config api fetch failed: {} {} {} - {} {}",
                    key.namespace(), key.group(), key.dataId(), response.getCode(), response.getMessage());
            return null;
        }
        Snapshot next = new Snapshot(
                response.getContent(),
                response.getMd5(),
                response.getLastModified(),
                response.getContentType());
        repository.put(key, next);
        return next;
    }

    private void fetchAndNotify(ServerKey key) {
        Snapshot old = repository.get(key);
        Snapshot next = fetchAndCache(key);
        if (next == null) {
            return;
        }
        if (old != null && old.lastModified() == next.lastModified()) {
            return;
        }
        CopyOnWriteArrayList<ConfigListener> listeners = listenersByKey.get(key);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (ConfigListener listener : listeners) {
            if (listener == null) {
                continue;
            }
            try {
                listener.onChanged(key, old, next);
            } catch (Exception e) {
                logger.warn("carrot config api listener error: {} {} {} - {}",
                        key.namespace(), key.group(), key.dataId(), e.getMessage());
            }
        }
    }

    private static ServerKey normalizeKey(String namespace, String group, String dataId) {
        String ns = normalizeNamespace(namespace);
        String g = normalizeGroup(group);
        String did = dataId == null ? "" : dataId.trim();
        if (did.isEmpty()) {
            throw new IllegalArgumentException("dataId is required");
        }
        return new ServerKey(ns, g, did);
    }

    private static String normalizeNamespace(String namespace) {
        String value = namespace == null ? "" : namespace.trim();
        return value.isEmpty() ? "public" : value;
    }

    private static String normalizeGroup(String group) {
        String value = group == null ? "" : group.trim();
        return value.isEmpty() ? "DEFAULT_GROUP" : value;
    }

    private static Config.ConfigKey toProtoKey(ServerKey key) {
        return Config.ConfigKey.newBuilder()
                .setNamespace(nullToEmpty(key.namespace()))
                .setGroup(nullToEmpty(key.group()))
                .setDataId(nullToEmpty(key.dataId()))
                .build();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void ensureWatchSchedulerLocked() {
        if (watchTask != null && !watchTask.isCancelled() && !watchTask.isDone()) {
            return;
        }
        if (watchScheduler == null || watchScheduler.isShutdown()) {
            watchScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "carrot-config-api-watch");
                t.setDaemon(true);
                return t;
            });
        }
        long interval = Math.max(1000L, properties.getWatchIntervalMillis());
        watchTask = watchScheduler.scheduleAtFixedRate(this::safeSendWatchRequest, interval, interval,
                TimeUnit.MILLISECONDS);
    }

    private void stopWatchingIfIdle() {
        if (!watchedKeys.isEmpty()) {
            return;
        }
        synchronized (watchLock) {
            if (!watchedKeys.isEmpty()) {
                return;
            }
            closeWatchStreamLocked();
            if (watchTask != null) {
                watchTask.cancel(true);
                watchTask = null;
            }
            if (watchScheduler != null) {
                watchScheduler.shutdownNow();
                watchScheduler = null;
            }
            currentAdmin = null;
            started.set(false);
        }
    }

    private void closeWatchStreamLocked() {
        StreamObserver<Config.WatchRequest> observer = watchRequestObserver;
        watchRequestObserver = null;
        if (observer == null) {
            return;
        }
        try {
            observer.onCompleted();
        } catch (Exception e) {
            logger.warn("carrot config api watch close error: {}", e.getMessage());
        }
    }

    @Override
    public void destroy() {
        running = false;
        synchronized (watchLock) {
            closeWatchStreamLocked();
            if (watchTask != null) {
                watchTask.cancel(true);
                watchTask = null;
            }
            try {
                if (watchScheduler != null) {
                    watchScheduler.shutdownNow();
                }
            } catch (Exception e) {
                logger.warn("carrot config api watch scheduler shutdown error: {}", e.getMessage());
            } finally {
                watchScheduler = null;
                started.set(false);
            }
        }
    }

    private class SubscriptionImpl implements Subscription {
        private final ServerKey key;
        private final ConfigListener listener;
        private volatile boolean closed;

        private SubscriptionImpl(ServerKey  key, ConfigListener listener) {
            this.key = key;
            this.listener = listener;
        }

        @Override
        public ServerKey key() {
            return key;
        }

        @Override
        public Snapshot snapshot() {
            return repository.get(key);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            CopyOnWriteArrayList<ConfigListener> list = listenersByKey.get(key);
            if (list != null && listener != null) {
                list.remove(listener);
            }
            if (list == null || list.isEmpty()) {
                listenersByKey.remove(key);
                watchedKeys.remove(key);
                stopWatchingIfIdle();
            }
        }
    }
}
