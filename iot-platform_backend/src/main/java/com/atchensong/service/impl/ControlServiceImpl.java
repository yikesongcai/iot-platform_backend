package com.atchensong.service.impl;

import com.atchensong.mapper.ControlMapper;
import com.atchensong.pojo.Control;
import com.atchensong.service.ControlService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ControlServiceImpl extends ServiceImpl<ControlMapper, Control> implements ControlService {
}
