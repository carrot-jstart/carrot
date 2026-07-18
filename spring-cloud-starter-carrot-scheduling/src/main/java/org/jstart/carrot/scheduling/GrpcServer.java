package org.jstart.carrot.scheduling;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 调度执行端的 gRPC Server 封装。
 * <p>
 * 基于 Netty 启动 gRPC 服务，支持注册多个 {@link BindableService}，并在 JVM 关闭时优雅停止。
 * </p>
 */
public class GrpcServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);
    private final Integer port;
    private Server server;

    /**
     * @param port 监听端口
     */
    public GrpcServer(Integer port) {
        this.port=port;
    }

    /**
     * 启动 gRPC Server 并注册服务实现。
     *
     * @param services gRPC 服务实现列表
     * @return 是否启动成功
     * @throws IOException 启动失败时抛出
     */
    public boolean start(BindableService... services) throws IOException {
        if (server != null) {
            return true;
        }
        NettyServerBuilder builder=NettyServerBuilder.forPort(port);
        if (services == null || services.length == 0) {
            throw new IllegalArgumentException("services is empty");
        }
        Arrays.stream(services).filter(Objects::nonNull).forEach(builder::addService);
        server = builder.build().start();
        logger.info("Scheduling gRPC server started, listening on port {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered, stopping gRPC server...");
            try {
                GrpcServer.this.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while stopping gRPC server", e);
            }
        }, "carrot-scheduling-grpc-shutdown"));
        return true;
    }

    /**
     * 停止 gRPC Server（优雅停止，超时后强制停止）。
     *
     * @throws InterruptedException 等待停止时被中断
     */
    public synchronized void stop() throws InterruptedException {
        if (server == null) {
            return;
        }
        server.shutdown();
        if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
            server.shutdownNow();
            server.awaitTermination(30, TimeUnit.SECONDS);
        }
        server = null;
    }

    /**
     * 关闭资源，等价于 {@link #stop()}。
     *
     * @throws Exception 关闭时异常
     */
    @Override
    public void close() throws Exception {
        stop();
    }
}
