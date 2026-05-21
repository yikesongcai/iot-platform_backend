package com.atchensong.handler;

import com.atchensong.common.Constants;
import com.atchensong.mq.ConsumeHandler;
import com.atchensong.mq.MqConsumer;
import com.atchensong.pojo.DeviceMessage;
import com.atchensong.pojo.Warning;
import com.atchensong.service.WarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class WarningConsumeHandler implements ConsumeHandler<DeviceMessage> {

    @Autowired
    WarningService warningService;

    public WarningConsumeHandler(MqConsumer<DeviceMessage> mqConsumer) {
        mqConsumer.consumeMessage(Constants.DEVICE_WARNING_REPORT, this);
    }

    @Override
    public void handler(DeviceMessage deviceMessage) {
        log.warn("[EventBus]:consumer收到消息{}", deviceMessage);

        //将消息中的数据保存到数据库中
        warningService.save(new Warning(null
                , deviceMessage.getDeviceId()
                , deviceMessage.getMsg(), null
                , null));

        log.info("[dataService]:数据已存入数据库{}", deviceMessage);
    }
}
