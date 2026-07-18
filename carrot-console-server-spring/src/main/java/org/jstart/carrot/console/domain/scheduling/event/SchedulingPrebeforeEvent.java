package org.jstart.carrot.console.domain.scheduling.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
@ToString
public class SchedulingPrebeforeEvent extends ApplicationEvent {

    /**
     * 开始时间
     */
    private long startSecondTime;
    /**
     * 时间片长度
     */
    private long timeSlice;
    /**
     * 时间片长度
     * @param source
     * @param startSecondTime
     * @param timeSlice
     */
    public SchedulingPrebeforeEvent(Object source, long startSecondTime, long timeSlice) {
        super(source);
        this.startSecondTime = startSecondTime;
        this.timeSlice = timeSlice;
    }
}
