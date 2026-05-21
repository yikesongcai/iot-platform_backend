package com.atchensong.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("设备数据返回对象")
public class DeviceDataVO {
    @ApiModelProperty("数据ID")
    private Long id;

    @ApiModelProperty("设备ID")
    private Long deviceId;

    @ApiModelProperty("消息类型")
    private String messageType;

    @ApiModelProperty("数值")
    private Double value;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("状态(0-正常,1-警告,2-故障)")
    private Integer status;

    @ApiModelProperty("数据时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @ApiModelProperty("位置编码")
    private String locationCode;

    @ApiModelProperty("信号强度")
    private Integer signalStrength;

    // 用于前端展示的格式化值
    @ApiModelProperty("格式化值")
    public String getFormattedValue() {
        if (value == null) return "";
        return String.format("%.2f %s", value, unit != null ? unit : "");
    }

    // 状态描述
    @ApiModelProperty("状态描述")
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "正常";
            case 1: return "警告";
            case 2: return "故障";
            default: return "未知";
        }
    }
}
