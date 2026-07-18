package org.jstart.carrot.config.config;

import org.jstart.carrot.config.comm.ServerKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Carrot Config 客户端配置项。
 * <p>
 * 通过 {@code spring.cloud.carrot.config.*} 配置前缀注入。
 * </p>
 */
@ConfigurationProperties(prefix = "spring.cloud.carrot.config")
public class CarrotConfigProperties {
    private boolean refreshEnabled = true;
    private String serverAddr;
    private String accessToken;
    private List<ServerKey> files;
    private long watchIntervalMillis = 5000L;

    /**
     * @return 是否启用 Carrot Config
     */
    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    /**
     * @param refreshEnabled 是否启用 Carrot Config
     */
    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    /**
     * @return Config Server 地址列表（逗号分隔，形如 host:port）
     */
    public String getServerAddr() {
        return serverAddr;
    }

    /**
     * @param serverAddr Config Server 地址列表（逗号分隔，形如 host:port）
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
     * @return 需要加载/监听的配置文件列表（逗号分隔），支持 dataId@group 形式
     */
    public List<ServerKey> getFiles() {
        return files;
    }

    /**
     * @param files 需要加载/监听的配置文件列表（逗号分隔），支持 dataId@group 形式
     */
    public void setFiles(List<ServerKey> files) {
        this.files = files;
    }

    /**
     * @return watch 发送间隔（毫秒）
     */
    public long getWatchIntervalMillis() {
        return watchIntervalMillis;
    }

    /**
     * @param watchIntervalMillis watch 发送间隔（毫秒）
     */
    public void setWatchIntervalMillis(long watchIntervalMillis) {
        this.watchIntervalMillis = watchIntervalMillis;
    }
}
