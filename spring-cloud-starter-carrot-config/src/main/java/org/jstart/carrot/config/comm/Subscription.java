package org.jstart.carrot.config.comm;

/**
 * 订阅
 */
public interface Subscription extends AutoCloseable {
    ServerKey key();

    Snapshot snapshot();

    @Override
    void close();
}