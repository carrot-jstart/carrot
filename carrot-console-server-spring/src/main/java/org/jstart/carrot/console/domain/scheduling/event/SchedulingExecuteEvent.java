package org.jstart.carrot.console.domain.scheduling.event;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
public class SchedulingExecuteEvent extends ApplicationEvent {

    /**
     * 主键
     */
    private String id;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 秘钥
     */
    private String secret;

    /**
     * 任务id
     */
    private String unitId;

    /**
     * 单位名称
     */
    private String unitName;



    public SchedulingExecuteEvent(Object source,
                                  String id,
                                  String ip,
                                  Integer port,
                                  String secret,
                                  String unitId,
                                  String unitName

    ) {
        super(source);
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.secret = secret;
        this.unitId = unitId;
        this.unitName = unitName;
    }
}
