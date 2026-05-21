package com.atchensong.controller;

import com.atchensong.common.R;
import com.atchensong.pojo.SystemLog;
import com.atchensong.service.SystemLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/system-log")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    @GetMapping("/page")
    public R<Page<SystemLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String logType) {
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        if (logType != null && !logType.isEmpty()) {
            wrapper.eq(SystemLog::getLogType, logType);
        }
        wrapper.orderByDesc(SystemLog::getCreateTime);
        Page<SystemLog> result = systemLogService.page(new Page<>(page, pageSize), wrapper);
        return R.success(result);
    }
}
