package com.atchensong.service.impl;

import com.atchensong.mapper.WarningMapper;
import com.atchensong.pojo.Warning;
import com.atchensong.service.WarningService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WarningServiceImpl extends ServiceImpl<WarningMapper, Warning> implements WarningService {
}
