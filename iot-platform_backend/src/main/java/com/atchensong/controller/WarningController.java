package com.atchensong.controller;

import com.atchensong.common.R;
import com.atchensong.pojo.Warning;
import com.atchensong.service.WarningService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("warning")
@Slf4j
public class WarningController {

    @Autowired
    WarningService warningService;

    @GetMapping
    public R<List<Warning>> list() {
        return R.success(warningService.list());
    }

    @GetMapping("page")
    public R<Page<Warning>> page(@RequestParam int page, @RequestParam int size) {
        log.info("[分页查询]:page={},size={}", page, size);
        Page<Warning> warningPage = warningService.page(new Page<>(page, size));
        LambdaQueryWrapper<Warning> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Warning::getCreateTime);
        return R.success(warningPage);
    }
}
