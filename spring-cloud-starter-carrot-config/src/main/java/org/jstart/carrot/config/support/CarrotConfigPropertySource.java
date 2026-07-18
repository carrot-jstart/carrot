package org.jstart.carrot.config.support;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carrot Config 的 PropertySource 实现。
 * <p>
 * 用于将从配置中心拉取到的键值对，注入到 Spring Environment 中，以便通过
 * {@code @Value} / {@code Environment#getProperty} 等方式读取。
 * </p>
 */
public class CarrotConfigPropertySource extends MapPropertySource {
    /**
     * PropertySource 在 Spring Environment 中的名称。
     */
    public static final String NAME = "carrotConfig";

    /**
     * 创建一个使用线程安全 Map 作为底层存储的 PropertySource。
     */
    public CarrotConfigPropertySource() {
        super(NAME, new ConcurrentHashMap<>());
    }

    /**
     * 获取底层可变 Map 存储。
     *
     * @return 用于存储配置键值对的 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> map() {
        return (Map<String, Object>) this.source;
    }

    /**
     * 批量写入配置键值对。
     * <p>
     * 传入空或空集合时不做任何处理。
     * </p>
     *
     * @param props 待写入的配置键值对
     */
    public void putAll(Map<String, Object> props) {
        if (props == null || props.isEmpty()) {
            return;
        }
        map().putAll(props);
    }
}
