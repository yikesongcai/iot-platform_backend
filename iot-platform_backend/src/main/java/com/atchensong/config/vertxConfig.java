package com.atchensong.config;

import com.atchensong.mq.MqConsumer;
import com.atchensong.mq.MqProducer;
import com.atchensong.mq.vertx.VertxMqConsumer;
import com.atchensong.mq.vertx.VertxMqProducer;
import com.atchensong.pojo.DeviceMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class vertxConfig {

    @ConditionalOnMissingBean
    @Bean
    public MqConsumer<DeviceMessage> getMessageConsumer( ){
        return new VertxMqConsumer<>(DeviceMessage.class);
    }
    @ConditionalOnMissingBean
    @Bean
    public MqProducer<DeviceMessage> getMessageProducer(){
        return new VertxMqProducer<>(DeviceMessage.class);
    }
}
