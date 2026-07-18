package org.jstart.carrot.config.support;

import org.jstart.carrot.config.comm.ServerKey;
import org.jstart.carrot.config.comm.Snapshot;
import org.jstart.carrot.config.config.CarrotConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Spring Boot 启动早期的 Carrot Config 加载器。
 * <p>
 * 在 {@link org.springframework.context.ApplicationContext} 创建之前，从 Carrot Config
 * Server
 * 拉取配置内容并合并为键值对，注入到 {@link ConfigurableEnvironment} 的 PropertySources 中。
 * 典型用途是提前加载 {@code server.port} 等需要在容器创建前生效的配置。
 * </p>
 */
public class CarrotConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(CarrotConfigEnvironmentPostProcessor.class);

    /**
     * 根据环境变量配置决定是否启用早期加载，并将远端配置写入 {@link CarrotConfigPropertySource}。
     *
     * @param environment 当前可配置环境
     * @param application Spring Boot 应用
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
            org.springframework.boot.SpringApplication application) {
        String enabled = environment.getProperty("spring.cloud.carrot.config.enabled", "true");
        if (!Boolean.parseBoolean(enabled)) {
            return;
        }
        String serverAddr = environment.getProperty("spring.cloud.carrot.config.server-addr");
        if (serverAddr == null || serverAddr.isBlank()) {
            return;
        }
        List<ServerKey> files = resolveFiles(environment);
        if (files.isEmpty()) {
            return;
        }

        CarrotConfigProperties props = new CarrotConfigProperties();
        props.setRefreshEnabled(true);
        props.setServerAddr(serverAddr);
        props.setAccessToken(environment.getProperty("spring.cloud.carrot.config.access-token"));
        props.setFiles(files);

        Map<String, Object> merged = new HashMap<>();
        CarrotConfigClient client = null;
        CarrotConfigService subscriber = null;
        try {
            client = new CarrotConfigClient(props);
            subscriber = new CarrotConfigService(client, props);
            List<ServerKey> keys = files.stream()
                    .map(item -> new ServerKey(normalizeNamespace(item.namespace()),
                            normalizeGroup(item.group()), item.dataId()))
                    .toList();
            for (ServerKey key : keys) {
                Snapshot snapshot = subscriber.get(key.namespace(), key.group(), key.dataId());
                if (snapshot == null) {
                    continue;
                }
                merged.putAll(CarrotConfigContentParser.parseToProperties(
                        key.dataId(),
                        snapshot.content(),
                        snapshot.contentType()));
            }
        } catch (Exception e) {
            logger.warn("carrot config early load failed", e);
        } finally {
            if (subscriber != null) {
                try {
                    subscriber.destroy();
                } catch (Exception e) {
                    logger.warn("carrot config early load destroy subscriber failed", e);
                }
            }
            if (client != null) {
                try {
                    client.destroy();
                } catch (Exception e) {
                    logger.warn("carrot config early load destroy client failed", e);
                }
            }
        }

        if (merged.isEmpty()) {
            return;
        }

        MutablePropertySources sources = environment.getPropertySources();
        CarrotConfigPropertySource ps = findOrCreate(sources);
        ps.putAll(merged);
        Object sp = merged.get("server.port");
        if (sp != null) {
            logger.info("carrot config early loaded server.port={}", sp);
        }
    }

    /**
     * 设置本处理器的执行顺序，确保尽早运行以便影响后续配置解析。
     *
     * @return Ordered 顺序值
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    /**
     * 从环境中查找已有的 {@link CarrotConfigPropertySource}，不存在则创建并置于最高优先级。
     *
     * @param sources 环境的 PropertySources
     * @return 可写入配置的 CarrotConfigPropertySource
     */
    private static CarrotConfigPropertySource findOrCreate(MutablePropertySources sources) {
        Objects.requireNonNull(sources, "sources");
        if (sources.contains(CarrotConfigPropertySource.NAME)) {
            Object existing = sources.get(CarrotConfigPropertySource.NAME);
            if (existing instanceof CarrotConfigPropertySource cps) {
                return cps;
            }
        }
        CarrotConfigPropertySource ps = new CarrotConfigPropertySource();
        sources.addFirst(ps);
        return ps;
    }

    /**
     * 规范化 namespace，空值回落到 public。
     *
     * @param namespace 原始 namespace
     * @return 规范化后的 namespace
     */
    private static String normalizeNamespace(String namespace) {
        String value = namespace == null ? "" : namespace.trim();
        return value.isEmpty() ? "public" : value;
    }

    /**
     * 规范化 group，空值回落到 DEFAULT_GROUP。
     *
     * @param group 原始 group
     * @return 规范化后的 group
     */
    private static String normalizeGroup(String group) {
        String value = group == null ? "" : group.trim();
        return value.isEmpty() ? "DEFAULT_GROUP" : value;
    }

    private static List<ServerKey> resolveFiles(ConfigurableEnvironment environment) {
        Objects.requireNonNull(environment, "environment");
        try {
            Binder binder = Binder.get(environment);
            List<ServerKey> bound = binder
                    .bind("spring.cloud.carrot.config.files", Bindable.listOf(ServerKey.class)).orElse(null);
            if (bound != null && !bound.isEmpty()) {
                return bound;
            }
            List<String> asStrings = binder.bind("spring.cloud.carrot.config.files", Bindable.listOf(String.class))
                    .orElse(null);
            if (asStrings != null && !asStrings.isEmpty()) {
                return asStrings.stream()
                        .map(CarrotConfigEnvironmentPostProcessor::parseConfigDataId)
                        .filter(Objects::nonNull)
                        .toList();
            }
        } catch (Exception e) {
            logger.warn("carrot config early load bind files failed", e);
        }

        Object raw = environment.getProperty("spring.cloud.carrot.config.files", Object.class);
        if (raw instanceof List<?> list && !list.isEmpty()) {
            return list.stream()
                    .map(CarrotConfigEnvironmentPostProcessor::coerceConfigDataId)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private static ServerKey coerceConfigDataId(Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof ServerKey dataId) {
            return isBlank(dataId.dataId()) ? null : dataId;
        }
        if (item instanceof String text) {
            return parseConfigDataId(text);
        }
        if (item instanceof Map<?, ?> map) {
            String namespace = getMapString(map, "namespace");
            String group = getMapString(map, "group");
            String dataId = getMapString(map, "dataId");
            if (isBlank(dataId)) {
                return null;
            }
            return new ServerKey(namespace, group, dataId);
        }
        return null;
    }

    private static String getMapString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static ServerKey parseConfigDataId(String text) {
        if (isBlank(text)) {
            return null;
        }
        String s = text.trim();
        String[] parts = split3(s);
        if (parts == null) {
            return null;
        }
        if (parts.length == 1) {
            return new ServerKey(null, null, parts[0]);
        }
        if (parts.length == 2) {
            return new ServerKey(null, parts[0], parts[1]);
        }
        return new ServerKey(parts[0], parts[1], parts[2]);
    }

    private static String[] split3(String s) {
        String[] parts;
        if (s.contains("/")) {
            parts = s.split("/", 3);
        } else if (s.contains(":")) {
            parts = s.split(":", 3);
        } else if (s.contains(",")) {
            parts = s.split(",", 3);
        } else {
            parts = new String[] { s };
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i] == null ? null : parts[i].trim();
        }
        if (parts.length == 1) {
            return isBlank(parts[0]) ? null : parts;
        }
        if (parts.length == 2) {
            return isBlank(parts[0]) || isBlank(parts[1]) ? null : parts;
        }
        return isBlank(parts[0]) || isBlank(parts[1]) || isBlank(parts[2]) ? null : parts;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
