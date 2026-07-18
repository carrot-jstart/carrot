package org.jstart.carrot.scheduling.config;

import org.jstart.carrot.scheduling.constant.IpUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调度执行器配置项。
 * <p>
 * 通过 {@code spring.cloud.carrot.scheduling.executor.*} 注入，包含执行器身份信息、端口以及线程池参数等。
 * </p>
 */
@ConfigurationProperties("spring.cloud.carrot.scheduling.executor")
public class SchedulingExecutorConfig {
    /**
     * 执行器名称
     */
    private String name = "defaut_excutor";

    /**
     * ip
     */
    private String ip = IpUtil.LOCAL_IP;

    /**
     * 端口
     */
    private Integer port = 9003;

    /**
     * 命名空间
     */
    private String namespace = "public";

    /**
     * 组
     */
    private String group = "default";

    /**
     * 线程核心线程
     */
    private Integer threadPoolCorePoolSize = 1;

    /**
     * 最大线程数
     */
    private Integer threadPoolMaxPoolSize = 4;

    /**
     * 空闲线程存活时间
     */
    private Integer threadPoolKeepAliveTime = 60;

    /**
     * 有界队列容量
     */
    private Integer threadPoolQueueCapacity = 100;

    /**
     * 线程权重
     */
    private Double threadWeight = 1.0;

    /**
     * 队列权重
     */
    private Double queueWeight = 1.0;

    public SchedulingExecutorConfig() {
    }

    /**
     * @param name                    执行器名称
     * @param ip                      执行器 IP
     * @param port                    执行器端口
     * @param namespace               命名空间
     * @param group                   组
     * @param threadPoolCorePoolSize  核心线程数
     * @param threadPoolMaxPoolSize   最大线程数
     * @param threadPoolKeepAliveTime 空闲线程存活时间（毫秒）
     * @param threadPoolQueueCapacity 有界队列容量
     * @param threadWeight            线程权重
     * @param queueWeight             队列权重
     */
    public SchedulingExecutorConfig(String name, String ip, Integer port, String namespace, String group,
            Integer threadPoolCorePoolSize,
            Integer threadPoolMaxPoolSize,
            Integer threadPoolKeepAliveTime,
            Integer threadPoolQueueCapacity,
            Double threadWeight,
            Double queueWeight) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.namespace = namespace;
        this.group = group;
        this.threadPoolCorePoolSize = threadPoolCorePoolSize;
        this.threadPoolMaxPoolSize = threadPoolMaxPoolSize;
        this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
        this.threadPoolQueueCapacity = threadPoolQueueCapacity;
        this.threadWeight = threadWeight;
        this.queueWeight = queueWeight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getThreadPoolCorePoolSize() {
        return threadPoolCorePoolSize;
    }

    public void setThreadPoolCorePoolSize(Integer threadPoolCorePoolSize) {
        this.threadPoolCorePoolSize = threadPoolCorePoolSize;
    }

    public Integer getThreadPoolMaxPoolSize() {
        return threadPoolMaxPoolSize;
    }

    public void setThreadPoolMaxPoolSize(Integer threadPoolMaxPoolSize) {
        this.threadPoolMaxPoolSize = threadPoolMaxPoolSize;
    }

    public Integer getThreadPoolKeepAliveTime() {
        return threadPoolKeepAliveTime;
    }

    public void setThreadPoolKeepAliveTime(Integer threadPoolKeepAliveTime) {
        this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
    }

    public Integer getThreadPoolQueueCapacity() {
        return threadPoolQueueCapacity;
    }

    public void setThreadPoolQueueCapacity(Integer threadPoolQueueCapacity) {
        this.threadPoolQueueCapacity = threadPoolQueueCapacity;
    }

    public Double getThreadWeight() {
        return threadWeight;
    }

    public void setThreadWeight(Double threadWeight) {
        this.threadWeight = threadWeight;
    }

    public Double getQueueWeight() {
        return queueWeight;
    }

    public void setQueueWeight(Double queueWeight) {
        this.queueWeight = queueWeight;
    }
}
