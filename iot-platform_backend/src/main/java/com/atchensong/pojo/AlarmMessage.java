package com.atchensong.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("alarm_message")
public class AlarmMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceId;
    private String deviceName;
    private String alarmName;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> alarmParams;

    private Integer alarmLevel; // 0-一般, 1-紧急, 2-致命

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;
}
