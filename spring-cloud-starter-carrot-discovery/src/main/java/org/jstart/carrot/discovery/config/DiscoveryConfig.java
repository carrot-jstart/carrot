package org.jstart.carrot.discovery.config;

import org.jstart.carrot.discovery.IpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Carrot Discovery 客户端配置项。
 * <p>
 * 通过 {@code spring.cloud.carrot.discovery.*} 配置前缀注入，包含服务注册、心跳、元数据等参数。
 * </p>
 */
@ConfigurationProperties(prefix = "spring.cloud.carrot.discovery")
public class DiscoveryConfig {
    /**
     * carrot 地址
     */
    private String serverAddr;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * carrot命名空间
     */
    private String namespace;
    /**
     * carrot服务名
     */
    @Value("${spring.cloud.carrot.discovery.service:${spring.application.name:}}")
    private String service;
    /**
     * carrot权重
     */
    private float weight = 1.0F;

    /**
     * carrot集群名
     */
    private String group = "DEFAULT_GROUP";

    /**
     * carrot元数据
     */
    private Map<String, String> metadata = new HashMap<>();

    /**
     * carrot ip
     */
    private String ip = IpUtil.LOCAL_IP;

    /**
     * carrot 端口
     */
    private int port = -1;


    /**
     * @return Discovery Server 地址（通常为 host:port 或逗号分隔）
     */
    public String getServerAddr() {
        return serverAddr;
    }

    /**
     * @param serverAddr Discovery Server 地址
     */
    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    /**
     * @return 访问令牌（可为空）
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken 访问令牌（可为空）
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    /**
     * @return 命名空间
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace 命名空间
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return 服务名（默认取 spring.application.name）
     */
    public String getService() {
        return service;
    }

    /**
     * @param service 服务名
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return 权重
     */
    public float getWeight() {
        return weight;
    }

    /**
     * @param weight 权重
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * @return 集群/分组名
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group 集群/分组名
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return 元数据（键值对）
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata 元数据（键值对）
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * @return 实例 IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip 实例 IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return 实例端口
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port 实例端口
     */
    public void setPort(int port) {
        this.port = port;
    }
}
