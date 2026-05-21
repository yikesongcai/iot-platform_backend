package com.atchensong.service;

import com.atchensong.pojo.IndexPanel;
import com.atchensong.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PanelService  extends IService<IndexPanel> {
    IndexPanel getDashboardData();
}
