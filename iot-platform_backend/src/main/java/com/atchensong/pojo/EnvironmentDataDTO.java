package com.atchensong.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EnvironmentDataDTO {
    private Double co2;         // 二氧化碳含量(ppm)
    private Double temperature; // 环境温度(°C)
    private Double light;       // 光照强度(lux)
    private Double soilMoisture; // 土壤湿度(%)
    private LocalDateTime updateTime; // 数据更新时间
}
