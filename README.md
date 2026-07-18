# Carrot

Carrot is a lightweight microservice governance component collection for Spring Boot / Spring Cloud scenarios. This repository includes the following capabilities:

- Configuration Center: `spring-cloud-starter-carrot-config`
- Service Registration & Discovery: `spring-cloud-starter-carrot-discovery`
- Distributed Task Scheduling: `spring-cloud-starter-carrot-scheduling`
- Dubbo Registry Adapter: `dubbo-spring-boot-starter`
- Admin Console: `carrot-console-server-spring`
- Demo Projects: `demo`

> **English** | [中文文档](./README-cn.md)

This document focuses on "how to use". Dependency versions are written as `latest version` — replace them with the latest released version when using.

## Directory Structure

```text
carrot
├─ carrot-console-server-spring            # Backend console, provides configuration / discovery / scheduling management
├─ console-admin-web                       # Frontend admin page source
├─ spring-cloud-starter-carrot-config      # Configuration center client
├─ spring-cloud-starter-carrot-discovery   # Service discovery client
├─ spring-cloud-starter-carrot-scheduling  # Scheduling executor client
├─ dubbo-spring-boot-starter               # Dubbo integration with Carrot Discovery
├─ sql                                     # Console initialization SQL
├─ proto                                   # gRPC proto files
└─ demo                                    # Demo examples
```

## Prerequisites

- JDK 17+
- Maven 3.9+
- PostgreSQL 16+
- Node.js 18+ (only required when developing `console-admin-web`)

## 5-Minute Quick Start

### 1. Initialize the Database

Create a PostgreSQL database, e.g., `carrot`, then run:

```bash
psql -U postgres -d carrot -f sql/public.sql
```

### 2. Start the Console

Console defaults:

- HTTP Port: `8848`
- gRPC Port: `9848`
- Default Access Token: `123456`

Run directly:

```bash
mvn -f console-server/pom.xml spring-boot:run
```

You can also override default configuration via environment variables:

```bash
SERVER_PORT=8848
SPRING_GRPC_SERVER_PORT=9090
SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/carrot?currentSchema=public
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
CARROT_ADMIN_ACCESS_TOKEN=123456
```

After starting, visit:

- Console home page: `http://${console-server}:8848/`
- Configuration, service discovery, and scheduling capabilities are all provided by the same `carrot-console-server-spring`

It is recommended to declare the version variable uniformly in `pom.xml`:

```xml
<properties>
    <carrot.version>latest version</carrot.version>
</properties>
```

## Configuration Center

### Add Dependency

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>spring-cloud-starter-carrot-config</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### Basic Configuration

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

### Key Configuration Items

- `spring.cloud.carrot.config.server-addr`: Configuration center gRPC address, format `host:port`
- `spring.cloud.carrot.config.access-token`: Access token
- `spring.cloud.carrot.config.files`: List of configuration files to pull and watch
- `spring.cloud.carrot.config.watch-interval-millis`: Configuration change polling interval, in milliseconds

### Usage

- On startup, the application fetches the configurations declared in `files`
- The client continuously watches for configuration updates and refreshes local values
- See `demo/config-demo` for an example

## Service Registration & Discovery

### Add Dependency

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>spring-cloud-starter-carrot-discovery</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### Enable Discovery

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

### Basic Configuration

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

### Key Configuration Items

- `spring.cloud.carrot.discovery.server-addr`: Discovery gRPC address, defaults to `console-server` port `9090`
- `spring.cloud.carrot.discovery.access-token`: Access token
- `spring.cloud.carrot.discovery.namespace`: Namespace
- `spring.cloud.carrot.discovery.group`: Group, defaults to `DEFAULT_GROUP`
- `spring.cloud.carrot.discovery.service`: Service name, defaults to `spring.application.name`
- `spring.cloud.carrot.discovery.ip` / `port`: Instance address
- `spring.cloud.carrot.discovery.metadata`: Instance metadata

### Usage

- The application automatically registers itself and sends heartbeats on startup
- Temporary instances expire automatically when the application shuts down
- See `demo/discovery-demo` for an example

## Task Scheduling

### Add Dependency

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>spring-cloud-starter-carrot-scheduling</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### Enable Scheduling

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

### Basic Configuration

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

### Define a Job

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

### Key Configuration Items

- `spring.cloud.carrot.scheduling.admin.*`: Configuration for connecting the executor to the admin console
- `spring.cloud.carrot.scheduling.executor.name`: Executor name
- `spring.cloud.carrot.scheduling.executor.namespace`: Namespace
- `spring.cloud.carrot.scheduling.executor.group`: Executor group
- `spring.cloud.carrot.scheduling.executor.port`: Executor communication port

### Usage

- The executor automatically registers with the admin console on startup
- Methods annotated with `@CarrotJobUnit` are scanned and registered as schedulable tasks
- It is recommended to manage task start/stop, execution records, and logs through the admin console

## Dubbo Integration

### Add Dependency

```xml
<dependency>
    <groupId>io.github.carrot-jstart</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>${carrot.version}</version>
</dependency>
```

### Provider Configuration

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

### Consumer Configuration

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

### Service Exposure & Invocation

Provider:

```java
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DemoServiceImpl implements DemoService {
}
```

Consumer:

```java
import org.apache.dubbo.config.annotation.DubboReference;

public class DemoController {

    @DubboReference
    private DemoService demoService;
}
```

### Usage

- The `carrot://` protocol completes service registration and subscription through Carrot Discovery
- `accessToken`, `namespace`, and `group` can be specified directly in the registry address parameters
- See `demo/dubbo-demo` for an example

## Admin Console

### Backend

The backend module is `carrot-console-server-spring`, which already bundles frontend static resources. In most cases, you only need to start this one service.

Build:

```bash
mvn -f carrot-console-server-spring/pom.xml clean package
```

Run:

```bash
java -jar carrot-console-server-spring/target/console-server-*.jar
```

Common environment variables:

- `SERVER_PORT`: Console HTTP port, default `8848`
- `SPRING_GRPC_SERVER_PORT`: gRPC port, default `9848`
- `SPRING_DATASOURCE_URL`: Database connection string
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `CARROT_ADMIN_ACCESS_TOKEN`: Admin access token
- `CARROT_ADMIN_NAMESPACE`: Admin namespace, default `carrot@admin`

### Frontend Development

To develop the frontend pages separately, navigate to `console-admin-web`:

```bash
cd console-admin-web
npm install
npm run dev
```

Build:

```bash
cd console-admin-web
npm run build
```

## Demo Overview

- `demo/config-demo`: Demonstrates configuration fetching and watching
- `demo/discovery-demo`: Demonstrates application instance registration and discovery
- `demo/dubbo-demo`: Demonstrates Dubbo Provider/Consumer communication via the Carrot registry

It is recommended to start `carrot-console-server-spring` first, then run the demos — this allows you to observe configuration, instances, and task status directly from the console.

## General Recommendations

- Use the default token `123456` only in development environments; be sure to replace it in production
- Segregate namespaces into `dev`, `test`, `prod` to avoid cross-environment interference
- Keep `spring.application.name` consistent with the service name in the registry
- Name scheduling executors by business domain, e.g., `order-executor`, `billing-executor`

## Reference Modules

- Configuration Center Example: `demo/config-demo`
- Service Discovery Example: `demo/discovery-demo`
- Dubbo Example: `demo/dubbo-demo`
- Console Backend: `carrot-console-server-spring`
- Frontend Source: `console-admin-web`

## Q&A

- Email: jstartcarrot@gmail.com
