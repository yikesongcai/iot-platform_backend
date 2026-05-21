package com.atchensong.component;

import com.atchensong.common.Constants;
import com.atchensong.mq.MqProducer;
import com.atchensong.pojo.DeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 设备模拟器：定时生成模拟传感器数据并通过EventBus发布，
 * 模拟温室中4种传感器（温度、湿度、CO2、光照）的持续数据上报。
 */
@Slf4j
@Component
@EnableScheduling
public class DeviceSimulator {

    @Autowired
    private MqProducer<DeviceMessage> producer;

    @Value("${simulator.interval-ms:30000}")
    private long intervalMs;

    @Value("${simulator.enabled:true}")
    private boolean enabled;

    private static final Long DEV_TEMP = 1L;
    private static final Long DEV_HUM = 2L;
    private static final Long DEV_CO2 = 3L;
    private static final Long DEV_LIGHT = 4L;

    private long baseTime = System.currentTimeMillis();

    @Scheduled(fixedDelayString = "${simulator.interval-ms:30000}")
    public void reportSensorData() {
        if (!enabled) return;

        try {
            // 用模拟时间（加速流逝）计算一天中的"相位"
            double phase = getDayPhase();

            reportTemperature(phase);
            reportHumidity(phase);
            reportCO2(phase);
            reportLight(phase);

        } catch (Exception e) {
            log.error("[DeviceSimulator]: 模拟上报异常", e);
        }
    }

    private double getDayPhase() {
        // 将真实时间映射为模拟的一天中的小时（0-24），加速60倍便于演示
        long elapsedMs = System.currentTimeMillis() - baseTime;
        double simulatedMinutes = (elapsedMs / 1000.0) * 60; // 60x加速
        double simulatedHour = (simulatedMinutes / 60) % 24;
        return simulatedHour;
    }

    private void reportTemperature(double hour) {
        // 26.5°C均值，±9°C振幅，白天高夜晚低
        double temp = 26.5 + 9 * Math.sin((hour - 8) * Math.PI / 12) + (Math.random() - 0.5) * 0.6;
        temp = Math.round(temp * 10.0) / 10.0;

        int status = temp > 33 ? 1 : 0;
        String msg = String.format("温度:%.1f℃", temp);
        DeviceMessage dm = new DeviceMessage(DEV_TEMP, msg);
        producer.publish(Constants.DEVICE_PROPERTY_REPORT, dm);

        if (status == 1) {
            log.warn("[DeviceSimulator]: 触发温度告警 {}", temp);
            DeviceMessage warn = new DeviceMessage(DEV_TEMP, String.format("警告:温度过高 %.1f℃", temp));
            producer.publish(Constants.DEVICE_WARNING_REPORT, warn);
        }
    }

    private void reportHumidity(double hour) {
        // 湿度与温度反相，50-90%RH
        double hum = 70 - 20 * Math.sin((hour - 8) * Math.PI / 12) + (Math.random() - 0.5) * 2;
        hum = Math.round(hum * 10.0) / 10.0;
        hum = Math.max(45, Math.min(95, hum));

        int status = hum < 55 ? 1 : 0;
        String msg = String.format("湿度:%.1f%%", hum);
        DeviceMessage dm = new DeviceMessage(DEV_HUM, msg);
        producer.publish(Constants.DEVICE_PROPERTY_REPORT, dm);

        if (status == 1) {
            log.warn("[DeviceSimulator]: 触发湿度告警 {}", hum);
            DeviceMessage warn = new DeviceMessage(DEV_HUM, String.format("警告:湿度过低 %.1f%%", hum));
            producer.publish(Constants.DEVICE_WARNING_REPORT, warn);
        }
    }

    private void reportCO2(double hour) {
        // CO2基线450ppm，夜间积累，白天通风降低
        double co2;
        if (hour >= 6 && hour <= 18) {
            co2 = 420 + Math.random() * 60; // 白天较低
        } else {
            co2 = 450 + 80 * Math.sin((hour - 20) * Math.PI / 10) + Math.random() * 30;
        }
        co2 = Math.round(co2);

        int status = co2 > 600 ? 1 : 0;
        String msg = String.format("CO2:%.0fppm", co2);
        DeviceMessage dm = new DeviceMessage(DEV_CO2, msg);
        producer.publish(Constants.DEVICE_PROPERTY_REPORT, dm);

        if (status == 1) {
            log.warn("[DeviceSimulator]: 触发CO2告警 {}", co2);
            DeviceMessage warn = new DeviceMessage(DEV_CO2, String.format("警告:CO2浓度过高 %.0fppm", co2));
            producer.publish(Constants.DEVICE_WARNING_REPORT, warn);
        }
    }

    private void reportLight(double hour) {
        // 光照：6-18点有光照，峰值60000lux
        double light;
        if (hour >= 6 && hour <= 18) {
            light = 60000 * Math.sin((hour - 6) * Math.PI / 12);
            light = Math.max(0, light);
            if (Math.random() < 0.2) light *= (0.3 + Math.random() * 0.4); // 20%概率阴天
        } else {
            light = Math.random() * 5;
        }
        light = Math.round(light);

        int status = 0;
        String msg = String.format("光照:%.0flux", light);
        DeviceMessage dm = new DeviceMessage(DEV_LIGHT, msg);
        producer.publish(Constants.DEVICE_PROPERTY_REPORT, dm);
    }
}
