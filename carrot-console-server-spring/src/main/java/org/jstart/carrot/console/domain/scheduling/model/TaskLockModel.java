package org.jstart.carrot.console.domain.scheduling.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("scheduling_task_lock")
public class TaskLockModel {
    /**
     * 锁id
     */
    @TableId(value = "value")
    private String value;
}
