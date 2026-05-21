package com.atchensong.mapper;

import com.atchensong.pojo.AlarmListDTO;
import com.atchensong.pojo.AlarmMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlarmMessageMapper extends BaseMapper<AlarmMessage> {

    @Select("SELECT id, device_name, alarm_name, alarm_level, alarm_time " +
            "FROM alarm_message " +
            "ORDER BY alarm_time DESC")
    List<AlarmListDTO> selectListByDeviceId();

    @Select("select count(*) from alarm_message order by alarm_time DESC")
    Integer selectCountAlarm();
}
