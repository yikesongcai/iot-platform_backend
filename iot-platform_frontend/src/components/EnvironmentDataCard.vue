<template>
  <el-card class="chart-card">
    <div class="chart-title">环境数据监测</div>
    <div class="environment-data-grid">
      <div v-for="item in dataItems" :key="item.type" class="data-item">
        <div class="data-icon" :style="{ backgroundColor: item.color }" >
          <img
              :src="item.icon"
              :alt="item.label"
              class="custom-icon"
          />
        </div>
        <div class="data-content">
          <div class="data-label">{{ item.label }}</div>
          <div class="data-value">
            {{ formatValue(item.value) }}<span class="data-unit">{{ item.unit }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
/// 1. 导入PNG图片
import Thermometer from '@/assets/img/Thermometer.png'
import Co2 from '@/assets/img/co2.png'
import Sunny from '@/assets/img/sunny.png'
import WaterDrop from '@/assets/img/WaterDrop.png'

import axios from 'axios'

const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: true
  }
})

const environmentData = ref({
  co2: null,
  temperature: null,
  light: null,
  soilMoisture: null,
  updateTime: null
})

const dataItems = ref([
  {
    type: 'temperature',
    label: '环境温度',
    value: null,
    unit: '°C',
    icon: Thermometer,
    color: '#FF6B6B'
  },
  {
    type: 'co2',
    label: 'CO₂浓度',
    value: null,
    unit: 'ppm',
    icon: Co2,
    color: '#4ECDC4'
  },
  {
    type: 'light',
    label: '光照强度',
    value: null,
    unit: 'lux',
    icon: Sunny,
    color: '#FFD166'
  },
  {
    type: 'soilMoisture',
    label: '土壤湿度',
    value: null,
    unit: '%',
    icon: WaterDrop,
    color: '#06AED5'
  }
])

const formatValue = (value) => {
  return value !== null ? value.toFixed(1) : '--'
}

const fetchData = async () => {
  try {
    const response = await axios.get('/api/latest')
    const data = response.data.data

    environmentData.value = data
    dataItems.value.forEach(item => {
      item.value = data[item.type]
    })
  } catch (error) {
    console.error('获取环境数据失败:', error)
  }
}

onMounted(() => {
  fetchData()
  if (props.autoRefresh) {
    setInterval(fetchData, 60000) // 每分钟自动刷新
  }
})
</script>

<style scoped>
/* 图标样式 */
.custom-icon {
  width: 24px;       /* 控制显示大小 */
  height: 24px;
  object-fit: contain; /* 保持比例 */
  filter: brightness(0) invert(1); /* 使图标变为白色 */
}

/* 图标容器 */
.data-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.environment-data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  height: 300px;
}

.data-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.data-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.data-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.data-content {
  flex: 1;
}

.data-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.data-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.data-unit {
  font-size: 14px;
  color: #999;
  margin-left: 4px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2e7d32;
}
</style>
