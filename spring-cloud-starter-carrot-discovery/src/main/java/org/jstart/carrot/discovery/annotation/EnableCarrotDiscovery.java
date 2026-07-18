package org.jstart.carrot.discovery.annotation;

import org.jstart.carrot.discovery.config.CarrotDiscoveryExecutorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Carrot Discovery 客户端能力。
 * <p>
 * 引入注册中心相关自动装配，使应用能够向 Discovery Server 注册、并订阅服务实例变更。
 * </p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({CarrotDiscoveryExecutorConfiguration.class})
public @interface EnableCarrotDiscovery {
}
