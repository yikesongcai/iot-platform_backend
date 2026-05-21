package com.atchensong.controller;

import com.atchensong.common.DateFormatValidator;
import com.atchensong.common.R;
import com.atchensong.mapper.DeviceDataMapper;
import com.atchensong.pojo.DeviceData;
import com.atchensong.pojo.DeviceDataVO;
import com.atchensong.service.DataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/data")
public class DataController {

    @Autowired
    DataService dataService;
    @Autowired
    DeviceDataMapper mapper;

    @GetMapping("/history")
    public R<List<DeviceDataVO>> getHistory(
            @RequestParam Long deviceId,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false, defaultValue = "false") Boolean aggregate) {

        log.info("[设备历史数据检索] 设备ID={}, 消息类型={}", deviceId, messageType);

        // 1. 构建查询条件
        LambdaQueryWrapper<DeviceData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceData::getDeviceId, deviceId);

        // 添加消息类型过滤
        if (StringUtils.isNotBlank(messageType)) {
            queryWrapper.eq(DeviceData::getMessageType, messageType);
        }

        // 2. 处理时间范围
        LocalDateTime startTime;
        LocalDateTime endTime;

        if (StringUtils.isEmpty(start) || StringUtils.isEmpty(end)) {
            // 默认查询最近5天数据
            endTime = LocalDateTime.now();
            startTime = endTime.minusDays(5);
            log.info("[设备历史数据检索] 使用默认时间范围: {} 至 {}", startTime, endTime);
        } else {
            // 验证日期格式
            if (!(DateFormatValidator.isValidDateFormat(start) && DateFormatValidator.isValidDateFormat(end))) {
                log.error("[设备历史数据检索] 日期格式有误: start={}, end={}", start, end);
                return R.error("日期格式应为yyyy-MM-dd");
            }

            startTime = LocalDate.parse(start).atStartOfDay();
            endTime = LocalDate.parse(end).plusDays(1).atStartOfDay(); // 包含结束日期的全天数据
        }

        queryWrapper.between(DeviceData::getTimestamp, startTime, endTime);

        // 3. 执行查询
//        List<DeviceData> rawData = dataService.list(queryWrapper.orderByAsc(DeviceData::getTimestamp));
        List<DeviceData> rawData = mapper.selectByDeviceAndType(deviceId, messageType, startTime, endTime);
//        dataService
//        System.out.println(rawData.size());
//        for (DeviceData rawDatum : rawData) {
//            System.out.println(rawDatum);
//        }
        // 4. 数据处理
        List<DeviceDataVO> result;
        if (Boolean.TRUE.equals(aggregate)) {
            // 聚合模式 - 按小时平均
            result = aggregateByHour(rawData);
            log.info("[设备历史数据检索] 返回聚合数据，共{}条", result.size());
        } else {
            // 原始数据模式
            result = rawData.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            log.info("[设备历史数据检索] 返回原始数据，共{}条", result.size());
        }
        for (DeviceDataVO deviceDataVO : result) {
            System.out.println(deviceDataVO);
        }
        return R.success(result);
    }

    // 数据聚合方法 - 按小时平均
    private List<DeviceDataVO> aggregateByHour(List<DeviceData> rawData) {
        Map<LocalDateTime, List<DeviceData>> grouped = rawData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().truncatedTo(ChronoUnit.HOURS))
                );

        return grouped.entrySet().stream()
                .map(entry -> {
                    Double avgValue = entry.getValue().stream()
                            .collect(Collectors.averagingDouble(DeviceData::getValue));

                    DeviceDataVO vo = new DeviceDataVO();
                    vo.setId(entry.getValue().get(0).getId());
                    vo.setDeviceId(entry.getValue().get(0).getDeviceId());
                    vo.setStatus(entry.getValue().get(0).getStatus());
                    vo.setTimestamp(entry.getKey());
                    vo.setValue(avgValue);
                    vo.setUnit(entry.getValue().get(0).getUnit());
                    vo.setMessageType(entry.getValue().get(0).getMessageType());
                    return vo;
                })
                .sorted(Comparator.comparing(DeviceDataVO::getTimestamp))
                .collect(Collectors.toList());
    }

    // 数据转换方法
    private DeviceDataVO convertToVO(DeviceData data) {
        DeviceDataVO vo = new DeviceDataVO();
        vo.setTimestamp(data.getTimestamp());
        vo.setValue(data.getValue());
        vo.setUnit(data.getUnit());
        vo.setMessageType(data.getMessageType());
        vo.setStatus(data.getStatus());
        return vo;
    }

}
