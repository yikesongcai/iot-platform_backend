package com.atchensong.mapper;

import com.atchensong.pojo.EnvironmentDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface EnvironmentDataMapper {
    @Select("SELECT " +
            "(SELECT `value` FROM device_messages WHERE message_type = 'co2' ORDER BY `timestamp` DESC LIMIT 1) AS co2," +
            "(SELECT `value` FROM device_messages WHERE message_type = 'temperature' ORDER BY `timestamp` DESC LIMIT 1) AS temperature," +
            "(SELECT `value` FROM device_messages WHERE message_type = 'light' ORDER BY `timestamp` DESC LIMIT 1) AS light," +
            "(SELECT `value` FROM device_messages WHERE message_type = 'humidity' ORDER BY `timestamp` DESC LIMIT 1) AS soil_moisture," +
            "NOW() AS updateTime")
    EnvironmentDataDTO selectLatestEnvironmentData();

}
