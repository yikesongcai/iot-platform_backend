package com.atchensong.mq.vertx;

import com.atchensong.config.VertxManager;
import com.atchensong.mq.ConsumeHandler;
import com.atchensong.mq.MqConsumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;

public class VertxMqConsumer<T> implements MqConsumer<T> {
    private final MqConsumerVerticle<T> consumerVerticle;
    private final CountDownLatch countDownLatch = new CountDownLatch(4);

    @SneakyThrows
    public VertxMqConsumer(Class<T> cls) {
        consumerVerticle =new MqConsumerVerticle<>(cls);
        for (int i = 0; i < 4; i++) {
            Vertx vertx= VertxManager.getVertx();
            vertx.deployVerticle(consumerVerticle,new DeploymentOptions().setWorker(true),
                    stringAsyncResult -> {
                        countDownLatch.countDown();
                    });
        }
    }

    @Override
    public void consumeMessage(String topic, ConsumeHandler<T> handler) {
        consumerVerticle.consume(topic,handler);
    }

    public static class MqConsumerVerticle<T> extends AbstractVerticle{
        private final Class<T> cls;
        private EventBus eventBus;

        public MqConsumerVerticle(Class<T> cls) {
            this.cls = cls;
        }

        @Override
        public void start() {
                eventBus=vertx.eventBus();
                eventBus.registerCodec(new BeanCodec<>(cls));
        }
        public void consume(String topic, ConsumeHandler<T> consumeHandler){
            MessageConsumer<T> messageConsumer=eventBus.consumer(topic);

            messageConsumer.handler(message->{
               consumeHandler.handler(message.body());
            });
        }
    }
}
