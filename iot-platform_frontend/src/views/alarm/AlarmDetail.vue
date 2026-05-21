<template>
  <div class="alarm-detail-container">
    <el-card class="detail-card">
      <!-- 头部返回按钮和标题 -->
      <template #header>
        <div class="card-header">
          <el-button
              type="primary"
              :icon="ArrowLeft"
              @click="goBack"
              size="small"
          >
            返回列表
          </el-button>
          <span class="detail-title">告警详情</span>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-loading="loading" class="detail-content">
        <!-- 错误提示 -->
        <el-alert
            v-if="error"
            :title="error"
            type="error"
            show-icon
            class="error-alert"
        />

        <!-- 详情内容 -->
        <div v-if="detailData" class="detail-info">
          <!-- 基本信息 -->
          <div class="info-section">
            <h3 class="section-title">基本信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="告警ID">{{ detailData.id }}</el-descriptions-item>
              <el-descriptions-item label="设备ID">{{ detailData.deviceId }}</el-descriptions-item>
              <el-descriptions-item label="设备名称">{{ detailData.deviceName }}</el-descriptions-item>
              <el-descriptions-item label="告警级别">
                <el-tag
                    :type="detailData.alarmLevelClass"
                    effect="dark"
                >
                  {{ detailData.alarmLevelText }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="告警名称">{{ detailData.alarmName }}</el-descriptions-item>
              <el-descriptions-item label="告警时间">{{ detailData.alarmTime }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 告警参数 -->
          <div class="info-section">
            <h3 class="section-title">告警参数</h3>
            <el-table
                :data="paramsTableData"
                border
                style="width: 100%"
                empty-text="无参数数据"
            >
              <el-table-column prop="key" label="参数名" width="180" />
              <el-table-column prop="value" label="参数值" />
            </el-table>
          </div>

          <!-- 原始数据 -->
          <div class="info-section">
            <h3 class="section-title">原始数据</h3>
            <el-input
                type="textarea"
                :rows="6"
                readonly
                v-model="rawJsonData"
                class="raw-data"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAlarmDetail } from '@/api/alarm'

const route = useRoute()
const router = useRouter()

// 数据状态
const detailData = ref(null)
const loading = ref(false)
const error = ref(null)

// 计算属性
const paramsTableData = computed(() => {
  if (!detailData.value?.alarmParams) return []
  return Object.entries(detailData.value.alarmParams).map(([key, value]) => ({
    key,
    value: typeof value === 'object' ? JSON.stringify(value) : value
  }))
})

const rawJsonData = computed(() => {
  return JSON.stringify(detailData.value, null, 2)
})

// 获取详情数据
const fetchDetail = async () => {
  try {
    loading.value = true
    error.value = null
    const response = await getAlarmDetail(route.params.id)
    detailData.value = response.data.data
  } catch (err) {
    error.value = err.message || '获取告警详情失败'
    console.error('获取告警详情失败:', err)
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/alarm/list')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>/* 告警详情容器 - 考虑左侧导航栏 */
.alarm-detail-container {
  padding: 24px 24px 24px 244px; /* 左侧留出导航栏宽度+间距 */
  margin-left: 0;
  background-color: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
  transition: padding-left 0.3s ease;
}

/* 导航栏收起状态 */
.sidebar-container.collapsed ~ .alarm-detail-container {
  padding-left: 84px; /* 缩小后的导航栏宽度+间距 */
}

/* 详情卡片 */
.detail-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: #fff;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
  background-color: #f8fafc;
  border-bottom: 1px solid #eaeef2;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
  flex-grow: 1;
}

/* 详情内容区域 */
.detail-content {
  padding: 24px;
}

/* 信息区块 */
.info-section {
  margin-bottom: 32px;
  background-color: #fff;
  border-radius: 6px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #4a5568;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #43bc87;
  margin-right: 12px;
  border-radius: 2px;
}

/* 参数表格样式 */
.params-table {
  margin-top: 12px;
}

.params-table :deep(.el-table__header-wrapper) th {
  background-color: #f7fafc !important;
  color: #4a5568;
  font-weight: 600;
}

.params-table :deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

.params-table :deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

/* 原始数据展示 */
.raw-data {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 14px;
  line-height: 1.5;
  background-color: #f8fafc;
  border-radius: 4px;
  padding: 12px;
  border: 1px solid #e2e8f0;
}

/* 标签样式优化 */
:deep(.el-tag) {
  font-weight: 500;
  letter-spacing: 0.3px;
  padding: 0 10px;
  height: 26px;
  line-height: 26px;
}

:deep(.el-tag--danger) {
  background-color: #fff5f5;
  border-color: #fed7d7;
  color: #f56565;
}

:deep(.el-tag--warning) {
  background-color: #fffaf0;
  border-color: #feebc8;
  color: #ed8936;
}

:deep(.el-tag--info) {
  background-color: #f8fafc;
  border-color: #e2e8f0;
  color: #718096;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .alarm-detail-container {
    padding-left: 224px;
  }

  .sidebar-container.collapsed ~ .alarm-detail-container {
    padding-left: 64px;
  }
}

@media (max-width: 992px) {
  .alarm-detail-container {
    padding: 20px;
  }

  .sidebar-container.collapsed ~ .alarm-detail-container {
    padding: 20px;
  }

  .detail-content {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .alarm-detail-container {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .info-section {
    padding: 12px;
  }

  .section-title {
    font-size: 15px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-card {
  animation: fadeInUp 0.3s ease-out;
}

.info-section {
  transition: all 0.3s ease;
}

.info-section:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

</style>
