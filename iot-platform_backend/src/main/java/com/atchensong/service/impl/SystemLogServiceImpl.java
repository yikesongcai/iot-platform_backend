package com.atchensong.service.impl;

import com.atchensong.mapper.SystemLogMapper;
import com.atchensong.pojo.SystemLog;
import com.atchensong.service.SystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {
}
