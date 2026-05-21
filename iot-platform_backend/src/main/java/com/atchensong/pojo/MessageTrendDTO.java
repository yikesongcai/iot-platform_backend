package com.atchensong.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 消息趋势响应DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageTrendDTO {
    private String hour;       // 小时点，如 "08:00"
    private Long messageCount; // 消息数量
    private Long warnCount;    // 告警数量
}
