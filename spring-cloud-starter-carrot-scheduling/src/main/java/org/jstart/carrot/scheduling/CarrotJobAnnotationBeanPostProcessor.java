package org.jstart.carrot.scheduling;

import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
/**
 * {@link CarrotJobUnit} 注解扫描与注册器。
 * <p>
 * 在 Bean 初始化完成后扫描其方法上的 {@link CarrotJobUnit} 注解，并将任务单元注册到
 * {@link ExecutorServer}。
 * </p>
 */
public class CarrotJobAnnotationBeanPostProcessor implements BeanPostProcessor {

    private final ExecutorServer executorServer;

    /**
     * @param executorServer 执行端服务，用于注册任务单元
     */
    public CarrotJobAnnotationBeanPostProcessor(ExecutorServer executorServer) {
        this.executorServer = executorServer;
    }

    /**
     * Bean 初始化后回调：扫描方法注解并执行注册。
     *
     * @param bean     Spring Bean 实例
     * @param beanName Bean 名称
     * @return 原 Bean
     * @throws BeansException Spring Bean 异常
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Method[] methods = targetClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(CarrotJobUnit.class)) {
                // 注册 job
                executorServer.registerJob(beanName, bean, method, method.getAnnotation(CarrotJobUnit.class));
            }
        }
        return bean;
    }
}
