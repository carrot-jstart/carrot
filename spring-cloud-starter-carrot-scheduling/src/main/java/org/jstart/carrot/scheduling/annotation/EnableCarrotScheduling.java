package org.jstart.carrot.scheduling.annotation;

import org.jstart.carrot.scheduling.config.CarrotSchedulingExecutorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Carrot Scheduling（任务调度）客户端能力。
 * <p>
 * 引入调度执行端所需的自动装配，并支持扫描 {@link CarrotJobUnit} 等自定义注解以注册任务单元。
 * </p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({CarrotSchedulingExecutorConfiguration.class})
public @interface EnableCarrotScheduling {
    /**
     * 指定需要扫描任务单元注解的包路径。
     *
     * @return 包路径列表
     */
    String[] scanBasePackages() default {};
}
