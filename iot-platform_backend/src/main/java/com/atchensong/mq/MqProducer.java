package com.atchensong.mq;

public interface MqProducer<T> {
    void publish(String topic,T msg);
}
