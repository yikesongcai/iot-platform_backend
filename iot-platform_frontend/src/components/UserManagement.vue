<template>
  <div class="management-container">
    <div class="content-header">
      <h2>用户管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
<!--        <el-breadcrumb-item>系统管理</el-breadcrumb-item>-->
        <el-breadcrumb-item>用户管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="search-card">
      <div class="search-bar">
        <el-form :inline="true" :model="searchParams" class="search-form">
          <el-form-item label="用户名">
            <el-input
                v-model="searchParams.username"
                placeholder="请输入用户名"
                clearable
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select
                v-model="searchParams.role"
                placeholder="请选择角色"
                clearable
            >
              <el-option label="管理员" value="admin" />
              <el-option label="操作员" value="operator" />
              <el-option label="观察员" value="viewer" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadUserData">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-button
            type="primary"
            :icon="CirclePlus"
            @click="handleAdd"
        >
          新增用户
        </el-button>
      </div>
    </el-card>

    <el-card>
      <el-table
          :data="userList"
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="{ background: '#f0f7f1', color: '#2e7d32' }"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" effect="dark">
              {{ roleTextMap[row.role] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                active-value="active"
                inactive-value="inactive"
                active-text=""
                inactive-text=""
                @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
                size="small"
                :icon="Edit"
                @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
                size="small"
                :icon="Delete"
                type="danger"
                @click="handleDelete(row)"
            >删除</el-button>
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
            @size-change="loadUserData"
            @current-change="loadUserData"
        />
      </div>
    </el-card>

    <!-- 用户对话框 -->
    <user-dialog
        v-model="dialogVisible"
        :current-user="currentUser"
        :mode="dialogMode"
        @submit="handleDialogSubmit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, CirclePlus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserDialog from '@/components/UserDialog.vue'
import axios from '@/utils/axios'

// 用户数据
const userList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentUser = ref(null)

// 分页参数
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 搜索参数
const searchParams = ref({
  username: '',
  role: ''
})

// 角色文本映射
const roleTextMap = {
  admin: '管理员',
  operator: '操作员',
  viewer: '观察员'
}

// 角色标签类型
const roleTagType = (role) => {
  const map = { admin: 'danger', operator: 'warning', viewer: 'success' }
  return map[role] || ''
}

// 加载用户数据
const loadUserData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.current,
      pageSize: pagination.value.size,
      ...searchParams.value
    }

    const res = await axios.get('/user/page', { params })
    const data = res.data || {}
    userList.value = data.records || data.list || []
    pagination.value.total = data.total || 0
  } catch (error) {
    ElMessage.error('获取用户列表失败: ' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await axios.put('/user/status', null, {
      params: { id: row.id, status: row.status }
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败: ' + (error.response?.data?.msg || error.message))
    // 恢复原状态
    row.status = row.status === 'active' ? 'inactive' : 'active'
  }
}

// 新增用户
const handleAdd = () => {
  currentUser.value = null
  dialogMode.value = 'add'
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  currentUser.value = { ...row }
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除用户 ${row.name}?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/user/${row.id}`)
      ElMessage.success('删除成功')
      loadUserData()
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.msg || error.message))
    }
  }).catch(() => {})
}

// 对话框提交处理
const handleDialogSubmit = async (formData) => {
  try {
    const isEdit = dialogMode.value === 'edit'
    const apiUrl = isEdit ? '/user' : '/user'
    const method = isEdit ? 'put' : 'post'

    await axios[method](apiUrl, formData)
    ElMessage.success(isEdit ? '用户更新成功' : '用户添加成功')
    dialogVisible.value = false
    loadUserData()
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.msg || error.message))
  }
}

// 初始化加载数据
onMounted(() => {
  loadUserData()
})
</script>

<style lang="less" scoped>
@import url("../assets/css/management.css");

/* 用户管理特有样式 */
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.status-badge {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-active {
  background-color: #67c23a;
}

.status-inactive {
  background-color: #f56c6c;
}

.role-tag {
  margin-right: 4px;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  gap: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .search-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .search-form :deep(.el-input),
  .search-form :deep(.el-select) {
    width: 100%;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
