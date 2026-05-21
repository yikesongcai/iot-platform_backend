<template>
  <div class="management-container">
    <div class="content-header">
      <h2>告警管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
<!--        <el-breadcrumb-item></el-breadcrumb-item>-->
        <el-breadcrumb-item>告警列表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="search-card">
      <div class="search-bar">
        <el-form :inline="true" :model="searchParams" class="search-form">
          <el-form-item label="设备名称">
            <el-input
                v-model="searchParams.deviceName"
                placeholder="请输入设备名称"
                clearable
            />
          </el-form-item>
          <el-form-item label="告警级别">
            <el-select
                v-model="searchParams.level"
                placeholder="请选择告警级别"
                clearable
            >
              <el-option label="一般" :value="0" />
              <el-option label="紧急" :value="1" />
              <el-option label="致命" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
                v-model="searchParams.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="searchAlarms">搜索</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card>
      <div class="table-actions">
        <el-button
            type="primary"
            :icon="Refresh"
            @click="refreshList"
            :loading="loading"
        >
          刷新
        </el-button>
        <el-button
            v-if="!userStore.isGuest"
            type="danger"
            :icon="Delete"
            @click="handleBatchDelete"
            :disabled="selectedAlarms.length === 0"
        >
          批量删除
        </el-button>
      </div>

      <el-table
          :data="alarmList"
          v-loading="loading"
          style="width: 100%"
          @row-click="handleRowClick"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="alarmName" label="告警名称" min-width="150" />
        <el-table-column prop="alarmTime" label="告警时间" width="180" sortable />
        <el-table-column prop="alarmLevelText" label="告警级别" width="120">
          <template #default="{ row }">
            <el-tag
                :type="getLevelTagType(row.alarmLevel)"
                effect="dark"
                class="level-tag"
            >
              {{ row.alarmLevelText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
                size="small"
                type="text"
                @click.stop="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
                v-if="!userStore.isGuest"
                size="small"
                type="text"
                @click.stop="handleDelete(row)"
                style="color: #f56c6c"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadAlarmData"
            @current-change="loadAlarmData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAlarmStore } from '@/stores/alarm'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const alarmStore = useAlarmStore()
const userStore = useUserStore()

// 加载数据
onMounted(() => {
  alarmStore.fetchAlarmList()
})

// 修正后的计算属性
const alarmList = computed(() => {
  return alarmStore.alarmList || []
})


// 搜索参数
const searchParams = ref({
  deviceName: '',
  level: null,
  timeRange: []
})

// 分页参数
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 选中项
const selectedAlarms = ref([])

// 加载数据
const loadAlarmData = async () => {
  try {
    const params = {
      page: pagination.value.current,
      pageSize: pagination.value.size,
      ...searchParams.value,
      startTime: searchParams.value.timeRange?.[0] || '',
      endTime: searchParams.value.timeRange?.[1] || ''
    }
    await alarmStore.fetchAlarmList(params)
    pagination.value.total = alarmStore.total
  } catch (error) {
    ElMessage.error('获取告警列表失败: ' + (error.message || '未知错误'))
  }
}

// 搜索告警
const searchAlarms = () => {
  pagination.value.current = 1
  loadAlarmData()
}

// 刷新列表
const refreshList = () => {
  searchParams.value = {
    deviceName: '',
    level: null,
    timeRange: []
  }
  loadAlarmData()
}

// 告警级别标签样式
const getLevelTagType = (level) => {
  switch(level) {
    case 2: return 'danger'
    case 1: return 'warning'
    default: return 'info'
  }
}

// 表格行选择变化
const handleSelectionChange = (selection) => {
  selectedAlarms.value = selection
}

// 查看详情
const handleViewDetail = (row) => {
  router.push(`/alarm/detail/${row.id}`)
}

// 点击行
const handleRowClick = (row) => {
  handleViewDetail(row)
}

// 删除单个告警
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除 "${row.alarmName}" 告警记录?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await alarmStore.deleteAlarm(row.id)
      ElMessage.success('删除成功')
      loadAlarmData()
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  const ids = selectedAlarms.value.map(item => item.id)
  ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条告警记录?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await alarmStore.batchDeleteAlarms(ids)
      ElMessage.success('批量删除成功')
      selectedAlarms.value = []
      loadAlarmData()
    } catch (error) {
      ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 初始化加载数据
onMounted(() => {
  loadAlarmData()
})
</script>

<style lang="less" scoped>
@import url("@/assets/css/management.css");

/* 告警列表特有样式 */
.level-tag {
  font-weight: 500;
  padding: 0 10px;
  height: 26px;
  line-height: 26px;
}

.table-actions {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.el-table {
  :deep(.el-table__row) {
    cursor: pointer;

    &:hover {
      background-color: #f5f7fa !important;
    }
  }

  :deep(.el-table__cell) {
    .cell {
      padding: 0 8px;
    }
  }
}

/* 操作按钮样式 */
.el-button--text {
  padding: 4px 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .table-actions {
    flex-wrap: wrap;
  }

  .el-table {
    :deep(th),
    :deep(td) {
      padding: 8px 0;
    }
  }
}
</style>
