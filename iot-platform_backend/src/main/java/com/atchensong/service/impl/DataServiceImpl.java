package com.atchensong.service.impl;

import com.atchensong.mapper.DataMapper;
import com.atchensong.pojo.DeviceData;
import com.atchensong.service.DataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataServiceImpl extends ServiceImpl<DataMapper, DeviceData> implements DataService {
}
