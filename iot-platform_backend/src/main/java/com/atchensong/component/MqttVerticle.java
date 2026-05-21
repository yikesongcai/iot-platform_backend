package com.atchensong.component;

import com.atchensong.common.Constants;
import com.atchensong.mq.MqProducer;
import com.atchensong.pojo.Device;
import com.atchensong.pojo.DeviceMessage;
import com.atchensong.service.DeviceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.*;
import io.vertx.mqtt.impl.MqttClientImpl;
import io.vertx.mqtt.messages.MqttDisconnectMessage;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 缺少
 * */
@Slf4j
@Component
public class MqttVerticle extends AbstractVerticle {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private MqProducer<DeviceMessage> producer;
    private final Map<String, MqttEndpoint> endpointMap = new HashMap<>();

    @Override
    public void start() {
        MqttServerOptions options = new MqttServerOptions();
        options.setPort(1884);
        MqttServer mqttServer = MqttServer.create(vertx, options);

        mqttServer.endpointHandler(endpoint -> {//使用lambda表达式实现了endpointHandler方法，传入参数endpoint；
            //获取客户端的基本信息
            String deviceName = endpoint.auth().getUsername();
            String password = endpoint.auth().getPassword();
            String clientId = endpoint.clientIdentifier();

            log.info("[MqttServer]:MQTT 客户端 [{}] 请求连接, clean session = {}", clientId, endpoint.isCleanSession());

            if (deviceName != null && password != null) {
                log.info("[MqttServer]:[deviceName = [{}] , password = [{}]", deviceName, password);
            } else {
                log.warn("[MqttServer]:MQTT 客户端 {} 未通过验证", clientId);
                return;
            }

            log.info("[MqttServer]:[properties = " + endpoint.connectProperties() + "]");
            endpoint.accept(false);

            //获取客户端clientId的内容
            String[] arr = clientId.split("_");
            if (arr.length < 3) {
                log.warn("[MqttServer]:不合法的 client_id{}", clientId);
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID);
            }

            //Mqtt通信时topic规范
            // {productKey}_{deviceName}_{model}
            //例：test_Test01_m1

            //根据topic规范，pk应为clientId的第一部分字符串
            String productKey = arr[0];

            //根据topic规范,deviceName应为clientId的第二部分字符串
            if (!deviceName.equals(arr[1])) {
                log.warn("[MqttServer]:ClientId中所包含DeviceName与实际不符");
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID);
            }

            //根据productKey从数据库中查找设备实例 (productKey为设备注册时自动生成且唯一)
            LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<Device>();
            wrapper.eq(Device::getProductKey, productKey);
            Device device = deviceService.getOne(wrapper);

            //如果未找到，则拒绝客户端的连接
            if (device == null) {
                log.warn("[MqttServer]:incorrect productKey");
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID);
                return;
            }


            //若密码错误，则拒绝连接
            if (!device.getPassword().equals(password)) {
                log.warn("[MqttServer]:设备未通过验证");
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD);
                return;
            } else {

                //将新接入的设备维护到endpointMap中
                endpointMap.put(getEndpointKey(productKey, deviceName),endpoint);
//                mqttConnectPool.put(clientId, true);
                log.info("[MqttServer]:设备验证通过，设备上线");

                //将设备设置为在线状态
                device.setOnline("online");
                deviceService.updateById(device);
            }

            endpoint.subscribeHandler(subscribe -> {

                List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
                for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
                    System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                    reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
                }
                // ack the subscriptions request
                endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);

            });
            endpoint.unsubscribeHandler(unsubscribe -> {

                for (String t: unsubscribe.topics()) {
                    System.out.println("Unsubscription for " + t);
                }
                device.setOnline("offline");
                // ack the subscriptions request
                endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
            });

            endpoint.publishHandler((mqttPublishMessage) -> {
                PublishHandle(mqttPublishMessage, endpoint);
            });
            AtomicBoolean flag = new AtomicBoolean(true);
            endpoint.disconnectMessageHandler(mqttDisconnectMessage -> {
                flag.set(false);
                DisConnectHandle(mqttDisconnectMessage, clientId, deviceName, productKey);
            });


            endpoint.closeHandler((v) -> {
                if (flag.get()) {
                    CloseHandle(clientId, deviceName, productKey);
                }
            });


        });

        mqttServer.listen(ar -> {
            if (ar.succeeded()) {
                log.info("[MqttServer]:MQTT server is listening on port " + ar.result().actualPort());
            } else {
                log.info("[MqttServer]:Error on starting the server");
                ar.cause().printStackTrace();
            }
        });
    }


    /*
     * 下发消息到客户端
     *
     * */
    public void sendMessageToDevice(String productKey, String deviceName, String topic, String msg) {
        MqttEndpoint endpoint = endpointMap.get(getEndpointKey(productKey, deviceName));
        if (endpoint == null) {
            log.info("endpoint doesn't exist");
            return;
        }
        endpoint.publish(topic, Buffer.buffer(msg), MqttQoS.AT_LEAST_ONCE, false, false);
    }

    /*
     *  客户端消息处理器
     *
     * */
    private void PublishHandle(MqttPublishMessage publishMessage, MqttEndpoint endpoint) {

        String topic = publishMessage.topicName();
        String clientId = endpoint.clientIdentifier();
        String payload = publishMessage.payload().toString();

        log.info("[MqttServer]:检测到客户端{}在topic={}下发布消息{}", clientId, topic, payload);


        if (publishMessage.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            endpoint.publishAcknowledge(publishMessage.messageId());
        } else if (publishMessage.qosLevel() == MqttQoS.EXACTLY_ONCE) {
            endpoint.publishReceived(publishMessage.messageId());
        }

        //消息内容为空，返回
        if (payload.isEmpty()) {
            return;
        }

        //根据topic中的信息，从数据库中查找设备
        Device device = getDeviceFromTopic(topic);

        //设备不存在，返回
        if (device == null) {
            return;
        }

        //构建EventBus中设备消息模型
        DeviceMessage deviceMessage = new DeviceMessage(device.getDeviceId(), payload);
        if (topic.contains("post")) {
            log.info("[属性上报]topic中含有post字符,开始进行属性上报...");
            //发布消息
            log.info("[EventBus]:producer发布消息{}", deviceMessage);
            producer.publish(Constants.DEVICE_PROPERTY_REPORT, deviceMessage);
        }

        if (topic.contains("warning")) {
            log.info("[告警事件]:topic中含有warning字符,开始记录告警信息...");
            //发布告警
            log.info("[EventBus]:producer发布消息{}", deviceMessage);
            producer.publish(Constants.DEVICE_WARNING_REPORT, deviceMessage);
        }

        //处理器回收
        endpoint.publishReleaseHandler(endpoint::publishComplete);
    }


    /**
     * 设备意外断开处理
     */
    private void CloseHandle(String clientId, String deviceName, String productKey) {
        log.info("[客户端{}意外断开连接]:设备{}自动下线中...", clientId, deviceName);

        //在数据库中根据pk查找客户端对应的设备
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getProductKey, productKey);
        Device device = deviceService.getOne(wrapper);

        if (device == null) {
            log.info("[客户端意外断开连接]:数据库中未找到对应的设备{}", productKey);
            return;
        }
        //设备离线
        device.setOnline("offline");
        deviceService.updateById(device);
        log.info("[客户端意外断开连接]:设备DN={}已下线", deviceName);
    }

    /*
     * 设备主动断开连接处理
     * */
    private void DisConnectHandle(MqttDisconnectMessage disconnectMessage, String clientId, String
            deviceName, String productKey) {
        log.info("[客户端{}主动断开连接]:设备{}自动下线中...", clientId, deviceName);
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getProductKey, productKey);
        Device device = deviceService.getOne(wrapper);
        if (device == null) {
            log.error("[客户端主动断开连接]:数据库中未找到对应的设备{}", productKey);
            return;
        }
        //设备离线
        device.setOnline("offline");
        deviceService.updateById(device);
        log.info("[客户端主动断开连接]:设备DN={}已下线", deviceName);
    }


    public Device getDeviceFromTopic(String topic) {
        String[] topicParts = topic.split("/");
        if (topicParts.length < 5) {
            return null;
        }
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getProductKey, topicParts[2]);

        return deviceService.getOne(wrapper);
    }

    private String getEndpointKey(String productKey, String deviceName) {
        return String.format("%s_%s", productKey, deviceName);
    }


}
