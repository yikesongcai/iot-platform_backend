package com.atchensong.service.impl;

import com.atchensong.mapper.DeviceMapper;
import com.atchensong.pojo.Device;
import com.atchensong.service.DeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
}
