package org.jstart.carrot.scheduling.annotation;

import org.jstart.carrot.scheduling.constant.EJobUnitType;

import java.lang.annotation.*;

/**
 * 标记一个可被 Carrot Scheduling 扫描与注册的调度单元。
 * <p>
 * 该注解通常标注在方法上，由框架在启动时解析并注册到执行端。
 * </p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CarrotJobUnit {
    /**
     * 调度单元名称
     */
    String value();

    /**
     * 调度类型
     */
    EJobUnitType type();

    /**
     * 调度值
     */
    String typeValue();
}
