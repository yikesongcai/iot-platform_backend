package com.atchensong.mq.vertx;

import com.atchensong.config.VertxManager;
import com.atchensong.mq.MqProducer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;

public class VertxMqProducer<T> implements MqProducer<T> {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final MqProducerVerticle<T> producerVerticle;
    @SneakyThrows
    public VertxMqProducer(Class<T> cls) {
        producerVerticle =new MqProducerVerticle<>(cls);
        VertxManager.getVertx().deployVerticle(producerVerticle,
                stringAsyncResult ->
                countDownLatch.countDown());
        countDownLatch.await();
    }

    @Override
    public void publish(String topic, T msg) {
        producerVerticle.publish(topic,msg);
    }
    public static class MqProducerVerticle<T> extends AbstractVerticle{
        private final Class<T> cls;
        private EventBus eventBus;

        public MqProducerVerticle(Class<T> cls) {
            this.cls = cls;
        }
        @Override
        public void start() {
            eventBus = vertx.eventBus();
            eventBus.registerCodec(new BeanCodec<>(cls));
        }
        public void publish(String topic,T msg){
            DeliveryOptions options =new DeliveryOptions().setCodecName(cls.getName());
            eventBus.publish(topic,msg,options);
        }

    }
}
