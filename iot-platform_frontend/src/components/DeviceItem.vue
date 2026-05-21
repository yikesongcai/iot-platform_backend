<template>
  <div class="device-item">
    <div class="top">
      <div class="left">
        <div class="left-block">
          <div class="deviceName">
            <h4>{{ device.title }}</h4>
          </div>
          <div>
            <span class="state">运行状态:
              <span :class="device.online === 'online' ? 'online' : 'offline'">{{ device.online === "online" ? "在线" : "离线" }}</span>
<!--                  {{device.online}}-->
            </span>
          </div>
        </div>
      </div>
      <div class="right">
        <img src="../assets/icon.png" alt="设备图标" class="device-image">
      </div>
    </div>
    <div class="footer">
      <el-button type="primary" @click="viewDetails">查看</el-button>
      <el-button type="success" @click="editDevice">编辑</el-button>
      <el-button type="warning" @click="showDeviceMessages">设备消息</el-button>
      <el-button type="danger" :icon="Delete" @click="confirmDeleteDevice" circle/>
    </div>

    <!-- 设备详情对话框 -->
    <el-dialog
        v-model="detailsDialogVisible"
        title="设备详情"
        width="600px"
        :modal="true"
        :close-on-click-modal="false"
        :append-to-body="true"
    >
      <div class="detail-content">
        <div class="detail-row">
          <span class="detail-label">ProductKey:</span>
          <span class="detail-value">{{ device.productKey }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">设备名称:</span>
          <span class="detail-value">{{ device.deviceName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">设备密码:</span>
          <span class="detail-value">{{ device.password }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">创建时间:</span>
          <span class="detail-value">{{ formattedCreateTime }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">更改时间:</span>
          <span class="detail-value">{{ formattedUpdateTime }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">在线状态:</span>
<!--          <span class="detail-value" :class="device.online ? 'online' : 'offline'">{{-->
<!--              device.online ? "在线" : "离线"-->
<!--            }}</span>-->
          <span class="detail-value">{{ device.online }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑设备对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑设备"
        width="600px"
        :modal="true"
        :close-on-click-modal="false"
        :append-to-body="true"
    >
      <el-form label-width="100px">
        <el-form-item label="设备名称:">
          <el-input v-model="editDeviceData.deviceName" placeholder="设备名称"/>
        </el-form-item>
        <el-form-item label="设备密码:">
          <el-input v-model="editDeviceData.password" placeholder="设备密码" type="password" show-password/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDevice">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设备消息对话框 -->
    <el-dialog
        v-model="deviceMessagesVisible"
        :title="`${device.title} - 数据监控`"
        width="800px"
        :modal="true"
        :close-on-click-modal="false"
        :append-to-body="true"
    >
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="数据趋势" name="report">
          <div class="chart-controls">
            <el-select v-model="selectedDataType" placeholder="选择数据类型" @change="fetchDeviceData">
              <el-option
                  v-for="type in dataTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
              />
            </el-select>
            <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="fetchDeviceData"
            />
          </div>
          <div id="messageChart" style="width: 100%; height: 350px;"></div>
          <div class="data-summary">
            <div class="summary-item">
              <span class="label">当前值:</span>
              <span class="value">{{ currentValue }} {{ currentUnit }}</span>
            </div>
            <div class="summary-item">
              <span class="label">平均值:</span>
              <span class="value">{{ averageValue }} {{ currentUnit }}</span>
            </div>
            <div class="summary-item">
              <span class="label">最大值:</span>
              <span class="value">{{ maxValue }} {{ currentUnit }}</span>
            </div>
            <div class="summary-item">
              <span class="label">最小值:</span>
              <span class="value">{{ minValue }} {{ currentUnit }}</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="控制下发" name="control">
          <el-form label-width="100px" style="margin-top: 20px;">
            <el-form-item label="动作名称:">
              <el-input v-model="controlForm.action" placeholder="请输入动作名称"></el-input>
            </el-form-item>
            <el-form-item label="动作参数:">
              <el-input v-model="controlForm.params" placeholder="请输入参数，如: {key:value}"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="sendControlCommand">下发指令</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>


<script setup>
import {ref, computed, onBeforeUnmount, nextTick} from 'vue';
import {Delete} from '@element-plus/icons-vue';
import {ElMessageBox, ElMessage, ElLoading} from 'element-plus';
import * as echarts from 'echarts';
import axios from 'axios';
import {useRouter} from "vue-router";


// Props定义
const props = defineProps({
  device: {
    type: Object,
    required: true,
    validator: (val) => {
      return ['deviceId', 'deviceName'].every(key => key in val)
    }
  }
})
const emit = defineEmits(['deviceDeleted'])
const router = useRouter()

// 状态管理
const detailsDialogVisible = ref(false);
const editDialogVisible = ref(false);
const deviceMessagesVisible = ref(false);
const activeTab = ref('report');
const editDeviceData = ref({
  deviceName: '',
  password: '',
});
const controlForm = ref({
  action: '',
  params: ''
});

// 图表相关
let messageChart = null;
const selectedDataType = ref('temperature');
const dateRange = ref([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()]);
const deviceData = ref([]);
const currentValue = ref(0);
const currentUnit = ref('');
const averageValue = ref(0);
const maxValue = ref(0);
const minValue = ref(0);

const dataTypes = [
  {value: 'temperature', label: '温度'},
  {value: 'humidity', label: '湿度'},
  {value: 'co2', label: '二氧化碳'},
  {value: 'light', label: '光照'}
];

// 计算属性
const formattedCreateTime = computed(() => formatDateTime(props.device.createTime));
const formattedUpdateTime = computed(() => formatDateTime(props.device.updateTime));

// 工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-');
};

const formatDate = (date) => {
  return date.toISOString().split('T')[0];
};

const getStatusDesc = (status) => {
  const statusMap = {
    0: '正常',
    1: '警告',
    2: '故障'
  };
  return statusMap[status] || '未知';
};

// 设备操作
const viewDetails = () => {
  detailsDialogVisible.value = true;
};

const editDevice = () => {
  editDeviceData.value = {
    deviceName: props.device.deviceName,
    password: props.device.password,
  };
  editDialogVisible.value = true;
};

const saveDevice = async () => {
  try {
    // 这里替换为实际API调用
    await new Promise(resolve => setTimeout(resolve, 500));
    ElMessage.success('设备信息更新成功');
    emit('deviceUpdated', {
      ...props.device,
      deviceName: editDeviceData.value.deviceName,
      password: editDeviceData.value.password
    });
    editDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('设备信息更新失败');
  }
};

const deleteDevice = async () => {
  try {
    // 确认对话框
    await ElMessageBox.confirm(
        `确定删除设备 "${props.device.deviceName}" 吗？`,
        '删除确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const deviceId = props.device.deviceId
    if (!deviceId) throw new Error('设备ID不存在')

    // API调用
    const response = await axios.delete(
        `/api/device/${encodeURIComponent(deviceId)}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          validateStatus: status => status === 200 || status === 404
        }
    )

    // 处理响应
    if (response.status === 404) {
      console.warn('设备不存在')
    }

    // 显示成功消息
    ElMessage.success({
      message: '设备删除成功',
      duration: 1000,
      onClose: () => {
        // 跳转前确保消息已显示
        router.push('/management').then(() => {
          // 可选：刷新设备列表数据
          if (router.currentRoute.value.path === '/management') {
            emit('deviceDeleted', deviceId)
          }
        })
      }
    })

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(
          error.response?.data?.message || '设备删除失败'
      )
    }
  }
}

// 控制下发方法
const sendControlCommand = async () => {
  let deviceId=props.device.deviceId
  // let msg = controlForm.params
  console.log("controlForm",controlForm)
  const loading = ElLoading.service({
    lock: true,
    text: '命令下发中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 1. 参数校验
    if (!deviceId) {
      throw new Error('设备ID不能为空')
    }
    console.log(deviceId)
    //
    // if (!commandData || typeof commandData !== 'object') {
    //   throw new Error('下发指令格式不正确')
    // }

    // 2. 构造请求数据
    // const payload = {
    //
    //   deviceId: deviceId,
    //   msg:
    //   // "msg": ,
    //   // "value": ,
    //   // "status":
    //
    // }

    const payload = {
      actionName: controlForm.value.action,
      params: controlForm.value.params
    }

    // 3. 发送请求
    const response = await axios.post(
        `/api/device/${deviceId}/send`,
        JSON.stringify(payload), // 确保字符串化
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          timeout: 10000 // 10秒超时
        }
    )

    // 4. 处理响应
    if (response.data?.code === 0) {
      ElMessage.success({
        message: '控制指令下发成功',
        duration: 3000,
        showClose: true
      })

      // 可以在这里触发设备状态刷新
      // emit('commandSent', deviceId)

      return response.data.data // 返回设备信息
    } else {
      throw new Error(response.data?.msg || '指令下发失败')
    }
  } catch (error) {
    console.error('控制指令下发失败:', error)

    let errorMessage = '指令下发失败'
    if (error.response) {
      // 请求已发出，服务器响应状态码非2xx
      errorMessage = `服务器错误: ${error.response.status}`
      if (error.response.data?.msg) {
        errorMessage = error.response.data.msg
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = '网络连接异常，请检查网络'
    }

    ElMessage.error({
      message: errorMessage,
      duration: 5000,
      showClose: true
    })

    throw error // 继续抛出错误供外部处理
  } finally {
    loading.close()
  }
}

const confirmDeleteDevice = () => {
  ElMessageBox.confirm(
      '此操作将永久删除该设备, 是否继续?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(deleteDevice).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 设备消息相关
const showDeviceMessages = async () => {
  try {
    deviceMessagesVisible.value = true;
    activeTab.value = 'report';
    await nextTick();
    await fetchDeviceData();
  } catch (error) {
    console.error('初始化设备消息失败:', error);
    ElMessage.error('初始化设备消息失败');
  }
};


// 数据获取和处理
const fetchDeviceData = async () => {
  try {
    const [start, end] = dateRange.value;
    const params = new URLSearchParams();

    params.append('deviceId', props.device.deviceId);
    params.append('messageType', selectedDataType.value);
    params.append('start', formatDate(start));
    params.append('end', formatDate(end));
    params.append('aggregate', 'true');

    const response = await axios.get('/api/data/history', {
      params,
      paramsSerializer: params => params.toString()
    });

    deviceData.value = (response.data.data || []).map(item => ({
      id: item.id || null,
      deviceId: item.deviceId || null,
      messageType: item.messageType || 'unknown',
      value: Number(item.value) || 0,
      unit: item.unit || '',
      status: item.status || 0,
      timestamp: item.timestamp || '',
      locationCode: item.locationCode || null,
      signalStrength: item.signalStrength || null,
      statusDesc: getStatusDesc(item.status),
      formattedValue: `${Number(item.value).toFixed(2)} ${item.unit || ''}`
    }));

    if (deviceData.value.length > 0) {
      calculateStats();
      updateChart();
    } else {
      ElMessage.warning('该时间段内没有数据');
    }
  } catch (error) {
    console.error('获取设备数据失败:', error);
    ElMessage.error('获取设备数据失败: ' + error.message);
  }
};

const calculateStats = () => {
  const values = deviceData.value.map(item => item.value);
  if (values.length === 0) return;

  currentValue.value = parseFloat(values[values.length - 1].toFixed(2));
  averageValue.value = parseFloat((values.reduce((a, b) => a + b, 0) / values.length).toFixed(2));
  maxValue.value = parseFloat(Math.max(...values).toFixed(2));
  minValue.value = parseFloat(Math.min(...values).toFixed(2));
  currentUnit.value = deviceData.value[0]?.unit || '';
};

// 图表处理
const updateChart = () => {
  const chartDom = document.getElementById('messageChart');
  if (!chartDom) return;

  // 初始化或更新图表
  if (!messageChart) {
    messageChart = echarts.init(chartDom);
  }

  const xData = deviceData.value.map(item => {
    const date = new Date(item.timestamp);
    return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
  });

  const yData = deviceData.value.map(item => item.value);

  const option = {
    animation: true,
    animationDuration: 2000,
    title: {
      text: `${props.device.title} - ${dataTypes.find(t => t.value === selectedDataType.value)?.label}趋势`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = deviceData.value[params[0].dataIndex];
        return `
          <div>时间: ${data.timestamp}</div>
          <div>数值: ${data.value} ${data.unit}</div>
          <div>状态: ${getStatusDesc(data.status)}</div>
        `;
      }
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: {lineStyle: {color: '#2e7d32'}}
    },
    yAxis: {
      type: 'value',
      name: dataTypes.find(t => t.value === selectedDataType.value)?.label +
          (selectedDataType.value === 'temperature' ? '(℃)' :
              selectedDataType.value === 'humidity' ? '(%RH)' :
                  selectedDataType.value === 'co2' ? '(ppm)' : '(lux)'),
      axisLine: {lineStyle: {color: '#2e7d32'}}
    },
    series: [{
      name: selectedDataType.value,
      type: 'line',
      smooth: true,
      data: yData,
      itemStyle: {color: '#2e7d32'},
      lineStyle: {width: 3},
      markPoint: {data: [{type: 'max'}, {type: 'min'}]},
      markLine: {data: [{type: 'average'}]}
    }],
    grid: {
      containLabel: true,
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%'
    }
  };

  messageChart.setOption(option);
};

// 清理
onBeforeUnmount(() => {
  if (messageChart) {
    messageChart.dispose();
    messageChart = null;
  }
});
</script>


<style scoped>
/* 新增样式 */
.chart-controls {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.data-summary {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.label {
  font-size: 14px;
  color: #666;
}

.value {
  font-size: 18px;
  font-weight: bold;
  color: #2e7d32;
  margin-top: 5px;
}

.device-item {
  padding: 16px;
  border-radius: 12px;
  margin: 1px;
  background-color: #fff;
  width: calc(33.333% - 16px);
  box-sizing: border-box;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.device-item:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex: 1;
}

.left-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.deviceName h4 {
  margin: 0;
  font-size: 18px;
  color: #2e7d32;
  font-weight: 600;
}

.state {
  font-size: 14px;
  color: #666;
}

.online {
  color: #67c23a;
  font-weight: 500;
}

.offline {
  color: #f44336;
  font-weight: 500;
}

.device-image {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid #ebeef5;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
  flex-wrap: wrap;
  gap: 8px;
}

.footer .el-button {
  flex: 1;
  min-width: 80px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.detail-label {
  width: 100px;
  font-weight: 500;
  color: #666;
}

.detail-value {
  flex: 1;
  color: #333;
}

.data-summary {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.label {
  font-size: 14px;
  color: #666;
}

.value {
  font-size: 18px;
  font-weight: bold;
  color: #2e7d32;
  margin-top: 5px;
}

/* 标签页样式 */
:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 1600px) {
  .device-item {
    width: calc(33.333% - 16px);
  }
}

@media (max-width: 1200px) {
  .device-item {
    width: calc(50% - 16px);
  }
}

@media (max-width: 768px) {
  .device-item {
    width: 100%;
  }

  .top {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .device-image {
    align-self: center;
  }
}
</style>
