package com.atchensong.config;

import com.atchensong.handler.MessageConsumeHandler;
import com.atchensong.mq.MqConsumer;
import com.atchensong.pojo.DeviceMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConsumerConfiguration {
    @Bean
    public MessageConsumeHandler getDeviceMessageConsumer(MqConsumer<DeviceMessage> consumer) {
        return new MessageConsumeHandler(consumer);
    }
}
