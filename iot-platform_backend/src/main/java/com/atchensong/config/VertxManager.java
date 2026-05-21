package com.atchensong.config;

import io.vertx.core.Vertx;

public class VertxManager {

    private static  Vertx INSTANCE = Vertx.vertx();

    public static Vertx getVertx() {
        return INSTANCE;
    }
}
