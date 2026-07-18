package org.jstart.carrot.scheduling;


import java.util.Objects;

/**
 * 调度 Admin 节点信息。
 * <p>
 * 用于描述当前可连接的调度管理端（IP、端口与访问令牌）。
 * </p>
 */
public class AdminNode {
    /**
     * 管理节点ip
     */
    private final String ip;

    /**
     * 管理节点端口
     */
    private final Integer port;

    /**
     * @param ip          管理节点 IP
     * @param port        管理节点端口
     */
    public AdminNode(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * @return 管理节点 IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return 管理节点端口
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 仅以 ip/port 判断节点是否相同。
     *
     * @param object 其他对象
     * @return 是否相同节点
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AdminNode adminNode = (AdminNode) object;
        return Objects.equals(ip, adminNode.ip) && Objects.equals(port, adminNode.port);
    }

    /**
     * 基于 ip/port 计算 hash。
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }


    @Override
    public String toString() {
        return "AdminNode{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
