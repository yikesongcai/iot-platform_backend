<template>
  <div class="dashboard-container">
    <div class="content-header">
      <h2>系统概览</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <!-- 新增CPU使用率卡片 -->
      <el-card shadow="hover" class="stats-card" v-if="panelData.cpuUsage !== undefined">
        <div class="card-content">
          <div class="card-icon" style="background-color: #e8f5e9;">
            <el-icon color="#2e7d32" :size="24"><Cpu /></el-icon>
          </div>
          <div class="card-info">
            <span class="card-title">CPU使用率</span>
            <span class="card-value">{{ formatCpuUsage(panelData.cpuUsage) }}</span>
            <span class="card-trend" :class="getCpuTrendClass(panelData.cpuUsage)">
              <el-icon><CaretTop v-if="panelData.cpuUsage > 60" /><CaretBottom v-else /></el-icon>
              {{ panelData.cpuUsage > 60 ? '高负载' : '正常' }}
            </span>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stats-card">
        <div class="card-content">
          <div class="card-icon" style="background-color: #e8f5e9;">
            <el-icon color="#2e7d32" :size="24"><Box /></el-icon>
          </div>
          <div class="card-info">
            <span class="card-title">总设备数量</span>
            <span class="card-value">{{ panelData.totalDevices || 0 }}</span>
            <span class="card-trend normal-trend">共 {{ panelData.totalDevices || 0 }} 台</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stats-card">
        <div class="card-content">
          <div class="card-icon" style="background-color: #e8f5e9;">
            <el-icon color="#2e7d32" :size="24"><Connection /></el-icon>
          </div>
          <div class="card-info">
            <span class="card-title">在线设备</span>
            <span class="card-value">{{ panelData.onlineDevices || 0 }}</span>
            <span class="card-trend normal-trend">在线率 {{ onlineRate }}%</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stats-card">
        <div class="card-content">
          <div class="card-icon" style="background-color: #e8f5e9;">
            <el-icon color="#2e7d32" :size="24"><DataLine /></el-icon>
          </div>
          <div class="card-info">
            <span class="card-title">上报消息</span>
            <span class="card-value">{{ panelData.upMessages || 0 }}</span>
            <span class="card-trend normal-trend">累计上报</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stats-card">
        <div class="card-content">
          <div class="card-icon" style="background-color: #e8f5e9;">
            <el-icon color="#2e7d32" :size="24"><Bell /></el-icon>
          </div>
          <div class="card-info">
            <span class="card-title">告警数量</span>
            <span class="card-value">{{ panelData.warnMessages || 0 }}</span>
            <span class="card-trend normal-trend">需关注</span>
          </div>
        </div>
      </el-card>


    </div>

    <!-- 图表区域 -->
    <div class="chart-area">
      <el-card class="chart-card">
        <div class="chart-title">设备类型分布</div>
        <div id="deviceTypeChart" style="width: 100%; height: 350px;"></div>
      </el-card>
      <environment-data-card />
    </div>
  </div>
</template>

<script setup>
import EnvironmentDataCard from '@/components/EnvironmentDataCard.vue'
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import {
  Box,
  Connection,
  Bell,
  DataLine,
  Cpu,
  CaretTop,
  CaretBottom
} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";

// 仪表盘数据
const panelData = ref({
  totalDevices: 0,
  onlineDevices: 0,
  upMessages: 0,
  warnMessages: 0,
  cpuUsage: 0
})

// 加载状态
const loading = ref(true)

// 修改fetchPanelData方法如下：
const fetchPanelData = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/panel')
    console.log(response.data)
    // 关键修复：正确访问嵌套的data字段
    if (response.data?.code === 0) {
      panelData.value = {
        totalDevices: response.data.data.totalDevices,
        onlineDevices: response.data.data.onlineDevices,
        upMessages: response.data.data.upMessages,
        warnMessages: response.data.data.warnMessages,
        cpuUsage: response.data.data.cpuUsage
      }
    } else {
      throw new Error(response.data?.msg || '接口返回异常')
    }

    initCharts()
  } catch (err) {
    console.error('获取数据失败:', err)
    ElMessage.error(`数据加载失败: ${err.message}`)
    // 可以设置默认值保证UI不崩溃
    panelData.value = {
      totalDevices: 0,
      onlineDevices: 0,
      upMessages: 0,
      warnMessages: 0,
      cpuUsage: 0
    }
  } finally {
    loading.value = false
  }
}

// 在线率
const onlineRate = computed(() => {
  if (!panelData.value.totalDevices || panelData.value.totalDevices === 0) return 0
  return ((panelData.value.onlineDevices / panelData.value.totalDevices) * 100).toFixed(0)
})

// 格式化CPU使用率
const formatCpuUsage = (usage) => {
  if (usage < 0) return 'N/A'
  return `${usage.toFixed(1)}%`
}

// CPU使用率趋势样式
const getCpuTrendClass = (usage) => {
  return usage > 80 ? 'danger-trend' : usage > 60 ? 'warn-trend' : 'safe-trend'
}

// 图表相关
let deviceTypeChart = null
let messageTrendChart = null

// 初始化图表
const initCharts = () => {
  initDeviceTypeChart()
  // initMessageTrendChart()
}

// 设备类型名称映射
const typeNameMap = {
  temperature: '温度传感器',
  humidity: '湿度传感器',
  co2: 'CO2传感器',
  light: '光照传感器'
}

// 初始化设备类型图表（从设备列表获取真实分布）
const initDeviceTypeChart = async () => {
  const chartDom = document.getElementById('deviceTypeChart')
  if (!chartDom) return

  if (!deviceTypeChart) {
    deviceTypeChart = echarts.init(chartDom)
  }

  try {
    const res = await axios.post('/api/device/list', {})
    if (res.data?.code === 0) {
      const devices = res.data.data || []
      const typeCount = {}
      devices.forEach(d => {
        const type = d.thingModel || 'unknown'
        typeCount[type] = (typeCount[type] || 0) + 1
      })
      const data = Object.entries(typeCount).map(([type, count]) => ({
        value: count,
        name: typeNameMap[type] || type
      }))
      deviceTypeChart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [{
          name: '设备类型',
          type: 'pie',
          data: data.length > 0 ? data : [{ value: 1, name: '暂无设备' }]
        }]
      })
    }
  } catch (e) {
    console.error('获取设备分布失败:', e)
  }
}

// // 初始化消息趋势图表（示例数据，需根据实际API调整）
// const initMessageTrendChart = () => {
//   const chartDom = document.getElementById('messageTrendChart')
//   if (!chartDom) return
//
//   messageTrendChart = echarts.init(chartDom)
//   const option = {
//     xAxis: {
//       type: 'category',
//       data: Array.from({ length: 24 }, (_, i) => `${i}:00`)
//     },
//     yAxis: { type: 'value' },
//     series: [{
//       data: Array.from({ length: 24 }, () => Math.random() * panelData.value.upMessages / 24),
//       type: 'line'
//     }]
//   }
//   messageTrendChart.setOption(option)
// }

// 处理窗口大小变化
const handleResize = () => {
  deviceTypeChart?.resize()
  messageTrendChart?.resize()
}

// 定时刷新数据
let refreshTimer = null
const setupAutoRefresh = () => {
  refreshTimer = setInterval(fetchPanelData, 30000) // 30秒刷新一次
}

onMounted(() => {
  fetchPanelData()
  window.addEventListener('resize', handleResize)
  setupAutoRefresh()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  deviceTypeChart?.dispose()
  messageTrendChart?.dispose()
  clearInterval(refreshTimer)
})
</script>

<style scoped>
@import url("../assets/css/dashboard.css");
</style>
