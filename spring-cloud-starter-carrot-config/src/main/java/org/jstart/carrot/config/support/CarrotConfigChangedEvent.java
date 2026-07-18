package org.jstart.carrot.config.support;

import org.jstart.carrot.config.comm.ServerKey;
import org.jstart.carrot.config.comm.Snapshot;
import org.springframework.context.ApplicationEvent;

/**
 * 配置变更事件。
 * <p>
 * 当某个配置 Key 的内容发生变化并完成重新拉取后发布，用于触发 Spring 环境更新/字段刷新等后续动作。
 * </p>
 */
public class CarrotConfigChangedEvent extends ApplicationEvent {
    private final ServerKey key;
    private final Snapshot oldSnapshot;
    private final Snapshot newSnapshot;

    /**
     * @param source      事件源
     * @param key         配置 Key
     * @param oldSnapshot 旧快照（可能为 null）
     * @param newSnapshot 新快照
     */
    public CarrotConfigChangedEvent(Object source,
                                    ServerKey key,
                                    Snapshot oldSnapshot,
                                    Snapshot newSnapshot) {
        super(source);
        this.key = key;
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
    }

    /**
     * @return 发生变更的配置 Key
     */
    public ServerKey getKey() {
        return key;
    }

    /**
     * @return 变更前的快照（可能为 null）
     */
    public Snapshot getOldSnapshot() {
        return oldSnapshot;
    }

    /**
     * @return 变更后的快照
     */
    public Snapshot getNewSnapshot() {
        return newSnapshot;
    }
}
