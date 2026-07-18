package org.jstart.carrot.scheduling.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调度 Admin 连接配置项。
 * <p>
 * 通过 {@code spring.cloud.carrot.scheduling.admin.*} 注入，描述执行端如何找到并连接调度 Admin。
 * </p>
 */
@ConfigurationProperties(prefix = "spring.cloud.carrot.scheduling.admin")
public class SchedulingAdminConfig {

    private String serverAddr;

    /**
     * 直连方式的秘钥
     */
    private String accessToken;

    /**
     * 默认构造。
     */
    public SchedulingAdminConfig() {
    }

    /**
     * @param accessToken  直连接口秘钥
     */
    public SchedulingAdminConfig(String serverAddr, String accessToken){
        this.accessToken=accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getServerAddr() {
        return serverAddr;
    }
    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
}
