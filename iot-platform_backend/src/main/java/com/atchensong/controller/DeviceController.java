package com.atchensong.controller;

import com.atchensong.common.R;
import com.atchensong.common.RandomUtil;
import com.atchensong.component.MqttVerticle;
import com.atchensong.pojo.Control;
import com.atchensong.pojo.Device;
import com.atchensong.pojo.IndexPanel;
import com.atchensong.service.ControlService;
import com.atchensong.service.DataService;
import com.atchensong.service.DeviceService;
import com.atchensong.service.WarningService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController()
@CrossOrigin
@Slf4j
@RequestMapping("device")
public class
DeviceController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private MqttVerticle mqttVerticle;

    @Autowired
    private ControlService controlService;

    @Autowired
    private DataService dataService;

    @Autowired
    private WarningService warningService;

    @PostMapping("/register")
    public R<String> registerDevice(@RequestBody Device device) {
        log.info("[设备注册]:{}", device);

        //生成唯一设备密钥
        device.setProductKey(RandomUtil.getRandomKey(device.getDeviceName(), device.getPassword()).toLowerCase());

        //调用数据库接口，将设备存入数据库
        boolean save = deviceService.save(device);

        if (save) {
            log.info("[设备注册]:成功...");
            return R.success("设备注册成功!");
        } else {
            log.error("[设备注册]:失败...");
            return R.error("设备注册失败!");
        }
    }

    @PutMapping("/update")
    public R<String> updateDevice(@RequestBody Device device) {
        log.info("[设备修改]:{}", device);
        boolean update = deviceService.updateById(device);
        if (update) {
            log.info("[设备修改]:成功...");
            return R.success("设备修改成功!");
        } else {
            log.error("[设备修改]:失败...");
            return R.error("设备修改失败");
        }
    }


    @PostMapping("/list")
    public R<List<Device>> list(@RequestBody Device device) {
        log.info("[设备检索]:检索条件{}", device);

        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<Device>();

        //若传入的设备名称不为空，则根据设备名称来检索
        queryWrapper.like(!device.getDeviceName().isBlank(), Device::getDeviceName, device.getDeviceName());
        //若传入的设备在线状态不为空，则根据在线状态来检索
        queryWrapper.eq(!device.getOnline().isBlank(), Device::getOnline, device.getOnline());
        //若传入的设备密钥不为空，则根据设备密钥来检索
        queryWrapper.eq(!device.getProductKey().isBlank(), Device::getProductKey, device.getProductKey());
        //若传入的设备标题不为空，则根据设备标题来检索
        queryWrapper.eq(!device.getTitle().isBlank(), Device::getTitle, device.getTitle());

        //根据设备的修改时间排降序
        queryWrapper.orderByDesc(Device::getOnline);

        List<Device> list = deviceService.list(queryWrapper);
        log.info("[设备检索]:检索到{}条数据", list.size());
        return R.success(list);


    }

    @GetMapping("/page")
    public R<Page<Device>> page(@RequestParam int page, @RequestParam int size) {
        Page<Device> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Device::getUpdateTime);
        Page<Device> pageResult = deviceService.page(pageParam, queryWrapper);
        return R.success(pageResult);
    }

    @DeleteMapping("/{id}")
    public R<Device> remove(@PathVariable Long id) {
        log.info("[设备删除]:删除ID={}", id);
        Device device = deviceService.getById(id);
        boolean b = deviceService.removeById(id);
        if (b) {
            log.info("[设备删除]:设备{}已删除", device);
            return R.success(device);
        }
        return R.error("删除失败！");
    }

    /*
    * 设备控制
    *
    * */
    @PostMapping("/{deviceId}/send")
    public R<Device> send(@PathVariable Long deviceId,@RequestBody String message) {
        System.out.println("[检测到对设备id]"+deviceId+"进行控制下发");
        Device device = deviceService.getById(deviceId);
        String topic = "/sys/" + device.getProductKey() + "/" + device.getDeviceName()+"/send";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String actionName = jsonNode.path("actionName").asText();
            String params = jsonNode.path("params").toString();
            System.out.println("[控制下发消息解析...]");
            System.out.println("[控制下发]actionName="+actionName);
            System.out.println("[控制下发]params="+params);
            Control control = new Control();
            control.setDeviceId(deviceId);
            control.setActionName(actionName);
            control.setContent(params);
            control.setTimestamp(LocalDateTime.now());
            System.out.println("[控制下发]消息持久化中...");
            controlService.save(control);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[执行控制下发]：deviceId="+deviceId);
        System.out.println("[执行控制下发]：message="+message);


        mqttVerticle.sendMessageToDevice(device.getProductKey(), device.getDeviceName(), topic, message);
        return R.success(device);
    }


    /**
     * 仪表盘
     * @return
     */
    @GetMapping("/panel")
    public R<IndexPanel> get(){
        IndexPanel indexPanel = new IndexPanel();
        //获取设备总数量
        indexPanel.setTotalDevices((int) deviceService.count());

        //获取在线设备总数量
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper();
        wrapper.eq(Device::getOnline,"online");
        indexPanel.setOnlineDevices((int) deviceService.count(wrapper));

        //获取消息数量
        indexPanel.setUpMessages((int) dataService.count());

        //获取告警消息数量
        indexPanel.setWarnMessages((int) warningService.count());
        return R.success(indexPanel);
    }

}


