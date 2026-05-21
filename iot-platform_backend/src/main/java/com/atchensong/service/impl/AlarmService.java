package com.atchensong.service.impl;

import com.atchensong.mapper.AlarmMessageMapper;
import com.atchensong.pojo.AlarmDetailDTO;
import com.atchensong.pojo.AlarmListDTO;
import com.atchensong.pojo.AlarmMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmMessageMapper alarmMessageMapper;

    // 获取告警列表
    public List<AlarmListDTO> getAlarmList() {
        return alarmMessageMapper.selectListByDeviceId();
    }

    // 获取告警详情
    public AlarmDetailDTO getAlarmDetail(Long id) {
        AlarmMessage alarm = alarmMessageMapper.selectById(id);
        if (alarm == null) {
            System.out.println("设备告警记录不存在");
            return null;
        }

        AlarmDetailDTO detail = new AlarmDetailDTO();
        BeanUtils.copyProperties(alarm, detail);
        return detail;
    }

    // 创建告警
    public Long createAlarm(AlarmMessage alarm) {
        alarmMessageMapper.insert(alarm);
        return alarm.getId();
    }

    //获取告警消息数量
    public Integer countAlarm(){
        return alarmMessageMapper.selectCountAlarm();
    }
}
