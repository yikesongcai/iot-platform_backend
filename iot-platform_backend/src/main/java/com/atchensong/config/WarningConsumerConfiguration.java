package com.atchensong.config;

import com.atchensong.handler.WarningConsumeHandler;
import com.atchensong.mq.MqConsumer;
import com.atchensong.pojo.DeviceMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarningConsumerConfiguration {
    @Bean
    public WarningConsumeHandler getWarningConsumer(MqConsumer<DeviceMessage> consumer) {
        return new WarningConsumeHandler(consumer);
    }
}
