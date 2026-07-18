package org.jstart.carrot.config.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

/**
 * Carrot Config 的 Spring 内部刷新器。
 * <p>
 * 监听 {@link CarrotConfigChangedEvent}，将变更后的配置写入 {@link CarrotConfigPropertySource}，
 * 并尝试刷新 Bean 中使用 {@link Value} 注入的字段值。
 * </p>
 */
public class CarrotConfigSpringRefresher implements ApplicationListener<CarrotConfigChangedEvent> {
    private final ApplicationContext applicationContext;
    private final ConfigurableEnvironment environment;

    /**
     * @param applicationContext Spring 上下文
     * @param environment        Spring Environment
     */
    public CarrotConfigSpringRefresher(ApplicationContext applicationContext, ConfigurableEnvironment environment) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    /**
     * 收到配置变更事件时，更新 PropertySource 并刷新 {@link Value} 注入字段。
     *
     * @param event 变更事件
     */
    @Override
    public void onApplicationEvent(CarrotConfigChangedEvent event) {
        if (event == null || event.getKey() == null || event.getNewSnapshot() == null) {
            return;
        }

        Map<String, Object> props = CarrotConfigContentParser.parseToProperties(
                event.getKey().dataId(),
                event.getNewSnapshot().content(),
                event.getNewSnapshot().contentType()
        );
        if (!props.isEmpty()) {
            CarrotConfigPropertySource ps = findOrCreate(environment.getPropertySources());
            ps.putAll(props);
        }

        refreshValueFields();
    }

    /**
     * 遍历所有 Bean，尝试刷新其 {@link Value} 注入字段。
     */
    private void refreshValueFields() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        ConversionService conversionService = null;
        try {
            conversionService = applicationContext.getBean(ConversionService.class);
        } catch (Exception ignored) {
        }

        for (String beanName : beanNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception ignored) {
                continue;
            }
            if (bean == null) {
                continue;
            }
            try {
                refreshBean(bean, conversionService);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 刷新指定 Bean 上标注 {@link Value} 的非 static、非 final 字段。
     *
     * @param bean              目标 Bean
     * @param conversionService 类型转换服务（可为 null）
     */
    private void refreshBean(Object bean, ConversionService conversionService) {
        Class<?> type = bean.getClass();
        while (type != null && type != Object.class) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (field == null) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                Value ann = field.getAnnotation(Value.class);
                if (ann == null) {
                    continue;
                }
                String expr = ann.value();
                if (expr == null || expr.isBlank()) {
                    continue;
                }
                String resolved = environment.resolvePlaceholders(expr);
                Object value = convert(resolved, field.getType(), conversionService);
                try {
                    field.setAccessible(true);
                    field.set(bean, value);
                } catch (Exception ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    /**
     * 将字符串转换为目标类型。
     *
     * @param value             原始字符串
     * @param targetType        目标类型
     * @param conversionService Spring ConversionService（可为 null）
     * @return 转换后的值（无法转换时可能返回 null 或默认值）
     */
    private static Object convert(String value, Class<?> targetType, ConversionService conversionService) {
        if (targetType == String.class) {
            return value;
        }
        if (conversionService != null && conversionService.canConvert(String.class, targetType)) {
            try {
                return conversionService.convert(value, targetType);
            } catch (Exception ignored) {
            }
        }
        String v = value == null ? "" : value.trim();
        if (targetType == int.class || targetType == Integer.class) {
            try {
                return Integer.parseInt(v);
            } catch (Exception ignored) {
                return 0;
            }
        }
        if (targetType == long.class || targetType == Long.class) {
            try {
                return Long.parseLong(v);
            } catch (Exception ignored) {
                return 0L;
            }
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(v);
        }
        return null;
    }

    /**
     * 从环境中查找已有的 {@link CarrotConfigPropertySource}，不存在则创建并置于最高优先级。
     *
     * @param sources 环境的 PropertySources
     * @return 可写入配置的 CarrotConfigPropertySource
     */
    private static CarrotConfigPropertySource findOrCreate(MutablePropertySources sources) {
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
}
