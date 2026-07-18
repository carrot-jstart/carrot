package org.jstart.carrot.console.domain.admin;

import org.jstart.carrot.console.domain.admin.model.AdminNodeModel;


import java.util.List;

public interface AdminQueryDomainServer {

    /**
     * 获取适合的admin节点
     * @return
     * x： ip
     * y：端口
     * z：权重
     */
    List<AdminNodeModel> getFitAdminNode();

    /**
     * 安全校验是否通过
     */
    boolean authorized(String accessToken);
}
