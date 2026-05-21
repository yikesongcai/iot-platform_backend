package com.atchensong.mq;


public interface MqConsumer<T> {
    void consumeMessage(String topic, ConsumeHandler<T> handler);
}
