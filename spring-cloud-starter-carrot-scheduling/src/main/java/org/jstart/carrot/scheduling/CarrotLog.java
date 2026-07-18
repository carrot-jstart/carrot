package org.jstart.carrot.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务执行日志采集器（线程内）。
 * <p>
 * 基于 {@link ThreadLocal} 在任务执行线程内记录日志片段，供任务回传时携带到 Admin 端展示。
 * </p>
 */
public class CarrotLog {
    private static final ThreadLocal<List<String>> SchedulingThreadLocal = new ThreadLocal<>();
    private static final Logger log = LoggerFactory.getLogger(CarrotLog.class);


    /**
     * 启动日志记录
     */
    public static void startLogRecord(){
        SchedulingThreadLocal.set(new ArrayList<>());
    }

    /**
     * 添加日志记录
     * @param record
     */
    public static void addLogRecord(String record){
        SchedulingThreadLocal.get().add(record);
    }

    /**
     * 结束日志
     */
    public static List<String> endLogRecord(){
        return SchedulingThreadLocal.get();
    }

    /**
     * 清除
     */
    public static void clear(){
        SchedulingThreadLocal.remove();
    }
}
