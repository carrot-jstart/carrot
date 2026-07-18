package org.jstart.carrot.console.domain.config.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("config_item")
public class ConfigItemModel implements Serializable {
    @TableId(value = "id")
    private String id;

    @TableField(value = "namespace")
    private String namespace;

    @TableField(value = "group_name")
    private String groupName;

    @TableField(value = "data_id")
    private String dataId;

    @TableField(value = "content")
    private String content;

    @TableField(value = "md5")
    private String md5;

    @TableField(value = "content_type")
    private String contentType;

    @TableField(value = "create_time")
    private Long createTime;

    @TableField(value = "update_time")
    private Long updateTime;
}
