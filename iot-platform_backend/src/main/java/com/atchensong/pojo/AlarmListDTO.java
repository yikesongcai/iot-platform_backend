package com.atchensong.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmListDTO {
    private Long id;
    private String deviceName;
    private String alarmName;
    private Integer alarmLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;

    // 告警级别文字描述
    public String getAlarmLevelText() {
        switch (alarmLevel) {
            case 1: return "紧急";
            case 2: return "致命";
            default: return "一般";
        }
    }
}
