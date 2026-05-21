package com.atchensong.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_log")
public class SystemLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String logType;
    private String content;
    private String operator;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE, exist = false)
    private LocalDateTime updateTime;
}
