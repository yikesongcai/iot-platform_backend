package com.atchensong;

import com.atchensong.component.MqttVerticle;
import com.atchensong.config.VertxManager;
import io.vertx.core.DeploymentOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@MapperScan("com.atchensong.mapper")

public class IotFinalTrainingApplication {

    @Autowired
    private MqttVerticle mqttVerticle;

    public static void main(String[] args) {
        SpringApplication.run(IotFinalTrainingApplication.class, args);
    }

    @PostConstruct
    public void start() {
        VertxManager.getVertx().deployVerticle(mqttVerticle, new DeploymentOptions());
        log.info("Succeed deployed mqttVerticle");
    }
}
