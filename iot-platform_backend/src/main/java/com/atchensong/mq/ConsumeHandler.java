package com.atchensong.mq;

public interface ConsumeHandler<T> {
    void handler(T msg);
}
