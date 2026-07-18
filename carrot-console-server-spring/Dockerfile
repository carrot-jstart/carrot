FROM eclipse-temurin:17-jre-alpine

# 时区配置 (Alpine需单独安装tzdata)
#ENV TZ=Asia/Shanghai

# 基础JVM参数 (容器感知+内存限制)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=80.0 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heapdump.hprof"

# 独立GC调优参数 (根据内存大小动态选择GC)
ENV GC_OPTS="-XX:+UseG1GC \
             -XX:MaxGCPauseMillis=200 \
             -XX:InitiatingHeapOccupancyPercent=45 \
             -XX:G1ReservePercent=15 \
             -XX:+ParallelRefProcEnabled"

# 应用部署 (分层构建优化)
COPY carrot-console-server-spring.jar app.jar

#
# 启动命令 (使用exec形式保证信号传递)
EXPOSE 8848 9848 9966
#ENTRYPOINT exec java $JAVA_OPTS $GC_OPTS -jar app.jar

ENTRYPOINT exec java $JAVA_OPTS $GC_OPTS \
  -Dspring.cloud.bootstrap.location=file:/config/application.yml \
  -jar app.jar