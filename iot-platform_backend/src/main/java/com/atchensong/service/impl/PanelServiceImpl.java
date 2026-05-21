package com.atchensong.service.impl;

import com.atchensong.mapper.DeviceDataMapper;
import com.atchensong.mapper.IndexPanelMapper;
import com.atchensong.pojo.Device;
import com.atchensong.pojo.DeviceData;
import com.atchensong.pojo.IndexPanel;
import com.atchensong.service.DeviceService;
import com.atchensong.service.PanelService;
import com.atchensong.service.WarningService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PanelServiceImpl implements PanelService {
    private final DeviceDataMapper messageMapper;
    private final DeviceService deviceService;
    private final WarningService warningService;

    public IndexPanel getDashboardData() {
        IndexPanel panel = new IndexPanel();

        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getOnline,"online");
        // 基础数据
        panel.setTotalDevices((int) deviceService.count());
        panel.setOnlineDevices((int) deviceService.count(queryWrapper));
        panel.setUpMessages(Math.toIntExact(messageMapper.selectCount(null)));
        panel.setWarnMessages((int) warningService.count());
        panel.setCpuUsage(getCpuUsage());
        panel.setTimestamp(System.currentTimeMillis());

        // 新增48小时消息趋势
        panel.setMessageTrendLast48h(messageMapper.selectMessageTrendLast48Hours());

        return panel;
    }

    private Double getCpuUsage() {
        // 实际项目中从监控系统获取
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
        }
        return ThreadLocalRandom.current().nextDouble(5, 30);
    }

    @Override
    public boolean saveBatch(Collection<IndexPanel> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<IndexPanel> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<IndexPanel> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(IndexPanel entity) {
        return false;
    }

    @Override
    public IndexPanel getOne(Wrapper<IndexPanel> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<IndexPanel> getOneOpt(Wrapper<IndexPanel> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<IndexPanel> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<IndexPanel> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<IndexPanel> getBaseMapper() {
        return null;
    }

    @Override
    public Class<IndexPanel> getEntityClass() {
        return null;
    }
}
