package com.atchensong.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IndexPanel {
    private Integer totalDevices;
    private Integer onlineDevices;
    private Integer upMessages;
    private Integer warnMessages;

    @ApiModelProperty("CPU使用率(百分比)，-1表示获取失败")
    private Double cpuUsage;

    @ApiModelProperty("数据时间戳")
    private Long timestamp = System.currentTimeMillis();

    // 新增消息趋势数据
    private List<MessageTrendDTO> messageTrendLast48h;
}
