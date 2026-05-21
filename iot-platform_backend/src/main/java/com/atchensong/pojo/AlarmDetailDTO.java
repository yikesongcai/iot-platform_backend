package com.atchensong.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AlarmDetailDTO {
    private Long id;
    private String deviceId;
    private String deviceName;
    private String alarmName;
    private Map<String, Object> alarmParams;
    private Integer alarmLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;

    // 告警级别样式类（用于前端显示不同颜色）
    public String getAlarmLevelClass() {
        switch (alarmLevel) {
            case 1: return "warning";
            case 2: return "danger";
            default: return "info";
        }
    }
}
