package com.atchensong.handler;

import com.atchensong.common.Constants;
import com.atchensong.mq.ConsumeHandler;
import com.atchensong.mq.MqConsumer;
import com.atchensong.pojo.DeviceData;
import com.atchensong.pojo.DeviceMessage;
import com.atchensong.service.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Slf4j
public class MessageConsumeHandler implements ConsumeHandler<DeviceMessage> {

    @Autowired
    DataService dataService;

    public MessageConsumeHandler(MqConsumer<DeviceMessage> mqConsumer) {
        mqConsumer.consumeMessage(Constants.DEVICE_PROPERTY_REPORT, this);
    }

    @Override
    public void handler(DeviceMessage deviceMessage) {
        log.info("[EventBus]:consumer收到消息{}", deviceMessage);
        Long deviceId = deviceMessage.getDeviceId();
        String messageType = determineMessageType(deviceMessage);
        Double value = parseValue(String.valueOf(deviceMessage));
        String unit = determineUnit(deviceMessage);
        Integer status = determineStatus(deviceMessage);
        DeviceData deviceData = new DeviceData(
                null, // id 自动生成
                deviceId, // device_id
                messageType, // message_type 根据消息内容判断类型
                value, // value 从消息中解析数值
                unit, // unit 根据消息类型确定单位
                status, // status 根据消息内容判断状态
                LocalDateTime.now(),// timestamp 使用当前时间
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //将消息中的数据保存到数据库中
        dataService.save(deviceData);


        log.info("[dataService]:数据已存入数据库{}", deviceMessage);
    }

    // 根据消息内容判断消息类型
    private String determineMessageType(DeviceMessage message) {
        String msg = message.getMsg().toLowerCase();
        if (msg.contains("temp")) return "temperature";
        if (msg.contains("hum")) return "humidity";
        if (msg.contains("co2")) return "co2";
        if (msg.contains("light")) return "light";
        return "unknown";
    }

    // 从消息中解析数值
    private Double parseValue(String msg) {
        try {
            // 假设消息格式如："温度:25.6℃" 或 "CO2:412ppm"
            String[] parts = msg.split(":");
            if (parts.length > 1) {
                String numStr = parts[1].replaceAll("[^0-9.]", "");
                return Double.parseDouble(numStr);
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // 确定单位
    private String determineUnit(DeviceMessage message) {
        String msg = message.getMsg().toLowerCase();
        if (msg.contains("℃") || msg.contains("c")) return "℃";
        if (msg.contains("%")) return "%RH";
        if (msg.contains("ppm")) return "ppm";
        if (msg.contains("lux")) return "lux";
        return "";
    }

    // 判断设备状态
    private Integer determineStatus(DeviceMessage message) {
        String msg = message.getMsg().toLowerCase();
        if (msg.contains("error") || msg.contains("故障")) return 2;
        if (msg.contains("warn") || msg.contains("警告")) return 1;
        return 0;
    }


}
