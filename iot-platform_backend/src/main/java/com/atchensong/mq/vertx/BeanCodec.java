package com.atchensong.mq.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanCodec<T> implements MessageCodec<T,T> {
    private final Class<T> beanType;

    public BeanCodec(Class<T> cls) {
        beanType = cls;
    }

    @Override
    public void encodeToWire(Buffer buffer, T t) {
        String json=Json.encode(t);
        Buffer encoded=Buffer.buffer(json);
        buffer.appendInt(encoded.length());
        buffer.appendBuffer(encoded);
    }

    @Override
    public T decodeFromWire(int position, Buffer buffer) {
        int length=buffer.getInt(position);
        position+=4;
        return Json.decodeValue(buffer.slice(position,position+length),beanType);
    }

    @Override
    public T transform(T t) {
        return t;
    }

    @Override
    public String name() {
        return beanType.getName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
