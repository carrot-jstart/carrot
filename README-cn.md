# Carrot

Carrot 是一个面向 Spring Boot / Spring Cloud 场景的轻量级微服务治理组件集合，当前仓库包含以下能力：

- 配置中心：`spring-cloud-starter-carrot-config`
- 服务注册与发现：`spring-cloud-starter-carrot-discovery`
- 分布式任务调度：`spring-cloud-starter-carrot-scheduling`
- Dubbo 注册中心适配：`dubbo-spring-boot-starter`
- 管理控制台：`carrot-console-server-spring`
- 示例工程：`demo`
> **中文文档** | [English](./README.md)
> 
本文档以“怎么用”为主，依赖版本统一写为 `最新版本`，使用时请替换为当前最新发布版本。

## 目录结构

```text
carrot
├─ carrot-console-server-spring            # 后端控制台，提供配置/注册发现/调度管理能力
├─ console-admin-web                       # 前端管理页面源码
├─ spring-cloud-starter-carrot-config      # 配置中心客户端
├─ spring-cloud-starter-carrot-discovery   # 注册发现客户端
├─ spring-cloud-starter-carrot-scheduling  # 调度执行器客户端
├─ dubbo-spring-boot-starter               # Dubbo 与 Carrot Discovery 集成
├─ sql                                     # 控制台初始化 SQL
├─ proto                                   # grpc proto文件
└─ demo                                    # 演示示例
```

## 环境要求

- JDK 17 及以上
- Maven 3.9 及以上
- PostgreSQL 16 及以上
- Node.js 18 及以上，仅在开发 `console-admin-web` 时需要

## 5 分钟快速启动

### 1. 初始化数据库

创建一个 PostgreSQL 数据库，例如 `carrot`，然后执行：

```bash
psql -U postgres -d carrot -f sql/public.sql
```

### 2. 启动控制台

控制台默认：

- HTTP 端口：`8848`
- gRPC 端口：`9848`
- 默认访问令牌：`123456`

直接运行：

```bash
mvn -f console-server/pom.xml spring-boot:run
```

也可以通过环境变量覆盖默认配置：

```bash
SERVER_PORT=8848
SPRING_GRPC_SERVER_PORT=9090
SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/carrot?currentSchema=public
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
CARROT_ADMIN_ACCESS_TOKEN=123456
```

启动后访问：

- 控制台首页：`http://${console-server}:8848/`
- 配置、服务发现、调度能力均由同一个 `carrot-console-server-spring` 提供

建议在 `pom.xml` 中统一声明版本变量：

```xml
<properties>
    <carrot.version>最新版本</carrot.version>
</properties>
```

## 配置中心

### 引入依赖

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>spring-cloud-starter-carrot-config</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### 基础配置

```yaml
spring:
  application:
    name: config-demo
  cloud:
    carrot:
      config:
        server-addr: ${CARROT_CONFIG_SERVER_ADDR:127.0.0.1:9848}
        access-token: ${CARROT_CONFIG_ACCESS_TOKEN:123456}
        files:
          - namespace: public
            group: DEFAULT_GROUP
            data-id: test-demo.yml
        watch-interval-millis: 2000
        refresh-enabled: true
```

### 关键配置项

- `spring.cloud.carrot.config.server-addr`：配置中心 gRPC 地址，格式为 `host:port`
- `spring.cloud.carrot.config.access-token`：访问令牌
- `spring.cloud.carrot.config.files`：需要拉取和监听的配置文件列表
- `spring.cloud.carrot.config.watch-interval-millis`：配置变更轮询间隔，单位毫秒

### 使用说明

- 启动应用时会先拉取 `files` 中声明的配置
- 配置更新后客户端会持续监听并刷新本地值
- 示例可参考 `demo/config-demo`

## 服务注册与发现

### 引入依赖

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>spring-cloud-starter-carrot-discovery</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### 启用功能

```java
import org.jstart.carrot.discovery.annotation.EnableCarrotDiscovery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCarrotDiscovery
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

### 基础配置

```yaml
spring:
  application:
    name: discovery-demo
  cloud:
    carrot:
      discovery:
        server-addr: 127.0.0.1:9848
        access-token: 123456
        namespace: public
        group: DEFAULT_GROUP
        service: ${spring.application.name}
        ip: 127.0.0.1
        port: ${server.port}

server:
  port: 8085
```

### 关键配置项

- `spring.cloud.carrot.discovery.server-addr`：Discovery gRPC 地址，默认接 `console-server` 的 `9090`
- `spring.cloud.carrot.discovery.access-token`：访问令牌
- `spring.cloud.carrot.discovery.namespace`：命名空间
- `spring.cloud.carrot.discovery.group`：分组，默认 `DEFAULT_GROUP`
- `spring.cloud.carrot.discovery.service`：服务名，默认取 `spring.application.name`
- `spring.cloud.carrot.discovery.ip` / `port`：实例地址
- `spring.cloud.carrot.discovery.metadata`：实例元数据

### 使用说明

- 应用启动后会自动注册实例并定时发送心跳
- 关闭应用后，临时实例会自动过期
- 示例可参考 `demo/discovery-demo`

## 任务调度

### 引入依赖

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>spring-cloud-starter-carrot-scheduling</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### 启用功能

```java
import org.jstart.carrot.scheduling.annotation.EnableCarrotScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCarrotScheduling
public class JobApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}
```

### 基础配置

```yaml
spring:
  cloud:
    carrot:
      scheduling:
        admin:
          server-addr: 127.0.0.1:9848
          access-token: 123456
        executor:
          name: order-executor
          ip: 127.0.0.1
          port: 9966
          namespace: public
          group: default
          thread-pool-core-pool-size: 1
          thread-pool-max-pool-size: 4
          thread-pool-keep-alive-time: 60
          thread-pool-queue-capacity: 100
```

### 定义任务

```java
import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.springframework.stereotype.Component;

@Component
public class DemoJob {

    @CarrotJobUnit(value = "syncOrderJob", type = EJobUnitType.CORN, typeValue = "0 */1 * * * ?")
    public String syncOrder() {
        return "ok";
    }
}
```

### 关键配置项

- `spring.cloud.carrot.scheduling.admin.*`：执行器连接调度中心所需配置
- `spring.cloud.carrot.scheduling.executor.name`：执行器名称
- `spring.cloud.carrot.scheduling.executor.namespace`：命名空间
- `spring.cloud.carrot.scheduling.executor.group`：执行器分组
- `spring.cloud.carrot.scheduling.executor.port`：执行器对外通信端口

### 使用说明

- 启动后会自动向调度中心注册执行器
- 标注 `@CarrotJobUnit` 的方法会被扫描并注册为可调度任务
- 建议通过控制台统一管理任务启停、执行记录和运行日志

## Dubbo 集成

### 引入依赖

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### Provider 配置

```yaml
spring:
  application:
    name: dubbo-demo-provider

server:
  port: 8081

dubbo:
  application:
    name: ${spring.application.name}
    qos-enable: false
    register-mode: interface
  protocol:
    name: dubbo
    port: 20880
  provider:
    group: dubbo
  registry:
    address: carrot://192.168.0.23:9848
    check: false
    parameters:
      namespace: public
      accessToken: 123456
      group: DEFAULT_GROUP
```

### Consumer 配置

```yaml
spring:
  application:
    name: dubbo-demo-consumer

server:
  port: 8082

dubbo:
  application:
    name: ${spring.application.name}
    qos-enable: false
    register-mode: interface
  consumer:
    check: false
  registry:
    address: carrot://192.168.0.23:9848
    check: false
    parameters:
      namespace: public
      accessToken: 123456
      group: DEFAULT_GROUP
```

### 服务暴露与调用

Provider：

```java
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DemoServiceImpl implements DemoService {
}
```

Consumer：

```java
import org.apache.dubbo.config.annotation.DubboReference;

public class DemoController {

    @DubboReference
    private DemoService demoService;
}
```

### 使用说明

- `carrot://` 协议会通过 Carrot Discovery 完成服务注册与订阅
- `accessToken`、`namespace`、`group` 可直接写在注册中心地址参数中
- 示例可参考 `demo/dubbo-demo`

## 管理控制台

### 后端

后端模块为 `carrot-console-server-spring`，已经内置前端静态资源，通常只需要启动这一个服务即可。

打包：

```bash
mvn -f carrot-console-server-spring/pom.xml clean package
```

运行：

```bash
java -jar carrot-console-server-spring/target/console-server-*.jar
```

常用环境变量：

- `SERVER_PORT`：控制台 HTTP 端口，默认 `8848`
- `SPRING_GRPC_SERVER_PORT`：gRPC 端口，默认 `9848`
- `SPRING_DATASOURCE_URL`：数据库连接串
- `SPRING_DATASOURCE_USERNAME`：数据库用户名
- `SPRING_DATASOURCE_PASSWORD`：数据库密码
- `CARROT_ADMIN_ACCESS_TOKEN`：管理端访问令牌
- `CARROT_ADMIN_NAMESPACE`：管理端命名空间，默认 `carrot@admin`

### 前端开发

如果需要单独开发前端页面，可进入 `console-admin-web`：

```bash
cd console-admin-web
npm install
npm run dev
```

构建：

```bash
cd console-admin-web
npm run build
```

## Demo 说明

- `demo/config-demo`：演示配置拉取与监听
- `demo/discovery-demo`：演示应用实例注册与发现
- `demo/dubbo-demo`：演示 Dubbo Provider / Consumer 通过 Carrot 注册中心通信

建议先启动 `carrot-console-server-spring`，再运行各个 demo，这样可以直接在控制台观察配置、实例和任务状态。

## 常见使用建议

- 开发环境可直接使用默认令牌 `123456`，生产环境请务必替换
- 将 `namespace` 区分为 `dev`、`test`、`prod`，避免环境互串
- 建议让业务服务的 `spring.application.name` 与注册中心服务名保持一致
- 调度执行器名称建议按业务域划分，例如 `order-executor`、`billing-executor`

## 参考模块

- 配置中心示例：`demo/config-demo`
- 服务发现示例：`demo/discovery-demo`
- Dubbo 示例：`demo/dubbo-demo`
- 控制台后端：`carrot-console-server-spring`
- 前端源码：`console-admin-web`

## Q&A
- 邮箱: 1511529730.@qq.com
