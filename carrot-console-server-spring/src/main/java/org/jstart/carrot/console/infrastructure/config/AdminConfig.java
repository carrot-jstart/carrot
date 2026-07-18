package org.jstart.carrot.console.infrastructure.config;


import org.jstart.carrot.console.comm.utils.IpUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "carrot.admin")
public class AdminConfig {

    /**
     *  ip
     */
    private String ip= IpUtil.LOCAL_IP;

    /**
     * 秘钥
     */
    private String  accessToken="";

    /**
     * 系统管理命名空间
     */
    private String AdminNamespace="carrot@admin";


    public AdminConfig(){}

    public AdminConfig(String ip,String accessToken,String AdminNamespace){
        this.ip=ip;
        this.accessToken=accessToken;
        this.AdminNamespace=AdminNamespace;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAdminNamespace() {
        return AdminNamespace;
    }

    public void setAdminNamespace(String AdminNamespace) {
        this.AdminNamespace = AdminNamespace;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
