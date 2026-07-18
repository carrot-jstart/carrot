package org.jstart.carrot.config.comm;

@FunctionalInterface
public interface ConfigListener {
    void onChanged(ServerKey key,
                   Snapshot oldSnapshot,
                   Snapshot newSnapshot);
}

