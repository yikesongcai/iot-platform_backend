package com.atchensong.mapper;

import com.atchensong.pojo.DeviceData;
import com.atchensong.pojo.MessageTrendDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData> {

    // 自定义查询：获取设备在时间范围内的指定类型数据
    @Select("SELECT * FROM device_messages " +
            "WHERE device_id = #{deviceId} " +
            "AND `timestamp` BETWEEN #{start} AND #{end} " +
            "ORDER BY `timestamp`")
    List<DeviceData> selectByDeviceAndType(
            @Param("deviceId") Long deviceId,
            @Param("messageType") String messageType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 自定义查询：获取设备最新数据
    @Select("SELECT * FROM device_messages " +
            "WHERE device_id = #{deviceId} " +
            "ORDER BY timestamp DESC " +
            "LIMIT 1")
    DeviceData selectLatestData(@Param("deviceId") Long deviceId);


    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%H:00') AS hour, " +
            "COUNT(*) AS messageCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS warnCount " +
            "FROM device_messages " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 48 HOUR) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d %H:00') " +
            "ORDER BY hour")
    List<MessageTrendDTO> selectMessageTrendLast48Hours();




}
