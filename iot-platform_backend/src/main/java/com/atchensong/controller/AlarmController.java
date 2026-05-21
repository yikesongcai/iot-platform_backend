package com.atchensong.controller;

import com.atchensong.common.R;
import com.atchensong.pojo.AlarmDetailDTO;
import com.atchensong.pojo.AlarmListDTO;
import com.atchensong.pojo.AlarmMessage;
import com.atchensong.service.impl.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
@CrossOrigin
public class AlarmController {
    @Autowired
    private final AlarmService alarmService;

    // 获取告警列表
    @GetMapping("/list")
    public R<List<AlarmListDTO>> listAlarms() {
        System.out.println("[获取告警列表]...");
        return R.success(alarmService.getAlarmList());
    }

    // 获取告警详情
    @GetMapping("/detail/{id}")
    public R<AlarmDetailDTO> getAlarmDetail(
            @PathVariable Long id) {
        System.out.println("[获取告警详情]设备id="+id);
        AlarmDetailDTO alarmDetail = alarmService.getAlarmDetail(id);
        System.out.println(alarmDetail);
        return R.success(alarmDetail);
    }

    // 创建告警（模拟设备上报）
    @PostMapping("/create")
    public R<Long> createAlarm(
            @RequestBody AlarmMessage alarm) {
        return R.success(alarmService.createAlarm(alarm));
    }
}
