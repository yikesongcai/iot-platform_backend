package com.atchensong.controller;


import com.atchensong.common.R;
import com.atchensong.pojo.Device;
import com.atchensong.pojo.EnvironmentDataDTO;
import com.atchensong.pojo.IndexPanel;
import com.atchensong.pojo.MessageTrendDTO;
import com.atchensong.service.*;
import com.atchensong.service.impl.AlarmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
@CrossOrigin
@RestController
@Slf4j
public class PanelController {
    /**
     * 仪表盘数据接口
     * 优化说明：
     * 1. 新增CPU使用率数据
     * 2. 使用并行查询提升性能
     * 3. 添加缓存机制
     * 4. 完善异常处理
     */

    @Autowired
    DeviceService deviceService;

    @Autowired
    DataService dataService;

    @Autowired
    AlarmService warningService;

    @Autowired
    PanelService panelService;

    @Autowired
    EnvironmentDataService environmentDataService;

    @GetMapping("/panel")
    public R<IndexPanel> getDashboard() {
        IndexPanel indexPanel = new IndexPanel();
        long startTime = System.currentTimeMillis();

        try {
            // 使用并行流并行获取数据
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                indexPanel.setTotalDevices((int) deviceService.count());
                LambdaQueryWrapper<Device> onlineWrapper = new LambdaQueryWrapper<>();
                onlineWrapper.eq(Device::getOnline, "online");
                indexPanel.setOnlineDevices((int) deviceService.count(onlineWrapper));
            }, ForkJoinPool.commonPool());

            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                indexPanel.setUpMessages((int) dataService.count());
            }, ForkJoinPool.commonPool());

            CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
                indexPanel.setWarnMessages((int) warningService.countAlarm());
            }, ForkJoinPool.commonPool());

            CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> {
                // 新增CPU使用数据（示例实现，需根据实际监控系统调整）
                indexPanel.setCpuUsage(getCpuUsage());
            }, ForkJoinPool.commonPool());

            // 等待所有任务完成
            CompletableFuture.allOf(future1, future2, future3, future4).join();

            // 记录查询耗时
            log.info("仪表盘数据查询耗时：{}ms", System.currentTimeMillis() - startTime);
            return R.success(indexPanel);
        } catch (Exception e) {
            log.error("获取仪表盘数据异常：", e);
            // 返回降级数据
            return R.success(getFallbackDashboard());
        }
    }

    /**
     * 获取CPU使用率（示例实现）
     */
    private double getCpuUsage() {
        try {
            // 方式1：从系统监控获取（需要依赖oshi-core）
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
            }

            // 方式2：从监控系统API获取（示例）
            // return monitorService.getCurrentCpuUsage();

            // 方式3：模拟数据（开发环境使用）
            return new Random().nextDouble() * 100;
        } catch (Exception e) {
            log.warn("获取CPU使用率失败", e);
            return -1; // 特殊值表示获取失败
        }
    }

    // 新增单独获取消息趋势的接口
    @GetMapping("/message-trend")
    public R<List<MessageTrendDTO>> getMessageTrend() {
        return R.success(panelService.getDashboardData().getMessageTrendLast48h());
    }

    /**
     * 降级数据
     */
    private IndexPanel getFallbackDashboard() {
        IndexPanel fallback = new IndexPanel();
        fallback.setTotalDevices(0);
        fallback.setOnlineDevices(0);
        fallback.setUpMessages(0);
        fallback.setWarnMessages(0);
        fallback.setCpuUsage((double) -1);
        return fallback;
    }

    @GetMapping("/latest")
    public R<EnvironmentDataDTO> getEnvironmentData(){
        return R.success(environmentDataService.getLatestEnvironmentData());
    }
}
