package com.atchensong.service.impl;

import com.atchensong.mapper.UserMapper;
import com.atchensong.pojo.User;
import com.atchensong.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
