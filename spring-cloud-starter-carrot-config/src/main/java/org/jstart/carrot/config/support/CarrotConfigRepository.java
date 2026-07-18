package org.jstart.carrot.config.support;

import org.jstart.carrot.config.comm.ServerKey;
import org.jstart.carrot.config.comm.Snapshot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carrot Config 的本地缓存仓库。
 * <p>
 * 以 {@link ServerKey} 作为索引，缓存远端配置的内容与元信息（md5、更新时间、contentType），
 * 供拉取/监听线程以及刷新逻辑复用。
 * </p>
 */
public class CarrotConfigRepository {
    private final ConcurrentHashMap<ServerKey, Snapshot> cache = new ConcurrentHashMap<>();

    /**
     * 获取指定 Key 的缓存快照。
     *
     * @param key 配置 Key
     * @return 缓存快照，不存在时返回 null
     */
    public Snapshot get(ServerKey key) {
        return key == null ? null : cache.get(key);
    }

    /**
     * 写入指定 Key 的缓存快照。
     *
     * @param key      配置 Key
     * @param snapshot 快照
     */
    public void put(ServerKey key, Snapshot snapshot) {
        if (key == null || snapshot == null) {
            return;
        }
        cache.put(key, snapshot);
    }

    /**
     * 获取当前缓存中的所有 Key（快照视图）。
     *
     * @return Key 列表
     */
    public List<ServerKey> keys() {
        return List.copyOf(cache.keySet());
    }

    /**
     * 获取当前缓存的不可变副本。
     *
     * @return Key->Snapshot 的 Map 副本
     */
    public Map<ServerKey, Snapshot> snapshot() {
        return Map.copyOf(cache);
    }


}
