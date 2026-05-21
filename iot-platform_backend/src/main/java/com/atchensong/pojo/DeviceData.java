package com.atchensong.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("device_messages")
public class DeviceData {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 设备唯一标识
    @TableField("device_id")
    private Long deviceId;

    // 消息类型(如: temperature, humidity)
    @TableField("message_type")
    private String messageType;

    // 传感器数值
    private Double value;

    // 单位(如: ℃, %RH)
    private String unit;

    // 设备状态(0-正常, 1-警告, 2-故障)
    private Integer status;

    // 数据采集时间戳(设备上报时间)
    private LocalDateTime timestamp;

    // 记录创建时间(数据入库时间)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


//    public DeviceData(Object o, Long deviceId, String s, Double aDouble, String s1, Integer integer, LocalDateTime now) {
//    }
}
