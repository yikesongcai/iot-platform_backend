<template>
  <div class="log-container">
    <div class="content-header">
      <h2>系统日志</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>系统日志</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="log-card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="日志类型">
            <el-select
                v-model="searchForm.logType"
                placeholder="请选择日志类型"
                clearable
            >
              <el-option label="全部" value="" />
              <el-option label="登录" value="登录" />
              <el-option label="设备注册" value="设备注册" />
              <el-option label="修改" value="修改" />
              <el-option label="删除" value="删除" />
              <el-option label="查询" value="查询" />
              <el-option label="新增" value="新增" />
              <el-option label="指令下发" value="指令下发" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
                v-model="searchForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志表格 -->
      <el-table
          :data="tableData"
          style="width: 100%"
          stripe
          v-loading="loading"
      >
        <el-table-column prop="time" label="时间" width="180" sortable />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getLogTypeStyle(row.type)" effect="plain">
              {{ getLogTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :small="false"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const searchForm = ref({ logType: '', timeRange: [] })
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getLogTypeStyle = (type) => {
  const map = { '登录': 'success', '删除': 'danger', '修改': 'warning', '设备注册': 'primary', '指令下发': 'info' }
  return map[type] || ''
}

const getLogTypeText = (type) => type

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value }
    if (searchForm.value.logType) params.logType = searchForm.value.logType
    const res = await axios.get('/api/system-log/page', { params })
    if (res.data?.code === 0) {
      const pageData = res.data.data
      tableData.value = (pageData.records || []).map(r => ({
        id: r.id,
        time: r.createTime,
        type: r.logType,
        content: r.content
      }))
      total.value = pageData.total || 0
    }
  } catch (e) {
    console.error('获取日志失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; fetchData() }
const resetSearch = () => { searchForm.value = { logType: '', timeRange: [] }; currentPage.value = 1; fetchData() }
const handleSizeChange = (val) => { pageSize.value = val; fetchData() }
const handleCurrentChange = (val) => { currentPage.value = val; fetchData() }
const handleDetail = (row) => { console.log('日志详情:', row) }

onMounted(() => { fetchData() })
</script>

<style scoped>
  @import url("../assets/css/systemlog.css");
</style>
